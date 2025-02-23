package ru.romindous.skills.skills.abils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.MainTask;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.listeners.DamageLst;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.chas.ChasMod;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.survs.Survivor;

public abstract class Ability implements Scroll {//способность

    public static final Map<String, Ability> VALUES = new HashMap<>();
    public static final IntHashMap<List<Ability>> RARITIES = new IntHashMap<>();

    public static final ArrayList<InterNext> nexts = new ArrayList<>();
    public record InterNext(Chain ch, int time) {
        public void run() {
//            Bukkit.getConsoleSender().sendMessage("run step=" + ch.curr());
            ch.sk().step(ch);
        }
    }

    public void next(final Chain chain) {
        nexts.add(new InterNext(chain.next(this), MainTask.tick));
    }

    public static final String data = "abil";

    public static final int stepCd = ConfigVars.get(data + ".stepCd", 10);
    public static final double defKB = ConfigVars.get(data + ".defKb", 1d);
    public static final double defDY = ConfigVars.get(data + ".defY", 1d);
    public static final double defDEL = ConfigVars.get(data + ".defDEL", 2d);

    public final ChasMod MANA = new ChasMod(this, "mana", Chastic.MANA);
    public final ChasMod CD = new ChasMod(this, "cd", Chastic.COOLDOWN);

    private static int id_count = 0;
    final int nid = id_count++;

    protected Ability() {
        VALUES.put(id(), this);
        final List<Ability> mds = RARITIES.get(sum());
        if (mds != null) mds.add(this);
        else RARITIES.put(sum(), new ArrayList<>(Arrays.asList(this)));
    }

//    protected abstract Selector[] selectors();

    protected abstract String[] descs();

    public abstract ChasMod[] stats();

    public abstract boolean selfCast();

    public static final String SIDE = "✵";
    public String side() {
        return SIDE;
    }

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

    public ItemStack display(final int lvl) {
        return new ItemBuilder(icon()).name(TCUtil.sided(name(lvl), side()))
            .flags(true, ItemFlag.HIDE_ADDITIONAL_TOOLTIP).lore(desc(lvl)).build();
    }

    public ItemStack drop(final int lvl) {
        return new ItemBuilder(icon()).name(TCUtil.sided("<u>" + name(lvl) + "</u>", side())).flags(true, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            .lore(desc(lvl)).data(OStrap.key(data()), id()).data(OStrap.key(LVL), lvl).lore(TCUtil.P + "ПКМ " + TCUtil.N + "- присвоить").build();
    }

    public ItemType icon() {
        return switch (role()) {
//            case VAMPIRE -> ItemType.RIB_ARMOR_TRIM_SMITHING_TEMPLATE;
            case ASSASIN -> ItemType.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE;
            case ARCHER -> ItemType.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE;
            case WARRIOR -> ItemType.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE;
            case MAGE -> ItemType.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE;
            case PHANTOM -> ItemType.VEX_ARMOR_TRIM_SMITHING_TEMPLATE;
//            case NECROS -> ItemType.WILD_ARMOR_TRIM_SMITHING_TEMPLATE;
            case STONER -> ItemType.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE;
            case null -> ItemType.COAST_ARMOR_TRIM_SMITHING_TEMPLATE;
        };
    }

    public String data() {
        return data;
    }

    public List<String> context(final Skill sk, final int lvl) {
        final List<String> dscs = new ArrayList<>();
        final ChasMod[] stats = stats();
        for (final String d : descs()) {
            String ed = d.replace(CLR, rarity().color());
            for (final ChasMod st : stats) {
                ed = ed.replace(st.id(), st.chs().color()
                    + StringUtil.toSigFigs(st.modify(sk, lvl), Stat.SIG_FIGS_NUM));
            }
            dscs.add(ed);
        }
        return dscs;
    }

    public String[] desc(final int lvl) {
        final List<String> dscs = new ArrayList<>();
        dscs.add(TCUtil.N + "Применимая роль: " + (role() == null ? Role.ANY : role().disName()));
        if (selfCast()) dscs.add(TCUtil.P + "Всегда подбирает пользователя!");
        dscs.add("<dark_gray>Способность:");
        final ChasMod[] stats = stats();
        for (final String d : descs()) {
            String ed = d.replace(CLR, rarity().color());
            for (final ChasMod st : stats) {
                ed = ed.replace(st.id(), st.chs().color()
                    + StringUtil.toSigFigs(st.calc(lvl), Stat.SIG_FIGS_NUM));
            }
            dscs.add(ed);
        }
        if (equip() != null || trig() != null) {
            dscs.add("<dark_gray>Требует:");
            if (trig() != null) dscs.add(trig().describe());
            if (equip() != null) dscs.add(equip().describe());
        }
        dscs.add(" ");
        dscs.add(TCUtil.N + "Влияющие Модификаторы:");
        dscs.add(TCUtil.N + "- " + Chastic.MANA.disName());
        dscs.add(TCUtil.N + "- " + Chastic.COOLDOWN.disName());
        for (final ChasMod st : stats()) {
            dscs.add(TCUtil.N + "- " + st.chs().disName());
        }
        dscs.add(" ");
        dscs.add(TCUtil.N + "Стоимость: " + Main.manaClr + StringUtil.toSigFigs(MANA.calc(lvl), Stat.SIG_FIGS_PER) + " душ");
        dscs.add(TCUtil.N + "Перезарядка: " + Main.cdClr + StringUtil.toSigFigs(CD.calc(lvl), Stat.SIG_FIGS_PER) + " сек");
//        dscs.add(" ");
        return dscs.toArray(new String[0]);
    }

    public String[] next(final int lvl) {
        final List<String> dscs = new ArrayList<>();
        dscs.add(TCUtil.N + "Применимая роль: " + (role() == null ? Role.ANY : role().disName()));
        if (selfCast()) dscs.add(TCUtil.P + "Всегда подбирает пользователя!");
        dscs.add("<dark_gray>Способность:");
        final ChasMod[] stats = stats();
        for (final String d : descs()) {
            String ed = d.replace(CLR, rarity().color());
            for (final ChasMod st : stats) {
                ed = ed.replace(st.id(), st.chs().color() + StringUtil.toSigFigs(st.calc(lvl), Stat.SIG_FIGS_NUM) + TCUtil.P
                    + (st.scale() > 0 ? " (+" : " (") + StringUtil.toSigFigs(st.scale(), Stat.SIG_FIGS_NUM) + ")" + st.chs().color());
            }
            dscs.add(ed);
        }
        if (equip() != null || trig() != null) {
            dscs.add("<dark_gray>Требует:");
            if (trig() != null) dscs.add(trig().describe());
            if (equip() != null) dscs.add(equip().describe());
        }
        dscs.add(" ");
        dscs.add(TCUtil.N + "Влияющие Модификаторы:");
        dscs.add(TCUtil.N + "- " + Chastic.MANA.disName());
        dscs.add(TCUtil.N + "- " + Chastic.COOLDOWN.disName());
        for (final ChasMod st : stats()) {
            dscs.add(TCUtil.N + "- " + st.chs().disName());
        }
        dscs.add(" ");
        dscs.add(TCUtil.N + "Стоимость: " + Main.manaClr + StringUtil.toSigFigs(MANA.calc(lvl), Stat.SIG_FIGS_PER) + TCUtil.P
            + (MANA.scale() > 0 ? " (+" : " (") + StringUtil.toSigFigs(MANA.scale(), Stat.SIG_FIGS_NUM) + ")" + MANA.chs().color() + " душ");
        dscs.add(TCUtil.N + "Перезарядка: " + Main.cdClr + StringUtil.toSigFigs(CD.calc(lvl), Stat.SIG_FIGS_PER) + TCUtil.P
            + (CD.scale() > 0 ? " (+" : " (") + StringUtil.toSigFigs(CD.scale(), Stat.SIG_FIGS_NUM) + ")" + CD.chs().color() + " сек");
//        dscs.add(" ");
        return dscs.toArray(new String[0]);
    }

    public @Nullable Trigger trig() {
        return null;
    }

    public @Nullable InvCondition equip() {
        return null;
    }

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

    protected void inform(final Chain ch, final String msg) {
        if ((selfCast() || ch.curr() == 1) && ch.caster() instanceof final Player pl) {
            final Survivor sv = PM.getOplayer(pl, Survivor.class);
            sv.inform(pl, "<red>" + msg);
        }
    }

    protected static Collection<LivingEntity> getChArcLents(final Location loc, final double dst, final double arc, final Predicate<LivingEntity> can) {
        final Vector dir = loc.getDirection();
        final double dArc = arc * arc;
        return LocUtil.getChEnts(loc, dst,
            LivingEntity.class, ent -> {
                return can.test(ent)
                    && EntityUtil.center(ent).subtract(loc).toVector()
                    .normalize().subtract(dir).lengthSquared() < dArc;
            });
    }

    protected static LivingEntity getClsArcLent(final Location loc, final double dst, final double arc, final Predicate<LivingEntity> can) {
        final Vector dir = loc.getDirection();
        final double dArc = arc * arc;
        return LocUtil.getClsChEnt(loc, dst,
            LivingEntity.class, ent -> can.test(ent)
                && EntityUtil.center(ent).subtract(loc).toVector()
                .normalize().subtract(dir).lengthSquared() < dArc);
    }

    protected static void addEffect(final LivingEntity le, final PotionEffectType type, final double dur, final int amp, final boolean vis) {
        final PotionEffect pe = le.getPotionEffect(type);
        if (pe == null) {
            le.addPotionEffect(new PotionEffect(type, (int) (dur * 20d), amp, !vis, vis, true));
            return;
        }

        if (pe.getAmplifier() > amp) return;
        le.removePotionEffect(type);
        le.addPotionEffect(new PotionEffect(type, (int) (dur * 20d) + pe.getDuration(), amp, !vis, vis, true));
    }

    protected static EntityDamageByEntityEvent makeDamageEvent(final LivingEntity caster, final LivingEntity le) {
        return DamageLst.damageEvent(caster, le, le, DamageType.MAGIC, 0d);
    }

    protected static void defKBLe(final LivingEntity caster, final LivingEntity tgt, final boolean up) {
        final Vector vel = tgt.getVelocity();
        final Vector kbv = tgt.getLocation().subtract(caster.getLocation()).toVector().normalize().multiply(defKB);
//        Bukkit.getConsoleSender().sendMessage("already-" + vel + " and " + balMul(vel));
        tgt.setVelocity(vel.add((up ? kbv.setY(kbv.getY() + defDY) : kbv).multiply(balMul(vel))));
    }

    protected static void defKBLe(final LivingEntity caster, final LivingEntity tgt, final boolean up, final double mul) {
        final Vector vel = tgt.getVelocity();
        final Vector kbv = tgt.getLocation().subtract(caster.getLocation()).toVector().normalize().multiply(defKB * mul);
        tgt.setVelocity(vel.add((up ? kbv.setY(kbv.getY() + defDY) : kbv).multiply(balMul(vel))));
    }

    protected static void defKBLe(final LivingEntity tgt, final boolean back, final boolean up) {
        final Vector vel = tgt.getVelocity();
        final Vector kbv = tgt.getEyeLocation().getDirection().normalize().multiply(back ? defKB * -1 : defKB);
        tgt.setVelocity(vel.add((up ? kbv.setY(kbv.getY() + defDY) : kbv).multiply(balMul(vel))));
    }

    protected static void defKBLe(final LivingEntity tgt) {
        final Vector kbv = tgt.getVelocity();
        tgt.setVelocity(kbv.setY(kbv.getY() + defDY * balMul(kbv)));
    }

    protected static double balMul(final Vector vel) {
        return Math.min(1d / vel.clone().multiply(defDEL).lengthSquared(), 1d);
    }

    public record AbilState(Ability val, int lvl) implements State {}

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
