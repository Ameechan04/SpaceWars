package main;

import entity.Entity;
import entity.Ship;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.stage.Screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CombatGUI {
    private CombatManager manager;
    private final int HEIGHT = 800;
    private final int WIDTH = 1200;

    private double SCREEN_WIDTH;
    private double START_X;
    private double START_Y;
    private double SCREEN_HEIGHT;

    public boolean setUp = false;
    public HashMap<Entity, double[]> entityCentreMap = new HashMap<>();
    private boolean visible = false;
    boolean viewingResult = false;
    private Color backGroundColor = Color.rgb(56, 41, 66);
    private Font headingFont = Font.font("Arial", 30);
    private Font textFont = Font.font("Arial", 24);

    Star star;
    String starName;
    int battleCount;

    final double COLUMN_WIDTH = WIDTH / 6.0;

    public List<CombatAnimation> animations = new ArrayList<>();

    public CombatGUI(CombatManager manager) {
        this.manager = manager;

        Rectangle2D bounds = Screen.getPrimary().getBounds();
        SCREEN_WIDTH = bounds.getWidth();
        SCREEN_HEIGHT = bounds.getHeight();

        this.star = manager.star;

        this.START_X = SCREEN_WIDTH / 2 - WIDTH / 2;
        this.START_Y = SCREEN_HEIGHT / 2 - HEIGHT / 2;
    }

    public void draw(GraphicsContext gc) {
        if (!visible) return;

        starName = manager.starName;
        battleCount = manager.battleCounter;

        // Draw panel background
        gc.setFill(backGroundColor);
        gc.fillRoundRect(START_X, START_Y, WIDTH, HEIGHT, 20, 20);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeRoundRect(START_X, START_Y, WIDTH, HEIGHT, 20, 20);

        // Draw close button
        double closeX = START_X + WIDTH - 110;
        double closeY = START_Y + 10;
        double closeW = 100;
        double closeH = 30;

        gc.setFill(Color.WHITE);
        gc.fillRoundRect(closeX, closeY, closeW, closeH, 20, 20);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRoundRect(closeX, closeY, closeW, closeH, 20, 20);

        gc.setFont(Font.font("Arial", 20));
        String closeText = "Close";
        double textWidth = computeTextWidth(closeText, gc.getFont());
        double textHeight = gc.getFont().getSize();
        gc.setFill(Color.BLUE);
        gc.fillText(closeText, closeX + (closeW - textWidth) / 2, closeY + (closeH + textHeight) / 2);

        // Draw battle heading
        gc.setFont(headingFont);
        String heading = manager.starName != null ? toOrdinal(battleCount) + " Battle of " + starName : "";
        textWidth = computeTextWidth(heading, gc.getFont());
        gc.setFill(Color.WHITE);
        gc.fillText(heading, SCREEN_WIDTH / 2 - textWidth / 2, 100);

        viewingResult = true;

        if (manager.combatFinished()) {
            drawBattleResults(gc);
        } else {
            drawCombatGrid(gc, 120); // example offset
            drawEntities(gc, 120);
            drawCombatAnimations(gc);
        }
    }

    private void drawCombatGrid(GraphicsContext gc, double yOffset) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        for (int i = 1; i <= 5; i++) {
            gc.strokeLine(START_X + COLUMN_WIDTH * i, yOffset, START_X + COLUMN_WIDTH * i, START_Y + HEIGHT);
        }
        gc.setStroke(Color.MAGENTA);
        gc.strokeLine(START_X + WIDTH / 2, yOffset, START_X + WIDTH / 2, START_Y + HEIGHT);
    }

    private void drawEntities(GraphicsContext gc, double startY) {
        double imgSize = 100;

        int i = 0, count = 0;
        double rowOffset = 0;

        for (Entity e : manager.playerEntities) {
            if (count > 18) {
                rowOffset += 50;
                count = 0;
                i = 0;
            }
            e.facingLeft = false;
            Image img = e.right1; // replace BufferedImage with Image
            double xPos = START_X + COLUMN_WIDTH * 2 - rowOffset;
            double yPos = startY + i;
            gc.drawImage(img, xPos, yPos, imgSize, imgSize);
            entityCentreMap.put(e, new double[]{xPos + imgSize / 2, yPos + imgSize / 2});
            i += 30;
            count++;
        }

        i = 0; count = 0; rowOffset = 0;

        for (Entity e : manager.enemyEntities) {
            if (count > 18) {
                rowOffset += 50;
                count = 0;
                i = 0;
            }
            e.facingLeft = true;
            Image img = e.left1;
            double xPos = START_X + COLUMN_WIDTH * 5 - rowOffset;
            double yPos = startY + i;
            gc.drawImage(img, xPos, yPos, imgSize, imgSize);
            entityCentreMap.put(e, new double[]{xPos + imgSize / 2, yPos + imgSize / 2});
            i += 30;
            count++;
        }
    }

    private void drawCombatAnimations(GraphicsContext gc) {
        List<CombatAnimation> toRemove = new ArrayList<>();
        for (CombatAnimation anim : animations) {
            Entity attacker = anim.attacker;
            Entity target = anim.target;

            double[] p1 = entityCentreMap.get(attacker);
            double[] p2 = entityCentreMap.get(target);
            if (p1 == null || p2 == null) continue;

            LinearGradient gradient;
            if (attacker.faction.equals(Entity.Faction.PLAYER)) {
                gradient = new LinearGradient(
                        p1[0], p1[1], p2[0], p2[1], false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.GREEN),
                        new Stop(1, Color.rgb(94, 197, 102))
                );
            } else {
                gradient = new LinearGradient(
                        p1[0], p1[1], p2[0], p2[1], false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.RED),
                        new Stop(1, Color.rgb(153, 0, 0))
                );
            }
            gc.setStroke(gradient);
            gc.setLineWidth(3);
            gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);

            if (anim.isExpired()) toRemove.add(anim);
        }
        animations.removeAll(toRemove);
    }

    private void drawBattleResults(GraphicsContext gc) {
        // TODO: implement similar to draw() but using gc.fillText and proper positioning
    }

    public void show() { visible = true; }
    public void hide() { visible = false; }

    private double computeTextWidth(String text, Font font) {
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(font);
        return tempText.getLayoutBounds().getWidth();
    }

    public String toOrdinal(int number) {
        if (number <= 0) return Integer.toString(number);
        int mod100 = number % 100;
        if (mod100 >= 11 && mod100 <= 13) return number + "th";
        return switch (number % 10) {
            case 1 -> number + "st";
            case 2 -> number + "nd";
            case 3 -> number + "rd";
            default -> number + "th";
        };
    }
}
