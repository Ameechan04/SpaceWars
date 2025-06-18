package entity;

import main.GamePanel;
import main.Star;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Frigate extends Ship{
    public Frigate(GamePanel gamePanel, Star currentStar) {
        super(gamePanel, "Frigate", currentStar, 1.2, 10, 10, gamePanel.buildCosts.get("Frigate"), 150,15);

        this.solidOffsetX = 15;
        this.solidOffsetY = 15;
        solidArea = new Rectangle(worldX + solidOffsetX , worldY + solidOffsetY, gamePanel.TILE_SIZE / 2 , gamePanel.TILE_SIZE/2);
        solidAreaDefaultX = worldX;
        solidAreaDefaultY = worldY;
        setCentrePosition(currentStar.x, currentStar.y);

        getShipImage();
    }


    public void getShipImage() {

        try {
            left1 = ImageIO.read(getClass().getResourceAsStream("/units/FrigateShipLeft.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/units/FrigateShipRight.png"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
