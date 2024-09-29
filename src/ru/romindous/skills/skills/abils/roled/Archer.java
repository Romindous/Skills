package ru.romindous.skills.skills.abils.roled;


import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.events.EntityCastEvent;
import ru.romindous.skills.listeners.ShotLst;
import ru.romindous.skills.skills.ChasMod;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;

public class Archer implements Ability.AbilReg {
    @Override
    public void register() {
        new Ability() {//Флэшка
            final ChasMod POWER = new ChasMod(this, "power", Chastic.EFFECT);
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            protected ChasMod[] stats() {
                return new ChasMod[] {POWER};
            }
            private final double delPow = value("delPow", 20d);
            private final FireworkEffect fe = FireworkEffect.builder()
                .withColor(Color.YELLOW).with(FireworkEffect.Type.BURST).build();
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final Firework fw = shoot(caster, Firework.class);
                final Chain chn = ch.event(new ProjectileLaunchEvent(fw));
                fw.setShotAtAngle(true);
                fw.setVelocity(tgt.getLocation().subtract(caster.getEyeLocation())
                    .toVector().normalize().multiply(SPEED.modify(chn, lvl)));
                final FireworkMeta fm = fw.getFireworkMeta();
                final double dmg = POWER.modify(chn, lvl);
                fm.addEffect(fe);
                fw.setFireworkMeta(fm);
                fw.setTicksToDetonate((int) (dmg * delPow));
                ShotLst.damage(fw, dmg);

                next(chn);
                return true;
            }
            public String id() {
                return "flash";
            }
            public String disName() {
                return "Флэшка";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Запускает " + CLR + "феерверк " + TCUtil.N + "со средним",
                TCUtil.N + "уроном в " + POWER.id + " ед. " + TCUtil.N + "и",
                TCUtil.N + "скоростью в " + SPEED.id + " бл./сек."};
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

        /*new Ability() {//Обстрел
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DIST};
            protected ChasMod[] stats() {
                return stats;
            }
            private final double range = value("range", 12d);
            private final int delay = value("delay", 12);
            private final ItemStack arr = new ItemStack(Material.ARROW);
            public boolean cast(final EntityCastEvent ece, final int lvl, final Skill sk, final int next) {
                final LivingEntity caster = ch.caster();
                final Location loc = ece.getLocation();
                final double dst = DIST.modify(ch, lvl);
                final Vector dir = caster.getEyeLocation().getDirection()
                    .setY(0d).normalize().multiply(range).setY(-range);

                final Location arrLoc = loc.subtract(dir);
                arrLoc.setPitch(45f); arrLoc.setYaw(0f);
                final ItemDisplay[] prjs = new ItemDisplay[(int) (dst * dst)];
                for (int i = 0; i != prjs.length; i++) {
                    prjs[i] = arrLoc.getWorld().spawn(arrLoc.clone()
                        .add(ApiOstrov.rndSignNum(0, (int) dst), Ostrov.random.nextDouble() - 0.5d,
                            ApiOstrov.rndSignNum(0, (int) dst)), ItemDisplay.class, id -> {
                        id.setBillboard(Display.Billboard.VERTICAL);
                        id.setTransformation(new Transformation(new Vector3f(),
                            new AxisAngle4f(0.45f, 0f, 0f, 1f),
                            new Vector3f(), new AxisAngle4f(0f, 0f, 0f, 0f)));
                        id.setItemStack(arr);
                    });
                }

                final Vector ndir = dir.clone().normalize();
                new BukkitRunnable() {
                    int i = delay;
                    @Override
                    public void run() {
                        i--;
                        for (final ItemDisplay pd : prjs) {
                            if (!pd.isValid()) continue;
                            pd.teleport(pd.getLocation().add(ndir));
                        }
                        if (i == 0) {
                            final List<Event> evs = new ArrayList<>();
                            for (final LivingEntity le : LocUtil.getChEnts(loc, dst,
                                LivingEntity.class, ent -> Main.canAttack(caster, ent, false))) {
                                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, le);
                                fe.setDamage(DAMAGE.modify(chn, lvl));
                                le.damage(fe.getDamage(), fe.getDamageSource());
                                defKBLe(caster, le, false);
                                evs.add(fe);
                            }

                            loc.getWorld().playSound(loc, Sound.ITEM_CROSSBOW_HIT, 4f, 0.6f);
                            for (final ItemDisplay pd : prjs) pd.remove();

                            Ostrov.sync(() -> {
                                for (final Event fe : evs)
                                    sk.step(finish(), fe, next);
                            }, stepCd);
                            cancel();
                        }
                    }
                }.runTaskTimer(Main.main, 1, 1);
                return true;
            }
            public String id() {
                return "volley";
            }
            public String disName() {
                return "Обстрел";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Призывает волну стрел, поражающих территорию",
                TCUtil.N + "врадиусе " + DIST.id + " бл." + TCUtil.N + " и нанося",
                TCUtil.N + "целям " + DAMAGE.id + " ед." + TCUtil.N + " урона"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.BOW_ANY;
            }
            public Role role() {return Role.ARCHER;}
        };*/

        new Ability() {//Спектраль
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            protected ChasMod[] stats() {
                return new ChasMod[] {SPEED};
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.event() instanceof final ProjectileLaunchEvent ee)
                    || !(ee.getEntity() instanceof final AbstractArrow ar)) return false;
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
            public String disName() {
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
            protected ChasMod[] stats() {
                return new ChasMod[] {DIST, RATIO};
            }
            public boolean cast(final Chain ch, final int lvl) {
                if (!(ch.event() instanceof final ProjectileHitEvent ee)
                    || !(ee.getEntity() instanceof final AbstractArrow ae)
                    || !(ee.getHitEntity() instanceof final LivingEntity le)) return false;
                final LivingEntity tgt = LocUtil.getClsChEnt(ae.getLocation(), DIST.modify(ch, lvl),
                    LivingEntity.class, ent -> ent.getEntityId() != le.getEntityId() && Main.canAttack(ch.caster(), ent, false));
                if (tgt == null) return false;
                if (ae.getPierceLevel() == 0) ae.setPierceLevel(1);
                final EntityCastEvent ece = ch.on(this);
                final Location tlc = tgt.getEyeLocation();
                final Vector dir = tgt.getEyeLocation().subtract(tlc).toVector();
                final Vector ndir = dir.clone().normalize();
                tlc.add(ndir); tlc.setY(ae.getLocation().getY());
                ae.teleport(tlc);
                ae.setVelocity(getArrowVc(ndir, ae.getVelocity().length() * RATIO.modify(ch, lvl)));
                EntityUtil.effect(tgt, Sound.BLOCK_CALCITE_BREAK, 1.4f, Particle.ELECTRIC_SPARK);

                next(ch.event(ece));
                return true;
            }
            private Vector getArrowVc(final Vector dst, final double spd) {
                final double DlnSq = dst.lengthSquared() * 0.01d / spd;
                if (dst.getY() > -DlnSq) dst.setY((dst.getY() + 1d) * DlnSq);
                return dst.normalize().multiply(spd);
            }
            public String id() {
                return "ricochet";
            }
            public String disName() {
                return "Рикошет";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Позволяет стреле срикошетить при " + CLR + "попадании",
                TCUtil.N + "в моба, перенося " + RATIO.id + "x " + TCUtil.N + "скорости"};
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
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            protected ChasMod[] stats() {
                return new ChasMod[] {DAMAGE};
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                caster.teleport(tgt.getEyeLocation().add(caster.getEyeLocation().getDirection()));
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                tgt.damage(fe.getDamage());
                tgt.setNoDamageTicks(0);
                EntityUtil.effect(tgt, Sound.ENTITY_FOX_TELEPORT, 0.8f, Particle.REVERSE_PORTAL);

                next(chn);
                return true;
            }
            public String id() {
                return "jounte";
            }
            public String disName() {
                return "Жонт";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Телепортирует за " + CLR + "спину " + TCUtil.N + "цели,",
                TCUtil.N + "нанося " + DAMAGE.id + " ед. " + TCUtil.N + "урона"};
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
            protected ChasMod[] stats() {
                return new ChasMod[] {DAMAGE};
            }
            private final double height = value("height", 12d);
            private final double speed = value("speed", 1d);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final Location start = caster.getEyeLocation().add(0d, height, 0d);
                final Snowball sb = caster.getWorld().spawn(start, Snowball.class);
                final Chain chn = ch.event(new ProjectileLaunchEvent(sb));
                sb.setVelocity(EntityUtil.center(tgt).subtract(start)
                    .toVector().normalize().multiply(speed));
                sb.setItem(ItemType.FIREWORK_STAR.createItemStack());
                sb.setGravity(false);
                ShotLst.damage(sb, Math.sqrt(DAMAGE.modify(chn, lvl)));
                EntityUtil.effect(sb, Sound.ENTITY_DROWNED_SHOOT, 1.4f, Particle.SQUID_INK);

                next(chn);
                return true;
            }
            public String id() {
                return "refire";
            }
            public String disName() {
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

        //no gravity charged shot
        //shulker bullets on mob death
    }
}
