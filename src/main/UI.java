package main;

import entity.Entity;
import entity.Ship;
import javafx.geometry.Insets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;

public class UI {

    public ArrayList<Ship> selectedShips = new ArrayList<>();
    public Star star;

    GamePanel gamePanel;
    AIDecisionDisplay aidecisionDisplay;

    Font arial_24B, arial_40B, arial_80B, buttonFont;
    public boolean starIsSelected = false;

    // JavaFX UI elements for the star panel
    public VBox starPanelVBox;
    public Label starNameLabel;
    public Label starQualityLabel;
    public Label starStatusLabel;
    public Label starPopulationLabel;
    public Label starStationLabel;
    public Button buildScoutButton;
    public Button buildFrigateButton;
    public Button buildColonyShipButton;
    public Button buildShipyardButton;
    public Button buildSmallSatelliteButton;

    // Canvas drawing colors
    private static final Color SEMI_TRANSPARENT_BLACK = Color.rgb(0, 0, 0, 0.7);
    private final MessagePanel messagePanel;
    private final Pane rootPane; // the main root pane for JavaFX UI components

    public UI(GamePanel gamePanel, Pane rootPane) {
        this.gamePanel = gamePanel;
        this.rootPane = rootPane;

        // Fonts
        arial_24B = Font.font("Arial", FontWeight.BOLD, 24);
        arial_40B = Font.font("Arial", FontWeight.BOLD, 40);
        arial_80B = Font.font("Arial", FontWeight.BOLD, 80);
        buttonFont = Font.font("Arial", 20);

        aidecisionDisplay = new AIDecisionDisplay(gamePanel, gamePanel.getWidth(), gamePanel.getHeight());

        setupStarPanel();

        // Create MessagePanel
        messagePanel = new MessagePanel();
        messagePanel.setLayoutX(10); // X position
        messagePanel.setLayoutY(10); // Y position
        messagePanel.setPrefWidth(400); // width
        messagePanel.setPrefHeight(200); // height

        // Add to scene graph
        rootPane.getChildren().add(messagePanel);
    }


    public void addMessage(String text) {
        messagePanel.addMessage(text);
    }

    public void addMessage(String text, String colour) {
        messagePanel.addMessage(text, colour, 3.0);
    }

    public void updateMessages(double elapsedTime) {
        messagePanel.updateMessages(elapsedTime);
    }

    /**
     * Sets up the JavaFX star panel with labels and buttons.
     */
    private void setupStarPanel() {
        starPanelVBox = new VBox(10);
        starPanelVBox.setPadding(new Insets(10));
        starPanelVBox.setStyle("-fx-background-color: rgba(53,53,85,0.8);"); // match old panel colour
        starPanelVBox.setPrefWidth(250);

        starNameLabel = new Label("Star Name");
        starNameLabel.setFont(arial_40B);
        starQualityLabel = new Label("Quality: UNKNOWN");
        starQualityLabel.setFont(arial_24B);
        starStatusLabel = new Label("Status: UNEXPLORED");
        starStatusLabel.setFont(arial_24B);
        starPopulationLabel = new Label("Population: None");
        starPopulationLabel.setFont(arial_24B);
        starStationLabel = new Label("Station: None");
        starStationLabel.setFont(arial_24B);

        // Buttons
        buildShipyardButton = new Button("Build Shipyard");
        buildScoutButton = new Button("Build Scout");
        buildFrigateButton = new Button("Build Frigate");
        buildColonyShipButton = new Button("Build Colony Ship");
        buildSmallSatelliteButton = new Button("Build Small Satellite");

        buildShipyardButton.setFont(buttonFont);
        buildScoutButton.setFont(buttonFont);
        buildFrigateButton.setFont(buttonFont);
        buildColonyShipButton.setFont(buttonFont);
        buildSmallSatelliteButton.setFont(buttonFont);

        // Add all to VBox
        starPanelVBox.getChildren().addAll(
                starNameLabel,
                starQualityLabel,
                starStatusLabel,
                starPopulationLabel,
                starStationLabel,
                buildShipyardButton,
                buildScoutButton,
                buildFrigateButton,
                buildColonyShipButton,
                buildSmallSatelliteButton
        );

        starPanelVBox.setVisible(false); // hide by default
    }

    /**
     * Updates the star panel to display info for the selected star.
     */
    public void updateStarPanel(Star star) {
        if (star == null) {
            starPanelVBox.setVisible(false);
            return;
        }

        starPanelVBox.setVisible(true);
        this.star = star;

        starNameLabel.setText(star.name);

        if (gamePanel.humanPlayer.getVisitedStars().contains(star)) {
            starQualityLabel.setText("Quality: " + star.quality);
            starStatusLabel.setText("Status: " + star.colonised);
            starPopulationLabel.setText("Population: " +
                    (star.colonised == Star.Colonised.COLONISED ? star.population : "None"));
            starStationLabel.setText("Station: " + (star.station != null ? star.station.name : "None"));
        } else {
            starQualityLabel.setText("Quality: UNKNOWN");
            starStatusLabel.setText("Status: UNEXPLORED");
            starPopulationLabel.setText("Population: UNKNOWN");
            starStationLabel.setText("Station: None");
        }

        // Show/hide buttons based on star state
        buildShipyardButton.setVisible(star.station == null && star.colonised == Star.Colonised.COLONISED);
        buildScoutButton.setVisible(star.station != null && "Basic Shipyard".equals(star.station.name));
        buildFrigateButton.setVisible(star.station != null && "Basic Shipyard".equals(star.station.name));
        buildColonyShipButton.setVisible(star.station != null && "Basic Shipyard".equals(star.station.name));
        buildSmallSatelliteButton.setVisible(star.colonised == Star.Colonised.COLONISED);
    }

    /**
     * Draws all custom canvas elements for the main gameplay.
     */
    public void draw(GraphicsContext gc) {
        // Draw main game graphics
        gc.setFill(SEMI_TRANSPARENT_BLACK);
        gc.fillRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());

        // Draw messages, ships, colonisation bars, etc.
        aidecisionDisplay.drawAIDebugPanel(gc);

        // Keep star panel separate (JavaFX nodes), no drawing here
    }
}
