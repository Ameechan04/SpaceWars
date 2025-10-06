package main;

import entity.Entity;
import entity.Ship;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class MouseHandler {

    private final GamePanel gp;
    private final ColonisationManager colonisationManager;
    private final List<Ship> selectedShips = new ArrayList<>();
    private Star selectedStar = null;

    private int worldX, worldY;

    public MouseHandler(GamePanel gp, Scene scene) {
        this.gp = gp;
        colonisationManager = new ColonisationManager(gp);

        // Attach mouse listeners to the JavaFX scene
        scene.setOnMousePressed(this::handleMousePressed);
        scene.setOnMouseClicked(this::handleMouseClicked);
    }

    private void handleMousePressed(MouseEvent e) {
        Point2D world = gp.screenToWorld(e.getX(), e.getY());
        worldX = (int) world.getX();
        worldY = (int) world.getY();

        boolean shiftHeld = e.isShiftDown();

        if (e.getButton() == MouseButton.SECONDARY) {
            rightMouseClick(worldX, worldY);
            return;
        }

        if (handleBuildButtons(e)) return;
        if (handleShipClick(worldX, worldY, shiftHeld)) return;
        if (handleStarClick(worldX, worldY, e.getClickCount())) return;

        deselectAll();
    }

    private void handleMouseClicked(MouseEvent e) {
        // Optional: separate click logic if needed (double-click, etc.)
    }

    private boolean handleBuildButtons(MouseEvent e) {
        Star star = gp.ui.star;
        if (star == null || !gp.ui.starIsSelected) return false;

        if (star.station != null) {
            if (gp.ui.buildScoutButton != null && gp.ui.buildScoutButton.contains(e.getX(), e.getY())) {
                if (canAfford("scout")) gp.shipBuilderHelper.scheduleBuild("scout", star);
                return true;
            }

            if (gp.ui.buildFrigateButton != null && gp.ui.buildFrigateButton.contains(e.getX(), e.getY())) {
                if (canAfford("frigate")) gp.shipBuilderHelper.scheduleBuild("frigate", star);
                return true;
            }

            if (gp.ui.buildColonyShipButton != null && gp.ui.buildColonyShipButton.contains(e.getX(), e.getY())) {
                if (star.population < 10_000_000) {
                    gp.ui.addMessage("Population too low - 10,000,000 needed before colonisation!");
                    return true;
                }
                if (canAfford("colonyship")) {
                    gp.ui.addMessage("Building Colony Ship...");
                    star.population -= 200_000;
                    gp.shipBuilderHelper.scheduleBuild("colonyship", star);
                }
                return true;
            }
        } else {
            if (gp.ui.buildBasicShipyardButton != null && gp.ui.buildBasicShipyardButton.contains(e.getX(), e.getY())
                    && star.colonised == Star.Colonised.COLONISED) {
                if (canAfford("basicshipyard")) {
                    gp.ui.addMessage("Building Basic Shipyard...");
                    gp.shipBuilderHelper.scheduleBuild("basicshipyard", star);
                }
                return true;
            }
        }

        if (gp.ui.buildSmallSatelliteButton != null && gp.ui.buildSmallSatelliteButton.contains(e.getX(), e.getY())
                && star.colonised == Star.Colonised.COLONISED) {
            if (canAfford("smallsatellite")) {
                gp.ui.addMessage("Building Small Satellite...");
                gp.shipBuilderHelper.scheduleBuild("smallsatellite", star);
            }
            return true;
        }

        return false;
    }

    private boolean handleShipClick(double x, double y, boolean shiftHeld) {
        for (Ship ship : gp.getShips()) {
            if (ship.faction.equals(Entity.Faction.PLAYER)) {
                Rectangle2D hitBox = new Rectangle2D(
                        ship.worldX + ship.solidOffsetX,
                        ship.worldY + ship.solidOffsetY,
                        ship.solidArea.width,
                        ship.solidArea.height
                );

                if (hitBox.contains(x, y) && ship.clickable) {
                    if (!shiftHeld) deselectShips();

                    selectedShips.add(ship);
                    ship.selected = true;

                    gp.ui.selectedShips = new ArrayList<>();
                    gp.ui.selectedShips.add(ship);

                    if (selectedStar != null) selectedStar.selected = false;
                    selectedStar = null;

                    gp.requestFocus();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleStarClick(double x, double y, int clickCount) {
        for (Star star : gp.starMap.getStars()) {
            Rectangle2D hitBox = new Rectangle2D(
                    star.x - star.solidArea.getWidth() / 2,
                    star.y - star.solidArea.getHeight() / 2,
                    star.solidArea.getWidth(),
                    star.solidArea.getHeight()
            );

            if (hitBox.contains(x, y)) {
                if (selectedStar != null) selectedStar.selected = false;

                star.selected = true;
                selectedStar = star;
                gp.ui.setStar(star);
                gp.ui.starIsSelected = true;
                gp.ui.selectedMessageOn = true;

                if (clickCount == 2) { // double-click
                    deselectShips();
                    for (Ship ship : gp.getShips()) {
                        if (ship.faction.equals(Entity.Faction.PLAYER) && ship.inOrbit && ship.currentStar == star) {
                            ship.selected = true;
                            selectedShips.add(ship);
                        }
                    }
                    if (!selectedShips.isEmpty()) {
                        gp.ui.addMessage(selectedShips.size() + " ship(s) selected at " + star.name);
                        gp.ui.selectedShips = new ArrayList<>(selectedShips);
                    }
                    return true;
                }

                if (!selectedShips.isEmpty() && clickCount == 1) { // single-click move
                    for (Ship ship : selectedShips) {
                        if (ship.moving || ship.enteringOrbit) {
                            gp.ui.addMessage(ship.name + " is currently moving and cannot receive new orders.", "red");
                            continue;
                        }

                        if (!(ship.name.equalsIgnoreCase("Colony Ship") && ship.currentStar == star)) {
                            ship.startMovingToStar(star);
                            gp.ui.addMessage(ship.name + " started moving to " + star.name);
                        }
                    }
                    deselectShips();
                    star.selected = false;
                    selectedStar = null;
                    gp.ui.selectedMessageOn = false;
                    return true;
                }

                return true;
            }
        }
        return false;
    }

    private void rightMouseClick(int x, int y) {
        for (Star star : gp.starMap.getStars()) {
            Rectangle2D hitBox = new Rectangle2D(star.solidArea.getX(), star.solidArea.getY(),
                    star.solidArea.getWidth(), star.solidArea.getHeight());

            if (hitBox.contains(x, y)) {
                selectedStar = star;

                if (selectedShips.size() == 1) {
                    Ship selectedShip = selectedShips.get(0);
                    if (selectedShip.name.equalsIgnoreCase("Colony Ship") &&
                            selectedShip.currentStar == star) {
                        colonisationManager.beginColonisation(selectedShip, star);

                        selectedShips.clear();
                        selectedStar.selected = false;
                        selectedStar = null;
                        return;
                    }
                }
            }
        }
    }

    private void deselectShips() {
        for (Ship s : selectedShips) s.selected = false;
        selectedShips.clear();
        gp.ui.selectedShips.clear();
    }

    private void deselectAll() {
        deselectShips();
        if (selectedStar != null) selectedStar.selected = false;
        selectedStar = null;
        gp.ui.star = null;
        gp.ui.starIsSelected = false;
        gp.ui.selectedMessageOn = false;
    }

    public boolean canAfford(String eName) {
        if (gp.humanPlayer.getMoney() - gp.humanPlayer.getBuildCost(eName) < 0) {
            gp.ui.addMessage("Cannot afford " + eName);
            return false;
        } else {
            gp.addMoney(-gp.humanPlayer.getBuildCost(eName), Entity.Faction.PLAYER);
            return true;
        }
    }
}
