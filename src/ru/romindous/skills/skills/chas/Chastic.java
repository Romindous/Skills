package ru.romindous.skills.skills.chas;

public enum Chastic {
    REGENERATION("Реген Здоровья", "§к", false),
    AMOUNT("Кол-во Созданий", "§6", false),
    REWARD("Полученая Награда", "§с", false),
    DAMAGE_DEALT("Нанесенный Урон", "§4", false),
    DAMAGE_TAKEN("Принятый Урон", "§c", true),
    VELOCITY("Скорость Движения", "§м", false),
    NUTRITION("Восполнение Голода", "§2", false),
    MANA("Потребность Душ", "§9", true),
    COOLDOWN("Время Перезарядки", "§т", true),
    DISTANCE("Дистанция Действия", "§b", false),
    EFFECT("Эффект Способности", "§5", false),
    TIME("Время Действия", "§ч", false);

    private final String name;
    private final String color;
    private final boolean dec;

    Chastic(final String name, final String color, final boolean dec) {
        this.name = name;
        this.color = color;
        this.dec = dec;
    }

    public String disName() {
        return color + name;
    }

    public String color() {
        return color;
    }

    public boolean dec() {
        return dec;
    }
}
