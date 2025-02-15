package ru.romindous.skills.skills.mods;

import javax.annotation.Nullable;
import java.util.*;
import org.bukkit.inventory.ItemType;
import ru.komiss77.notes.OverrideMe;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Chain;

public abstract class Modifier implements Scroll {//модификатор

    public static final Map<String, Modifier> VALUES = new HashMap<>();
    public static final IntHashMap<List<Modifier>> RARITIES = new IntHashMap<>();

    public static final String prefix = "mods.";
    public static final String data = "mod";

    private static int id_count = 0;
    final int nid = id_count++;

    private static final int chNum = Chastic.values().length;

    private final Mod[] chMods = new Mod[chNum];

    protected Modifier() {
        VALUES.put(id(), this);
        final List<Modifier> mds = RARITIES.get(sum());
        if (mds != null) mds.add(this);
        else RARITIES.put(sum(), new ArrayList<>(Arrays.asList(this)));

        for (final Chastic ch : chastics()) {
            chMods[ch.ordinal()] = new Mod(
                value("conBase", 1d),
                value("conScale", 1d),
                value("mulBase", 0d),
                value("mulScale", 0d));
        }
    }

    public String data() {
        return data;
    }

    @OverrideMe
    protected String needs() {
        return "";
    }

    public static final String SIDE = "💠";
    public String side() {
        return SIDE;
    }

    public abstract ItemType icon();

    public abstract Chastic[] chastics();

    public double modify(final Chastic ch, final double def, final int lvl, final @Nullable Chain info) {
        final Mod md = chMods[ch.ordinal()];
        if (md == null) return def;
        final double dm = ch.dec() ? -1d : 1d;
        return dm * NumUtil.absMin(md.conScale * lvl + md.conBase,
             (md.mulScale * lvl + md.mulBase) * def) + def;
    }

    /*protected double modified(final double def, final Chastic ch, final int level) {
        final Mod md = chMods[ch.ordinal()];
        if (md == null) return def;
        return def + NumUtil.absMin(md.conScale * level + md.conBase,
            (md.mulScale * level + md.mulBase) * def);
    }*/

    public record ModState(Modifier mod, int lvl) {}

    private static final byte SIG_FIGS = 2;

    private String relate(final Modifier md, final Skill sk) {
        if (sk == null) return "";
        final Chastic[] chs = md.chastics();
        Arrays.sort(chs);
        return null;
    }

    @Override
    public String[] desc(final int lvl) {
        final List<String> dscs = new ArrayList<>();
        dscs.add(TCUtil.N + "Применимая роль: " + (role() == null ? Role.ANY : role().disName()));
        dscs.add("<dark_gray>Модифицирует:");
        for (final Chastic ch : chastics()) {
            final Mod md = chMods[ch.ordinal()];
            dscs.add(TCUtil.N + "◇ " + ch.disName() + TCUtil.N + " - на");
            dscs.add(ch.color() + StringUtil.toSigFigs(md.conScale * lvl + md.conBase, SIG_FIGS)
                + TCUtil.N + " или " + ch.color() +
                StringUtil.toSigFigs(md.mulScale * lvl + md.mulBase * 100f, SIG_FIGS) +
                "%" + TCUtil.N + ", смотря что ниже");
        }
        final String nds = needs();
        if (!nds.isEmpty()) {
            dscs.add("<dark_gray>Требования:");
            dscs.add(nds.replace(CLR, rarity().color()));
        }
        dscs.add(" ");
        return dscs.toArray(new String[0]);
    }

    @Override
    public int hashCode() {
        return nid;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Modifier && ((Modifier) o).nid == nid;
    }

    private record Mod(double conBase, double conScale, double mulBase, double mulScale) {}
}
