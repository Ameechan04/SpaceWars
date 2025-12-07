package entity;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import main.GamePanel;
import main.Star;

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
    public int orbitStackIndex = 0;

    // Movement rendering
    public Star startingStarForLeg;
    private final List<Point2D> renderPath = new ArrayList<>();

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

        getImage();
    }

    public void startMovingToStar(Star destination) {
        if (currentStar == destination) {
            moving = false;
            enteringOrbit = false;
            targetStar = null;
            return;
        }

        Star leavingStar = currentStar;

        if (leavingStar != null) {
            leavingStar.orbitingShips.remove(this);
            if (leavingStar.orbitManager != null) {
                leavingStar.orbitManager.removeShip(this);
            }

            this.inOrbit = false;
            this.moving = true;
            this.orbitOffsetX = defaultOrbitOffsetX;
            this.orbitOffsetY = defaultOrbitOffsetY;
        }

        if (leavingStar != null && leavingStar.connections.contains(destination)) {
            targetStar = destination;
            prepareRenderPath(leavingStar, destination);
        } else {
            Star fromStar = (leavingStar != null) ? leavingStar : currentStar;
            if (fromStar == null) return;

            jumpPath = gamePanel.starMap.findShortestPath(fromStar, destination);
            if (jumpPath == null || jumpPath.size() < 2) {
                return;
            }
            jumpIndex = 1;
            targetStar = jumpPath.get(jumpIndex);
            prepareRenderPath(fromStar, destination);
        }

        this.currentStar = null;
    }

    private void prepareRenderPath(Star start, Star end) {
        renderPath.clear();
        if (jumpPath == null || jumpPath.size() < 2) {
            renderPath.add(new Point2D(start.x, start.y));
            renderPath.add(new Point2D(end.x, end.y));
            return;
        }

        Star from = startingStarForLeg != null ? startingStarForLeg : start;
        for (int i = jumpIndex; i < jumpPath.size(); i++) {
            Star to = jumpPath.get(i);
            renderPath.add(new Point2D(from.x, from.y));
            renderPath.add(new Point2D(to.x, to.y));
            from = to;
        }
    }

    public void update(double currentGameDay) {
        if (!moving && !enteringOrbit) return;

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

    public void enterOrbit(Star star) {
        this.currentStar = star;
        this.inOrbit = true;

        if (!star.orbitingShips.contains(this)) star.orbitingShips.add(this);
        star.orbitManager.addShip(this);

        setCentrePosition(star.orbitManager.getTargetX(this),
                star.orbitManager.getTargetY(this));
    }

    @Override
    public void draw(GraphicsContext gc) {
        Image image = facingLeft ? left1 : right1;

        double imgX = exactCentreX - (solidArea.getWidth() / 2.0);
        double imgY = exactCentreY - (solidArea.getHeight() / 2.0);

        if (image != null) {
            gc.drawImage(image, imgX, imgY, solidArea.getWidth(), solidArea.getHeight());
        } else {
            if (faction == null) gc.setFill(Color.GRAY);
            else if (faction == Faction.PLAYER) gc.setFill(Color.GREEN);
            else gc.setFill(Color.RED);

            gc.fillRect(solidArea.getX(), solidArea.getY(), solidArea.getWidth(), solidArea.getHeight());
        }

        if (faction == null) gc.setStroke(Color.GRAY);
        else if (faction == Faction.PLAYER) gc.setStroke(Color.GREEN);
        else gc.setStroke(Color.RED);

        gc.strokeRect(solidArea.getX(), solidArea.getY(), solidArea.getWidth(), solidArea.getHeight());

        if (selected) {
            gc.setStroke(Color.BLUE);
            gc.strokeRect(solidArea.getX(), solidArea.getY(), solidArea.getWidth(), solidArea.getHeight());
        }
    }

    public void setupSolidArea(int width, int height) {
        this.solidArea.setWidth(width);
        this.solidArea.setHeight(height);
        this.solidArea.setX(this.worldX + solidOffsetX);
        this.solidArea.setY(this.worldY + solidOffsetY);
    }
}
