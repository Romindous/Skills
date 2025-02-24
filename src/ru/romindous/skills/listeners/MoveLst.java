package ru.romindous.skills.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.Input;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerInputEvent;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.BlockUtil;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.skills.trigs.Trigger;


public class MoveLst implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortal(final EntityPortalEnterEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            final Player p = (Player) e.getEntity();
            final Block b = e.getEntity().getLocation().getBlock();
            if (BlockType.NETHER_PORTAL.equals(b.getType().asBlockType())) {
                b.setBlockData(BlockUtil.air);
            } else {
                for (final BlockFace bf : RehabLst.near) {
                    if (BlockType.NETHER_PORTAL.equals(b.getRelative(bf).getType().asBlockType())) {
                        b.getRelative(bf).setBlockData(BlockUtil.air);
                        break;
                    }
                }
            }
            Ostrov.sync(() -> p.performCommand("skill world"), 4);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJump(final PlayerJumpEvent e) {
        final Player p = e.getPlayer();
        if (!p.isSneaking()) return;
        final Survivor sv = PM.getOplayer(p, Survivor.class);
        if (sv == null) return;
        sv.trigger(Trigger.SHIFT_JUMP, e, p);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInput(final PlayerInputEvent e) {
        final Player p = e.getPlayer();
        if (p.isInsideVehicle()) return;
        final Survivor sv = PM.getOplayer(p, Survivor.class);
        if (sv == null) return;
        final Input in = e.getInput();
        if (!in.isJump()) {
            if (p.getVelocity().getY() < 0) return;
            sv.jump = in;
            return;
        }
        if (sv.jump == null) return;
        sv.jump = null;
        if (p.getVelocity().getY() < 0) return;
        sv.trigger(Trigger.DOUBLE_JUMP, e, p);
    }

}
