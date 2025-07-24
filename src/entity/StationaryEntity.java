package entity;

import main.GamePanel;
import main.Star;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class StationaryEntity extends Entity {
    public boolean hasVisibleOrbit = false;


    public StationaryEntity(GamePanel gamePanel, String name,Star currentStar, boolean hasVisibleOrbit, int buildCost, int maxHealth, int damage) {
        super(gamePanel, name, buildCost, maxHealth, damage);
        this.currentStar = currentStar;
        orbitOffsetX = -12;
        orbitOffsetY = +10;
        this.hasVisibleOrbit = hasVisibleOrbit;

        defending = true; //stationary entities are always defending
        if (currentStar != null) {
            setCentrePosition(currentStar.x + orbitOffsetX, currentStar.y + orbitOffsetY);
        }

        // Load fallback image
        getImage();
    }





    public void draw(Graphics2D g2) {

        //new :
        BufferedImage image = facingLeft ? left1 : right1;


        if (debug) {
            g2.setColor(Color.YELLOW);
            g2.draw(new Rectangle(worldX, worldY, gamePanel.TILE_SIZE, gamePanel.TILE_SIZE));
        }

        g2.drawImage(image, worldX, worldY, gamePanel.TILE_SIZE, gamePanel.TILE_SIZE, null);

    }

    public void drawOrbit(Graphics2D g2) {
        Ellipse2D orbit = createOrbit(30,30);
        if (this.faction == Faction.PLAYER) {
            g2.setColor(new Color(22, 64, 172));
        } else {
            g2.setColor(new Color(232, 23, 23));
        }
        g2.draw(orbit);
    }

    public Ellipse2D createOrbit(int width, int height) {
        if (currentStar == null) return null;

        // Center the ellipse at the star's position, adjusting for width/height
        double x = currentStar.x - width / 2.0;
        double y = currentStar.y - height / 2.0;

        return new Ellipse2D.Double(x, y, width, height);
    }

    protected void print_debug(){
        System.out.println("////////////");
        System.out.println("DEBUG");
        System.out.println("SOLID AREA:");
        System.out.println("x:" + solidArea.x + " y:" + solidArea.y + " width:" + solidArea.width + " height:" + solidArea.height);
        System.out.println("Entity's central x,y: " + this.exactCentreX + ", " + this.exactCentreY);
        System.out.println("World x,y: " + this.worldX + ", " + this.worldY);
        System.out.println("Solid offset's x,y: " + this.solidOffsetX + ", " + this.solidOffsetY);


    }

}
