package ru.romindous.skills.skills.abils.roled;


import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Main;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.enums.Trigger;
import ru.romindous.skills.events.PlayerKillEntityEvent;
import ru.romindous.skills.skills.ChasMod;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;
import ru.romindous.skills.utils.EffectUtil;

import static ru.romindous.skills.enums.Trigger.ATTACK_ENTITY;

public class Warrior implements Ability.AbilReg {
    @Override
    public void register() {

        /*new Ability() {//Кругобрез
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DIST, DAMAGE};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final LivingEntity tgt = ch.target();
                final Location elc = caster.getEyeLocation();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                defKBLe(caster, tgt, false);

                *//*elc.setPitch(20f);
                final double cls = dst * 0.6d;
                final int times = (int) dst << 2;
                final int add = 360 / times;
                for (int i = 0; i != times; i++) {
                    elc.setYaw(i * add);
                    final Location prt = elc.clone().add(elc.getDirection().multiply(cls));
                    new ParticleBuilder(Particle.SWEEP_ATTACK)
                        .location(prt).count(1).extra(0d).allPlayers().spawn();
                    prt.getWorld().playSound(prt, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.8f);
                }*//*

                next(ch.to(fe));
                return true;
            }
            public String id() {
                return "spin";
            }
            public String disName() {
                return "Кругобрез";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Наносит" + DAMAGE.id + " ед. " + TCUtil.N + "урона целям",
                TCUtil.N + "вокруг, c радиусом в " + DIST.id + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SWORD_BOTH;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.WARRIOR;}
        };*/

        new Ability() {//Пронзание
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DIST, DAMAGE};
            protected ChasMod[] stats() {
                return stats;
            }
            private final double arc = value("arc", 0.2d);
            private final double vel = value("vel", 0.2d);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Location start = caster.getEyeLocation();
                final Vector dir = start.getDirection();
                caster.setVelocity(caster.getVelocity().add(dir.multiply(vel)));

                final double dst = DIST.modify(ch, lvl);
                final Location fin = start.add(dir.multiply(dst));
                fin.setYaw(-fin.getYaw());
                fin.setPitch(-fin.getPitch());

                //TODO effect

                for (final LivingEntity le : getChArcLents(fin, dst, arc, ent -> Main.canAttack(caster, ent, false))) {
                    final EntityDamageByEntityEvent fe = makeDamageEvent(caster, le);
                    final Chain chn = ch.event(fe);
                    fe.setDamage(DAMAGE.modify(chn, lvl));

                    next(chn, () -> {
                        le.damage(fe.getDamage(), fe.getDamageSource());
                        defKBLe(caster, le, true);
                    });
                }
                return true;
            }
            public String id() {
                return "spear";
            }
            public String disName() {
                return "Пронзание";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Продвигает чародея на " + DIST.id + " бл. " + TCUtil.N + "вперед,",
                TCUtil.N + "нанося " + DAMAGE.id + " ед. " + TCUtil.N + " урона задетым целям"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SWORD_FIST;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.WARRIOR;}
        };

        new Ability() {//Просвет
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DAMAGE};
            protected ChasMod[] stats() {
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
                final LivingEntity caster = ch.caster();
                final LivingEntity tgt = ch.target();
                final Location tlc = tgt.getLocation();
                final int light = EffectUtil.getLight(tlc.getBlock());
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl) * light);

                new ParticleBuilder(Particle.WHITE_ASH).location(tlc).offset(0.4d, 0.8d, 0.4d).extra(0).count(light << 4);
                tlc.getWorld().playSound(tlc, Sound.BLOCK_LARGE_AMETHYST_BUD_BREAK, 1f, light * 0.1f);

                next(chn, () -> {
                    tgt.damage(fe.getDamage(), fe.getDamageSource());
                });
                return true;
            }
            public String id() {
                return "light";
            }
            public String disName() {
                return "Просвет";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Взывает к " + CLR + "святости  " + TCUtil.N + "оружия пользователя,",
                TCUtil.N + "нанося дополнительный (" + DAMAGE.id + " ед. " + TCUtil.N + "макс.) урон,",
                TCUtil.N + "завися от уровня " + CLR + "света " + TCUtil.N + "в точке удара"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SWORD;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.WARRIOR;}
        };

        /*new Ability() {//Отражение
            final Stat DIST = new Stat("dist", Chastic.DISTANCE);
            final Stat[] stats = new Stat[] {DIST};
            protected Stat[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {SHIFT_JUMP, DOUBLE_JUMP,
                CAST_SELF, SHIFT_RIGHT, SHIFT_LEFT, USER_HURT};
            public Trigger[] triggers() {
                return trigs;
            }
            public Trigger finish() {
                return ATTACK_ENTITY;
            }
            private final int cd = value("cd", 2);
            private final double kb = value("kb", 1d);
            private final double defY = value("defY", 0.2d);
            private final double arc = value("arc", 0.2d);
            public boolean cast(final EntityCastEvent ece, final int lvl, final Skill sk, final int next) {
                final LivingEntity caster = ch.caster();

                if (caster instanceof final Player pl) {
                    if (!pl.isBlocking()) return false;
                    pl.setCooldown(ItemType.SHIELD, cd * 20);
                }

                final Location elc = caster.getEyeLocation();
                final double dst = DIST.modify(ch, lvl);
                final ArrayList<Event> evs = new ArrayList<>();
                final Vector kbv = elc.getDirection().setY(defY).multiply(kb);

                //TODO effect
                for (final LivingEntity le : getChArcLents(elc, dst, arc, ent -> Main.canAttack(caster, ent, false))) {
                    le.setVelocity(le.getVelocity().add(kbv));
                    evs.add(makeDamageEvent(caster, le));
                }

                Ostrov.sync(() -> {
                    for (final Event fe : evs)
                        sk.step(finish(), fe, next);
                }, stepCd);
                return true;
            }
            public String id() {
                return "repel";
            }
            public String disName() {
                return "Отражение";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Отталкивает цели ближе " + DIST.id + " бл. " + TCUtil.N + "спереди,",
                TCUtil.N + "при поднятом щите, ставя щит в КД на " + CLR + cd + " сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SHIELD_OFF;
            }
            public Role role() {return Role.WARRIOR;}
        };*/

        new Ability() {//Фокус
            final ChasMod EFFECT = new ChasMod(this, "effect", Chastic.EFFECT);
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {TIME, EFFECT};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final PotionEffect pe = caster.getPotionEffect(PotionEffectType.HASTE);
                final int amp;
                final Chain chn = ch.event(ch.on(this));
                if (pe != null) {
                    final int mx = pe.getAmplifier() + 1;
                    amp = mx > EFFECT.modify(chn, lvl) ?
                        pe.getAmplifier() : pe.getAmplifier() + 1;
                    caster.removePotionEffect(PotionEffectType.HASTE);
                } else amp = 0;
                caster.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, (int) (TIME.modify(chn, lvl) * 20d), amp));
                EntityUtil.effect(caster, Sound.BLOCK_CONDUIT_AMBIENT, amp * 0.5f, Particle.DRIPPING_LAVA);

                next(chn);
                return true;
            }
            public String id() {
                return "focus";
            }
            public String disName() {
                return "Фокус";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Концентрирует аттаки пользователя, " + CLR + "ускоряя",
                TCUtil.N + "их " + CLR + "еффектом " + TCUtil.N + "с макс. силой в",
                TCUtil.N + EFFECT.id + TCUtil.N + " и длительностью " + TIME.id + " сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SWORD;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.WARRIOR;}
        };

        new Ability() {//Рипост
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DAMAGE};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                if (!(ch.event() instanceof final EntityDamageByEntityEvent ee)
                    || ee.getDamageSource().getDirectEntity().getEntityId() != tgt.getEntityId()) return false;
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl) * ee.getDamage());

                Nms.swing(caster, EquipmentSlot.HAND);
                EntityUtil.effect(caster, Sound.ITEM_SHIELD_BREAK, 0.6f, Particle.SCRAPE);

                next(chn, () -> {
                    tgt.damage(fe.getDamage(), fe.getDamageSource());
                });
                return true;
            }
            public String id() {
                return "repost";
            }
            public String disName() {
                return "Рипост";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Реагиует на " + CLR + "аттаку " + TCUtil.N + "по пользователю",
                TCUtil.N + "нанося " + DAMAGE.id + "x " + TCUtil.N + "урона цели, в ответ"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SWORD_SHIELD;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.WARRIOR;}
        };

        new Ability() {//Триумф
            final ChasMod TIME = new ChasMod(this, "time", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {TIME};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.event() instanceof PlayerKillEntityEvent)) return false;
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(ch.on(this));
                addEffect(caster, PotionEffectType.STRENGTH, TIME.modify(chn, lvl), 0, true);
                EntityUtil.effect(tgt, Sound.BLOCK_MUDDY_MANGROVE_ROOTS_BREAK, 0.6f, Particle.TRIAL_SPAWNER_DETECTION);
                next(chn);
                return true;
            }
            public String id() {
                return "triumph";
            }
            public String disName() {
                return "Триумф";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Дает эффект " + CLR + "силы " + TCUtil.N + "пользователю,",
                TCUtil.N + "при убийстве цели, на " + TIME.id + " сек"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.MELEE;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.WARRIOR;}
        };
    }
}
