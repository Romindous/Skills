package ru.romindous.skills.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import ru.romindous.skills.Main;
import ru.romindous.skills.enums.SubServer;


public class WorldLst implements Listener  {
    
    //жестко багануло Skills.jar//ru.romindous.skills.listener.WorldLst.wastesWaterControl(WorldLst.java:22)
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void wastesWaterControl(final BlockPhysicsEvent e) {
        if (e.getBlock().getType() == Material.WATER && Main.subServer == SubServer.WASTES) {
            int i = 0;
            for (final BlockFace bf : RehabLst.near) {
                final Block bl = e.getBlock().getRelative(bf);
                if ((bl.getType() == Material.WATER && ((Levelled) bl.getBlockData()).getLevel() == 0) || 
                	(bl.getBlockData() instanceof Waterlogged && ((Waterlogged) bl.getBlockData()).isWaterlogged())) {
                    i++;
                }
            }
            if (i > 1) {
                final Levelled nw = (Levelled) Material.WATER.createBlockData();
                nw.setLevel(1);
                e.getBlock().setBlockData(nw);
            }
        }
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void wastesCancelWaterlogged(final BlockFromToEvent e) {
        e.setCancelled(Main.subServer == SubServer.WASTES && e.getToBlock().getBlockData() instanceof Waterlogged);
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
