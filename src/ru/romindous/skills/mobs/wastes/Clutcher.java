package ru.romindous.skills.mobs.wastes;

import com.destroystokyo.paper.ParticleBuilder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.items.ItemRoll;
import ru.romindous.skills.objects.SkillMats;
import ru.romindous.skills.mobs.SednaMob;

import java.util.EnumSet;
import java.util.Map;

public class Clutcher extends SednaMob {

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

            private final GoalKey<Mob> key = GoalKey.of(Mob.class, Clutcher.this.getKey());
            private final EnumSet<GoalType> types = EnumSet.of(GoalType.MOVE, GoalType.LOOK);

            private int tick = 0;

            @Override
            public boolean shouldActivate() {
                return true;
            }

            @Override
            public void tick() {
                if (!mb.isValid()) return;

                if ((tick++ & 7) != 0) return;
                final LivingEntity tgt = mb.getTarget();
                if (tgt == null || !tgt.isValid()) return;
                final Entity vc = mb.getVehicle();
                if (vc == null) {
                    final Location tlc = tgt.getEyeLocation();
                    final Location mlc = mb.getEyeLocation();
                    if (tlc.distanceSquared(tlc) < JUMP_DST_SQ)
                        mb.setVelocity(tlc.subtract(mlc).toVector());
                } else if (vc.getEntityId() == tgt.getEntityId()) {
                    mb.attack(tgt);
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
    protected void onAttack(final EntityDamageByEntityEvent e) {
        super.onAttack(e);
        if (e.isCancelled()) return;
        if (!(e.getDamageSource().getCausingEntity() instanceof final Mob mb)) return;
        final Entity ent = e.getEntity();
        if (mb.isInsideVehicle() || !ent.getPassengers().isEmpty()) return;
        ent.addPassenger(mb);
    }

    private static final float THORN = 0.25f;

    @Override
    protected void onHurt(final EntityDamageEvent e) {
        super.onHurt(e);
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof final Mob mb)) return;
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
            return;
        }

        if (e.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr && Ostrov.random.nextFloat() < THORN) {
            dmgr.damage(mb.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue() * THORN,
                DamageSource.builder(DamageType.THORNS).withCausingEntity(mb).withDirectEntity(mb).build());
        }
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_scales", SkillMats.SILVER.getItem(Material.PHANTOM_MEMBRANE), 2, 1), 1)
        .add(new ItemRoll(key().value() + "_meal", new ItemStack(Material.BONE_MEAL), 2, 1), 4)
        .build(1, 1);

    @Override
    public RollTree loot() {
        return drop;
    }
}
