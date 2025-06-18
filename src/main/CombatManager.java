package main;

import entity.Entity;
import entity.Ship;
import entity.StationaryEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CombatManager {
    GamePanel gamePanel;
    public List<CombatAnimation> animations = new ArrayList<>();

    public CombatManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void dailyCombat() {
        // Get lists of friendly and enemy ships (or entities)

        List<Star> starsP = new ArrayList<>();
        List<Star> starsE = new ArrayList<>();
        for (Entity e : gamePanel.getPlayerEntities()) {
            if (e.currentStar != null) starsP.add(e.currentStar);
        }


        for (Entity e : gamePanel.getEnemyEntities()) {
            if (e.currentStar != null) starsE.add(e.currentStar);
        }
        Random rand = new Random();

        boolean overlap = false;
        Star matchingStar = null;
        for (Star sp : starsP) {
            for (Star se : starsE) {
                if (sp == se) { // or sp.equals(se) if equals is overridden
                    System.out.println("match");
                    matchingStar = sp;
                    overlap = true;
                    break;
                }
            }
            if (overlap) break;
        }
        if (!overlap) {
            return;
        }
        List<Entity> playerEntities = new ArrayList<>();
        List<Entity> enemyEntities = new ArrayList<>();
        for (Entity e : gamePanel.getPlayerEntities()) {
            if (e.currentStar != null && e.currentStar == matchingStar) playerEntities.add(e);
        }
         for (Entity e : gamePanel.getEnemyEntities()) {
                    if (e.currentStar != null && e.currentStar == matchingStar) enemyEntities.add(e);
                }

        System.out.println("Combat started");

        // Each friendly ship attacks one enemy ship
        for (Entity friendly : playerEntities) {
            Entity enemy = enemyEntities.get(rand.nextInt(enemyEntities.size()));
            enemy.takeDamage(friendly.getDamage());
            animations.add(new CombatAnimation(friendly, enemy));
            gamePanel.ui.addMessage(friendly.name + " did " + friendly.getDamage() + " damage to " + enemy.name);
            System.out.println(friendly.name + " did " + friendly.getDamage() + " damage to " + enemy.name);
            System.out.println(friendly.name + " HP: " + friendly.getCurrentHealth() + ", DMG: " + friendly.getDamage());

        }

        // Each enemy ship attacks one friendly ship
        for (Entity enemy : enemyEntities) {
            Entity friendly = playerEntities.get(rand.nextInt(playerEntities.size()));
            friendly.takeDamage(enemy.getDamage());
            animations.add(new CombatAnimation(enemy, friendly));
            gamePanel.ui.addMessage(enemy.name + " did " + enemy.getDamage() + " damage to " + friendly.name);
            System.out.println(enemy.name + " did " + enemy.getDamage() + " damage to " + friendly.name);
            System.out.println(enemy.name + " HP: " + enemy.getCurrentHealth() + ", DMG: " + enemy.getDamage());

        }


        // Remove dead ships
        removeDeadShips();
        removeDeadStationaryEntities();
    }

    private void removeDeadShips() {
        List<Ship> toRemove = new ArrayList<>();

        for (Ship s : gamePanel.getShips()) {
            System.out.println(s.name + s.getCurrentHealth());
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

    public void drawCombatAnimations(Graphics2D g2) {
        g2.setColor(Color.RED); // or another

        List<CombatAnimation> toRemove = new ArrayList<>();

        for (CombatAnimation anim : this.animations) {
            System.out.println("drawing animation");
            Rectangle a = anim.attacker.solidArea;
            Rectangle b = anim.target.solidArea;

//            int x1 = anim.attacker.worldX + a.x + a.width / 2;
//            int y1 = anim.attacker.worldY + a.y + a.height / 2;
//            int x2 = anim.target.worldX + b.x + b.width / 2;
//            int y2 = anim.target.worldY + b.y + b.height / 2;

            int x1 = anim.attacker.centreX;
            int y1 = anim.attacker.centreY;
            int x2 = anim.target.centreX;
            int y2 = anim.target.centreY;

            g2.drawLine(x1, y1, x2, y2);
            System.out.println("drawing between " + x1 + ", " + y1 + " and " + x2 + ", " + y2);

            if (anim.isExpired()) toRemove.add(anim);
        }

        this.animations.removeAll(toRemove);
    }


}
