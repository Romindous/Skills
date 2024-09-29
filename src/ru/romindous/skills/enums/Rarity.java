package ru.romindous.skills.enums;

public enum Rarity {
    COMMON("§fКонсута"),
    UNCOM("§aИнсолита"),
    RARE("§9Рария"),
    EPIC("§5Хероика"),
    MYTHIC("§3Фабулоса"),
    LEGEND("§6Традитиа"),
    ABNORMAL("§чАлиа"),
    ERROR("§к#?&]%;");

    public final String name;
    public final String clr;

    Rarity(final String name) {
        this.name = name;
        this.clr = name.substring(0, 2);
    }
}
