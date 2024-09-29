package ru.romindous.skills.skills.abils.roled;


import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.skills.ChasMod;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;

public class Stoner implements Ability.AbilReg {
    @Override
    public void register() {

        /*new Ability() {//Пульсар
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DAMAGE};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                defKBLe(caster, tgt, true);

                //TODO effect

                next(chn);
                return true;
            }
            public String id() {
                return "pulsar";
            }
            public String disName() {
                return "Пульсар";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Отталкивает цель от пользователя, нанося ей",
                TCUtil.N + DAMAGE.id + " ед. " + TCUtil.N + "урона"};
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
        };*/

        new Ability() {//Толчок
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DAMAGE};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                if (!tgt.isOnGround()) return false;
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                defKBLe(caster, tgt, true);

                //TODO effect

                next(chn);
                return true;
            }
            public String id() {
                return "bump";
            }
            public String disName() {
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
            protected ChasMod[] stats() {
                return stats;
            }
            private final int amp = value("amp", 1);
            private static final BlockData bd = BlockType.SPONGE.createBlockData();
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(ch.on(this));
                final double abs = caster.getAbsorptionAmount() + HEAL.modify(chn, lvl);
                final AttributeInstance ain = caster.getAttribute(Attribute.GENERIC_MAX_ABSORPTION);
                if (ain == null) return false;
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
            public String disName() {
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

        /*new Ability() {//Поток
            final Stat DIST = new Stat("dist", Chastic.DISTANCE);
            final Stat TIME = new Stat("time", Chastic.TIME);
            final Stat[] stats = new Stat[] {DIST, TIME};
            protected Stat[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {SHIFT_RIGHT, SHIFT_LEFT, CAST_SELF};
            public Trigger[] triggers() {
                return trigs;
            }
            public Trigger finish() {
                return ATTACK_ENTITY;
            }
            private final double thick = value("thick", 0.2d);
            private final double step = value("step", 1d);
            private final int amp = value("amp", 8);
            public boolean cast(final EntityCastEvent ece, final int lvl, final Skill sk, final int next) {
                final LivingEntity caster = ch.caster();
                final Location start = caster.getEyeLocation();
                final Vector dir = start.getDirection();
                final double dst = DIST.modify(ch, lvl);
                dir.multiply(step);
                final ArrayList<Event> evs = new ArrayList<>();
                final int time = (int) (TIME.modify(ch, lvl) * 20d);

                for (final LivingEntity le : LocUtil.getChEnts(start, dst, LivingEntity.class, ent -> {
                        if (!Main.canAttack(caster, ent, false)) return false;
                        final Location lc = ent.getLocation();
                        final Location dlc = lc.subtract(start);
                        final double ln = Math.sqrt(NumberConversions.square(dlc.getX()) + NumberConversions.square(dlc.getZ()));
                        if (ln > dst || NumberConversions.square(-Math.sin(Math.toRadians((180f - start.getYaw()))) - dlc.getX() / ln) +
                        NumberConversions.square(-Math.cos(Math.toRadians((180f - start.getYaw()))) - dlc.getZ() / ln) > thick / (ln * ln)) return false;
                        final double pty = start.getY() + Math.tan(Math.toRadians(-start.getPitch())) * ln - lc.getY();
                        return pty < ent.getHeight() && pty > 0d;
                    })) {

                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, time, amp, true, false, false));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, time, amp, true, false, false));
                    evs.add(makeDamageEvent(caster, le));
                }

                //TODO effect

                Ostrov.sync(() -> {
                    for (final Event fe : evs)
                        sk.step(finish(), fe, next);
                }, stepCd);
                return true;
            }
            public String id() {
                return "beam";
            }
            public String disName() {
                return "Поток";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Выстреливает " + CLR + "луч" + TCUtil.N + ", разрывающий пространство",
                TCUtil.N + "с дальностью " + DIST.id + " бл." + TCUtil.N + ", дизориентируя",
                TCUtil.N + "каждую подбитую цель на " + TIME.id + " сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.STAFF_ANY;
            }
            public Role role() {return Role.STONER;}
        };*/

        /*new Ability() {//Волна
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DIST, DAMAGE};
            protected ChasMod[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {CAST_SELF, ATTACK_ENTITY, KILL_ENTITY, PROJ_LAUNCH};
            public Trigger[] selectors() {
                return trigs;
            }
            private Trigger finish() {
                return Trigger.ATTACK_ENTITY;
            }
            private final double step = value("step", 1d);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Location start = ece.getLocation();
                final Location eye = caster.getEyeLocation();
                final Vector dvc = start.subtract(eye).toVector();
                final Vector dir = dvc.lengthSquared() > 1d ? dvc.normalize() : eye.getDirection();

                final Set<Integer> entIds = new HashSet<>();
                final double dst = DIST.modify(ch, lvl);
                final LivingEntity[] les = LocUtil.getChEnts(start, dst, LivingEntity.class,
                    ent -> Main.canAttack(caster, ent, false)).toArray(new LivingEntity[0]);
                dir.multiply(step);
                new BukkitRunnable() {
                    double d = 0;
                    @Override
                    public void run() {
                        d += step;
                        start.add(dir);

                        final double ds2 = d * d * 0.25d;
                        final ArrayList<Event> evs = new ArrayList<>();
                        for (final LivingEntity le : les) {
                            final Location elc = le.getEyeLocation();
                            if (entIds.contains(le.getEntityId()) || elc.distanceSquared(start) > ds2) continue;
                            entIds.add(le.getEntityId());

                            final EntityDamageByEntityEvent fe = makeDamageEvent(caster, le);
                            fe.setDamage(DAMAGE.modify(chn, lvl));
                            le.damage(fe.getDamage(), fe.getDamageSource());
                            defKBLe(caster, le, true);
                            evs.add(fe);
                        }

                        //TODO effect

                        Ostrov.sync(() -> {
                            for (final Event fe : evs)
                                sk.step(finish(), fe, next);
                        }, stepCd);
                        if (d > dst) cancel();
                    }
                }.runTaskTimer(Main.main, 0, 4);
                return true;
            }
            public String id() {
                return "wave";
            }
            public String disName() {
                return "Волна";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Подбрасывает блоки и цели " + CLR + "волной",
                TCUtil.N + "с дальностью " + DIST.id + " бл." + TCUtil.N + ", нанося",
                TCUtil.N + "каждому " + DAMAGE.id + " ед. " + TCUtil.N + "урона"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.AXE;
            }
            public Role role() {return Role.STONER;}
        };*/

        new Ability() {//Подскок
            final ChasMod SPEED = new ChasMod(this, "speed", Chastic.VELOCITY);
            final ChasMod[] stats = new ChasMod[] {SPEED};
            protected ChasMod[] stats() {
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
            public String disName() {
                return "Подскок";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Позволяет совершить " + CLR + "подскок",
                TCUtil.N + "пользователю со скоростью в " + SPEED.id + " блок./сек.",
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
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(makeDamageEvent(caster, tgt));
                addEffect(tgt, PotionEffectType.SLOWNESS, TIME.modify(chn, lvl),
                    (int) Math.round(EFFECT.modify(chn, lvl)), true);

                if (!tgt.isOnGround()) return true;
                final Location fin = tgt.getLocation().add(0d, -0.45d, 0d);
                tgt.teleport(fin);
                EntityUtil.effect(tgt, Sound.BLOCK_GILDED_BLACKSTONE_BREAK,
                    0.8f, Particle.DUST_PILLAR, fin.getBlock().getBlockData());

                next(chn);
                return true;
            }
            public String id() {
                return "pinch";
            }
            public String disName() {
                return "Прищемление";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Замщемляет цель в " + CLR + "окружающей " + TCUtil.N + "среде,",
                TCUtil.N + "погребая ее и давая замедление " + EFFECT.id + " ур.",
                TCUtil.N + "(округляемо), на " + TIME.id + " сек."};
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
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                if (!(ch.event() instanceof final EntityDamageEvent ee
                    && ee.getEntity().getEntityId() == caster.getEntityId())) return false;
                final LivingEntity tgt = ch.target();
                final EntityDamageEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                defKBLe(caster, tgt, false);

                EntityUtil.effect(caster, Sound.ENCHANT_THORNS_HIT, 0.8f, Particle.ENCHANTED_HIT);

                next(chn);
                return true;
            }
            public String id() {
                return "thorns";
            }
            public String disName() {
                return "Зубчатость";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "При получении урона, шипы на " + CLR + "чешуе",
                TCUtil.N + "пользователя, в ответ наносят " + DAMAGE.id + " ед."};
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
    }
}
