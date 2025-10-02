package main;

import entity.*;

import java.util.*;

public class AIBrain {
    public boolean debug = true;
    GamePanel gamePanel;
    ArrayList<StationaryEntity> stationaryEntities;
    ArrayList<Ship> ships;
    ArrayList<Entity> entities;
    public Set<Star> visitedStars = new HashSet<>();
    private Set<Star> pendingDestinations = new HashSet<>();
    int balance = 1000;
    ShipBuilderHelper shipBuilderHelper;
    public List<Star> colonisedStars = new ArrayList<>();
    public HashMap<Star.Quality, Integer> exploredStarQualities;
    private int revenue;
    ColonisationManager colonisationManager;


    private boolean hasExplored = false;
    private boolean readyToExplore = false;


    public AIBrain(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.entities = (ArrayList<Entity>) gamePanel.getEnemyEntities();
        this.stationaryEntities = new ArrayList<>();
        this.ships = new ArrayList<>();
        this.shipBuilderHelper = new ShipBuilderHelper(gamePanel, Entity.Faction.ENEMY);
        this.colonisationManager = new ColonisationManager(gamePanel);

//        System.out.println("ai entities");

        revenue = 0;

        exploredStarQualities = new HashMap<>();
        exploredStarQualities.put(Star.Quality.POOR, 0);
        exploredStarQualities.put(Star.Quality.MEDIUM, 0);
        exploredStarQualities.put(Star.Quality.RICH, 0);
        exploredStarQualities.put(Star.Quality.BARREN, 0);
        exploredStarQualities.put(Star.Quality.UNINHABITABLE, 0);

        for (Entity entity : entities) {
            if (entity instanceof StationaryEntity) {
                stationaryEntities.add((StationaryEntity) entity);
            } else if (entity instanceof Ship) {
                ships.add((Ship) entity);
            }
        }
    }

    //moving scouts for now
    public void explore() {
        ArrayList<Star> unexploredStars = new ArrayList<>();

        pendingDestinations.clear();
        for (Ship ship : ships) {

            //colony ships shouldn't explore
            if (ship instanceof ColonyShip)
                continue;
            if (ship.targetStar != null) {
                pendingDestinations.add(ship.targetStar);
            }
        }

        for (Star star : gamePanel.starMap.getStars()) {
            if (!visitedStars.contains(star) && !pendingDestinations.contains(star)) {
                unexploredStars.add(star);
            }
        }

        // If nothing left to explore, make ships orbit and exit
        if (unexploredStars.isEmpty()) {
            for (Ship ship : ships) {
                // Only mark orbit if the ship is at a star
                if (ship.currentStar != null) {
                    ship.inOrbit = true;
                }

                if (ship.inOrbit) {
//                    exploredStarQualities.put(ship.targetStar.quality, exploredStarQualities.get(ship.targetStar.quality) + 1);
                    ship.targetStar = null;
                }
            }
            return;
        }

        // Otherwise assign idle scouts to nearest unexplored stars
        for (Ship ship : ships) {
            if (ship instanceof Scout) {
                // Only assign if ship is idle
                if (!ship.inOrbit) continue;

                Star target = findNearestUnassignedStar(ship, unexploredStars);
                if (target != null) {
//                send_debug_message("Sending " + ship.name + " to " + target.name);
                    ship.startMovingToStar(target);
                    unexploredStars.remove(target); // prevent duplicates
                }
            }
        }
    }



    private Star findNearestUnassignedStar(Ship ship, ArrayList<Star> candidates) {
        Star nearest = null;
        double minDistance = Double.MAX_VALUE;
        Star from = ship.currentStar;

        if (from == null) return null;
        for (Star star : candidates) {
            double dist = Math.hypot(from.x - star.x, from.y - star.y);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = star;
            }
//            System.out.println(star.name + " " + dist + " from " + from.name);
        }

        if (nearest != null) {
//            System.out.println("The nearest unexplored is: " + nearest.name + " from  " + from.name);
        } else {
//            System.out.println("No unexplored stars found or candidates list was empty.");
        }
        return nearest;
    }


    public void update() {
        updateEntities();

       updateColonisedStars();

        if (!readyToExplore) {
            readyToExplore = true;
            return; // Wait 1 tick to give entities time to initialise
        }

        explore(); // Called every tick to assign idle scouts
        handleColonisation();



        handleBuilds(); // Schedule new ships/stations
        shipBuilderHelper.update(); // Complete scheduled builds


    }

    protected void updateEntities() {
        this.entities = (ArrayList<Entity>) gamePanel.getEnemyEntities();

        for (Entity entity : entities) {
            if (entity instanceof StationaryEntity) {
                stationaryEntities.add((StationaryEntity) entity);
            } else if (entity instanceof Ship) {
                ships.add((Ship) entity);
            }
        }
    }

    private void handleBuilds() {
        if (balance <= 0) {
            send_debug_message("My balance is below 0: I cannot build");
            return;
        }

        if (revenue <= 0) {
            send_debug_message("Revenue is negative, I will avoid debt");
            return;
        }

        for (Star star : colonisedStars) {
            // Determine what exists at this star
            boolean hasShipyard = stationaryEntities.stream()
                    .anyMatch(s -> s instanceof entity.BasicShipyard && s.currentStar == star);

            // Build options
            String[] buildOptions = hasShipyard
                    ? new String[]{"scout", "colonyship", "frigate"}
                    : new String[]{"basicshipyard", "smallsatellite"};

            // Evaluate merits
            Map<String, Integer> buildMerits = new HashMap<>();
            for (String type : buildOptions) {
                // Priority rules: if shipyard is queued/under construction, don't spam satellites
                if (!hasShipyard && isBeingBuilt("basicshipyard", star) && type.equals("smallsatellite")) {
                    buildMerits.put(type, 0); // deprioritize
                    continue;
                }

                // Cap per type
                if (countQueued(type, star) >= 50) {
                    buildMerits.put(type, 0); // reached max
                    continue;
                }

                int merit = evaluateBuildMerit(type, star);
                buildMerits.put(type, merit);
            }

            // Pick the best build
            String bestBuild = buildMerits.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (bestBuild != null && buildMerits.get(bestBuild) > 0) {
                shipBuilderHelper.scheduleBuild(bestBuild, star);
                send_debug_message("AI decided to build: " + bestBuild + " at " + star.name);
                int cost = gamePanel.humanPlayer.getBuildCost(bestBuild);
                if (balance >= cost) {
                    shipBuilderHelper.scheduleBuild(bestBuild, star);
                    balance -= cost; // Deduct immediately so further builds respect it
                    send_debug_message("AI decided to build: " + bestBuild + " at " + star.name + " | cost: " + cost + " | remaining balance: " + balance);
                } else {
                    send_debug_message("Cannot afford " + bestBuild + " at " + star.name + " | cost: " + cost + " | balance: " + balance);
                }
            }
        }
    }




    private int evaluateBuildMerit(String type, Star star) {
        int merit = 0;

        // Skip if already building
        if (isAlreadyBuilding(type, star)) {
            return 0; // or very low value
        }

        if (countQueued(type, star) >= 2) return 0; // stop adding more

        switch (type.toLowerCase()) {
            case "basicshipyard" -> {
                boolean hasShipyard = stationaryEntities.stream().anyMatch(s ->
                        s instanceof entity.BasicShipyard && s.currentStar == star
                );


                if (!hasShipyard && canAfford("basicshipyard") ) {
                    send_debug_message("I have no shipyard, that is my highest priority", "yellow");
                    merit += 1000; // priority if none
                }

            }
            case "smallsatellite" -> {
                if (!canAfford("smallsatellite")) {
                    break;
                }
                int numberSats = star.defences;
                if (numberSats == 0) {
                    send_debug_message(star.name + " has no defences, I should build 1 satellite", "yellow");
                    merit += 900;
                } else if (numberSats < 3) {
                    send_debug_message(star.name + " has only a few defences, I should build 1 satellite", "yellow");
                    merit += 400;
                } else if (star.population > 100_000_000 && numberSats < 10) {
                    send_debug_message(star.name + " has a large population and should get more defences!", "yellow");
                    merit+= 500;

                } else {
                    merit += (int) (star.population / 1_000_000) / numberSats;
                    send_debug_message(star.name + " has " + star.population + " and only " + numberSats + " defences. Merit for more: " + merit, "yellow");

                }
            }
            case "scout" -> {
                if (!canAfford("scout")) {
                    break;
                }
                int unexplored = (int) gamePanel.starMap.getStars().stream()
                        .filter(s -> !visitedStars.contains(s))
                        .count();
                if (unexplored > 0)  {
                    send_debug_message("There are stars that I should prioritise exploring!", "yellow");
                    merit += 100;
                } else  {
                    send_debug_message("There are no stars that I need to explore, I will not prioritise this");
                    merit += 10;
                }

            }
            case "colonyship" -> {
                if (!canAfford("colonyship")) {
                    break;
                }



                //e.g. if we have 1 star colonised and explored 10, we have 10% colonised
                double percentageVisitedAndColonised = (double) visitedStars.size() / (colonisedStars.size()) * 100;


                //a rich star is the best reason to build a colony ship
                if (exploredStarQualities.get(Star.Quality.RICH) > 1) {
                    merit += 500;
                    send_debug_message("There is an un-colonised Rich Star! I will prioritise colonising this!", "yellow");
                    //there are no rich stars free but i've explored a lot so i'll settle with a medium star
                    //try at 15% colonised compared to explored
                } else if (exploredStarQualities.get(Star.Quality.MEDIUM) > 1 && percentageVisitedAndColonised < 15 ) {
                    merit += 500;
                    send_debug_message("Less than 15% of visited stars are colonised by me! I will now look to medium stars!", "yellow");


                    //if less than 5% of stars are colonised we will still build a colony ship to keep up
                } else if (percentageVisitedAndColonised < 5) {
                    merit += 600;
                    send_debug_message("I have colonised less than 5% of explored stars! I need to colonise more!", "yellow");

                } else {
                    merit += 10;
                }


                //if we are colonising any stars then the priority should be lowered (0 for now)
                for (Star star1: visitedStars) {
                    if (star1.colonised.equals(Star.Colonised.BEGUN)) {
                        merit = 0;
                        break;
                    }
                }


            }
            case "frigate" -> {

                if (canAfford("frigate"))  {
                    send_debug_message("I can afford a frigate so it has increased priority.", "yellow");
                    merit += 50;
                }
            }
        }
        return merit;
    }

    private void handleColonisation() {
        // Only consider stars we have explored but not colonised yet
        Set<Star> exploredStars = new HashSet<>(visitedStars);
        exploredStars.removeIf(s -> s.colonised == Star.Colonised.COLONISED);

        if (exploredStars.isEmpty()) return;

        List<StarScore> targets = rateColonisationTargets(exploredStars, colonisedStars);
        if (targets.isEmpty()) return;

        for (Ship ship : ships) {
            if (!ship.name.equalsIgnoreCase("Colony Ship")) continue;
            if (!ship.inOrbit) continue;

            // Best available target
            StarScore bestTarget = targets.get(0);
            Star targetStar = bestTarget.star();

            if (ship.currentStar == targetStar) {
                // Colony ship has arrived — attempt colonisation via ColonisationManager
                boolean started = colonisationManager.beginColonisation(ship, targetStar);
                if (started) {
                    colonisedStars.add(targetStar); // track AI-colonised stars
                    send_debug_message("AI started colonising " + targetStar.name + "!", "green");
                } else {
                    send_debug_message("AI failed to colonise " + targetStar.name, "red");
                }
            } else {
                // Ship still needs to travel
                ship.startMovingToStar(targetStar);
                send_debug_message("Sending colony ship to colonise " + targetStar.name, "yellow");
            }
        }
    }





    //    HELPERS
    public void send_debug_message(String message) {
        gamePanel.ui.aidecisionDisplay.addAIDebugMessage(message);
    }
    public void send_debug_message(String message, String colour) {
        gamePanel.ui.aidecisionDisplay.addAIDebugMessage(message, colour);
    }

    private void updateColonisedStars() {
        colonisedStars = new ArrayList<>(
                gamePanel.starMap.getStars().stream()
                        .filter(s -> s.owner == Entity.Faction.ENEMY && s.colonised == Star.Colonised.COLONISED)
                        .toList()
        );
    }

    private boolean isAlreadyBuilding(String buildType, Star star) {
        LinkedList<BuildTask> queue = shipBuilderHelper.starQueues.get(star);
        if (queue == null) return false;
        for (BuildTask task : queue) {
            if (task.buildType.equalsIgnoreCase(buildType)) {
                return true;
            }
        }
        return false;
    }

    private int countQueued(String type, Star star) {
        LinkedList<BuildTask> queue = shipBuilderHelper.starQueues.get(star);
        if (queue == null) return 0;
        int count = 0;
        for (BuildTask task : queue) {
            if (task.buildType.equalsIgnoreCase(type)) count++;
        }
        return count;
    }

    // Returns true if a given build type is already in progress at the star
    private boolean isBeingBuilt(String type, Star star) {
        return countQueued(type, star) > 0;
    }


    public List<StarScore> rateColonisationTargets(Set<Star> exploredStars, List<Star> colonisedStars) {
        List<StarScore> scoredStars = new ArrayList<>();

        for (Star candidate : exploredStars) {
            // Skip uninhabitable stars
            if (candidate.quality == Star.Quality.UNINHABITABLE) continue;

            //skip colonised or stars that have begun to be colonised
            if (candidate.colonised.equals(Star.Colonised.COLONISED) || candidate.colonised.equals(Star.Colonised.BEGUN)) continue;
            // 1. Star quality score
            int qualityScore = switch (candidate.quality) {
                case RICH -> 50;
                case MEDIUM -> 0;
                case POOR -> -20;
                case BARREN -> -50;
                default -> 0;
            };



            // 2. Proximity to nearest colonised star
            double shortestDistance = Double.POSITIVE_INFINITY;

            for (Star colony : colonisedStars) {
                List<Star> path = gamePanel.starMap.findShortestPath(candidate, colony);
                if (!path.isEmpty()) {
                    double pathDistance = 0;
                    for (int i = 0; i < path.size() - 1; i++) {
                        Star from = path.get(i);
                        Star to = path.get(i + 1);
                        pathDistance += Math.hypot(from.x - to.x, from.y - to.y);
                    }
                    shortestDistance = Math.min(shortestDistance, pathDistance);
                }
            }

            // Avoid division by zero
            double proximityScore = shortestDistance < Double.POSITIVE_INFINITY ? 100.0 / (shortestDistance + 1) : 0;

            double totalScore = qualityScore + proximityScore;
            scoredStars.add(new StarScore(candidate, totalScore));
        }

        // Sort descending by score
        scoredStars.sort((a, b) -> Double.compare(b.score, a.score));
        return scoredStars;
    }

    public int getRevenue() {
        return revenue;
    }

    public void setRevenue(int revenue) {
        this.revenue = revenue;
    }

    // Helper record
    public record StarScore(Star star, double score) {}


    private boolean canAfford(String e) {
        return balance >= gamePanel.humanPlayer.getBuildCost(e);
    }

}
