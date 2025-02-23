package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.NARoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.romindous.skills.objects.Groups;
import ru.romindous.skills.mobs.SednaMob;

public class Clutcher extends SednaMob {

    private final int CLUTCH_CD = value("clutch_cd", 40);
    private final double JUMP_DST_SQ = value("jump_dst_sq", 8d);
    private final double CLUTCH_DST_SQ = value("clutch_dst_sq", 2.8d);

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
    public MobGoal goal(final Mob mb) {
        return new MobGoal() {

            private int tick = 0;

            @Override
            public void tick() {
                if (!mb.isValid()) return;

                final LivingEntity tgt = mb.getTarget();
                if (tgt == null || !tgt.isValid()) {
                    if (!mb.hasGravity()) mb.setGravity(true);
                    return;
                }
                final Location tlc = tgt.getEyeLocation();
                final Location mlc = mb.getEyeLocation();
                final double dst = tlc.distanceSquared(mlc);
                if (dst < CLUTCH_DST_SQ) {
                    if (mb.hasGravity()) {
                        if (mb.getPortalCooldown() < 1)
                            mb.setGravity(false);
                        return;
                    }
                    mb.teleport(tgt.getLocation().add(0d, tgt.getHeight() * 0.1d * (mb.getEntityId() & 7), 0d));
                    return;
                }

                if (!mb.hasGravity()) mb.setGravity(true);
                if ((tick++ & 1) == 0 && dst < JUMP_DST_SQ && mb.isOnGround()) {
                    mb.setVelocity(tlc.subtract(mlc).toVector().normalize().multiply(0.6d));
                }
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
                b.setBlockData(BlockType.INFESTED_STONE.createBlockData(), false);
                yield true;
            }
            case DEEPSLATE -> {
                b.setBlockData(BlockType.INFESTED_DEEPSLATE.createBlockData(), false);
                yield true;
            }
            case STONE_BRICKS -> {
                b.setBlockData(BlockType.INFESTED_STONE_BRICKS.createBlockData(), false);
                yield true;
            }
            case COBBLESTONE -> {
                b.setBlockData(BlockType.INFESTED_COBBLESTONE.createBlockData(), false);
                yield true;
            }
            case CHISELED_STONE_BRICKS -> {
                b.setBlockData(BlockType.INFESTED_CHISELED_STONE_BRICKS.createBlockData(), false);
                yield true;
            }
            case CRACKED_STONE_BRICKS -> {
                b.setBlockData(BlockType.INFESTED_CRACKED_STONE_BRICKS.createBlockData(), false);
                yield true;
            }
            case MOSSY_STONE_BRICKS -> {
                b.setBlockData(BlockType.INFESTED_MOSSY_STONE_BRICKS.createBlockData(), false);
                yield true;
            }
            default -> false;
        };

        if (stn) {
            new ParticleBuilder(Particle.BLOCK).data(b.getBlockData()).location(loc).count(20)
                .offset(0.4d, 0.4d, 0.4d).extra(0.1d).allPlayers().spawn();
            new ParticleBuilder(Particle.CLOUD).location(loc).count(16).extra(0d)
                .offset(0.2d, 0.4d, 0.2d).allPlayers().spawn();
            mb.remove();
        }
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_nugget", Groups.SILVER.item(ItemType.IRON_NUGGET), 1, 0), 1)
        .add(new ItemRoll(key().value() + "_meal", new ItemBuilder(ItemType.BONE_MEAL).build(), 1, 0), 2)
        .add(new NARoll(), 4).build(1, 0);

    @Override
    public RollTree loot() {
        return drop;
    }
}
