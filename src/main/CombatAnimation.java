package main;

import entity.Entity;

public class CombatAnimation {
    public Entity attacker;
    public Entity target;
    public int ticksRemaining = 10; // or any short lifespan

    public CombatAnimation(Entity attacker, Entity target) {
        this.attacker = attacker;
        this.target = target;
    }

    public boolean isExpired() {
        return --ticksRemaining <= 0;
    }
}

