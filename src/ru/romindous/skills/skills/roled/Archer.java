package ru.romindous.skills.skills.roled;


import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.listeners.ShotLst;
import ru.romindous.skills.skills.Rarity;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;
import ru.romindous.skills.skills.chas.ChasMod;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.survs.Role;

public class Archer implements Scroll.Regable {
    @Override
    public void register() {

        new Selector() {
            public String id() {
                return "forward_closest";
            }
            public String name() {
                return "Сущность Позади";
            }
            final ChasMod DIST = distChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST};
            }
            private final String[] desc = new String[]{
                TCUtil.N + "Сущность, с " + CLR + "обратной " + TCUtil.N + "стороны",
                TCUtil.N + CLR + "цели" + TCUtil.N + ", не далее " + DIST.id() + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.ARCHER;}
            private final double arc = value("arc", 0.6d);
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final LivingEntity le = getClsArcLent(loc, DIST.modify(ch, lvl), arc,
                    ent -> Main.canAttack(ch.caster(), ent, false));
                return le == null ? List.of() : List.of(le);
            }
        };

        new Ability() {//Флэшка
            final ChasMod POWER = new ChasMod(this, "power", Chastic.EFFECT);
            public ChasMod[] stats() {
                return new ChasMod[] {POWER};
            }
            private final double timeMul = value("timeMul", 20d);
            private static final FireworkEffect FW_EFF = FireworkEffect.builder()
                .withColor(Color.YELLOW).with(FireworkEffect.Type.BURST).build();
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final double dmg = POWER.modify(ch, lvl);

                next(ch);
                Ostrov.sync(() -> {
                    final Firework fw = caster.launchProjectile(Firework.class);
                    final FireworkMeta fm = fw.getFireworkMeta();
                    fm.addEffect(FW_EFF);
                    fw.setFireworkMeta(fm);
                    fw.setShotAtAngle(true);
                    fw.setTicksToDetonate((int) (dmg * timeMul));
                    ShotLst.damage(fw, dmg);
                }, shotCd);
                return true;
            }
            public String id() {
                return "flash";
            }
            public String name() {
                return "Флэшка";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Запускает " + CLR + "феерверк " + TCUtil.N + "со ",
                TCUtil.N + "средним уроном в " + POWER.id() + " ед."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.ARCHER;}
        };

        new Ability() {//Спектраль
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            public ChasMod[] stats() {
                return new ChasMod[] {SPEED};
            }
            public Trigger trig() {
                return Trigger.PROJ_LAUNCH;
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.trig() instanceof final ProjectileLaunchEvent ee)) {
                    inform(ch, name() + " <red>должна следовать тригеру <u>"
                        + Trigger.PROJ_LAUNCH.disName());
                    return false;
                }
                if (!(ee.getEntity() instanceof final AbstractArrow ar)) {
                    inform(ch, "Снаряд должен быть стрелой!");
                    return false;
                }
                final LivingEntity caster = ch.caster();
                final ItemStack wpn = ar.getWeapon();
                final Vector vc = ar.getVelocity().multiply(SPEED.modify(ch, lvl));

                EntityUtil.effect(caster, Sound.ITEM_CROSSBOW_SHOOT, 1.4f, Particle.LANDING_HONEY);

                next(ch);
                Ostrov.sync(() -> {
                    final SpectralArrow spa = caster
                        .launchProjectile(SpectralArrow.class, vc);
                    if (wpn != null) spa.setWeapon(wpn);
                }, shotCd);
                return true;
            }
            public String id() {
                return "spectral";
            }
            public String name() {
                return "Спектраль";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает спектральную " + CLR + "копию " + TCUtil.N + "при выстреле стрел,",
                TCUtil.N + "со скоростью равной " + SPEED.id() + "x " + TCUtil.N + "оригинала"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.BOW_ANY;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.ARCHER;}
        };

        new Ability() {//Рикошет
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod RATIO = new ChasMod(this, "ratio", Chastic.VELOCITY);
            public ChasMod[] stats() {
                return new ChasMod[] {DIST, RATIO};
            }
            public Trigger trig() {
                return Trigger.RANGED_HIT;
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.trig() instanceof final ProjectileHitEvent ee)) {
                    inform(ch, name() + " <red>должна следовать тригеру <u>"
                        + Trigger.RANGED_HIT.disName());
                    return false;
                }
                if (!(ee.getEntity() instanceof final AbstractArrow ae)) {
                    inform(ch, "Снаряд должен быть стрелой!");
                    return false;
                }
                if (ae.getPierceLevel() != 0) return false;
                final LivingEntity target = ch.target();
                final LivingEntity tgt = LocUtil.getClsChEnt(ae.getLocation(), DIST.modify(ch, lvl),
                    LivingEntity.class, ent -> ent.getEntityId() != target.getEntityId()
                        && Main.canAttack(ch.caster(), ent, false));
                if (tgt == null) {
                    inform(ch, "Не найдено следующей цели в радиусе!");
                    return false;
                }
                ae.setPierceLevel(1);
                final Location tlc = EntityUtil.center(target);
                final Vector dir = EntityUtil.center(tgt).subtract(tlc).toVector().normalize();
                tlc.add(dir); tlc.setY(ae.getLocation().getY()); ae.teleport(tlc);
                ae.setVelocity(getArrowVc(dir, ae.getVelocity().length() * RATIO.modify(ch, lvl)));
                EntityUtil.effect(tgt, Sound.BLOCK_CALCITE_BREAK, 1.4f, Particle.ELECTRIC_SPARK);

                next(ch);
                return true;
            }
            private Vector getArrowVc(final Vector dst, final double spd) {
                final double DlnSq = 0.01d / spd;
                if (dst.getY() > -DlnSq) dst.setY((dst.getY() + 1d) * DlnSq);
                return dst.multiply(spd);
            }
            public String id() {
                return "ricochet";
            }
            public String name() {
                return "Рикошет";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Позволяет стреле срикошетить при",
                TCUtil.N + CLR + "попадании" + TCUtil.N + "в цель, перенося "
                    + RATIO.id() + "x " + TCUtil.N + "скорости,",
                TCUtil.N + "если след. цель в радиусе " + DIST.id() + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public InvCondition equip() {
                return InvCondition.BOW_ANY;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.ARCHER;}
        };

        new Ability() {//Жаунт
            final ChasMod HUNGER = new ChasMod(this, "hunger", Chastic.HUNGER);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            public ChasMod[] stats() {
                return new ChasMod[] {HUNGER, DAMAGE};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final LivingEntity tgt = ch.target();
                final Location elc = tgt.getEyeLocation();
                caster.teleport(elc.add(elc.getDirection().multiply(-2d)));
                if (caster instanceof final HumanEntity he) {
                    if (!EntityUtil.food(he, (float) HUNGER.modify(ch, lvl))) {
                        inform(ch, "Не хватает сытости для прыжка!");
                        return false;
                    }
                }

                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                fe.setDamage(DAMAGE.modify(ch, lvl));
                EntityUtil.effect(tgt, Sound.ENTITY_FOX_TELEPORT, 0.8f, Particle.REVERSE_PORTAL);
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                if (tgt instanceof Mob) ((Mob) tgt).setTarget(null);
                next(ch);
                return true;
            }
            public String id() {
                return "jounte";
            }
            public String name() {
                return "Жаунт";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Использует " + HUNGER.id() + " ед. " + TCUtil.N + "сытости,",
                TCUtil.N + "пользователя, телепортируя его за",
                TCUtil.N + CLR + "спину" + TCUtil.N + "цели, и нанося "
                    + DAMAGE.id() + " ед. " + TCUtil.N + "урона"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public InvCondition equip() {
                return InvCondition.MELEE;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.ARCHER;}
        };

        new Ability() {//Дострел
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            public ChasMod[] stats() {
                return new ChasMod[] {DAMAGE};
            }
            private final ItemStack FWS = new ItemBuilder(ItemType.FIREWORK_STAR)
                .color(Color.ORANGE).build();
            private final double height = value("height", 12d);
            private final double speed = value("speed", 1d);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final double dmg = DAMAGE.modify(ch, lvl);
                final Location start = caster.getEyeLocation()
                    .add(Main.srnd.nextFloat() - 0.5d, height, Main.srnd.nextFloat() - 0.5d);

                new ParticleBuilder(Particle.SQUID_INK).location(start).count(8)
                    .offset(0.4d, 0.4d, 0.4d).extra(0.0).allPlayers().spawn();
                start.getWorld().playSound(start, Sound.ENTITY_DROWNED_SHOOT, 1f, 1.4f);

                next(ch);
                Ostrov.sync(() -> {
                    final Snowball sb = caster.launchProjectile(Snowball.class, EntityUtil.center(tgt).subtract(start)
                        .toVector().normalize().multiply(speed).add(tgt.getVelocity().multiply(0.5d)), s -> {
                        s.setItem(FWS); s.setGravity(false); s.setGlowing(true);
                    });
                    sb.teleport(start);
                    ShotLst.damage(sb, dmg);
                }, shotCd);
                return true;
            }
            public String id() {
                return "refire";
            }
            public String name() {
                return "Дострел";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает снаряд над пользователем, при " + CLR + "ударе",
                TCUtil.N + "моба, нанося " + DAMAGE.id() + " ед. " + TCUtil.N + "урона при попадании"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.FIST_ANY;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.ARCHER;}
        };

        //pull to other near target
        //melee bow fire
        //arrow circle on mob death
        //обсьрел

        //no gravity charged shot
        //shulker bullets on mob death

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.COOLDOWN, Chastic.VELOCITY};
            }
            public String id() {
                return "cd_to_vel";
            }
            public String name() {
                return "Локеан";
            }
            public ItemType icon() {
                return ItemType.FLOW_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.ARCHER;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_DEALT};
            }
            public String id() {
                return "ranged_dmg";
            }
            public String name() {
                return "Назиел";
            }
            private final double dst = value("distance", 10);
            protected String needs() {
                return TCUtil.N + "Расстояние более " + CLR + dst + " бл. " + TCUtil.N + "до цели";
            }
            public double modify(Chastic ch, double def, int lvl, @Nullable Chain info) {
                if (info == null || info.caster().getLocation()
                    .distanceSquared(info.target().getLocation()) < dst * dst) return def;
                return super.modify(ch, def, lvl, info);
            }
            public ItemType icon() {
                return ItemType.ARCHER_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return Role.ARCHER;}
        };
    }
}
