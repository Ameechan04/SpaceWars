package main;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("SpaceWars"); //todo change
        window.setUndecorated(true);  // Remove title bar and borders

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); //set to preferred size of subcomponents
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Set frame to full-screen mode
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(window);
        } else {
            // Fallback if full screen not supported: maximize window
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
            window.setVisible(true);
        }
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.setUpGame();
        gamePanel.startGameThread();
    }
}
