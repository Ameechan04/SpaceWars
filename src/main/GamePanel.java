package main;

import entity.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GamePanel extends Pane {

    // screen settings
    final int ORIGINAL_TILE_SIZE = 32;
    public final float SCALE_FACTOR = 1.5F; // Renamed to avoid confusion with JavaFX Scale
    private long lastUpdateTime = 0;

    public StarMap starMap = new StarMap(this);

    private List<Entity> entities = new ArrayList<>();
    private ArrayList<StationaryEntity> stationaryEntities = new ArrayList<>();
    private ArrayList<Ship> ships = new ArrayList<>();

    public final int TILE_SIZE = (int) (ORIGINAL_TILE_SIZE * SCALE_FACTOR);
    int FPS = 60;

    KeyHandler keyHandler;
    MouseHandler mouseHandler;
    AnimationTimer gameThread;
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;

    public Image[] starOverlays = new Image[5];

    private double lastMouseX, lastMouseY;
    private boolean middleMouseDown = false;

    public Scout scout;
    public Scout scout2;
    public ColonyShip colonyShip, colonyShip2;
    public SmallSatellite satellite;
    public BasicShipyard basicShipyard;
    public Ship e1, e2, e3;

    public HumanPlayer humanPlayer = new HumanPlayer(this, 200);
    public ShipBuilderHelper shipBuilderHelper = new ShipBuilderHelper(this, Entity.Faction.PLAYER);
    public AIBrain ai = new AIBrain(this);
    public UI ui;
    public GameClock gameClock = new GameClock();
    List<CombatManager> activeCombats = new ArrayList<>();

    Color blueColour = Color.rgb(51, 63, 220);

    // ---- ZOOM & CAMERA VARIABLES ----
    final DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);
    public double minZoom = 0.5;
    public double maxZoom = 3.0;
    final DoubleProperty cameraOffsetXProperty = new SimpleDoubleProperty(0);
    final DoubleProperty cameraOffsetYProperty = new SimpleDoubleProperty(0);

    // JavaFX nodes for rendering
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Group worldGroup;

    public GamePanel() {
        this.setStyle("-fx-background-color: black;");
        this.setFocusTraversable(true);


        this.canvas = new Canvas();
        this.gc = canvas.getGraphicsContext2D();
        this.worldGroup = new Group();

        // Bind canvas size to pane size
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        // Add canvas and world group to the pane
        this.getChildren().addAll(worldGroup, canvas);

        // --- Mouse and Camera Handling ---
        this.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                middleMouseDown = true;
                lastMouseX = event.getX();
                lastMouseY = event.getY();
            }
        });

        this.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                middleMouseDown = false;
            }
        });

        this.setOnMouseDragged(event -> {
            if (middleMouseDown) {
                double dx = event.getX() - lastMouseX;
                double dy = event.getY() - lastMouseY;

                cameraOffsetXProperty.set(cameraOffsetXProperty.get() - dx / zoomProperty.get());
                cameraOffsetYProperty.set(cameraOffsetYProperty.get() - dy / zoomProperty.get());

                lastMouseX = event.getX();
                lastMouseY = event.getY();
            }
        });

        this.setOnScroll(event -> {
            double zoomFactor = 1.1;
            double prevZoom = zoomProperty.get();
            double newZoom = (event.getDeltaY() > 0) ? prevZoom * zoomFactor : prevZoom / zoomFactor;

            newZoom = Math.max(minZoom, Math.min(maxZoom, newZoom));

            double mouseX = event.getX();
            double mouseY = event.getY();

            cameraOffsetXProperty.set((cameraOffsetXProperty.get() + mouseX) * (newZoom / prevZoom) - mouseX);
            cameraOffsetYProperty.set((cameraOffsetYProperty.get() + mouseY) * (newZoom / prevZoom) - mouseY);

            zoomProperty.set(newZoom);
        });

        // --- Game Loop and Setup ---
        setUpGame();
        startGameLoop();
    }

    public void startGameLoop() {
        gameThread = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsedTime = (now - lastUpdateTime) / 1_000_000_000.0;
                if (lastUpdateTime == 0) { // First run
                    elapsedTime = 0;
                }
                lastUpdateTime = now;

                update(elapsedTime);
                repaint();
            }
        };
        gameThread.start();
    }

    public void setUpGame() {
        gameState = titleState;

        try {
            starMap.loadFromFile("res/maps/starmap.txt");
            loadStarOverlays();
            starMap.assignStarOverlays(starOverlays);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Star alpha = starMap.getStar("Alpha");
        createHomeworld(Entity.Faction.PLAYER, alpha);
        humanPlayer.addVisitedStar(alpha);

        basicShipyard = new BasicShipyard(this, alpha, Entity.Faction.PLAYER);

        Star Avalon = starMap.getStar("Avalon");
        createHomeworld(Entity.Faction.ENEMY, Avalon);
        ai.visitedStars.add(Avalon);

        for (int i = 0; i < 1; i++) {
            e2 = new Scout(this, alpha, Entity.Faction.PLAYER);
            e2.enterOrbit(alpha);
            humanPlayer.addShip(e2);
        }
        e1 = new ColonyShip(this, alpha, Entity.Faction.PLAYER);
        e1.enterOrbit(alpha);
        humanPlayer.addShip(e1);

        e1 = new ColonyShip(this, Avalon, Entity.Faction.ENEMY);
        e1.enterOrbit(Avalon);
        addShip(e1);
    }

    public void update(double elapsedTime) {
        gameClock.updateTime(elapsedTime);
        double currentGameDay = gameClock.getTotalGameDays();

        starMap.updateFlashingCircle();
        shipBuilderHelper.update();

        if (gameClock.isNewMonth()) updateMonthlyPlanetLogic();
        if (gameClock.isNewDay()) detectCombatAndAdvance();

        for (Star star : starMap.getStars()) {
            star.updateColonisation(gameClock, ui);
            star.updateCombatButton();
        }

        for (Entity entity : new ArrayList<>(entities)) {
            if (entity instanceof Ship ship) ship.update(currentGameDay);
            else entity.update();
        }

        ai.update();
        ui.updateMessages(elapsedTime);
    }

    private void detectCombatAndAdvance() {
        HashMap<Star, List<Entity>> starToPlayerEntities = new HashMap<>();
        HashMap<Star, List<Entity>> starToEnemyEntities = new HashMap<>();

        for (Entity e : getPlayerEntities()) {
            if (e.currentStar != null) starToPlayerEntities.computeIfAbsent(e.currentStar, k -> new ArrayList<>()).add(e);
        }
        for (Entity e : getEnemyEntities()) {
            if (e.currentStar != null) starToEnemyEntities.computeIfAbsent(e.currentStar, k -> new ArrayList<>()).add(e);
        }

        for (Star star : starToPlayerEntities.keySet()) {
            if (starToEnemyEntities.containsKey(star) && !star.hasCombat) {
                List<Entity> newPlayers = starToPlayerEntities.get(star);
                List<Entity> newEnemies = starToEnemyEntities.get(star);
                CombatManager manager = new CombatManager(this, star, newPlayers, newEnemies);
                activeCombats.add(manager);
                star.hasCombat = true;

                getPlayerEntities().removeAll(newPlayers);
                getEnemyEntities().removeAll(newEnemies);
            }
        }

        // Advance existing combats
        Iterator<CombatManager> iter = activeCombats.iterator();
        while (iter.hasNext()) {
            CombatManager combatManager = iter.next();
            combatManager.dailyCombat();
            if (combatManager.combatRecently) {
                combatManager.daysPassedSinceEnding++;
                if (combatManager.daysPassedSinceEnding > 30) {
                    combatManager.showResult = false;
                    combatManager.star.recentCombat = false;
                    combatManager.star.hasCombat = false;
                    iter.remove();
                }
            }
        }
    }

    private void repaint() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (gameState == titleState) {
            ui.draw(gc);
            return;
        }

        // --- Apply transformations to a "world" group instead of the canvas ---
        worldGroup.getTransforms().clear();
        worldGroup.getTransforms().addAll(
                new Translate(-cameraOffsetXProperty.get(), -cameraOffsetYProperty.get()),
                new Scale(zoomProperty.get(), zoomProperty.get())
        );

        // Drawing logic remains similar, but uses GraphicsContext
        starMap.draw(gc);

        for (StationaryEntity e : stationaryEntities) {
            if (e.hasVisibleOrbit) e.drawOrbit(gc);
        }

        drawOrbitingShips(gc);
        drawNonOrbitingEntities(gc);

        // Draw UI on a separate layer (directly on the canvas)
        ui.draw(gc);

        for (CombatManager combatManager : activeCombats) {
            combatManager.combatGUI.draw(gc);
        }
    }

    private void drawOrbitingShips(GraphicsContext gc) {
        Map<Star, Map<String, List<Ship>>> shipsByStarAndType = new HashMap<>();

        for (Ship ship : getShips()) {
            if (ship.inOrbit && ship.currentStar != null) {
                shipsByStarAndType
                        .computeIfAbsent(ship.currentStar, s -> new HashMap<>())
                        .computeIfAbsent(ship.name, n -> new ArrayList<>())
                        .add(ship);
            }
        }

        for (Star star : shipsByStarAndType.keySet()) {
            Map<String, List<Ship>> shipsByType = shipsByStarAndType.get(star);

            for (String type : shipsByType.keySet()) {
                List<Ship> group = shipsByType.get(type);

                for (int i = 0; i < group.size(); i++) {
                    Ship ship = group.get(i);

                    if (group.size() < 3 || i == 0) {
                        ship.clickable = true;
                        ship.draw(gc);
                    } else {
                        ship.clickable = false;
                    }
                }

                if (group.size() >= 3) {
                    gc.setFill(Color.WHITE);
                    gc.setFont(Font.font("Arial", 16));
                    Ship representative = group.get(0);
                    gc.fillText("x " + group.size(),
                            representative.exactCentreX + representative.solidArea.getWidth() / 2 + 5,
                            representative.exactCentreY + representative.solidArea.getHeight() / 2);
                }
            }
        }
    }

    private void drawNonOrbitingEntities(GraphicsContext gc) {
        for (Entity entity : new ArrayList<>(entities)) {
            if (!(entity instanceof Ship && ((Ship) entity).inOrbit)) {
                entity.draw(gc);
                if (entity.debug) {
                    entity.drawCentrePosition(gc);
                    entity.drawWorldXY(gc);
                }
            }
        }
    }

    public void loadStarOverlays() {
        for (int i = 0; i < 5; i++) {
            try {
                starOverlays[i] = new Image(getClass().getResourceAsStream("/planets/planet" + (i + 1) + ".png"));
            } catch (Exception e) {
                System.err.println("Failed to load overlay image: planet" + (i + 1) + ".png");
                e.printStackTrace();
            }
        }
    }

    // ---------- Entity Management ----------
    public List<Entity> getEntities() { return entities; }
    public List<Ship> getShips() { return ships; }

    public void addShip(Ship ship) {
        ships.add(ship);
        entities.add(ship);
    }

    public void addStationaryEntity(StationaryEntity e) {
        stationaryEntities.add(e);
        entities.add(e);
    }

    public void removeStationaryEntity(StationaryEntity e) {
        stationaryEntities.remove(e);
        entities.remove(e);
    }

    public void removeShip(Ship ship) {
        ships.remove(ship);
        entities.remove(ship);
    }

    public ArrayList<Ship> getShipsOrbitingStar(Star star) {
        ArrayList<Ship> result = new ArrayList<>();
        for (Ship s : ships) {
            if (s.currentStar == star && s.inOrbit && !s.moving) result.add(s);
        }
        return result;
    }

    public ArrayList<StationaryEntity> getStationaryEntities() { return stationaryEntities; }

    public void addMoney(int amount, Entity.Faction faction) {
        if (faction.equals(Entity.Faction.PLAYER)) {
            humanPlayer.addMoney(amount);
        } else {
            ai.balance += amount;
        }
    }

    public boolean spendMoney(int amount) {
        return humanPlayer.spendMoney(amount);
    }

    public void updateMonthlyPlanetLogic() {
        int[] totalIncomes = new int[2];
        for (Star star : starMap.getStars()) {
            if (star.colonised == Star.Colonised.COLONISED) {
                int populationGrowth = (int) star.getPopulationGrowthRate();
                star.population += populationGrowth;
                int income = (int) star.getMonthlyIncome();

                if (star.owner.equals(Entity.Faction.PLAYER)) {
                    totalIncomes[0] += income;
                } else {
                    totalIncomes[1] += income;
                }

                ui.addMessage("[" + star.owner + "] Monthly update at " + star.name + ": +" + populationGrowth + " population, +$"+income);
                if (star.owner.equals(Entity.Faction.ENEMY)) {
                    ai.send_debug_message("[" + star.owner + "] Monthly update at " + star.name + ": +" + populationGrowth + " population, +$"+income);;
                }
            } else if (star.colonised == Star.Colonised.BEGUN) {
                if (star.owner.equals(Entity.Faction.PLAYER)) {
                    totalIncomes[0] = -100;
                } else {
                    totalIncomes[1] = -100;
                }
            }
        }

        double[] totalUpkeep = new double[2];

        for (Entity e : getEntities()) {
            if (e.faction.equals(Entity.Faction.PLAYER)) {
                totalUpkeep[0] += ((double) e.buildCost / 100);
            } else {
                totalUpkeep[1] += ((double) e.buildCost / 100);
            }
        }

        humanPlayer.setRevenue((int) (totalIncomes[0] - totalUpkeep[0]));
        addMoney(humanPlayer.getRevenue(), Entity.Faction.PLAYER);

        ai.setRevenue((int) (totalIncomes[1] - totalUpkeep[1]));
        addMoney(ai.getRevenue(), Entity.Faction.ENEMY);

        ui.addMessage("[PLAYER] Revenue is: " + humanPlayer.getRevenue());
        ui.addMessage("[AI] Revenue is: " + ai.getRevenue());
        ai.send_debug_message("[AI] Revenue is: " + ai.getRevenue());
    }

    public List<Entity> getPlayerEntities() {
        return getEntities().stream().filter(s -> s.getFaction() == Entity.Faction.PLAYER).collect(Collectors.toList());
    }

    public List<Entity> getEnemyEntities() {
        return getEntities().stream().filter(s -> s.getFaction() == Entity.Faction.ENEMY).collect(Collectors.toList());
    }

    private void createHomeworld(Entity.Faction faction, Star star) {
        star.quality = Star.Quality.RICH;
        star.colonised = Star.Colonised.COLONISED;
        starMap.colonisedStars.add(star);
        star.owner = faction;
        star.population = 9_000_000_000L;
    }

    public Point2D screenToWorld(double screenX, double screenY) {
        double worldX = (screenX / zoomProperty.get()) + cameraOffsetXProperty.get();
        double worldY = (screenY / zoomProperty.get()) + cameraOffsetYProperty.get();
        return new Point2D((int) worldX, (int) worldY);
    }
}