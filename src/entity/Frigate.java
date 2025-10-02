package entity;

import main.GamePanel;
import main.Star;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Frigate extends Ship{
    public Frigate(GamePanel gamePanel, Star currentStar, Faction faction) {
        super(gamePanel, "Frigate", currentStar, 1.01, 25, 25, gamePanel.humanPlayer.getBuildCost("frigate"), 120, 10, faction);

        this.solidOffsetX = 10;
        this.solidOffsetY = 15;

        setupSolidArea(gamePanel.TILE_SIZE / 2, gamePanel.TILE_SIZE / 3);

        setCentrePosition(currentStar.x , currentStar.y );

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
