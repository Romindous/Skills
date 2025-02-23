package ru.romindous.skills.items;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.modules.items.ItemGroup;
import ru.komiss77.modules.quests.Quest;
import ru.komiss77.utils.ClassUtil;
import ru.romindous.skills.config.ConfigVars;

public abstract class SkillGroup extends ItemGroup {

    public static final String prefix = "item.";

    private final Map<ItemType, Quest> quested = new HashMap<>();

    protected SkillGroup(final ItemStack... its) {
        super(its);
    }

    @Nullable
    public static SkillGroup get(final ItemStack it) {
        final String id = it.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        return id == null ? null : get(id);
    }

    @Nullable
    public static SkillGroup get(final String id) {
        return ClassUtil.cast(ItemGroup.get(id), SkillGroup.class);
    }

    public Quest quest(final ItemType type) {
        return quested.get(type);
    }

    public SkillGroup conQuest(final Quest qs, final ItemType... its) {
        for (final ItemType it : its) quested.put(it, qs); return this;
    }

    protected static boolean has(final EquipmentSlot[] ess, final EquipmentSlot e) {
        for (final EquipmentSlot es : ess) if (es == e) return true;
        return false;
    }

    protected int itemConfig(final String id, final int val) {
        return ConfigVars.get(prefix + key().value() + "." + id, val);
    }

    protected double itemConfig(final String id, final double val) {
        return ConfigVars.get(prefix + key().value() + "." + id, val);
    }

    /*DEFAULT(),//10
    MEDALINE("GOLD_NUGGET&1&§fКусочек медалина&&11", 
    		"GLOWSTONE_DUST&1&§fМедалиновый порошок&&11"),//11
    SEREBRITE("RAW_IRON&1&§fРудный серебрит&&12&G", 
    		"IRON_INGOT&1&§fCеребритовый слиток&&12&G", 
    		"IRON_NUGGET&1&§fКусочек серебрита&&12&G", 
    		"GUNPOWDER&1&§fСеребритовый порошок&&12&G"),//12
    SPARK("GUNPOWDER&1&§eЗаряженый порох&;§7Издаёт §eискры;§7падает с босса&13&G", 
    		"FLINT&1&§eЭлектро-Кремень&&13&G"),//13
    GRENADE,//14
    AZULITE("PRISMARINE_SHARD&1&§eАзулит&&15&G"),//15
    BONED("PHANTOM_MEMBRANE&1&§fКостяная пластина&&16"),//16
    UNDEAD("BONE_MEAL&1&§eОживший прах&;§7До сих пор §eшевелится;§7падает с босса&17&G", 
    		"BONE&1&§eОживший реликт&&17&G"),//17
    LUCKOR("RAW_GOLD&1&§eРудная лукория&&18&G", 
    		"GOLD_INGOT&1&§bЛукориевый слиток&&18&G", 
    		"GOLD_NUGGET&1&§bЛукориевый кусочек&&18&G", 
    		"GLOWSTONE_DUST&1&§bЛукориевый порошок&&18&G", 
    		"ENDER_PEARL&1&§bАлчный жемчуг&&18&G"),//18
    WAXED("ORANGE_DYE&1&§fВосковая гуща&&19", 
    		"CLAY_BALL&1&§eРудный восковит&&19", 
    		"BRICK&1&§eВосковитовый слиток&&19"),//19
    AMETHYST("POPPED_CHORUS_FRUIT&1&§eАметистовая пластина&&20"),//20
    BRAID("STRING&1&§fРастительное волокно&&21"),//21
    EXTRA("GLOWSTONE_DUST&1&§fЗолотой порошок&&22", 
    		"GUNPOWDER&1&§fЖелезный порошок&&22", 
    		"GREEN_DYE&1&§eСвязка бамбука&&22"),//22
    ACID("FERMENTED_SPIDER_EYE&1&§bКатализатор&;§7Ускоряет внутренние;§bреакции§7, падает с босса&23&G"),//23
    HELLITE("NETHER_BRICK&1&§eХеллитовый слиток&&24"),//24
    BLAZE("BLAZE_ROD&1&§dОкисленный стержень&&25&G", 
    		"COPPER_INGOT&1&§bПолыхающий слиток&&25&G", 
    		"ENDER_EYE&1&§dОко жадности&&25&G"),//25
    RESTILE("AMETHYST_SHARD&1&§bОсколок ристалии&&26&G"),//26
    WITHER("CHARCOAL&1&§bВизерит&&27"),//27
    BARATRA("RABBIT_HIDE&1&§dШкура зверя&;§dПрочная §7и §dчерствая;§7падает с босса&28&G", 
    		"BRICK&1&§dБаратровый слиток&&28&G"),//28
    TURTLE(),//29
    SIPHOR("BLACK_DYE&1&§bТемная связка&&30&G", 
    		"GLOW_INK_SAC&1&§dФрагмент эха&&30", 
    		"HEART_OF_THE_SEA&1&§5Яремное эхо&;§7Заключенный §5звук;§7охваченый кристалами&30"),//30
    CHITIN("RED_DYE&1&§dХитиновая чешуя&&31&G"),//31
    VOID("PRISMARINE_CRYSTALS&1&§dГлубинный кристал&;§7Взывает к §dпустоте;§7падает с босса&32&G", 
    		"ENDER_EYE&1&§5Жемчуг пустоты&&32&G", 
    		"ECHO_SHARD&1&§5Стержень пустоты&&32&G"),//32
    SHULK("LIGHT_GRAY_DYE&1&§bЭссенция шалка&&33", 
    		"WHITE_DYE&1&§dШалковая связка&&33&G", 
    		"GHAST_TEAR&1&§dШалковый экстракт&&33&G"),//33
    CALYX("POPPED_CHORUS_FRUIT&1&§5Калексовая пластина&&34&G"),//34
    IRRITE("PHANTOM_MEMBRANE&1&§dПлотная клетчатка&;§dЛегкая §7как перо;§7падает с босса&35&G", 
    		"NETHERITE_SCRAP&1&§5Ирритовая пластина&&35&G"),//34
    ;

    private static final EnumMap<CustomItems, ItemStack[]> materials = new EnumMap<>(CustomItems.class);

    static {
        for (final CustomItems ci : CustomItems.values()) {
            if (ci.mats != null && ci.mats.length != 0) {
                final ItemStack[] its = new ItemStack[ci.mats.length], 0; i < its.length; i++) {
                    its[i] = Crafts.getItemStack(ci.mats[i]);
                }
                materials.put(ci, its);
            }
        }
    }

    private String[] mats;

    private CustomItems(final String... mats) {
        this.mats = mats;
    }
    
    public static CustomItems getCstmItm(final ItemMeta im) {
        if (im != null && im.hasCustomModelData()) {
            final int model = im.getCustomModelData() - 10;
            if (model>0 && model<values().length) {
                return values()[model];
            }
        }
        return DEFAULT;
    }
    
    public static CustomItems getCstmItm(final Integer cmd) {
        if (cmd == null) return null, cmd - 10;
        if (model>0 && model<values().length) {
            return values()[model];
        }
        return DEFAULT;
    }

    public static ItemStack[] getCustomMats(final ItemMeta im) {
        return getCustomMats(getCstmItm(im));
    }

    private static final ItemStack[] eis = new ItemStack[0];
    public static ItemStack[] getCustomMats(final CustomItems ci) {
    	final ItemStack[] is = materials.val(ci);
        return is == null ? eis : is;
    }

    public float getPercentArmor(final ItemStack[] armor) {
        int part = 0;
        switch (armor.length) {
            case 4:
                if (!ItemUtil.isBlankItem(armor[3], true) && getCstmItm(armor[3].getItemMeta()) == this) {
                    part += 2;
                }
            case 3:
                if (!ItemUtil.isBlankItem(armor[2], true) && getCstmItm(armor[2].getItemMeta()) == this) {
                    part += 4;
                }
            case 2:
                if (!ItemUtil.isBlankItem(armor[1], true) && getCstmItm(armor[1].getItemMeta()) == this) {
                    part += 3;
                }
            case 1:
                if (!ItemUtil.isBlankItem(armor[0], true) && getCstmItm(armor[0].getItemMeta()) == this) {
                    part += 1;
                }
                return part * 0.1f;
            default:
                return 0f;
        }
    }*/
}
