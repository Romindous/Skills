package ru.romindous.skills.objects;

import javax.annotation.Nullable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.utils.ItemBuilder;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;

public interface Scroll {

//    NamespacedKey key = new NamespacedKey(Ostrov.instance, "scroll");
    String CLR = "<c>";
    String LVL = "lvl";

    String data();

    String id();

    String disName();

    default String name(final int lvl) {
        return rarity().clr + disName() + " " + toINums(lvl);
    }

    String[] desc(final int lvl);

    default ItemStack item(final int lvl) {
        return new ItemBuilder(icon()).name(name(lvl)).lore(desc(lvl))
            .data(data(), id()).data(LVL, lvl).build();
    }

    ItemType icon();

    Rarity rarity();

    @Nullable Role role();

    static String toINums(final int lvl) {
        return switch (lvl + 1) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "IIX";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(lvl);
        };
    }

    default int value(final String val, final int def) {
        return ConfigVars.get(this.getClass().getSimpleName()
            .toLowerCase() + "." + id() + "." + val, def);
    }

    default double value(final String val, final double def) {
        return ConfigVars.get(this.getClass().getSimpleName()
            .toLowerCase() + "." + id() + "." + val, def);
    }
}
