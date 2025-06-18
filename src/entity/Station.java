package entity;

import main.GamePanel;
import main.Star;

public class Station extends StationaryEntity{

    public Station(GamePanel gamePanel, String name,Star currentStar, int buildCost, int maxHealth, int damage) {
        super(gamePanel, name,currentStar, false, buildCost, maxHealth, damage);
        currentStar.setStation(this);
    }


  }
