package ru.romindous.skills.skills.chas;

import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Chain;

public class ChasMod {
    public final String id;
    public final Chastic chs;
    private final double base;
    private final double scale;
//    private final boolean dec;

    public ChasMod(final Scroll ability, final String id, final Chastic chs) {
        this.id = id;
        this.chs = chs;
        this.base = ability.value(id + "_base", 1d);
        this.scale = ability.value(id + "_scale", 0d);
//        dec = scale < 0;
    }

    public double modify(final Skill sk, final int lvl) {
        return sk.modifyAll(chs, calc(lvl));
    }

    public double modify(final Chain ch, final int lvl) {
        return ch.sk().modifyAll(chs, calc(lvl), ch);
    }

    public double calc(final int lvl) {
        return scale * lvl + base;
    }
}
