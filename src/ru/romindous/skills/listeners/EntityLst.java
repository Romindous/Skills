package ru.romindous.skills.listeners;

import java.util.List;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Spellcaster.Spell;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.modules.entities.CustomEntity;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.world.BVec;
import ru.romindous.skills.Main;
import ru.romindous.skills.SubServer;
import ru.romindous.skills.mobs.wastes.Crawler;
import ru.romindous.skills.mobs.wastes.Spored;


public class EntityLst implements Listener {
	
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSlimeSplit(final SlimeSplitEvent e) {
        e.setCancelled(true);
    }
	
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSpell(final EntitySpellCastEvent e) {
        e.setCancelled(e.getSpell() == Spell.BLINDNESS);
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTgt(final EntityTargetLivingEntityEvent e) {
        if (e.getEntity() instanceof final Mob mb) {
            if (mb.hasPotionEffect(PotionEffectType.BLINDNESS)
                || mb.hasPotionEffect(PotionEffectType.NAUSEA)) {
                e.setCancelled(true);
                return;
            }
        }

        final LivingEntity tgt = e.getTarget();
        if (tgt == null) return;

        final PotionEffect ipe = tgt.getPotionEffect(PotionEffectType.INVISIBILITY);
        if (ipe != null) {
            if (Main.srnd.nextInt(BVec.of(tgt.getLocation())
                .dist(e.getEntity().getLocation()) + ipe.getAmplifier()) != 0) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    protected void onHit(final ProjectileHitEvent e) {
        final Projectile prj = e.getEntity();
        if (!prj.isValid() || !(prj.getShooter() instanceof final Mob shtr)
            || !(CustomEntity.get(shtr) instanceof final Crawler cr)) return;
        cr.onHit(e);
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    protected void onExplode(final CreatureSpawnEvent e) {
        if (e.getEntity() instanceof final WanderingTrader wt) {
            final MerchantRecipe mr = new MerchantRecipe(new ItemBuilder(ItemType.BEDROCK)
                .name("<gray>Загляни в меня попожже...").build(), 0);
            mr.setIngredients(List.of(new ItemBuilder(ItemType.BARRIER)
                .name("<gray>Торговля еще в разработке!").build()));
            wt.setRecipes(List.of(mr));
        }
    }
    
    @EventHandler
    public void onExpld(final EntityExplodeEvent e) {
        if (CustomEntity.get(e.getEntity()) instanceof final Spored sp) {
            sp.onExplode(e);
            return;
        }
        switch (e.getEntityType()) {
            case FIREBALL, SMALL_FIREBALL, TNT, WITHER_SKULL:
                if (Main.subServer == SubServer.INFERNAL) break;
                e.blockList().clear();
            default:
                break;
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
