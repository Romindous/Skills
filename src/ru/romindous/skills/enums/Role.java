package ru.romindous.skills.enums;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.objects.Scroll;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.roled.*;

import java.util.Locale;

public enum Role {

    VAMPIRE("Вампир", "§4", ItemType.BEETROOT_SOUP, Stat.METABOLISM, new Vampire(),
        TCUtil.N + "Восставшие " + Scroll.CLR + "кровососы" + TCUtil.N + ". Они питаются плотью",
        TCUtil.N + "своих жертв, и используют ее " + Scroll.CLR + "себе " + TCUtil.N + "во благо."),
    ASSASIN("Ассасин", "§d", ItemType.AMETHYST_SHARD, Stat.AGILITY, new Assasin(),
        TCUtil.N + "Древний класс " + Scroll.CLR + "убийц" + TCUtil.N + ". Ловкость позволяет им уклоняться от атак.",
        TCUtil.N + "Их основная способность - быть " + Scroll.CLR + "незаметными " + TCUtil.N + "при нападении."),
    ARCHER("Стрелок", "§c", ItemType.CROSSBOW, Stat.ACCURACY, new Archer(),
        TCUtil.N + "Воин средних веков, вооруженный " + Scroll.CLR + "луком " + TCUtil.N + "или" + Scroll.CLR + "арбалетом" + TCUtil.N + ",",
        TCUtil.N + "Может развить большую скорость полета " + Scroll.CLR + "снарядов" + TCUtil.N + "."),
    WARRIOR("Паладин", "§e", ItemType.IRON_SWORD, Stat.STRENGTH, new Warrior(),
        TCUtil.N + "Боец древнего " + Scroll.CLR + "ордена " + TCUtil.N + "по борьбе с нечистью. Хороший",
        TCUtil.N + "урон как по одной " + Scroll.CLR + "нечисти" + TCUtil.N + ", так и по их скоплениям."),
    MAGE("Маг", "§9", ItemType.HEART_OF_THE_SEA, Stat.MAGIC, new Mage(),
        TCUtil.N + "Древний " + Scroll.CLR + "чародей" + TCUtil.N + ", может атаковать свою жертву силами стихий.",
        TCUtil.N + "Может запускать " + Scroll.CLR + "снаряды " + TCUtil.N + "и накладывать " + Scroll.CLR + "эффекты" + TCUtil.N + " своим посохом."),
    PHANTOM("Фантом", "§5", ItemType.PHANTOM_MEMBRANE, Stat.SPIRIT, new Phantom(),
        TCUtil.N + "Образ или просто " + Scroll.CLR + "призрак" + TCUtil.N + ", питающийся душами человека.",
        TCUtil.N + "Хорошо манипулирует " + Scroll.CLR + "входящим " + TCUtil.N + "и " + Scroll.CLR + "нанесенным " + TCUtil.N + "уроном,",
        TCUtil.N + "и может переходить в " + Scroll.CLR + "иной мир " + TCUtil.N + "на время."),
    NECROS("Некромант", "§о", ItemType.KNOWLEDGE_BOOK, Stat.CONTROL, new Necros(),
        TCUtil.N + "Великий " + Scroll.CLR + "жрец" + TCUtil.N + ", который вопреки пророчеству, принялся",
        TCUtil.N + "за " + Scroll.CLR + "темную магию " + TCUtil.N + "для создания армии пустых " + Scroll.CLR + "сосудов " + TCUtil.N + ",",
        TCUtil.N + "следующих его воле. Может манипулировать " + Scroll.CLR + "мобами" + TCUtil.N + ", и знает их слабости в бою."),
    STONER("Каменьщик", "§ч", ItemType.CHAINMAIL_CHESTPLATE, Stat.PASSIVE, new Stoner(),
        TCUtil.N + "Бывалый боец, может постепенно " + Scroll.CLR + "укреплять " + TCUtil.N + "свою шкуру,",
        TCUtil.N + "как и атаковать, используя " + Scroll.CLR + "поверхность " + TCUtil.N + "на которой стоит."),
    ;

    public static final String ANY = "§8Любая";

    private final String name;
    private final String color;
    private final String[] desc;

    public final Ability.AbilReg reg;
    public final ItemType icon;
    public final Stat stat;

    Role(final String name, final String color, final ItemType icon, final Stat stat, final Ability.AbilReg reg, final String... desc) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.stat = stat;
        this.reg = reg;
        final String[] dsc = new String[desc.length];
        for (int i = 0; i != dsc.length; i++) {
            dsc[i] = desc[i].replace(Scroll.CLR, color);
        }
        this.desc = dsc;
    }

    public String getName() {
        return color + name;
    }

    public String color() {
        return color;
    }

    public ItemStack getIcon() {
        return new ItemBuilder(icon).name(getName()).lore(desc).build();
    }

    public static Role get(final String rl) {
        if (rl == null || rl.isEmpty()) return null;
        try {
            return valueOf(rl.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void init() {
        new All().register();
        for (final Role r : Role.values()) {
            r.reg.register();
        }
    }
}
