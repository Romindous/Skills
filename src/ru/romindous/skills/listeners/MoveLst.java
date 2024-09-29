package ru.romindous.skills.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.PM;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.enums.Trigger;


public class MoveLst implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortal(final EntityPortalEnterEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            final Player p = (Player) e.getEntity();
            final Block b = e.getEntity().getLocation().getBlock();
            if (b.getType() == Material.NETHER_PORTAL) {
                b.setType(Material.AIR);
            } else {
                for (final BlockFace bf : RehabLst.near) {
                    if (b.getRelative(bf).getType() == Material.NETHER_PORTAL) {
                        b.getRelative(bf).setType(Material.AIR);
                        break;
                    }
                }
            }
            Ostrov.sync(() -> p.performCommand("skill world"), 4);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFly(final PlayerToggleFlightEvent e) {
        final Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE) return;
        e.setCancelled(true);
        p.setFlying(false);
        p.setAllowFlight(false);
        final Survivor sv = PM.getOplayer(p, Survivor.class);
        if (sv == null) return;
        sv.trigger(Trigger.DOUBLE_JUMP, e, p);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJump(final PlayerJumpEvent e) {
        final Player p = e.getPlayer();
        final Survivor sv = PM.getOplayer(p, Survivor.class);
        if (sv == null) return;
        p.setAllowFlight(true);
        if (p.isSneaking()) {
            sv.trigger(Trigger.SHIFT_JUMP, e, p);
        }
//        p.setFallDistance(SM.DJ_FALL_DST);
//        p.setFlyingFallDamage(TriState.TRUE);
    }


}
