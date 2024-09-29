package ru.romindous.skills.enums;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.config.ConfigVars;

public enum Stat {
    STRENGTH("Сила", "§c", ItemType.REDSTONE_BLOCK), //warrior - max hp, melee dmg
    AGILITY("Ловкость", "§e", ItemType.SCAFFOLDING), //assasin - move speed, skill cd
    METABOLISM("Метаболизм", "§6", ItemType.SMOKER), //vampire - hp regen, saturation
    CONTROL("Контроль", "§a", ItemType.SCULK_SHRIEKER), //necros - minion stats, mob stats
    PASSIVE("Находчивость", "§б", ItemType.REINFORCED_DEEPSLATE), //stone - hurt dmg, mob drops
    MAGIC("Магия", "§9", ItemType.ENCHANTING_TABLE), //mage - max mana, magic dmg
    ACCURACY("Точность", "§b", ItemType.TARGET), //cross - ranged dmg, exp
    SPIRIT("Душевность", "§d", ItemType.SOUL_CAMPFIRE); //phantom - mana regen, skill mana

    private static final String prefix = "stats.";
    private static final String plus = "§2(+) " + TCUtil.N;
    private static final String minus = "§4(-) " + TCUtil.N;
    private static final byte SIG_FIGS = 2;

    private final String name;
    private final String clr;
    private final ItemType icon;

    Stat(final String name, final String clr, final ItemType icon) {
        this.name = name;
        this.clr = clr;
        this.icon = icon;
    }

    public String getName() {
        return clr + name;
    }

    public String color() {
        return clr;
    }

    public static @Nullable Stat get(final String st) {
        if (st == null || st.isEmpty()) return null;
        try {
            return valueOf(st.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ItemStack getItem(final Survivor sv) {
        return new ItemBuilder(icon).name(getName()).lore(getDesc(sv)).build();
    }

    private String[] getDesc(final Survivor sv) {
        final int lvl = sv.getStat(this);
        final boolean next = sv.statsPoints != 0;
        final List<String> dscs = new ArrayList<>();
        dscs.add(TCUtil.N + "Модифицирует характеристики:");
        dscs.add(" ");
        final double rt = rootate(lvl);
        final double nrt = rootate(lvl + 1);
        switch (this) {
            case STRENGTH:
                numDesc(plus + "Максимальное " + clr + "здоровье",
                    rt, nrt, next, healthAdd, true, dscs);
                numDesc(plus + "Урон в " + clr + "ближнем " + TCUtil.N + "бою",
                    rt, nrt, next, directAdd, directMul, dscs);
                break;
            case AGILITY:
                numDesc(plus + clr + "Скорость " + TCUtil.N + "перемещения",
                    rt, nrt, next, speedAdd, true, dscs);
                numDesc(minus + clr + "Перезарядка " + TCUtil.N + "способностей",
                    rt, nrt, next, skillCDAdd, skillCDMul, dscs);
                break;
            case METABOLISM:
                numDesc(plus + clr + "Регенерация " + TCUtil.N + "здоровья",
                    rt, nrt, next, regenAdd, regenMul, dscs);
                numDesc(plus + clr + "Насыщение " + TCUtil.N + "пищей",
                    rt, nrt, next, nutriAdd, nutriMul, dscs);
                break;
            case CONTROL:
                numDesc(plus + "Здоровье " + plus + clr + "преспешников "
                    + TCUtil.N + "и " + minus + clr + "враждебных мобов",
                    rt, nrt, next, mobHealthMul, false, dscs);
                numDesc(plus + "Защита " + plus + clr + "преспешников "
                    + TCUtil.N + "и " + minus + clr + "враждебных мобов",
                    rt, nrt, next, mobArmorMul, false, dscs);
                numDesc(plus + "Урон " + plus + clr + "преспешников "
                    + TCUtil.N + "и " + minus + clr + "враждебных мобов",
                    rt, nrt, next, mobDamageMul, false, dscs);
                numDesc(plus + "Скорость " + plus + clr + "преспешников "
                    + TCUtil.N + "и " + minus + clr + "враждебных мобов",
                    rt, nrt, next, mobSpeedMul, false, dscs);
                break;
            case PASSIVE:
                numDesc(minus + "Полученный " + clr + "урон" + minus,
                    rt, nrt, next, defenseAdd, defenseMul, dscs);
                numDesc(plus + "Дроп с " + clr + "мобов" + plus,
                    rt, nrt, next, dropsAdd, true, dscs);
                break;
            case MAGIC:
                numDesc(plus + clr + "Магический " + TCUtil.N + "урон",
                    rt, nrt, next, magicAdd, magicMul, dscs);
                numDesc(plus + "Максимальное " + clr + "кол-во душ",
                    rt, nrt, next, manaAdd, true, dscs);
                break;
            case ACCURACY:
                numDesc(plus + "Урон в " + clr + "дальнем " + TCUtil.N + "бою",
                    rt, nrt, next, rangedAdd, rangedMul, dscs);
                numDesc(plus + "Полученый " + clr + "опыт скилла",
                    rt, nrt, next, expAdd, expMul, dscs);
                break;
            case SPIRIT:
                numDesc(plus + "Возобновление " + clr + "душ ",
                    rt, nrt, next, reManaAdd, reManaMul, dscs);
                numDesc(minus + "Затрата " + clr + "душ " + TCUtil.N + "на скиллы",
                    rt, nrt, next, skillManaAdd, skillManaMul, dscs);
                break;
        }

        if (next) dscs.add("Клик - улучшить " + TCUtil.P + "(" + sv.statsPoints + ")");
        return dscs.toArray(new String[0]);
    }

    private void numDesc(final String name, final double rt, final double nrt,
        final boolean next, final double num, final boolean add, final List<String> dscs) {
        dscs.add(name);
        if (next) {
            if (add) {
                dscs.add(TCUtil.N + "на " + clr + StringUtil.toSigFigs(num * rt, SIG_FIGS));
            } else {
                dscs.add(TCUtil.N + "на " + clr + StringUtil.toSigFigs(num * rt, SIG_FIGS) + "%");
            }
        } else {
            if (add) {
                dscs.add(TCUtil.N + "на " + clr + StringUtil.toSigFigs(num * rt, SIG_FIGS)
                    + TCUtil.P + " (-> " + StringUtil.toSigFigs(num * nrt, SIG_FIGS) + ")");
            } else {
                dscs.add(TCUtil.N + "на " + clr + StringUtil.toSigFigs(num * rt, SIG_FIGS) + "%"
                    + TCUtil.P + " (-> " + StringUtil.toSigFigs(num * nrt, SIG_FIGS) + "%)");
            }
        }
        dscs.add(" ");
    }

    private void numDesc(final String name, final double rt, final double nrt,
        final boolean next, final double add, final double mul, final List<String> dscs) {
        dscs.add(name);
        if (next) {
            dscs.add(TCUtil.N + "на " + clr + StringUtil.toSigFigs(add * rt, SIG_FIGS)
                + TCUtil.N + " или " + clr + StringUtil.toSigFigs(mul * rt, SIG_FIGS) + "%");
        } else {
            dscs.add(TCUtil.N + "на " + clr + StringUtil.toSigFigs(add * rt, SIG_FIGS)
                + TCUtil.P + " (-> " + StringUtil.toSigFigs(add * nrt, SIG_FIGS) + ")"
                + TCUtil.N + " или " + clr + StringUtil.toSigFigs(mul * rt, SIG_FIGS) + "%"
                + TCUtil.P + " (-> " + StringUtil.toSigFigs(mul * nrt, SIG_FIGS) + "%)");
        }
        dscs.add(TCUtil.N + "смотря какое значение " + clr + "ниже");
        dscs.add(" ");
    }

    public static double getVar(final String id, final double def) {
        return ConfigVars.get(prefix + id, def);
    }

    private static final int del = 4;
    private static final int sub = del - 1;
    private static double rootate(final int stat) {
        return stat == 0 ? 0d : Math.sqrt(stat << del) - sub;
    }

    private final static double defenseAdd = getVar("defenseAdd", 0.5d);
    private final static double defenseMul = getVar("defenseMul", 0.02d);
    public static double defense(final double def, final int passive) {
        return def - Math.min(defenseAdd, def * defenseMul) * rootate(passive);
    }

    private final static double directAdd = getVar("directAdd", 0.5d);
    private final static double directMul = getVar("directMul", 0.02d);
    public static double direct(final double def, final int strength) {
        return def + Math.min(directAdd, def * directMul) * rootate(strength);
    }

    private final static double rangedAdd = getVar("rangedAdd", 0.5d);
    private final static double rangedMul = getVar("rangedMul", 0.02d);
    public static double ranged(final double def, final int accuracy) {
        return def + Math.min(rangedAdd, def * rangedMul) * rootate(accuracy);
    }

    private final static double magicAdd = getVar("magicAdd", 0.5d);
    private final static double magicMul = getVar("magicMul", 0.02d);
    public static double magic(final double def, final int magic) {
        return def + Math.min(magicAdd, def * magicMul) * rootate(magic);
    }

    private final static double healthAdd = getVar("healthAdd", 0.5d);
    public static double health(final double def, final int strength) {
        return def + healthAdd * rootate(strength);
    }

    private final static double manaAdd = getVar("manaAdd", 0.5d);
    public static double mana(final double def, final int magic) {
        return def + manaAdd * rootate(magic);
    }

    private final static double speedAdd = getVar("speedAdd", 0.004d);
    public static double speed(final double def, final int agility) {
        return def + speedAdd * rootate(agility);
    }

    private final static double skillCDAdd = getVar("skillCDAdd", 0.5d);
    private final static double skillCDMul = getVar("skillCDMul", 0.02d);
    public static double skillCD(final double def, final int agility) {
        return def - Math.min(skillCDAdd, def * skillCDMul) * rootate(agility);
    }

    private final static double skillManaAdd = getVar("skillManaAdd", 0.5d);
    private final static double skillManaMul = getVar("skillManaMul", 0.02d);
    public static double skillMana(final double def, final int spirit) {
        return def - Math.min(skillManaAdd, def * skillManaMul) * rootate(spirit);
    }

    private final static double nutriAdd = getVar("nutriAdd", 0.5d);
    private final static double nutriMul = getVar("nutriMul", 0.02d);
    public static double food(final double def, final int metabolism) {
        return def + Math.min(nutriAdd, def * nutriMul) * rootate(metabolism);
    }

    private final static double regenAdd = getVar("regenAdd", 0.5d);
    private final static double regenMul = getVar("regenMul", 0.02d);
    public static double regen(final double def, final int metabolism) {
        return def + Math.min(regenAdd, def * regenMul) * rootate(metabolism);
    }

    private final static double expAdd = getVar("expAdd", 0.5d);
    private final static double expMul = getVar("expMul", 0.02d);
    public static double exp(final double def, final int accuracy) {
        return def + Math.min(expAdd, def * expMul) * rootate(accuracy);
    }

    private final static double dropsAdd = getVar("dropsAdd", 0.1d);
    public static double drops(final double def, final int passive) {
        return def + dropsAdd * rootate(passive);
    }

    private final static double reManaAdd = getVar("reManaAdd", 0.5d);
    private final static double reManaMul = getVar("reManaMul", 0.02d);
    public static double remana(final double def, final int spirit) {
        return def + Math.min(reManaAdd, def * reManaMul) * rootate(spirit);
    }

    private final static double mobHealthMul = getVar("mobHealthMul", 0.0025d);
    private final static double mobArmorMul = getVar("mobArmorMul", 0.004d);
    private final static double mobDamageMul = getVar("mobDamageMul", 0.002d);
    private final static double mobSpeedMul = getVar("mobDamageMul", 0.002d);

    public static void modMini(final Mob mb, final int control) {
        final double rt = rootate(control);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_MAX_HEALTH), mobHealthMul * rt);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_ARMOR), mobArmorMul * rt);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS), mobArmorMul * rt);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE), mobDamageMul * rt);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), mobSpeedMul * rt);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_FLYING_SPEED), mobSpeedMul * rt);
        mb.setHealth(mb.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
    }

    public static void modMob(final Mob mb, final int control) {
        final double rt = rootate(control);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_MAX_HEALTH), -mobHealthMul * rt);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_ARMOR), -mobArmorMul * rt);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS), -mobArmorMul * rt);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE), -mobDamageMul * rt);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), -mobSpeedMul * rt);
        Survivor.scaleAtr(mb.getAttribute(Attribute.GENERIC_FLYING_SPEED), -mobSpeedMul * rt);
        mb.setHealth(mb.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
    }
}
