package main;

import entity.Ship;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class MouseHandler extends MouseAdapter {

    GamePanel gamePanel;
    List<Ship> selectedShips = new ArrayList<>();
    Star selectedStar = null;

    public MouseHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            rightMouseClick(e);
            return;
        }
        int mouseX = e.getX();
        int mouseY = e.getY();
        boolean shiftHeld = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;

        ShipBuilderHelper shipBuilderHelper = gamePanel.shipBuilderHelper;


        combatManagerClicks(mouseX, mouseY);







        if (gamePanel.ui.starIsSelected && gamePanel.ui.star != null && gamePanel.ui.star.station != null) {
            if (gamePanel.ui.buildScoutButton != null && gamePanel.ui.buildScoutButton.contains(e.getPoint())) {
                if (canAfford("Scout")) {
                    gamePanel.ui.addMessage("Building Scout...");
                    shipBuilderHelper.scheduleShipBuildInDays("scout", gamePanel.ui.star, 14.0);
                }
                return;
            }

            if (gamePanel.ui.buildFrigateButton != null && gamePanel.ui.buildFrigateButton.contains(e.getPoint())) {
                if (canAfford("Frigate")) {
                    gamePanel.ui.addMessage("Building Frigate...");
                    shipBuilderHelper.scheduleShipBuildInDays("frigate", gamePanel.ui.star, 45);
                }
                return;
            }

            if (gamePanel.ui.buildColonyShipButton != null && gamePanel.ui.buildColonyShipButton.contains(e.getPoint())) {
                if (selectedStar.population < 10_000_000) {
                    gamePanel.ui.addMessage("Population too low - 10,000,000 needed before able to colonise!");
                    return;
                }

                if (canAfford("Colony Ship")) {
                    gamePanel.ui.addMessage("Building Colony Ship...");
                    selectedStar.population -= 200_000;
                    shipBuilderHelper.scheduleShipBuildInDays("colonyship", gamePanel.ui.star, 180);
                }
                return;
            }
        }

        if (gamePanel.ui.starIsSelected && gamePanel.ui.star != null && gamePanel.ui.star.station == null) {
            Star star = gamePanel.ui.star;
            if (gamePanel.ui.buildBasicShipyardButton != null && gamePanel.ui.buildBasicShipyardButton.contains(e.getPoint()) && star.colonised == Star.Colonised.COLONISED) {
                if (canAfford("Basic Shipyard")) {
                    gamePanel.ui.addMessage("Building Basic Shipyard...");
                    shipBuilderHelper.scheduleStationBuildInDays("basicshipyard", gamePanel.ui.star, 180);
                }
                return;
            }
        }

        if (gamePanel.ui.starIsSelected && gamePanel.ui.star != null) {
            Star star = gamePanel.ui.star;
            if (gamePanel.ui.buildSmallSatelliteButton != null && gamePanel.ui.buildSmallSatelliteButton.contains(e.getPoint()) && star.colonised == Star.Colonised.COLONISED) {
                if (canAfford("Small Satellite")) {
                    gamePanel.ui.addMessage("Building Small Satellite...");
                    shipBuilderHelper.scheduleStationBuildInDays("smallsatellite", gamePanel.ui.star, 30);
                }
                return;
            }
        }

        for (Ship ship : gamePanel.getShips()) {
            if (ship != null) {
                int entityLeft = ship.solidArea.x;
                int entityTop = ship.solidArea.y;
                int entityRight = entityLeft + ship.solidArea.width;
                int entityBottom = entityTop + ship.solidArea.height;

                if (mouseX >= entityLeft && mouseX <= entityRight &&
                        mouseY >= entityTop && mouseY <= entityBottom) {

                    if (!shiftHeld) {
                        // Deselect all previously selected ships
                        for (Ship s : selectedShips) s.selected = false;
                        selectedShips.clear();
                    }

                    if (!selectedShips.contains(ship)) {
                        selectedShips.add(ship);
                        ship.selected = true;
                        gamePanel.ui.addMessage(ship.name + " is selected");
                    }

                    if (selectedStar != null) selectedStar.selected = false;
                    selectedStar = null;
                    gamePanel.repaint();




                    return;
                }
            }
        }

        for (Star star : gamePanel.starMap.getStars()) {

            combatFlashClickHandler(mouseX,mouseY,star);

            if (mouseX >= star.solidArea.x && mouseX <= star.solidArea.x + star.solidArea.width &&
                    mouseY >= star.solidArea.y && mouseY <= star.solidArea.y + star.solidArea.height) {

                if (selectedStar != null) selectedStar.selected = false;

                star.selected = true;
                selectedStar = star;
                gamePanel.ui.setStar(star);
                gamePanel.ui.starIsSelected = true;
                gamePanel.ui.selectedMessageOn = true;



                /*
                if (e.getClickCount() == 2 && selectedShips.size() == 1) {
                    Ship ship = selectedShips.getFirst();
                    System.out.println("test 2");
                    if (ship.name.equalsIgnoreCase("Colony Ship") && ship.currentStar == selectedStar) {
                        System.out.println("TEST 1; SELECTED COLONY SHIP AND GOT CURRENT STAR AS SELECTED STAR");

                        if (selectedStar.quality == Star.Quality.UNINHABITABLE) {
                            gamePanel.ui.addMessage("Cannot colonise " + selectedStar.name + ", the system is uninhabitable");
                        } else if (selectedStar.colonised == Star.Colonised.COLONISED) {
                            gamePanel.ui.addMessage("Cannot colonise " + selectedStar.name + ", the system is already colonised");
                        } else {
                            selectedStar.colonised = Star.Colonised.BEGUN;
                            gamePanel.starMap.colonisedStars.add(selectedStar);
                            gamePanel.removeShip(ship);
                            gamePanel.ui.addMessage("Colonisation begun on " + selectedStar.name);
                        }
                        selectedShips.clear();
                        selectedStar.selected = false;
                        selectedStar = null;
                        gamePanel.repaint();
                        return;
                    }
                }

                 */

                if (!selectedShips.isEmpty()) {
                    double delayIncrement = 0.2; // 0.1 game days between each ship
                    double baseGameDay = gamePanel.gameClock.getTotalGameDays();

                    for (int i = 0; i < selectedShips.size(); i++) {
                        Ship ship = selectedShips.get(i);
                        if (!(ship.name.equalsIgnoreCase("Colony Ship") && ship.currentStar == selectedStar)) {
                            double delay = i * delayIncrement;
                            ship.startMovingToStarWithDelay(selectedStar, delay, baseGameDay);
                            gamePanel.ui.addMessage(ship.name + " scheduled to move to star: " + selectedStar.name + " in " + delay + " days");
                        }
                    }

                    for (Ship s : selectedShips) s.selected = false;
                    selectedShips.clear();
                    selectedStar.selected = false;
                    selectedStar = null;
                    gamePanel.ui.selectedMessageOn = false;
                    gamePanel.repaint();
                    return;
                }

            }
        }

        // Clicked empty space
        for (Ship s : selectedShips) s.selected = false;
        selectedShips.clear();
        gamePanel.ui.selectedMessageOn = false;

        gamePanel.repaint();
    }

    private void combatFlashClickHandler(int mouseX, int mouseY, Star star) {

        if (gamePanel.visitedStars.contains(star) && star.combatButton != null) {
            if (star.combatButton.contains(mouseX, mouseY)) {
                System.out.println("Viewing combat at star: " + star.name); //todo remove
                gamePanel.combatManager.combatGUI.show();

            }


        }
    }

    public boolean canAfford(String eName) {
        if (gamePanel.money - gamePanel.buildCosts.get(eName) < 0) {
            gamePanel.ui.addMessage("Cannot afford to build " + eName);
            return false;
        } else {
            gamePanel.addMoney(-gamePanel.buildCosts.get(eName));
            return true;
        }
    }


    public void rightMouseClick(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        for (Star star : gamePanel.starMap.getStars()) {
            if (mouseX >= star.solidArea.x && mouseX <= star.solidArea.x + star.solidArea.width &&
                    mouseY >= star.solidArea.y && mouseY <= star.solidArea.y + star.solidArea.height) {

//                if (selectedStar != null) selectedStar.selected = false;

                selectedStar = star;


                if (selectedShips.size() == 1) {
                    Ship selectedShip = selectedShips.getFirst();

                    if (selectedShip.name.equalsIgnoreCase("Colony Ship") &&
                            selectedShip.currentStar == star) {
                        System.out.println("colonising test true");
                        if (selectedStar.quality == Star.Quality.UNINHABITABLE) {
                            gamePanel.ui.addMessage("Cannot colonise " + selectedStar.name + ", the system is uninhabitable");
                        } else if (selectedStar.colonised == Star.Colonised.COLONISED) {
                            gamePanel.ui.addMessage("Cannot colonise " + selectedStar.name + ", the system is already colonised");
                        } else {
                            selectedStar.colonised = Star.Colonised.BEGUN;
                            gamePanel.starMap.colonisedStars.add(selectedStar);
                            gamePanel.removeShip(selectedShip);
                            gamePanel.ui.addMessage("Colonisation begun on " + selectedStar.name);
                        }
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
        if (gamePanel.combatManager.inCombat) {
            RoundRectangle2D closeButton = gamePanel.combatManager.combatGUI.closeButton;
            if (mouseX >= closeButton.getX() && mouseX <= closeButton.getX() + closeButton.getWidth() &&
                    mouseY >= closeButton.getY() && mouseY <= closeButton.getY() + closeButton.getHeight()) {
                System.out.println("closing combat panel");
                gamePanel.combatManager.combatGUI.hide();
            }

        }

    }
}
