package ru.romindous.skills.skills.abils.roled;


import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.objects.Bleeding;
import ru.romindous.skills.skills.ChasMod;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;

public class Assasin implements Ability.AbilReg {
    @Override
    public void register() {

        new Ability() {//Спурт
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod EFFECT = new ChasMod(this, "effect", Chastic.EFFECT);
            final ChasMod[] stats = new ChasMod[] {TIME, EFFECT};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(ch.on(this));
                addEffect(caster, PotionEffectType.SPEED, TIME.modify(chn, lvl), (int) Math.round(EFFECT.modify(chn, lvl)), true);
                EntityUtil.effect(caster, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.8f, Particle.REVERSE_PORTAL);

                next(chn);
                return true;
            }
            public String id() {
                return "spurt";
            }
            public String disName() {
                return "Спурт";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Концентрация дает " + CLR + "скорость " + TCUtil.N + "пользователю,",
                TCUtil.N + CLR + "уровня " + EFFECT.id + TCUtil.N + " (округляемо) на " + TIME.id + " сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SWORD;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.ASSASIN;}
        };

        /*new Ability() {//Заспинье
            final Stat TIME = new Stat("time", Chastic.TIME);
            final Stat[] stats = new Stat[] {TIME};
            protected Stat[] stats() {
                return stats;
            }
            final Trigger[] trigs = new Trigger[] {ATTACK_ENTITY};
            public Trigger[] triggers() {
                return trigs;
            }
            public Trigger finish() {
                return CAST_SELF;
            }
            private final int amp = value("amp", 1);
            public boolean cast(final EntityCastEvent ece, final int lvl, final Skill sk, final int next) {
                if (!(ch.event() instanceof final EntityDamageByEntityEvent ee)
                    || !(ee.getEntity() instanceof final LivingEntity target)) return false;
                final LivingEntity caster = ch.caster();
                EntityUtil.effect(caster, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.8f, Particle.PORTAL);
                addEffect(caster, PotionEffectType.HASTE, TIME.modify(ch, lvl), amp, true);
                caster.teleport(target.getLocation().subtract(target.getEyeLocation().getDirection()));
                if (target instanceof Mob) ((Mob) target).setTarget(null);

                EntityUtil.effect(caster, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.8f, Particle.REVERSE_PORTAL);

                next(ch.to(ece));
                return true;
            }
            public String id() {
                return "backtp";
            }
            public String disName() {
                return "Заспинье";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Телепорует пользователя за " + CLR + "спину " + TCUtil.N + "цели,",
                TCUtil.N + "и дает ему ускорение на " + TIME.id + " сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SWORD;
            }
            public Role role() {return Role.ASSASIN;}
        };*/

        /*new Ability() {//Разброс
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {DAMAGE};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final LivingEntity tgt, final int lvl) {
                final LivingEntity caster = ch.caster();
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                fe.setDamage(DAMAGE.modify(chn, lvl));
                tgt.damage(fe.getDamage(), fe.getDamageSource());
                defKBLe(caster, tgt, false);

                //TODO knife

                next(ch.event(fe));
                return true;
            }
            public String id() {
                return "knife";
            }
            public String disName() {
                return "Разброс";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Выбрасывает " + CLR + "нож " + TCUtil.N + "в сторону цели,",
                TCUtil.N + "нанося " + DAMAGE.id + " ед. " + TCUtil.N + "урона"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SWORD_BOTH;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.ASSASIN;}
        };*/

        new Ability() {//Проворство
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {TIME};
            protected ChasMod[] stats() {
                return stats;
            }
            private final int amp = value("amp", 1);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                if (!(ch.event() instanceof final EntityDamageEvent ee
                    && ee.getEntity().getEntityId() == caster.getEntityId())) return false;
                if (!caster.hasPotionEffect(PotionEffectType.INVISIBILITY)) return false;
                caster.removePotionEffect(PotionEffectType.INVISIBILITY);
                final Chain chn = ch.event(ch.on(this));
                addEffect(caster, PotionEffectType.SPEED, TIME.modify(chn, lvl), amp, false);
                EntityUtil.effect(caster, Sound.ENTITY_PLAYER_ATTACK_WEAK, 0.6f, Particle.CAMPFIRE_COSY_SMOKE);

                next(chn);
                return true;
            }
            public String id() {
                return "evasion";
            }
            public String disName() {
                return "Проворство";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Дает ускорение на " + TIME.id + "сек. " + TCUtil.N + " при",
                TCUtil.N + "получении " + CLR + "урона, " + TCUtil.N + "снимая невидимость"};
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
            public Role role() {return Role.ASSASIN;}
        };

        new Ability() {//Дымовуха
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {DIST, TIME};
            protected ChasMod[] stats() {
                return stats;
            }
            private final int amp = value("amp", 1);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                if (tgt instanceof Mob) ((Mob) tgt).setTarget(null);
                final EntityDamageByEntityEvent fe = makeDamageEvent(caster, tgt);
                final Chain chn = ch.event(fe);
                addEffect(tgt, PotionEffectType.BLINDNESS, TIME.modify(chn, lvl), amp, false);
                //TODO effect

                next(chn);
                return true;
            }
            public String id() {
                return "smoke";
            }
            public String disName() {
                return "Дымовуха";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Выпускает дымовую занавесь, которая",
                TCUtil.N + "дезориентирует цели в радиусе " + DIST.id + " бл.",
                TCUtil.N + "на " + TIME.id + " сек."};
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
            public Role role() {return Role.ASSASIN;}
        };

        new Ability() {//Занавес
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {TIME};
            protected ChasMod[] stats() {
                return stats;
            }
            private final int amp = value("amp", 1);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(ch.on(this));
                addEffect(caster, PotionEffectType.INVISIBILITY, TIME.modify(chn, lvl), amp, false);
                EntityUtil.effect(caster, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.6f, Particle.CAMPFIRE_COSY_SMOKE);

                next(chn);
                return true;
            }
            public String id() {
                return "hide";
            }
            public String disName() {
                return "Занавес";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Прячет чародея в тенях, давая",
                TCUtil.N + "ему " + TIME.id + " сек. " + TCUtil.N + "невидимости"};
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
            public Role role() {return Role.ASSASIN;}
        };

        new Ability() {//Порез
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {TIME};
            protected ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();
                final Chain chn = ch.event(makeDamageEvent(caster, tgt));
                Bleeding.bleed(tgt, DAMAGE.modify(chn, lvl),
                    TIME.modify(chn, lvl), caster);
                Bleeding.effect(tgt);

                next(chn);
                return true;
            }
            public String id() {
                return "slice";
            }
            public String disName() {
                return "Порез";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Атакуемая цель будет истекать " + CLR + "кровью",
                TCUtil.N + "на " + TIME.id + " сек, " + TCUtil.N + "получая",
                DAMAGE.id + " ед. " + TCUtil.N + "урона каждую секунду"};
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
            public Role role() {return Role.ASSASIN;}
        };

        //teleport
        //hit a diff mob when hurt
    }
}
