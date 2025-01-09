package ru.romindous.skills.mobs.wastes;

import java.util.EnumSet;
import java.util.Map;
import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.NARoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.world.AreaSpawner;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.mobs.SednaMob;

public class Steed extends SednaMob {

    public final Infected RIDER = new Infected();

    private static final byte MIN_LIGHT = 4;
    private AreaSpawner spawn;

    public String biome() {
        return "dry_ocean";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return ZombieHorse.class;
    }

    @Override
    protected AreaSpawner spawner() {
        return spawn == null ? spawn = new Spawner() {
            protected boolean extra(final WXYZ loc) {
                return limit(loc) && loc.getBlock().getLightFromSky() > MIN_LIGHT;
            }
        } : spawn;
    }

    @Override
    public @Nullable Goal<Mob> goal(final Mob mb) {
        return new RideGoal(mb);
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_flesh", ItemType.ROTTEN_FLESH.createItemStack(), 2, 1), 6)
        .add(new ItemRoll(key().value() + "_kelp", ItemType.DRIED_KELP.createItemStack(), 1, 1), 2)
        .add(new ItemRoll(key().value() + "_leather", ItemType.LEATHER.createItemStack(), 1, 0), 1)
        .add(new NARoll(), 2).build(1, 1);

    @Override
    protected void onAttack(final EntityDamageByEntityEvent e) {
        super.onAttack(e);
        if (e.isCancelled()) return;
        if (!(e.getDamageSource().getCausingEntity() instanceof final Mob mb)) return;
        mb.setTarget(null);
    }

    @Override
    protected void onHurt(final EntityDamageEvent e) {
        super.onHurt(e);
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof final Mob mb)) return;
        if (e.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr) {
            mb.setTarget(dmgr);
        }
    }

    @Override
    public RollTree loot() {
        return drop;
    }

    private class RideGoal implements Goal<Mob> {

        private static final GoalKey<Mob> key = GoalKey.of(Mob.class, OStrap.key("ride"));
        private static final EnumSet<GoalType> types = EnumSet.noneOf(GoalType.class);
        private static final double RIDE_DST = 4d;
        private static final double SCARE_DST = 10d;

        private final Mob mob;

        private int tick = Main.srnd.nextInt(8);

        private RideGoal(final Mob mob) {this.mob = mob;}

        @Override
        public boolean shouldActivate() {return true;}

        @Override
        public void tick() {
            if (!mob.isValid()) return;
            if ((tick++ & 7) != 0) return;
            final Location loc = mob.getLocation();
            if (mob.getPassengers().isEmpty()) {
                for (final LivingEntity rd : LocUtil.getChEnts(loc,
                    RIDE_DST, RIDER.getEntClass(), e -> !e.isInsideVehicle())) {
                    if (rd instanceof final Mob mb) {
                        mob.addPassenger(mb);
                        mob.setTarget(mb.getTarget());
                        mob.setAggressive(true);
                        EntityUtil.effect(mob, Sound.ITEM_ARMOR_EQUIP_LEATHER,
                            0.8f, Particle.ANGRY_VILLAGER);
                        break;
                    }
                }
            }

            if (!mob.getPassengers().isEmpty()) {
                if (mob.getPassengers().getFirst() instanceof final Mob mb) {
                    mob.setTarget(mb.getTarget());
                    mob.setAggressive(true);
                }
                return;
            }

            final Player p = LocUtil.getClsChEnt(loc, SCARE_DST, Player.class, null);
            if (p == null) return;
            final Pathfinder pf = mob.getPathfinder();
            pf.moveTo(loc.add(loc.toVector().subtract(p.getLocation()
                .toVector()).normalize().multiply(SCARE_DST)));
        }

        @Override
        public GoalKey<Mob> getKey() {return key;}

        @Override
        public EnumSet<GoalType> getTypes() {return types;}
    }

    public static class Infected extends SednaMob {

        public String biome() {
            return "dry_ocean";
        }

        @Override
        protected Class<? extends LivingEntity> getEntClass() {
            return ZombieVillager.class;
        }

        @Override
        public Map<EquipmentSlot, ItemStack> equipment() {
            return Map.of(EquipmentSlot.HAND, ClassUtil.rndElmt(
                    new ItemBuilder(ItemType.WOODEN_HOE).build(),
                    new ItemBuilder(ItemType.WOODEN_SHOVEL).build(),
                    new ItemBuilder(ItemType.WOODEN_PICKAXE).build(),
                    ItemUtil.air),

                EquipmentSlot.HEAD, ClassUtil.rndElmt(
                    new ItemBuilder(ItemType.LEATHER_HELMET).color(Color.GREEN).build(),
                    new ItemBuilder(ItemType.SLIME_BLOCK).build(),
                    new ItemBuilder(ItemType.AZALEA).build(),
                    new ItemBuilder(ItemType.GREEN_STAINED_GLASS).build(),
                    new ItemBuilder(ItemType.CACTUS).build(), ItemUtil.air),
                EquipmentSlot.CHEST, ClassUtil.rndElmt(
                    new ItemBuilder(ItemType.LEATHER_CHESTPLATE).color(Color.GRAY).build(), ItemUtil.air),
                EquipmentSlot.LEGS, ClassUtil.rndElmt(
                    new ItemBuilder(ItemType.LEATHER_LEGGINGS).color(Color.GREEN).build(), ItemUtil.air),
                EquipmentSlot.FEET, ClassUtil.rndElmt(
                    new ItemBuilder(ItemType.LEATHER_BOOTS).color(Color.GRAY).build(), ItemUtil.air));
        }

        private final RollTree drop = RollTree.of(key().value())
            .add(new ItemRoll(key().value() + "_flesh", new ItemBuilder(ItemType.ROTTEN_FLESH).build(), 1, 1), 2)
            .add(RollTree.of(key().value() + "_extra")
                .add(new ItemRoll(key().value() + "_paper", new ItemBuilder(ItemType.PAPER).build(), 1, 1), 2)
                .add(new ItemRoll(key().value() + "_hide", new ItemBuilder(ItemType.RABBIT_HIDE).build(), 1, 0), 2)
                .add(new ItemRoll(key().value() + "_seeds", new ItemBuilder(ItemType.WHEAT_SEEDS).build(), 1, 0), 1)
                .add(new ItemRoll(key().value() + "_kelp", new ItemBuilder(ItemType.DRIED_KELP).build(), 1, 2), 2)
                .add(new ItemRoll(key().value() + "_coal", new ItemBuilder(ItemType.CHARCOAL).build(), 1, 0), 2)
                .add(new ItemRoll(key().value() + "_flint", new ItemBuilder(ItemType.FLINT).build(), 1, 0), 1)
                .build(1, 0), 1)
            .add(new NARoll(), 2).build(1, 0);

        @Override
        public RollTree loot() {
            return drop;
        }
    }
}
