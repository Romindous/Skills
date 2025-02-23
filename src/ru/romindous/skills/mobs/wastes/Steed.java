package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import com.destroystokyo.paper.entity.Pathfinder;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.NARoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.world.AreaSpawner;
import ru.komiss77.modules.world.BVec;
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
            protected boolean extra(final BVec loc) {
                final World w = loc.w(); if (w == null) return false;
                return limit(loc) && loc.block(w).getLightFromSky() > MIN_LIGHT;
            }
        } : spawn;
    }

    @Override
    public @Nullable MobGoal goal(final Mob mb) {
        return new MobGoal() {

            private static final int RIDER_DST = 16;
            private static final double SADDLE_DST_SQ = 16d;

            private int tick = Main.srnd.nextInt(8);

            @Override
            public void tick() {
                if (!mb.isValid()) return;
                if ((tick++ & 7) != 0) return;
                final Location loc = mb.getEyeLocation();

                if (!mb.getPassengers().isEmpty()) {
                    if (mb.getPassengers().getFirst() instanceof final Mob rm) {
                        mb.setTarget(rm.getTarget());
                        mb.setAggressive(true);
                    }
                    return;
                }

                final LivingEntity rd = LocUtil.getClsChEnt(BVec.of(loc),
                    RIDER_DST, RIDER.getEntClass(), e -> !e.isInsideVehicle());
                if (rd instanceof final Mob rm) {
                    final Location mlc = rm.getEyeLocation();
                    if (!LocUtil.trace(loc, mlc.toVector().subtract(loc.toVector()),
                        (bp, bd) -> bd.getMaterial().asBlockType().hasCollision()).endDst()) return;
                    if (mlc.distanceSquared(loc) < SADDLE_DST_SQ) {
                        mb.addPassenger(rm);
                        mb.setTarget(rm.getTarget());
                        mb.setAggressive(true);
                        EntityUtil.effect(mb, Sound.ITEM_ARMOR_EQUIP_LEATHER,
                            0.8f, Particle.ANGRY_VILLAGER);
                        return;
                    }

                    final Pathfinder pf = mb.getPathfinder();
                    pf.moveTo(rd.getLocation());
                }
            }
        };
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
                    new ItemBuilder(ItemType.CACTUS).build()),
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
                .add(new ItemRoll(key().value() + "_feather", new ItemBuilder(ItemType.FEATHER).build(), 1, 1), 20)
                .add(new ItemRoll(key().value() + "_paper", new ItemBuilder(ItemType.PAPER).build(), 1, 0), 20)
                .add(new ItemRoll(key().value() + "_hide", new ItemBuilder(ItemType.RABBIT_HIDE).build(), 1, 0), 20)
                .add(new ItemRoll(key().value() + "_seeds", new ItemBuilder(ItemType.WHEAT_SEEDS).build(), 1, 0), 10)
                .add(new ItemRoll(key().value() + "_kelp", new ItemBuilder(ItemType.DRIED_KELP).build(), 1, 2), 20)
                .add(new ItemRoll(key().value() + "_coal", new ItemBuilder(ItemType.CHARCOAL).build(), 1, 1), 20)
                .add(new ItemRoll(key().value() + "_flint", new ItemBuilder(ItemType.FLINT).build(), 1, 0), 10)
                .add(new ItemRoll(key().value() + "_sac", new ItemBuilder(ItemType.INK_SAC).build(), 1, 0), 10)
                .add(new ItemRoll(key().value() + "_potato", new ItemBuilder(ItemType.POTATO).build(), 1, 0), 8)
                .add(new ItemRoll(key().value() + "_potato", new ItemBuilder(ItemType.POISONOUS_POTATO).build(), 1, 0), 10)
                .build(1, 0), 1)
            .add(new NARoll(), 1).build(1, 0);

        @Override
        public RollTree loot() {
            return drop;
        }
    }
}
