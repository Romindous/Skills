package ru.romindous.skills.mobs.wastes;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Zoglin;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.items.ItemRoll;
import ru.romindous.skills.mobs.SednaMob;

import java.util.Map;

public class Beast extends SednaMob {

    public String biome() {
        return "bloody_desert";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return Zoglin.class;
    }

    private final double minSize = mobConfig("min_scl", 0.6d),
        maxSize = mobConfig("scale", 1.0d);


    @Override
    public Map<Attribute, Double> attributes() {
        atts.put(Attribute.SCALE, minSize + (Ostrov.random.nextDouble() * (maxSize - minSize)));
        return atts;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        super.onDeath(e);
        if (!(e.getEntity() instanceof final Mob mb)) return;

    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_flesh", new ItemStack(Material.ROTTEN_FLESH), 2, 1, 2), 4)
        .add(new ItemRoll(key().value() + "_leather", new ItemStack(Material.LEATHER), 1, 1), 1)
        .add(new ItemRoll(key().value() + "_bone", new ItemStack(Material.BONE), 1, 1), 1)
        .add(new ItemRoll(key().value() + "_pork", new ItemStack(Material.PORKCHOP), 1, 1, 1), 2)
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
