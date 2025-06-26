package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler  implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;

    GamePanel gp;
    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }
    //unused
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();


        if (gp.gameState == gp.titleState) {
            if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
                gp.ui.menuNum--;
                if (gp.ui.menuNum < 0) {
                    gp.ui.menuNum = 2;
                }
            }
            if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
                gp.ui.menuNum++;
                if (gp.ui.menuNum > 2) {
                    gp.ui.menuNum = 0;
                }
            }

            if (keyCode == KeyEvent.VK_ENTER) {
                switch (gp.ui.menuNum) {
                    case 0:
                        gp.gameState = gp.playState;
                        break;
                    case 1:
                        //load not implemented yet
                            break;
                    case 2:
                        System.exit(0);
                }
            }
        } else {

            if (keyCode == KeyEvent.VK_P) {
                if (gp.gameClock.gameSpeed != 0) {
                    gp.gameClock.setGameSpeed(0);
                } else {
                    gp.gameClock.setGameSpeed(1);
                }
            }

            if (keyCode == KeyEvent.VK_RIGHT) {
                gp.gameClock.gameSpeed++;
            }

            if (keyCode == KeyEvent.VK_LEFT) {
                gp.gameClock.gameSpeed--;
            }





        }


        if (keyCode == KeyEvent.VK_W) {
            upPressed = true;
        }
        if (keyCode == KeyEvent.VK_S) {
            downPressed = true;
        }
        if (keyCode == KeyEvent.VK_A) {
            leftPressed = true;
        }
        if (keyCode == KeyEvent.VK_D) {
            rightPressed = true;
        }
//        System.out.println("key pressed: " + keyCode);

    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W) {
            upPressed = false;
        }
        if (keyCode == KeyEvent.VK_S) {
            downPressed = false;
        }
        if (keyCode == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if (keyCode == KeyEvent.VK_D) {
            rightPressed = false;
        }
    }
}
