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
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.NARoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.world.AreaSpawner;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.*;
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
            protected boolean extra(final BVec loc) {
                final World w = loc.w(); if (w == null) return false;
                return limit(loc) && loc.block(w).getLightFromSky() > MIN_LIGHT;
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
        private static final int RIDER_DST = 16;
        private static final double SADDLE_DST_SQ = 16d;

        private final Mob mob;

        private int tick = Main.srnd.nextInt(8);

        private RideGoal(final Mob mob) {this.mob = mob;}

        @Override
        public boolean shouldActivate() {return true;}

        @Override
        public void tick() {
            if (!mob.isValid()) return;
            if ((tick++ & 7) != 0) return;
            final Location loc = mob.getEyeLocation();

            if (!mob.getPassengers().isEmpty()) {
                if (mob.getPassengers().getFirst() instanceof final Mob mb) {
                    mob.setTarget(mb.getTarget());
                    mob.setAggressive(true);
                }
                return;
            }

            final LivingEntity rd = LocUtil.getClsChEnt(new WXYZ(loc),
                RIDER_DST, RIDER.getEntClass(), e -> !e.isInsideVehicle());
            if (rd instanceof final Mob mb) {
                final Location mlc = mb.getEyeLocation();
                if (!LocUtil.trace(loc, mlc.toVector().subtract(loc.toVector()),
                    (bp, bd) -> bd.getMaterial().asBlockType().hasCollision()).endDst()) return;
                if (mlc.distanceSquared(loc) < SADDLE_DST_SQ) {
                    mob.addPassenger(mb);
                    mob.setTarget(mb.getTarget());
                    mob.setAggressive(true);
                    EntityUtil.effect(mob, Sound.ITEM_ARMOR_EQUIP_LEATHER,
                        0.8f, Particle.ANGRY_VILLAGER);
                    return;
                }

                final Pathfinder pf = mob.getPathfinder();
                pf.moveTo(rd.getLocation());
            }

            /*final Pathfinder pf = mob.getPathfinder();
            final Player pl = LocUtil.getClsChEnt(loc, SCARE_DST,
                Player.class, p -> p.getGameMode() == GameMode.SURVIVAL);
            if (pl != null) {
                pf.moveTo(loc.add(loc.toVector().subtract(pl.getLocation()
                    .toVector()).normalize().multiply(SCARE_DST)));
                return;
            }
            if (Main.srnd.nextInt(WALK_CH) == 0)
                pf.moveTo(loc.add(NumUtil.rndSignNum(2, 8),
                    0d, NumUtil.rndSignNum(2, 8)));*/
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
                .add(new ItemRoll(key().value() + "_feather", new ItemBuilder(ItemType.FEATHER).build(), 1, 1), 2)
                .add(new ItemRoll(key().value() + "_paper", new ItemBuilder(ItemType.PAPER).build(), 1, 0), 2)
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
