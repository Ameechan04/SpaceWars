package entity;

import main.GamePanel;
import main.Star;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Ship extends Entity{
    Star targetStar;
    public boolean enteringOrbit = false;
    boolean arrivedAtStarCentre = false;
    double speed;
    public List<Star> jumpPath = new ArrayList<>();
    public int jumpIndex = 0;
    int orbitOffsetX;  //default
    int orbitOffsetY; //default
    final int defaultOrbitOffsetX;
    final int defaultOrbitOffsetY;

    int solidOffsetX = worldX;
    int solidOffsetY = worldY;

    /*use for default offsets */
    public Ship(GamePanel gamePanel, String name, Star currentStar, double speed, int defaultOrbitOffsetX, int defaultOrbitOffsetY, int buildCost, int maxHealth, int damage) {
        super(gamePanel, name, buildCost, maxHealth, damage);

        this.name = name;
        this.speed = speed;
        this.direction = "right";
        this.defaultOrbitOffsetX = defaultOrbitOffsetX;
        this.defaultOrbitOffsetY = defaultOrbitOffsetY;
        orbitOffsetX = defaultOrbitOffsetX;
        orbitOffsetY = defaultOrbitOffsetY;



        this.currentStar = currentStar;
        int orbitX = (int) currentStar.x + this.orbitOffsetX;
        int orbitY = (int) currentStar.y + this.orbitOffsetY;


//        this.setCentrePosition(orbitX, orbitY);
        this.inOrbit = true;
        this.moving = false;
    }

    public void startMovingToStar(Star destination) {
        if (currentStar == null) return;

        System.out.println("LEAVING SYSTEM : " + currentStar.name);

        // Save the current star before clearing it
        Star leavingStar = currentStar;
        if (leavingStar.orbitingShips.contains(this)) {
            leavingStar.orbitingShips.remove(this);
        }

        this.inOrbit = false;
        this.moving = true;
        this.currentStar = null; // Must do this before recalculating offsets

        // Reassign orbit offsets for remaining ships
        for (Ship s : leavingStar.orbitingShips) {
            s.assignOrbitOffset();
            s.setCentrePosition((int) leavingStar.x + s.orbitOffsetX, (int) leavingStar.y + s.orbitOffsetY);
        }


        // Handle jump logic
        if (leavingStar.connections.contains(destination)) {
            this.targetStar = destination;
            return;
        }

        this.jumpPath = gamePanel.starMap.findShortestPath(leavingStar, destination);
        if (jumpPath.size() < 2) return;

        this.jumpIndex = 1;
        this.targetStar = jumpPath.get(jumpIndex);
    }




    public void update() {
        if (!moving) {
            updateCentreFromWorldPosition();
            return;
        }

//        updateCentreFromWorldPosition();
        if (arrivedAtStarCentre) {

            arrivedAtStarCentre = false;
            currentStar = targetStar;

            if (!gamePanel.visitedStars.contains(currentStar)) {
                gamePanel.visitedStars.add(currentStar);
               // gamePanel.ui.showMessage("Discovered a " + currentStar.quality + " star");
            }
            jumpIndex++;
            if (jumpPath != null && jumpIndex < jumpPath.size()) {
                targetStar = jumpPath.get(jumpIndex);
                return;
            } else {
                targetStar = null;
                inOrbit = false;

                enteringOrbit = true;

                assignOrbitOffset();
//                Ship.recalculateOrbitOffsetsAt(gamePanel, currentStar);
                return;
            }
        }

        if (enteringOrbit) {
            moveAboutOrbit();
            return;
        }

        if (targetStar != null) {
            int centreX = (int) targetStar.x;
            int centreY = (int) targetStar.y;

            if (moveTowards(centreX, centreY)) {
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


    private void moveAboutOrbit() {



        int orbitX = (int) currentStar.x + orbitOffsetX;
        int orbitY = (int) currentStar.y + orbitOffsetY;

        if (moveTowards(orbitX, orbitY)) {
//                Ship.recalculateOrbitOffsetsAt(gamePanel, currentStar);
            setCentrePosition(orbitX, orbitY); // ensure exact position
            inOrbit = true;
            moving = false;
            enteringOrbit = false;
            jumpPath.clear();
            jumpIndex = 0;
            System.out.println("Ship entered orbit at " + currentStar.name);
            System.out.println("Its offsets are: " + this.orbitOffsetX + ", " + this.orbitOffsetY);
        }



    }

    void assignOrbitOffset() {
        int offsetY = defaultOrbitOffsetY;

        for (Ship ship : gamePanel.getShips()) {
            if (ship != this && ship.currentStar == this.currentStar && ship.inOrbit) {
                //if existing ship is not colony ship and you aren't then adjust position
                if (!ship.name.equals("Colony Ship") && !this.name.equals("Colony Ship")) {
                    offsetY += ship.solidArea.height + 5;
                    //if existing ship is colony ship and you are a colony ship, go above
                } else if (ship.name.equals("Colony Ship") && this.name.equals("Colony Ship")) {
                    offsetY -= ship.solidArea.height + 2;
                }
                //if colony ship then ignore



            }
        }

        this.orbitOffsetX = defaultOrbitOffsetX;
        this.orbitOffsetY = offsetY;
    }


    public void draw(Graphics2D g2) {
//        updateCentreFromWorldPosition();
        BufferedImage image = null;
        if (facingLeft) {
            image = left1;
        } else {
            image = right1;
        }


        solidArea.x = worldX + solidOffsetX;
        solidArea.y = worldY + solidOffsetY;
        //for debugging collisions:
        g2.setColor(Color.RED);
        g2.drawRect(solidArea.x,  solidArea.y, solidArea.width, solidArea.height);
        if (selected) {
            g2.setColor(Color.BLUE);
            g2.drawRect(solidArea.x,  solidArea.y, solidArea.width, solidArea.height);
        }

        if (currentStar != null && targetStar != null) {
            Graphics2D g2d = (Graphics2D) g2;
            Stroke oldStroke = g2d.getStroke();

            // Set stroke to dotted
            float[] dashPattern = {5, 5};
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dashPattern, 0));
            g2d.setColor(Color.BLUE);

            // Draw line from current star to target star
            int x1 = (int) currentStar.x;
            int y1 = (int) currentStar.y;
            int x2 = (int) targetStar.x;
            int y2 = (int) targetStar.y;
            g2d.drawLine(x1, y1, x2, y2);

            // Restore original stroke
            g2d.setStroke(oldStroke);
        }

        g2.drawImage(image, worldX, worldY, gamePanel.TILE_SIZE, gamePanel.TILE_SIZE, null);

    }

    public void enterOrbit(Star star) {
        this.currentStar = star;
        this.inOrbit = true;
        assignOrbitOffset();

        if (!star.orbitingShips.contains(this)) {
            star.orbitingShips.add(this);
        }

        this.setCentrePosition((int) star.x + orbitOffsetX, (int) star.y + orbitOffsetY);
    }

    public static void recalculateOrbitOffsetsAt(GamePanel gamePanel, Star star) {
        List<Ship> ships = gamePanel.getShipsOrbitingStar(star);

        int downwardOffset = 0;
        int upwardOffset = 0;

        for (Ship ship : ships) {
            ship.orbitOffsetX = ship.defaultOrbitOffsetX;

            if (ship.name.equals("Colony Ship")) {
                ship.orbitOffsetY = ship.defaultOrbitOffsetY + upwardOffset;
                upwardOffset -= ship.solidArea.height + 2;
            } else {
                ship.orbitOffsetY = ship.defaultOrbitOffsetY + downwardOffset;
                downwardOffset += ship.solidArea.height + 5;
            }

            ship.setCentrePosition((int) star.x + ship.orbitOffsetX, (int) star.y + ship.orbitOffsetY);
        }
    }






}