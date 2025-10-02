package main;

import entity.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Star {
    public boolean combatVisible;
    public Ellipse2D combatButton;

    public enum Faction { PLAYER, ENEMY }
    public Entity.Faction coloniserFaction = null;
    public int defences;

    public String name;
    public float x, y;
    public List<Star> connections;
    public boolean selected = false;
    public Rectangle solidArea;
    public int solidAreaDefaultX, solidAreaDefaultY;
    public List<Ship> orbitingShips = new ArrayList<>();
    public Station station = null;
    double colonisationTimer = 0;
    double colonisationStartDate = -1;
    Entity.Faction owner;
    public ArrayList<StationaryEntity> satellites = new ArrayList<>();
    public long population = 0;
    public Ship.Faction orbitController = null;
    public boolean hasCombat = false;
    public boolean recentCombat = false;
    public int battleCounter;
    public Rectangle combatHitbox = null;
    public OrbitManager orbitManager;

    public void updateColonisation(GameClock gameClock, UI ui) {
        if (this.colonised == Colonised.BEGUN) {
            if (this.colonisationStartDate < 0) {
                this.colonisationStartDate = gameClock.getTotalGameDays();
            }

            if (gameClock.getTotalGameDays() - this.colonisationStartDate >= 180 ) {
                this.colonised = Colonised.COLONISED;
                this.population = 200_000;
                this.owner = coloniserFaction;
                ui.addMessage("Colonisation of " + this.name + " complete by " + this.owner, "green");
            }
        }
    }


    public void updateCombatButton() {
        if (hasCombat && combatVisible) {
            combatButton = new Ellipse2D.Double(this.x + 2, this.y + 2, 40, 40);
        }

    }


    public enum Colonised  {
        UNCOLONISED,
        BEGUN,
        COLONISED,
        ABANDONED
    }

    public enum Quality {
        UNINHABITABLE,
        BARREN,
        POOR,
        MEDIUM,
        RICH
    }

    public Colonised colonised = Colonised.UNCOLONISED;
    public Quality quality;

    public int overlayIndex = -1; // index from 0 to 4
    public BufferedImage overlay;


    public Star(String name, float x, float y, GamePanel gamePanel) {
        orbitManager = new OrbitManager(gamePanel, this);
        this.name = name;
        this.x = x;
        this.y = y;
        this.connections = new ArrayList<>();
        int hitboxSize = 24;
        defences = 0;
        battleCounter = 0;
        // Centre the hitbox around (x, y)
        int hitboxX = (int) x - hitboxSize / 2;
        int hitboxY = (int) y - hitboxSize / 2;

        solidArea = new Rectangle(hitboxX -10 , hitboxY - 10, hitboxSize + 20, hitboxSize + 20);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        quality =  randomisedStarQuality();
    }


    private Quality randomisedStarQuality() {
        Quality[] qualities = Quality.values();
        Random random = new Random();


            int index = random.nextInt(qualities.length);
            return qualities[index]; // assuming each Star has a 'quality' field of type StarQuality
    }

    public void connectTo(Star other) {
        if (!connections.contains(other)) {
            connections.add(other);
        }
    }



    public void setStation(Station station) {
        this.station = station;
    }

    public double getPopulationGrowthRate() {

        double planetQuality = switch (this.quality) {
            case BARREN -> 0.1;
            case POOR -> 0.3;
            case MEDIUM -> 0.6;
            case RICH -> 1.0;
            default -> -0.1;
        };

        double pop = population;
        double maxGrowthRate = 0.20; // 10% max monthly growth at low population
        double scale = 500_000_000.0; // growth halves at ~1B pop

        // Logistic-style drop-off as population increases
        double growthRate = maxGrowthRate / (1.0 + (pop / scale));

        return planetQuality * pop * growthRate;
    }


    public double getMonthlyIncome() {
        double maxIncome;
        switch (this.quality) {
            case BARREN -> maxIncome = 10;
            case POOR -> maxIncome = 30;
            case MEDIUM -> maxIncome = 60;
            case RICH -> maxIncome = 100;
            default -> maxIncome = 20;
        }

        double pop = population;

        double breakEvenPop = 10_000_000.0;  // 10 million population break-even point
        double negativeBase = -40;            // Income at zero pop

        if (pop < breakEvenPop) {
            // Linear from -20 at 0 pop to 0 at break-even
            return negativeBase * (1 - pop / breakEvenPop);
        }

        // For pop >= breakEvenPop, income saturates at maxIncome as pop grows
        // K controls how quickly income saturates; tweak to get desired curve
        double K = 2_000_000_000.0; // 2 billion saturation factor

        double income = maxIncome * (pop / (pop + K));

        return income;
    }









}
