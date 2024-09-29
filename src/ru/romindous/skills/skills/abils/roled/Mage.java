package ru.romindous.skills.skills.abils.roled;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.enums.Trigger;
import ru.romindous.skills.events.EntityCastEvent;
import ru.romindous.skills.listeners.ShotLst;
import ru.romindous.skills.skills.ChasMod;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;

import static ru.romindous.skills.enums.Trigger.*;

public class Mage implements Ability.AbilReg {
    @Override
    public void register() {

        new Ability() {//Молния
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {DAMAGE, TIME};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                tgt.setFireTicks((int) (TIME.modify(chn, lvl) * 20));
                tgt.getWorld().strikeLightningEffect(tgt.getLocation());
                defKBLe(tgt);

                next(chn);
                return true;
            }
            public String id() {
                return "leap";
            }
            public String disName() {
                return "Молния";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Поражает цель могучей " + CLR + "молнией,",
                TCUtil.N + "нанося, " + DAMAGE.id + " ед. " + TCUtil.N + "урона и",
                TCUtil.N + "поджигая цель на " + TIME.id + " сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.STAFF_ANY;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.MAGE;}
        };

        new Ability() {//Метеор
            final ChasMod POWER = new ChasMod(this, "power", Chastic.EFFECT);
            final ChasMod[] stats = new ChasMod[] {POWER};
            protected ChasMod[] stats() {
                return stats;
            }
            private final double range = value("range", 12d);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final Location elc = caster.getEyeLocation();
                final Location flc = tgt.getLocation();
                final Vector vc = flc.toVector().subtract(elc.toVector()).multiply(0.8d);
                vc.setY(vc.getY() - range);
                final LargeFireball fb = flc.getWorld().spawn(flc.clone().subtract(vc), LargeFireball.class);
                final Chain chn = ch.event(new ProjectileLaunchEvent(fb));
                final double pwr = POWER.modify(chn, lvl);
                fb.setShooter(caster);
                fb.setVelocity(vc.normalize());
                fb.setYield((float) pwr);
                EntityUtil.effect(fb, Sound.ENTITY_BREEZE_SHOOT, 0.8f, Particle.SQUID_INK);

                next(chn);
                return true;
            }
            public String id() {
                return "meteor";
            }
            public String disName() {
                return "Метеор";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Вызывает метеор из небес, чей " + CLR + "размер",
                TCUtil.N + "и сила взрыва равны " + POWER.id + " ед."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.STAFF_ANY;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.MAGE;}
        };

        new Ability() {//Обновление
            final ChasMod HEAL = new ChasMod(this, "heal", Chastic.REGENERATION);
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {HEAL, TIME};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(ch.on(this));
                final int regen = (int) HEAL.modify(chn, lvl);
                final double abs = caster.getAbsorptionAmount() + regen;
                final AttributeInstance ain = caster.getAttribute(Attribute.GENERIC_MAX_ABSORPTION);
                if (ain == null) return false;
                if (ain.getBaseValue() < abs) ain.setBaseValue(abs);
                caster.setAbsorptionAmount(Math.min(abs, ain.getValue()));
                addEffect(caster, PotionEffectType.REGENERATION, TIME.modify(chn, lvl), regen, true);
                EntityUtil.effect(caster, Sound.BLOCK_SCULK_SENSOR_CLICKING, 1.6f, Particle.COMPOSTER);
                new ParticleBuilder(Particle.SONIC_BOOM).count(1).location(EntityUtil.center(caster)).allPlayers().spawn();

                next(chn);
                return true;
            }
            public String id() {
                return "harden";
            }
            public String disName() {
                return "Обновление";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Дает пользователю " + HEAL.id + " сердец абсорбции",
                TCUtil.N + "и регенерацию такого же уровня на " + TIME.id + " cек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.FIST_ANY;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.MAGE;}
        };

        new Ability() {//Скопление
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod[] stats = new ChasMod[] {DAMAGE, DIST};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.caster();
                final LivingEntity caster = ch.caster();
                final Location loc = EntityUtil.center(tgt);
                final LivingEntity le = LocUtil.getClsChEnt(loc, DIST.modify(ch, lvl), LivingEntity.class,
                    e -> Main.canAttack(caster, e, false) && e.getEntityId() != tgt.getEntityId());
                if (le == null) return false;
                final Chain chn = ch.event(ch.on(this));
                final ShulkerBullet sb = loc.getWorld().spawn(loc, ShulkerBullet.class);
                ShotLst.damage(sb, DAMAGE.modify(chn, lvl));
                sb.setShooter(caster);
                sb.setTarget(le);
                sb.setTargetDelta(le.getLocation().subtract(loc).toVector());
                EntityUtil.effect(caster, Sound.BLOCK_BEEHIVE_ENTER, 1.6f, Particle.GUST);

                next(ch.event(new ProjectileLaunchEvent(sb)));
                return true;
            }
            public String id() {
                return "bunch";
            }
            public String disName() {
                return "Скопление";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает снаряд, нацеленый на ближайшую",
                TCUtil.N + "целю, в радиусе " + DIST.id + " бл. " + TCUtil.N + " и нанося",
                TCUtil.N + DAMAGE.id + " ед. " + TCUtil.N + " урона при попадании"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.NONE;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.MAGE;}
        };

        new Ability() {//Дисперсия
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            final ChasMod[] stats = new ChasMod[] {SPEED};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.event() instanceof final ProjectileLaunchEvent ee) || !(ee.getEntity() instanceof final Snowball sb)
                    || !ItemType.SNOWBALL.equals(sb.getItem().getType().asItemType())) return false;
                final LivingEntity caster = ch.caster();
                final Location loc = sb.getLocation();
                final Vector dir = sb.getVelocity();
                loc.setDirection(dir);
                final float yw = loc.getYaw();
                final double spd = dir.length();
                final List<Chain> chs = new ArrayList<>(4);
                for (int i = 0; i != 2; i++) {
                    loc.setYaw(((i & 1) == 0 ? 15 : -15) + yw);
                    final Snowball nsb = shoot(caster, Snowball.class, loc);
                    final Chain chn = ch.event(new ProjectileLaunchEvent(nsb));
                    nsb.setVelocity(loc.getDirection().multiply(spd * SPEED.modify(chn, lvl)));
                    nsb.setShooter(caster);
                    nsb.setGravity(false);
                    nsb.setItem(sb.getItem());
                    chs.add(chn);
                }
                loc.getWorld().playSound(caster, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1.4f);

                for (final Chain c : chs) next(c);
                return true;
            }
            public String id() {
                return "disperce";
            }
            public String disName() {
                return "Дисперсия";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает доп. снаряды посоха, при " + CLR + "выстреле",
                TCUtil.N + "по сторонам, с " + SPEED.id + "x " + TCUtil.N + "скорости оригинала"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.NONE;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.MAGE;}
        };

        /*new Ability() {//Цепочка
            final ChasMod AMOUNT = new ChasMod(this, "amount", Chastic.AMOUNT);
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DIST, DAMAGE};
            protected ChasMod[] stats() {
                return stats;
            }
            private final int per = value("per", 1);
            public boolean cast(final EntityCastEvent ece, final int lvl, final Skill sk, final int next) {
                final LivingEntity caster = ch.caster();
                final Set<Integer> entIds = new HashSet<>();
                final double dst = DIST.modify(ch, lvl);
                new BukkitRunnable() {
                    int i = (int) AMOUNT.modify(ch, lvl);
                    Location last = ece.getLocation();
                    @Override
                    public void run() {
                        final LivingEntity tgt = LocUtil.getClsChEnt(last, dst, LivingEntity.class,
                            ent -> Main.canAttack(caster, ent, false) && !entIds.contains(ent));
                        if (tgt == null || i-- == 0) {
                            cancel();
                            return;
                        }
                        entIds.add(tgt.getEntityId());
                        final Location nlc = tgt.getEyeLocation();
                        final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                        fe.setDamage(DAMAGE.modify(chn, lvl));
                        tgt.damage(fe.getDamage(), fe.getDamageSource());

                        //TODO effect

                        sk.step(finish(), fe, next);

                        last = nlc;
                    }
                }.runTaskTimer(Main.main, 0, per);
                return true;
            }
            public String id() {
                return "chain";
            }
            public String disName() {
                return "Цепочка";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Поражает ближайшие цели, одну за другой,",
                TCUtil.N + "с дальностью " + DIST.id + " бл." + TCUtil.N + ", нанося",
                TCUtil.N + "каждой " + DAMAGE.id + " ед. " + TCUtil.N + "урона"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.STAFF_ANY;
            }
            public Role role() {return Role.MAGE;}
        };*/

        //teleport

        /*new Ability() {
            final Stat DIST = new Stat("dist", Chastic.DISTANCE);
            final Stat DAMAGE = new Stat("damage", Chastic.DAMAGE_DEALT);
            final Stat[] stats = new Stat[] {DIST, DAMAGE};
            protected Stat[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {SHIFT_RIGHT, SHIFT_LEFT, CAST_SELF, USER_HURT};
            public Trigger[] triggers() {
                return trigs;
            }
            public Trigger finish() {
                return Trigger.ATTACK_ENTITY;
            }
            private static final Set<DamageType> FALLS = Set.of(DamageType.FALL,
                DamageType.STALAGMITE, DamageType.FLY_INTO_WALL);
            public boolean cast(final EntityCastEvent ece, final int lvl, final Skill sk, final int next) {
                if (ch.event() instanceof final EntityDamageEvent ee
                    && FALLS.contains(ee.getDamageSource().getDamageType())) ee.setDamage(0d);
                final LivingEntity caster = ch.caster();
                final Location start = caster.getEyeLocation();
                final ArrayList<Event> evs = new ArrayList<>();
                for (final LivingEntity le : LocUtil.getChEnts(start, DIST.modify(ch, lvl),
                    LivingEntity.class, ent -> Main.canAttack(caster, ent, false))) {
                    final EntityDamageByEntityEvent fe = makeDamageEvent(caster, le);
                    fe.setDamage(DAMAGE.modify(chn, lvl));
                    le.damage(fe.getDamage(), fe.getDamageSource());
                    le.setVelocity(le.getVelocity().add(le.getEyeLocation().subtract(start)
                        .toVector().normalize().setY(defDY).multiply(defKB)));
                    evs.add(fe);
                }

                //TODO effect

                Ostrov.sync(() -> {
                    for (final Event fe : evs)
                        sk.step(finish(), fe, next);
                }, stepCd);
                return true;
            }
            public String id() {
                return "stomp";
            }
            public String disName() {
                return "Грохот";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает шоковую волну вокруг с",
                TCUtil.N + "радиусом " + DIST.id + " бл." + TCUtil.N + ", нанося",
                TCUtil.N + "каждой цели " + DAMAGE.id + " ед. " + TCUtil.N + "урона",
                TCUtil.N + "Смегчает урон от " + CLR + "падения"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.AXE;
            }
            public Role role() {return Role.MAGE;}
        };*/

        /*new Ability() {
            final Stat DIST = new Stat("dist", Chastic.DISTANCE);
            final Stat TIME = new Stat("time", Chastic.TIME);
            final Stat[] stats = new Stat[] {DIST, TIME};
            protected Stat[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {SHIFT_RIGHT, SHIFT_LEFT, CAST_SELF,
                ATTACK_ENTITY, KILL_ENTITY, PROJ_LAUNCH, RANGED_HIT};
            public Trigger[] triggers() {
                return trigs;
            }
            public Trigger finish() {
                return ATTACK_ENTITY;
            }
            public boolean cast(final EntityCastEvent ece, final int lvl, final Skill sk, final int next) {
                final LivingEntity caster = ch.caster();
                final Location start = ece.getLocation();
                final double dst = DIST.modify(ch, lvl);
                final ArrayList<Event> evs = new ArrayList<>();
                for (final LivingEntity le : LocUtil.getChEnts(start, dst,
                    LivingEntity.class, ent -> Main.canAttack(caster, ent, false))) {

                    le.setFreezeTicks((int) (TIME.modify(ch, lvl) * 20d));
                    EntityUtil.effect(le, Sound.ENTITY_PLAYER_HURT_FREEZE, 0.6f, Particle.SPIT);
                    evs.add(makeDamageEvent(caster, le));
                }
                Ostrov.sync(() -> {
                    for (final Event fe : evs)
                        sk.step(finish(), fe, next);
                }, stepCd);
                return true;
            }
            public String id() {
                return "freeze";
            }
            public String disName() {
                return "Примороз";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Примораживает ближайшие цели в радиусе",
                TCUtil.N + DIST.id + " бл." + TCUtil.N + ", на" + TIME.id + "сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.NONE;
            }
            public Role role() {return Role.STONER;}
        };*/
    }
}
