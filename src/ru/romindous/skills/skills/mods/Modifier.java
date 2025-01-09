package ru.romindous.skills.skills.mods;

import javax.annotation.Nullable;
import java.util.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemType;
import ru.komiss77.notes.OverrideMe;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Rarity;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.objects.Scroll;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.abils.InvCondition;

public abstract class Modifier implements Scroll {//модификатор

    public static final Map<String, Modifier> VALUES = new HashMap<>();
    public static final Map<Rarity, List<Modifier>> RARITIES = new EnumMap<>(Rarity.class);

    public static final String prefix = "mods.";
    public static final String data = "mod";

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

    @OverrideMe
    protected String needs() {
        return "";
    }

    public String side() {
        return "💠";
    }

    public abstract ItemType icon();

    public abstract Chastic[] chastics();

    public double modify(final Chastic ch, final double def, final int lvl, final @Nullable Chain info) {
        final Mod md = chMods[ch.ordinal()];
        if (md == null) return def;
        return def + NumUtil.absMin(md.conScale * lvl + md.conBase,
            (md.mulScale * lvl + md.mulBase) * def);
    }

    /*protected double modified(final double def, final Chastic ch, final int level) {
        final Mod md = chMods[ch.ordinal()];
        if (md == null) return def;
        return def + NumUtil.absMin(md.conScale * level + md.conBase,
            (md.mulScale * level + md.mulBase) * def);
    }*/

    public record ModState(Modifier mod, int lvl) {}

    private static final byte SIG_FIGS = 2;

    private String relate(final Modifier md, final Skill sk) {
        if (sk == null) return "";
        final Chastic[] chs = md.chastics();
        Arrays.sort(chs);
        return null;
    }

    @Override
    public String[] desc(final int lvl) {
        final List<String> dscs = new ArrayList<>();
        dscs.add(TCUtil.N + "Применимая роль: " + (role() == null ? Role.ANY : role().disName()));
        dscs.add("<dark_gray>Модифицирует:");
        for (final Chastic ch : chastics()) {
            final Mod md = chMods[ch.ordinal()];
            dscs.add(TCUtil.N + "◇ " + ch.disName() + TCUtil.N + " - на");
            dscs.add(ch.color() + StringUtil.toSigFigs(md.conScale * lvl + md.conBase, SIG_FIGS)
                + TCUtil.N + " или " + ch.color() +
                StringUtil.toSigFigs(md.mulScale * lvl + md.mulBase * 100f, SIG_FIGS) +
                "%" + TCUtil.N + ", смотря что ниже");
        }
        final String nds = needs();
        if (!nds.isEmpty()) {
            dscs.add("<dark_gray>Требования:");
            dscs.add(nds.replace(CLR, rarity().color()));
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
            public String name() {
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
            public String name() {
                return "Полученный Урон";
            }
            public ItemType icon() {
                return ItemType.HEARTBREAK_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
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
            public String name() {
                return "Количество";
            }
            public ItemType icon() {
                return ItemType.PLENTY_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
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
            public String name() {
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
            public String name() {
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
            public String name() {
                return "Стремление";
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
            public String name() {
                return "Насыщение";
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
            public String name() {
                return "Рефляция";
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
                return "abil_mana_dec";
            }
            public String name() {
                return "Расход";
            }
            public ItemType icon() {
                return ItemType.BURN_POTTERY_SHERD;
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
            public String name() {
                return "Зазор";
            }
            public ItemType icon() {
                return ItemType.EXPLORER_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return null;}
        };

        //mage
        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.MANA, Chastic.COOLDOWN};
            }
            public String id() {
                return "abil_mana_to_cd";
            }
            public String name() {
                return "Кандиас";
            }
            public ItemType icon() {
                return ItemType.BREWER_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.MAGE;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_DEALT};
            }
            public String id() {
                return "burning_dmg_inc";
            }
            public String name() {
                return "Ингорто";
            }
            protected String needs() {
                return TCUtil.N + "Поджег указаной " + CLR + "цели";
            }
            public double modify(Chastic ch, double def, int lvl, @Nullable Chain info) {
                if (info == null || info.target().getFireTicks() < 1) return def;
                return super.modify(ch, def, lvl, info);
            }
            public ItemType icon() {
                return ItemType.BURN_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return Role.MAGE;}
        };

        //warior
        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_DEALT, Chastic.DISTANCE};
            }
            public String id() {
                return "dmg_and_dst_inc";
            }
            public String name() {
                return "Минера";
            }
            public ItemType icon() {
                return ItemType.BLADE_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.COMMON;
            }
            public Role role() {return Role.WARRIOR;}
        };

        new Modifier() {
            public Chastic[] chastics() {
                return new Chastic[] {Chastic.DAMAGE_TAKEN};
            }
            public String id() {
                return "shield_hurt_dec";
            }
            public String name() {
                return "Шандре";
            }
            protected String needs() {
                return TCUtil.N + "Экипировка щита " + CLR + "пользователем";
            }
            public double modify(Chastic ch, double def, int lvl, @Nullable Chain info) {
                if (info == null || InvCondition.SHIELD_OFF.test(info.caster()
                    .getEquipment()) != EquipmentSlot.OFF_HAND) return def;
                return super.modify(ch, def, lvl, info);
            }
            public ItemType icon() {
                return ItemType.HEARTBREAK_POTTERY_SHERD;
            }
            public Rarity rarity() {
                return Rarity.UNCOM;
            }
            public Role role() {return Role.WARRIOR;}
        };
    }
}
