package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

public class Main extends Application {

    private GamePanel gamePanel;
    private final int WIDTH = 1200;
    private final int HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SpaceWars");

        // Root pane
        Pane root = new Pane();

        // Initialize game logic
        gamePanel = new GamePanel();
        gamePanel.setUpGame();

        // Add the GamePanel node to the JavaFX scene
        root.getChildren().add(gamePanel);

        // Set up full-screen using screen bounds
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());

        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true); // optional
        primaryStage.show();

        // Start the existing game loop inside GamePanel
        gamePanel.startGameThread();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
