package ru.romindous.skills.skills.chas;

import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Chain;

public record ChasMod(String id, Chastic chs, double base, double scale) {

    public ChasMod(final Scroll ability, final String id, final Chastic chs) {
        this(id, chs, ability.value(id + "_base", 1d),
            ability.value(id + "_scale", 0d));
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
