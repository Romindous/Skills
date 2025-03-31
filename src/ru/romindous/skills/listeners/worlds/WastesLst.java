package ru.romindous.skills.listeners.worlds;

import java.util.Iterator;
import java.util.Set;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import ru.komiss77.boot.OStrap;
import ru.romindous.skills.Main;
import ru.romindous.skills.SubServer;
import ru.romindous.skills.listeners.RehabLst;


public class WastesLst implements Listener  {
    
    //жестко багануло Skills.jar//ru.romindous.skills.listener.WorldLst.wastesWaterControl(WorldLst.java:22)
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPhys(final BlockPhysicsEvent e) {
        if (BlockType.WATER.equals(e.getBlock().getType().asBlockType()) && Main.subServer == SubServer.WASTES) {
            int i = 0;
            for (final BlockFace bf : RehabLst.near) {
                final Block bl = e.getBlock().getRelative(bf);
                if ((BlockType.WATER.equals(bl.getType().asBlockType()) && ((Levelled) bl.getBlockData()).getLevel() == 0) ||
                	(bl.getBlockData() instanceof Waterlogged && ((Waterlogged) bl.getBlockData()).isWaterlogged())) {
                    i++;
                }
            }
            if (i > 1) {
                final Levelled nw = BlockType.WATER.createBlockData();
                nw.setLevel(1);
                e.getBlock().setBlockData(nw);
            }
        }
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTrans(final BlockFromToEvent e) {
        e.setCancelled(Main.subServer == SubServer.WASTES && e.getToBlock().getBlockData() instanceof Waterlogged);
    }

    private static final Set<BlockType> CROPS = OStrap.getAll(BlockTypeTagKeys.CROPS);
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInter(final BlockFertilizeEvent e) {
        for (final Iterator<BlockState> it = e.getBlocks().iterator(); it.hasNext();) {
            if (CROPS.contains(it.next().getType().asBlockType())) continue;
            it.remove();
            return;
        }
    }

    /*@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUnload(final ChunkUnloadEvent e) {
        final ChunkContent cc = Land.getChunkContent(e.getChunk());
    	Land.removeIfEmpty(cc);
    }*/

    /*@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChunkLoad(final ChunkLoadEvent e) {
    	Bukkit.getConsoleSender().sendMessage("scan-" + e.getChunk().getX() + "," + e.getChunk().getZ());
    	for (final Entity ent : e.getChunk().getEntities()) {
    		if (ent instanceof ItemFrame) {
    			final ItemFrame fm = (ItemFrame) ent;
        		if (!SM.frameBlocks.containsKey(fm.getUniqueId())) {
        			final Material mt = fm.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        			for (final FrameBlock fb : FrameBlock.values()) {
        				if (fb.mat == mt) {
        					SM.frameBlocks.put(fm.getUniqueId(), fb);
                			Bukkit.getConsoleSender().sendMessage("Found a " + fb.toString() + " on coords " + new XYZ(fm.getLocation()).toString());
                			break;
        				}
        			}
        		}
    		}
    	}
    }*/
}
