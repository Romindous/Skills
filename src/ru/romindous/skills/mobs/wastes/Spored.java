package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ClassUtil;
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
    private static final Set<BlockType> DIRT = Set.of(BlockType.DIRT, BlockType.PODZOL,
        BlockType.GRASS_BLOCK, BlockType.ROOTED_DIRT, BlockType.COARSE_DIRT, BlockType.MYCELIUM);

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        super.onDeath(e);
        if (e.getEntity() instanceof final Mob mb) {
            final Location loc = mb.getLocation();
            final Block b = loc.getBlock();
            if (b.getType().isAir() && DIRT.contains(Nms.fastType(loc.getWorld(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))) {
                b.setType(MOSS.getType(), false);
                Ostrov.sync(() -> {
                    final Block b2 = loc.getBlock();
                    if (b2.getType() == MOSS.getType()) {
                        b2.setType(Material.AIR, false);
                        new ParticleBuilder(Particle.HAPPY_VILLAGER).location(loc)
                            .count(40).extra(0.1d).offset(0.4d, 0.4d, 0.4d).allPlayers().spawn();
                        spawn(loc);
                    }
                }, 200);
            }
        }
    }

    @Override
    protected void onExtra(final EntityEvent e) {
        super.onExtra(e);
        if (e instanceof final EntityExplodeEvent ee) {
            if (ee.isCancelled()) return;
            final Set<WXYZ> bls = ee.blockList().stream().map(WXYZ::new).collect(Collectors.toSet());
            final WXYZ[] sbls = ClassUtil.shuffle(bls.toArray(new WXYZ[0]));
            for (int i = sbls.length >> 1; i != 0; i--) {
                final WXYZ bl = sbls[i];
                final WXYZ below = new WXYZ(bl.w, bl.x, bl.y - 1, bl.z);
                if (bls.contains(below)) continue;
                final Block b = bl.getBlock();
                if (b.getType().isAir() && DIRT.contains(Nms.fastType(below))) {
                    b.setType(MOSS.getType(), false);
                    Ostrov.sync(() -> {
                        final Block b2 = bl.getBlock();
                        if (b2.getType() == MOSS.getType()) {
                            b2.setType(Material.AIR, false);
                            new ParticleBuilder(Particle.HAPPY_VILLAGER).location(bl.getCenterLoc())
                                .count(40).offset(0.4d, 0.4d, 0.4d).allPlayers().spawn();
                            spawn(bl.getCenterLoc());
                        }
                    }, 200);
                }
            }
        }
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_powder", new ItemStack(Material.STRING), 2, 1, 2), 2)
        .add(new ItemRoll(key().value() + "_seeds", new ItemStack(Material.WHEAT_SEEDS), 2, 1, 2), 1)
        .build(1, 1);

    @Override
    public RollTree loot() {
        return drop;
    }
}
