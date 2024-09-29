package ru.romindous.skills.mobs.wastes;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.world.AreaSpawner;
import ru.romindous.skills.mobs.SednaMob;

import java.util.Map;

public class Boned extends SednaMob {

    private final AreaSpawner.SpawnCondition empty = new AreaSpawner
        .SpawnCondition(0, CreatureSpawnEvent.SpawnReason.NATURAL);

    @Override
    protected AreaSpawner.SpawnCondition condition() {
        return empty;
    }

    @Override
    protected AreaSpawner spawner() {
        return null;
    }

    public String biome() {
        return "bloody_desert";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return Skeleton.class;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_bone", new ItemStack(Material.BONE), 2, 1), 1)
        .add(new ItemRoll(key().value() + "_meal", new ItemStack(Material.BONE_MEAL), 1, 1, 1), 2)
        .build(1, 2);

    @Override
    public RollTree loot() {
        return drop;
    }

    /*private static class WebGoal implements Goal<Mob> {

        private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Ostrov.instance, "web"));

        private final Mob mob;

        private WebGoal(final Mob mob) {
            this.mob = mob;
        }

        @Override
        public boolean shouldActivate() {
            return true;
        }

        @Override
        public boolean shouldStayActive() {
            return true;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void tick() {
        }

        @Override
        public @NotNull
        GoalKey<Mob> getKey() {
            return key;
        }

        @Override
        public @NotNull
        EnumSet<GoalType> getTypes() {
            return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
        }
    }*/
}
