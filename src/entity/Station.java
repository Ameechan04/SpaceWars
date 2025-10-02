package entity;

import main.GamePanel;
import main.Star;

public class Station extends StationaryEntity{

    public Station(GamePanel gamePanel, String name,Star currentStar, int buildCost, int maxHealth, int damage, Faction faction) {
        super(gamePanel, name,currentStar, false, buildCost, maxHealth, damage, faction);
        currentStar.setStation(this);
    }


  }
