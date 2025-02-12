package ru.romindous.skills.skills.roled;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.skills.Rarity;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.listeners.ShotLst;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.chas.ChasMod;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;

public class Mage implements Scroll.Registerable {
    //
    @Override
    public void register() {

        new Selector() {
            public String id() {
                return "sides_thin";
            }
            public String name() {
                return "Сущности по Сторонам";
            }
            final ChasMod DIST = distChMod(), AMT = amtChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST, AMT};
            }
            private final double arc = value("arc", 0.6d);
            private final String[] desc = new String[]{
                TCUtil.N + "Сущности по " + CLR + "сторонам " + TCUtil.N + "предыдушей " + CLR + "цели" + TCUtil.N + ", с",
                TCUtil.N + "аркой в " + CLR + (int) (arc * 100) + "° " + TCUtil.N + "и дистанцией " + DIST.id + " бл.",
                TCUtil.N + "Лимит - " + AMT.id + " сущ. (округляемо)"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.MAGE;}
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                loc.setDirection(loc.toVector().subtract(ch.caster().getLocation().toVector()));
                final float yw = loc.getYaw();
                loc.setYaw(yw + 90f);
                final Collection<LivingEntity> chLEnts = getChArcLents(loc, DIST.modify(ch, lvl), arc,
                    ent -> Main.canAttack(ch.caster(), ent, false));
                loc.setYaw(yw - 90f);
                final Collection<LivingEntity> chREnts = getChArcLents(loc, DIST.modify(ch, lvl), arc,
                    ent -> Main.canAttack(ch.caster(), ent, false));
                if (chLEnts.isEmpty() || chREnts.isEmpty()) return List.of();
                final List<LivingEntity> les = new ArrayList<>();
                final Iterator<LivingEntity> chLi = chLEnts.iterator();
                final Iterator<LivingEntity> chRi = chREnts.iterator();
                final int amt = (int) Math.round(AMT.modify(ch, lvl));
                les.add(ch.target());
                for (int cnt = 1; cnt != amt; cnt++) {
                    if ((cnt & 1) == 0) {
                        if (!chLi.hasNext()) {
                            if (chRi.hasNext()) continue;
                            break;
                        }
                        les.add(chLi.next());
                    } else {
                        if (!chRi.hasNext()) {
                            if (chLi.hasNext()) continue;
                            break;
                        }
                        les.add(chRi.next());
                    }
                }
                return les;
            }
        };

        new Ability() {//Пламя
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {TIME};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                tgt.setFireTicks(tgt.getFireTicks() + (int) (TIME.modify(ch, lvl) * 20d));

                EntityUtil.effect(tgt, Sound.ENTITY_BLAZE_SHOOT, 0.8f, Particle.LAVA);

                next(ch);
                return true;
            }
            public String id() {
                return "ignite";
            }
            public String name() {
                return "Возжигание";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Воспламеняет указанную " + CLR + "цель,",
                TCUtil.N + "на " + TIME.id + " сек. " + TCUtil.N + "горения"};
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

        new Ability() {//Молния
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DAMAGE};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();

                tgt.getWorld().strikeLightningEffect(tgt.getLocation());

                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                fe.setDamage(DAMAGE.modify(ch, lvl));
                next(ch, () -> {
                    tgt.damage(fe.getDamage(), fe.getDamageSource());
                    defKBLe(tgt);
                });
                return true;
            }
            public String id() {
                return "strike";
            }
            public String name() {
                return "Молния";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Поражает цель могучей " + CLR + "молнией,",
                TCUtil.N + "нанося " + DAMAGE.id + " ед. " + TCUtil.N + "урона"};
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
            public Role role() {return Role.MAGE;}
        };

        new Ability() {//Метеор
            final ChasMod POWER = new ChasMod(this, "power", Chastic.EFFECT);
            final ChasMod[] stats = new ChasMod[] {POWER};
            public ChasMod[] stats() {
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
                final Location fin = flc.subtract(vc);

                new ParticleBuilder(Particle.SQUID_INK).location(fin).count(8)
                    .offset(0.4d, 0.4d, 0.4d).extra(0.0).allPlayers().spawn();
                fin.getWorld().playSound(fin, Sound.ENTITY_BREEZE_SHOOT, 1f, 0.8f);

                final double pwr = POWER.modify(ch, lvl);
                next(ch, () -> {
                    final LargeFireball fb = flc.getWorld().spawn(fin, LargeFireball.class, f -> {
                        f.setShooter(caster); f.setDirection(vc); f.setVelocity(vc.normalize());
                    });
                    fb.setYield((float) pwr * 0.25f); ShotLst.damage(fb, pwr);
                });
                return true;
            }
            public String id() {
                return "meteor";
            }
            public String name() {
                return "Метеор";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Вызывает метеор с небес, чей " + CLR + "размер",
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
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final int regen = (int) HEAL.modify(ch, lvl);
                final double abs = caster.getAbsorptionAmount() + regen;
                final AttributeInstance ain = caster.getAttribute(Attribute.MAX_ABSORPTION);
                if (ain == null) {
                    inform(ch, "Нет атрибута абзорбции!");
                    return false;
                }
                if (ain.getBaseValue() < abs) ain.setBaseValue(abs); caster.setAbsorptionAmount(abs);
                addEffect(caster, PotionEffectType.REGENERATION, TIME.modify(ch, lvl), regen, true);
                EntityUtil.effect(caster, Sound.BLOCK_SCULK_SENSOR_CLICKING, 1.6f, Particle.COMPOSTER);
                new ParticleBuilder(Particle.SONIC_BOOM).count(1).location(EntityUtil.center(caster)).allPlayers().spawn();

                next(ch);
                return true;
            }
            public String id() {
                return "adsorb";
            }
            public String name() {
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

        /*new Ability() {//Скопление
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod[] stats = new ChasMod[] {DAMAGE, DIST};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.caster();
                final LivingEntity caster = ch.caster();
                final Location loc = EntityUtil.center(tgt);
                final LivingEntity le = LocUtil.getClsChEnt(loc, DIST.modify(ch, lvl), LivingEntity.class,
                    e -> Main.canAttack(caster, e, false) && e.getEntityId() != tgt.getEntityId());
                if (le == null) {
                    inform(ch, "Не найдено следующей цели в радиусе!");
                    return false;
                }
                final Chain chn = ch.event(ch.on(this));
                final ShulkerBullet sb = loc.getWorld().spawn(loc, ShulkerBullet.class);
                ShotLst.damage(sb, DAMAGE.modify(ch, lvl));
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
            public String name() {
                return "Скопление";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает снаряд, нацеленый на ближайшую",
                TCUtil.N + "цель, в радиусе " + DIST.id + " бл. " + TCUtil.N + " и нанося",
                TCUtil.N + DAMAGE.id + " ед. " + TCUtil.N + " урона при попадании"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public InvCondition equip() {
                return InvCondition.NONE;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.MAGE;}
        };*/

        new Ability() {//Дисперсия
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            final ChasMod AMOUNT = new ChasMod(this, "amount", Chastic.AMOUNT);
            final ChasMod[] stats = new ChasMod[] {SPEED};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.trig() instanceof final ProjectileLaunchEvent ee)) {
                    inform(ch, name() + " должна следовать тригеру <u>"
                        + Trigger.PROJ_LAUNCH.disName());
                    return false;
                }
                if (!(ee.getEntity() instanceof final Snowball sb)
                    || !ItemType.SNOWBALL.equals(sb.getItem().getType().asItemType())) {
                    inform(ch, "Снаряд не был выпущен из посоха!");
                    return false;
                }
                final LivingEntity caster = ch.caster();
                final Location loc = sb.getLocation();
                final Vector dir = sb.getVelocity();
                loc.setDirection(dir);

                loc.getWorld().playSound(caster, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1.4f);

                final float yw = loc.getYaw();
                final double spd = dir.length() * SPEED.modify(ch, lvl);
                final int amt = (int) AMOUNT.modify(ch, lvl);
                for (int i = amt; i != 0; i--) {
                    final float yaw = ((i & 1) == 0 ? 5 : -5) * i + yw;
                    next(ch, () -> {
                        loc.setYaw(yaw);
                        caster.launchProjectile(Snowball.class, loc.getDirection().multiply(spd), s -> {
                            s.setItem(sb.getItem());
                            s.setGravity(false);
                        });
                    });
                }
                return true;
            }
            public String id() {
                return "disperce";
            }
            public String name() {
                return "Дисперсия";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает " + AMOUNT.id + " доп. " + TCUtil.N + "снаряда посоха, при" + CLR + "выстреле",
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

        //set fire
        //teleport
        //freeze

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.MANA, Chastic.EFFECT};
            }
            public String id() {
                return "mana_to_eff";
            }
            public String name() {
                return "Кандиас";
            }
            public ItemType icon() {
                return ItemType.BREWER_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.MAGE;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_DEALT};
            }
            public String id() {
                return "burning_dmg";
            }
            public String name() {
                return "Ингорто";
            }
            protected String needs() {
                return TCUtil.N + "Поджег указаной " + CLR + "цели";
            }
            public double modify(Chastic ch, double def, int lvl, @Nullable Chain info) {
                if (info == null || info.target().getFireTicks() < 1) return def;
                return super.modify(ch, def, lvl, info);
            }
            public ItemType icon() {
                return ItemType.BURN_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return Role.MAGE;}
        };

        //cooldown skill buff
    }
}
