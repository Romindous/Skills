package ru.romindous.skills.mobs.wastes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.utils.FastMath;
import ru.romindous.skills.objects.SkillMats;
import ru.romindous.skills.mobs.SednaMob;

import java.util.Map;

public class Crawler extends SednaMob {

    public String biome() {
        return "ruined_city";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return Spider.class;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    private static final ItemStack WEB = new ItemStack(Material.COBWEB);

    @Override
    protected void onTarget(final EntityTargetEvent e) {
        if (e.getEntity() instanceof final Mob mb) {
            final Snowball sb = mb.launchProjectile(Snowball.class, FastMath.getShotVec(
                e.getTarget().getLocation().subtract(mb.getLocation()).toVector(), 1.6d), s -> {
                s.setItem(WEB);
            });

            Ostrov.sync(() -> {
                if (sb.isValid()) sb.remove();
            }, 100);
        }
    }

    @Override
    protected void onExtra(final EntityEvent e) {
        super.onExtra(e);
        if (e instanceof final ProjectileHitEvent ee) {
            final Projectile prj = ee.getEntity();
            if (!prj.isValid()) return;
            final Location loc = prj.getLocation();
            final Block b = loc.getBlock();
            if (b.getType().isAir()) {
                b.setType(WEB.getType(), false);
                Ostrov.sync(() -> {
                    final Block bl = loc.getBlock();
                    if (bl.getType() == WEB.getType()) {
                        bl.setType(Material.AIR, false);
                    }
                }, 100);
            }
        }
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_string", new ItemStack(Material.STRING), 1, 1, 2), 2)
        .add(new ItemRoll(key().value() + "_meat", SkillMats.CRAWLER.getItem(Material.MUTTON), 3, 1), 1)
        .add(new ItemRoll(key().value() + "_eye", new ItemStack(Material.SPIDER_EYE), 2, 1), 1)
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