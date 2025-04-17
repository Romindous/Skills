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
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Main;
import ru.romindous.skills.objects.Effects;
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

public class Stoner implements Scroll.Regable {
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
                TCUtil.N + "чем у " + CLR + "цели" + TCUtil.N + ", не далее " + DIST.id() + " бл."};
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
                fin.setYaw(0f); fin.setPitch(0f);
                final BlockData bd = Nms.fastData(fin);
                if (!bd.getMaterial().asBlockType().isSolid()) {
                    inform(ch, "Цель должна быть на земле!");
                    return false;
                }
                final LivingEntity caster = ch.caster();

                new ParticleBuilder(Particle.BLOCK).count(40).offset(0.6d, 0.6d, 0.6d)
                    .location(fin).allPlayers().extra(0.1d).data(bd).spawn();
                fin.getWorld().playSound(fin, Sound.BLOCK_BASALT_BREAK, 1.6f, 0.8f);
                final BlockDisplay rck = fin.getWorld().spawn(fin.add(-0.5d, -0.5d, -0.5d),
                    BlockDisplay.class, dis -> dis.setBlock(bd));
                Ostrov.sync(() -> {
                    rck.setInterpolationDelay(0);
                    rck.setInterpolationDuration(4);
                    final Transformation tm = rck.getTransformation();
                    rck.setTransformation(new Transformation(new Vector3f(0f, 0.4f, 0f),
                        tm.getLeftRotation(), tm.getScale(), tm.getRightRotation()));
                    Ostrov.sync(() -> rck.remove(), 12);
                }, 2);

                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                fe.setDamage(DAMAGE.modify(ch, lvl));
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                defKBLe(caster, tgt, true);
                next(ch);
                return true;
            }
            public String id() {
                return "bump";
            }
            public String name() {
                return "Здвиг";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает малый подземный толчек под целью,",
                TCUtil.N + "нанося ей " + DAMAGE.id() + " ед. " + TCUtil.N + "урона",
                TCUtil.N + "<amber>Цель должна быть на земле!"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public InvCondition equip() {
                return InvCondition.FIST_ANY;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.STONER;}
        };

        new Ability() {//Укрепление
            final ChasMod HEAL = new ChasMod(this, "heal", Chastic.REGENERATION);
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {HEAL, TIME};
            public ChasMod[] stats() {
                return stats;
            }
            private final int amp = value("amp", 1);
            private static final BlockData bd = BlockType.SPONGE.createBlockData();
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final int regen = (int) HEAL.modify(ch, lvl);
                if (regen << 1 < caster.getAbsorptionAmount()) {
                    inform(ch, "Тебя уже переполняет абзорбция!");
                    return false;
                }
                final double abs = caster.getAbsorptionAmount() + regen;
                final AttributeInstance ain = caster.getAttribute(Attribute.MAX_ABSORPTION);
                if (ain == null) {
                    inform(ch, "Нет атрибута абзорбции!");
                    return false;
                }
                if (ain.getBaseValue() < abs) ain.setBaseValue(abs);
                caster.setAbsorptionAmount(Math.min(abs, ain.getValue()));
                addEffect(caster, PotionEffectType.RESISTANCE, TIME.modify(ch, lvl), amp, true);
                EntityUtil.effect(caster, Sound.BLOCK_GILDED_BLACKSTONE_BREAK, 0.6f, Particle.DUST_PILLAR, bd);

                next(ch);
                return true;
            }
            public String id() {
                return "harden";
            }
            public String name() {
                return "Укрепление";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Защищает пользователя, давая ему " + HEAL.id() + " ед.",
                TCUtil.N + "абсорбции и защиту на " + TIME.id() + " сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SHIELD_OFF;
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
                caster.setVelocity(caster.getEyeLocation().getDirection().setY(0d).normalize()
                    .setY(defY).multiply(SPEED.modify(ch, lvl) * balMul(caster.getVelocity())));

                EntityUtil.effect(caster, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, Particle.GUST);

                next(ch);
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
                TCUtil.N + "пользователю со скоростью в " + SPEED.id() + " бл./сек.",
                TCUtil.N + "<amber>Не нулирует урон от падения после прыжка!"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public InvCondition equip() {
                return InvCondition.FIST_ANY;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.STONER;}
        };

        new Ability() {//Прищемление
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {TIME};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final Location fin = tgt.getLocation().add(0d, tgt.getHeight() * -0.5d, 0d);
                final BlockData bd = Nms.fastData(fin);
                if (!bd.getMaterial().asBlockType().isSolid()) {
                    inform(ch, "Цель должна быть на земле!");
                    return false;
                }

                EntityUtil.effect(tgt, bd.getSoundGroup().getHitSound(),
                    0.8f, Particle.DUST_PILLAR, bd);

                final double time = TIME.modify(ch, lvl);
                tgt.setVelocity(new Vector()); tgt.teleport(fin);
                Effects.STAG.apply(tgt, new Vector(0d, -1d, 0d), (int) (time * 20d), 1d);
                next(ch);
                return true;
            }
            public String id() {
                return "pinch";
            }
            public String name() {
                return "Щемление";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Погребает цель в " + CLR + "почве " + TCUtil.N + "вокруг,",
                TCUtil.N + "давая ей замедление на " + TIME.id() + " сек.",
                TCUtil.N + "<amber>Цель должна быть на земле!"};
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
            public Role role() {return Role.STONER;}
        };

        new Ability() {//Зубчатость
            final ChasMod HURT = new ChasMod(this, "hurt", Chastic.DAMAGE_TAKEN);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {HURT, DAMAGE};
            public ChasMod[] stats() {
                return stats;
            }
            public Trigger trig() {
                return Trigger.USER_HURT;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final LivingEntity tgt = ch.target();
                if (!(ch.trig() instanceof final EntityDamageEvent e)
                    || e.getEntity().getEntityId() != caster.getEntityId()) {
                    inform(ch, name() + " <red>должна следовать тригеру <u>"
                        + Trigger.USER_HURT.disName());
                    return false;
                }
                final double dmg = e.getDamage();
                final double back = HURT.modify(ch, lvl);
                if (dmg < back) return false;

                EntityUtil.effect(caster, Sound.ENCHANT_THORNS_HIT, 0.8f, Particle.ENCHANTED_HIT);

                final EntityDamageEvent fe = makeDamageEvent(caster, tgt);
                fe.setDamage(back * DAMAGE.modify(ch, lvl));
                e.setDamage(Math.max(back, dmg - back));
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                defKBLe(caster, tgt, false);
                next(ch);
                return true;
            }
            public String id() {
                return "thorns";
            }
            public String name() {
                return "Зубчатость";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Шипы на " + CLR + "чешуе " + TCUtil.N + "пользовотеля отражают",
                TCUtil.N + HURT.id() + " ед. " + TCUtil.N + " полученого урона обратно.",
                TCUtil.N + "Отраженный урон множится на " + DAMAGE.id() + "x"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
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
