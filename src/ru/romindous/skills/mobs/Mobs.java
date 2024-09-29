package ru.romindous.skills.mobs;

import ru.romindous.skills.mobs.minons.MiniSilverfish;
import ru.romindous.skills.mobs.wastes.*;

public interface Mobs {

    SednaMob BEAST = new Beast(),
    BONED = new Boned(), CLUTCHER = new Clutcher(),
    CRAWLER = new Crawler(), HIVERFISH = new Hiverfish(),
    INFECTED = new Infected(), ROTTING = new Rotting(),
    SPORED = new Spored(), STEED = new Steed();

    Minion MINI_SILVERFISH = new MiniSilverfish();

    static void init() {}
}
