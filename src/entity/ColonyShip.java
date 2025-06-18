package entity;

import main.GamePanel;
import main.KeyHandler;
import main.Star;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class ColonyShip extends Ship{
    public ColonyShip(GamePanel gamePanel, Star currentStar) {
        super(gamePanel, "Colony Ship", currentStar, 0.5, 30, -30, gamePanel.buildCosts.get("Colony Ship"), 200, 1);



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
            left1 = ImageIO.read(getClass().getResourceAsStream("/units/ColonyShipLeft.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/units/ColonyShipRight.png"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }







}
