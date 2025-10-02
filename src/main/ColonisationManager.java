package main;

import entity.Ship;

public class ColonisationManager {

    private final GamePanel gamePanel;

    public ColonisationManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * Attempt to colonise a star with a colony ship.
     * Returns true if colonisation started, false if invalid.
     */
    public boolean beginColonisation(Ship colonyShip, Star star) {
        if (colonyShip == null || star == null) return false;

        // Check star conditions
        if (star.quality == Star.Quality.UNINHABITABLE) {
            gamePanel.ui.addMessage("Cannot colonise " + star.name + ", the system is uninhabitable");
            return false;
        }

        if (star.colonised == Star.Colonised.COLONISED || star.colonised == Star.Colonised.BEGUN) {
            gamePanel.ui.addMessage("Cannot colonise " + star.name + ", the system is already colonised or under colonisation");
            return false;
        }

        // Begin colonisation
        star.colonised = Star.Colonised.BEGUN;
        star.coloniserFaction = colonyShip.faction;
        star.owner = colonyShip.faction;
        gamePanel.starMap.colonisedStars.add(star);
        gamePanel.removeShip(colonyShip);

        gamePanel.ui.addMessage("Colonisation begun on " + star.name);

        return true;
    }
}