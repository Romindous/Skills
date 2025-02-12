package ru.romindous.skills.skills.abils;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import ru.komiss77.utils.EntityUtil;
import ru.romindous.skills.events.EntityCastEvent;
import ru.romindous.skills.events.PlayerKillEntityEvent;
import ru.romindous.skills.mobs.Minion;
import ru.romindous.skills.skills.Skill;

public record Chain(Skill sk, LivingEntity caster, LivingEntity target, Event trig, Location at, int curr) {
    public Chain next(final Ability ab) {return new Chain(sk, caster, target, new EntityCastEvent(this, ab), at, curr);}
    public Chain target(final LivingEntity tgt) {return new Chain(sk, caster, tgt, trig, at, curr);}
    public Chain curr(final int step) {return new Chain(sk, caster, target, trig, at, step);}
//    public EntityCastEvent on(final Ability ab) {return new EntityCastEvent(this, ab);}
    public static Chain of(final Skill sk, final LivingEntity caster,
        final Event ev, final Location at, final int curr) {
        return new Chain(sk, caster, caster, ev, at, curr);
    }
    public static Chain of(final Skill sk, final LivingEntity caster, final Event ev, final int curr) {
        return new Chain(sk, caster, tgtOf(ev, caster), ev, locOf(ev, caster), curr);
    }
    private static final int TGT_DST = 40;
    private static Location locOf(final Event e, final LivingEntity cst) {
        final Location loc = switch (e) {
            case final PlayerKillEntityEvent ee:
                yield EntityUtil.center(ee.getEntity());
            case final EntityDamageEvent ee:
                if (!(ee.getEntity() instanceof final LivingEntity tgt)) yield null;
                if (ee.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr) {
                    if (cst.getEntityId() == dmgr.getEntityId())
                        yield EntityUtil.center(tgt);
                    else if (tgt.getEntityId() == cst.getEntityId())
                        yield EntityUtil.center(dmgr);
                }
                yield EntityUtil.center(tgt);
            case final ProjectileLaunchEvent ee:
                if (!(ee.getEntity().getShooter() instanceof LivingEntity)) yield null;
                yield EntityUtil.center(ee.getEntity());
            case final ProjectileHitEvent ee:
                if (!(ee.getEntity().getShooter() instanceof LivingEntity)) yield null;
                if (!(ee.getHitEntity() instanceof LivingEntity)) yield null;
                yield EntityUtil.center(ee.getEntity());
            case final PlayerInteractEvent ee:
                if (ee.getInteractionPoint() == null) {
                    final Block b = ee.getPlayer().getTargetBlockExact(TGT_DST);
                    if (b == null) yield null;
                    yield b.getLocation().toCenterLocation();
                }
                yield ee.getInteractionPoint();
            case final PlayerJumpEvent ee:
                yield ee.getPlayer().getLocation();
            case final PlayerToggleFlightEvent ee:
                yield ee.getPlayer().getLocation();
            case final EntityDeathEvent ee:
                yield EntityUtil.center(ee.getEntity());
            case final Minion.MinionSpawnEvent ee:
                yield EntityUtil.center(ee.getEntity());
            case final EntityCastEvent ee:
                yield ee.getEntity().getEyeLocation();
            default: yield null;
        };
        return loc == null ? cst.getEyeLocation() : loc;
    }
    private static LivingEntity tgtOf(final Event e, final LivingEntity cst) {
        final LivingEntity ent = switch (e) {
            case final PlayerKillEntityEvent ee:
                yield ee.getEntity();
            case final EntityDamageEvent ee:
                if (!(ee.getEntity() instanceof final LivingEntity tgt)) yield null;
                if (ee.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr) {
                    if (cst.getEntityId() == dmgr.getEntityId()) yield tgt;
                    else if (tgt.getEntityId() == cst.getEntityId()) yield dmgr;
                }
                yield tgt;
            case final ProjectileLaunchEvent ignored: yield null;
            case final ProjectileHitEvent ee:
                if (!(ee.getEntity().getShooter() instanceof LivingEntity)) yield null;
                if (!(ee.getHitEntity() instanceof final LivingEntity tgt)) yield null;
                yield tgt;
            case final PlayerInteractEvent ee: yield ee.getPlayer();
            case final PlayerJumpEvent ee: yield ee.getPlayer();
            case final PlayerToggleFlightEvent ee: yield ee.getPlayer();
            case final EntityDeathEvent ee: yield ee.getEntity();
            case final Minion.MinionSpawnEvent ee: yield ee.getEntity();
            case final EntityCastEvent ee: yield ee.getEntity();
            default: yield null;
        };
        return ent == null ? cst : ent;
    }
}
