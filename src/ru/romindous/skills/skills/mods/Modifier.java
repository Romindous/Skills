package ru.romindous.skills.skills.mods;

import java.util.*;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemType;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.objects.Scroll;

public abstract class Modifier implements Scroll {//модификатор

    public static final Map<String, Modifier> VALUES = new HashMap<>();
    public static final Map<Rarity, List<Modifier>> RARITIES = new HashMap<>();

    public static final String prefix = "mods.";
    public static final String data = "sel";

    private static int id_count = 0;
    final int nid = id_count++;

    private static final int chNum = Chastic.values().length;

    private final Mod[] chMods = new Mod[chNum];

    protected Modifier() {
        VALUES.put(id(), this);
        final List<Modifier> mds = RARITIES.get(rarity());
        if (mds == null) {
            RARITIES.put(rarity(), new ArrayList<>(Arrays.asList(this)));
        } else mds.add(this);

        for (final Chastic ch : chastics()) {
            chMods[ch.ordinal()] = new Mod(
                ConfigVars.get(prefix + id() + ".conBase", 1d),
                ConfigVars.get(prefix + id() + ".conScale", 1d),
                ConfigVars.get(prefix + id() + ".mulBase", 1d),
                ConfigVars.get(prefix + id() + ".mulScale", 1d));
        }
    }

    public String data() {
        return data;
    }

    protected String needs() {
        return "";
    }

    public abstract ItemType icon();

    protected abstract Chastic[] chastics();

    public double modify(final Chastic ch, final double def, final int lvl, final Event e) {
        return modified(def, ch, lvl);
    }

    protected double modified(final double def, final Chastic ch, final int level) {
        final Mod md = chMods[ch.ordinal()];
        if (md == null) return def;
        return def + FastMath.absDec(md.conScale * level + md.conBase,
            (md.mulScale * level + md.mulBase) * def);
    }

    public record ModState(Modifier mod, int lvl) {}

    private static final byte SIG_FIGS = 2;

    @Override
    public String[] desc(final int lvl) {
        final List<String> dscs = new ArrayList<>();
        dscs.add(TCUtil.N + "Применим для: " + (role() == null ? Role.ANY : role().getName()));
        dscs.add(" ");
        dscs.add(TCUtil.N + "Модифицирует характеристики:");
        for (final Chastic ch : chastics()) {
            final Mod md = chMods[ch.ordinal()];
            dscs.add(ch.getName() + TCUtil.N + " - на");
            dscs.add("  " + TCUtil.P + StringUtil.toSigFigs(md.conScale * lvl + md.conBase, SIG_FIGS) +
                TCUtil.N + " или " + TCUtil.P + StringUtil.toSigFigs(md.mulScale * lvl + md.mulBase * 100f, SIG_FIGS) +
                "%" + TCUtil.N + ", смотря что ниже");
        }
        dscs.add(" ");
        final String nds = needs();
        if (!nds.isEmpty()) {
            dscs.add(TCUtil.N + "Допольнительные требования:");
            dscs.add(TCUtil.P + nds);
        }
        return dscs.toArray(new String[0]);
    }

    @Override
    public int hashCode() {
        return nid;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Modifier && ((Modifier) o).nid == nid;
    }

    private record Mod(double conBase, double conScale, double mulBase, double mulScale) {}

    public static void register() {
        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_DEALT};
            }
            public String id() {return "damage_inc";}
            public String disName() {
                return "Нанесенный Урон";
            }
            public ItemType icon() {
                return ItemType.BLADE_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_TAKEN};
            }
            public String id() {
                return "hurt_dec";
            }
            public String disName() {
                return "Полученный Урон";
            }
            public ItemType icon() {
                return ItemType.HEARTBREAK_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.AMOUNT};
            }
            public String id() {
                return "amount_inc";
            }
            public String disName() {
                return "Количество";
            }
            public ItemType icon() {
                return ItemType.PLENTY_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.REWARD};
            }
            public String id() {
                return "reward_inc";
            }
            public String disName() {
                return "Награда";
            }
            public ItemType icon() {
                return ItemType.PRIZE_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.REGENERATION};
            }
            public String id() {
                return "regen_inc";
            }
            public String disName() {
                return "Регенерация";
            }
            public ItemType icon() {
                return ItemType.HEART_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.VELOCITY};
            }
            public String id() {
                return "velocity_inc";
            }
            public String disName() {
                return "Скорость";
            }
            public ItemType icon() {
                return ItemType.FLOW_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.NUTRITION};
            }
            public String id() {
                return "food_inc";
            }
            public String disName() {
                return "Насыщаемость";
            }
            public ItemType icon() {
                return ItemType.SHEAF_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.COOLDOWN};
            }
            public String id() {
                return "cooldown_dec";
            }
            public String disName() {
                return "Перезарядка";
            }
            public ItemType icon() {
                return ItemType.ARMS_UP_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.MANA};
            }
            public String id() {
                return "abil_mana_reduce";
            }
            public String disName() {
                return "Затрат Маны";
            }
            public ItemType icon() {
                return ItemType.BLADE_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DISTANCE};
            }
            public String id() {
                return "distance_inc";
            }
            public String disName() {
                return "Дистанция";
            }
            public ItemType icon() {
                return ItemType.BLADE_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };
    }
}
