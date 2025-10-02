package main;

import entity.Entity;
import entity.Ship;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class MouseHandler extends MouseAdapter {
    ColonisationManager colonisationManager;
    GamePanel gamePanel;
    List<Ship> selectedShips = new ArrayList<>();
    Star selectedStar = null;
    int worldX, worldY;
    public MouseHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
       colonisationManager = new ColonisationManager(gamePanel);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        int screenX = e.getX();
        int screenY = e.getY();

        // Convert to world coordinates using camera and zoom
        this.worldX = (int) ((screenX / gamePanel.zoom) + gamePanel.cameraOffsetX);
        this.worldY = (int) ((screenY / gamePanel.zoom) + gamePanel.cameraOffsetY);

        System.out.println("Clicked: screenX=" + screenX + ", screenY=" + screenY
                + ", worldX=" + worldX + ", worldY=" + worldY);


        boolean shiftHeld = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;

        if (e.getButton() == MouseEvent.BUTTON3) {
            rightMouseClick(e);
            return;
        }

        ShipBuilderHelper shipBuilderHelper = gamePanel.shipBuilderHelper;

        // Handle active combats
        combatManagerClicks(mouseX, mouseY);

        // Handle clicks on build buttons
        if (handleBuildButtons(e)) return;

        // Handle clicks on ships
        if (handleShipClick(mouseX, mouseY, shiftHeld)) return;

        // Handle clicks on stars
        if (handleStarClick(mouseX, mouseY, e.getClickCount())) return;

        // Clicked empty space
        deselectAll();
    }

    private boolean handleBuildButtons(MouseEvent e) {
        if (!gamePanel.ui.starIsSelected || gamePanel.ui.star == null) return false;

        Star star = gamePanel.ui.star;

        if (star.station != null) {
            if (gamePanel.ui.buildScoutButton != null && gamePanel.ui.buildScoutButton.contains(e.getPoint())) {
                if (canAfford("scout")) {
                    gamePanel.ui.addMessage("Building Scout...");
                    gamePanel.shipBuilderHelper.scheduleBuild("scout", star);
                }
                return true;
            }

            if (gamePanel.ui.buildFrigateButton != null && gamePanel.ui.buildFrigateButton.contains(e.getPoint())) {
                if (canAfford("frigate")) {
                    gamePanel.ui.addMessage("Building Frigate...");
                    gamePanel.shipBuilderHelper.scheduleBuild("frigate", star);
                }
                return true;
            }

            if (gamePanel.ui.buildColonyShipButton != null && gamePanel.ui.buildColonyShipButton.contains(e.getPoint())) {
                if (star.population < 10_000_000) {
                    gamePanel.ui.addMessage("Population too low - 10,000,000 needed before able to colonise!");
                    return true;
                }

                if (canAfford("colonyship")) {
                    gamePanel.ui.addMessage("Building Colony Ship...");
                    star.population -= 200_000;
                    gamePanel.shipBuilderHelper.scheduleBuild("colonyship", star);
                }
                return true;
            }
        } else {
            if (gamePanel.ui.buildBasicShipyardButton != null && gamePanel.ui.buildBasicShipyardButton.contains(e.getPoint())
                    && star.colonised == Star.Colonised.COLONISED) {
                if (canAfford("basicshipyard")) {
                    gamePanel.ui.addMessage("Building Basic Shipyard...");
                    gamePanel.shipBuilderHelper.scheduleBuild("basicshipyard", star);
                }
                return true;
            }
        }

        if (gamePanel.ui.buildSmallSatelliteButton != null && gamePanel.ui.buildSmallSatelliteButton.contains(e.getPoint())
                && star.colonised == Star.Colonised.COLONISED) {
            if (canAfford("smallsatellite")) {
                gamePanel.ui.addMessage("Building Small Satellite...");
                gamePanel.shipBuilderHelper.scheduleBuild("smallsatellite", star);
            }
            return true;
        }
        return false;
    }

    private boolean handleShipClick(int mouseX, int mouseY, boolean shiftHeld) {
        for (Ship ship : gamePanel.getShips()) {
            if (ship != null && ship.faction.equals(Entity.Faction.PLAYER)) {
                Rectangle hitBox = new Rectangle(
                        ship.worldX + ship.solidOffsetX,
                        ship.worldY + ship.solidOffsetY,
                        ship.solidArea.width,
                        ship.solidArea.height
                );

                if (hitBox.contains(worldX, worldY) && ship.clickable) {
                    if (!shiftHeld) deselectShips(); // deselect previous

                    selectedShips.add(ship);
                    ship.selected = true;
                    gamePanel.ui.addMessage(ship.name + " is selected");

// Make sure UI knows the selection
                    gamePanel.ui.selectedShips = new ArrayList<>();
                    gamePanel.ui.selectedShips.add(ship);

                    if (selectedStar != null) selectedStar.selected = false;
                    selectedStar = null;

                    gamePanel.repaint();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleStarClick(int mouseX, int mouseY, int clickCount) {
        for (Star star : gamePanel.starMap.getStars()) {
            combatFlashClickHandler(mouseX, mouseY, star);

            Rectangle hitBox = new Rectangle(
                    (int) star.x - star.solidArea.width/2,
                    (int) star.y - star.solidArea.height/2,
                    star.solidArea.width,
                    star.solidArea.height
            );

            if (hitBox.contains(worldX, worldY)) {
                if (selectedStar != null) selectedStar.selected = false;

                star.selected = true;
                selectedStar = star;
                gamePanel.ui.setStar(star);
                gamePanel.ui.starIsSelected = true;
                gamePanel.ui.selectedMessageOn = true;

                // --- Double-click: select all player ships in orbit ---
                if (clickCount == 2) {
                    deselectShips();
                    for (Ship ship : gamePanel.getShips()) {
                        if (ship.faction.equals(Entity.Faction.PLAYER) && ship.inOrbit && ship.currentStar == star) {
                            ship.selected = true;
                            selectedShips.add(ship);
                        }
                    }
                    if (!selectedShips.isEmpty()) {
                        gamePanel.ui.addMessage(selectedShips.size() + " ship(s) selected at " + star.name);
                        gamePanel.ui.selectedShips = (ArrayList<Ship>) selectedShips;
                    }
                    gamePanel.repaint();
                    return true;
                }

                // Single-click: move selected ships to this star
                if (!selectedShips.isEmpty() && clickCount == 1) {

                    for (int i = 0; i < selectedShips.size(); i++) {
                        Ship ship = selectedShips.get(i);

                        if (ship.moving || ship.enteringOrbit) {
                            gamePanel.ui.addMessage(ship.name + " is currently moving and cannot receive new orders.", "red");
                            continue;
                        }


                        if (!(ship.name.equalsIgnoreCase("Colony Ship") && ship.currentStar == star)) {

                            ship.startMovingToStar(star);
                            gamePanel.ui.addMessage(ship.name + " started moving to " + star.name);
                        }
                    }

                    deselectShips();
                    star.selected = false;
                    selectedStar = null;
                    gamePanel.ui.selectedMessageOn = false;
                    gamePanel.repaint();
                    return true;
                }

                return true;
            }
        }
        return false;
    }

    private void deselectShips() {
        for (Ship s : selectedShips) s.selected = false;
        selectedShips.clear();
        gamePanel.ui.selectedShips.clear();
    }

    private void deselectAll() {
        deselectShips();
        if (selectedStar != null) {
            selectedStar.selected = false;
            selectedStar = null;

        }
        gamePanel.ui.star = null;
        gamePanel.ui.starIsSelected = false;
        gamePanel.ui.selectedMessageOn = false;
        gamePanel.repaint();
    }

    private void combatFlashClickHandler(int mouseX, int mouseY, Star star) {
        if (gamePanel.humanPlayer.getVisitedStars().contains(star) && star.combatHitbox != null) {
            Point world = gamePanel.screenToWorld(mouseX, mouseY);

            if (star.combatHitbox.contains(world)) {
                System.out.println("Viewing combat at star: " + star.name);
                for (CombatManager combatManager : gamePanel.activeCombats) {
                    if (combatManager.star.name.equals(star.name)) {

                        combatManager.combatGUI.show();
                    }
                }
            }
        }
    }

    public boolean canAfford(String eName) {
        if (gamePanel.humanPlayer.getMoney() - gamePanel.humanPlayer.getBuildCost(eName) < 0) {
            gamePanel.ui.addMessage("Cannot afford to build " + eName);
            return false;
        } else {
            gamePanel.addMoney(-gamePanel.humanPlayer.getBuildCost(eName), Entity.Faction.PLAYER);
            return true;
        }
    }

    public void rightMouseClick(MouseEvent e) {
        Point world = gamePanel.screenToWorld(e.getX(), e.getY());
        int mouseX = world.x;
        int mouseY = world.y;

        for (Star star : gamePanel.starMap.getStars()) {
            if (mouseX >= star.solidArea.x && mouseX <= star.solidArea.x + star.solidArea.width &&
                    mouseY >= star.solidArea.y && mouseY <= star.solidArea.y + star.solidArea.height) {
                selectedStar = star;

                if (selectedShips.size() == 1) {
                    Ship selectedShip = selectedShips.get(0);

                    if (selectedShip.name.equalsIgnoreCase("Colony Ship") &&
                            selectedShip.currentStar == star) {
                        colonisationManager.beginColonisation(selectedShip, star);


                        selectedShips.clear();
                        selectedStar.selected = false;
                        selectedStar = null;
                        gamePanel.repaint();
                        return;
                    }
                }
            }
        }
    }

    public void combatManagerClicks(int mouseX, int mouseY) {
        for (CombatManager combatManager : gamePanel.activeCombats) {
            if (combatManager.inCombat || combatManager.combatRecently) {
                RoundRectangle2D closeButton = combatManager.combatGUI.closeButton;
                if (closeButton.contains(mouseX, mouseY)) {
                    System.out.println("closing combat panel");
                    combatManager.combatGUI.hide();
                    combatManager.combatGUI.viewingResult = false;
                }
            }
        }
    }
}
