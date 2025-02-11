package ru.romindous.skills.skills.roled;


import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.skills.Rarity;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.events.EntityCastEvent;
import ru.romindous.skills.listeners.ShotLst;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.chas.ChasMod;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;

public class Archer implements Scroll.Registerable {
    @Override
    public void register() {

        new Selector() {
            public String id() {
                return "forward_closest";
            }
            public String name() {
                return "Сущность Позади";
            }
            final ChasMod DIST = distChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST};
            }
            private final String[] desc = new String[]{
                TCUtil.N + "Сущность, с " + CLR + "обратной " + TCUtil.N + "стороны",
                TCUtil.N + CLR + "цели" + TCUtil.N + ", не далее " + DIST.id + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.ARCHER;}
            private final double arc = value("arc", 0.6d);
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final LivingEntity le = getClsArcLent(loc, DIST.modify(ch, lvl), arc,
                    ent -> Main.canAttack(ch.caster(), ent, false));
                return le == null ? List.of() : List.of(le);
            }
        };

        new Ability() {//Флэшка
            final ChasMod POWER = new ChasMod(this, "power", Chastic.EFFECT);
            public ChasMod[] stats() {
                return new ChasMod[] {POWER};
            }
            private final double timeMul = value("timeMul", 20d);
            private static final FireworkEffect FW_EFF = FireworkEffect.builder()
                .withColor(Color.YELLOW).with(FireworkEffect.Type.BURST).build();
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Firework fw = shoot(caster, Firework.class);
                final Chain chn = ch.event(new ProjectileLaunchEvent(fw));
//                fw.setVelocity(tgt.getLocation().subtract(caster.getEyeLocation())
//                    .toVector().normalize().multiply(SPEED.modify(chn, lvl)));
                final FireworkMeta fm = fw.getFireworkMeta();
                final double dmg = POWER.modify(chn, lvl);
                fm.addEffect(FW_EFF);
                fw.setFireworkMeta(fm);
                fw.setTicksToDetonate((int) (dmg * timeMul));
                ShotLst.damage(fw, dmg);

                next(chn);
                return true;
            }
            public String id() {
                return "flash";
            }
            public String name() {
                return "Флэшка";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Запускает " + CLR + "феерверк " + TCUtil.N + "со ",
                TCUtil.N + "средним уроном в " + POWER.id + " ед."};
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
            public Role role() {return Role.ARCHER;}
        };

        new Ability() {//Спектраль
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            public ChasMod[] stats() {
                return new ChasMod[] {SPEED};
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.event() instanceof final ProjectileLaunchEvent ee)) {
                    inform(ch, "Этой способности нужен тригер: "
                        + Trigger.PROJ_LAUNCH.disName());
                    return false;
                }
                if (!(ee.getEntity() instanceof final AbstractArrow ar)) {
                    inform(ch, "Снаряд должен быть стрелой!");
                    return false;
                }
                final LivingEntity caster = ch.caster();
                final SpectralArrow spa = shoot(caster, SpectralArrow.class);
                spa.setShooter(caster);
                final ItemStack wpn = ar.getWeapon();
                if (wpn != null) spa.setWeapon(wpn);
                final Chain chn = ch.event(new ProjectileLaunchEvent(spa));
                spa.setVelocity(ar.getVelocity().multiply(SPEED.modify(chn, lvl)));
                EntityUtil.effect(caster, Sound.ITEM_CROSSBOW_SHOOT, 1.4f, Particle.LANDING_HONEY);

                next(ch.event(new ProjectileLaunchEvent(spa)));
                return true;
            }
            public String id() {
                return "spectral";
            }
            public String name() {
                return "Спектраль";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает спектральную " + CLR + "копию " + TCUtil.N + "при выстреле стрел,",
                TCUtil.N + "со скоростью равной " + SPEED.id + "x " + TCUtil.N + "оригинала"};
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
            public Role role() {return Role.ARCHER;}
        };

        new Ability() {//Рикошет
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod RATIO = new ChasMod(this, "ratio", Chastic.VELOCITY);
            public ChasMod[] stats() {
                return new ChasMod[] {DIST, RATIO};
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.event() instanceof final ProjectileHitEvent ee)) {
                    inform(ch, "Этой способности нужен тригер: "
                        + Trigger.RANGED_HIT.disName());
                    return false;
                }
                if (!(ee.getEntity() instanceof final AbstractArrow ae)) {
                    inform(ch, "Снаряд должен быть стрелой!");
                    return false;
                }
                final LivingEntity target = ch.target();
                final LivingEntity tgt = LocUtil.getClsChEnt(ae.getLocation(), DIST.modify(ch, lvl),
                    LivingEntity.class, ent -> ent.getEntityId() != target.getEntityId()
                        && Main.canAttack(ch.caster(), ent, false));
                if (tgt == null) {
                    inform(ch, "Не найдено следующей цели в радиусе!");
                    return false;
                }
                if (ae.getPierceLevel() == 0) ae.setPierceLevel(1);
                final EntityCastEvent ece = ch.on(this);
                final Location tlc = EntityUtil.center(target);
                final Vector dir = EntityUtil.center(tgt).subtract(tlc).toVector().normalize();
                tlc.add(dir); tlc.setY(ae.getLocation().getY()); ae.teleport(tlc);
                ae.setVelocity(getArrowVc(dir, ae.getVelocity().length() * RATIO.modify(ch, lvl)));
                EntityUtil.effect(tgt, Sound.BLOCK_CALCITE_BREAK, 1.4f, Particle.ELECTRIC_SPARK);

                next(ch.event(ece));
                return true;
            }
            private Vector getArrowVc(final Vector dst, final double spd) {
                final double DlnSq = 0.01d / spd;
                if (dst.getY() > -DlnSq) dst.setY((dst.getY() + 1d) * DlnSq);
                return dst.multiply(spd);
            }
            public String id() {
                return "ricochet";
            }
            public String name() {
                return "Рикошет";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Позволяет стреле срикошетить при " + CLR + "попадании",
                TCUtil.N + "в моба, перенося " + RATIO.id + "x " + TCUtil.N + "скорости,",
                TCUtil.N + "если след. цель в радиусе " + DIST.id + " бл."};
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
            public Role role() {return Role.ARCHER;}
        };

        new Ability() {//Жаунт
            final ChasMod HUNGER = new ChasMod(this, "hunger", Chastic.NUTRITION);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            public ChasMod[] stats() {
                return new ChasMod[] {DAMAGE};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                caster.teleport(tgt.getEyeLocation().add(caster.getEyeLocation().getDirection()));
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                if (caster instanceof final HumanEntity he) {
                    final int fdl = he.getFoodLevel() - (int) Math.round(HUNGER.modify(chn, lvl));
                    if (fdl < 0) {
                        inform(ch, "Не хватает сытости для прыжка!");
                        return false;
                    }
                    he.setFoodLevel(fdl);
                }

                EntityUtil.effect(tgt, Sound.ENTITY_FOX_TELEPORT, 0.8f, Particle.REVERSE_PORTAL);

                next(chn, () -> {
                    tgt.damage(fe.getDamage(), fe.getDamageSource());
                    tgt.setNoDamageTicks(0);
                });
                return true;
            }
            public String id() {
                return "jounte";
            }
            public String name() {
                return "Жаунт";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Использует " + HUNGER.id + " ед. " + TCUtil.N + " сытости (округляемо),",
                TCUtil.N + "пользователя, телепортируя его за " + CLR + "спину ",
                TCUtil.N + "цели, и нанося " + DAMAGE.id + " ед. " + TCUtil.N + "урона"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.BOW_ANY;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.ARCHER;}
        };

        new Ability() {//Дострел
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            public ChasMod[] stats() {
                return new ChasMod[] {DAMAGE};
            }
            private final ItemStack FWS = new ItemBuilder(ItemType.FIREWORK_STAR)
                .color(Color.ORANGE).build();
            private final double height = value("height", 12d);
            private final double speed = value("speed", 1d);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final Location start = caster.getEyeLocation().add(0d, height, 0d);
                final Snowball sb = caster.getWorld().spawn(start, Snowball.class, s -> {
                    s.setVelocity(EntityUtil.center(tgt).subtract(start)
                        .toVector().normalize().multiply(speed));
                    s.setItem(FWS); s.setGravity(false);
                });
                final Chain chn = ch.event(new ProjectileLaunchEvent(sb));
                ShotLst.damage(sb, Math.sqrt(DAMAGE.modify(chn, lvl)));
                EntityUtil.effect(sb, Sound.ENTITY_DROWNED_SHOOT, 1.4f, Particle.SQUID_INK);

                next(chn);
                return true;
            }
            public String id() {
                return "refire";
            }
            public String name() {
                return "Дострел";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Создает снаряд над пользователем, при " + CLR + "ударе",
                TCUtil.N + "моба, нанося " + DAMAGE.id + " ед. " + TCUtil.N + "урона при попадании"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.FIST_OFF;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.ARCHER;}
        };

        //pull to other near target
        //melee bow fire
        //arrow circle on mob death
        //обсьрел

        //no gravity charged shot
        //shulker bullets on mob death

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.COOLDOWN, Chastic.VELOCITY};
            }
            public String id() {
                return "cd_to_vel";
            }
            public String name() {
                return "Локеан";
            }
            public ItemType icon() {
                return ItemType.FLOW_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.ARCHER;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_DEALT};
            }
            public String id() {
                return "ranged_dmg";
            }
            public String name() {
                return "Назиел";
            }
            private final double dst = value("distance", 10);
            protected String needs() {
                return TCUtil.N + "Расстояние более " + CLR + dst + " бл. " + TCUtil.N + "до цели";
            }
            public double modify(Chastic ch, double def, int lvl, @Nullable Chain info) {
                if (info == null || info.caster().getLocation()
                    .distanceSquared(info.target().getLocation()) < dst * dst) return def;
                return super.modify(ch, def, lvl, info);
            }
            public ItemType icon() {
                return ItemType.ARCHER_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return Role.ARCHER;}
        };
    }
}
