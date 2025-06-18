package main;

import entity.Ship;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {

    GamePanel gamePanel;
    Ship selectedShip = null;
    Star selectedStar = null;
//    public ShipBuilderHelper shipBuilderHelper = new ShipBuilderHelper(gamePanel);
    public MouseHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        System.out.println("mouse: " + mouseX + " " + mouseY);

           ShipBuilderHelper shipBuilderHelper = gamePanel.shipBuilderHelper;

        if (gamePanel.ui.starIsSelected && gamePanel.ui.star != null && gamePanel.ui.star.station != null) {

            if (gamePanel.ui.buildScoutButton != null && gamePanel.ui.buildScoutButton.contains(e.getPoint())) {

                if (canAfford("Scout")) {
                    gamePanel.ui.addMessage("Building Scout...");
                    shipBuilderHelper.scheduleShipBuildInDays("scout", gamePanel.ui.star, 14.0); // in 5 game days
                }
                return;
            }

            if (gamePanel.ui.buildFrigateButton != null && gamePanel.ui.buildFrigateButton.contains(e.getPoint())) {
                if (canAfford("Frigate")) {
                    gamePanel.ui.addMessage("Building Frigate...");
                    shipBuilderHelper.scheduleShipBuildInDays("frigate", gamePanel.ui.star, 45); // in 5 game days

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
                    shipBuilderHelper.scheduleShipBuildInDays("colonyship", gamePanel.ui.star, 180); // in 5 game days
                }
                return;
            }
        }
        //no station: build basic shipyard

        if (gamePanel.ui.starIsSelected && gamePanel.ui.star != null && gamePanel.ui.star.station == null) {

           Star star = gamePanel.ui.star;
            if (gamePanel.ui.buildBasicShipyardButton != null && gamePanel.ui.buildBasicShipyardButton.contains(e.getPoint()) && star.colonised == Star.Colonised.COLONISED) {
                if (canAfford("Basic Shipyard")) {
                    gamePanel.ui.addMessage("Building Basic Shipyard...");
                    shipBuilderHelper.scheduleStationBuildInDays("basicshipyard", gamePanel.ui.star, 180); // in 5 game days
                }
                return;
            }
        }

        if (gamePanel.ui.starIsSelected && gamePanel.ui.star != null) {
            Star star = gamePanel.ui.star;
            if (gamePanel.ui.buildSmallSatelliteButton != null && gamePanel.ui.buildSmallSatelliteButton.contains(e.getPoint()) && star.colonised == Star.Colonised.COLONISED) {
                if (canAfford("Small Satellite")) {
                    gamePanel.ui.addMessage("Building Small Satellite...");
                    shipBuilderHelper.scheduleStationBuildInDays("smallsatellite", gamePanel.ui.star, 30); // in 5 game days
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
                    if (selectedShip != null) selectedShip.selected = false;
                    selectedShip = ship;
                    selectedShip.selected = true;

                    gamePanel.ui.addMessage(ship.name +" is selected");


                    if (selectedStar != null) selectedStar.selected = false;
                    selectedStar = null; // Clear any previous star selection
                    gamePanel.repaint();
                    return;
                }

            }
        }



        for (Star star : gamePanel.starMap.getStars()) {

            if (mouseX >= star.solidArea.x && mouseX <= star.solidArea.x + star.solidArea.width &&
                    mouseY >= star.solidArea.y && mouseY <= star.solidArea.y + star.solidArea.height) {

                if (selectedStar != null) selectedStar.selected = false; //only 1 star selectable


                star.selected = true;
                selectedStar = star;
//                System.out.println(star.name +" is selected");
                gamePanel.ui.setStar(star);
                gamePanel.ui.starIsSelected = true;
                gamePanel.ui.selectedMessageOn = true;

                if (e.getClickCount() == 2 && selectedShip != null) {

                    if (selectedShip.name.equalsIgnoreCase("Colony Ship") &&
                            selectedShip.currentStar == selectedStar) {

                        if (selectedStar.quality == Star.Quality.UNINHABITABLE) {
                            gamePanel.ui.addMessage("Cannot colonise " + selectedStar.name + ", the system is uninhabitable");
                            return;
                        } else if (selectedStar.colonised == Star.Colonised.COLONISED) {
                            gamePanel.ui.addMessage("Cannot colonise " + selectedStar.name + ", the system is already colonised");
                            return;
                        } else {

                            // Begin colonisation
                            selectedStar.colonised = Star.Colonised.BEGUN;
                            gamePanel.starMap.colonisedStars.add(selectedStar);

                            // Remove the ship from the game
                            gamePanel.removeShip(selectedShip);
                            gamePanel.ui.addMessage("Colonisation begun on " + selectedStar.name);


                            selectedShip = null;
                            selectedStar.selected = false;
                            selectedStar = null;
                            gamePanel.repaint();
                            return;
                        }
                    }
                }



                if (selectedShip != null) {
                    // Prevent moving colony ship if already orbiting — allow double-click colonisation
                    if (selectedShip.name.equalsIgnoreCase("Colony Ship") && selectedShip.currentStar == selectedStar) {
                        // Do nothing — wait for double click
                        return;
                    }

                    // Move the ship to the star
                    selectedShip.startMovingToStar(selectedStar);
                    gamePanel.ui.addMessage(selectedShip.name + " begun moving to star: " + selectedStar.name);
                    gamePanel.ui.selectedMessageOn = false;

                    // Deselect everything
                    selectedShip.selected = false; //todo set to true for debug
                    selectedShip = null;
                    selectedStar.selected = false;
                    selectedStar = null;
                    gamePanel.repaint();
                    return;
                }


            }

        }



        // Clicked empty space: clear all selections

        //clicked a ship then empty space
        if (selectedShip != null && selectedStar == null) {
            selectedShip.selected = false;
            selectedShip = null;
            gamePanel.ui.selectedMessageOn = false;

        }

//        if (selectedStar != null && selectedShip == null) {
//            selectedStar.selected = false;
//            selectedStar = null;
//        }









        gamePanel.repaint();

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
}
