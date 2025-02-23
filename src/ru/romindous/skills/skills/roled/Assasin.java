package ru.romindous.skills.skills.roled;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
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
import ru.romindous.skills.survs.Role;

public class Assasin implements Scroll.Regable {
    @Override
    public void register() {

        new Selector() {
            public String id() {
                return "less_hp_circle";
            }
            public String name() {
                return "Раненые Вокруг";
            }
            final ChasMod DIST = distChMod();
            final ChasMod AMT = amtChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST, AMT};
            }
            private final String[] desc = new String[]{
                TCUtil.N + "Сущности вокруг, со " + CLR + "здоровьем" + TCUtil.N + ", менее",
                TCUtil.N + "чем у " + CLR + "цели" + TCUtil.N + ", не далее " + DIST.id() + " бл.",
                TCUtil.N + "Лимит - " + AMT.id() + " сущ. (округляемо)"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.ASSASIN;}
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final double hp = ch.target().getHealth();
                final Collection<LivingEntity> chEnts = LocUtil.getChEnts(loc, DIST.modify(ch, lvl),
                    LivingEntity.class, ent -> ent.getHealth() < hp && Main.canAttack(ch.caster(), ent, false));
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

        new Ability() {//Спурт
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod EFFECT = new ChasMod(this, "effect", Chastic.EFFECT);
            final ChasMod[] stats = new ChasMod[] {TIME, EFFECT};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                addEffect(caster, PotionEffectType.SPEED, TIME.modify(ch, lvl), (int) Math.round(EFFECT.modify(ch, lvl)), true);
                EntityUtil.effect(caster, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.8f, Particle.REVERSE_PORTAL);
                next(ch);
                return true;
            }
            public String id() {
                return "spurt";
            }
            public String name() {
                return "Спурт";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Концентрация дает " + CLR + "скорость " + TCUtil.N + "пользователю,",
                TCUtil.N + CLR + "уровня " + EFFECT.id() + TCUtil.N + " (округляемо) на " + TIME.id() + " сек."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public InvCondition equip() {
                return InvCondition.SWORD_BOTH;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.ASSASIN;}
        };

        new Ability() {//Заспинье
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod[] stats = new ChasMod[] {DIST};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster(), target = ch.target();
                final Location tlc = EntityUtil.center(target);
                final double dst = DIST.modify(ch, lvl);
                if (tlc.distanceSquared(EntityUtil.center(caster)) > dst * dst) {
                    inform(ch, "Сущность недостаточно близко!");
                    return false;
                }
                EntityUtil.effect(caster, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.8f, Particle.PORTAL);
                EntityUtil.effect(caster, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.8f, Particle.REVERSE_PORTAL);

                caster.teleport(tlc.subtract(target.getEyeLocation().getDirection().multiply(2d)));
                if (target instanceof Mob) ((Mob) target).setTarget(null);
                next(ch);
                return true;
            }
            public String id() {
                return "backtp";
            }
            public String name() {
                return "Заспинье";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Телепорует пользователя за " + CLR + "спину",
                TCUtil.N + "цели, если она ближе " + DIST.id() + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public boolean selfCast() {return false;}
            public Role role() {return Role.ASSASIN;}
        };

        new Ability() {//Проворство
            final ChasMod EFFECT = new ChasMod(this, "effect", Chastic.EFFECT);
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod[] stats = new ChasMod[] {EFFECT, TIME};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                if (!caster.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    inform(ch, "Необходимо иметь невидимость!");
                    return false;
                }
                caster.removePotionEffect(PotionEffectType.INVISIBILITY);
                addEffect(caster, PotionEffectType.HASTE, TIME.modify(ch, lvl),
                    (int) Math.round(EFFECT.modify(ch, lvl)), false);

                EntityUtil.effect(caster, Sound.ENTITY_PLAYER_ATTACK_WEAK, 0.6f, Particle.CAMPFIRE_COSY_SMOKE);

                next(ch);
                return true;
            }
            public String id() {
                return "evasion";
            }
            public String name() {
                return "Проворство";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Дает спешку " + EFFECT.id() + " ур. " + TCUtil.N + "(округляемо) на " + TIME.id() + " сек.",
                TCUtil.N + "взамен на снятие" + CLR + "невидимости " + TCUtil.N + "у пользователя"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public boolean selfCast() {return true;}
            public Role role() {return Role.ASSASIN;}
        };

        new Ability() {//Занавес
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod DIST = new ChasMod(this, "dist", Chastic.DISTANCE);
            final ChasMod[] stats = new ChasMod[] {TIME, DIST};
            public ChasMod[] stats() {
                return stats;
            }
            private final int amp = value("amp", 1);
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity caster = ch.caster();
                addEffect(caster, PotionEffectType.INVISIBILITY, TIME.modify(ch, lvl), amp, false);

                EntityUtil.effect(caster, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.6f, Particle.CAMPFIRE_COSY_SMOKE);

                final double dst = DIST.modify(ch, lvl);
                for (final Mob mb : LocUtil.getChEnts(caster.getLocation(), dst, Mob.class, m -> {
                    final LivingEntity tgt = m.getTarget();
                    return tgt != null && tgt.getEntityId() == caster.getEntityId();
                })) mb.setTarget(null);
                next(ch);
                return true;
            }
            public String id() {
                return "cloak";
            }
            public String name() {
                return "Занавес";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Прячет чародея в тенях, давая",
                TCUtil.N + "ему " + TIME.id() + " сек. " + TCUtil.N + "невидимости,",
                TCUtil.N + "и отвлекая сущности ближе " + DIST.id() + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public boolean selfCast() {return true;}
            public InvCondition equip() {
                return InvCondition.FIST_ANY;
            }
            public Role role() {return Role.ASSASIN;}
        };

        new Ability() {//Порез
            final ChasMod TIME = new ChasMod(this, "time", Chastic.TIME);
            final ChasMod DAMAGE = new ChasMod(this, "damage", Chastic.DAMAGE_DEALT);
            final ChasMod[] stats = new ChasMod[] {TIME};
            public ChasMod[] stats() {
                return stats;
            }
            public boolean cast(final Chain ch, final int lvl) {
                final LivingEntity tgt = ch.target();
                final LivingEntity caster = ch.caster();

                final double dmg = DAMAGE.modify(ch, lvl);
                final double time = TIME.modify(ch, lvl);
                Effects.BLEED.apply(tgt, (int) (time * 20d), dmg);
                next(ch);
                return true;
            }
            public String id() {
                return "slice";
            }
            public String name() {
                return "Порез";
            }
            private final String[] desc = new String[] {
                TCUtil.N + "Атакуемая цель будет истекать " + CLR + "кровью",
                TCUtil.N + "на " + TIME.id() + " сек, " + TCUtil.N + "получая",
                DAMAGE.id() + " ед. " + TCUtil.N + "урона каждую секунду"};
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
            public Role role() {return Role.ASSASIN;}
        };

        //hit a diff mob when hurt
        //Разброс

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_DEALT, Chastic.DISTANCE};
            }
            public String id() {
                return "dmg_and_dst";
            }
            public String name() {
                return "Синедра";
            }
            public ItemType icon() {
                return ItemType.GUSTER_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.ASSASIN;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.MANA};
            }
            public String id() {
                return "nonagro_mana";
            }
            public String name() {
                return "Алзарин";
            }
            protected String needs() {
                return TCUtil.N + CLR + "Цель " + TCUtil.N + "не заагрена на " + CLR + "пользователя";
            }
            public double modify(Chastic ch, double def, int lvl, @Nullable Chain info) {
                if (info == null || !(info.target() instanceof final Mob mb)) return def;
                final LivingEntity tgt = mb.getTarget();
                if (tgt != null && tgt.getEntityId() == info.caster().getEntityId()) return def;
                return super.modify(ch, def, lvl, info);
            }
            public ItemType icon() {
                return ItemType.BREWER_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return Role.ASSASIN;}
        };

        //when invis
    }
}
