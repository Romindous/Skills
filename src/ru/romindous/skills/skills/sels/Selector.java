package ru.romindous.skills.skills.sels;

import java.util.*;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.skills.Rarity;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.chas.ChasMod;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.survs.Stat;

public abstract class Selector implements Scroll {//подборник

    public static final Map<String, Selector> VALUES = new HashMap<>();
    public static final IntHashMap<List<Selector>> RARITIES = new IntHashMap<>();

    private static int id_count = 0;
    final int nid = id_count++;

    public static final String prefix = "sels.";
    public static final String data = "sel";

    public final ChasMod manaMul = new ChasMod(this, "mana_mul", Chastic.MANA);
    public final ChasMod cdMul = new ChasMod(this, "cd_mul", Chastic.COOLDOWN);

    protected Selector() {
        VALUES.put(id(), this);
        final List<Selector> mds = RARITIES.get(sum());
        if (mds != null) mds.add(this);
        else RARITIES.put(sum(), new ArrayList<>(Arrays.asList(this)));
    }

    public ItemType icon() {
        return rarity().icon();
    }

    public String data() {
        return data;
    }

    public abstract Collection<LivingEntity> select(final Chain ch, final int lvl);

    protected abstract String[] descs();

    public static final String SIDE = "🞜";
    public String side() {
        return SIDE;
    }

    public abstract ChasMod[] stats();

    public int avgAmount(final int lvl) {
        int amt = 2;
        for (final ChasMod cm : stats())
            if (cm.chs() == Chastic.AMOUNT)
                amt += (int) cm.calc(lvl);
        return amt >> 1;
    }

    public record SelState(Selector val, int lvl) implements State {}

    public List<String> context(final Skill sk, final int lvl) {
        final List<String> dscs = new ArrayList<>();
        final ChasMod[] stats = stats();
        for (final String d : descs()) {
            String ed = d.replace(CLR, rarity().color());
            for (final ChasMod st : stats) {
                ed = ed.replace(st.id(), st.chs().color()
                    + StringUtil.toSigFigs(st.modify(sk, lvl), Stat.SIG_FIGS_NUM));
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
                ed = ed.replace(st.id(), st.chs().color()
                    + StringUtil.toSigFigs(st.calc(lvl), Stat.SIG_FIGS_NUM));
            }
            dscs.add(ed);
        }
        dscs.add(" ");
        if (stats.length != 0) {
            dscs.add(TCUtil.N + "Влияющие Модификаторы:");
            for (final ChasMod st : stats) {
                dscs.add(TCUtil.N + "- " + st.chs().disName());
            }
            dscs.add(" ");
        }
        final int manaMul = (int) ((this.manaMul.calc(lvl) - 1d) * 100d);
        if (manaMul != 0) dscs.add(TCUtil.N + "Требуемые души: "
            + Main.manaClr + (manaMul < 0 ? "" : "+") + manaMul + "%");
        final int cdMul = (int) ((this.cdMul.calc(lvl) - 1d) * 100d);
        if (cdMul != 0) dscs.add(TCUtil.N + "Время перезарядки: "
            + Main.cdClr + (cdMul < 0 ? "" : "+") + cdMul + "%");
//        dscs.add(" ");
        return dscs.toArray(new String[0]);
    }

    public String[] next(final int lvl) {
        final List<String> dscs = new ArrayList<>();
        dscs.add(TCUtil.N + "Применимая роль: " + (role() == null ? Role.ANY : role().disName()));
        if (CASTER.equals(this)) dscs.add(TCUtil.P + "Не может быть изменен!");
        dscs.add("<dark_gray>Выбирает:");
        final ChasMod[] stats = stats();
        for (final String d : descs()) {
            String ed = d.replace(CLR, rarity().color());
            for (final ChasMod st : stats) {
                ed = ed.replace(st.id(), st.chs().color() + StringUtil.toSigFigs(st.calc(lvl), Stat.SIG_FIGS_NUM) + TCUtil.P
                    + (st.scale() < 0 ? " (" : " (+") + StringUtil.toSigFigs(st.scale(), Stat.SIG_FIGS_PER) + ")" + st.chs().color());
            }
            dscs.add(ed);
        }
        dscs.add(" ");
        if (stats.length != 0) {
            dscs.add(TCUtil.N + "Влияющие Модификаторы:");
            for (final ChasMod st : stats) {
                dscs.add(TCUtil.N + "- " + st.chs().disName());
            }
            dscs.add(" ");
        }
        final int manaMul = (int) ((this.manaMul.calc(lvl) - 1d) * 100d);
        final int manaScl = (int) (this.manaMul.scale() * 100d);
        if (manaMul != 0) dscs.add(TCUtil.N + "Требуемые души: "
            + Main.manaClr + (manaMul < 0 ? "" : "+") + manaMul + "%"
            + TCUtil.P + (manaScl < 0 ? " (" : " (+") + manaScl + "%)");
        final int cdMul = (int) ((this.cdMul.calc(lvl) - 1d) * 100d);
        final int cdScl = (int) (this.cdMul.scale() * 100d);
        if (cdMul != 0) dscs.add(TCUtil.N + "Время перезарядки: "
            + Main.cdClr + (cdMul < 0 ? "" : "+") + cdMul + "%"
            + TCUtil.P + (cdScl < 0 ? " (" : " (+") + cdScl + "%)");
//        dscs.add(" ");
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
                && EntityUtil.center(ent).subtract(loc).toVector()
                .normalize().subtract(dir).lengthSquared() < dArc);
    }

    protected static LivingEntity getClsArcLent(final Location loc, final double dst, final double arc, final Predicate<LivingEntity> can) {
        final Vector dir = loc.getDirection();
        final double dArc = arc * arc;
        return LocUtil.getClsChEnt(loc, dst,
            LivingEntity.class, ent -> can.test(ent)
                && EntityUtil.center(ent).subtract(loc).toVector()
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
    public static final SelState SAME_ST;
    public static final Selector CASTER;
    public static final SelState CASTER_ST;

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
                TCUtil.P + "способностью " + TCUtil.N + "или " + Trigger.color + "тригером"};
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
        SAME_ST = new SelState(SAME, 0);

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
                TCUtil.N + CLR + "Существо" + TCUtil.N + ", которое",
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
        CASTER_ST = new SelState(CASTER, 0);

        DEFAULT.add(SAME); DEFAULT.add(CASTER);
        final List<Selector> def = RARITIES.get(SAME.sum());
        def.remove(SAME.sum()); def.remove(CASTER.sum());
    }

    //entity of same type
}
