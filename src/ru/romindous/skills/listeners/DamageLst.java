package ru.romindous.skills.listeners;

import java.util.EnumMap;
import java.util.Set;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.checkerframework.checker.nullness.qual.Nullable;
import ru.komiss77.modules.player.PM;
import ru.romindous.skills.Main;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.mobs.Minion;
import ru.romindous.skills.mobs.SednaMob;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.survs.Survivor;


public class DamageLst implements Listener {

    public static final Set<DamageType> DIRECT = Set.of(DamageType.PLAYER_ATTACK, DamageType.GENERIC, DamageType.STING,
        DamageType.MOB_ATTACK, DamageType.MOB_ATTACK_NO_AGGRO, DamageType.PLAYER_EXPLOSION, DamageType.EXPLOSION, DamageType.MACE_SMASH);
    public static final Set<DamageType> RANGED = Set.of(DamageType.THROWN, DamageType.ARROW, DamageType.FIREWORKS, DamageType.TRIDENT,
        DamageType.SPIT, DamageType.WITHER_SKULL, DamageType.UNATTRIBUTED_FIREBALL, DamageType.MOB_PROJECTILE);
    public static final Set<DamageType> MAGIC = Set.of(DamageType.MAGIC, DamageType.INDIRECT_MAGIC);

    @SuppressWarnings("deprecation")
    private static final EnumMap<EntityDamageEvent.DamageModifier, Function<@Nullable Object, Double>> MOD_FUN =
        new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(0.0d)));

//    private static final Set<DamageType> MOB_HIT = Set.of(DamageType.MOB_ATTACK, DamageType.THROWN,
//        DamageType.MOB_ATTACK_NO_AGGRO, DamageType.ARROW, DamageType.MOB_PROJECTILE);

    @SuppressWarnings("deprecation")
    public static EntityDamageByEntityEvent damageEvent(final LivingEntity cause,
        final Entity direct, final Entity tgt, final DamageType type, final double dmg) {
        return new EntityDamageByEntityEvent(cause, tgt, DamageCause.CUSTOM,
            DamageSource.builder(type).withCausingEntity(cause).withDirectEntity(direct).build(),
            new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, dmg)), MOD_FUN, false);
    }

    public static void onCustomAttack(final EntityDamageByEntityEvent e, final SednaMob sm) {
        final DamageSource ds = e.getDamageSource();
        if (!(ds.getCausingEntity() instanceof final Mob dmgr)
            || !(e.getEntity() instanceof final LivingEntity tgt)) return;
        if (!(sm instanceof Minion)) Minion.setAgroOf(dmgr, tgt);
        Minion.setAgroOf(tgt, dmgr);

        double dmg = initDmg(ds.getDirectEntity(), e.getDamage());
        if (dmg < 1d) return;

        if (!(tgt instanceof Player)) return;
        final Survivor sv = PM.getOplayer(tgt.getUniqueId(), Survivor.class);
        if (sv == null) return;
        e.setDamage(Stat.defense(dmg, sv.getStat(Stat.PASSIVE)));
        sv.trigger(Trigger.USER_HURT, e, tgt);
    }

    public static void onCustomDefense(final EntityDamageEvent e, final SednaMob sm) {
        if (!(e.getEntity() instanceof final Mob tgt)) return;
        final DamageSource ds = e.getDamageSource();
        if (!(ds.getCausingEntity() instanceof final LivingEntity dmgr)) return;
        if (!(sm instanceof Minion)) Minion.setAgroOf(tgt, dmgr);
        Minion.setAgroOf(dmgr, tgt);

        if (!(dmgr instanceof final Player p)) return;
        final Survivor sv = PM.getOplayer(p, Survivor.class);
        if (sv == null) return;

        final DamageType dt = ds.getDamageType();
        double dmg = initDmg(ds.getDirectEntity(), e.getDamage());
        if (dmg < 1d) return;

        if (DIRECT.contains(dt)) {
            e.setDamage(Stat.direct(dmg, sv.getStat(Stat.STRENGTH)));
            sv.trigger(Trigger.ATTACK_ENTITY, e, p);
        } else if (RANGED.contains(dt)) {
            e.setDamage(Stat.ranged(dmg, sv.getStat(Stat.ACCURACY)));
            sv.trigger(Trigger.ATTACK_ENTITY, e, p);
//                    sv.trigger(Trigger.RANGED_HIT, e, p);
        } else if (MAGIC.contains(dt)) {
            e.setDamage(Stat.magic(dmg, sv.getStat(Stat.MAGIC)));
        }
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDamagePlayer (final EntityDamageByEntityEvent e) {
        final DamageSource ds = e.getDamageSource();
        if (!(e.getEntity() instanceof final Player tgt)
            || !(ds.getCausingEntity() instanceof final Player dmgr)) return;
        if (!Main.canAttack(dmgr, tgt, true)) {
            e.setCancelled(true);
            e.setDamage(0d);
            return;
        }
        final Survivor dmgrSv = PM.getOplayer(dmgr, Survivor.class);
        final Survivor tgtSv = PM.getOplayer(tgt, Survivor.class);
        if (dmgrSv == null || tgtSv == null) return;
        Minion.setAgroOf(dmgr, tgt);
        Minion.setAgroOf(tgt, dmgr);

        final DamageType dt = ds.getDamageType();
        double dmg = initDmg(ds.getDirectEntity(), e.getDamage());
        if (dmg < 1d) return;

        if (DIRECT.contains(dt)) {
            dmg = Stat.direct(dmg, dmgrSv.getStat(Stat.STRENGTH));
            dmgrSv.trigger(Trigger.ATTACK_ENTITY, e, dmgr);
        } else if (RANGED.contains(dt)) {
            dmg = Stat.ranged(dmg, dmgrSv.getStat(Stat.ACCURACY));
            dmgrSv.trigger(Trigger.ATTACK_ENTITY, e, dmgr);
//                dmgrSv.trigger(Trigger.RANGED_HIT, e, dmgr);
        } else if (MAGIC.contains(dt)) {
            dmg = Stat.magic(dmg, dmgrSv.getStat(Stat.MAGIC));
        } else return;

        e.setDamage(Stat.defense(dmg, tgtSv.getStat(Stat.PASSIVE)));
        tgtSv.trigger(Trigger.USER_HURT, e, tgt);
    }

    private static double initDmg(final Entity ent, final double dmg) {
        if (!(ent instanceof final Projectile prj)) return dmg;
        if (prj.getShooter() instanceof final Mob mb)
            return mb.getAttribute(Attribute.ATTACK_DAMAGE).getValue();
        final double pd = ShotLst.damage(prj);
//        Bukkit.getConsoleSender().sendMessage(prj.getType().name() + " dmg2-" + pd);
        return pd == 0d ? dmg : pd;
    }

    private static final double envDmgPer = ConfigVars.get("damage.envFrac", 0.05d);
    private static final Set<DamageType> ENVIRONMENT = Set.of(DamageType.IN_FIRE, DamageType.ON_FIRE, DamageType.CAMPFIRE,
        DamageType.LAVA, DamageType.LIGHTNING_BOLT, DamageType.DRY_OUT, DamageType.DROWN, DamageType.WITHER, DamageType.FREEZE,
        DamageType.STARVE, DamageType.CACTUS, DamageType.CRAMMING, DamageType.DRAGON_BREATH, DamageType.OUT_OF_WORLD,
        DamageType.OUTSIDE_BORDER, DamageType.SWEET_BERRY_BUSH);
    private static final Set<DamageType> FALL = Set.of(DamageType.FLY_INTO_WALL, DamageType.FALL, DamageType.STALAGMITE,
        DamageType.FALLING_BLOCK, DamageType.FALLING_STALACTITE, DamageType.FALLING_ANVIL);

    //Bosses-HIGH
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void EntityDamageEvent (final EntityDamageEvent e) {
        if (!(e.getEntity() instanceof final LivingEntity ent)) return;

        final DamageType dt = e.getDamageSource().getDamageType();
        double dmg = e.getDamage();
        if (dmg < 1d) return;
        if (ENVIRONMENT.contains(dt)) {
            dmg = ent.getAttribute(Attribute.MAX_HEALTH).getBaseValue() * envDmgPer * dmg;
        } else if (FALL.contains(dt)) {
            if (!(ent instanceof Mob)) return;
            e.setCancelled(true);
            e.setDamage(0d);
            return;
        }

        final Survivor sv = PM.getOplayer(ent.getUniqueId(), Survivor.class);
        if (sv != null) dmg = Stat.defense(dmg, sv.getStat(Stat.PASSIVE));
        e.setDamage(dmg);

        /*final CuBlock cube = SM.cublocks.get(ent.getEntityId());
        if (cube != null) {
            switch (e.getCause()) {
                case SUFFOCATION, FIRE, POISON, WITHER,
                     CRAMMING, CONTACT, FALLING_BLOCK, DRYOUT:
                    e.setDamage(0d);
                    break;
                default:
                    cube.remove(ent);
                    break;
            }
            e.setCancelled(true);
        }*/
    }
}