package ru.romindous.skills.skills;

import org.bukkit.inventory.ItemType;

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

    public ItemType icon() {
        return switch (this) {
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

    public static final ItemType EMT_SOUL = ItemType.GRAY_CANDLE;
    public ItemType soul() {
        return switch (this) {
            case COMMON -> ItemType.LIGHT_GRAY_CANDLE;
            case UNCOM -> ItemType.LIME_CANDLE;
            case RARE -> ItemType.LIGHT_BLUE_CANDLE;
            case EPIC -> ItemType.PURPLE_CANDLE;
            case MYTHIC -> ItemType.CYAN_CANDLE;
            case LEGEND -> ItemType.ORANGE_CANDLE;
            case ABNORMAL -> ItemType.BLUE_CANDLE;
            case ERROR -> ItemType.RED_CANDLE;
        };
    }
}
