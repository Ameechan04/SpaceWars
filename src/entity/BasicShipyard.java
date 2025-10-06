package entity;

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import main.GamePanel;
import main.Star;

import javax.imageio.ImageIO;
import java.io.IOException;

public class BasicShipyard extends Station{
    public BasicShipyard(GamePanel gamePanel, Star currentStar, Faction faction) {
        super(gamePanel, "Basic Shipyard", currentStar, gamePanel.humanPlayer.getBuildCost("basicshipyard") , 200, 0, faction);

        solidArea = new Rectangle(worldX, worldY, gamePanel.TILE_SIZE , gamePanel.TILE_SIZE);
        solidAreaDefaultX = worldX;
        solidAreaDefaultY = worldY;
        int orbitX = (int) currentStar.x + this.orbitOffsetX;
        int orbitY = (int) currentStar.y + this.orbitOffsetY;
        this.setCentrePosition(orbitX, orbitY);

        currentStar.setStation(this);
        getImage();
    }


    public void getImage() {
        try {
            left1 = new Image(getClass().getResourceAsStream("/units/ShipyardStation.png"));
            right1 = new Image(getClass().getResourceAsStream("/units/ShipyardStation.png"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
