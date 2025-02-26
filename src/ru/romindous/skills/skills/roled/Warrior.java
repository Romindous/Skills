package ru.romindous.skills.skills.roled;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Main;
import ru.romindous.skills.events.PlayerKillEntityEvent;
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

public class Warrior implements Scroll.Regable {
    @Override
    public void register() {

        new Selector() {
            public String id() {
                return "armored_circle";
            }
            public String name() {
                return "Укрепленные Вокруг";
            }
            final ChasMod DIST = distChMod(), AMT = amtChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST, AMT};
            }
            private final String[] desc = new String[]{
                TCUtil.N + "Сущности вокруг, с " + CLR + "броней" + TCUtil.N + " на любой",
                TCUtil.N + "части " + CLR + "тела" + TCUtil.N + ", не далее " + DIST.id() + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.WARRIOR;}
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final Collection<LivingEntity> chEnts = LocUtil.getChEnts(loc, DIST.modify(ch, lvl),
                    LivingEntity.class, ent -> Main.canAttack(ch.caster(), ent, false)
                        && InvCondition.ARMOR_ANY.test(ent.getEquipment()));
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

        //только нежить

        new Ability() {//Пронзание
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DIST, DAMAGE};
            public ChasMod[] stats() {
                return stats;
            }
            private final double arc = value("arc", 0.2d);
            private final double vel = value("vel", 0.2d);
            private static final Color CLR = TCUtil.getBukkitColor(TCUtil.getTextColor(Role.WARRIOR.color()));
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Location start = EntityUtil.center(caster);
                final Vector dir = start.getDirection();
                final double dst = DIST.modify(ch, lvl);
                final Vector vl = caster.getVelocity();
                caster.setVelocity(vl.add(dir.clone().multiply(dst * vel * balMul(vl))));
                caster.setNoDamageTicks(20);

                final Location fin = start.add(dir.multiply(dst));
                fin.setYaw(180 + fin.getYaw());
                fin.setPitch(-fin.getPitch());

                EntityUtil.moveffect(caster, Sound.ENTITY_BREEZE_SHOOT, 1.2f, CLR);
//                new ParticleBuilder(Particle.SQUID_INK).location(fin).count(20).extra(0d).allPlayers().spawn();
                final double dmg = DAMAGE.modify(ch, lvl);
                for (final LivingEntity le : getChArcLents(fin, dst, arc, ent -> Main.canAttack(caster, ent, false))) {
                    final EntityDamageByEntityEvent fe = makeDamageEvent(caster, le);
                    fe.setDamage(dmg);
                    le.damage(fe.getDamage(), fe.getDamageSource());
                    defKBLe(caster, le, false);
                    next(ch);
                }
                return true;
            }
            public String id() {
                return "spear";
            }
            public String name() {
                return "Пронзание";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Продвигает чародея на " + DIST.id() + " бл. " + TCUtil.N + "вперед,",
                TCUtil.N + "нанося " + DAMAGE.id() + " ед. " + TCUtil.N + "урона задетым целям"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public InvCondition equip() {
                return InvCondition.SWORD_FIST;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.WARRIOR;}
        };

        /*new Ability() {//Просвет
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DAMAGE};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final LivingEntity tgt = ch.target();
                final Location tlc = tgt.getLocation();
                final int light = EffectUtil.getLight(tlc.getBlock());
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(ch, lvl) * light);

                new ParticleBuilder(Particle.WHITE_ASH).location(tlc).offset(0.4d, 0.8d, 0.4d).extra(0).count(light << 4);
                tlc.getWorld().playSound(tlc, Sound.BLOCK_LARGE_AMETHYST_BUD_BREAK, 1f, light * 0.1f);

                next(ch, () -> {
                    tgt.damage(fe.getDamage(), fe.getDamageSource());
                });
                return true;
            }
            public String id() {
                return "light";
            }
            public String name() {
                return "Просвет";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Взывает к " + CLR + "святости  " + TCUtil.N + "оружия пользователя,",
                TCUtil.N + "нанося (" + DAMAGE.id() + " ед. " + TCUtil.N + "макс.) урон, завися",
                TCUtil.N + "от уровня " + CLR + "света " + TCUtil.N + "в точке удара"};
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
        };*/

        new Ability() {//Отражение
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {SPEED, TIME};
            public ChasMod[] stats() {
                return stats;
            }
            private final int amp = value("amp", 1);
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
                if (caster instanceof final Player pl) {
                    if (!pl.isBlocking()) {
                        inform(ch, "Нужно держать поднятым щит!");
                        return false;
                    }
                }

                EntityUtil.effect(tgt, Sound.ITEM_WOLF_ARMOR_DAMAGE, 0.8f, Particle.CRIT);

                final double time = TIME.modify(ch, lvl);
                final double speed = SPEED.modify(ch, lvl);
                addEffect(tgt, PotionEffectType.SLOWNESS, time, amp, false);
                defKBLe(caster, tgt, true, speed);
                next(ch);
                return true;
            }
            public String id() {
                return "repel";
            }
            public String name() {
                return "Отражение";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Отталкивает цель со скоростью " + SPEED.id() + " бл./сек.",
                TCUtil.N + "при поднятом " + CLR + "щите" + TCUtil.N + ", замедляя ее на " + TIME.id() + " сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public InvCondition equip() {
                return InvCondition.SHIELD_OFF;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.WARRIOR;}
        };

        new Ability() {//Фокус
            final ChasMod EFFECT = new ChasMod(this, "effect", Chastic.EFFECT);
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {TIME, EFFECT};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final PotionEffect pe = caster.getPotionEffect(PotionEffectType.HASTE);
                final int amp;
                if (pe != null) {
                    final int mx = pe.getAmplifier() + 1;
                    amp = mx > EFFECT.modify(ch, lvl) ?
                        pe.getAmplifier() : pe.getAmplifier() + 1;
                    caster.removePotionEffect(PotionEffectType.HASTE);
                } else amp = 0;
                caster.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, (int) (TIME.modify(ch, lvl) * 20d), amp));
                EntityUtil.effect(ch.target(), Sound.BLOCK_CONDUIT_AMBIENT, amp * 0.5f, Particle.DRIPPING_LAVA);

                next(ch);
                return true;
            }
            public String id() {
                return "focus";
            }
            public String name() {
                return "Фокус";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Концентрирует аттаки пользователя, " + CLR + "ускоряя",
                TCUtil.N + "их " + CLR + "еффектом " + TCUtil.N + "с макс. силой в",
                TCUtil.N + EFFECT.id() + TCUtil.N + " и длительностью " + TIME.id() + " сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.WARRIOR;}
        };

        new Ability() {//Рипост
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DAMAGE};
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
                if (!(e instanceof EntityDamageByEntityEvent)) {
                    inform(ch, "Урон должен быть получен от сущности!");
                    return false;
                }

                Nms.swing(caster, EquipmentSlot.HAND);
                EntityUtil.effect(caster, Sound.ITEM_SHIELD_BREAK, 0.6f, Particle.SCRAPE);
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                fe.setDamage(DAMAGE.modify(ch, lvl) * e.getDamage());
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                defKBLe(caster, tgt, false);
                next(ch);
                return true;
            }
            public String id() {
                return "repost";
            }
            public String name() {
                return "Рипост";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Реагиует на " + CLR + "аттаку " + TCUtil.N + "по пользователю",
                TCUtil.N + "нанося " + DAMAGE.id() + "x " + TCUtil.N + "урона цели, в ответ"};
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

        new Ability() {//Триумф
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {TIME};
            public ChasMod[] stats() {
                return stats;
            }
            public Trigger trig() {
                return Trigger.KILL_ENTITY;
            }
            private final int amp = value("amp", 1);
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.trig() instanceof PlayerKillEntityEvent)) {
                    inform(ch, name() + " <red>должна следовать тригеру <u>"
                        + Trigger.KILL_ENTITY.disName());
                    return false;
                }
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                addEffect(caster, PotionEffectType.STRENGTH, TIME.modify(ch, lvl), amp, true);
                EntityUtil.effect(tgt, Sound.BLOCK_MUDDY_MANGROVE_ROOTS_BREAK, 0.6f, Particle.TRIAL_SPAWNER_DETECTION);
                next(ch);
                return true;
            }
            public String id() {
                return "triumph";
            }
            public String name() {
                return "Триумф";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Дает эффект " + CLR + "силы " + TCUtil.N + "пользователю,",
                TCUtil.N + "при убийстве цели, на " + TIME.id() + " сек"};
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



        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_DEALT, Chastic.DISTANCE};
            }
            public String id() {
                return "dmg_and_dst";
            }
            public String name() {
                return "Минера";
            }
            public ItemType icon() {
                return ItemType.BLADE_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.WARRIOR;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_TAKEN};
            }
            public String id() {
                return "shield_hurt";
            }
            public String name() {
                return "Шандре";
            }
            protected String needs() {
                return TCUtil.N + "Экипировка щита " + CLR + "пользователем";
            }
            public double modify(Chastic ch, double def, int lvl, @Nullable Chain info) {
                if (info == null || !InvCondition.SHIELD_OFF.test(info.caster().getEquipment())) return def;
                return super.modify(ch, def, lvl, info);
            }
            public ItemType icon() {
                return ItemType.HEARTBREAK_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return Role.WARRIOR;}
        };

    }
}
