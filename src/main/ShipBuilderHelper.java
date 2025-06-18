package main;

import entity.Ship;
import entity.StationaryEntity;

import java.util.*;

public class ShipBuilderHelper {
    GamePanel gamePanel;
    Map<Star, LinkedList<BuildTask>> starQueues = new HashMap<>();

    public ShipBuilderHelper(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void scheduleShipBuildInDays(String type, Star star, double buildDuration) {
        double now = gamePanel.gameClock.getTotalGameDays();
        LinkedList<BuildTask> queue = starQueues.computeIfAbsent(star, k -> new LinkedList<>());

        double startTime = queue.isEmpty() ? now : queue.getLast().scheduledCompletionDay;
        double endTime = startTime + buildDuration;

        BuildTask task = new BuildTask(type, star, startTime,endTime, BuildTask.Type.SHIP, gamePanel);
        queue.add(task);

        System.out.println("Scheduled build of " + type + " at " + star.name + " for day " + endTime);
    }


    public void scheduleStationBuildInDays(String type, Star star, double buildDuration) {
        double now = gamePanel.gameClock.getTotalGameDays();
        LinkedList<BuildTask> queue = starQueues.computeIfAbsent(star, k -> new LinkedList<>());

        double startTime = queue.isEmpty() ? now : queue.getLast().scheduledCompletionDay;
        double endTime = startTime + buildDuration;

        BuildTask task = new BuildTask(type, star, startTime,endTime, BuildTask.Type.STATIONARY, gamePanel);
        queue.add(task);

        System.out.println("Scheduled build of " + type + " at " + star.name + " for day " + endTime);   }

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
            switch (task.type) {
                case SHIP -> {
                    Ship newShip = switch (task.buildType.toLowerCase()) {
                        case "scout" -> new entity.Scout(gamePanel, task.star);
                        case "frigate" -> new entity.Frigate(gamePanel, task.star);
                        case "colonyship" -> new entity.ColonyShip(gamePanel, task.star);
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
                        case "basicshipyard" -> new entity.BasicShipyard(gamePanel, task.star);
                        case "smallsatellite" -> {
                            int index = task.star.satellites.size();
                            var sat = new entity.SmallSatellite(gamePanel, task.star, index);
                            task.star.satellites.add(sat);
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

