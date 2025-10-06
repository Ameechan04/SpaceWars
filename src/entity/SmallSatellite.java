package entity;

import javafx.scene.image.Image;
import main.GamePanel;
import main.Star;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.IOException;

public class SmallSatellite extends StationaryEntity {


    public SmallSatellite(GamePanel gamePanel, Star currentStar, int indexInOrbit, Faction faction) {
        super(gamePanel, "Small Satellite", currentStar, true, gamePanel.humanPlayer.getBuildCost("smallsatellite"), 70, 10, faction);

        // Calculate angle with consistent spacing (10px along orbit)
        int spacing = 10;
        double orbitCircumference = 2 * Math.PI * 15; // Approx radius = 15
        double anglePerSatellite = 360.0 * (spacing / orbitCircumference);
        double angle = indexInOrbit * anglePerSatellite;

        double radians = Math.toRadians(angle);
        int radiusX = 15;
        int radiusY = 15;
        this.orbitOffsetX = (int) Math.round(Math.cos(radians) * radiusX);
        this.orbitOffsetY = (int) Math.round(Math.sin(radians) * radiusY);

        // Now set its actual position
        int orbitX = (int) currentStar.x + this.orbitOffsetX;
        int orbitY = (int) currentStar.y + this.orbitOffsetY;


        //x and y do not matter as overridden when drawn
        this.solidArea = new Rectangle(-10, -10 , 10,10);
        solidAreaDefaultX = worldX; //where the ship's actual position is
        solidAreaDefaultY = worldY;
        this.solidOffsetX = 20;
        this.solidOffsetY = 20;
        this.setCentrePosition(orbitX, orbitY);

       // print_debug();
        getImage();

    }




    public void getImage() {

        try {
            left1 = new javafx.scene.image.Image(getClass().getResourceAsStream("/units/ColonyShipLeft.png"));
            right1 = new Image(getClass().getResourceAsStream("/units/ColonyShipRight.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream("/units/SmallSatellite.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/units/SmallSatellite.png"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }




}
