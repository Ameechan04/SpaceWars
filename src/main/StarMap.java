package main;

import entity.Entity;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
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

    public void draw(Graphics2D g2) {


                // Draw connections (edges)
                g2.setColor(new Color(255, 255, 255));





                for (Star star : this.getStars()) {
                    for (Star connected : star.connections) {


                        g2.setColor(new Color(255, 255, 255, 31)); // faint outer glow
                        g2.setStroke(new BasicStroke(8));
                        g2.drawLine((int) star.x, (int) star.y, (int) connected.x, (int) connected.y);

                        g2.setColor(new Color(255, 255, 255, 74)); // mid glow
                        g2.setStroke(new BasicStroke(4));
                        g2.drawLine((int) star.x, (int) star.y, (int) connected.x, (int) connected.y);

                        g2.setColor(new Color(255, 255, 255)); // core beam
                        g2.setStroke(new BasicStroke(1));
                        g2.drawLine((int) star.x, (int) star.y, (int) connected.x, (int) connected.y);

                    }
                }

                Set<Star> visitedSnapshot = new HashSet<>(gamePanel.humanPlayer.getVisitedStars()); // or use List if that's the type

                // Draw stars (nodes)
                for (Star star : this.getStars()) {




                    if (visitedSnapshot.contains(star) || (gamePanel.ai.visitedStars.contains(star) && gamePanel.ai.debug)) {
                        switch (star.quality) {
                            case UNINHABITABLE:
                                g2.setColor(Color.GRAY);
                                break;
                            case BARREN:
                                g2.setColor(new Color(119, 79, 41));
                                break;
                            case POOR:
                                g2.setColor(new Color(188, 3, 3));
                                break;
                            case MEDIUM:
                                g2.setColor(new Color(204, 201, 20));
                                break;
                            case RICH:
                                g2.setColor(new Color(7, 165, 17));
                                break;


                        }


                        //combat alert animation
                        if (star.hasCombat) {
                            star.combatVisible = true;
                            int circleX = (int) (star.x + 30 - flashRadius);
                            int circleY = (int) (star.y + 30 - flashRadius);

                            float diameter = flashRadius * 2;
                            g2.setColor(Color.WHITE);
                            g2.fillOval(circleX, circleY, (int) diameter, (int) diameter);


                            int rectSize = 40; // slightly larger than max circle diameter (36)
                            int rectX = (int) (star.x + 30 - rectSize / 2);
                            int rectY = (int) (star.y + 30 - rectSize / 2);

                            star.combatHitbox = new Rectangle(rectX, rectY, rectSize, rectSize);

                            // Debug: draw rectangle outline
                            g2.setColor(Color.MAGENTA);
                            g2.drawRect(star.combatHitbox.x, star.combatHitbox.y,
                                    star.combatHitbox.width, star.combatHitbox.height);


                        } else if (star.recentCombat) {

                            /*
                            int circleX = (int) (star.x + 30 - flashRadius);
                            int circleY = (int) (star.y + 30 - flashRadius);

                            float diameter = flashRadius * 2;
                            for (CombatManager combatManager : gamePanel.activeCombats) {
                                if (combatManager.star.name.equals(star.name)) {
                                    if (combatManager.playerWon()) {
                                        g2.setColor(Color.GREEN);
                                    } else {
                                        g2.setColor(Color.RED);
                                    }
                                    break;
                                }
                            }

                            //if there's combat then results are hidden to prioritise ongoing combat
                                g2.fillOval(circleX, circleY, (int) diameter, (int) diameter);

                             */
                        }
                    } else {
                        g2.setColor(new Color(133, 121, 121));
                    }

                    if (this.colonisedStars.contains(star) || (gamePanel.ai.colonisedStars.contains(star) && gamePanel.ai.debug)) {
                        switch (star.colonised) {
                            case UNCOLONISED:
//                            g2.setColor(Color.GRAY);
                                break;
                            case BEGUN:
                                if (star.owner == Entity.Faction.PLAYER)
                                    g2.setColor(new Color(79, 118, 214));
                                else
                                    g2.setColor(new Color(159, 56, 56));
                                break;
                            case COLONISED:

                                if (star.owner == Entity.Faction.PLAYER) {
                                    int ovalDiameter = 22;
                                    g2.setColor(new Color(5, 211, 18, 255));
                                    g2.fillOval((int) star.x - (ovalDiameter / 2), (int) star.y - (ovalDiameter / 2), ovalDiameter, ovalDiameter);

                                    ovalDiameter = 25;
                                    g2.setColor(new Color(5, 211, 18, 137));
                                    g2.fillOval((int) star.x - (ovalDiameter / 2), (int) star.y - (ovalDiameter / 2), ovalDiameter, ovalDiameter);


                                    ovalDiameter = 30;
                                    g2.setColor(new Color(5, 211, 18, 52));
                                    g2.fillOval((int) star.x - (ovalDiameter / 2), (int) star.y - (ovalDiameter / 2), ovalDiameter, ovalDiameter);


                                    g2.setColor(gamePanel.blueColour);
                                } else if (star.owner == Entity.Faction.ENEMY) {
                                    if (visitedSnapshot.contains(star) || gamePanel.ai.debug) {
                                        int ovalDiameter = 22;
                                        g2.setColor(new Color(255, 2, 2, 255));
                                        g2.fillOval((int) star.x - (ovalDiameter / 2), (int) star.y - (ovalDiameter / 2), ovalDiameter, ovalDiameter);

                                        ovalDiameter = 25;
                                        g2.setColor(new Color(255, 0, 0, 134));
                                        g2.fillOval((int) star.x - (ovalDiameter / 2), (int) star.y - (ovalDiameter / 2), ovalDiameter, ovalDiameter);


                                        ovalDiameter = 30;
                                        g2.setColor(new Color(255, 0, 0, 45));
                                        g2.fillOval((int) star.x - (ovalDiameter / 2), (int) star.y - (ovalDiameter / 2), ovalDiameter, ovalDiameter);

                                        g2.setColor(new Color(58, 8, 8, 255));

                                    } else {
                                        g2.setColor(new Color(133, 121, 121));
                                    }

                                }
                                break;
                            case ABANDONED:
                                g2.setColor(new Color(42, 42, 62));
                                break;
                        }
                    }

                    int ovalDiameter = 20;
                    float planetScale = (float) ovalDiameter / 32;
                    g2.fillOval((int) star.x - (ovalDiameter / 2), (int) star.y - (ovalDiameter / 2), ovalDiameter, ovalDiameter);
                    String str;
                    if (gamePanel.humanPlayer.getVisitedStars().contains(star)) {


                        if (star.colonised.equals(Star.Colonised.COLONISED)) {
                            g2.setColor(Color.GREEN);
                        } else {
                            g2.setColor(Color.white);

                        }
                        str = star.name;

                    } else {
                        g2.setColor(Color.gray);
                        str = "???";
                    }
                    g2.drawString(str, (int) star.x + 15, (int) star.y);

                    if (star.selected) {
                        g2.setStroke(new BasicStroke(3));
                        g2.setColor(Color.GREEN);
                        g2.drawRect(star.solidAreaDefaultX, star.solidAreaDefaultY, star.solidArea.width, star.solidArea.height);

                    }
                    g2.setStroke(new BasicStroke(1));


                    if (star.overlay != null) {

                        int drawX = (int) star.x - ovalDiameter / 2;
                        int drawY = (int) star.y - ovalDiameter / 2;

                        //overlay slightly off centre so corrected
                        g2.drawImage(star.overlay, drawX, drawY, ovalDiameter + ((int) planetScale), ovalDiameter + ((int) planetScale), null);
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

    public void assignStarOverlays(BufferedImage[] overlays) {
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
                // All options used by neighbours, just pick randomly (violation allowed)
                star.overlayIndex = rand.nextInt(overlays.length);
            } else {
                star.overlayIndex = available.get(rand.nextInt(available.size()));
            }

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
