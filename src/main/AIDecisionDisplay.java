package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class AIDecisionDisplay
{
    private final ArrayList<DebugMessage> aiDebugMessages = new ArrayList<>();
    private final int maxAIDebugMessages = 9;

    private Color messageColour = Color.white;

    Font arial_24B, arial_40B, arial_80B, buttonFont;

    private final int SCREEN_WIDTH;
    private final int SCREEN_HEIGHT;
//    private AIBrain aibrain;
    GamePanel gamePanel;


    public AIDecisionDisplay(GamePanel gamePanel) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        SCREEN_WIDTH = screenSize.width;
        SCREEN_HEIGHT = screenSize.height;
        this.gamePanel = gamePanel;
    }

    public void addAIDebugMessage(String text) {
        if (aiDebugMessages.size() >= maxAIDebugMessages) {
            aiDebugMessages.remove(0);
        }
        aiDebugMessages.add(new DebugMessage(text, Color.WHITE));
    }


    public void addAIDebugMessage(String text, String color) {
        Color chosen;
        switch (color.toLowerCase()) {
            case "red": chosen = Color.RED; break;
            case "blue": chosen = Color.BLUE; break;
            case "green": chosen = Color.GREEN; break;
            case "yellow": chosen = Color.YELLOW; break;
            default: chosen = Color.WHITE;
        }
        if (aiDebugMessages.size() >= maxAIDebugMessages) {
            aiDebugMessages.remove(0);
        }
        aiDebugMessages.add(new DebugMessage(text, chosen));
    }

    void drawAIDebugPanel(Graphics2D g2) {
//        if (aibrain == null) return; // safe guard
        AIBrain aiBrain = gamePanel.ai;

        int panelWidth = 500;
        int panelHeight = 300;
        int x = (SCREEN_WIDTH - panelWidth) / 2;
        int y = SCREEN_HEIGHT - panelHeight - 20; // 20 px from bottom

        // Background
        g2.setColor(new Color(30, 30, 30, 200)); // semi-transparent dark
        g2.fillRoundRect(x, y, panelWidth, panelHeight, 10, 10);

        // Border
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x, y, panelWidth, panelHeight, 10, 10);

        // Title
        g2.setFont(arial_24B);
        String title = "AI Brain Logic | AI Balance: " + aiBrain.balance + " | AI Revenue: " + aiBrain.getRevenue();
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, x + (panelWidth - titleWidth) / 2, y + 30);

        int[] starQualities = new int[5];
        starQualities[0] = aiBrain.exploredStarQualities.get(Star.Quality.POOR);
        starQualities[1] = aiBrain.exploredStarQualities.get(Star.Quality.MEDIUM);
        starQualities[2] = aiBrain.exploredStarQualities.get(Star.Quality.RICH);
        starQualities[3] = aiBrain.exploredStarQualities.get(Star.Quality.BARREN);
        starQualities[4] = aiBrain.exploredStarQualities.get(Star.Quality.UNINHABITABLE);

        title = "Explored Stars: Poor: " + starQualities[0] + " Medium: " + starQualities[1] + " Rich: " + starQualities[2];
        titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, x + (panelWidth - titleWidth) / 2, y + 50);

        title = "Barren: " + starQualities[3] + " Uninhabitable: " + starQualities[4];
        titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, x + (panelWidth - titleWidth) / 2, y + 70);

        // Debug messages
        g2.setFont(buttonFont);
        int lineHeight = 20;
        int startY = y + 110;
        for (int i = 0; i < aiDebugMessages.size(); i++) {
            DebugMessage msg = aiDebugMessages.get(i);
            g2.setColor(msg.color);
            g2.drawString(msg.text, x + 10, startY + i * lineHeight);
        }
    }

    private void resetMessageColour() {
        messageColour = Color.white;
    }

    private static class DebugMessage {
        String text;
        Color color;

        DebugMessage(String text, Color color) {
            this.text = text;
            this.color = color;
        }
    }



}
