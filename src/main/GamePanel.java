package main;
import entity.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GamePanel extends JPanel implements Runnable {
    //screen settings
    final int ORIGINAL_TILE_SIZE = 32;
    public final float SCALE = 1.5F;
    private long lastUpdateTime = System.nanoTime();


    public StarMap starMap = new StarMap(this);

//    private List<Entity> entities = new CopyOnWriteArrayList<>();
    private List<Entity> entities = new ArrayList<>();

    private ArrayList<StationaryEntity> stationaryEntities = new ArrayList<>();
    private ArrayList<Ship> ships = new ArrayList<>(); //100 ship cap for now

    public Set<Star> visitedStars = new HashSet<>();

    public final int TILE_SIZE = (int) (ORIGINAL_TILE_SIZE * SCALE); //32 * 32 * SCALE

    int FPS = 60;

//    TileManager tileManager = new TileManager(this);
        KeyHandler keyHandler = new KeyHandler(this);
        MouseHandler mouseHandler = new MouseHandler(this);
//    Sound sound = new Sound();

//    public CollisionChecker collisionChecker = new CollisionChecker(this);
//    public AssetSetter assetSetter = new AssetSetter(this);
    Thread gameThread;
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;


    public BufferedImage[] starOverlays = new BufferedImage[5];

    public Scout scout; // = new Scout(this, 0, 0);  // initial position doesn't matter much here
    public Scout scout2;
    public ColonyShip colonyShip, colonyShip2;
    public SmallSatellite satellite;
    public BasicShipyard basicShipyard;
    public Ship e1, e2, e3;

    public HashMap<String, Integer> buildCosts = new HashMap<>();
    public int money = 200;

    ShipBuilderHelper shipBuilderHelper = new ShipBuilderHelper(this);
    public UI ui = new UI(this);
    public GameClock gameClock = new GameClock();
//    public CombatManager combatManager = new CombatManager(this);
    List<CombatManager> activeCombats = new ArrayList<>();







//    public SuperObject[] obj = new SuperObject[10]; //TODO as many objects displayed at once as we want -  prepared 10 objects but can be replaced
Color blueColour = new Color(51, 63, 220);

    public GamePanel() {

//        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(new Color(0, 0, 0));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.addMouseListener(mouseHandler);

        this.setFocusable(true);
        requestFocusInWindow();




    }

    public void setUpGame() {
        gameState = titleState;
       buildCosts.put("Scout",120);
       buildCosts.put("Frigate",200);
       buildCosts.put("Colony Ship",500);
       buildCosts.put("Small Satellite",75);
       buildCosts.put("Basic Shipyard",150);

        try {
            starMap.loadFromFile("res/maps/starmap.txt"); // Adjust the path as needed
            loadStarOverlays(); // Loads overlays into starOverlays[]
            starMap.assignStarOverlays(starOverlays);

        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println("Loaded overlays:");
//        for (int i = 0; i < starOverlays.length; i++) {
//            System.out.println("Overlay " + i + ": " + (starOverlays[i] != null ? "OK" : "Missing"));
//        }
        Star alpha = starMap.getStar("Alpha");
        createHomeworld(1, alpha);

        Star Avalon = starMap.getStar("Avalon");
        createHomeworld(2, Avalon);



        scout = new Scout(this, alpha);  // initial position doesn't matter much here
            addShip(scout);
            scout.enterOrbit(alpha);
            visitedStars.add(alpha);


//            colonyShip = new ColonyShip(this, alpha);
//            addShip(colonyShip);
//            colonyShip.enterOrbit(alpha);
//
//
//

        e1 = new Frigate(this, alpha);
        e1.enterOrbit(alpha);
        addShip(e1);

        for (int i = 0; i < 8; i++) {
            e2 = new Scout(this, alpha);
            e2.faction = Entity.Faction.PLAYER;
            e2.enterOrbit(alpha);

            this.addShip(e2);
        }

            basicShipyard = new BasicShipyard(this, alpha);
            addStationaryEntity(basicShipyard);
//
            satellite = new SmallSatellite(this,alpha,0);
            alpha.satellites.add(satellite);
            addStationaryEntity(satellite);

            SmallSatellite enemy = new SmallSatellite(this, Avalon, Avalon.satellites.size());
            enemy.faction = Entity.Faction.ENEMY;
            Avalon.satellites.add(enemy);
            this.addStationaryEntity(enemy);
//
//        for (int i = 0; i < 2; i++) {
//            enemy = new SmallSatellite(this, Avalon, Avalon.satellites.size());
//            enemy.faction = Entity.Faction.ENEMY;
//            Avalon.satellites.add(enemy);
//            this.addStationaryEntity(enemy);
//        }
//
        for (int i = 0; i < 2; i++) {
            e3 = new Frigate(this, Avalon);
            e3.faction = Entity.Faction.ENEMY;
            e3.enterOrbit(Avalon);
            this.addShip(e3);
        }



//        assetSetter.setObject();
//
//        playMusic(0);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }
    @Override
    public void run() {
        System.out.println("Game Started");

        double drawInterval = 1000000000 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;


        while (gameThread != null) {

            //1. update information such as character position
            update();
            //2. draw the screen with updated information
            repaint();
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1000000;

                if (remainingTime < 0) {
                    remainingTime = 0;
                }
                Thread.sleep((long) remainingTime);

                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }

    public void update(){

        long currentTime = System.nanoTime();
        double elapsedTime = (currentTime - lastUpdateTime) / 1_000_000_000.0; // seconds
        lastUpdateTime = currentTime;
        gameClock.updateTime(elapsedTime);

        double currentGameDay = gameClock.getTotalGameDays();

        starMap.updateFlashingCircle();


        shipBuilderHelper.update();

        if (gameClock.isNewMonth()) {
            updateMonthlyPlanetLogic();
        }


        if (gameClock.isNewDay()) {
            detectCombat();

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
                        iter.remove(); // safe removal
                    }
                }
            }
        }


//        System.out.println("total game days passed: " + gameClock.getTotalGameDays());


        for (Star star : starMap.getStars()) {
            star.orbitingShips.clear();



           star.updateColonisation(gameClock, ui);
           star.updateCombatButton();
        }
       for (Entity entity : entities) {
           if (entity instanceof Ship) {
               Ship ship = (Ship) entity;
               if (ship != null) ship.update(currentGameDay);
           } else {
               if (entity != null) entity.update();
           }

       }

       ui.updateMessages(elapsedTime);

    }

    private void detectCombat() {
        HashMap<Star, List<Entity>> starToPlayerEntities = new HashMap<>();
        HashMap<Star, List<Entity>> starToEnemyEntities = new HashMap<>();

        for (Entity e : getPlayerEntities()) {
            if (e.currentStar != null) {
                starToPlayerEntities.computeIfAbsent(e.currentStar, k -> new ArrayList<>()).add(e);
            }
        }
        for (Entity e : getEnemyEntities()) {
            if (e.currentStar != null) {
                starToEnemyEntities.computeIfAbsent(e.currentStar, k -> new ArrayList<>()).add(e);
            }
        }

        for (Star star : starToPlayerEntities.keySet()) {
            if (starToEnemyEntities.containsKey(star) && !star.hasCombat) {
                // New battle! Create a CombatManager:
                CombatManager manager = new CombatManager(this, star,
                        starToPlayerEntities.get(star),
                        starToEnemyEntities.get(star)
                );
                activeCombats.add(manager);
                star.hasCombat = true;
            }
        }

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == titleState) {
            ui.draw(g2);
        } else {


            starMap.draw(g2);

            for (StationaryEntity e : stationaryEntities) {
                if (e.hasVisibleOrbit) {
                    e.drawOrbit(g2);
                }
            }

            for (Entity entity : new ArrayList<>(entities)) {


                if (entity != null) {
                    entity.draw(g2);
                    if (entity.debug) {
                        entity.drawCentrePosition(g2);
                        entity.drawWorldXY(g2);
                    }
                }


//
            }

            ui.draw(g2);


            for (CombatManager combatManager : activeCombats) {
                combatManager.combatGUI.draw(g2);
            }
//            combatManager.combatGUI.drawCombatAnimations(g2);
        }
        g2.dispose();
    }

//    public void playMusic(int i) {
//        sound.setFile(i);
//        sound.play();
//        sound.loop();
//    }
//
//    public void stopMusic() {
//        sound.stop();
//    }
//
//    public void playSoundEffect(int i) {
//        sound.setFile(i);
//        sound.play();
//    }


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



    public List<Entity> getEntities() { return entities; }
    public List<Ship> getShips() { return ships; }

    public void addShip(Ship ship) {
        ships.add(ship);
        System.out.println("last added ship entity " + ships.getLast().name);

        entities.add(ship);
        System.out.println("last added entity " + entities.getLast().name);

    }

    public void addStationaryEntity(StationaryEntity e) {
        stationaryEntities.add(e);
        System.out.println("last added stationary entity " + stationaryEntities.getLast().name);

        entities.add(e);
        System.out.println("last added entity " + entities.getLast().name);

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
            if (s.currentStar == star && s.inOrbit && !s.moving) {
                result.add(s);
            }
        }
        return result;
    }

    public ArrayList<StationaryEntity> getStationaryEntities() {
        return stationaryEntities;
    }

    public void addMoney(int amount) {
        money += amount;
    }

    public boolean spendMoney(int amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        } else {
            return false; // not enough funds
        }
    }

    public void updateMonthlyPlanetLogic() {
        for (Star star : starMap.getStars()) {
            if (star.colonised == Star.Colonised.COLONISED) {
                // Example logic:
                int populationGrowth = (int) star.getPopulationGrowthRate();
                star.population += populationGrowth;
                String message = String.format("%,d", star.population);

                int income = (int) star.getMonthlyIncome();
                addMoney(income);


                ui.addMessage("Monthly update at " + star.name + ": +"
                        + message + " population, +$"+income);
            }
        }

        double upkeep = 0.0;
        for (Entity e : getEntities()) {
            upkeep+=((double) e.buildCost / 100);
        }

        addMoney((int) -upkeep);
        ui.addMessage("Monthly upkeep was -" + (int) upkeep);
    }


    public List<Entity> getPlayerEntities() {
        return getEntities().stream()
                .filter(s -> s.getFaction() == Entity.Faction.PLAYER)
                .collect(Collectors.toList());
    }
    public List<Entity> getEnemyEntities() {
        return getEntities().stream()
                .filter(s -> s.getFaction() == Entity.Faction.ENEMY)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param playerNum The owner of the star (1 - player, 2 - enemy).
     * @param star The star to be set as the homeworld.
     */
    private void createHomeworld(int playerNum, Star star) {
        star.quality = Star.Quality.RICH;
        star.colonised = Star.Colonised.COLONISED;
        starMap.colonisedStars.add(star);
        star.owner = playerNum;
        star.population = 9_000_000_000L;
    }




}
