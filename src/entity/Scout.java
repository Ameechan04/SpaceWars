package entity;

import main.GamePanel;
import main.Star;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Scout extends Ship {
    public Scout(GamePanel gamePanel, Star currentStar) {
        super(gamePanel, "Scout", currentStar, 2, 25, 25, gamePanel.buildCosts.get("Scout"), 50, 5);

        this.solidOffsetX = 15;
        this.solidOffsetY = 15;

        setupSolidArea(gamePanel.TILE_SIZE / 3, gamePanel.TILE_SIZE / 3);

        setCentrePosition(currentStar.x , currentStar.y );

        getShipImage();
    }

    public void getShipImage() {
        try {
            left1 = ImageIO.read(getClass().getResourceAsStream("/units/ScoutShipLeft.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/units/ScoutShipRight.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
