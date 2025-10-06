package main;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class AIDecisionDisplay {

    private final ArrayList<DebugMessage> aiDebugMessages = new ArrayList<>();
    private final int maxAIDebugMessages = 9;

    private Color messageColour = Color.WHITE;

    private Font arial_24B = Font.font("Arial", 24);
    private Font buttonFont = Font.font("Arial", 16);

    private final double SCREEN_WIDTH;
    private final double SCREEN_HEIGHT;

    GamePanel gamePanel;

    public AIDecisionDisplay(GamePanel gamePanel, double screenWidth, double screenHeight) {
        this.SCREEN_WIDTH = screenWidth;
        this.SCREEN_HEIGHT = screenHeight;
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

    public void drawAIDebugPanel(GraphicsContext gc) {
        AIBrain aiBrain = gamePanel.ai;

        double panelWidth = 500;
        double panelHeight = 300;
        double x = (SCREEN_WIDTH - panelWidth) / 2;
        double y = SCREEN_HEIGHT - panelHeight - 20; // 20 px from bottom

        // Background
        gc.setFill(Color.rgb(30, 30, 30, 0.8));
        gc.fillRoundRect(x, y, panelWidth, panelHeight, 10, 10);

        // Border
        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, panelWidth, panelHeight, 10, 10);

        // Title
        gc.setFont(arial_24B);
        String title = "AI Brain Logic | AI Balance: " + aiBrain.balance + " | AI Revenue: " + aiBrain.getRevenue();
        double titleWidth = computeTextWidth(title, gc.getFont());
        gc.setFill(Color.WHITE);
        gc.fillText(title, x + (panelWidth - titleWidth) / 2, y + 30);

        int[] starQualities = new int[5];
        starQualities[0] = aiBrain.exploredStarQualities.get(Star.Quality.POOR);
        starQualities[1] = aiBrain.exploredStarQualities.get(Star.Quality.MEDIUM);
        starQualities[2] = aiBrain.exploredStarQualities.get(Star.Quality.RICH);
        starQualities[3] = aiBrain.exploredStarQualities.get(Star.Quality.BARREN);
        starQualities[4] = aiBrain.exploredStarQualities.get(Star.Quality.UNINHABITABLE);

        title = "Explored Stars: Poor: " + starQualities[0] + " Medium: " + starQualities[1] + " Rich: " + starQualities[2];
        titleWidth = computeTextWidth(title, gc.getFont());
        gc.fillText(title, x + (panelWidth - titleWidth) / 2, y + 50);

        title = "Barren: " + starQualities[3] + " Uninhabitable: " + starQualities[4];
        titleWidth = computeTextWidth(title, gc.getFont());
        gc.fillText(title, x + (panelWidth - titleWidth) / 2, y + 70);

        // Debug messages
        gc.setFont(buttonFont);
        double lineHeight = 20;
        double startY = y + 110;
        for (int i = 0; i < aiDebugMessages.size(); i++) {
            DebugMessage msg = aiDebugMessages.get(i);
            gc.setFill(msg.color);
            gc.fillText(msg.text, x + 10, startY + i * lineHeight);
        }
    }



    private double computeTextWidth(String text, Font font) {
        Text tempText = new Text(text);
        tempText.setFont(font);
        return tempText.getLayoutBounds().getWidth();
    }

    private void resetMessageColour() {
        messageColour = Color.WHITE;
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
