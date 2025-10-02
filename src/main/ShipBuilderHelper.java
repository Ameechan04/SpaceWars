package main;

import entity.Entity;
import entity.Ship;
import entity.StationaryEntity;

import java.util.*;

public class ShipBuilderHelper {
    GamePanel gamePanel;
    Map<Star, LinkedList<BuildTask>> starQueues = new HashMap<>();
    Entity.Faction faction;
    public ShipBuilderHelper(GamePanel gamePanel, Entity.Faction faction) {
        this.gamePanel = gamePanel;
        this.faction = faction;
    }

    private double getBuildDuration(String type) {
        return switch (type.toLowerCase()) {
            case "scout" -> 10.0;
            case "frigate" -> 30.0;
            case "colonyship" -> 30.0;
            case "basicshipyard" -> 5.0;
            case "smallsatellite" -> 15.0;
            default -> 1.0; // Default fallback
        };
    }

    public void scheduleBuild(String type, Star star) {
        double buildDuration = getBuildDuration(type);

        switch (type.toLowerCase()) {
            // Ships
            case "scout", "frigate", "colonyship" -> scheduleBuildInDays(type, star, BuildTask.Type.SHIP);

            // Stationary entities
            case "basicshipyard", "smallsatellite" -> scheduleBuildInDays(type, star, BuildTask.Type.STATIONARY);

            default -> System.out.println("Unknown build type: " + type);
        }
    }

    public void scheduleBuildInDays(String type, Star star, BuildTask.Type buildType) {
        double now = gamePanel.gameClock.getTotalGameDays();
        LinkedList<BuildTask> queue = starQueues.computeIfAbsent(star, k -> new LinkedList<>());

        double startTime = queue.isEmpty() ? now : queue.getLast().scheduledCompletionDay;
        double endTime = startTime + getBuildDuration(type);

        BuildTask task = new BuildTask(type, star, startTime, endTime, buildType, gamePanel, faction);
        queue.add(task);

//        System.out.println(faction + " has scheduled " + type + " at " + star.name + " for day " + endTime);
    }
    public void update() {
        double currentDay = gamePanel.gameClock.getTotalGameDays();

        for (Iterator<Map.Entry<Star, LinkedList<BuildTask>>> it = starQueues.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Star, LinkedList<BuildTask>> entry = it.next();
            LinkedList<BuildTask> queue = entry.getValue();

            if (!queue.isEmpty()) {
                BuildTask task = queue.peek(); // Only check the head
                if (currentDay >= task.scheduledCompletionDay) {
                    build(task);
                    queue.removeFirst();
                }
            }

            if (queue.isEmpty()) {
                it.remove(); // Clean up empty queues
            }
        }
    }


    private void build(BuildTask task) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            Entity.Faction faction = task.faction;
            switch (task.type) {
                case SHIP -> {
                    Ship newShip = switch (task.buildType.toLowerCase()) {
                        case "scout" -> new entity.Scout(gamePanel, task.star, faction);
                        case "frigate" -> new entity.Frigate(gamePanel, task.star, faction);
                        case "colonyship" -> new entity.ColonyShip(gamePanel, task.star, faction);
                        default -> null;
                    };
                    if (newShip != null) {
                        gamePanel.addShip(newShip);
                        newShip.enterOrbit(task.star);
                        gamePanel.ui.addMessage(task.buildType + " built at " + task.star.name);
                    }
                }
                case STATIONARY -> {
                    StationaryEntity newStation = switch (task.buildType.toLowerCase()) {
                        case "basicshipyard" -> new entity.BasicShipyard(gamePanel, task.star, faction);
                        case "smallsatellite" -> {
                            int index = task.star.satellites.size();
                            var sat = new entity.SmallSatellite(gamePanel, task.star, index, faction);
                            task.star.satellites.add(sat);
                            task.star.defences++;
                            yield sat;
                        }
                        default -> null;
                    };
                    if (newStation != null) {
                        gamePanel.addStationaryEntity(newStation);
                        gamePanel.ui.addMessage(newStation.name + " built at " + task.star.name);
                    }
                }
            }
        });
    }
}

