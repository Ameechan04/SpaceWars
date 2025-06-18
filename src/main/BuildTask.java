package main;

// BuildTask.java
public class BuildTask {
    public enum Type { SHIP, STATIONARY }

    public String buildType;
    public Star star;
    public double scheduledCompletionDay, startDay;
    public Type type;
    private GamePanel gamePanel;
    public BuildTask(String buildType, Star star, double startDay, double completionDay, Type type, GamePanel gamePanel) {
        this.buildType = buildType;
        this.star = star;
        this.startDay = startDay;
        this.scheduledCompletionDay = completionDay;
        this.type = type;
        this.gamePanel = gamePanel;
    }

    public double getProgress() {
        double currentDay = gamePanel.gameClock.getTotalGameDays();
        double duration = scheduledCompletionDay - startDay;
        double progress = (currentDay - startDay) / duration;
        return Math.min(Math.max(progress, 0), 1); // Clamp between 0 and 1    }
    }
}
