package ru.romindous.skills.skills.abils.roled;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.TCUtil;
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
            public ChasMod[] stats() {
                return new ChasMod[] {SPEED};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(ch.on(this));
                final Vector vc = caster.getVelocity().add(caster.getEyeLocation().getDirection().multiply(0.2d)).setY(0d);
                if (vc.lengthSquared() < 0.01d) return false;
                final Vector vel = vc.normalize().setY(defDY).multiply(SPEED.modify(chn, lvl));
                caster.setVelocity(vel);

                EntityUtil.moveffect(caster, Sound.BLOCK_VINE_STEP, 0.8f, Color.WHITE);

                next(chn);
                return true;
            }
            public String id() {
                return "dash";
            }
            public String name() {
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
            public ChasMod[] stats() {
                return new ChasMod[] {DAMAGE};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));

                EntityUtil.effect(tgt, Sound.ITEM_DYE_USE, 1f, Particle.ENCHANTED_HIT);

                next(chn, () -> {
                    tgt.damage(fe.getDamage(), fe.getDamageSource());
                    defKBLe(caster, tgt, false);
                });
                return true;
            }
            public String id() {
                return "slash";
            }
            public String name() {
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
                return InvCondition.MELEE;
            }
            public boolean selfCast() {return false;}
            public Role role() {return null;}
        };

        new Ability() {//Толчек
            public ChasMod[] stats() {
                return new ChasMod[] {};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(makeDamageEvent(caster, tgt));

                EntityUtil.effect(tgt, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.6f, Particle.DUST_PLUME);

                next(chn, () -> {
                    defKBLe(caster, tgt, true);
                });
                return true;
            }
            public String id() {
                return "punch";
            }
            public String name() {
                return "Толчек";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Толкает ближайшую сущность,",
                TCUtil.N + "откидывая ее назад"};
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

        /*new Ability() {//Отдача
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

                EntityUtil.effect(tgt, Sound.ENTITY_GENERIC_EAT, 0.8f, Particle.EGG_CRACK);

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
                TCUtil.N + "Позволяет заживо поедать " + CLR + "сущность",
                TCUtil.N + "имеющую " + CLR + "плоть" + TCUtil.N + ", получая регенерацию",
                TCUtil.N + "и голод на " + TIME.id + " сек." + TCUtil.N + ", при ударе"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public InvCondition equip() {
                return InvCondition.FIST;
            }
            public boolean selfCast() {return false;}
            public Role role() {return null;}
        };

        new Ability() {//Смягчение
            final ChasMod HEALTH = new ChasMod(this, "health", Chastic.REGENERATION);
            final ChasMod DAMAGE = new ChasMod(this, "regen", Chastic.DAMAGE_TAKEN);
            protected ChasMod[] stats() {
                return new ChasMod[] {HEALTH, DAMAGE};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                if (!(ch.event() instanceof final EntityDamageEvent ee
                    && ee.getEntity().getEntityId() == caster.getEntityId())) return false;
                final double dmg = ee.getDamage();
                final Chain chn = ch.event(ch.on(this));
                if (dmg < caster.getHealth() * HEALTH.modify(chn, lvl)) return false;
                ee.setDamage(dmg + DAMAGE.modify(chn, lvl));

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
                TCUtil.N + "Смягчает полученный урон на " + DAMAGE.id + " ед." + TCUtil.N + " если он",
                TCUtil.N + "равен или более " + HEALTH.id + "x " + TCUtil.N + "здоровья пользователя"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
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
                return Rarity.UNCOM;
            }
            public InvCondition equip() {
                return InvCondition.STAFF_ANY;
            }
            public boolean selfCast() {return false;}
            public Role role() {return null;}
        };*/
    }
}
