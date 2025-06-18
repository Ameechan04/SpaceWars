package main;

import entity.Ship;
import entity.Station;
import entity.StationaryEntity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Star {




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
    public ArrayList<StationaryEntity> satellites = new ArrayList<>();
    public long population = 0;

    public void updateColonisation(GameClock gameClock, UI ui) {
        if (this.colonised == Star.Colonised.BEGUN) {
//            System.out.println("total game days passed: " + gameClock.getTotalGameDays());
//            System.out.println("total colonised days passed: " + (gameClock.getTotalGameDays() - this.colonisationStartDate));

            if (this.colonisationStartDate < 0) {
                this.colonisationStartDate = gameClock.getTotalGameDays();
            }

//                if (star.colonisationTimer >= 7.0) {
            if (gameClock.getTotalGameDays() - this.colonisationStartDate >= 180 ) {
                this.colonised = Star.Colonised.COLONISED;
                this.population = 200_000;
                ui.addMessage("Colonisation of " + this.name + " complete.", "green");
            }
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


    public Star(String name, float x, float y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.connections = new ArrayList<>();
        int hitboxSize = 24; // Increase this as needed for better click accuracy

        // Centre the hitbox around (x, y)
        int hitboxX = (int) x - hitboxSize / 2;
        int hitboxY = (int) y - hitboxSize / 2;

        solidArea = new Rectangle(hitboxX, hitboxY, hitboxSize, hitboxSize);
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
