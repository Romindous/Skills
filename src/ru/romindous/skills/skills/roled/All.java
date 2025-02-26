package ru.romindous.skills.skills.roled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
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

public class All implements Scroll.Regable {
    @Override
    public void register() {

        new Selector() {
            public String id() {
                return "forward_thin";
            }
            public String name() {
                return "Сущности Позади";
            }
            final ChasMod DIST = distChMod(), AMT = amtChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST, AMT};
            }
            private final double arc = value("arc", 0.6d);
            private final String[] desc = new String[]{
                TCUtil.N + "Сущности за стороной предыдушей " + CLR + "цели" + TCUtil.N + ", с",
                TCUtil.N + "аркой в " + CLR + (int) (arc * 100) + "° " + TCUtil.N + "и дистанцией " + DIST.id() + " бл.",
                TCUtil.N + "Лимит - " + AMT.id() + " сущ. (округляемо)"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final Collection<LivingEntity> chEnts = getChArcLents(loc, DIST.modify(ch, lvl),
                    arc, ent -> Main.canAttack(ch.caster(), ent, false));
                if (chEnts.isEmpty()) return List.of();
                final List<LivingEntity> les = new ArrayList<>();
                final Iterator<LivingEntity> chi = chEnts.iterator();
                final int amt = (int) Math.round(AMT.modify(ch, lvl));
                les.add(ch.target()); int cnt = 1;
                while (chi.hasNext() && cnt < amt) {
                    les.add(chi.next()); cnt++;
                }
                return les;
            }
        };

        new Selector() {
            public String id() {
                return "forward_wide";
            }
            public String name() {
                return "Отдаленные Сущности";
            }
            final ChasMod DIST = distChMod(), AMT = amtChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST, AMT};
            }
            private final double arc = value("arc", 1.0d);
            private final String[] desc = new String[]{
                TCUtil.N + "Сущности за стороной предыдушей " + CLR + "цели" + TCUtil.N + ", с",
                TCUtil.N + "аркой в " + CLR + (int) (arc * 100) + "° " + TCUtil.N + "и дистанцией " + DIST.id() + " бл.",
                TCUtil.N + "Лимит - " + AMT.id() + " сущ. (округляемо)"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final Collection<LivingEntity> chEnts = getChArcLents(loc, DIST.modify(ch, lvl),
                    arc, ent -> Main.canAttack(ch.caster(), ent, false));
                if (chEnts.isEmpty()) return List.of();
                final List<LivingEntity> les = new ArrayList<>();
                final Iterator<LivingEntity> chi = chEnts.iterator();
                final int amt = (int) Math.round(AMT.modify(ch, lvl));
                les.add(ch.target());
                for (int cnt = 1; cnt != amt; cnt++) {
                    if (!chi.hasNext()) break;
                    les.add(chi.next());
                }
                return les;
            }
        };

        new Selector() {
            public String id() {
                return "closest";
            }
            public String name() {
                return "Ближняя Сущность";
            }
            final ChasMod DIST = distChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST};
            }
            private final String[] desc = new String[]{
                TCUtil.N + "Ближайшую сущность от предыдушей",
                TCUtil.N + CLR + "цели" + TCUtil.N + ", не далее " + DIST.id() + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final LivingEntity le = LocUtil.getClsChEnt(loc, DIST.modify(ch, lvl), LivingEntity.class,
                    ent -> ent.getEntityId() != ch.target().getEntityId() && Main.canAttack(ch.caster(), ent, false));
                return le == null ? List.of() : List.of(le);
            }
        };

        new Selector() {
            public String id() {
                return "circle";
            }
            public String name() {
                return "Сущности в Окружении";
            }
            final ChasMod DIST = distChMod(), AMT = amtChMod();
            final ChasMod[] stats = new ChasMod[]{DIST, AMT};
            public ChasMod[] stats() {
                return stats;
            }
            private final String[] desc = new String[]{
                TCUtil.N + "Сущности, окружающие предыдущую",
                TCUtil.N + CLR + "цель" + TCUtil.N + ", не далее " + DIST.id() + " бл.",
                TCUtil.N + "Лимит - " + AMT.id() + " сущ. (округляемо)"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final Collection<LivingEntity> chEnts = LocUtil.getChEnts(loc, DIST.modify(ch, lvl),
                    LivingEntity.class, ent -> Main.canAttack(ch.caster(), ent, false));
                final List<LivingEntity> les = new ArrayList<>();
                final Iterator<LivingEntity> chi = chEnts.iterator();
                final int amt = (int) Math.round(AMT.modify(ch, lvl));
                int cnt = 0;
                while (chi.hasNext() && cnt < amt) {
                    les.add(chi.next()); cnt++;
                }
                return les;
            }
        };

        new Ability() {//Рывок
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            public ChasMod[] stats() {
                return new ChasMod[] {SPEED};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Vector vc = caster.getVelocity().add(caster.getEyeLocation()
                    .getDirection().multiply(0.2d)).setY(0d);
                if (vc.isZero()) {
                    inform(ch, "Не удалось определить сторону рывка!");
                    return false;
                }
                final Vector vel = vc.normalize().setY(defDY).multiply(SPEED.modify(ch, lvl));
                caster.setVelocity(vel);

                EntityUtil.moveffect(caster, Sound.BLOCK_VINE_STEP, 0.8f, Color.WHITE);

                next(ch);
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
                TCUtil.N + "сторону движения, на " + SPEED.id() + " бл./сек." + TCUtil.N};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public boolean selfCast() {return true;}
            public Role role() {return null;}
        };

        /*new Ability() {//Удар
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            public ChasMod[] stats() {
                return new ChasMod[] {DAMAGE};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(ch, lvl));

                EntityUtil.effect(tgt, Sound.ITEM_DYE_USE, 1f, Particle.ENCHANTED_HIT);

                next(ch, () -> {
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
                TCUtil.N + "Атакует указаную цель, нанося",
                TCUtil.N + "ей " + DAMAGE.id() + " ед. " + TCUtil.N + "урона"};
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
        };*/

        new Ability() {//Толчек
            public ChasMod[] stats() {
                return new ChasMod[] {};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();

                EntityUtil.effect(tgt, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.6f, Particle.DUST_PLUME);

                defKBLe(caster, tgt, true);
                next(ch);
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
                return InvCondition.FIST_ANY;
            }
            public boolean selfCast() {return false;}
            public Role role() {return null;}
        };

        new Ability() {//Отдача
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            final ChasMod[] stats = new ChasMod[] {SPEED};
            public ChasMod[] stats() {
                return stats;
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
                final LivingEntity caster = ch.caster();
                final Vector vel = caster.getVelocity();
                caster.setVelocity(vel.add(ee.getEntity().getVelocity()
                    .multiply(-1d * SPEED.modify(ch, lvl) * balMul(vel))));
                EntityUtil.effect(caster, Sound.ENTITY_BREEZE_SHOOT, 0.8f, Particle.SMALL_GUST);

                next(ch);
                return true;
            }
            public String id() {
                return "recoil";
            }
            public String name() {
                return "Отдача";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Отбрасывает пользователя при " + CLR + "выстреле, " + TCUtil.N + "со",
                TCUtil.N + "скоростью равной " + SPEED.id() + "x " + TCUtil.N + "скорости снаряда."};
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
            public Role role() {return null;}
        };

        new Ability() {//Поедание
            final ChasMod REGEN = new ChasMod(this, "regen", Chastic.TIME);
            final ChasMod HUNGER = new ChasMod(this, "hunger", Chastic.HUNGER);
            public ChasMod[] stats() {
                return new ChasMod[] {REGEN, HUNGER};
            }
            private final int amp = value("amp", 1);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                if (tgt instanceof AbstractSkeleton || tgt instanceof SkeletonHorse) {
                    inform(ch, "У этого монстра нету плоти!");
                    return false;
                }
                final LivingEntity caster = ch.caster();
                addEffect(caster, PotionEffectType.REGENERATION, REGEN.modify(ch, lvl), amp, true);
                addEffect(caster, PotionEffectType.HUNGER, HUNGER.modify(ch, lvl), amp, true);

                EntityUtil.effect(tgt, Sound.ENTITY_GENERIC_EAT, 0.8f, Particle.EGG_CRACK);

                next(ch);
                return true;
            }
            public String id() {
                return "consume";
            }
            public String name() {
                return "Поедание";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Позволяет заживо поедать " + CLR + "сущность",
                TCUtil.N + "имеющую " + CLR + "плоть" + TCUtil.N + ", получая регенерацию",
                TCUtil.N + "на " + REGEN.id() + " сек." + TCUtil.N + " и голод на "
                    + HUNGER.id() + " сек." + TCUtil.N + ", при ее ударе"};
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
            final ChasMod HURT = new ChasMod(this, "hurt", Chastic.DAMAGE_TAKEN);
            public ChasMod[] stats() {
                return new ChasMod[] {HEALTH, HURT};
            }
            public Trigger trig() {
                return Trigger.USER_HURT;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                if (!(ch.trig() instanceof final EntityDamageEvent e)
                    || e.getEntity().getEntityId() != caster.getEntityId()) {
                    inform(ch, name() + " <red>должна следовать тригеру <u>"
                        + Trigger.USER_HURT.disName());
                    return false;
                }
                final double dmg = e.getDamage();
                final double mod = HURT.modify(ch, lvl);
                if (dmg < caster.getHealth() * mod) return false;
                e.setDamage(dmg - HEALTH.modify(ch, lvl));

                EntityUtil.effect(caster, Sound.BLOCK_DECORATED_POT_HIT, 0.8f, Particle.DAMAGE_INDICATOR);

                next(ch);
                return true;
            }
            public String id() {
                return "mitigate";
            }
            public String name() {
                return "Смягчение";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Смягчает полученный урон на " + HEALTH.id() + " ед." + TCUtil.N + " если он",
                TCUtil.N + "больше " + HURT.id() + "x " + TCUtil.N + "здоровья пользователя"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public boolean selfCast() {return true;}
            public Role role() {return null;}
        };

        new Ability() {//Пальпация
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            public ChasMod[] stats() {
                return new ChasMod[] {TIME};
            }
            private final int amp = value("amp", 1);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                addEffect(tgt, PotionEffectType.SLOWNESS, TIME.modify(ch, lvl), amp, true);
                EntityUtil.effect(tgt, Sound.ENTITY_PLAYER_HURT_FREEZE, 0.8f, Particle.ENCHANTED_HIT);

                next(ch);
                return true;
            }
            public String id() {
                return "pulpate";
            }
            public String name() {
                return "Пальпация";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Дестабилизирует цель, давая",
                TCUtil.N + "ей замедление на " + TIME.id() + " сек."};
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
        };



        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_DEALT};
            }
            public String id() {
                return "damage";
            }
            public String name() {
                return "Нанесенный Урон";
            }
            public ItemType icon() {
                return ItemType.BLADE_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_TAKEN};
            }
            public String id() {
                return "hurt";
            }
            public String name() {
                return "Полученный Урон";
            }
            public ItemType icon() {
                return ItemType.HEARTBREAK_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.AMOUNT};
            }
            public String id() {
                return "amount";
            }
            public String name() {
                return "Количество";
            }
            public ItemType icon() {
                return ItemType.PLENTY_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.REWARD};
            }
            public String id() {
                return "reward";
            }
            public String name() {
                return "Награда";
            }
            public ItemType icon() {
                return ItemType.PRIZE_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.REGENERATION};
            }
            public String id() {
                return "regen";
            }
            public String name() {
                return "Регенерация";
            }
            public ItemType icon() {
                return ItemType.HEART_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.VELOCITY};
            }
            public String id() {
                return "velocity";
            }
            public String name() {
                return "Стремление";
            }
            public ItemType icon() {
                return ItemType.FLOW_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.HUNGER};
            }
            public String id() {
                return "hunger";
            }
            public String name() {
                return "Голодание";
            }
            public ItemType icon() {
                return ItemType.SHEAF_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.COOLDOWN};
            }
            public String id() {
                return "cooldown";
            }
            public String name() {
                return "Рефляция";
            }
            public ItemType icon() {
                return ItemType.ARMS_UP_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.MANA};
            }
            public String id() {
                return "mana";
            }
            public String name() {
                return "Расход";
            }
            public ItemType icon() {
                return ItemType.BURN_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DISTANCE};
            }
            public String id() {
                return "distance";
            }
            public String name() {
                return "Зазор";
            }
            public ItemType icon() {
                return ItemType.EXPLORER_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };
    }
}
