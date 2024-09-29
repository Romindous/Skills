package ru.romindous.skills.skills.abils.roled;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.skills.ChasMod;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;

public class All implements Ability.AbilReg {
    @Override
    public void register() {

        new Ability() {//Рывок
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            protected ChasMod[] stats() {
                return new ChasMod[] {SPEED};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Vector vc = caster.getVelocity();
                final Chain chn = ch.event(ch.on(this));
                caster.setVelocity(vc.setY(0d).normalize().setY(defDY)
                    .multiply(SPEED.modify(chn, lvl)));

                //TODO effect

                next(chn);
                return true;
            }
            public String id() {
                return "dash";
            }
            public String disName() {
                return "Рывок";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Позволяет совершить " + CLR + "рывок " + TCUtil.N + "в",
                TCUtil.N + "сторону движения, на " + SPEED.id + " бл./сек." + TCUtil.N};
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
            public Role role() {return null;}
        };

        new Ability() {//Удар
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            protected ChasMod[] stats() {
                return new ChasMod[] {DAMAGE};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                defKBLe(caster, tgt, false);

                //TODO effect

                next(chn);
                return true;
            }
            public String id() {
                return "slash";
            }
            public String disName() {
                return "Разрез";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Режет область, нанося " + DAMAGE.id + " ед.",
                TCUtil.N + "урона указаной цели"};
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
            public Role role() {return null;}
        };

        new Ability() {//Толчек
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            protected ChasMod[] stats() {
                return new ChasMod[] {DAMAGE};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                Nms.swing(caster, EquipmentSlot.HAND);
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                defKBLe(caster, tgt, true);

                //TODO effect

                next(chn);
                return true;
            }
            public String id() {
                return "punch";
            }
            public String disName() {
                return "Толчек";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Толкает ближайшую сущность, нанося " + DAMAGE.id + " ед.",
                TCUtil.N + "урона и отталкивая ее назад"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.FIST;
            }
            public boolean selfCast() {return false;}
            public Role role() {return null;}
        };

        new Ability() {//Поедание
            final ChasMod TIME = new ChasMod(this, "time", Chastic.NUTRITION);
            protected ChasMod[] stats() {
                return new ChasMod[] {TIME};
            }
            private final int amp = value("amp", 1);
            private final double del = value("del", 2d);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                if (tgt instanceof Skeleton) return false;
                if (tgt instanceof SkeletonHorse) return false;

                final LivingEntity caster = ch.caster();

                final Chain chn = ch.event(ch.on(this));
                final double tm = TIME.modify(chn, lvl);
                addEffect(caster, PotionEffectType.REGENERATION, tm, amp, true);
                addEffect(caster, PotionEffectType.HUNGER, tm / del, 0, true);
                if (caster instanceof final Player pl) {
                    pl.setFoodLevel(Math.max((int) (tm / del) + pl.getFoodLevel(), 20));
                }

                //TODO effect

                next(chn);
                return true;
            }
            public String id() {
                return "consume";
            }
            public String disName() {
                return "Поедание";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Позволяет заживо поедать " + CLR + " сущность",
                TCUtil.N + "имеющую " + CLR + "плоть" + TCUtil.N + ", получая регенерацию",
                TCUtil.N + "и голод на " + TIME.id + " сек." + TCUtil.N + ", при ударе"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.FIST;
            }
            public boolean selfCast() {return false;}
            public Role role() {return null;}
        };

        /*new Ability() {//Побочность
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod RATIO = new ChasMod(this, "ratio", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DIST, RATIO};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final LivingEntity target, final int lvl) {
                if (!(ch.event() instanceof final EntityDamageByEntityEvent ee)
                    || !(ee.getEntity() instanceof final LivingEntity target)) return false;
                final LivingEntity caster = ch.caster();
                final LivingEntity close = LocUtil.getClsChEnt(target.getLocation(),
                    DIST.modify(ch, lvl), LivingEntity.class, ent -> {
                        final int nid = ent.getEntityId();
                        return Main.canAttack(caster, ent, false) && nid != target.getEntityId();
                    });
                if (close == null) return false;
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, target);
                fe.setDamage(fe.getDamage() * RATIO.modify(chn, lvl));
                target.damage(fe.getDamage(), fe.getDamageSource());

                defKBLe(caster, close, true);
                new ParticleBuilder(Particle.SWEEP_ATTACK).location(caster.getEyeLocation()
                    .add(close.getEyeLocation()).multiply(0.5d)).count(1).extra(0d).allPlayers().spawn();

                Ostrov.sync(() -> sk.step(finish(), fe, next), stepCd);
                return true;
            }
            public String id() {
                return "collate";
            }
            public String disName() {
                return "Побочность";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Ударяет сущность, рядом с целью на " + DIST.id + " бл.",
                TCUtil.N + "нанося урон равный " + RATIO.id + "x " + TCUtil.N + "оригинала"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SWORD_BOTH;
            }
            public Role role() {return null;}
        };*/

        new Ability() {//Смягчение
            final ChasMod HEALTH = new ChasMod(this, "health", Chastic.REGENERATION);
            final ChasMod REGEN = new ChasMod(this, "regen", Chastic.DAMAGE_TAKEN);
            protected ChasMod[] stats() {
                return new ChasMod[] {HEALTH, REGEN};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                if (!(ch.event() instanceof final EntityDamageEvent ee
                    && ee.getEntity().getEntityId() == caster.getEntityId())) return false;
                final double dmg = ee.getDamage();
                final double mxHP = caster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                final Chain chn = ch.event(ch.on(this));
                if (dmg < caster.getHealth() * HEALTH.modify(chn, lvl)) return false;
                final double nhp = caster.getHealth() + REGEN.modify(chn, lvl);
                if (nhp > mxHP) return false;
                caster.setHealth(nhp);

                EntityUtil.effect(caster, Sound.BLOCK_DECORATED_POT_HIT, 0.8f, Particle.DAMAGE_INDICATOR);

                next(chn);
                return true;
            }
            public String id() {
                return "mitigate";
            }
            public String disName() {
                return "Смягчение";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Смягчает полученный урон на " + REGEN.id + " ед." + TCUtil.N + "если он",
                TCUtil.N + "равен или более " + HEALTH.id + "x " + TCUtil.N + "здоровья пользователя"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.FIST_OFF;
            }
            public boolean selfCast() {return true;}
            public Role role() {return null;}
        };

        new Ability() {//Пальпация
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            protected ChasMod[] stats() {
                return new ChasMod[] {TIME};
            }
            private final int amp = value("amp", 1);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                addEffect(tgt, PotionEffectType.SLOWNESS, TIME.modify(chn, lvl), amp, true);
                EntityUtil.effect(tgt, Sound.ENTITY_PLAYER_HURT_FREEZE, 0.8f, Particle.ENCHANTED_HIT);

                next(chn);
                return true;
            }
            public String id() {
                return "pulpate";
            }
            public String disName() {
                return "Пальпация";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Дестабилизирует цель, давая",
                TCUtil.N + "ей замедление на " + TIME.id + " сек."};
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
            public Role role() {return null;}
        };

        new Ability() {//Отдача
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            final ChasMod[] stats = new ChasMod[] {SPEED};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.event() instanceof final ProjectileLaunchEvent ee)) return false;
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(ch.on(this));
                final Vector vc = ee.getEntity().getVelocity().multiply(-1d * SPEED.modify(chn, lvl));
                caster.setVelocity(caster.getVelocity().add(vc.setY(vc.getY() + defDY)));
                EntityUtil.effect(caster, Sound.ENTITY_BREEZE_SHOOT, 0.8f, Particle.SMALL_GUST);

                next(chn);
                return true;
            }
            public String id() {
                return "recoil";
            }
            public String disName() {
                return "Отдача";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Отбрасывает пользователя при " + CLR + "выстреле, " + TCUtil.N + "со",
                TCUtil.N + "скоростью равной " + SPEED.id + "x " + TCUtil.N + "скорости снаряда."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.BOW;
            }
            public boolean selfCast() {return true;}
            public Role role() {return null;}
        };
    }
}
