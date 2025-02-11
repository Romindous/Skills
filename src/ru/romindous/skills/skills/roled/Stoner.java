package ru.romindous.skills.skills.roled;


import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Main;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.skills.Rarity;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.chas.ChasMod;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;

public class Stoner implements Scroll.Registerable {
    @Override
    public void register() {

        new Selector() {
            public String id() {
                return "more_hp_closest";
            }
            public String name() {
                return "Ближняя Здоровее";
            }
            final ChasMod DIST = distChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST};
            }
            private final String[] desc = new String[]{
                TCUtil.N + "Ближайшая сущность, со " + CLR + "здоровьем" + TCUtil.N + ", более",
                TCUtil.N + "чем у " + CLR + "цели" + TCUtil.N + ", не далее " + DIST.id + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.STONER;}
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final double hp = ch.target().getHealth();
                final LivingEntity le = LocUtil.getClsChEnt(loc, DIST.modify(ch, lvl), LivingEntity.class,
                    ent -> ent.getHealth() > hp && Main.canAttack(ch.caster(), ent, false));
                return le == null ? List.of() : List.of(le);
            }
        };

        //сущности с большим хитбоксом

        new Ability() {//Толчок
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DAMAGE};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final Location fin = tgt.getLocation().add(0d, -0.4d, 0d);
                final BlockData bd = Nms.fastData(fin);
                if (!bd.getMaterial().asBlockType().isSolid()) {
                    inform(ch, "Цель должна быть на земле!");
                    return false;
                }
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));

                new ParticleBuilder(Particle.BLOCK_CRUMBLE).count(20).offset(0.4d, 0.4d, 0.4d)
                    .location(fin).allPlayers().extra(0.1d).data(bd).spawn();
                fin.getWorld().playSound(fin, Sound.BLOCK_BASALT_BREAK, 1.6f, 0.8f);
                final BlockDisplay rck = fin.getWorld().spawn(fin, BlockDisplay.class, dis -> dis.setBlock(bd));
                Ostrov.sync(() -> {
                    rck.setInterpolationDelay(0);
                    rck.setInterpolationDuration(8);
                    final Transformation tm = rck.getTransformation();
                    rck.setTransformation(new Transformation(new Vector3f(0f, 0.5f, 0f),
                        tm.getLeftRotation(), tm.getScale(), tm.getRightRotation()));
                    Ostrov.sync(() -> {
                        rck.remove();
                    }, 12);
                }, 2);

                next(chn, () -> {
                    tgt.damage(fe.getDamage(), fe.getDamageSource());
                    defKBLe(caster, tgt, true);
                });
                return true;
            }
            public String id() {
                return "bump";
            }
            public String name() {
                return "Толчок";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает малый подземный толчек под целью,",
                TCUtil.N + "нанося ей " + DAMAGE.id + " ед. " + TCUtil.N + "урона",
                TCUtil.N + "<red>Цель должна быть на земле!"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SHIELD_OFF;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.STONER;}
        };

        new Ability() {//Укрепление
            final ChasMod HEAL = new ChasMod(this, "heal", Chastic.REGENERATION);
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {HEAL};
            public ChasMod[] stats() {
                return stats;
            }
            private final int amp = value("amp", 1);
            private static final BlockData bd = BlockType.SPONGE.createBlockData();
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(ch.on(this));
                final double abs = caster.getAbsorptionAmount() + HEAL.modify(chn, lvl);
                final AttributeInstance ain = caster.getAttribute(Attribute.MAX_ABSORPTION);
                if (ain == null) {
                    inform(ch, "Нет атрибута абзорбции!");
                    return false;
                }
                if (ain.getBaseValue() < abs) ain.setBaseValue(abs);
                caster.setAbsorptionAmount(Math.min(abs, ain.getValue()));
                addEffect(caster, PotionEffectType.RESISTANCE, TIME.modify(chn, lvl), amp, true);
                EntityUtil.effect(caster, Sound.BLOCK_GILDED_BLACKSTONE_BREAK, 0.6f, Particle.DUST_PILLAR, bd);

                next(chn);
                return true;
            }
            public String id() {
                return "harden";
            }
            public String name() {
                return "Укрепление";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Защищает пользователя, давая ему " + HEAL.id + " хп",
                TCUtil.N + "абсорбции и защиту на " + TIME.id + " сек."};
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
            public Role role() {return Role.STONER;}
        };

        new Ability() {//Подскок
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            final ChasMod[] stats = new ChasMod[] {SPEED};
            public ChasMod[] stats() {
                return stats;
            }
            private final double defY = value("defY", 1d);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(ch.on(this));
                caster.setVelocity(caster.getEyeLocation().getDirection().setY(0d)
                    .normalize().setY(defY).multiply(SPEED.modify(chn, lvl)));
                EntityUtil.effect(caster, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, Particle.GUST);

                next(chn);
                return true;
            }
            public String id() {
                return "leap";
            }
            public String name() {
                return "Подскок";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Позволяет совершить " + CLR + "подскок",
                TCUtil.N + "пользователю со скоростью в " + SPEED.id + " бл./сек.",
                TCUtil.N + "<red>Не нулирует урон от падения после прыжка!"};
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
            public Role role() {return Role.STONER;}
        };

        new Ability() {//Прищемление
            final ChasMod EFFECT = new ChasMod(this, "effect", Chastic.EFFECT);
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {TIME, EFFECT};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final Location fin = tgt.getLocation().add(0d, -0.4d, 0d);
                final BlockData bd = Nms.fastData(fin);
                if (!bd.getMaterial().asBlockType().isSolid()) {
                    inform(ch, "Цель должна быть на земле!");
                    return false;
                }
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(makeDamageEvent(caster, tgt));
                addEffect(tgt, PotionEffectType.SLOWNESS, TIME.modify(chn, lvl),
                    (int) Math.round(EFFECT.modify(chn, lvl)), true);
                tgt.teleport(fin);
                EntityUtil.effect(tgt, bd.getSoundGroup().getHitSound(),
                    0.8f, Particle.DUST_PILLAR, bd);

                next(chn);
                return true;
            }
            public String id() {
                return "pinch";
            }
            public String name() {
                return "Прищемление";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Замщемляет цель в " + CLR + "окружающей " + TCUtil.N + "среде,",
                TCUtil.N + "погребая ее и давая замедление " + EFFECT.id + " ур.",
                TCUtil.N + "(округляемо), на " + TIME.id + " сек.",
                TCUtil.N + "<red>Цель должна быть на земле!"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.AXE;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.STONER;}
        };

        new Ability() {//Зубчатость
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_TAKEN);
            final ChasMod[] stats = new ChasMod[] {DAMAGE};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.event() instanceof EntityDamageEvent)) {
                    inform(ch, "Этой способности нужен тригер: "
                        + Trigger.USER_HURT.disName());
                    return false;
                }
                final LivingEntity caster = ch.caster();
                final LivingEntity tgt = ch.target();
                final EntityDamageEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));

                EntityUtil.effect(caster, Sound.ENCHANT_THORNS_HIT, 0.8f, Particle.ENCHANTED_HIT);

                next(chn, () -> {
                    tgt.damage(fe.getDamage(), fe.getDamageSource());
                    defKBLe(caster, tgt, false);
                });
                return true;
            }
            public String id() {
                return "thorns";
            }
            public String name() {
                return "Зубчатость";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "При получении урона, шипы на " + CLR + "чешуе",
                TCUtil.N + "пользователя в ответ наносят " + DAMAGE.id + " ед."};
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
            public Role role() {return Role.STONER;}
        };

        //волна

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_TAKEN, Chastic.COOLDOWN};
            }
            public String id() {
                return "hurt_and_cd";
            }
            public String name() {
                return "Герзаун";
            }
            public ItemType icon() {
                return ItemType.MOURNER_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.STONER;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.TIME};
            }
            public String id() {
                return "armored_time";
            }
            public String name() {
                return "Улзиан";
            }
            protected String needs() {
                return TCUtil.N + CLR + "Пользователь " + TCUtil.N + "имеет полный сет брони";
            }
            public double modify(Chastic ch, double def, int lvl, @Nullable Chain info) {
                if (info == null || !InvCondition.ARMOR_FULL.test(info.caster().getEquipment())) return def;
                return super.modify(ch, def, lvl, info);
            }
            public ItemType icon() {
                return ItemType.SNORT_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return Role.STONER;}
        };
    }
}
