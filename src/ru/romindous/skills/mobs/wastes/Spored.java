package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import java.util.Set;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.NARoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.BlockUtil;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.mobs.SednaMob;

public class Spored extends SednaMob {

    public String biome() {
        return "dry_ocean";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return Creeper.class;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    private static final ItemStack MOSS = ItemType.GLOW_LICHEN.createItemStack();
    private static final BlockData MOSS_DATA = BlockType.GLOW_LICHEN.createBlockData(gl -> {
        for (final BlockFace bf : gl.getFaces()) gl.setFace(bf, false);
        gl.setFace(BlockFace.DOWN, true);
    });
    private static final Set<BlockType> DIRT = Set.of(BlockType.DIRT, BlockType.PODZOL,
        BlockType.GRASS_BLOCK, BlockType.ROOTED_DIRT, BlockType.COARSE_DIRT, BlockType.MYCELIUM);
    private static final int SPORE_TICKS = 80;
    private static final double MINI_SCALE = 0.6d;

    public void onExplode(final EntityExplodeEvent e) {
        if (!(e.getEntity() instanceof final Mob mb)) return;
        final double dmg = mb.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue();
        final AttributeInstance sca = mb.getAttribute(Attribute.SCALE);
        final double scl = sca == null ? 1d : sca.getValue();
        e.setYield((float) (dmg * scl));
        if (scl < 1d) {e.blockList().clear(); return;}
        new ParticleBuilder(Particle.COMPOSTER).location(mb.getLocation())
            .count((int) (dmg * 20d)).offset(dmg, dmg, dmg).receivers(40).spawn();
        final World w = e.getEntity().getWorld();
        final BVec[] sbls = ClassUtil.shuffle(e.blockList().stream().map(BVec::of).toArray(BVec[]::new));
        for (int i = sbls.length >> 2; i != 0; i--) {
            final BVec bl = sbls[i];
            final BVec above = BVec.of(bl.x, bl.y + 1, bl.z);
            if (!DIRT.contains(Nms.fastType(w, bl))
                || !Nms.fastType(w, above).isAir()) {
                sbls[i] = null;
                continue;
            }
            sbls[i] = above;
            final Block b = above.block(w);
            b.setBlockData(MOSS_DATA, false);
        }
        e.blockList().clear();
        Ostrov.sync(() -> {
            for (int i = sbls.length >> 2; i != 0; i--) {
                final BVec bl = sbls[i];
                if (bl == null) continue;
                final Block b = bl.block(w);
                if (b.getType() != MOSS.getType()) continue;
                b.setBlockData(BlockUtil.air, false);
                EntityUtil.effect(spawn(bl.center(w), MINI_SCALE),
                    Sound.BLOCK_BIG_DRIPLEAF_BREAK, 0.6f, Particle.HAPPY_VILLAGER);
            }
        }, SPORE_TICKS);
    }

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        super.onDeath(e);
        if (e.getEntity() instanceof final Mob mb) {
            final AttributeInstance scl = mb.getAttribute(Attribute.SCALE);
            if (scl == null || scl.getValue() < 1d) return;
            final Location loc = mb.getLocation();
            final Block b = loc.getBlock();
            if (b.getType().isAir() && DIRT.contains(Nms.fastType(loc.getWorld(),
                loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()))) {
                b.setBlockData(MOSS_DATA, false);
                EntityUtil.effect(mb, Sound.BLOCK_SMALL_DRIPLEAF_BREAK,
                    0.6f, Particle.COMPOSTER);
                Ostrov.sync(() -> {
                    final Block b2 = loc.getBlock();
                    if (b2.getType() == MOSS.getType()) {
                        b2.setBlockData(BlockUtil.air, false);
                        EntityUtil.effect(spawn(loc, MINI_SCALE), Sound.BLOCK_BIG_DRIPLEAF_BREAK,
                            0.6f, Particle.HAPPY_VILLAGER);
                    }
                }, SPORE_TICKS);
            }
        }
    }

    public LivingEntity spawn(final Location loc, final double size) {
        final LivingEntity le = spawn(loc);
        if (size == 1d) return le;
        final AttributeInstance scl = le.getAttribute(Attribute.SCALE);
        if (scl == null) return le;
        scl.setBaseValue(size);
        return le;
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_powder", ItemType.GUNPOWDER.createItemStack(), 1, 1), 2)
        .add(new ItemRoll(key().value() + "_seeds", ItemType.WHEAT_SEEDS.createItemStack(), 0, 1), 1)
        .add(new NARoll(), 1).build(1, 0);

    @Override
    public RollTree loot() {
        return drop;
    }
}
