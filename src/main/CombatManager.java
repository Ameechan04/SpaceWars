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
    boolean p_turn, first_turn = true;
    Random rand;
    public boolean combatRecently = false;
    public boolean showResult = false;
    public int daysPassedSinceEnding = -1;
    public String starName;
    public int battleCounter;
    private boolean playerVictory;  // Stores the result permanently after combat ends



    ArrayList<Entity> playerEntities = new ArrayList<>();
    ArrayList<Entity> enemyEntities = new ArrayList<>();

    ArrayList<Entity> deadPlayerEntities = new ArrayList<>();
    ArrayList<Entity> deadEnemyEntities = new ArrayList<>();

    Star star;
    public CombatManager(GamePanel gamePanel, Star star, List<Entity> players, List<Entity> enemies) {
            this.gamePanel = gamePanel;
            this.combatGUI = new CombatGUI(this);
                this.star = star;
                this.star.battleCounter++;
                this.starName = star.name;
                this.battleCounter = this.star.battleCounter;
                this.playerEntities.addAll(players);
                this.enemyEntities.addAll(enemies);
                this.combatGUI = new CombatGUI(this);
                this.inCombat = true;
                this.first_turn = true;
                removeEntitiesFromGamePanel();
    }

    public void dailyCombat() {

        if (!inCombat) {
            tryStartCombat();
        } else {
            combatTurn(); // If combat is ongoing, progress it
        }

    }

    public void tryStartCombat() {
        playerEntities.clear();
        enemyEntities.clear();
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

        starName = matchingStar.name;
        star = matchingStar;
        star.hasCombat = true;
        inCombat = true;
        first_turn = true;

        playerEntities.addAll(starToPlayerEntities.get(matchingStar));
        enemyEntities.addAll(starToEnemyEntities.get(matchingStar));
        removeEntitiesFromGamePanel();

    }



        // Get lists of friendly and enemy ships (or entities)

    //

// Map stars to entities at that star



        // Each friendly ship attacks one enemy ship



        //determines who's turn it is based on who is defending


    private void removeDeadEntitiesFromLocal() {
        List<Entity> toRemove = new ArrayList<>();
        ArrayList<Entity> combined = new ArrayList<>();
        combined.addAll(playerEntities);
        combined.addAll(enemyEntities);

        for (Entity e : combined) {
            if (e.isDead()) {
                toRemove.add(e);
                if (e.faction.equals(Entity.Faction.PLAYER)) {
                    deadPlayerEntities.add(e);
                } else {
                    deadEnemyEntities.add(e);
                }

            }
        }

        for (Entity e: toRemove) {
            if (e.faction.equals(Entity.Faction.PLAYER)) {
                playerEntities.remove(e);
            } else {
                enemyEntities.remove(e);
            }
        }


    }


    private void combatTurn() {
        System.out.println("Combat started");

         rand = new Random();

        if (first_turn) {
            if (star.orbitController != null) {
                p_turn = star.orbitController.equals(Entity.Faction.PLAYER);
            }
            first_turn = false;
        }



        if (p_turn) {
            for (Entity friendly : playerEntities) {
                Entity enemy = enemyEntities.get(rand.nextInt(enemyEntities.size()));
                enemy.takeDamage(friendly.getDamage());
                combatGUI.animations.add(new CombatAnimation(friendly, enemy));
                gamePanel.ui.addMessage(friendly.name + " did " + friendly.getDamage() + " damage to " + enemy.name + "." + enemy.getFaction());
                System.out.println(friendly.name + " did " + friendly.getDamage() + " damage to " + enemy.name + "." + enemy.getFaction());
                System.out.println(enemy.name + " HP: " + enemy.getCurrentHealth() + ", DMG: " + enemy.getDamage());


            }
            p_turn = false;
        } else {
            // Each enemy ship attacks one friendly ship
            for (Entity enemy : enemyEntities) {
                Entity friendly = playerEntities.get(rand.nextInt(playerEntities.size()));
                friendly.takeDamage(enemy.getDamage());
                combatGUI.animations.add(new CombatAnimation(enemy, friendly));
                gamePanel.ui.addMessage(enemy.name + " did " + enemy.getDamage() + " damage to " + friendly.name + "." + friendly.getFaction());
                System.out.println(enemy.name + " did " + enemy.getDamage() + " damage to " + friendly.name+ "." + friendly.getFaction());
                System.out.println(friendly.name + " HP: " + friendly.getCurrentHealth() + ", DMG: " + friendly.getDamage());
            }
            p_turn = true;
        }


//        // Remove dead ships
        //  removeDeadShips();
        //  removeDeadStationaryEntities();

        removeDeadEntitiesFromLocal();

        if (combatFinished()) {
            combatFinishedHelper();
        }
    }

    private void combatFinishedHelper(){

        inCombat = false;
        showResult = true;

        playerVictory = calculatePlayerVictory();

        addEntitiesToGamePanel();
        star.hasCombat = false;
        star.recentCombat = true;
        System.out.println("combat finished at " + star.name);

        if (playerWon()) {
            System.out.println("Player won at " + star.name);
        } else {
            System.out.println("Player lost at " + star.name);
        }
        combatRecently = true;
        daysPassedSinceEnding = 0;

    }


    /**
     * Temporarily removes all entities that are in combat from the global entities list.
     * This ensures that conflicting logic cannot be applied while they are in combat such as moving them by clicking.
     */
    private void removeEntitiesFromGamePanel(){
        for (Entity e : playerEntities) {
            if (e instanceof Ship) {
                gamePanel.removeShip((Ship) e);
            } else {
                gamePanel.removeStationaryEntity((StationaryEntity) e);
            }
        }
        for (Entity e : enemyEntities) {
            if (e instanceof Ship) {
                gamePanel.removeShip((Ship) e);
            } else {
                gamePanel.removeStationaryEntity((StationaryEntity) e);
            }
        }

    }
    /**
     * Re-adds all the survivors to the global entity lists in GamePanel.
     */
    private void addEntitiesToGamePanel(){
        ArrayList<Entity> survivors;
        if (playerWon()) {
           survivors = new ArrayList<>(playerEntities);
        } else {
           survivors = new ArrayList<>(enemyEntities);
        }
        for (Entity localE: survivors) {
                    if (localE instanceof Ship) {
                        gamePanel.addShip((Ship) localE);
                    }else {
                        gamePanel.addStationaryEntity((StationaryEntity) localE);
                    }
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
    /**
     * Returns {@code true} if the player won the battle.
     * Returns {@code false} if the enemy won the battle.
     *
     * @return {@code true} if the enemy list is empty (all the ships/stationary entities killed or left).
     */
    protected boolean playerWon(){
        return playerVictory;
    }


    private boolean calculatePlayerVictory() {
        for (Entity e : enemyEntities) {
            if (!e.isDead()) return false;
        }
        return true;
    }









}
