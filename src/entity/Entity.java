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


    public int worldX, worldY;
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





    public Entity(GamePanel gamePanel, String name, int buildCost, int maxHealth, int damage) {
        this.gamePanel = gamePanel;
        this.name = name;
        this.centreX = 0;
        this.centreY = 0;
        getImage(); // Load ErrorLoading.png by default
        this.buildCost = buildCost;
        this.faction = Faction.PLAYER;
        this.maxHealth = maxHealth;
        this.damage = damage;
        defaultCurrentHealth();


    }



    public void updateCentreFromWorldPosition() {
        this.centreX = worldX + gamePanel.TILE_SIZE / 2;
        this.centreY = worldY + gamePanel.TILE_SIZE / 2;
        this.exactCentreX = centreX;
        this.exactCentreY = centreY;
    }


    public void setCentrePosition(double centreX, double centreY) {
        this.exactCentreX = centreX;
        this.exactCentreY = centreY;
        this.centreX = (int) centreX;
        this.centreY = (int) centreY;
        this.worldX = (int) (centreX - (double) solidArea.width / 2);
        this.worldY = (int) (centreY - (double) solidArea.height / 2);
        this.solidArea.x = worldX + solidOffsetX;
        this.solidArea.y = worldY + solidOffsetY;
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

        Ellipse2D centreDot = new Ellipse2D.Double(this.centreX, this.centreY, 5, 5);
        g2.setColor(Color.ORANGE);
        g2.fill(centreDot);
        g2.draw(centreDot);
    }



}
