package main;

import entity.Entity;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class StarMap {
    public Map<String, Star> stars = new ConcurrentHashMap<>();
    GamePanel gamePanel;
    public HashSet<Star> colonisedStars = new HashSet<>();

    public boolean overlayDrawn = false;

    //for flashing animation:
    private float flashRadius = 10;
    private float flashDelta = 0.5f;
    private final int FLASH_MIN = 8;
    private final int FLASH_MAX = 18;


    public StarMap(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

//
public void loadFromFile(String filename) throws IOException {
    List<String> lines = Files.readAllLines(new File(filename).toPath());

    // First pass: Create all stars
    for (String line : lines) {
        if (line.startsWith("Star")) createStar(line);
    }

    // Second pass: Connect stars
    for (String line : lines) {
        if (line.startsWith("Star")) connectStar(line);
    }
}

    private void createStar(String line) {
        String[] tokens = line.split("\\s+");
        String name = tokens[1];
        float x = Float.parseFloat(tokens[2]);
        float y = Float.parseFloat(tokens[3]);
        stars.put(name, new Star(name, x, y, gamePanel));
    }

    private void connectStar(String line) {
        String[] tokens = line.split("\\s+");
        Star star = stars.get(tokens[1]);
        for (int i = 5; i < tokens.length; i++) {
            Star connected = stars.get(tokens[i]);
            if (connected != null) {
                star.connectTo(connected);
                connected.connectTo(star);
            }
        }
    }




    public Collection<Star> getStars() {
        return stars.values();
    }

    public Star getStar(String name) {
        return stars.get(name);
    }

    public void draw(GraphicsContext gc) {
        // Draw connections (edges)
        gc.setLineWidth(8);
        for (Star star : getStars()) {
            for (Star connected : star.connections) {
                // faint outer glow
                gc.setStroke(Color.rgb(255, 255, 255, 0.12));
                gc.strokeLine(star.x, star.y, connected.x, connected.y);

                // mid glow
                gc.setLineWidth(4);
                gc.setStroke(Color.rgb(255, 255, 255, 0.29));
                gc.strokeLine(star.x, star.y, connected.x, connected.y);

                // core beam
                gc.setLineWidth(1);
                gc.setStroke(Color.rgb(255, 255, 255, 1.0));

                gc.strokeLine(star.x, star.y, connected.x, connected.y);
            }
        }

        Set<Star> visitedSnapshot = new HashSet<>(gamePanel.humanPlayer.getVisitedStars());

        for (Star star : getStars()) {
            // Determine star color based on quality
            Color starColor = switch (star.quality) {
                case UNINHABITABLE -> Color.rgb(255, 255, 255, 0.12);
                case BARREN -> Color.rgb(119, 79, 41);
                case POOR -> Color.rgb(188, 3, 3);
                case MEDIUM -> Color.rgb(204, 201, 20);
                case RICH -> Color.rgb(7, 165, 17);
            };
            gc.setStroke(starColor);


            if (!visitedSnapshot.contains(star) && !(gamePanel.ai.visitedStars.contains(star) && gamePanel.ai.debug)) {
                starColor = Color.GRAY;
            }

            // Combat flashing circle
            if (star.hasCombat) {
                star.combatVisible = true;
                double circleX = star.x + 30 - flashRadius;
                double circleY = star.y + 30 - flashRadius;
                gc.setFill(Color.WHITE);
                gc.fillOval(circleX, circleY, flashRadius * 2, flashRadius * 2);

                int rectSize = 40;
                star.combatHitbox = new Rectangle((int) (star.x + 30 - rectSize / 2),
                        (int) (star.y + 30 - rectSize / 2), rectSize, rectSize);
                gc.setStroke(Color.MAGENTA);
                gc.strokeRect(star.combatHitbox.getX(), star.combatHitbox.getY(),
                        star.combatHitbox.getWidth(), star.combatHitbox.getHeight());
            }

            // Colonisation overlay
            switch (star.colonised) {
                case COLONISED -> {
                    if (star.owner == Entity.Faction.PLAYER) {
                        gc.setFill(Color.rgb(5, 211, 18, 1.0));
                    } else if (star.owner == Entity.Faction.ENEMY && (visitedSnapshot.contains(star) || gamePanel.ai.debug)) {
                        gc.setFill(Color.rgb(255, 2, 2, 1.0));
                    }
                }
                case BEGUN -> {
                    gc.setFill(star.owner == Entity.Faction.PLAYER ? Color.rgb(79, 118, 214) : Color.rgb(159, 56, 56));
                }
                case ABANDONED -> gc.setFill(Color.rgb(42, 42, 62));
            }

            // Draw main star circle
            double ovalDiameter = 20;
            gc.setFill(starColor);
            gc.fillOval(star.x - ovalDiameter / 2, star.y - ovalDiameter / 2, ovalDiameter, ovalDiameter);

            // Draw star name
            String label = visitedSnapshot.contains(star) ? star.name : "???";
            gc.setFill(visitedSnapshot.contains(star) ? Color.WHITE : Color.GRAY);
            gc.fillText(label, star.x + 15, star.y);

            // Selection rectangle
            if (star.selected) {
                gc.setLineWidth(3);
                gc.setStroke(Color.GREEN);
                gc.strokeRect(star.solidAreaDefaultX, star.solidAreaDefaultY, star.solidArea.getWidth(), star.solidArea.getHeight());
                gc.setLineWidth(1);
            }

            // Overlay image
            if (star.overlay != null) {
                double drawX = star.x - ovalDiameter / 2;
                double drawY = star.y - ovalDiameter / 2;
                gc.drawImage(star.overlay, drawX, drawY, ovalDiameter, ovalDiameter);
            }
        }
    }

    public List<Star> findShortestPath(Star start, Star goal) {
        Map<Star, Double> distance = new HashMap<>();
        Map<Star, Star> previous = new HashMap<>();
        PriorityQueue<Star> queue = new PriorityQueue<>(Comparator.comparingDouble(distance::get));

        for (Star star : this.getStars()) {
            distance.put(star, Double.POSITIVE_INFINITY);
        }
        distance.put(start, 0.0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Star current = queue.poll();
            if (current == goal) break;

            for (Star neighbor : current.connections) {
                double edgeCost = Math.hypot(current.x - neighbor.x, current.y - neighbor.y);
                double alt = distance.get(current) + edgeCost;

                if (alt < distance.get(neighbor)) {
                    distance.put(neighbor, alt);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // Reconstruct path
        List<Star> path = new ArrayList<>();
        for (Star at = goal; at != null; at = previous.get(at)) {
            path.add(0, at);
        }
        return path;
    }

    public void assignStarOverlays(Image[] overlays) {
        Random rand = new Random();

        for (Star star : getStars()) {
            boolean[] used = new boolean[overlays.length]; // Track which overlays neighbours are using

            for (Star neighbour : star.connections) {
                if (neighbour.overlayIndex != -1) {
                    used[neighbour.overlayIndex] = true;
                }
            }

            // Build a list of available indices
            List<Integer> available = new ArrayList<>();
            for (int i = 0; i < overlays.length; i++) {
                if (!used[i]) {
                    available.add(i);
                }
            }

            // Choose randomly from available overlays
            if (available.isEmpty()) {
                // All options used by neighbours, just pick randomly
                star.overlayIndex = rand.nextInt(overlays.length);
            } else {
                star.overlayIndex = available.get(rand.nextInt(available.size()));
            }

            // Assign JavaFX Image directly
            star.overlay = overlays[star.overlayIndex];
        }
    }
    public void updateFlashingCircle() {
        flashRadius += flashDelta;
        if (flashRadius >= FLASH_MAX || flashRadius <= FLASH_MIN) {
            flashDelta *= -1;
        }
    }


}
