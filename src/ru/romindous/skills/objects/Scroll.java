package ru.romindous.skills.objects;

import javax.annotation.Nullable;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.TCUtil;
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
        return rarity().color() + disName() + " " + toINums(lvl);
    }

    String[] desc(final int lvl);

    default ItemStack display(final int lvl) {
        return new ItemBuilder(icon()).name(name(lvl)).lore(desc(lvl)).build();
    }

    default ItemStack drop(final int lvl) {
        return new ItemBuilder(icon()).name(name(lvl)).lore(desc(lvl)).data(Key.key(data()), id())
            .data(Key.key(LVL), lvl).lore(TCUtil.P + "ПКМ " + TCUtil.N + "- присвоить").build();
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
        return ConfigVars.get(data() + "." + id() + "." + val, def);
    }

    default double value(final String val, final double def) {
        return ConfigVars.get(data() + "." + id() + "." + val, def);
    }
}
