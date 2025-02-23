package ru.romindous.skills.listeners;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Vector;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.utils.ItemUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.MainTask;
import ru.romindous.skills.SubServer;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.survs.Survivor;



public class ShotLst implements Listener {

    public record DmgPrj(int startSec, double damage) {}
    public static final IntHashMap<DmgPrj> projDmg = new IntHashMap<>();
    public static void damage(final Projectile prj, final double dmg) {
        projDmg.put(prj.getEntityId(), new DmgPrj(MainTask.tick / 20, dmg));
    }
    public static double damage(final Projectile prj) {
        final DmgPrj dmg = projDmg.get(prj.getEntityId());
//        Bukkit.getConsoleSender().sendMessage("pr-" + prj.getType().name() + " d=" + dmg);
        if (dmg != null) return dmg.damage;
        final ItemStack it;
        final int lvl;
        double d;
        return switch (prj) {
            case final Trident pr -> {
                d = pr.getDamage();
                it = pr.getItemStack();
                lvl = it.getEnchantmentLevel(Enchantment.IMPALING);
                if (lvl != 0 && (pr.getWorld().hasStorm()
                    || pr.getLocation().getBlock().isLiquid())) {
                    d += lvl * 2.5d;
                }
                yield d;
            }
            case final AbstractArrow pr -> {
                d = pr.getDamage() + pr.getVelocity().length();
                if (pr.isCritical()) d += ((int) d >> 1) + 1;
                yield d;
            }
            case final Snowball pr -> {
                if (ItemUtil.is(pr.getItem(), ItemType.SNOWBALL)) yield 0.1d;
                d = pr.getVelocity().lengthSquared();
                //TODO staff dmg enchant
                yield d;
            }
            case final Firework pr -> 5d + (pr.getFireworkMeta().getEffectsSize() << 1);
            case final ShulkerBullet ignored -> 4d;
            default -> 0d;
        };
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLaunch(final ProjectileLaunchEvent e) {
        final Projectile prj = e.getEntity();
        if (prj.getShooter() instanceof final Player p) {
            final Survivor sv = PM.getOplayer(p, Survivor.class);
            if (sv == null) return;

            sv.trigger(Trigger.PROJ_LAUNCH, e, p);
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    //тут только фиксируются попадания, без урона
    //урон от снарядов модифицируется в EntityDamageByEntityEvent
    public void onProjectileHit(final ProjectileHitEvent e) {
        final Projectile prj = e.getEntity();
        //стреляющий живчик
        if (!(prj.getShooter() instanceof final LivingEntity shoter)) return;
        if (e.getHitEntity() instanceof final LivingEntity target) { //попадание было в живчика
            target.setNoDamageTicks(0);

            switch (shoter) {
                case final Player pl -> {
                    final Survivor sv = PM.getOplayer(pl, Survivor.class);
                    if (sv == null) return;
                    sv.trigger(Trigger.RANGED_HIT, e, pl);
                }
                case final Mob mb -> {
                    final Vector vec = prj.getVelocity();
                    final AttributeInstance dmg = mb.getAttribute(Attribute.ATTACK_DAMAGE);
                    if (dmg == null) return;
                    vec.multiply(dmg.getBaseValue() * 0.1d + 1d);
                    prj.setVelocity(vec);
                }
                default -> {}
            }
            return;
        }
        // --- конец попадание было в живчика ---

        switch (prj.getType()) {
        case SMALL_FIREBALL:
            if (Main.subServer == SubServer.INFERNAL) {
                prj.getWorld().createExplosion(prj, 0.6f, true, false);
                return;
            }
            break;
        case EXPERIENCE_BOTTLE:
            ((ExpBottleEvent) e).setExperience(Main.srnd.nextInt(16) + 16);
            break;
        default:
            break;
        }

            /*//попадание в блок
            if (e.getHitBlock() != null) {
                // *** зачары на блок ***
            	if (sourceWeapon == null) {
            		if (prj instanceof ThrowableProjectile) {
            			final ItemStack prjIt = ((ThrowableProjectile) prj).getItem();
            			if (prjIt != null && prjIt.hasItemMeta()) {
                			switch (SkillMats.getCstmItm(prjIt.getItemMeta())) {
    						case GRENADE:
    							prj.getWorld().createExplosion(prj.getLocation(), 2.5f, false, false, shoter);
    							break;
    						default:
    							break;
    						}
            			}
            		}
            	}
            }*/
        // --- конец стреляющий живчик ---
    }
}
