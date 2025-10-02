package main;

import entity.Entity;
import entity.Ship;

import java.util.*;

public class HumanPlayer {

    private GamePanel gamePanel;
    private int money;
    private HashMap<String, Integer> buildCosts = new HashMap<>();
    private Set<Star> visitedStars = new HashSet<>();
    private List<Entity> entities = new ArrayList<>();
    private List<Ship> ships = new ArrayList<>();
    private int revenue;

    public HumanPlayer(GamePanel gamePanel, int startingMoney) {
        this.gamePanel = gamePanel;
        this.money = startingMoney;

        // default build costs (could later load from config)
        buildCosts.put("scout", 120);
        buildCosts.put("frigate", 200);
        buildCosts.put("colonyship", 500);
        buildCosts.put("smallsatellite", 75);
        buildCosts.put("basicshipyard", 150);

        revenue = 0;
    }

    // ---------------- Money ----------------
    public int getMoney() { return money; }

    public boolean spendMoney(int amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        }
        return false;
    }

    public void addMoney(int amount) {
        money += amount;
    }

    // ---------------- Entities ----------------
    public void addShip(Ship ship) {
        ships.add(ship);
        entities.add(ship);
        gamePanel.addShip(ship); // delegate to GamePanel’s entity list
    }

    public void removeShip(Ship ship) {
        ships.remove(ship);
        entities.remove(ship);
        gamePanel.removeShip(ship);
    }

    public List<Ship> getShips() { return ships; }
    public List<Entity> getEntities() { return entities; }

    // ---------------- Stars ----------------
    public Set<Star> getVisitedStars() { return visitedStars; }
    public void addVisitedStar(Star star) { visitedStars.add(star); }

    // ---------------- Build Costs ----------------
    public int getBuildCost(String type) {
        return buildCosts.getOrDefault(type.toLowerCase(), Integer.MAX_VALUE);
    }

    public int getRevenue() { return revenue; }
    public void setRevenue(int revenue) { this.revenue = revenue; }
}
