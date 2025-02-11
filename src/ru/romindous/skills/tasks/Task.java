package ru.romindous.skills.tasks;

import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.romindous.skills.survs.Survivor;

public interface Task {

    HashSet<Integer> occupied = new HashSet<>();
    HashMap<Integer, TaskType> clickable = new HashMap<>();
    int chs = 100;
    int factor = 10;
    int shift = 6;
    float charFct = 0.06f;

    void tryComplete();

    boolean secondTick();

    boolean end(final Player pl);

    //public abstract void onInteract(final Player p);
    TaskType getType();

    Survivor getSurv();

    LivingEntity getNpc();

    static Sound getTalkSound(final EntityType et) {
        return switch (et) {
            case PILLAGER -> Sound.ENTITY_PILLAGER_AMBIENT;
            case PIGLIN, PIGLIN_BRUTE -> Sound.ENTITY_PIGLIN_AMBIENT;
            default -> Sound.ENTITY_VILLAGER_AMBIENT;
        };
    }
}
