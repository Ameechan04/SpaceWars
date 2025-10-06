package entity;

import javafx.scene.image.Image;
import main.GamePanel;
import main.Star;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Scout extends Ship {
    public Scout(GamePanel gamePanel, Star currentStar, Faction faction) {
        super(gamePanel, "Scout", currentStar, 1.02, 25, 25, gamePanel.humanPlayer.getBuildCost("scout"), 50, 5, faction);

        this.solidOffsetX = 15;
        this.solidOffsetY = 15;

        setupSolidArea(gamePanel.TILE_SIZE / 3, gamePanel.TILE_SIZE / 3);

        setCentrePosition(currentStar.x , currentStar.y );

        getShipImage();
    }

    public void getShipImage() {
        try {
            left1 = new javafx.scene.image.Image(getClass().getResourceAsStream("/units/ScoutShipLeft.png"));
            right1 = new Image(getClass().getResourceAsStream("/units/ScoutShipRight.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
