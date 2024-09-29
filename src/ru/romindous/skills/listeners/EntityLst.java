package ru.romindous.skills.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Spellcaster.Spell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.potion.PotionEffectType;
import ru.romindous.skills.Main;
import ru.romindous.skills.enums.SubServer;


public class EntityLst implements Listener {
	
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSlimeSplit(final SlimeSplitEvent e) {
        e.setCancelled(true);
    }
	
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSpell(final EntitySpellCastEvent e) {
        e.setCancelled(e.getSpell() == Spell.BLINDNESS);
    }

    @EventHandler
    public void onTgt(final EntityTargetLivingEntityEvent e) {
        final LivingEntity tgt = e.getTarget();
        if (tgt == null) {
            return;
        }

        if (e.getEntity() instanceof final Mob mb) {
            if (mb.hasPotionEffect(PotionEffectType.BLINDNESS)
                || mb.hasPotionEffect(PotionEffectType.NAUSEA)) {
                e.setCancelled(true);
            }
        }
    }
    
    
    @EventHandler
    public void onExpld(final EntityExplodeEvent e) {
    	final Entity ent = e.getEntity();
        if (ent instanceof Mob) {
            //return;
        } else {
            switch (e.getEntityType()) {
            	case FIREBALL, SMALL_FIREBALL, TNT, WITHER_SKULL:
                    if (Main.subServer != SubServer.INFERNAL) {
                        e.blockList().clear();
                    }
                default:
                	break;
            }
		}
    }

    /*case SLIME:
        final Slime cb = (Slime) ent;
        if (!SM.cublocks.containsKey(cb.getEntityId()) && cb.isInvisible()) {
            final Material mt = cb.getLocation().getBlock().getType();
            for (final CuBType fb : CuBType.values()) {
                if (fb.mat == mt) {
                    Bukkit.getConsoleSender().sendMessage("Found " + fb.toString() + " at " + new XYZ(cb.getLocation()).toString());
                    SM.cublocks.put(cb.getEntityId(), CuBlock.createOf(cb, fb, true));
                    break;
                }
            }
        }
        break;*/
    
    /*@EventHandler
    public void onTrade(final PlayerTradeEvent e) {
    	QM.tryCompleteQuest(e.getPlayer(), Quest.VillagerTrade, 1, true);
    }*/
    
    @EventHandler
    public void onTransform(final EntityTransformEvent e) {
        switch (e.getTransformReason()) {
            case INFECTION:
            case CURED:
                e.setCancelled(Main.subServer != SubServer.TERRA);
                break;
            case DROWNED:
                e.setCancelled(Main.subServer != SubServer.AQUAROD);
                break;
            case FROZEN:
                e.setCancelled(Main.subServer != SubServer.KRIOLITE);
                break;
            case LIGHTNING:
            case PIGLIN_ZOMBIFIED:
                e.setCancelled(Main.subServer != SubServer.INFERNAL);
                break;
            case METAMORPHOSIS:
                e.setCancelled(Main.subServer != SubServer.LOCUS);
                break;
            case SHEARED:
            case SPLIT:
            case UNKNOWN:
                break;
        }
    }
    
    
    
    
}
