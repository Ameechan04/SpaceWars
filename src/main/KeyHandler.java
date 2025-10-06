package main;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyHandler {

    public boolean upPressed, downPressed, leftPressed, rightPressed;

    private final GamePanel gp;

    public KeyHandler(GamePanel gp, Scene scene) {
        this.gp = gp;

        // Key pressed handler
        scene.setOnKeyPressed(this::handleKeyPressed);

        // Key released handler
        scene.setOnKeyReleased(this::handleKeyReleased);
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode keyCode = event.getCode();

        // Title state navigation
        if (gp.gameState == gp.titleState) {
            if (keyCode == KeyCode.W || keyCode == KeyCode.UP) {
                gp.ui.menuNum--;
                if (gp.ui.menuNum < 0) gp.ui.menuNum = 2;
            }
            if (keyCode == KeyCode.S || keyCode == KeyCode.DOWN) {
                gp.ui.menuNum++;
                if (gp.ui.menuNum > 2) gp.ui.menuNum = 0;
            }
            if (keyCode == KeyCode.ENTER) {
                switch (gp.ui.menuNum) {
                    case 0 -> gp.gameState = gp.playState;
                    case 1 -> { /* load not implemented yet */ }
                    case 2 -> System.exit(0);
                }
            }
        } else { // In-game shortcuts
            if (keyCode == KeyCode.P) {
                if (gp.gameClock.gameSpeed != 0) gp.gameClock.setGameSpeed(0);
                else gp.gameClock.setGameSpeed(1);
            }
            if (keyCode == KeyCode.RIGHT) gp.gameClock.gameSpeed++;
            if (keyCode == KeyCode.LEFT) gp.gameClock.gameSpeed--;
        }

        // Movement keys
        if (keyCode == KeyCode.W) upPressed = true;
        if (keyCode == KeyCode.S) downPressed = true;
        if (keyCode == KeyCode.A) leftPressed = true;
        if (keyCode == KeyCode.D) rightPressed = true;
    }

    private void handleKeyReleased(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        if (keyCode == KeyCode.W) upPressed = false;
        if (keyCode == KeyCode.S) downPressed = false;
        if (keyCode == KeyCode.A) leftPressed = false;
        if (keyCode == KeyCode.D) rightPressed = false;
    }
}
