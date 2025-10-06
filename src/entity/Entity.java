package entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.GamePanel;
import main.Star;

import java.io.IOException;


public class Entity {

    public boolean debug = false;

    public enum Faction { PLAYER, ENEMY }

    public final Faction faction;
    public int orbitOffsetX, orbitOffsetY;
    public boolean inOrbit = true;
    public String name;
    public double exactCentreX, exactCentreY;
    Image errorImage;

    public int screenX;
    public int screenY;
    public int worldX, worldY;  //ONLY for rendering and hit box alignment
    public int solidOffsetX;// = worldX;  //Since the images are larger than the ships (whitespace), the offset shows where the image actually starts
    public int solidOffsetY;// = worldY;
    public int centreX, centreY;
    public int speed, solidAreaDefaultX, solidAreaDefaultY;
    public Image left1, right1;

    public String direction;

    public Rectangle solidArea;
    public boolean selected = false;
    public boolean moving = false;
    public boolean facingLeft = false;

    public GamePanel gamePanel;

    public Star currentStar;
    public int targetX = -1;
    public int targetY = -1;
    public boolean defending = false; //whether or not the entity was at the star first or second

    public int buildCost;
    protected int currentHealth, maxHealth, damage;





    public Entity(GamePanel gp, String name, int cost, int hp, int dmg, Faction faction) {
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
        this.faction     = faction;
    }
    public void updateScreenPosition(double cameraOffsetX, double cameraOffsetY, double zoom) {
        screenX = (int) ((worldX + solidOffsetX - cameraOffsetX) * zoom);
        screenY = (int) ((worldY + solidOffsetY - cameraOffsetY) * zoom);
        centreX = (int) (screenX + solidArea.getWidth() / 2);
        centreY = (int) (screenY + solidArea.getHeight() / 2);
    }


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
        this.solidArea.setX(worldX + solidOffsetX);
        this.solidArea.setY(worldY + solidOffsetY);

//        System.out.printf("centreX=%d, centreY=%d, worldX=%d, worldY=%d, solidArea=(%d,%d,%d,%d)\n",
//                this.centreX, this.centreY, this.worldX, this.worldY,
//                solidArea.x, solidArea.y, solidArea.width, solidArea.height);

    }



    /*override*/
    public void update(){}

    public void draw(GraphicsContext g) {}

    public void getImage() {
        try {
            // Try to load the image from resources
            errorImage = new Image(getClass().getResourceAsStream("/units/ErrorLoading.png"));

            // If loading failed (e.g., file missing), fallback
            if (errorImage.isError()) {
                throw new IOException("ErrorLoading.png not found or failed to load.");
            }

            left1 = errorImage;
            right1 = errorImage;
        } catch (Exception e) {
            System.err.println("Failed to load ErrorLoading.png fallback image.");
            e.printStackTrace();
        }
    }

    private void loadFallbackImage() {
            try {
                // Try to load the image from resources
                errorImage = new Image(getClass().getResourceAsStream("/units/ErrorLoading.png"));

                // If loading failed (e.g., file missing), fallback
                if (errorImage.isError()) {
                    throw new IOException("ErrorLoading.png not found or failed to load.");
                }

                left1 = errorImage;
                right1 = errorImage;
            } catch (Exception e) {
                System.err.println("Failed to load ErrorLoading.png fallback image.");
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

    public void drawCentrePosition(GraphicsContext gc) {
        double radius = 2; // match your 2x2 dot
        double x = this.exactCentreX;
        double y = this.exactCentreY;

        gc.setFill(Color.ORANGE);
        gc.fillOval(x, y, radius, radius);
        gc.setStroke(Color.ORANGE);
        gc.strokeOval(x, y, radius, radius);
    }

    public void drawWorldXY(GraphicsContext gc) {
        double radius = 2;
        double x = this.worldX;
        double y = this.worldY;

        gc.setFill(Color.PINK);
        gc.fillOval(x, y, radius, radius);
        gc.setStroke(Color.PINK);
        gc.strokeOval(x, y, radius, radius);
    }


    public int getMaxHealth(){
        return maxHealth;
    }



}
