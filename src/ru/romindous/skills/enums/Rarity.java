package ru.romindous.skills.enums;

public enum Rarity {
    COMMON("Консута", "§f"),
    UNCOM("Инсолита", "§a"),
    RARE("Рария", "§9"),
    EPIC("Хероика", "§5"),
    MYTHIC("Фабулоса", "§3"),
    LEGEND("Традитиа", "§6"),
    ABNORMAL("Алиа", "§ч"),
    ERROR("#?&]%;", "§к");

    private final String name;
    private final String clr;

    Rarity(final String name, final String clr) {
        this.name = name;
        this.clr = clr;
    }

    public String getName() {
        return clr + name;
    }

    public String color() {
        return clr;
    }
}
