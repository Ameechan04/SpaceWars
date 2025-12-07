package main;

import entity.Entity;
import entity.Ship;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextBoundsType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*HANDLES ALL ONSCREEN UI SUCH AS TEXT, ITEM ICONS ETC */
public class UI {
    public ArrayList<Ship> selectedShips = new ArrayList<>();
    private final ArrayList<Rectangle2D> shipIconBounds = new ArrayList<>();

    final ArrayList<Message> messages = new ArrayList<>();
    private final int maxMessages = 5;
    private Color messageColour = Color.WHITE;
    private int endOfTextY;
    public boolean starIsSelected;
    GamePanel gamePanel;
    AIDecisionDisplay aidecisionDisplay;

    Font arial_24B, arial_40B, arial_80B, buttonFont;
    Image scoutImage, frigateImage, colonyShipImage, smallSatelliteImage, smallShipyardImage;

    private static final Color SEMI_TRANSPARENT_BLACK = Color.rgb(0, 0, 0, 0.7);
    private static final Color PANEL_COLOUR = Color.rgb(53, 53, 85);
    private static final Color BUTTON_COLOUR = Color.rgb(46, 16, 85);
    private static final Color STATION_FRAME_COLOUR = Color.rgb(170, 18, 18);
    private final double SCREEN_WIDTH;
    private final double SCREEN_HEIGHT;

    public int menuNum = 0;
    public boolean selectedMessageOn = false;
    public String selectedMessage = "";
    public boolean gameFinished = false;
    Star star;

    public Rectangle2D buildScoutButton, buildFrigateButton, buildColonyShipButton, buildBasicShipyardButton, buildSmallSatelliteButton;

    public UI(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.SCREEN_WIDTH = gamePanel.getScene().getWidth();
        this.SCREEN_HEIGHT = gamePanel.getScene().getHeight();
        loadImages();

        arial_24B = Font.font("Arial", FontWeight.BOLD, 24);
        buttonFont = Font.font("Arial", 20);
        arial_40B = Font.font("Arial", FontWeight.BOLD, 40);
        arial_80B = Font.font("Arial", FontWeight.BOLD, 80);

        aidecisionDisplay = new AIDecisionDisplay(gamePanel);
    }

    public void draw(GraphicsContext gc) {
        if (gamePanel.gameState == gamePanel.titleState) {
            drawTitleScreen(gc);
        } else {
            drawDate(gc);
            drawMoney(gc);
            if (starIsSelected && this.star != null) {
                drawStarPanel(gc, star);
                drawColonisationProgress(gc, star);
                drawBuildOptions(gc);
            }

            for (Star star : gamePanel.starMap.getStars()) {
                if (star.colonised == Star.Colonised.BEGUN) {
                    drawColonisationBarBelowStar(gc, star);
                }
            }
            drawMessages(gc);
            aidecisionDisplay.drawAIDebugPanel(gc);
            drawSelectedShipPanel(gc);
        }
    }

    private void drawSelectedShipPanel(GraphicsContext gc) {
        if (selectedShips == null || selectedShips.isEmpty()) return;

        if (selectedShips.size() == 1) {
            Ship single = selectedShips.get(0);
            drawSingleShipPanel(gc, single);
        } else {
            drawMultiShipPanel(gc, selectedShips);
        }
    }

    private void drawMultiShipPanel(GraphicsContext gc, ArrayList<Ship> ships) {
        int padding = 5;
        int iconSize = 32;
        int panelHeight = 150;
        int panelWidth = 300;
        int panelX = 20;
        int panelY = (int) (gamePanel.getHeight() - panelHeight - 20);

        Rectangle2D multiShipPanelBounds = new Rectangle2D(panelX, panelY, panelWidth, panelHeight);

        gc.setFill(SEMI_TRANSPARENT_BLACK);
        gc.fillRoundRect(multiShipPanelBounds.getMinX(), multiShipPanelBounds.getMinY(),
                multiShipPanelBounds.getWidth(), multiShipPanelBounds.getHeight(), 10, 10);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(multiShipPanelBounds.getMinX(), multiShipPanelBounds.getMinY(),
                multiShipPanelBounds.getWidth(), multiShipPanelBounds.getHeight(), 10, 10);

        shipIconBounds.clear();

        String currentType = null;
        int x = (int) multiShipPanelBounds.getMinX() + padding;
        int y = (int) multiShipPanelBounds.getMinY() + padding;

        for (Ship ship : ships) {
            if (currentType != null && !ship.name.equals(currentType)) {
                y += iconSize + padding / 2;
                gc.setStroke(Color.GRAY);
                gc.setLineWidth(2);
                gc.strokeLine(multiShipPanelBounds.getMinX(), y, multiShipPanelBounds.getMaxX(), y);
                y += padding;
            }
            currentType = ship.name;

            Image shipImage = ship.facingLeft ? ship.left1 : ship.right1;
            if (shipImage != null) {
                gc.drawImage(shipImage, x, y, iconSize, iconSize);
            } else {
                gc.setFill(Color.CYAN);
                gc.fillRect(x, y, iconSize, iconSize);
            }
            shipIconBounds.add(new Rectangle2D(x, y, iconSize, iconSize));

            x += iconSize + padding;
            if (x + iconSize > multiShipPanelBounds.getMinX() + multiShipPanelBounds.getWidth()) {
                x = (int) multiShipPanelBounds.getMinX() + padding;
                y += iconSize + padding;
            }
        }
    }

    private void drawTitleScreen(GraphicsContext gc) {
        double screenWidth = gamePanel.getWidth();
        double screenHeight = gamePanel.getHeight();

        gc.setFont(arial_80B);
        String text = "SpaceWars!";
        double textWidth = textWidth(gc, text, arial_80B);
        double x = screenWidth / 2 - textWidth / 2;
        double y = gamePanel.TILE_SIZE * 2;

        gc.setFill(Color.MAGENTA);
        gc.fillText(text, x + 3, y + 5);

        gc.setFill(Color.WHITE);
        gc.fillText(text, x, y);

        x = screenWidth / 2;
        double imageY = gamePanel.TILE_SIZE / 2.0;
        try {
            Image image = new Image(getClass().getResourceAsStream("/units/ScoutShipLeft.png"));
            gc.drawImage(image, x - (36 * 8), imageY, gamePanel.TILE_SIZE * 12, gamePanel.TILE_SIZE * 12);
        } catch (Exception e) {
            e.printStackTrace();
        }

        gc.setFont(arial_24B);
        text = "New Game";
        textWidth = textWidth(gc, text, arial_24B);
        x = screenWidth / 2 - textWidth / 2;
        y += gamePanel.TILE_SIZE * 8;
        gc.fillText(text, x, y);
        if (menuNum == 0) {
            gc.fillText(">", x - (gamePanel.TILE_SIZE / 2.0), y);
        }

        text = "Load Game";
        textWidth = textWidth(gc, text, arial_24B);
        x = screenWidth / 2 - textWidth / 2;
        y += gamePanel.TILE_SIZE * 2;
        gc.fillText(text, x, y);
        if (menuNum == 1) {
            gc.fillText(">", x - (gamePanel.TILE_SIZE / 2.0), y);
        }

        text = "Quit";
        textWidth = textWidth(gc, text, arial_24B);
        x = screenWidth / 2 - textWidth / 2;
        y += gamePanel.TILE_SIZE * 2;
        gc.fillText(text, x, y);
        if (menuNum == 2) {
            gc.fillText(">", x - (gamePanel.TILE_SIZE / 2.0), y);
        }
    }

    public void setStar(Star star) {
        this.star = star;
    }

    private void loadImages() {
        try {
            scoutImage = new Image(getClass().getResourceAsStream("/generatedImages/spaceship1.jpeg"));
            frigateImage = new Image(getClass().getResourceAsStream("/generatedImages/frigateShip.jpeg"));
            colonyShipImage = new Image(getClass().getResourceAsStream("/generatedImages/colonyShip.jpeg"));
            smallSatelliteImage = new Image(getClass().getResourceAsStream("/generatedImages/smallSatellite.jpeg"));
            smallShipyardImage = new Image(getClass().getResourceAsStream("/generatedImages/smallShipyard.jpeg"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawDate(GraphicsContext gc) {
        String formattedDate = String.format("%02d / %02d / %04d",
                gamePanel.gameClock.day, gamePanel.gameClock.month, gamePanel.gameClock.year);

        gc.setFont(arial_40B);
        double textWidth = textWidth(gc, formattedDate, arial_40B);
        double textHeight = gc.getFont().getSize();
        int padding = 10;

        int panelWidth = (int) (gamePanel.getWidth() / 5);
        int panelStartX = (int) (gamePanel.getWidth() - panelWidth);
        double x = panelStartX - textWidth - 2 * padding - 10;
        double y = 20;

        gc.setFill(SEMI_TRANSPARENT_BLACK);
        gc.fillRoundRect(x, y, textWidth + 2 * padding, textHeight + 2 * padding, 10, 10);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, textWidth + 2 * padding, textHeight + 2 * padding, 10, 10);

        gc.setFill(Color.WHITE);
        gc.fillText(formattedDate, x + padding, y + padding + gc.getFont().getSize());
    }

    private void drawStarPanel(GraphicsContext gc, Star star) {
        double panelWidth = gamePanel.getWidth() / 5;
        double panelStartX = gamePanel.getWidth() - panelWidth;
        double y = 0;

        gc.setFill(PANEL_COLOUR);
        gc.fillRect(panelStartX, 0, panelWidth, gamePanel.getHeight());

        gc.setFill(Color.WHITE);

        gc.setFont(arial_40B);
        String message = star.name;
        double textWidth = textWidth(gc, message, arial_40B);
        double textHeight = gc.getFont().getSize();
        double x = panelStartX + (panelWidth - textWidth) / 2;
        gc.fillText(message, x, 65);

        gc.setFont(arial_24B);

        message = gamePanel.humanPlayer.getVisitedStars().contains(star) ? "Quality: " + star.quality : "Quality: UNKNOWN";
        textWidth = textWidth(gc, message, arial_24B);
        x = panelStartX + (panelWidth - textWidth) / 2;
        y = textHeight + 70;
        gc.fillText(message, x, y);

        message = gamePanel.humanPlayer.getVisitedStars().contains(star) ? "Status: " + star.colonised : "Status: UNEXPLORED";
        textWidth = textWidth(gc, message, arial_24B);
        x = panelStartX + (panelWidth - textWidth) / 2;
        y += 50;
        gc.fillText(message, x, y);

        message = "Population:";
        textWidth = textWidth(gc, message, arial_24B);
        x = panelStartX + (panelWidth - textWidth) / 2;
        y += 50;
        gc.fillText(message, x, y);

        message = !gamePanel.humanPlayer.getVisitedStars().contains(star) ? "UNKNOWN" :
                (star.colonised == Star.Colonised.COLONISED ? String.format("%,d", star.population) : "None");

        gc.setFont(buttonFont);
        textWidth = textWidth(gc, message, buttonFont);
        x = panelStartX + (panelWidth - textWidth) / 2;
        y += 30;
        gc.fillText(message, x, y);

        message = star.station != null ? "Station: " + star.station.name : "Station: None";
        gc.setFont(arial_24B);
        textWidth = textWidth(gc, message, arial_24B);
        x = panelStartX + (panelWidth - textWidth) / 2;
        y += 50;
        gc.fillText(message, x, y);

        message = "Satellites:";
        textWidth = textWidth(gc, message, arial_24B);
        x = panelStartX + (panelWidth - textWidth) / 2;
        y += 50;
        gc.fillText(message, x, y);

        message = star.satellites.isEmpty() ? "No Satellites" : star.satellites.getFirst().name + " x " + star.satellites.size();
        gc.setFont(buttonFont);
        textWidth = textWidth(gc, message, buttonFont);
        x = panelStartX + (panelWidth - textWidth) / 2;
        y += 30;
        gc.fillText(message, x, y);
        endOfTextY = (int) y;
    }

    private void drawBuildOptions(GraphicsContext gc) {
        if (star.owner != Entity.Faction.PLAYER) return;
        int padding = 10;
        int frameWidth = 40;
        int buttonWidth = 240;
        double panelWidth = gamePanel.getWidth() / 5;
        double panelStartX = gamePanel.getWidth() - panelWidth;
        double layoutStartX = panelStartX + (panelWidth - (padding + frameWidth + buttonWidth + padding)) / 2;

        gc.setFont(buttonFont);
        double y = endOfTextY + 20;
        double resetHeight = y;

        if (star.station == null && star.colonised == Star.Colonised.COLONISED) {
            Rectangle2D buildBasicShipyardButtonBounds = new Rectangle2D(layoutStartX + padding + frameWidth, y, buttonWidth, 40);
            gc.setFill(BUTTON_COLOUR);
            gc.fillRect(buildBasicShipyardButtonBounds.getMinX(), buildBasicShipyardButtonBounds.getMinY(), buildBasicShipyardButtonBounds.getWidth(), buildBasicShipyardButtonBounds.getHeight());

            gc.setFill(STATION_FRAME_COLOUR);
            gc.fillRect(layoutStartX + padding, y, frameWidth, 40);

            gc.setFill(Color.WHITE);
            String text = "Build Basic Shipyard";
            double textWidth = textWidth(gc, text, buttonFont);
            double textX = buildBasicShipyardButtonBounds.getMinX() + (buildBasicShipyardButtonBounds.getWidth() - textWidth) / 2;
            double textY = buildBasicShipyardButtonBounds.getMinY() + (buildBasicShipyardButtonBounds.getHeight() + buttonFont.getSize()) / 2 - 2;
            gc.fillText(text, textX, textY);

            gc.drawImage(smallShipyardImage, layoutStartX + padding, y, frameWidth, 40);
            this.buildBasicShipyardButton = buildBasicShipyardButtonBounds;
        } else if (star.station != null && star.station.name.equals("Basic Shipyard")) {
            Rectangle2D buildScoutButtonBounds = new Rectangle2D(layoutStartX + padding + frameWidth, y, buttonWidth, 40);
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(layoutStartX + padding, y, frameWidth, 40);
            y += 50;
            Rectangle2D buildFrigateButtonBounds = new Rectangle2D(layoutStartX + padding + frameWidth, y, buttonWidth, 40);
            gc.fillRect(layoutStartX + padding, y, frameWidth, 40);
            y += 50;
            Rectangle2D buildColonyShipButtonBounds = new Rectangle2D(layoutStartX + padding + frameWidth, y, buttonWidth, 40);
            gc.fillRect(layoutStartX + padding, y, frameWidth, 40);
            y = resetHeight;

            gc.drawImage(scoutImage, layoutStartX + padding, y, frameWidth, 40);
            y += 50;
            gc.drawImage(frigateImage, layoutStartX + padding, y, frameWidth, 40);
            y += 50;
            gc.drawImage(colonyShipImage, layoutStartX + padding, y, frameWidth, 40);

            gc.setFill(BUTTON_COLOUR);
            gc.fillRect(buildScoutButtonBounds.getMinX(), buildScoutButtonBounds.getMinY(), buildScoutButtonBounds.getWidth(), buildScoutButtonBounds.getHeight());
            gc.fillRect(buildFrigateButtonBounds.getMinX(), buildFrigateButtonBounds.getMinY(), buildFrigateButtonBounds.getWidth(), buildFrigateButtonBounds.getHeight());
            gc.fillRect(buildColonyShipButtonBounds.getMinX(), buildColonyShipButtonBounds.getMinY(), buildColonyShipButtonBounds.getWidth(), buildColonyShipButtonBounds.getHeight());

            gc.setFill(Color.WHITE);
            String name = "Scout";
            String text = name + "  ₡" + gamePanel.humanPlayer.getBuildCost(name.toLowerCase());
            double textWidth = textWidth(gc, text, buttonFont);
            double textX = buildScoutButtonBounds.getMinX() + (buildScoutButtonBounds.getWidth() - textWidth) / 2;
            double textY = buildScoutButtonBounds.getMinY() + (buildScoutButtonBounds.getHeight() + buttonFont.getSize()) / 2 - 2;
            gc.fillText(text, textX, textY);

            name = "Frigate";
            text = name + "  ₡" + gamePanel.humanPlayer.getBuildCost(name.toLowerCase());
            textWidth = textWidth(gc, text, buttonFont);
            textX = buildFrigateButtonBounds.getMinX() + (buildFrigateButtonBounds.getWidth() - textWidth) / 2;
            textY = buildFrigateButtonBounds.getMinY() + (buildFrigateButtonBounds.getHeight() + buttonFont.getSize()) / 2 - 2;
            gc.fillText(text, textX, textY);

            name = "Colony Ship";
            text = name + "  ₡" + gamePanel.humanPlayer.getBuildCost(name.toLowerCase().replace(" ", ""));
            textWidth = textWidth(gc, text, buttonFont);
            textX = buildColonyShipButtonBounds.getMinX() + (buildColonyShipButtonBounds.getWidth() - textWidth) / 2;
            textY = buildColonyShipButtonBounds.getMinY() + (buildColonyShipButtonBounds.getHeight() + buttonFont.getSize()) / 2 - 2;
            gc.fillText(text, textX, textY);

            this.buildScoutButton = buildScoutButtonBounds;
            this.buildFrigateButton = buildFrigateButtonBounds;
            this.buildColonyShipButton = buildColonyShipButtonBounds;
        }

        if (star.colonised == Star.Colonised.COLONISED) {
            y = y + 50;
            Rectangle2D buildSmallSatelliteButtonBounds = new Rectangle2D(layoutStartX + padding + frameWidth, y, buttonWidth, 40);

            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(layoutStartX + padding, y, frameWidth, 40);
            gc.setFill(BUTTON_COLOUR);
            gc.fillRect(buildSmallSatelliteButtonBounds.getMinX(), buildSmallSatelliteButtonBounds.getMinY(), buildSmallSatelliteButtonBounds.getWidth(), buildSmallSatelliteButtonBounds.getHeight());

            gc.drawImage(smallSatelliteImage, layoutStartX + padding, y, frameWidth, 40);
            String name = "Small Satellite";
            String nameTransformed = "smallsatellite";
            String text = name + "  ₡" + gamePanel.humanPlayer.getBuildCost(nameTransformed);
            gc.setFont(buttonFont);
            gc.setFill(Color.WHITE);
            double textWidth = textWidth(gc, text, buttonFont);
            double textX = buildSmallSatelliteButtonBounds.getMinX() + (buildSmallSatelliteButtonBounds.getWidth() - textWidth) / 2;
            double textY = buildSmallSatelliteButtonBounds.getMinY() + (buildSmallSatelliteButtonBounds.getHeight() + buttonFont.getSize()) / 2 - 2;
            gc.fillText(text, textX, textY);

            this.buildSmallSatelliteButton = buildSmallSatelliteButtonBounds;

            LinkedList<BuildTask> queue = gamePanel.shipBuilderHelper.starQueues.get(star);
            if (queue != null && !queue.isEmpty()) {
                BuildTask buildTask = queue.peek();
                double x = layoutStartX + padding;
                y = resetHeight;
                switch (buildTask.buildType) {
                    case "scout":
                        drawProgressBar(gc, x, y, 40, 40, buildTask.getProgress());
                        break;
                    case "frigate":
                        drawProgressBar(gc, x, y + 50, 40, 40, buildTask.getProgress());
                        break;
                    case "colonyship":
                        drawProgressBar(gc, x, y + 100, 40, 40, buildTask.getProgress());
                        break;
                    case "basicshipyard":
                        drawProgressBar(gc, x, y, 40, 40, buildTask.getProgress());
                        break;
                }
                if (buildTask.buildType.equals("smallsatellite")) {
                    if (star.station != null) {
                        drawProgressBar(gc, x, y + 150, 40, 40, buildTask.getProgress());
                    } else {
                        drawProgressBar(gc, x, y + 50, 40, 40, buildTask.getProgress());
                    }
                }
            }
        }
    }

    private void drawColonisationProgress(GraphicsContext gc, Star star) {
        if (star.colonised != Star.Colonised.BEGUN) return;
        gc.setFont(arial_24B);
        gc.setFill(Color.WHITE);
        String message = "Colonisation Progress:";
        double textWidth = textWidth(gc, message, arial_24B);
        double panelWidth = gamePanel.getWidth() / 5;
        double panelStartX = gamePanel.getWidth() - panelWidth;
        double x = panelStartX + (panelWidth - textWidth) / 2;
        gc.fillText(message, x, endOfTextY + 50);

        double progress = (gamePanel.gameClock.getTotalGameDays() - star.colonisationStartDate) / 180.0;
        double barX = panelStartX + 20;
        double barY = endOfTextY + 90;
        double barWidth = panelWidth - 40;
        int barHeight = 40;
        drawProgressBar(gc, barX, barY, barWidth, barHeight, progress);
    }

    private void drawColonisationBarBelowStar(GraphicsContext gc, Star star) {
        double progress = (gamePanel.gameClock.getTotalGameDays() - star.colonisationStartDate) / 180.0;
        double zoom = gamePanel.zoomProperty.get();
        double barWidth = 20 * zoom;
        double barHeight = 10 * zoom;

        double screenX = ((star.x - barWidth / 2.0 - gamePanel.cameraOffsetXProperty.get()) * zoom);
        double screenY = ((star.y + 15 - gamePanel.cameraOffsetYProperty.get()) * zoom);
        drawProgressBar(gc, screenX, screenY, barWidth, barHeight, progress);
    }

    public void addMessage(String text) {
        if (messages.size() >= maxMessages) {
            messages.remove(0);
        }
        messages.add(new Message(text, 3.0));
        messageColour = Color.WHITE;
    }

    public void addMessage(String text, String colour) {
        if (messages.size() >= maxMessages) {
            messages.remove(0);
        }
        switch (colour.toLowerCase()) {
            case "green":
                messageColour = Color.rgb(5, 211, 18);
                break;
            case "red":
                messageColour = Color.rgb(255, 0, 0);
                break;
            default:
                messageColour = Color.WHITE;
        }
        messages.add(new Message(text, 3.0));
    }

    private void drawMoney(GraphicsContext gc) {
        String moneyText = "₡" + gamePanel.humanPlayer.getMoney();
        gc.setFont(Font.font(gc.getFont().getName(), FontWeight.BOLD, 24));
        double textWidth = textWidth(gc, moneyText, gc.getFont());
        double x = (gamePanel.getWidth() / 2) - (textWidth / 2);
        double y = gamePanel.TILE_SIZE;

        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRoundRect(x - 8, y - 24, textWidth + 16, 28, 10, 10);
        gc.setFill(Color.YELLOW);
        gc.fillText(moneyText, x, y);
    }

    private void drawMessages(GraphicsContext gc) {
        int x = 20;
        int y = 20;
        int lineHeight = 20;
        gc.setFont(Font.font("Arial", 14));

        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            double drawY = y + i * lineHeight;

            gc.setFill(SEMI_TRANSPARENT_BLACK);
            gc.fillRoundRect(x - 5, drawY - 15, textWidth(gc, msg.text, gc.getFont()) + 10, 20, 10, 10);

            gc.setFill(messageColour);
            gc.fillText(msg.text, x, drawY);
        }
    }

    public void updateMessages(double elapsedDays) {
        Iterator<Message> iterator = messages.iterator();
        while (iterator.hasNext()) {
            Message msg = iterator.next();
            msg.life -= elapsedDays;
            if (msg.life <= 0) {
                iterator.remove();
            }
        }
    }

    public void drawProgressBar(GraphicsContext gc, double x, double y, double width, double height, double progress) {
        progress = Math.max(0.0, Math.min(1.0, progress));
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, width, height);

        int filledWidth = (int) (width * progress);
        gc.setFill(Color.GREEN);
        gc.fillRect(x + 1, y + 1, filledWidth - 1, height - 1);
    }

    private void drawSingleShipPanel(GraphicsContext gc, Ship selectedShip) {
        if (selectedShip == null) return;

        int panelWidth = 300;
        int panelHeight = 120;
        int padding = 10;
        double x = 20;
        double y = gamePanel.getHeight() - panelHeight - 20;

        gc.setFill(SEMI_TRANSPARENT_BLACK);
        gc.fillRoundRect(x, y, panelWidth, panelHeight, 10, 10);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, panelWidth, panelHeight, 10, 10);

        int shipSquareSize = 80;
        double shipX = x + padding;
        double shipY = y + padding;
        Image shipImage = selectedShip.facingLeft ? selectedShip.left1 : selectedShip.right1;
        if (shipImage != null) {
            gc.drawImage(shipImage, shipX, shipY, shipSquareSize, shipSquareSize);
        }

        gc.setFont(arial_24B);
        String name = selectedShip.name;
        double nameX = shipX;
        double nameY = shipY - 5;
        gc.setFill(Color.WHITE);
        gc.fillText(name, nameX, nameY);

        double statsX = shipX + shipSquareSize + padding;
        double statsY = shipY + 20;
        gc.setFont(buttonFont);
        gc.setFill(Color.WHITE);

        String healthText = "Health: " + selectedShip.getCurrentHealth() + " / " + selectedShip.getMaxHealth();
        gc.fillText(healthText, statsX, statsY);

        statsY += 25;
        String damageText = "Damage: " + selectedShip.getDamage();
        gc.fillText(damageText, statsX, statsY);

        statsY += 25;
        String currentStarText = "Current Star: " + (selectedShip.currentStar != null ? selectedShip.currentStar.name : "Moving");
        gc.fillText(currentStarText, statsX, statsY);

        statsY += 25;
        String targetStarText = "Moving to: " + (selectedShip.targetStar != null ? selectedShip.targetStar.name : "None");
        gc.fillText(targetStarText, statsX, statsY);
    }

    // Helper method to measure text width, as FontMetrics is AWT-specific
    private double textWidth(GraphicsContext gc, String text, Font font) {
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(font);
        tempText.setBoundsType(TextBoundsType.VISUAL);
        return tempText.getBoundsInLocal().getWidth();
    }
}