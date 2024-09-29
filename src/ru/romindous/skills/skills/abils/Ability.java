package ru.romindous.skills.skills.abils;

import java.util.*;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.MainTask;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.listeners.EntityDamageLst;
import ru.romindous.skills.objects.Scroll;
import ru.romindous.skills.skills.ChasMod;

public abstract class Ability implements Scroll {//способность

    public static final Map<String, Ability> VALUES = new HashMap<>();
    public static final Map<Rarity, List<Ability>> RARITIES = new HashMap<>();

    public static final ArrayList<InterNext> nexts = new ArrayList<>();
    public record InterNext(Chain ch, int time) {
        public void run() {ch.sk().step(ch);}
    }

    public void next(final Chain chain) {
        nexts.add(new InterNext(chain, MainTask.tick));
    }

    public static final String prefix = "abils.";
    public static final String data = "sel";

    public static final int stepCd = ConfigVars.get(prefix + ".stepCd", 10);
    public static final double defKB = ConfigVars.get(prefix + ".defKb", 1d);
    public static final double defDY = ConfigVars.get(prefix + ".defY", 1d);

    public final ChasMod MANA = new ChasMod(this, "mana", Chastic.MANA);
    public final ChasMod CD = new ChasMod(this, "cd", Chastic.COOLDOWN);

    private static int id_count = 0;
    final int nid = id_count++;

    protected Ability() {
        VALUES.put(id(), this);
        final List<Ability> mds = RARITIES.get(rarity());
        if (mds == null) {
            RARITIES.put(rarity(), new ArrayList<>(Arrays.asList(this)));
        } else mds.add(this);
    }

//    protected abstract Selector[] selectors();

    protected abstract String[] descs();

    protected abstract ChasMod[] stats();

    public abstract boolean selfCast();

//    public int mana(final int level) {
//        return (int) (manaScale * level + manaBase);
//    }

//    public int cooldown(final int level) {
//        return (int) (cdScale * level + cdBase);
//    }

    /*public boolean catches(final Selector tr) {
        if (tr == null) return false;
        for (final Selector t : selectors()) {
            if (t.equals(tr)) return true;
        }
        return false;
    }*/

    public ItemType icon() {
        return switch (role()) {
            case VAMPIRE -> ItemType.RIB_ARMOR_TRIM_SMITHING_TEMPLATE;
            case ASSASIN -> ItemType.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE;
            case ARCHER -> ItemType.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE;
            case WARRIOR -> ItemType.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE;
            case MAGE -> ItemType.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE;
            case PHANTOM -> ItemType.VEX_ARMOR_TRIM_SMITHING_TEMPLATE;
            case NECROS -> ItemType.WILD_ARMOR_TRIM_SMITHING_TEMPLATE;
            case STONER -> ItemType.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE;
            case null -> ItemType.COAST_ARMOR_TRIM_SMITHING_TEMPLATE;
        };
    }

    public String data() {
        return data;
    }

    private static final byte SIG_FIGS = 2;

    public String[] desc(final int lvl) {
        final List<String> dscs = new ArrayList<>();
        dscs.add(TCUtil.N + "Применим для: " + (role() == null ? Role.ANY : role().getName()));
        if (selfCast()) dscs.add(TCUtil.P + "Всегда подбирает только пользователя!");
        dscs.add(" ");
        for (final String d : descs()) {
            String ed = d.replace(CLR, rarity().clr);
            for (final ChasMod st : stats()) {
                ed = ed.replace(st.id, st.chs.color()
                    + StringUtil.toSigFigs(st.calc(lvl), SIG_FIGS));
            }
            dscs.add(ed);
        }
        dscs.add(" ");
        dscs.add(TCUtil.N + "Стоимость: " + Main.manaClr + (int) MANA.calc(lvl) + " душ");
        dscs.add(TCUtil.N + "Перезарядка: " + Main.cdClr + (int) CD.calc(lvl) + " сек");
        dscs.add(" ");
        dscs.add(TCUtil.N + "Влияющие Модификаторы:");
        for (final ChasMod st : stats()) {
            dscs.add(TCUtil.N + "- " + st.chs.getName());
        }
        return dscs.toArray(new String[0]);
    }

//    public abstract Trigger finish();

    public abstract InvCondition equip();

    /*public interface Cast {boolean single();}
    public interface SingleCast {
        boolean cast(final EntityCastEvent ece, final Chain ch, final int lvl);
    }
    public interface MultiCast {
        boolean cast(final EntityCastEvent ece, final Chain ch, final int lvl);
    }*/

    public abstract boolean cast(final Chain ch, final int lvl);

    /*private static final int TGT_DST = 40;
    public EntityCastEvent act(final LivingEntity caster, final Event e) {
        return new EntityCastEvent(caster, this, e, caster);
    }*/

    /*public @Nullable EntityCastEvent act(final Chain ch) {
        final Selector.SelState sls = ch.sk().sels[ch.curr()];
        final LivingEntity main;
        return switch (ch.event()) {
            case final PlayerKillEntityEvent ee:
                yield new EntityCastEvent(ch, this,
                    ee.getEntity(), sls, EntityUtil.center(ee.getEntity()));
            case final EntityDamageEvent ee:
                if (!(ee.getEntity() instanceof final LivingEntity target)) yield null;
                if (ee.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr) {
                    if (ch.caster().getEntityId() == dmgr.getEntityId())
                        yield new EntityCastEvent(ch, this, target, sls, EntityUtil.center(target));
                    else if (target.getEntityId() == ch.caster().getEntityId())
                        yield new EntityCastEvent(ch, this, dmgr, sls, EntityUtil.center(dmgr));
                }
                yield new EntityCastEvent(ch, this, target, sls, EntityUtil.center(target));
            case final ProjectileLaunchEvent ee:
                if (!(ee.getEntity().getShooter() instanceof final LivingEntity shtr)) yield null;
                yield new EntityCastEvent(ch, this, shtr, sls, EntityUtil.center(ee.getEntity()));
            case final ProjectileHitEvent ee:
                if (!(ee.getEntity().getShooter() instanceof final LivingEntity shtr)) yield null;
                if (!(ee.getHitEntity() instanceof final LivingEntity target)) yield null;
                yield new EntityCastEvent(ch, this, target, sls, EntityUtil.center(ee.getEntity()));
            case final PlayerInteractEvent ee:
                if (ee.getInteractionPoint() == null) {
                    main = ee.getPlayer();
                    final Block b = main.getTargetBlockExact(TGT_DST);
                    if (b == null) yield null;
                    yield new EntityCastEvent(ch, this,
                        main, sls, new WXYZ(b).getCenterLoc());
                }
                yield new EntityCastEvent(ch, this,
                    ee.getPlayer(), sls, ee.getInteractionPoint());
            case final PlayerJumpEvent ee:
                main = ee.getPlayer();
                yield new EntityCastEvent(ch, this, main, sls, main.getLocation());
            case final PlayerToggleFlightEvent ee:
                main = ee.getPlayer();
                yield new EntityCastEvent(ch, this, main, sls, main.getLocation());
            case final EntityDeathEvent ee:
                main = ee.getEntity();
                yield new EntityCastEvent(ch, this, main, sls, EntityUtil.center(main));
            case final Minion.MinionSpawnEvent ee:
                yield new EntityCastEvent(ch, this, ee.getEntity(), sls, EntityUtil.center(ee.getEntity()));
            case final EntityCastEvent ee:
                main = ee.getEntity();
                yield new EntityCastEvent(ch, this, main, sls, ee.getLocation());
            default: yield null;
        };
    }*/

    /*public @Nullable EntityCastEvent act(final Trigger tr, final Event e, final Selector.SelState sls) {
        final LivingEntity caster;
        final EntityCastEvent ce = act(e, sls);
        switch (tr) {
            case KILL_ENTITY:
                if (!(e instanceof final PlayerKillEntityEvent ee)) return null;
                return new EntityCastEvent(ee.getPlayer(), this, e,
                    ee.getEntity(), sls, EntityUtil.center(ee.getEntity()));
            case ATTACK_ENTITY:
                if (!(e instanceof final EntityDamageByEntityEvent ee)) return null;
                if (!(ee.getEntity() instanceof final LivingEntity target)) return null;
                if (!(ee.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr)) return null;
                return new EntityCastEvent(dmgr, this, e, target, sls, EntityUtil.center(target));
            case PROJ_LAUNCH:
                if (!(e instanceof final ProjectileLaunchEvent ee)) return null;
                if (!(ee.getEntity().getShooter() instanceof final LivingEntity shtr)) return null;
                return new EntityCastEvent(shtr, this, e, shtr, sls, EntityUtil.center(ee.getEntity()));
            case RANGED_HIT:
                if (!(e instanceof final ProjectileHitEvent ee)) return act(Trigger.ATTACK_ENTITY, e, sls);
                if (!(ee.getEntity().getShooter() instanceof final LivingEntity shtr)) return null;
                if (!(ee.getHitEntity() instanceof final LivingEntity target)) return null;
                return new EntityCastEvent(shtr, this, e, target, sls, EntityUtil.center(ee.getEntity()));
            case SHIFT_RIGHT, SHIFT_LEFT:
                if (!(e instanceof final PlayerInteractEvent ee)) return null;
                if (ee.getInteractionPoint() == null) {
                    caster = ee.getPlayer();
                    final Block b = caster.getTargetBlockExact(TGT_DST);
                    if (b == null) return null;
                    return new EntityCastEvent(caster, this, e, caster, sls, new WXYZ(b).getCenterLoc());
                }
                return new EntityCastEvent(ee.getPlayer(), this, e, ee.getPlayer(), sls, ee.getInteractionPoint());
            case SHIFT_JUMP:
                if (!(e instanceof final PlayerJumpEvent ee)) return null;
                caster = ee.getPlayer();
                if (!caster.isSneaking()) return null;
                return new EntityCastEvent(caster, this, e, caster, sls, caster.getLocation());
            case DOUBLE_JUMP:
                if (!(e instanceof final PlayerToggleFlightEvent ee)) return null;
                caster = ee.getPlayer();
                return new EntityCastEvent(caster, this, e, caster, sls, caster.getLocation());
            case USER_DEATH:
                if (!(e instanceof final EntityDeathEvent ee)) return null;
                caster = ee.getEntity();
                return new EntityCastEvent(caster, this, e, caster, sls, EntityUtil.center(caster));
            case USER_HURT:
                if (!(e instanceof final EntityDamageEvent ee)) return null;
                if (!(ee.getEntity() instanceof final LivingEntity ent)) return null;
                if (ee.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr) {
                    return new EntityCastEvent(ent, this, e, dmgr, sls, EntityUtil.center(dmgr));
                }
                return new EntityCastEvent(ent, this, e, ent, sls, EntityUtil.center(ent));
            case SPAWN_MINION:
                if (!(e instanceof final Minion.MinionSpawnEvent ee)) return null;
                return new EntityCastEvent(ee.getOwner(), this, e, ee.getEntity(), sls, EntityUtil.center(ee.getEntity()));
            case CAST_SELF:
                if (!(e instanceof final EntityCastEvent ee)) return null;
                caster = ee.getEntity();
                return new EntityCastEvent(caster, this, e, caster, sls, ee.getLocation());
            case UNKNOWN:
                return null;
        }
        return null;
    }*/

    protected static Collection<LivingEntity> getChArcLents(final Location loc, final double dst, final double arc, final Predicate<LivingEntity> can) {
        final Vector dir = loc.getDirection();
        return LocUtil.getChEnts(loc, dst,
            LivingEntity.class, ent -> can.test(ent)
                && ent.getEyeLocation().subtract(loc).toVector()
                .normalize().subtract(dir).lengthSquared() < arc);
    }

    private static LivingEntity getClsArcLent(final Location loc, final double dst, final double arc, final Predicate<LivingEntity> can) {
        final Vector dir = loc.getDirection();
        return LocUtil.getClsChEnt(loc, dst,
            LivingEntity.class, ent -> can.test(ent)
                && ent.getEyeLocation().subtract(loc).toVector()
                .normalize().subtract(dir).lengthSquared() < arc);
    }

    protected static void addEffect(final LivingEntity le, final PotionEffectType type, final double dur, final int amp, final boolean vis) {
        final PotionEffect pe = le.getPotionEffect(type);
        if (pe == null) {
            le.addPotionEffect(new PotionEffect(type, (int) (dur * 20d), amp, !vis, vis, vis));
            return;
        }

        if (pe.getAmplifier() > amp) return;
        le.removePotionEffect(type);
        le.addPotionEffect(new PotionEffect(type, (int) (dur * 20d) + pe.getDuration(), amp, !vis, vis, vis));
    }

    protected static <P extends Projectile> P shoot(final LivingEntity shtr, final Class<P> pcl) {
        return shoot(shtr, pcl, shtr.getEyeLocation());
    }

    protected static <P extends Projectile> P shoot(final LivingEntity shtr, final Class<P> pcl, final Location loc) {
        final P prj = shtr.getWorld().spawn(loc, pcl);
        prj.setShooter(shtr);
        return prj;
    }

    protected static EntityDamageByEntityEvent makeDamageEvent(final LivingEntity caster, final LivingEntity le) {
        return EntityDamageLst.damageEvent(caster, le, le, DamageType.MAGIC, 0d);
    }

    protected static void defKBLe(final LivingEntity caster, final LivingEntity tgt, final boolean up) {
        final Vector kbv = tgt.getLocation().subtract(caster.getLocation()).toVector().normalize().multiply(defKB);
        tgt.setVelocity(tgt.getVelocity().add(up ? kbv.setY(kbv.getY() + defDY) : kbv));
    }

    protected static void defKBLe(final LivingEntity tgt, final boolean back, final boolean up) {
        final Vector kbv = tgt.getEyeLocation().getDirection().normalize().multiply(back ? defKB * -1 : defKB);
        tgt.setVelocity(tgt.getVelocity().add(up ? kbv.setY(kbv.getY() + defDY) : kbv));
    }

    protected static void defKBLe(final LivingEntity tgt) {
        final Vector kbv = tgt.getVelocity();
        tgt.setVelocity(kbv.setY(kbv.getY() + defDY));
    }

    public record AbilState(Ability abil, int lvl) {}

    public interface AbilReg {void register();}

    @Override
    public int hashCode() {
        return nid;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Ability && ((Ability) o).nid == nid;
    }

    //    public static final Ability POSSESS = new Ability();
}
