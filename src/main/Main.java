package main;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    private GamePanel gamePanel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SpaceWars");

        // Root pane
        Pane root = new Pane();

        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Create the Scene first
        Scene scene = new Scene(root, screenWidth, screenHeight);

        // Now, initialize GamePanel with the required arguments
        gamePanel = new GamePanel(screenWidth, screenHeight, scene, root);
        gamePanel.setUpGame();

        // Add the GamePanel node to the root pane
        root.getChildren().add(gamePanel);

        // Set up the stage
        primaryStage.setScene(scene);
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenWidth);
        primaryStage.setHeight(screenHeight);
        primaryStage.setFullScreen(true);
        primaryStage.show();

        // Start the game loop
        gamePanel.startGameThread();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
