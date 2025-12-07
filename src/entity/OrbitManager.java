package entity;

import main.GamePanel;
import main.Star;

import java.util.*;

public class OrbitManager {
    private Star star;
    private GamePanel gamePanel;

    // Map: Ship type -> ordered list of ships
    private Map<String, List<Ship>> shipsByType = new HashMap<>();

    // Spacing constants
    private final int DOWN_SPACING = 5;
    private final int UP_SPACING = 2;

    public OrbitManager(GamePanel gamePanel, Star star) {
        this.gamePanel = gamePanel;
        this.star = star;
    }

    /** Add a ship to orbiting list */
    public void addShip(Ship ship) {
        shipsByType.computeIfAbsent(ship.name, k -> new ArrayList<>()).add(ship);
        recalcOrbit(ship.name);
    }

    /** Remove a ship from orbiting list */
    public void removeShip(Ship ship) {
        List<Ship> group = shipsByType.get(ship.name);
        if (group != null) {
            group.remove(ship);
            if (group.isEmpty()) shipsByType.remove(ship.name);
            else recalcOrbit(ship.name);
        }
    }

    /** Recalculate orbit positions for a given type */
    private void recalcOrbit(String shipName) {
        List<Ship> group = shipsByType.get(shipName);
        if (group == null || group.isEmpty()) return;

        for (int i = 0; i < group.size(); i++) {
            Ship ship = group.get(i);
            ship.orbitStackIndex = i;

            if (group.size() >= 3) {
                // Stack logic
                Ship representative = group.get(0);
                if (ship == representative) {
                    ship.orbitOffsetX = ship.defaultOrbitOffsetX;
                    ship.orbitOffsetY = (int) (ship.defaultOrbitOffsetY + i * (ship.solidArea.getHeight() + DOWN_SPACING));
                } else {
                    ship.orbitOffsetX = 0;
                    ship.orbitOffsetY = 0;
                    ship.exactCentreX = representative.exactCentreX;
                    ship.exactCentreY = representative.exactCentreY;
                }
            } else {
                // 1-2 ships: normal offset by index
                ship.orbitOffsetX = ship.defaultOrbitOffsetX;
                ship.orbitOffsetY = (int) (ship.defaultOrbitOffsetY + i * (ship.solidArea.getHeight() + DOWN_SPACING));
            }

            // Update centre position
            if (group.size() < 3 || ship == group.get(0)) {
                ship.setCentrePosition(star.x + ship.orbitOffsetX, star.y + ship.orbitOffsetY);
            }
        }
    }

    /** Get the target orbit position for a ship (for movement) */
    public int getTargetX(Ship ship) {
        return (int) (star.x + ship.orbitOffsetX);
    }

    public int getTargetY(Ship ship) {
        return (int) (star.y + ship.orbitOffsetY);
    }

    public void recalculateStacks() {
        for (String shipType : shipsByType.keySet()) {
            recalcOrbit(shipType);
        }
    }
}
