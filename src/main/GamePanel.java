package main;
import entity.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GamePanel extends JPanel implements Runnable, MouseWheelListener {
    //screen settings
    final int ORIGINAL_TILE_SIZE = 32;
    public final float SCALE = 1.5F;
    private long lastUpdateTime = System.nanoTime();

    public StarMap starMap = new StarMap(this);

    private List<Entity> entities = new ArrayList<>();
    private ArrayList<StationaryEntity> stationaryEntities = new ArrayList<>();
    private ArrayList<Ship> ships = new ArrayList<>();

    public final int TILE_SIZE = (int) (ORIGINAL_TILE_SIZE * SCALE); //32 * SCALE
    int FPS = 60;

    KeyHandler keyHandler = new KeyHandler(this);
    MouseHandler mouseHandler = new MouseHandler(this);
    Thread gameThread;
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;

    public BufferedImage[] starOverlays = new BufferedImage[5];

    private int lastMouseX, lastMouseY;
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
    public UI ui = new UI(this);
    public GameClock gameClock = new GameClock();
    List<CombatManager> activeCombats = new ArrayList<>();

    Color blueColour = new Color(51, 63, 220);

    // ---- ZOOM VARIABLES ----
    public double zoom = 1.0;
    public double minZoom = 0.5;
    public double maxZoom = 3.0;
    public int cameraOffsetX = 0;
    public int cameraOffsetY = 0;

    public GamePanel() {
        this.setBackground(new Color(0, 0, 0));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.addMouseListener(mouseHandler);
        this.addMouseWheelListener(this);
        this.setFocusable(true);
        requestFocusInWindow();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    middleMouseDown = true;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    middleMouseDown = false;
                }
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (middleMouseDown) {
                    int dx = e.getX() - lastMouseX;
                    int dy = e.getY() - lastMouseY;

                    // Adjust camera offset; divide by zoom to keep drag consistent
                    cameraOffsetX -= dx / zoom;
                    cameraOffsetY -= dy / zoom;

                    lastMouseX = e.getX();
                    lastMouseY = e.getY();

                    repaint();
                }
            }
        });
    }

    // ---- ZOOM HANDLING ----
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        double zoomFactor = 1.1;

        double prevZoom = zoom;

        if (notches < 0) zoom *= Math.pow(zoomFactor, -notches);
        else zoom /= Math.pow(zoomFactor, notches);

        zoom = Math.max(minZoom, Math.min(maxZoom, zoom));

        // Zoom around mouse pointer
        int mouseX = e.getX();
        int mouseY = e.getY();
        cameraOffsetX = (int) ((cameraOffsetX + mouseX) * (zoom / prevZoom) - mouseX);
        cameraOffsetY = (int) ((cameraOffsetY + mouseY) * (zoom / prevZoom) - mouseY);

        repaint();
    }

    // --------------------------

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

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        System.out.println("Game Started");
        double drawInterval = 1_000_000_000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();
            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1_000_000;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void update() {
        long currentTime = System.nanoTime();
        double elapsedTime = (currentTime - lastUpdateTime) / 1_000_000_000.0;
        lastUpdateTime = currentTime;
        gameClock.updateTime(elapsedTime);

        double currentGameDay = gameClock.getTotalGameDays();

        starMap.updateFlashingCircle();
        shipBuilderHelper.update();

        if (gameClock.isNewMonth()) updateMonthlyPlanetLogic();

        if (gameClock.isNewDay()) detectCombatAndAdvance();

        for (Star star : starMap.getStars()) {
//            star.orbitingShips.clear();
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == titleState) {
            ui.draw(g2);
            g2.dispose();
            return;
        }

        Graphics2D g2UI = (Graphics2D) g2.create();

        g2.scale(zoom, zoom);
        g2.translate(-cameraOffsetX, -cameraOffsetY);

        for (Entity entity : entities) {
            entity.updateScreenPosition(cameraOffsetX, cameraOffsetY, zoom);
        }

        starMap.draw(g2);

        for (StationaryEntity e : stationaryEntities) {
            if (e.hasVisibleOrbit) e.drawOrbit(g2);
        }

        drawOrbitingShips(g2);
        drawNonOrbitingEntities(g2);

        g2.scale(1/zoom, 1/zoom);
        g2.translate(cameraOffsetX, cameraOffsetY);

        ui.draw(g2UI);

        for (CombatManager combatManager : activeCombats) {
            combatManager.combatGUI.draw(g2UI);
        }
        g2UI.dispose();
        g2.dispose();
    }

    private void drawOrbitingShips(Graphics2D g2) {
        Map<Star, Map<String, List<Ship>>> shipsByStarAndType = new HashMap<>();

        // Group ships by star and type
        for (Ship ship : getShips()) {
            if (ship.inOrbit && ship.currentStar != null) {
                shipsByStarAndType
                        .computeIfAbsent(ship.currentStar, s -> new HashMap<>())
                        .computeIfAbsent(ship.name, n -> new ArrayList<>())
                        .add(ship);
            }
        }

        // Draw ships for each star
        for (Star star : shipsByStarAndType.keySet()) {
            Map<String, List<Ship>> shipsByType = shipsByStarAndType.get(star);

            for (String type : shipsByType.keySet()) {
                List<Ship> group = shipsByType.get(type);

                for (int i = 0; i < group.size(); i++) {
                    Ship ship = group.get(i);

                    // DON'T recalculate positions here - trust Ship.recalculateOrbitOffsetsAt()
                    // The Ship class has already positioned everything correctly

                    // Determine visibility and clickability based on stacking rules
                    if (group.size() < 3 || i == 0) {
                        ship.clickable = true;  // only visible ships are clickable
                        ship.draw(g2);
                    } else {
                        ship.clickable = false; // hidden ships not clickable
                    }
                }

                // Draw stack count if more than 3 ships
                if (group.size() >= 3) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 16));
                    Ship representative = group.get(0);
                    g2.drawString("x " + group.size(),
                            (int) representative.exactCentreX + representative.solidArea.width / 2 + 5,
                            (int) representative.exactCentreY + representative.solidArea.height / 2);
                }
            }
        }
    }


    private void drawNonOrbitingEntities(Graphics2D g2) {
        for (Entity entity : new ArrayList<>(entities)) {
            if (!(entity instanceof Ship && ((Ship) entity).inOrbit)) {
                entity.draw(g2);
                if (entity.debug) {
                    entity.drawCentrePosition(g2);
                    entity.drawWorldXY(g2);
                }
            }
        }
    }

    public void loadStarOverlays() {
        for (int i = 0; i < 5; i++) {
            try {
                starOverlays[i] = ImageIO.read(getClass().getResourceAsStream("/planets/planet" + (i + 1) + ".png"));
            } catch (IOException e) {
                System.err.println("Failed to load overlay image: star" + i + ".png");
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

    public Point screenToWorld(int screenX, int screenY) {
        int worldX = (int) ((screenX / zoom) + cameraOffsetX);
        int worldY = (int) ((screenY / zoom) + cameraOffsetY);
        return new Point(worldX, worldY);
    }
}
