package entity;

import main.GamePanel;
import main.Star;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Scout extends Ship{
    public Scout(GamePanel gamePanel, Star currentStar) {
        super(gamePanel, "Scout", currentStar, 2, 5, 5, gamePanel.buildCosts.get("Scout"), 50, 5);

        System.out.println("scout created");

        this.solidOffsetX = 15;
        this.solidOffsetY = 15;

        updateCentreFromWorldPosition();
        System.out.println("centre x, y : " + this.centreX + ", " + this.centreY);

//        setupSolidArea(worldX + solidOffsetX , worldY + solidOffsetY, gamePanel.TILE_SIZE / 3 , gamePanel.TILE_SIZE/3);

        //only width and height matter
        solidArea =  new Rectangle(worldX + solidOffsetX , worldY + solidOffsetY, gamePanel.TILE_SIZE / 3 , gamePanel.TILE_SIZE/3);
        solidAreaDefaultX = worldX;
        solidAreaDefaultY = worldY;
//        setCentrePosition(currentStar.x, currentStar.y);
        //setCentrePosition(worldX, worldY);

        getShipImage();
    }


    public void getShipImage() {

        try {
            left1 = ImageIO.read(getClass().getResourceAsStream("/units/ScoutShipLeft.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/units/ScoutShipRight.png"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
