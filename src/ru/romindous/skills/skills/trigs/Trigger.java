package ru.romindous.skills.skills.trigs;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.skills.sels.Selector;

public enum Trigger {//тригер

    ATTACK_ENTITY("Нанесение Урона", null, ItemType.IRON_SWORD, "Срабатывает при нанесении", "физического урона пользователем"), //EntityDamageByEntityEvent
    PROJ_LAUNCH("Запуск Снаряда", null, ItemType.FIRE_CHARGE, "Срабатывает при запуске", "любого снаряда пользователем"), //ProjectileLaunchEvent
    RANGED_HIT("Попадание с Дистанции", null, ItemType.APPLE, "Срабатывает при попадании", "снарядом любого моба игроком"), //ProjectileHitEvent
    KILL_ENTITY("Убийство Сущности", null, ItemType.ROTTEN_FLESH, "Срабатывает при убийстве", "любого моба игроком"), //PlayerKillEntityEvent
    SHIFT_LEFT("Шифт с ЛКМ", null, ItemType.TNT_MINECART, "Срабатывает при нажатии", "ЛКМ в крадущемся виде"), //PlayerInteractEvent
    SHIFT_RIGHT("Шифт с ПКМ", null, ItemType.CHEST_MINECART, "Срабатывает при нажатии", "ПКМ в крадущемся виде", "(надо дерать что-то в руке)"), //PlayerInteractEvent
    SHIFT_JUMP("Шифт с Прыжком", null, ItemType.BIG_DRIPLEAF, "Срабатывает при прыжке", "пользователя в крадущемся виде"), //PlayerJumpEvent -
    DOUBLE_JUMP("Двойной Прыжок", null, ItemType.SMALL_DRIPLEAF, "Срабатывает при двойном", "прыжке пользователя"), //PlayerToggleFlightEvent -
    USER_DEATH("Смертельный Урон", null, ItemType.SKELETON_SKULL, "Срабатывает при получении", "смертельного урона пользователем"), //EntityDeathEvent -
    USER_HURT("Получение Урона", null, ItemType.FERMENTED_SPIDER_EYE, "Срабатывает при получении", "физического урона пользователем"), //EntityDamageEvent -
    SPAWN_MINION("Спавн Приспешника", null, ItemType.GLOW_SQUID_SPAWN_EGG, "Срабатывает при спавне", "мобов-пресмешников пользователя"), //MinionSpawnEvent -
    ABIL_CAST("Прокаст Способности", null, ItemType.ENDER_EYE, "Срабатывает при использовании", "предыдущей способности в списке"), //EntityCastEvent -
    UNKNOWN("Неизвестный", null, ItemType.SCULK_SENSOR, "Срабатывает при", "неописуемых обстоятельствах"); //Event -

    private static final String prefix = "trigs.";
    public static final String color = "<pink>";
    public static final String SIDE = "🟃";

//    public final double cdFactor = value("cdFactor", 1d);
//    public final double soulFactor = value("soulFactor", 1d);

    private final String name;
    private final Selector sel;
    private final ItemType icon;
    private final String[] desc;

    Trigger(final String name, final Selector sel, final ItemType icon, final String... desc) {
        this.sel = sel;
        this.name = name;
        this.icon = icon;
        final String[] dsc = new String[desc.length];
        for (int i = 0; i != dsc.length; i++) {
            dsc[i] = TCUtil.N + desc[i];
        }
        this.desc = dsc;
    }

    public Selector selector() {
        return sel;
    }

    public String disName() {
        return color + name;
    }

    public ItemStack icon() {
        return new ItemBuilder(icon).name(TCUtil.sided(disName(), SIDE)).lore(desc).build();
    }

    public List<String> context() {
        return Arrays.asList(desc);
    }

    /*private double value(final String val, final double def) {
        return ConfigVars.val(prefix + name() + "." + val, def);
    }*/

    public static Trigger get(final String st) {
        if (st == null || st.isEmpty()) return UNKNOWN;
        try {
            return valueOf(st.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    public String describe() {
        return TCUtil.N + "Тригер " + TCUtil.sided(disName(), SIDE);
    }
}
