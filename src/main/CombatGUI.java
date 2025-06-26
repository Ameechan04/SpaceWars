package main;

import entity.Entity;
import entity.Ship;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CombatGUI {
    private CombatManager manager;
    private final int HEIGHT = 800;
    private final int WIDTH = 1200;

    private int SCREEN_WIDTH;
    private int START_X;
    private int START_Y;
    private int SCREEN_HEIGHT;

    public boolean setUp = false;
//
//    ArrayList<Point> friendlyCentres = new ArrayList<>();
//    ArrayList<Point> enemyCentres = new ArrayList<>();
    public HashMap<Entity, Point> entityCentreMap = new HashMap<>();

    private boolean visible = false;
    Color backGroundColor = new Color(56, 41, 66);
    Font headingFont = new Font("Arial", Font.BOLD, 30);
    Font textFont = new Font("Arial", Font.PLAIN, 24);

    Star star;
    ArrayList<Entity> friendlyEntities;
    ArrayList<Entity> enemyEntities;

    RoundRectangle2D combatPanelBackground;
    RoundRectangle2D closeButton;

    final int COLUMN_WIDTH = WIDTH / 6;

    final int COLUMN1_X = START_X + COLUMN_WIDTH * 1 + 5;
    final int COLUMN2_X = START_X + COLUMN_WIDTH * 2 + 5;
    final int COLUMN3_X = START_X + COLUMN_WIDTH * 3 + 5;
    final int COLUMN4_X = START_X + COLUMN_WIDTH * 4 + 5;
    final int COLUMN5_X = START_X + COLUMN_WIDTH * 5 + 5;
    final int COLUMN6_X = START_X + COLUMN_WIDTH * 6 + 5;



    public List<CombatAnimation> animations = new ArrayList<>();

    /**
     * @param manager element to ensure the GUI created correctly links with the correct combat logic processing
     */
    public CombatGUI(CombatManager manager) {
        this.manager = manager;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        SCREEN_WIDTH = screenSize.width;
        SCREEN_HEIGHT = screenSize.height;

        this.star = manager.star;
        this.friendlyEntities = manager.playerEntities;
        this.enemyEntities = manager.enemyEntities;



        this.START_X = SCREEN_WIDTH / 2 - WIDTH / 2;
        this.START_Y = SCREEN_HEIGHT / 2 - HEIGHT / 2;
        System.out.println("DEBUG: " + START_X + " " + START_Y);
        System.out.println("DEBUG SW SH: " + SCREEN_WIDTH + " " + SCREEN_HEIGHT);
        combatPanelBackground = new RoundRectangle2D.Double(START_X, START_Y, WIDTH, HEIGHT,20,20);
        closeButton = new RoundRectangle2D.Double(START_X + WIDTH - 110, START_Y + 10 , 100, 30,20,20);



        System.out.println("created combat panel");
    }

    public void draw(Graphics2D g2) {
        if (!manager.inCombat || !visible) return;

        System.out.println("drawing combat!");

        g2.setColor(backGroundColor);
        g2.fill(combatPanelBackground);
        g2.setColor(Color.WHITE); // border colour
        g2.setStroke(new BasicStroke(3)); // border thickness
        g2.draw(combatPanelBackground); // this draws the border of the rectangle

        g2.setColor(Color.white);
        g2.fill(closeButton);

        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(2)); // border thickness
        g2.draw(closeButton); // this draws the border of the rectangle

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        String text1 = "Close";
        FontMetrics fm1 = g2.getFontMetrics();
        int textWidth1 = fm1.stringWidth(text1);
        int textHeight1 = fm1.getAscent(); // This is the baseline height

        int x1 = (int) (closeButton.getX() + (closeButton.getWidth() - textWidth1) / 2);
        int y1 = (int) (closeButton.getY() + (closeButton.getHeight() + textHeight1) / 2);

        g2.setColor(Color.BLUE);
        g2.drawString(text1, x1, y1);




        g2.setColor(Color.WHITE);
        g2.setFont(headingFont);
        String text = "";
        if (star != null) {
            text = "Battle of " + star.name;
        }
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = SCREEN_WIDTH / 2 - textWidth / 2;
        int y = 100;


        g2.drawString(text, x, y);

        y += 20;


        g2.drawLine( START_X + WIDTH / 6 , y, START_X + WIDTH / 6, HEIGHT);
        g2.drawLine( START_X + WIDTH / 3, y, START_X + WIDTH / 3, HEIGHT);

        g2.drawLine( START_X + WIDTH / 3 * 2, y, START_X + WIDTH / 3 * 2, HEIGHT);
        g2.drawLine( START_X + WIDTH / 6 * 5, y, START_X + WIDTH / 6*5, HEIGHT);


        //half way line
        g2.setColor(Color.MAGENTA);
        g2.drawLine( START_X + WIDTH / 2 , y,START_X + WIDTH / 2, HEIGHT);


        //draw all friendly ships:



        int i = 0,c = 0;
        int count = 0;
        int rowOffset = 0;
        int imgWidthHeight = 100;

        for (Entity e : friendlyEntities) {
            if (count > 18) {
                rowOffset += 50;
                count = 0;
                i = 0;
            }

            e.facingLeft = false;
            BufferedImage image = e.right1;
            int xPos =COLUMN3_X - rowOffset;
            int yPos = y + i;

            g2.drawImage(image, xPos, yPos, imgWidthHeight, imgWidthHeight, null);
            Point centre = new Point(xPos + imgWidthHeight / 2, yPos + imgWidthHeight / 2);
            entityCentreMap.put(e, centre);

            i += 30;
            count++;
        }

        i = 0;
        rowOffset = 0;
        count = 0;

        for (Entity e : enemyEntities) {
            if (count > 18) {
                rowOffset += 50;
                count = 0;
                i = 0;
            }

            e.facingLeft = true;
            BufferedImage image = e.left1;
            int xPos =COLUMN6_X - rowOffset;
            int yPos = y + i;

            g2.drawImage(image, xPos, yPos, imgWidthHeight, imgWidthHeight, null);
            Point centre = new Point(xPos + imgWidthHeight / 2, yPos + imgWidthHeight / 2);
            entityCentreMap.put(e, centre);

            i += 30;
            count++;
        }


        for (Entity e : friendlyEntities) {
            System.out.println("friendly E : " + e.name);
            Point p = this.entityCentreMap.get(e);
            Ellipse2D centreDot = new Ellipse2D.Double(p.x - 1, p.y - 1, 2, 2);
            g2.setColor(Color.GREEN);
            g2.fill(centreDot);
            g2.draw(centreDot);
        }

        for (Entity e : enemyEntities) {
            System.out.println("enemy E : " + e.name);

            Point p = this.entityCentreMap.get(e);
            Ellipse2D centreDot = new Ellipse2D.Double(p.x - 1, p.y - 1, 2, 2);
            g2.setColor(Color.RED);
            g2.fill(centreDot);
            g2.draw(centreDot);
        }

        System.out.println("Friendly Entities");
        for (Entity e : friendlyEntities) {
            System.out.println(e.name + " @" + e.hashCode());
        }


        System.out.println("Enemy Entities:");
        for (Entity e : enemyEntities) {
            System.out.println(e.name + " @" + e.hashCode());
        }


        drawCombatAnimations(g2);




    }

    public void drawCombatAnimations(Graphics2D g2) {


        List<CombatAnimation> toRemove = new ArrayList<>();

        for (CombatAnimation anim : this.animations) {
            Entity attacker = anim.attacker;
            Entity target = anim.target;

            Point p1 = entityCentreMap.get(attacker);
            Point p2 = entityCentreMap.get(target);

            if (p1 == null || p2 == null) continue; // Skip if something went wrong
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(3));
            g2.setColor(attacker.getFaction() == Ship.Faction.PLAYER ? Color.GREEN : Color.RED);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);


            g2.setStroke(new BasicStroke(1));
            g2.setColor(Color.white);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);


            g2.setStroke(oldStroke);



            if (anim.isExpired()) toRemove.add(anim);
        }

        this.animations.removeAll(toRemove);
    }



    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }
}
