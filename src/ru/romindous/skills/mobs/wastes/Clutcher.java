package ru.romindous.skills.mobs.wastes;

import java.util.EnumSet;
import java.util.Map;
import com.destroystokyo.paper.ParticleBuilder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.utils.ItemBuilder;
import ru.romindous.skills.mobs.SednaMob;
import ru.romindous.skills.objects.SkillMats;

public class Clutcher extends SednaMob {

    private static final int CLUTCH_CD = 32;

    public String biome() {
        return "iron_hills";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return Silverfish.class;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    @Override
    public Goal<Mob> goal(final Mob mb) {
        return new Goal<>() {

            private static final double JUMP_DST_SQ = 8d;
            private static final double CLUTCH_DST_SQ = 2d;

            private final GoalKey<Mob> key = GoalKey.of(Mob.class, Clutcher.this.getKey());
            private static final EnumSet<GoalType> types = EnumSet.noneOf(GoalType.class);

            private int tick = 0;

            @Override
            public boolean shouldActivate() {
                return true;
            }

            @Override
            public void tick() {
                if (!mb.isValid()) return;

                final LivingEntity tgt = mb.getTarget();
                if (tgt == null || !tgt.isValid()) return;
                final Location tlc = tgt.getEyeLocation();
                final Location mlc = mb.getEyeLocation();
                final double dst = tlc.distanceSquared(mlc);
                if (dst < CLUTCH_DST_SQ) {
                    if (mb.hasGravity()) {
                        if (mb.getPortalCooldown() < 1)
                            mb.setGravity(false);
                        return;
                    }
                    mb.teleportAsync(tgt.getLocation().add(0d, tgt.getHeight() * 0.1d * (tgt.getEntityId() & 7), 0d));
//                    if ((tick++ & 7) != 0) return;
//                    mb.attack(tgt);
                    return;
                }

                if (!mb.hasGravity()) mb.setGravity(true);
                if ((tick++ & 15) == 0 && dst < JUMP_DST_SQ && mb.isOnGround()) {
                    mb.setVelocity(tlc.subtract(mlc).toVector().normalize().multiply(0.4d));
                }
            }

            @Override
            public @NotNull GoalKey<Mob> getKey() {
                return key;
            }

            @Override
            public @NotNull EnumSet<GoalType> getTypes() {
                return types;
            }
        };
    }

    @Override
    protected void onHurt(final EntityDamageEvent e) {
        super.onHurt(e);
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof final Mob mb)) return;
        if (!mb.hasGravity()) {
            mb.setPortalCooldown(CLUTCH_CD);
            mb.setGravity(true);
            return;
        }

        final Location loc = mb.getLocation();
        final Block b = loc.add(0d, -1d, 0d).getBlock();
        final boolean stn = switch (b.getType()) {
            case STONE -> {
                b.setType(Material.INFESTED_STONE, false);
                yield true;
            }
            case DEEPSLATE -> {
                b.setType(Material.INFESTED_DEEPSLATE, false);
                yield true;
            }
            case STONE_BRICKS -> {
                b.setType(Material.INFESTED_STONE_BRICKS, false);
                yield true;
            }
            case COBBLESTONE -> {
                b.setType(Material.INFESTED_COBBLESTONE, false);
                yield true;
            }
            case CHISELED_STONE_BRICKS -> {
                b.setType(Material.INFESTED_CHISELED_STONE_BRICKS, false);
                yield true;
            }
            case CRACKED_STONE_BRICKS -> {
                b.setType(Material.INFESTED_CRACKED_STONE_BRICKS, false);
                yield true;
            }
            case MOSSY_STONE_BRICKS -> {
                b.setType(Material.INFESTED_MOSSY_STONE_BRICKS, false);
                yield true;
            }
            default -> false;
        };

        if (stn) {
            new ParticleBuilder(Particle.BLOCK).data(b.getBlockData()).location(loc).count(20)
                .offset(0.2d, 0.4d, 0.2d).extra(0.1d).allPlayers().spawn();
            new ParticleBuilder(Particle.CLOUD).location(loc).count(16).extra(0.1d)
                .offset(0.2d, 0.4d, 0.2d).allPlayers().spawn();
            mb.remove();
        }
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_scales", SkillMats.SILVER.item(ItemType.PHANTOM_MEMBRANE), 2, 1), 1)
        .add(new ItemRoll(key().value() + "_meal", new ItemBuilder(ItemType.BONE_MEAL).build(), 2, 1), 4)
        .build(1, 1);

    @Override
    public RollTree loot() {
        return drop;
    }
}
