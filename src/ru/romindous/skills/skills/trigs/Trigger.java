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

    ATTACK_ENTITY("Нанесение Урона", null, ItemType.IRON_SWORD, false, "Срабатывает при нанесении физического", "урона по цели. §Использует ее локацию"), //EntityDamageByEntityEvent
    PROJ_LAUNCH("Запуск Снаряда", null, ItemType.FIRE_CHARGE, true, "Срабатывает при запуске любого", "снаряда. §Использует его локацию"), //ProjectileLaunchEvent
    RANGED_HIT("Попадание с Дистанции", null, ItemType.APPLE, false, "Срабатывает при попадании снарядом в", "любую цель. §Использует ее локацию"), //ProjectileHitEvent
    KILL_ENTITY("Убийство Сущности", null, ItemType.ROTTEN_FLESH, false, "Срабатывает при убийстве любой цели", "игроком. §Использует ее локацию"), //PlayerKillEntityEvent
    SHIFT_LEFT("Шифт с ЛКМ", null, ItemType.TNT_MINECART, true, "Срабатывает при нажатии ЛКМ в крадущемся", "виде. §Использует локацию блока, на", "§который смотрит пользователь"), //PlayerInteractEvent
    SHIFT_RIGHT("Шифт с ПКМ", null, ItemType.CHEST_MINECART, true, "Срабатывает при нажатии ПКМ в крадущемся", "виде. §Использует локацию блока, на", "§который смотрит пользователь"), //PlayerInteractEvent
    SHIFT_JUMP("Шифт с Прыжком", null, ItemType.BIG_DRIPLEAF, true, "Срабатывает при прыжке в крадущемся", "виде. §Использует локацию пользователя"), //PlayerJumpEvent -
    DOUBLE_JUMP("Двойной Прыжок", null, ItemType.SMALL_DRIPLEAF, true, "Срабатывает при двойном прыжке.", "§Использует локацию пользователя"), //PlayerToggleFlightEvent -
    USER_DEATH("Смертельный Урон", null, ItemType.SKELETON_SKULL, true, "Срабатывает при получении смертельного", "урона. §Использует локацию пользователя"), //EntityDeathEvent -
    USER_HURT("Получение Урона", null, ItemType.FERMENTED_SPIDER_EYE, false, "Срабатывает при получении физического", "урона. §Использует локацию цели"), //EntityDamageEvent -
    SPAWN_MINION("Спавн Приспешника", null, ItemType.GLOW_SQUID_SPAWN_EGG, false, "Срабатывает при спавне приспешника.", "§Использует его локацию"), //MinionSpawnEvent -
    ABIL_CAST("Прокаст Способности", null, ItemType.ENDER_EYE, true, "Срабатывает при использовании любой", "способности. §Использует указанную ей локацию"), //EntityCastEvent -
    UNKNOWN("Неизвестный", null, ItemType.SCULK_SENSOR, true, "Срабатывает при неописуемых обстоятельствах.", "§Использует локацию пользователя"); //Event -

    private static final String LOC_CLR = "<gray>";
    private static final String prefix = "trigs.";
    public static final String color = "<pink>";
    public static final String SIDE = "🟃";

//    public final double cdFactor = value("cdFactor", 1d);
//    public final double soulFactor = value("soulFactor", 1d);

    private final String name;
    private final Selector sel;
    private final ItemType icon;
    private final String[] desc;

    Trigger(final String name, final Selector sel,
    final ItemType icon, final boolean self, final String... desc) {
        this.sel = sel;
        this.name = name;
        this.icon = icon;
        final String[] dsc = new String[desc.length + 1];
        for (int i = 0; i != desc.length; i++) {
            dsc[i] = TCUtil.N + desc[i].replace("§", LOC_CLR);
        }
        dsc[desc.length] = "<dark_gray>Цель: " + TCUtil.A
            + (self ? "Пользователь (Ты)" : "Примененная Сущность");
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
