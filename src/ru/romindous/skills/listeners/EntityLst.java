package ru.romindous.skills.listeners;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Spellcaster.Spell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.entities.CustomEntity;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.BlockUtil;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.version.Nms;
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

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    protected void onHit(final ProjectileHitEvent e) {
        final Projectile prj = e.getEntity();
        if (!prj.isValid() || !(prj.getShooter() instanceof final Mob shtr)
            || !(CustomEntity.get(shtr) instanceof final Crawler cr)) return;
        cr.onHit(e);
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    protected void onExtra(final EntityExplodeEvent e) {
        if (!(e.getEntity() instanceof final Mob mb)
            || !(CustomEntity.get(mb) instanceof Spored)) return;
        final double dmg = mb.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue();
        e.setYield((float) dmg);
        new ParticleBuilder(Particle.COMPOSTER).location(mb.getLocation())
            .count((int) (dmg * 20d)).offset(dmg, dmg, dmg).allPlayers().spawn();
        final List<Block> bls = e.blockList();
        final Set<WXYZ> bps = bls.stream().map(WXYZ::new).collect(Collectors.toSet());
        final WXYZ[] sbls = ClassUtil.shuffle(bps.toArray(new WXYZ[0]));
        final List<WXYZ> finLocs = new LinkedList<>();
        for (int i = sbls.length >> 2; i != 0; i--) {
            final WXYZ bl = sbls[i];
            final WXYZ below = new WXYZ(bl.w, bl.x, bl.y - 1, bl.z);
            if (bps.contains(below) || !Spored.DIRT.contains(Nms.fastType(below))) continue;
            bls.removeIf(bb -> bl.distAbs(bb.getLocation()) == 0);
            final Block b = bl.getBlock();
            b.setBlockData(Spored.MOSS_DATA, false);
            Ostrov.sync(() -> {
                final Block b2 = bl.getBlock();
                if (b2.getType() == Spored.MOSS.getType()) {
                    b2.setBlockData(BlockUtil.air, false);
                    EntityUtil.effect(Main.mobs.SPORED.spawn(bl.getCenterLoc()),
                        Sound.BLOCK_BIG_DRIPLEAF_BREAK, 0.6f, Particle.HAPPY_VILLAGER);
                }
            }, Spored.SPORE_TICKS);
        }

    }
    
    @EventHandler
    public void onExpld(final EntityExplodeEvent e) {
    	final Entity ent = e.getEntity();
        if (ent instanceof Mob) return;
        switch (e.getEntityType()) {
            case FIREBALL, SMALL_FIREBALL, TNT, WITHER_SKULL:
                if (Main.subServer != SubServer.INFERNAL) {
                    e.blockList().clear();
                }
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
