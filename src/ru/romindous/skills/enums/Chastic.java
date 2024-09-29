package ru.romindous.skills.enums;

public enum Chastic {
    REGENERATION("Реген Здоровья", "§к"),
    AMOUNT("Кол-во Созданий", "§6"),
    REWARD("Полученая Награда", "§с"),
    DAMAGE_DEALT("Нанесенный Урон", "§4"),
    DAMAGE_TAKEN("Принятый Урон", "§c"),
    VELOCITY("Скорость Движения", "§м"),
    NUTRITION("Восполнение Голода", "§2"),
    MANA("Потребность Душ", "§9"),
    COOLDOWN("Время Перезарядки", "§н"),
    DISTANCE("Дистанция Действия", "§b"),
    EFFECT("Эффект Способности", "§5"),
    TIME("Время Действия", "§ч");

    private final String name;
    private final String color;

    Chastic(final String name, final String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return color + name;
    }

    public String color() {
        return color;
    }
}
