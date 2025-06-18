package entity;

import main.GamePanel;
import main.Star;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Ship extends Entity {
    public boolean enteringOrbit = false;
    private boolean arrivedAtStarCentre = false;
    private double speed;
    public Star targetStar;
    public List<Star> jumpPath = new ArrayList<>();
    public int jumpIndex = 0;

    // Orbit offset defaults (now already inherited)
    public final int defaultOrbitOffsetX;
    public final int defaultOrbitOffsetY;

    public Ship(GamePanel gamePanel, String name, Star currentStar, double speed, int defaultOrbitOffsetX, int defaultOrbitOffsetY, int buildCost, int maxHealth, int damage) {
        super(gamePanel, name, buildCost, maxHealth, damage);

        this.speed = speed;
        this.direction = "right";
        this.defaultOrbitOffsetX = defaultOrbitOffsetX;
        this.defaultOrbitOffsetY = defaultOrbitOffsetY;
        this.orbitOffsetX = defaultOrbitOffsetX;
        this.orbitOffsetY = defaultOrbitOffsetY;

        this.currentStar = currentStar;
        this.inOrbit = true;
        this.moving = false;

        // Initial positioning
        if (currentStar != null) {
            setCentrePosition(currentStar.x + orbitOffsetX, currentStar.y + orbitOffsetY);
        }

        // Load fallback image
        getImage(); // if you're not overriding it in subclasses
    }

    public void startMovingToStar(Star destination) {
        if (currentStar == null) return;

        Star leavingStar = currentStar;
        leavingStar.orbitingShips.remove(this);

        this.inOrbit = false;
        this.moving = true;
        this.currentStar = null;

        // Reassign orbit offsets to remaining ships
        for (Ship s : leavingStar.orbitingShips) {
            s.assignOrbitOffset();
            s.setCentrePosition(leavingStar.x + s.orbitOffsetX, leavingStar.y + s.orbitOffsetY);
        }

        // Handle jump logic
        if (leavingStar.connections.contains(destination)) {
            this.targetStar = destination;
        } else {
            this.jumpPath = gamePanel.starMap.findShortestPath(leavingStar, destination);
            if (jumpPath.size() >= 2) {
                this.jumpIndex = 1;
                this.targetStar = jumpPath.get(jumpIndex);
            }
        }
    }

    @Override
    public void update() {
        if (!moving) return;

        if (arrivedAtStarCentre) {
            arrivedAtStarCentre = false;
            currentStar = targetStar;

            if (!gamePanel.visitedStars.contains(currentStar)) {
                gamePanel.visitedStars.add(currentStar);
            }

            jumpIndex++;
            if (jumpPath != null && jumpIndex < jumpPath.size()) {
                targetStar = jumpPath.get(jumpIndex);
                return;
            } else {
                targetStar = null;
                enteringOrbit = true;
                assignOrbitOffset();
                return;
            }
        }

        if (enteringOrbit) {
            moveToOrbit();
            return;
        }

        if (targetStar != null) {
            int tx = (int) targetStar.x;
            int ty = (int) targetStar.y;

            if (moveTowards(tx, ty)) {
                arrivedAtStarCentre = true;
            }
        }
    }

    private boolean moveTowards(int tx, int ty) {
        double dx = tx - exactCentreX;
        double dy = ty - exactCentreY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed) {
            setCentrePosition(tx, ty);
            return true;
        } else {
            double angle = Math.atan2(dy, dx);
            double deltaX = speed * Math.cos(angle);
            double deltaY = speed * Math.sin(angle);
            setCentrePosition(exactCentreX + deltaX, exactCentreY + deltaY);
            facingLeft = dx < 0;
            return false;
        }
    }

    private void moveToOrbit() {
        int orbitX = (int) currentStar.x + orbitOffsetX;
        int orbitY = (int) currentStar.y + orbitOffsetY;

        if (moveTowards(orbitX, orbitY)) {
            setCentrePosition(orbitX, orbitY);
            inOrbit = true;
            moving = false;
            enteringOrbit = false;
            jumpPath.clear();
            jumpIndex = 0;
        }
    }

    public void assignOrbitOffset() {
        int offsetY = defaultOrbitOffsetY;

        for (Ship ship : gamePanel.getShips()) {
            if (ship != this && ship.currentStar == this.currentStar && ship.inOrbit) {
                if (!ship.name.equals("Colony Ship") && !this.name.equals("Colony Ship")) {
                    offsetY += ship.solidArea.height + 5;
                } else if (ship.name.equals("Colony Ship") && this.name.equals("Colony Ship")) {
                    offsetY -= ship.solidArea.height + 2;
                }
            }
        }

        this.orbitOffsetX = defaultOrbitOffsetX;
        this.orbitOffsetY = offsetY;
    }

    public void enterOrbit(Star star) {
        this.currentStar = star;
        this.inOrbit = true;
        assignOrbitOffset();

        if (!star.orbitingShips.contains(this)) {
            star.orbitingShips.add(this);
        }

        setCentrePosition(star.x + orbitOffsetX, star.y + orbitOffsetY);
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = facingLeft ? left1 : right1;

        if (selected) {
            g2.setColor(Color.BLUE);
            g2.drawRect(solidArea.x, solidArea.y, solidArea.width, solidArea.height);
        }

        // Optional dotted jump path line
        if (currentStar != null && targetStar != null) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));
            g2.setColor(Color.BLUE);
            g2.drawLine((int) currentStar.x, (int) currentStar.y, (int) targetStar.x, (int) targetStar.y);
            g2.setStroke(oldStroke);
        }

        g2.setColor(Color.YELLOW);
         g2.draw(new Rectangle(worldX, worldY, gamePanel.TILE_SIZE, gamePanel.TILE_SIZE));


        g2.drawImage(image, worldX, worldY, gamePanel.TILE_SIZE, gamePanel.TILE_SIZE, null);
    }

    public static void recalculateOrbitOffsetsAt(GamePanel gamePanel, Star star) {
        List<Ship> ships = gamePanel.getShipsOrbitingStar(star);
        int downOffset = 0;
        int upOffset = 0;

        for (Ship ship : ships) {
            ship.orbitOffsetX = ship.defaultOrbitOffsetX;

            if (ship.name.equals("Colony Ship")) {
                ship.orbitOffsetY = ship.defaultOrbitOffsetY + upOffset;
                upOffset -= ship.solidArea.height + 2;
            } else {
                ship.orbitOffsetY = ship.defaultOrbitOffsetY + downOffset;
                downOffset += ship.solidArea.height + 5;
            }

            ship.setCentrePosition(star.x + ship.orbitOffsetX, star.y + ship.orbitOffsetY);
        }
    }

    public void setupSolidArea(int width, int height) {
        this.solidArea.width = width;
        this.solidArea.height = height;

        // Automatically update solidArea position based on current offsets
        this.solidArea.x = this.worldX + solidOffsetX;
        this.solidArea.y = this.worldY + solidOffsetY;
    }

}
