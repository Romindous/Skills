package ru.romindous.skills.skills.abils.roled;


import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
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
import ru.romindous.skills.objects.Bleeding;
import ru.romindous.skills.skills.ChasMod;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;

import static ru.romindous.skills.enums.Trigger.ATTACK_ENTITY;
import static ru.romindous.skills.enums.Trigger.USER_HURT;

public class Phantom implements Ability.AbilReg {
    @Override
    public void register() {

        new Ability() {//Диспульсия
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DIST, DAMAGE};
            public ChasMod[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {USER_HURT};
            public Trigger[] selectors() {
                return trigs;
            }
            private Trigger finish() {
                return ATTACK_ENTITY;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                if (!(ch.event() instanceof final EntityDamageEvent ee
                    && ee.getEntity().getEntityId() == caster.getEntityId())) return false;
                final LivingEntity cls = LocUtil.getClsChEnt(caster.getEyeLocation(), DIST.modify(ch, lvl), LivingEntity.class,
                    ent -> ent.getEntityId() != ch.target().getEntityId() && Main.canAttack(caster, ent, false));
                if (cls == null) return false;

                final double dmg = ee.getDamage();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, cls);
                final Chain chn = ch.event(fe);
                ee.setDamage(0d);

                //TODO effect

                next(chn, () -> {
                    fe.setDamage(DAMAGE.modify(chn, lvl) * dmg);
                    cls.damage(fe.getDamage(), fe.getDamageSource());
                });
                return true;
            }
            public String id() {
                return "dispulse";
            }
            public String name() {
                return "Диспульсия";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Отражает полученый " + CLR + "урон " + TCUtil.N + "в ближайшую",
                TCUtil.N + "цель, в радиусе " + DIST.id + " бл. " + TCUtil.N + "Из за",
                TCUtil.N + "отскока, величина снижается до " + DAMAGE.id + "x " + TCUtil.N + "урона"};
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
            public Role role() {return Role.PHANTOM;}
        };

        new Ability() {//Прыжок
            final ChasMod SPEED = new ChasMod(this, "dist", Chastic.VELOCITY);
            final ChasMod[] stats = new ChasMod[] {SPEED};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Vector vc = caster.getVelocity();
                final Chain chn = ch.event(ch.on(this));
                caster.setVelocity(vc.setY(vc.getY() + SPEED.modify(chn, lvl)));

                //TODO effect

                next(chn);
                return true;
            }
            public String id() {
                return "jump";
            }
            public String name() {
                return "Прыжок";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Позволяет совершить небольшой " + CLR + "прыжок",
                TCUtil.N + "вверх, со скоростью " + SPEED.id + " бл./сек. "};
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
            public Role role() {return Role.PHANTOM;}
        };

        new Ability() {//Дислокация
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {TIME};
            public ChasMod[] stats() {
                return stats;
            }
            private final double secMul = value("secMul", 0.5d);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                if (!(caster instanceof final Player pl)) return false;
                final GameMode gm = pl.getGameMode();
                pl.setGameMode(GameMode.SPECTATOR);
                final Chain chn = ch.event(ch.on(this));
                new BukkitRunnable() {
                    int i = (int) (TIME.modify(chn, lvl) / secMul);
                    @Override
                    public void run() {
                        //TODO effect
                        i--;

                        if (i == 0) {
                            if (pl.isValid()) {
                                pl.setGameMode(gm);
                                next(chn);
                            }
                            cancel();
                        }
                    }
                }.runTaskTimer(Main.main, 0, (long) (20d * secMul));
                return true;
            }
            public String id() {
                return "ghost";
            }
            public String name() {
                return "Дислокация";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Переносит чародея в другое " + CLR + "измерение",
                TCUtil.N + "на " + TIME.id + " сек. " + TCUtil.N + "позволяя тому",
                TCUtil.N + "проходить сквозь блоки и сущностей"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.FIST;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.PHANTOM;}
        };

        new Ability() {//Скример
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {TIME};
            public ChasMod[] stats() {
                return stats;
            }
            private final int amp = value("amp", 1);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(makeDamageEvent(caster, tgt));
                final int time = (int) (TIME.modify(chn, lvl) * 20d);
                tgt.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, time, amp));
                tgt.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, time, amp));

                //TODO effect

                next(chn, () -> {
                    defKBLe(caster, tgt, true);
                });
                return true;
            }
            public String id() {
                return "scare";
            }
            public String name() {
                return "Скример";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Пугает указаную цель, " + CLR + "откидывая " + TCUtil.N + "ее",
                TCUtil.N + "назад и ослабляя на " + TIME.id + " сек."};
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
            public Role role() {return Role.PHANTOM;}
        };

        /*new Ability() {//Разряд
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DIST, DAMAGE};
            protected ChasMod[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {USER_HURT};
            public Trigger[] selectors() {
                return trigs;
            }
            private Trigger finish() {
                return ATTACK_ENTITY;
            }
            private final double add = value("add", 2.4d);
            public boolean cast(final EntityCastEvent ece, final int lvl, final Skill sk, final int next) {
                if (!(ch.ev() instanceof final EntityDamageEvent ee)) return false;
                final LivingEntity caster = ch.caster();
                final double dmg = DAMAGE.modify(ch, lvl, ee);
                if (ee.getFinalDamage() < dmg) return false;
                final ArrayList<Event> evs = new ArrayList<>();
                final Location loc = caster.getLocation();
                for (final LivingEntity le : LocUtil.getChEnts(loc, DIST.modify(ch, lvl),
                    LivingEntity.class, ent -> Main.canAttack(caster, ent, false))) {
                    final EntityDamageByEntityEvent fe = makeDamageEvent(caster, le);
                    fe.setDamage(dmg / (add + loc.distanceSquared(le.getLocation())));
                    defKBLe(caster, le, true);
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
                return "discharge";
            }
            public String disName() {
                return "Разряд";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Распостроняет полученый " + CLR + "урон " + TCUtil.N + "по целям",
                TCUtil.N + "вокруг, в радиусе " + DIST.id + " бл. " + TCUtil.N + "Урон снижается",
                TCUtil.N + "с дистанцией, максимум " + DAMAGE.id + " / "
                    + StringUtil.toSigFigs(add, (byte) 1) + " ед. " + TCUtil.N + "урона"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.NONE;
            }
            public Role role() {return Role.PHANTOM;}
        };*/

        new Ability() {//Путаница
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod[] stats = new ChasMod[] {DIST};
            public ChasMod[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {ATTACK_ENTITY};
            public Trigger[] selectors() {
                return trigs;
            }
            private Trigger finish() {
                return ATTACK_ENTITY;
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.target() instanceof final Mob tgt)) return false;
                final LivingEntity caster = ch.caster();
                final Location loc = caster.getLocation();
                final LivingEntity le = LocUtil.getClsChEnt(loc, DIST.modify(ch, lvl), LivingEntity.class,
                    ent -> Main.canAttack(caster, ent, false) && Main.canAttack(tgt, ent, false));
                if (le == null) return false;
                final Chain chn = ch.event(makeDamageEvent(caster, le));
                tgt.setTarget(le);

                //TODO effect

                next(chn);
                return true;
            }
            public String id() {
                return "confuse";
            }
            public String name() {
                return "Путаница";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Помутняет " + CLR + " разум " + TCUtil.N + "цели, заставляя ее",
                TCUtil.N + "атаковать другую цель в радиусе " + DIST.id + " бл."};
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
            public Role role() {return Role.PHANTOM;}
        };

        new Ability() {//Эссект
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            //            final Stat MAX_DMG = new Stat("ma_dmg", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {TIME, DAMAGE};
            public ChasMod[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {ATTACK_ENTITY};
            public Trigger[] selectors() {
                return trigs;
            }
            private Trigger finish() {
                return ATTACK_ENTITY;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                final Location loc = EntityUtil.center(tgt);
                new ParticleBuilder(Particle.SWEEP_ATTACK).extra(0d)
                    .allPlayers().location(loc).count(1).spawn();
                Bleeding.effect(tgt);

                next(chn, () -> {
                    Bleeding.bleed(tgt, fe.getDamage(),
                        TIME.modify(chn, lvl), caster);
                });
                return true;
            }
            public String id() {
                return "essect";
            }
            public String name() {
                return "Эссект";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает выплеск " + CLR + " энергии " + TCUtil.N + " возле цели,",
                TCUtil.N + "накладывая кровотечение на " + TIME.id + " сек,",
                TCUtil.N + "получая " + DAMAGE.id + " ед. " + TCUtil.N + "урона каждую секунду"};
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
            public Role role() {return Role.PHANTOM;}
        };

        /*new Ability() {//Гравитон
            final Stat DIST = new Stat("dist", Chastic.DISTANCE);
            //            final Stat MAX_DMG = new Stat("ma_dmg", Chastic.DAMAGE_DEALT);
            final Stat[] stats = new Stat[] {DIST};
            protected Stat[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {ATTACK_ENTITY};
            public Trigger[] triggers() {
                return trigs;
            }
            public Trigger finish() {
                return ATTACK_ENTITY;
            }
            private final double add = value("add", 2d);
            public boolean cast(final EntityCastEvent ece, final int lvl, final Skill sk, final int next) {
                if (!(ch.event() instanceof final EntityDamageByEntityEvent ee)
                    || !(ee.getEntity() instanceof final Mob target)) return false;
                final LivingEntity caster = ch.caster();
                final Location loc = caster.getLocation();
                Bukkit.getMobGoals().removeAllGoals(target);
                final LivingEntity le = LocUtil.getClsChEnt(loc, DIST.modify(ch, lvl), LivingEntity.class,
                    ent -> Main.canAttack(caster, ent, false) && Main.canAttack(target, ent, false));
                if (le == null) return false;
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, le);
                target.setTarget(le);

                //TODO effect

                Ostrov.sync(() -> sk.step(finish(), fe, next), stepCd);
                return true;
            }
            public String id() {
                return "graviton";
            }
            public String disName() {
                return "Гравитон";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Помутняет " + CLR + " разум " + TCUtil.N + "цели, заставляя ее",
                TCUtil.N + "атаковать другую цель в радиусе " + DIST.id + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.STAFF_ANY;
            }
            public Role role() {return Role.PHANTOM;}
        };*/
    }
}
