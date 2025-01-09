package ru.romindous.skills.skills.sels;

import java.util.*;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.enums.Trigger;
import ru.romindous.skills.objects.Scroll;
import ru.romindous.skills.skills.ChasMod;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Chain;

public abstract class Selector implements Scroll {//подборник

    public static final Map<String, Selector> VALUES = new HashMap<>();
    public static final Map<Rarity, List<Selector>> RARITIES = new EnumMap<>(Rarity.class);

    private static int id_count = 0;
    final int nid = id_count++;

    public static final String prefix = "sels.";
    public static final String data = "sel";

    public final ChasMod manaMul = new ChasMod(this, "mana_mul", Chastic.MANA);
    public final ChasMod cdMul = new ChasMod(this, "cd_mul", Chastic.COOLDOWN);

    protected Selector() {
        VALUES.put(id(), this);
        final List<Selector> mds = RARITIES.get(rarity());
        if (mds == null) {
            RARITIES.put(rarity(), new ArrayList<>(Arrays.asList(this)));
        } else mds.add(this);
    }

    public ItemType icon() {
        return switch (rarity()) {
            case COMMON -> ItemType.LIGHT_GRAY_DYE;
            case UNCOM -> ItemType.LIME_DYE;
            case RARE -> ItemType.LIGHT_BLUE_DYE;
            case EPIC -> ItemType.PURPLE_DYE;
            case MYTHIC -> ItemType.CYAN_DYE;
            case LEGEND -> ItemType.ORANGE_DYE;
            case ABNORMAL -> ItemType.BLUE_DYE;
            case ERROR -> ItemType.RED_DYE;
        };
    }

    public String data() {
        return data;
    }

    public abstract Collection<LivingEntity> select(final Chain ch, final int lvl);

    protected abstract String[] descs();

    public String side() {
        return "🞜";
    }

    public abstract ChasMod[] stats();

    public int avgAmount(final int lvl) {
        int amt = 2;
        for (final ChasMod cm : stats())
            if (cm.chs == Chastic.AMOUNT)
                amt += (int) cm.calc(lvl);
        return amt >> 1;
    }

    public record SelState(Selector sel, int lvl) {}

    private static final byte SIG_FIGS = 2;

    public List<String> context(final Skill sk, final int lvl) {
        final List<String> dscs = new ArrayList<>();
        final ChasMod[] stats = stats();
        for (final String d : descs()) {
            String ed = d.replace(CLR, rarity().color());
            for (final ChasMod st : stats) {
                ed = ed.replace(st.id, st.chs.color()
                    + StringUtil.toSigFigs(st.modify(sk, lvl), SIG_FIGS));
            }
            dscs.add(ed);
        }
        return dscs;
    }

    public String[] desc(final int lvl) {
        final List<String> dscs = new ArrayList<>();
        dscs.add(TCUtil.N + "Применимая роль: " + (role() == null ? Role.ANY : role().disName()));
        if (CASTER.equals(this)) dscs.add(TCUtil.P + "Не может быть изменен!");
        dscs.add("<dark_gray>Выбирает:");
        final ChasMod[] stats = stats();
        for (final String d : descs()) {
            String ed = d.replace(CLR, rarity().color());
            for (final ChasMod st : stats) {
                ed = ed.replace(st.id, st.chs.color()
                    + StringUtil.toSigFigs(st.calc(lvl), SIG_FIGS));
            }
            dscs.add(ed);
        }
        dscs.add(" ");
        if (stats.length != 0) {
            dscs.add(TCUtil.N + "Влияющие Модификаторы:");
            for (final ChasMod st : stats) {
                dscs.add(TCUtil.N + "- " + st.chs.disName());
            }
            dscs.add(" ");
        }
        final int manaMul = (int) ((this.manaMul.calc(lvl) - 1d) * 100d);
        if (manaMul != 0) dscs.add(TCUtil.N + "Эффект на затрату душ: "
            + Main.manaClr + manaMul + "%");
        final int cdMul = (int) ((this.cdMul.calc(lvl) - 1d) * 100d);
        if (cdMul != 0) dscs.add(TCUtil.N + "Эффект на перезарядку: "
            + Main.cdClr + cdMul + "%");
        return dscs.toArray(new String[0]);
    }

    protected ChasMod distChMod() {
        return new ChasMod(this, "dist", Chastic.DISTANCE);
    }

    protected ChasMod amtChMod() {
        return new ChasMod(this, "amt", Chastic.AMOUNT);
    }

    protected static Collection<LivingEntity> getChArcLents(final Location loc, final double dst, final double arc, final Predicate<LivingEntity> can) {
        final Vector dir = loc.getDirection();
        final double dArc = arc * arc;
        return LocUtil.getChEnts(loc, dst,
            LivingEntity.class, ent -> can.test(ent)
                && ent.getEyeLocation().subtract(loc).toVector()
                .normalize().subtract(dir).lengthSquared() < dArc);
    }

    private static LivingEntity getClsArcLent(final Location loc, final double dst, final double arc, final Predicate<LivingEntity> can) {
        final Vector dir = loc.getDirection();
        final double dArc = arc * arc;
        return LocUtil.getClsChEnt(loc, dst,
            LivingEntity.class, ent -> can.test(ent)
                && ent.getEyeLocation().subtract(loc).toVector()
                .normalize().subtract(dir).lengthSquared() < dArc);
    }

    @Nullable
    public Role role() {
        return null;
    }

    @Override
    public int hashCode() {
        return nid;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Selector && ((Selector) o).nid == nid;
    }

    public static final Selector SAME;
    public static final Selector CASTER;
    public static final Set<Selector> DEFAULT;

    static {
        SAME = new Selector() {
            public String id() {
                return "same";
            }
            public String name() {
                return "Данная Сущность";
            }
            public ChasMod[] stats() {
                return new ChasMod[]{};
            }
            private final String[] desc = new String[]{
                TCUtil.N + CLR + "Сущность " + TCUtil.N + "указанную предыдушей",
                TCUtil.P + "способностью " + TCUtil.N + "или " + Trigger.color + "триггером"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                return Main.canAttack(ch.caster(), ch.target(), true) ? List.of(ch.target()) : List.of();
            }
        };

        CASTER = new Selector() {
            public String id() {
                return "caster";
            }
            public String name() {
                return "Пользователь (Ты)";
            }
            public ChasMod[] stats() {
                return new ChasMod[]{};
            }
            private final String[] desc = new String[]{
                TCUtil.N + CLR + "Сущность, которая",
                TCUtil.N + "изпользует " + TCUtil.P + "способность"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                return List.of(ch.caster());
            }
        };

        DEFAULT = Set.of(SAME, CASTER);
        RARITIES.remove(SAME.rarity());
        RARITIES.remove(CASTER.rarity());
    }

    public static void register() {
        new Selector() {
            public String id() {
                return "fwd_arc_small";
            }
            public String name() {
                return "Малый Участок Спереди";
            }
            final ChasMod DIST = distChMod(), AMT = amtChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST, AMT};
            }
            private final double arc = value("arc", 0.6d);
            private final String[] desc = new String[]{
                TCUtil.N + "Сущности спереди предыдушей " + CLR + "цели" + TCUtil.N + ", с",
                TCUtil.N + "аркой в " + CLR + (int) (arc * 100) + "° " + TCUtil.N + "и дистанцией " + DIST.id + " бл.",
                TCUtil.N + "Лимит - " + AMT.id + " сущ. (округляемо)"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                loc.setDirection(loc.toVector().subtract(ch.caster().getLocation().toVector()));
                final Collection<LivingEntity> chEnts = getChArcLents(loc, DIST.modify(ch, lvl), arc,
                    ent -> Main.canAttack(ch.caster(), ent, false));
                if (chEnts.isEmpty()) return List.of();
                final List<LivingEntity> les = new ArrayList<>();
                final Iterator<LivingEntity> chi = chEnts.iterator();
                final int amt = (int) Math.round(AMT.modify(ch, lvl));
                les.add(ch.target()); int cnt = 1;
                while (chi.hasNext() && cnt < amt) {
                    les.add(chi.next()); cnt++;
                }
                return les;
            }
        };

        new Selector() {
            public String id() {
                return "fwd_arc_big";
            }
            public String name() {
                return "Большой Участок Спереди";
            }
            final ChasMod DIST = distChMod(), AMT = amtChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST, AMT};
            }
            private final double arc = value("arc", 1.0d);
            private final String[] desc = new String[]{
                TCUtil.N + "Сущности спереди предыдушей " + CLR + "цели" + TCUtil.N + ", с",
                TCUtil.N + "аркой в " + CLR + (int) (arc * 100) + "° " + TCUtil.N + "и дистанцией " + DIST.id + " бл.",
                TCUtil.N + "Лимит - " + AMT.id + " сущ. (округляемо)"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                loc.setDirection(loc.toVector().subtract(ch.caster().getLocation().toVector()));
                final Collection<LivingEntity> chEnts = getChArcLents(loc, DIST.modify(ch, lvl), arc,
                    ent -> Main.canAttack(ch.caster(), ent, false));
                if (chEnts.isEmpty()) return List.of();
                final List<LivingEntity> les = new ArrayList<>();
                final Iterator<LivingEntity> chi = chEnts.iterator();
                final int amt = (int) Math.round(AMT.modify(ch, lvl));
                les.add(ch.target()); int cnt = 1;
                while (chi.hasNext() && cnt < amt) {
                    les.add(chi.next()); cnt++;
                }
                return les;
            }
        };

        new Selector() {
            public String id() {
                return "close";
            }
            public String name() {
                return "Ближайшая Сущность";
            }
            final ChasMod DIST = distChMod();
            public ChasMod[] stats() {
                return new ChasMod[]{DIST};
            }
            private final String[] desc = new String[]{
                TCUtil.N + "Ближайшую сущность от предыдушей",
                TCUtil.N + CLR + "цели" + TCUtil.N + ", не далее " + DIST.id + " бл."};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final LivingEntity le = LocUtil.getClsChEnt(loc, DIST.modify(ch, lvl), LivingEntity.class,
                    ent -> ent.getEntityId() != ch.target().getEntityId() && Main.canAttack(ch.caster(), ent, false));
                return le == null ? List.of() : List.of(le);
            }
        };

        new Selector() {
            public String id() {
                return "circle";
            }
            public String name() {
                return "Сущности в Окружении";
            }
            final ChasMod DIST = distChMod(), AMT = amtChMod();
            final ChasMod[] stats = new ChasMod[]{DIST, AMT};
            public ChasMod[] stats() {
                return stats;
            }
            private final String[] desc = new String[]{
                TCUtil.N + "Сущности, окружающие предыдущую",
                TCUtil.N + CLR + "цель" + TCUtil.N + ", не далее " + DIST.id + " бл.",
                TCUtil.N + "Лимит - " + AMT.id + " сущ. (округляемо)"};
            public String[] descs() {
                return desc;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Collection<LivingEntity> select(final Chain ch, final int lvl) {
                final Location loc = ch.at();
                final Collection<LivingEntity> chEnts = LocUtil.getChEnts(loc, DIST.modify(ch, lvl),
                    LivingEntity.class, ent -> Main.canAttack(ch.caster(), ent, false));
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
    }

    //entity of same type
}
