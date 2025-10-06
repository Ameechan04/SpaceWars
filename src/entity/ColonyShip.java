package entity;

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import main.GamePanel;
import main.KeyHandler;
import main.Star;

import java.io.IOException;

public class ColonyShip extends Ship{
    public ColonyShip(GamePanel gamePanel, Star currentStar, Faction faction) {
        super(gamePanel, "Colony Ship", currentStar, 0.5, 30, -30, gamePanel.humanPlayer.getBuildCost("colonyship"), 200, 1, faction);



        this.solidOffsetX = 2;
        this.solidOffsetY = 5;
        solidArea = new Rectangle(worldX + solidOffsetX , worldY + solidOffsetY, gamePanel.TILE_SIZE - 5 , gamePanel.TILE_SIZE - 20);
        solidAreaDefaultX = worldX;
        solidAreaDefaultY = worldY;

        getShipImage();
        setCentrePosition(currentStar.x, currentStar.y);

    }


    public void getShipImage() {
        try {
            left1 = new Image(getClass().getResourceAsStream("/units/ColonyShipLeft.png"));
            right1 = new Image(getClass().getResourceAsStream("/units/ColonyShipRight.png"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }







}
