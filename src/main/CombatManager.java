package main;

import entity.Entity;
import entity.Ship;
import entity.StationaryEntity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CombatManager {
    GamePanel gamePanel;
    CombatGUI combatGUI;
    public boolean inCombat = false;
    private boolean combatEnabled = true;

    ArrayList<Entity> playerEntities = new ArrayList<>();
    ArrayList<Entity> enemyEntities = new ArrayList<>();

    Star star;
    public CombatManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.combatGUI = new CombatGUI(this);
    }

    public void dailyCombat() {
        // Get lists of friendly and enemy ships (or entities)

        playerEntities.clear();
        enemyEntities.clear();

// Map stars to entities at that star
        HashMap<Star, List<Entity>> starToPlayerEntities = new HashMap<>();
        HashMap<Star, List<Entity>> starToEnemyEntities = new HashMap<>();

        for (Entity e : gamePanel.getPlayerEntities()) {
            if (e.currentStar != null) {
                starToPlayerEntities.computeIfAbsent(e.currentStar, k -> new ArrayList<>()).add(e);
            }
        }
        for (Entity e : gamePanel.getEnemyEntities()) {
            if (e.currentStar != null) {
                starToEnemyEntities.computeIfAbsent(e.currentStar, k -> new ArrayList<>()).add(e);
            }
        }

        Random rand = new Random();

        Star matchingStar = null;
        for (Star star : starToPlayerEntities.keySet()) {
            if (starToEnemyEntities.containsKey(star)) {
                matchingStar = star;
                break;
            }
        }
        if (matchingStar == null) {
            inCombat = false;
            return;
        }

        star = matchingStar;
        star.hasCombat = true;
        inCombat = true;

        playerEntities.addAll(starToPlayerEntities.get(matchingStar));
        enemyEntities.addAll(starToEnemyEntities.get(matchingStar));
        System.out.println("Combat started");









        // Each friendly ship attacks one enemy ship


        for (Entity friendly : playerEntities) {
            Entity enemy = enemyEntities.get(rand.nextInt(enemyEntities.size()));
            enemy.takeDamage(friendly.getDamage());
            combatGUI.animations.add(new CombatAnimation(friendly, enemy));
            gamePanel.ui.addMessage(friendly.name + " did " + friendly.getDamage() + " damage to " + enemy.name);
            System.out.println(friendly.name + " did " + friendly.getDamage() + " damage to " + enemy.name);
            System.out.println(friendly.name + " HP: " + friendly.getCurrentHealth() + ", DMG: " + friendly.getDamage());

        }

        // Each enemy ship attacks one friendly ship
        for (Entity enemy : enemyEntities) {
            Entity friendly = playerEntities.get(rand.nextInt(playerEntities.size()));
            friendly.takeDamage(enemy.getDamage());
            combatGUI.animations.add(new CombatAnimation(enemy, friendly));
            gamePanel.ui.addMessage(enemy.name + " did " + enemy.getDamage() + " damage to " + friendly.name);
            System.out.println(enemy.name + " did " + enemy.getDamage() + " damage to " + friendly.name);
            System.out.println(enemy.name + " HP: " + enemy.getCurrentHealth() + ", DMG: " + enemy.getDamage());

        }


//        // Remove dead ships
        removeDeadShips();
        removeDeadStationaryEntities();

        if (combatFinished()) {
            star.hasCombat = false;
            System.out.println("combat finished at " + star.name);

        }
    }

    private void removeDeadShips() {
        List<Ship> toRemove = new ArrayList<>();

        for (Ship s : gamePanel.getShips()) {
            if (s.isDead()) {
                toRemove.add(s);
            }
        }

        for (Ship s : toRemove) {
            gamePanel.removeShip(s); // This may remove it from the gamePanel's ship list
//            gamePanel.getShips().remove(s); // Remove from the main list if needed
            gamePanel.ui.addMessage(s.name + " has been destroyed!");

        }
    }


    private void removeDeadStationaryEntities() {
        List<StationaryEntity> toRemove = new ArrayList<>();

        for (StationaryEntity se : gamePanel.getStationaryEntities()) {
            if (se.isDead()) {
                toRemove.add(se);
            }
        }

        for (StationaryEntity se : toRemove) {
            gamePanel.removeStationaryEntity(se); // This may remove it from the gamePanel's ship list
            gamePanel.ui.addMessage(se.name + " has been destroyed!");
        }
    }

    /**
     * Returns {@code true} if there are either no player or enemy entities left at the star
     *
     * @return {@code true} if either playerEntities or enemyEntities are empty
     */
    protected boolean combatFinished(){
            boolean playerAlive = false;
            boolean enemyAlive = false;

            for (Entity e : playerEntities) {
                if (!e.isDead()) {
                    playerAlive = true;
                    break;
                }
            }

            for (Entity e : enemyEntities) {
                if (!e.isDead()) {
                    enemyAlive = true;
                    break;
                }
            }

            return !(playerAlive && enemyAlive); // If either side is dead
    }





}
