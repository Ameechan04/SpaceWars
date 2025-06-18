package entity;

import main.GamePanel;
import main.Star;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Entity {


    public enum Faction { PLAYER, ENEMY }

    public Faction faction;
    public int orbitOffsetX, orbitOffsetY;
    public boolean inOrbit = true;
    public String name;
    public double exactCentreX, exactCentreY;
    BufferedImage errorImage;


    public int worldX, worldY;  //ONLY for rendering and hit box alignment
    int solidOffsetX;// = worldX;  //Since the images are larger than the ships (whitespace), the offset shows where the image actually starts
    int solidOffsetY;// = worldY;
    public int centreX, centreY;
    public int speed, solidAreaDefaultX, solidAreaDefaultY;
    BufferedImage left1, right1;

    public String direction;

    public Rectangle solidArea;
    public boolean selected = false;
    public boolean moving = false;
    public boolean facingLeft = false;

    public GamePanel gamePanel;

    public Star currentStar;
    public int targetX = -1;
    public int targetY = -1;

    public int buildCost;
    protected int currentHealth, maxHealth, damage;





    public Entity(GamePanel gp, String name, int cost, int hp, int dmg) {
        this.gamePanel = gp;
        this.name = name;

        // size of the sprite
        solidArea = new Rectangle(0, 0, gp.TILE_SIZE, gp.TILE_SIZE);

        // optional: default offsets if sprite has transparent padding
        this.solidOffsetX = 0;
        this.solidOffsetY = 0;

        // give centre an initial value (0,0) ➜ sync derived fields
        setCentrePosition(0, 0);

        // load fallback image once
        loadFallbackImage();

        // stats
        buildCost   = cost;
        maxHealth   = hp;
        currentHealth = hp;
        damage      = dmg;
        faction     = Faction.PLAYER;
    }





//    public void updateCentreFromWorldPosition() {
//        this.centreX = worldX + gamePanel.TILE_SIZE / 2;
//        this.centreY = worldY + gamePanel.TILE_SIZE / 2;
//        this.exactCentreX = centreX;
//        this.exactCentreY = centreY;
//    }


    /*Only method that sets the position*/
    public void setCentrePosition(double centreX, double centreY) {
        this.exactCentreX = centreX;
        this.exactCentreY = centreY;

        // integer centre used for cheap integer maths / debug drawing
        this.centreX = (int) Math.round(centreX);
        this.centreY = (int) Math.round(centreY);

//        // derive top‑left for rendering / collision
//        this.worldX = this.centreX - solidArea.width / 2;
//        this.worldY = this.centreY - solidArea.height / 2;
//
        this.worldX = this.centreX - gamePanel.TILE_SIZE / 2;
        this.worldY = this.centreY - gamePanel.TILE_SIZE / 2;


        // sync hit‑box
        this.solidArea.x = worldX + solidOffsetX;
        this.solidArea.y = worldY + solidOffsetY;

        System.out.printf("centreX=%d, centreY=%d, worldX=%d, worldY=%d, solidArea=(%d,%d,%d,%d)\n",
                this.centreX, this.centreY, this.worldX, this.worldY,
                solidArea.x, solidArea.y, solidArea.width, solidArea.height);

    }



    /*override*/
    public void update(){}

    public void draw(Graphics2D g2) {}

    public void getImage() {
        try {
            errorImage = ImageIO.read(getClass().getResourceAsStream("/units/ErrorLoading.png"));
            if (errorImage == null) {
                throw new IOException("ErrorLoading.png not found or failed to load.");
            }
            left1 = errorImage;
            right1 = errorImage;
        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to load ErrorLoading.png fallback image.");
            e.printStackTrace();
        }
    }

    private void loadFallbackImage() {
        try {
            BufferedImage errorImage = ImageIO.read(getClass().getResourceAsStream("/units/ErrorLoading.png"));
            if (errorImage == null) {
                throw new IOException("ErrorLoading.png not found");
            }
            left1 = errorImage;
            right1 = errorImage;
        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to load fallback image ErrorLoading.png");
            e.printStackTrace();
        }

    }

    public int getBuildCost() {
        return buildCost;
    }

    public int getCurrentHealth() { return currentHealth; }
    public void takeDamage(int amount) { currentHealth -= amount; }
    public int getDamage() { return damage; }
    public boolean isDead() { return currentHealth <= 0; }

    public Faction getFaction() { return faction; }

    private void defaultCurrentHealth(){
        this.currentHealth = maxHealth;
    }

//    protected void setupSolidArea(int solidOffsetX, int solidOffsetY, int width, int height) {
//        this.solidOffsetX = solidOffsetX;
//        this.solidOffsetY = solidOffsetY;
//        this.solidArea = new Rectangle(worldX + solidOffsetX, worldY + solidOffsetY, width, height);
//        this.solidAreaDefaultX = worldX;
//        this.solidAreaDefaultY = worldY;
//    }

    public void drawCentrePosition(Graphics2D g2) {

        Ellipse2D centreDot = new Ellipse2D.Double(this.exactCentreX, this.exactCentreY, 2, 2);
        System.out.println(this.exactCentreX + " " + this.exactCentreY);
        g2.setColor(Color.ORANGE);
        g2.fill(centreDot);
        g2.draw(centreDot);
    }

    public void drawWorldXY(Graphics2D g2) {

        Ellipse2D centreDot = new Ellipse2D.Double(this.worldX, this.worldY, 2, 2);
        g2.setColor(Color.PINK);
        g2.fill(centreDot);
        g2.draw(centreDot);
    }

    public void drawCentreDebug(Graphics2D g2) {
        g2.setColor(Color.MAGENTA);
        g2.fillOval((int) Math.round(exactCentreX) - 2,
                (int) Math.round(exactCentreY) - 2,
                4, 4);
    }




}
