package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.NARoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.utils.BlockUtil;
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

    public static final ItemStack MOSS = ItemType.GLOW_LICHEN.createItemStack();
    public static final BlockData MOSS_DATA = BlockType.GLOW_LICHEN.createBlockData(gl -> {
        for (final BlockFace bf : gl.getFaces()) gl.setFace(bf, false);
        gl.setFace(BlockFace.DOWN, true);
    });
    public static final Set<BlockType> DIRT = Set.of(BlockType.DIRT, BlockType.PODZOL,
        BlockType.GRASS_BLOCK, BlockType.ROOTED_DIRT, BlockType.COARSE_DIRT, BlockType.MYCELIUM);
    public static final int SPORE_TICKS = 80;

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        super.onDeath(e);
        if (e.getEntity() instanceof final Mob mb) {
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
                        EntityUtil.effect(spawn(loc), Sound.BLOCK_BIG_DRIPLEAF_BREAK,
                            0.6f, Particle.HAPPY_VILLAGER);
                    }
                }, SPORE_TICKS);
            }
        }
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
