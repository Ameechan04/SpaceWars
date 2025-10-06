package main;

import entity.*;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GamePanel extends Canvas {
    public MouseHandler mouseHandler;

    final int ORIGINAL_TILE_SIZE = 32;
    public final float SCALE = 1.5f;
    private long lastUpdateTime = System.nanoTime();

    public StarMap starMap = new StarMap(null); // pass GamePanelFX if needed in StarMap

    private List<Entity> entities = new ArrayList<>();
    private ArrayList<StationaryEntity> stationaryEntities = new ArrayList<>();
    private ArrayList<Ship> ships = new ArrayList<>();

    public final int TILE_SIZE = (int) (ORIGINAL_TILE_SIZE * SCALE);
    int FPS = 60;

    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;

    public Image[] starOverlays = new Image[5];

    private double lastMouseX, lastMouseY;
    private boolean middleMouseDown = false;

    public double zoom = 1.0;
    public double minZoom = 0.5;
    public double maxZoom = 3.0;
    public double cameraOffsetX = 0;
    public double cameraOffsetY = 0;

    public HumanPlayer humanPlayer = new HumanPlayer(null, 200);
    public ShipBuilderHelper shipBuilderHelper = new ShipBuilderHelper(null, Entity.Faction.PLAYER);
    public AIBrain ai = new AIBrain(null);
    public UI ui;
    public GameClock gameClock = new GameClock();
    List<CombatManager> activeCombats = new ArrayList<>();

    AnimationTimer gameLoop;

    private Pane rootPane;

    public GamePanel(double width, double height, Scene scene, Pane rootPane) {
        super(width, height);
        setupInput();
        startGameLoop();
        mouseHandler = new MouseHandler(this, scene);
        this.rootPane = rootPane;
        // Optionally, request focus so key events also work
        this.setFocusTraversable(true);
        this.requestFocus();

        ui = new UI(this, rootPane);
    }

    private void setupInput() {
        setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.MIDDLE) {
                middleMouseDown = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });

        setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.MIDDLE) {
                middleMouseDown = false;
            }
        });

        setOnMouseDragged(e -> {
            if (middleMouseDown) {
                double dx = e.getX() - lastMouseX;
                double dy = e.getY() - lastMouseY;

                cameraOffsetX -= dx / zoom;
                cameraOffsetY -= dy / zoom;

                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });

        setOnScroll((ScrollEvent e) -> {
            double zoomFactor = 1.1;
            double prevZoom = zoom;
            if (e.getDeltaY() > 0) zoom *= zoomFactor;
            else zoom /= zoomFactor;

            zoom = Math.max(minZoom, Math.min(maxZoom, zoom));

            // Zoom around mouse pointer
            cameraOffsetX = (cameraOffsetX + e.getX()) * (zoom / prevZoom) - e.getX();
            cameraOffsetY = (cameraOffsetY + e.getY()) * (zoom / prevZoom) - e.getY();
        });
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsedTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;
                update(elapsedTime);
                draw();
            }
        };
        gameLoop.start();
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
    }

    public void update(double elapsedTime) {
        double currentGameDay = gameClock.getTotalGameDays();

        starMap.updateFlashingCircle();
        shipBuilderHelper.update();

        if (gameClock.isNewMonth()) updateMonthlyPlanetLogic();

        if (gameClock.isNewDay()) detectCombatAndAdvance();

        for (Star star : starMap.getStars()) {
            star.updateColonisation(gameClock, ui);
            star.updateCombatButton();
        }

        for (Entity entity : entities) {
            if (entity instanceof Ship ship) ship.update(currentGameDay);
            else entity.update();
        }

        ai.update();
        ui.updateMessages(elapsedTime);
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());

        // transform for zoom and camera
        gc.save();
        gc.scale(zoom, zoom);
        gc.translate(-cameraOffsetX, -cameraOffsetY);

        for (Entity entity : entities) {
            entity.updateScreenPosition((int) cameraOffsetX, (int) cameraOffsetY, zoom);
        }

        starMap.draw(gc); // adapt StarMap.draw to accept GraphicsContext

        for (StationaryEntity e : stationaryEntities) {
            if (e.hasVisibleOrbit) e.drawOrbit(gc);
        }

        drawOrbitingShips(gc);
        drawNonOrbitingEntities(gc);

        gc.restore();

        ui.draw(gc);

        for (CombatManager combatManager : activeCombats) {
            combatManager.combatGUI.draw(gc); // adapt CombatGUI.draw to GraphicsContext
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
                        ship.draw(gc); // adapt Ship.draw to GraphicsContext
                    } else {
                        ship.clickable = false;
                    }
                }

                if (group.size() >= 3) {
                    gc.setFill(Color.WHITE);
                    gc.fillText("x " + group.size(),
                            group.get(0).exactCentreX + TILE_SIZE / 2 + 5,
                            group.get(0).exactCentreY + TILE_SIZE / 2);
                }
            }
        }
    }

    private void drawNonOrbitingEntities(GraphicsContext gc) {
        for (Entity entity : new ArrayList<>(entities)) {
            if (!(entity instanceof Ship && ((Ship) entity).inOrbit)) {
                entity.draw(gc); // adapt Entity.draw to GraphicsContext
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
                starOverlays[i] = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/planets/planet" + (i + 1) + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Entity> getPlayerEntities() {
        return entities.stream().filter(s -> s.getFaction() == Entity.Faction.PLAYER).collect(Collectors.toList());
    }

    public List<Entity> getEnemyEntities() {
        return entities.stream().filter(s -> s.getFaction() == Entity.Faction.ENEMY).collect(Collectors.toList());
    }

    public List<Ship> getShips() { return ships; }

    public void addShip(Ship ship) {
        ships.add(ship);
        entities.add(ship);
    }

    public void addStationaryEntity(StationaryEntity e) {
        stationaryEntities.add(e);
        entities.add(e);
    }

    public void removeShip(Ship ship) {
        ships.remove(ship);
        entities.remove(ship);
    }

    public void removeStationaryEntity(StationaryEntity e) {
        stationaryEntities.remove(e);
        entities.remove(e);
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

    public void addMoney(int amount, Entity.Faction faction) {
        if (faction.equals(Entity.Faction.PLAYER)) {
            humanPlayer.addMoney(amount);
        } else {
            ai.balance += amount;
        }
    }

    public List<Entity> getEntities() { return entities; }





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

    public Point2D screenToWorld(double screenX, double screenY) {
        double worldX = (screenX / zoom) + cameraOffsetX;
        double worldY = (screenY / zoom) + cameraOffsetY;
        return new Point2D(worldX, worldY);
    }
}
