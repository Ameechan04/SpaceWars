package entity;

import main.GamePanel;
import main.Star;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Ship extends Entity {
    // Movement & orbit
    public boolean clickable = true;
    public boolean enteringOrbit = false;
    private boolean arrivedAtStarCentre = false;
    private double speed;
    public Star targetStar;
    public List<Star> jumpPath = new ArrayList<>();
    public int jumpIndex = 0;

    // Orbit offset defaults
    public final int defaultOrbitOffsetX;
    public final int defaultOrbitOffsetY;
    public int orbitStackIndex = 0; // New: track position in stack

    // Movement rendering
    public Star startingStarForLeg;
    private final List<Point> renderPath = new ArrayList<>();
    private static final BasicStroke DASHED_STROKE =
            new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0);

    public Ship(GamePanel gamePanel, String name, Star currentStar, double speed,
                int defaultOrbitOffsetX, int defaultOrbitOffsetY,
                int buildCost, int maxHealth, int damage, Faction faction) {
        super(gamePanel, name, buildCost, maxHealth, damage, faction);
        this.speed = speed;
        this.defaultOrbitOffsetX = defaultOrbitOffsetX;
        this.defaultOrbitOffsetY = defaultOrbitOffsetY;
        this.orbitOffsetX = defaultOrbitOffsetX;
        this.orbitOffsetY = defaultOrbitOffsetY;
        this.startingStarForLeg = currentStar;
        this.currentStar = currentStar;
        this.inOrbit = true;
        this.moving = false;

        if (currentStar != null) {
            setCentrePosition(currentStar.x + orbitOffsetX, currentStar.y + orbitOffsetY);
        }

        getImage(); // Load fallback image
    }

    /** Start moving to a destination star */
    public void startMovingToStar(Star destination) {
        if (currentStar == destination) {
            moving = false;
            enteringOrbit = false;
            targetStar = null;
            return;
        }

        Star leavingStar = currentStar;

        if (leavingStar != null) {
            // Remove from orbit lists (only once)
            leavingStar.orbitingShips.remove(this);
            if (leavingStar.orbitManager != null) {
                leavingStar.orbitManager.removeShip(this);
            }

            // mark as in-transit
            this.inOrbit = false;
            this.moving = true;
            this.orbitOffsetX = defaultOrbitOffsetX;
            this.orbitOffsetY = defaultOrbitOffsetY;
            // DO NOT set currentStar = null yet; use leavingStar for path decisions
        }

        // Determine path FROM leavingStar (if present) — not currentStar which may be nulled later
        if (leavingStar != null && leavingStar.connections.contains(destination)) {
            targetStar = destination;
            prepareRenderPath(leavingStar, destination);
        } else {
            Star fromStar = (leavingStar != null) ? leavingStar : currentStar;
            // If fromStar is still null here then we can't compute path — bail out safely
            if (fromStar == null) return;

            jumpPath = gamePanel.starMap.findShortestPath(fromStar, destination);
            if (jumpPath == null || jumpPath.size() < 2) {
                // nothing to do
                return;
            }
            jumpIndex = 1;
            targetStar = jumpPath.get(jumpIndex);
            prepareRenderPath(fromStar, destination);
        }

        // finally clear currentStar to indicate we're in transit
        this.currentStar = null;
    }


    /** Precompute render path */
    private void prepareRenderPath(Star start, Star end) {
        renderPath.clear();
        if (jumpPath == null || jumpPath.size() < 2) {
            renderPath.add(new Point((int) start.x, (int) start.y));
            renderPath.add(new Point((int) end.x, (int) end.y));
            return;
        }

        Star from = startingStarForLeg != null ? startingStarForLeg : start;
        for (int i = jumpIndex; i < jumpPath.size(); i++) {
            Star to = jumpPath.get(i);
            renderPath.add(new Point((int) from.x, (int) from.y));
            renderPath.add(new Point((int) to.x, (int) to.y));
            from = to;
        }
    }

    /** Update ship movement and orbit */
    public void update(double currentGameDay) {
        if (!moving && !enteringOrbit) return;

        // Arrive at target star
        if (arrivedAtStarCentre) {
            arrivedAtStarCentre = false;
            currentStar = targetStar;
            startingStarForLeg = currentStar;

            if (currentStar.orbitController == null || currentStar.orbitController == faction) {
                currentStar.orbitController = faction;
                this.defending = true;
            } else this.defending = false;

            if (faction == Faction.PLAYER && !gamePanel.humanPlayer.getVisitedStars().contains(currentStar))
                gamePanel.humanPlayer.addVisitedStar(currentStar);
            if (faction == Faction.ENEMY && !gamePanel.ai.visitedStars.contains(currentStar)) {
                gamePanel.ai.visitedStars.add(currentStar);
                gamePanel.ai.exploredStarQualities.put(currentStar.quality,
                        gamePanel.ai.exploredStarQualities.get(currentStar.quality) + 1);
            }

            jumpIndex++;
            if (jumpPath != null && jumpIndex < jumpPath.size()) {
                targetStar = jumpPath.get(jumpIndex);
                return;
            } else {
                targetStar = null;
                enteringOrbit = true;
                moveToOrbit();
                return;
            }
        }

        if (enteringOrbit) {
            moveToOrbit();
            return;
        }

        if (targetStar != null && moveTowards(targetStar.x, targetStar.y)) {
            arrivedAtStarCentre = true;
        }
    }

    /** Move towards a position */
    private boolean moveTowards(double tx, double ty) {
        double dx = tx - exactCentreX;
        double dy = ty - exactCentreY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double scaledSpeed = speed * gamePanel.gameClock.gameSpeed;

        if (dist <= scaledSpeed) {
            setCentrePosition(tx, ty);
            return true;
        }

        double factor = scaledSpeed / dist;
        setCentrePosition(exactCentreX + dx * factor, exactCentreY + dy * factor);
        facingLeft = dx < 0;
        return false;
    }

    /** Move ship into orbit around current star */
    private void moveToOrbit() {
        if (currentStar == null) return;

        if (!currentStar.orbitingShips.contains(this)) {
            currentStar.orbitingShips.add(this);
            currentStar.orbitManager.addShip(this);
        }

        int targetX = currentStar.orbitManager.getTargetX(this);
        int targetY = currentStar.orbitManager.getTargetY(this);

        if (moveTowards(targetX, targetY)) {
            inOrbit = true;
            moving = false;
            enteringOrbit = false;
        }
    }

    /** Enter orbit immediately (used for initial placement) */
    public void enterOrbit(Star star) {
        this.currentStar = star;
        this.inOrbit = true;

        if (!star.orbitingShips.contains(this)) star.orbitingShips.add(this);
        star.orbitManager.addShip(this);

        setCentrePosition(star.orbitManager.getTargetX(this),
                star.orbitManager.getTargetY(this));
    }

    /** Efficiently draw ship */
    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = facingLeft ? left1 : right1;

        int imgX = (int) Math.round(exactCentreX - (solidArea.width / 2.0));
        int imgY = (int) Math.round(exactCentreY - (solidArea.height / 2.0));

        // Draw image if available
        if (image != null) {
            g2.drawImage(image, imgX, imgY, solidArea.width, solidArea.height, null);
        } else {
            // fallback visible representation (filled rectangle)
            if (faction == null) g2.setColor(Color.GRAY);
            else if (faction == Faction.PLAYER) g2.setColor(Color.GREEN);
            else g2.setColor(Color.RED);

            g2.fillRect(solidArea.x, solidArea.y, solidArea.width, solidArea.height);
        }

        if (faction == null) g2.setColor(Color.GRAY);
        else if (faction == Faction.PLAYER) g2.setColor(Color.GREEN);
        else g2.setColor(Color.RED);

        g2.drawRect(solidArea.x, solidArea.y, solidArea.width, solidArea.height);

        if (selected) {
            g2.setColor(Color.BLUE);
            g2.drawRect(solidArea.x, solidArea.y, solidArea.width, solidArea.height);
        }
    }

    /** Setup solid area */
    public void setupSolidArea(int width, int height) {
        this.solidArea.width = width;
        this.solidArea.height = height;
        this.solidArea.x = this.worldX + solidOffsetX;
        this.solidArea.y = this.worldY + solidOffsetY;
    }
}
