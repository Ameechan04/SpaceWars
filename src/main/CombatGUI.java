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
    boolean viewingResult = false;
    Color backGroundColor = new Color(56, 41, 66);
    Font headingFont = new Font("Arial", Font.BOLD, 30);
    Font textFont = new Font("Arial", Font.PLAIN, 24);


    Star star;
    String starName;
    int battleCount;

//    ArrayList<Entity> enemyEntities;

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
//        starName = manager.starName;
//        this.manager.playerEntities = manager.playerEntities;
//        this.enemyEntities = manager.enemyEntities;



        this.START_X = SCREEN_WIDTH / 2 - WIDTH / 2;
        this.START_Y = SCREEN_HEIGHT / 2 - HEIGHT / 2;
        System.out.println("DEBUG: " + START_X + " " + START_Y);
        System.out.println("DEBUG SW SH: " + SCREEN_WIDTH + " " + SCREEN_HEIGHT);
        combatPanelBackground = new RoundRectangle2D.Double(START_X, START_Y, WIDTH, HEIGHT,20,20);
        closeButton = new RoundRectangle2D.Double(START_X + WIDTH - 110, START_Y + 10 , 100, 30,20,20);



        System.out.println("created combat panel");
    }

    public void draw(Graphics2D g2) {
        if (!visible) return;


        //if visible but not in combat, must be viewing result
//        if (!manager.inCombat) viewingResult = true;


        System.out.println("drawing combat!");

        starName = manager.starName;
        battleCount = manager.battleCounter;
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
        if (manager.starName != null) {
            text = toOrdinal(battleCount) + " Battle of " + starName;
        }
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = SCREEN_WIDTH / 2 - textWidth / 2;
        int y = 100;


        g2.drawString(text, x, y);

        y += 20;
        viewingResult = true;

        if (manager.combatFinished()) {

            //print battle results to GUI
            //do not draw the battle lines
            g2.setFont(new Font("Arial", Font.PLAIN, 70));
            starName = manager.starName;
            battleCount = manager.battleCounter;

            if (manager.playerWon()) {
                g2.setColor(Color.GREEN);
                text = "Player Won the " +  toOrdinal(battleCount) + " Battle of " + starName;
            } else {
                g2.setColor(Color.RED);
                text = "Enemy Won the " +  toOrdinal(battleCount) + " Battle of " + starName;
            }
            fm = g2.getFontMetrics();
            textWidth = fm.stringWidth(text);
            x = SCREEN_WIDTH / 2 - textWidth / 2;
            y = SCREEN_HEIGHT / 2 - fm.getAscent() - 100;

            g2.drawString(text, x, y);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 30));
            text = "Ships lost:";
            fm = g2.getFontMetrics();
            textWidth = fm.stringWidth(text);
            x = SCREEN_WIDTH / 2 - textWidth / 2;
            y = SCREEN_HEIGHT / 2 - fm.getAscent();
            g2.drawString(text, x, y);

            //TODO add more
            int[] shipCount = new int[4];
           // scouts = frigates = small_satellites = colony_ship = 0;
            for (Entity e : manager.deadPlayerEntities) {
                switch (e.name) {
                    case "Scout" -> shipCount[0]++;
                    case "Frigate" -> shipCount[1]++;
                    case "Colony Ship" -> shipCount[2]++;
                    case "Small Satellite" -> shipCount[3]++;
                }
            }

            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            int extraY = 100;


            for (int i = 0; i < shipCount.length; i++) {
                if (shipCount[i] == 0) {
                    continue;
                }
                text = switch (i) {
                    case 0 -> "Scout X" + shipCount[0];
                    case 1 -> "Frigate X" + shipCount[1];
                    case 2 -> "Colony Ship X" + shipCount[2];
                    case 3 -> "Small Satellite X" + shipCount[3];
                    default -> text;
                };
                fm = g2.getFontMetrics();
                textWidth = fm.stringWidth(text);
                x = SCREEN_WIDTH / 2 - textWidth / 2;
                y = SCREEN_HEIGHT / 2 - fm.getAscent() + extraY;
                extraY += 40;
                g2.drawString(text, x, y);
            }


            //clear array
            shipCount = new int[4];
            for (Entity e : manager.deadEnemyEntities) {
                switch (e.name) {
                    case "Scout" -> shipCount[0]++;
                    case "Frigate" -> shipCount[1]++;
                    case "Colony Ship" -> shipCount[2]++;
                    case "Small Satellite" -> shipCount[3]++;
                }
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 30));
            text = "Enemy Ships lost:";
            fm = g2.getFontMetrics();
            textWidth = fm.stringWidth(text);
            x = SCREEN_WIDTH / 2 - textWidth / 2;
            y = SCREEN_HEIGHT / 2 - fm.getAscent() + extraY + 20;
            g2.drawString(text, x, y);


            extraY = y + 40;
            for (int i = 0; i < shipCount.length; i++) {
                if (shipCount[i] == 0) {
                    continue;
                }
                text = switch (i) {
                    case 0 -> "Scout X" + shipCount[0];
                    case 1 -> "Frigate X" + shipCount[1];
                    case 2 -> "Colony Ship X" + shipCount[2];
                    case 3 -> "Small Satellite X" + shipCount[3];
                    default -> text;
                };
                fm = g2.getFontMetrics();
                textWidth = fm.stringWidth(text);
                x = SCREEN_WIDTH / 2 - textWidth / 2;
                y = SCREEN_HEIGHT / 2 - fm.getAscent() + extraY;
                extraY += 40;
                g2.drawString(text, x, y);
            }




        } else {


            g2.drawLine(START_X + WIDTH / 6, y, START_X + WIDTH / 6, HEIGHT);
            g2.drawLine(START_X + WIDTH / 3, y, START_X + WIDTH / 3, HEIGHT);

            g2.drawLine(START_X + WIDTH / 3 * 2, y, START_X + WIDTH / 3 * 2, HEIGHT);
            g2.drawLine(START_X + WIDTH / 6 * 5, y, START_X + WIDTH / 6 * 5, HEIGHT);


            //half way line
            g2.setColor(Color.MAGENTA);
            g2.drawLine(START_X + WIDTH / 2, y, START_X + WIDTH / 2, HEIGHT);


            //draw all friendly ships:


            int i = 0, c = 0;
            int count = 0;
            int rowOffset = 0;
            int imgWidthHeight = 100;

            for (Entity e : manager.playerEntities) {
                if (count > 18) {
                    rowOffset += 50;
                    count = 0;
                    i = 0;
                }

                e.facingLeft = false;
                BufferedImage image = e.right1;
                int xPos = COLUMN3_X - rowOffset;
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

            for (Entity e : manager.enemyEntities) {
                if (count > 18) {
                    rowOffset += 50;
                    count = 0;
                    i = 0;
                }

                e.facingLeft = true;
                BufferedImage image = e.left1;
                int xPos = COLUMN6_X - rowOffset;
                int yPos = y + i;

                g2.drawImage(image, xPos, yPos, imgWidthHeight, imgWidthHeight, null);
                Point centre = new Point(xPos + imgWidthHeight / 2, yPos + imgWidthHeight / 2);
                entityCentreMap.put(e, centre);

                i += 30;
                count++;
            }


            for (Entity e : manager.playerEntities) {
                System.out.println("friendly E : " + e.name);
                Point p = this.entityCentreMap.get(e);
                Ellipse2D centreDot = new Ellipse2D.Double(p.x - 1, p.y - 1, 2, 2);
                g2.setColor(Color.GREEN);
                g2.fill(centreDot);
                g2.draw(centreDot);
            }

            for (Entity e : manager.enemyEntities) {
                System.out.println("enemy E : " + e.name);

                Point p = this.entityCentreMap.get(e);
                Ellipse2D centreDot = new Ellipse2D.Double(p.x - 1, p.y - 1, 2, 2);
                g2.setColor(Color.RED);
                g2.fill(centreDot);
                g2.draw(centreDot);
            }

            System.out.println("Friendly Entities");
            for (Entity e : manager.playerEntities) {
                System.out.println(e.name + " @" + e.hashCode());
            }


            System.out.println("Enemy Entities:");
            for (Entity e : manager.enemyEntities) {
                System.out.println(e.name + " @" + e.hashCode());
            }


            drawCombatAnimations(g2);
        }



    }

    public void drawCombatAnimations(Graphics2D g2) {


        List<CombatAnimation> toRemove = new ArrayList<>();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (CombatAnimation anim : this.animations) {
            Entity attacker = anim.attacker;
            Entity target = anim.target;

            Point p1 = entityCentreMap.get(attacker);
            Point p2 = entityCentreMap.get(target);

            if (p1 == null || p2 == null) continue; // Skip if something went wrong
//            Stroke oldStroke = g2.getStroke();
//            g2.setStroke(new BasicStroke(3));
//            g2.setColor(attacker.getFaction() == Ship.Faction.PLAYER ? Color.GREEN : Color.RED);
//            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

            float x1 = p1.x;
            float y1 = p1.y;
            float x2 = p2.x;
            float y2 = p2.y;
            GradientPaint gradient;
            if (attacker.faction.equals(Entity.Faction.PLAYER)) {
                Color c = new Color(94, 197, 102);
                gradient = new GradientPaint(
                        x1, y1, Color.GREEN,
                        x2,  y2, c
                );

            } else {
                Color c = new Color(153,0,0);

                gradient = new GradientPaint(
                        x1, y1, Color.RED,
                        x2,  y2, c
                );
            }

            g2.setPaint(gradient);
            g2.setStroke(new BasicStroke(3)); // thicker line
            g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);



//            g2.setStroke(oldStroke);



            if (anim.isExpired()) toRemove.add(anim);
        }

        this.animations.removeAll(toRemove);
    }

    protected void battleFinishedScreen(Graphics2D g2){
        hide();
    }



    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

        public String toOrdinal(int number) {
            if (number <= 0) return Integer.toString(number);

            int mod100 = number % 100;
            if (mod100 >= 11 && mod100 <= 13) {
                return number + "th";
            }

            switch (number % 10) {
                case 1:  return number + "st";
                case 2:  return number + "nd";
                case 3:  return number + "rd";
                default: return number + "th";
            }
        }




}
