package main;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Iterator;
import java.util.LinkedList;

public class MessagePanel extends VBox {

    private static class Message {
        Text text;
        double life; // in game days

        Message(String content, Color color, double life) {
            text = new Text(content);
            text.setFill(color);
            text.setFont(Font.font("Arial", 14));
            this.life = life;
        }
    }

    private final LinkedList<Message> messages = new LinkedList<>();
    private final int maxMessages = 5;

    public MessagePanel() {
        setSpacing(5);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");

        // Optional: auto-update fade over time
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                double elapsedDays = (now - lastUpdate) / 1e9; // approximate seconds to "days"
                updateMessages(elapsedDays);
                lastUpdate = now;
            }
        };
        timer.start();
    }

    public void addMessage(String content) {
        addMessage(content, "white", 3.0);
    }

    public void addMessage(String content, String colour, double life) {
        if (messages.size() >= maxMessages) {
            Message oldest = messages.removeFirst();
            getChildren().remove(oldest.text);
        }

        Color color;
        switch (colour.toLowerCase()) {
            case "red" -> color = Color.RED;
            case "green" -> color = Color.LIMEGREEN;
            default -> color = Color.WHITE;
        }

        Message msg = new Message(content, color, life);
        messages.add(msg);
        getChildren().add(msg.text);
    }

    public void updateMessages(double elapsedDays) {
        boolean changed = false;
        Iterator<Message> iter = messages.iterator();
        while (iter.hasNext()) {
            Message msg = iter.next();
            msg.life -= elapsedDays;
            if (msg.life <= 0) {
                getChildren().remove(msg.text);
                iter.remove();
                changed = true;
            }
        }
    }
}
