package main;

public class GameClock {
    public int day = 1, month = 1, year = 2300;
    private int lastProcessedDay = -1;
    private int lastProcessedMonth = -1;

    private double dayCounter = 0;
    StarMap starMap;
    private double elapsedGameDaysThisFrame = 0;
    private double totalGameDays = 0;
    public int gameSpeed = 1; //speed is 1 on default


    // In GamePanel.update()
    public void updateTime(double elapsedRealSeconds) {
        dayCounter += elapsedRealSeconds * 0.5 * gameSpeed; //1 in-game days per real second

        if (dayCounter >= 1.0) {
            int daysToAdvance = (int) dayCounter;
            for (int i = 0; i < daysToAdvance; i++) {
                advanceOneDay(); // update game state per day
            }
            dayCounter -= daysToAdvance;
        }
    }

    public void setGameSpeed(int gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    public void advanceOneDay() {
        day++;
        totalGameDays++;
        if (day > 30) {
            day = 1;
            month++;
            if (month > 12) {
                month = 1;
                year++;
            }
        }

//        starMap.updateDay(getCurrentDayCount()); // call daily updates
        // TODO: Add construction updates, income, etc.
    }

    public int getCurrentDayCount() {
        return year * 360 + month * 30 + day;
    }

    public double getElapsedGameDays() {
        return elapsedGameDaysThisFrame;
    }

    public double getTotalGameDays() {
        return totalGameDays;
    }


    public int getCurrentMonth() {
        return (int)(totalGameDays / 30); // Assuming 30 days per month
    }

    public boolean isNewMonth() {
        int currentMonth = getCurrentMonth();
        if (currentMonth > lastProcessedMonth) {
            lastProcessedMonth = currentMonth;
            return true;
        }
        return false;
    }

    public boolean isNewDay() {
        int currentDayCount = getCurrentDayCount();
        if (currentDayCount > lastProcessedDay) {
            lastProcessedDay = currentDayCount;
            return true;
        }
        return false;
    }


}
