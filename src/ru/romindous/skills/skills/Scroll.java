package ru.romindous.skills.skills;

import javax.annotation.Nullable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.survs.Role;

public interface Scroll {

//    NamespacedKey key = new NamespacedKey(Ostrov.instance, "scroll");
    String CLR = "<c>";
    String LVL = "lvl";

    String data();

    String id();

    String name();

    default String name(final int lvl) {
        return rarity().color() + name() + " " + toINums(lvl);
    }

    String[] desc(final int lvl);

    //✵🌟🟃🞜💠⛨✞
    String side();

    default ItemStack display(final int lvl) {
        return new ItemBuilder(icon()).name(TCUtil.sided(name(lvl), side())).lore(desc(lvl)).build();
    }

    default ItemStack drop(final int lvl) {
        return new ItemBuilder(icon()).name(TCUtil.sided("<u>" + name(lvl) + "</u>", side())).lore(desc(lvl))
            .data(OStrap.key(data()), id()).data(OStrap.key(LVL), lvl).lore(TCUtil.P + "ПКМ " + TCUtil.N + "- присвоить").build();
    }

    default int sum() {
        return total(rarity(), role());
    }

    static int total(final Rarity rar, final @Nullable Role rl) {
        return rl == null ? rar.ordinal() : rar.ordinal() + (rl.ordinal() << 4);
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
            default -> String.valueOf(lvl + 1);
        };
    }

    String DEF_ROLE = "Все";

    default int value(final String val, final int def) {
        return role() == null ? ConfigVars.get(DEF_ROLE + "." + data() + "." + id() + "." + val, def)
            : ConfigVars.get(role().name + "." + data() + "." + id() + "." + val, def);
    }

    default double value(final String val, final double def) {
        return role() == null ? ConfigVars.get(DEF_ROLE + "." + data() + "." + id() + "." + val, def)
            : ConfigVars.get(role().name + "." + data() + "." + id() + "." + val, def);
    }

    interface Registerable {void register();}
}
