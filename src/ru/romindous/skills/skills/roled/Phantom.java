package ru.romindous.skills.skills.roled;


import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.skills.Rarity;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.skills.abils.Bleeding;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.chas.ChasMod;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;

public class Phantom implements Scroll.Registerable {
    @Override
    public void register() {

        new Selector() {
            public String id() {
                return "caster_closest";
            }
            public String name() {
                return "Ближняя до Пользователя";
            }
            final ChasMod DIST = distChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST};
            }
            private final String[] desc = new String[]{
                TCUtil.N + "Сущность, наиболее " + CLR + "близкую " + TCUtil.N + "к",
                TCUtil.N + CLR + "пользователю" + TCUtil.N + ", не далее " + DIST.id + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.PHANTOM;}
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final LivingEntity le = LocUtil.getClsChEnt(loc, DIST.modify(ch, lvl),
                    LivingEntity.class, ent -> Main.canAttack(ch.caster(), ent, false));
                return le == null ? List.of() : List.of(le);
            }
        };

        new Ability() {//Диспульсия
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DIST, DAMAGE};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                if (!(ch.trig() instanceof final EntityDamageEvent e)
                    || e.getEntity().getEntityId() != caster.getEntityId()) {
                    inform(ch, name() + " должна следовать тригеру <u>"
                        + Trigger.USER_HURT.disName());
                    return false;
                }
                final LivingEntity cls = LocUtil.getClsChEnt(caster.getEyeLocation(), DIST.modify(ch, lvl), LivingEntity.class,
                    ent -> ent.getEntityId() != ch.target().getEntityId() && Main.canAttack(caster, ent, false));
                if (cls == null) {
                    inform(ch, "Не найдено следующей цели в радиусе!");
                    return false;
                }
                e.setDamage(0d);

                EntityUtil.effect(caster, Sound.ENCHANT_THORNS_HIT, 0.8f, Particle.ENCHANTED_HIT);

                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, cls);
                fe.setDamage(DAMAGE.modify(ch, lvl) * e.getDamage());
                next(ch, () -> {
                    cls.damage(fe.getDamage(), fe.getDamageSource());
                    defKBLe(caster, cls, false);
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
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            final ChasMod[] stats = new ChasMod[] {SPEED};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Vector vc = caster.getVelocity();
                final double dY = SPEED.modify(ch, lvl);
                caster.setVelocity(vc.setY(vc.getY() + dY));

                caster.getWorld().playSound(caster, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.4f);
                new ParticleBuilder(Particle.CLOUD).count((int) (dY * 16d)).offset(0.2d, 0.1d, 0.2d)
                    .location(caster.getLocation()).allPlayers().extra(0d).spawn();

                next(ch);
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
                if (!(caster instanceof final Player pl)) {
                    inform(ch, "Эта способность применяема только игроками!");
                    return false;
                }
                final GameMode gm = pl.getGameMode();
                pl.setGameMode(GameMode.SPECTATOR);

                new BukkitRunnable() {
                    int i = (int) (TIME.modify(ch, lvl) / secMul);
                    @Override
                    public void run() {
                        new ParticleBuilder(Particle.NAUTILUS).count(8).offset(0.2d, 0.2d, 0.2d)
                            .location(pl.getLocation()).allPlayers().extra(0d).spawn();
                        i--;

                        if (i == 0) {
                            if (pl.isValid()) {
                                pl.setGameMode(gm);
                                next(ch);
                            }
                            cancel();
                        }
                    }
                }.runTaskTimer(Main.main, 0, (long) (20d * secMul));

                EntityUtil.effect(caster, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.6f, Particle.NAUTILUS);
                return true;
            }
            public String id() {
                return "disloc";
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

                EntityUtil.effect(caster, Sound.ENTITY_WITCH_HURT, 0.6f, Particle.ANGRY_VILLAGER);

                final double time = TIME.modify(ch, lvl);
                next(ch, () -> {
                    addEffect(tgt, PotionEffectType.WEAKNESS, time, amp, true);
                    addEffect(tgt, PotionEffectType.SLOW_FALLING, time, amp, true);
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
                TCUtil.N + "Пугает указаную " + CLR + "цель" + TCUtil.N + ", откидывая ее",
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

        new Ability() {//Путаница
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod[] stats = new ChasMod[] {DIST};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.target() instanceof final Mob tgt)) {
                    inform(ch, "Запутать можно только монстров!");
                    return false;
                }
                final LivingEntity caster = ch.caster();
                final Location loc = caster.getLocation();
                final LivingEntity le = LocUtil.getClsChEnt(loc, DIST.modify(ch, lvl), LivingEntity.class,
                    ent -> Main.canAttack(caster, ent, false) && Main.canAttack(tgt, ent, false));
                if (le == null) {
                    inform(ch, "Не найдено следующей цели в радиусе!");
                    return false;
                }
                tgt.setTarget(le);

                EntityUtil.effect(caster, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.8f, Particle.CAMPFIRE_COSY_SMOKE);

                next(ch);
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
            final ChasMod[] stats = new ChasMod[] {TIME, DAMAGE};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();

                final Location loc = EntityUtil.center(tgt);
                new ParticleBuilder(Particle.SWEEP_ATTACK).extra(0d)
                    .allPlayers().location(loc).count(1).spawn();
                Bleeding.effect(tgt);

                final double dmg = DAMAGE.modify(ch, lvl);
                next(ch, () -> {
                    Bleeding.bleed(tgt, dmg,
                        TIME.modify(ch, lvl), caster);
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

        //притягивание к пользователю

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.MANA, Chastic.VELOCITY};
            }
            public String id() {
                return "mana_to_vel";
            }
            public String name() {
                return "Ксалина";
            }
            public ItemType icon() {
                return ItemType.FLOW_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.PHANTOM;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.REGENERATION};
            }
            public String id() {
                return "midair_regen";
            }
            public String name() {
                return "Треаден";
            }
            protected String needs() {
                return TCUtil.N + CLR + "Пользователь " + TCUtil.N + "находится в воздухе";
            }
            public double modify(Chastic ch, double def, int lvl, @Nullable Chain info) {
                if (info == null || !info.caster().isOnGround()) return def;
                return super.modify(ch, def, lvl, info);
            }
            public ItemType icon() {
                return ItemType.SHEAF_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return Role.PHANTOM;}
        };
    }
}
