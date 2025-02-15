package ru.romindous.skills.items;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.guides.Entries;
import ru.romindous.skills.items.groups.Crawler;
import ru.romindous.skills.items.groups.Medaline;
import ru.romindous.skills.items.groups.Serebrite;

public class Groups {
    public static final Crawler CRAWLER = new Crawler(new ItemBuilder(ItemType.MUTTON).name("§fПолзунина").build());

    public static final Medaline MEDAL = new Medaline(
        new ItemBuilder(ItemType.GOLD_NUGGET).name("§fКусочек медалина").build(),
        new ItemBuilder(ItemType.GLOWSTONE_DUST).name("§fМедалиновая пыль").build(),
        new ItemBuilder(ItemType.GOLDEN_SWORD).name("§fМедалиновый меч").build(),
        new ItemBuilder(ItemType.GOLDEN_PICKAXE).name("§fМедалиновая кирка").build(),
        new ItemBuilder(ItemType.GOLDEN_AXE).name("§fМедалиновый топор").build(),
        new ItemBuilder(ItemType.GOLDEN_HOE).name("§fМедалиновый посох").build(),
        new ItemBuilder(ItemType.GOLDEN_SHOVEL).name("§fМедалиновая лопата").build(),
        new ItemBuilder(ItemType.GOLDEN_HELMET).name("§fМедалиновый шлем").build(),
        new ItemBuilder(ItemType.GOLDEN_CHESTPLATE).name("§fМедалиновый нагрудник").build(),
        new ItemBuilder(ItemType.GOLDEN_LEGGINGS).name("§fМедалиновые поножи").build(),
        new ItemBuilder(ItemType.GOLDEN_BOOTS).name("§fМедалиновые ботинки").build());

    public static final Serebrite SILVER = new Serebrite(
        new ItemBuilder(ItemType.IRON_NUGGET).name("§fКусочек серебрита").glint(true).build(),
        new ItemBuilder(ItemType.IRON_INGOT).name("§fСеребритовый слиток").glint(true).build(),
        new ItemBuilder(ItemType.RAW_IRON).name("§fРудный серебрит").glint(true).build(),
        new ItemBuilder(ItemType.SUGAR).name("§fСеребритовая пыль").glint(true).build(),
        new ItemBuilder(ItemType.IRON_SWORD).name("§fСеребритовый меч").glint(true).build(),
        new ItemBuilder(ItemType.IRON_PICKAXE).name("§fСеребритовая кирка").glint(true).build(),
        new ItemBuilder(ItemType.IRON_AXE).name("§fСеребритовый топор").glint(true).build(),
        new ItemBuilder(ItemType.IRON_HOE).name("§fСеребритовый посох").glint(true).build(),
        new ItemBuilder(ItemType.IRON_SHOVEL).name("§fСеребритовая лопата").glint(true).build(),
        new ItemBuilder(ItemType.CHAINMAIL_HELMET).name("§fСеребритовый шлем").glint(true).build(),
        new ItemBuilder(ItemType.CHAINMAIL_CHESTPLATE).name("§fСеребритовый нагрудник").glint(true).build(),
        new ItemBuilder(ItemType.CHAINMAIL_LEGGINGS).name("§fСеребритовые поножи").glint(true).build(),
        new ItemBuilder(ItemType.CHAINMAIL_BOOTS).name("§fСеребритовые ботинки").glint(true).build(),
        new ItemBuilder(ItemType.PHANTOM_MEMBRANE).name("§fСеребритовая чешуя").glint(true).build());


    private static final Map<ItemType, StaffType> STF_TPS = new HashMap<>();
    public static class StaffType {
        private static final String STF_DATA = "staff.";
        public final ItemType shell;
        public final double spd;
        public final double dmg;
        private StaffType(final ItemType type) {
            shell = getShell(type);
            String tp = type.key().asMinimalString();
            if (tp.length() > 4) tp = tp.substring(0, 4);
            spd = ConfigVars.get(SkillGroup.prefix + STF_DATA + "speed." + tp, 1d);
            dmg = ConfigVars.get(SkillGroup.prefix + STF_DATA + "damage." + tp, 1d);
            STF_TPS.put(type, this);
        }

        private static ItemType getShell(final ItemType ht) {
            if (ht == ItemType.WOODEN_HOE) return ItemType.CONDUIT;
            if (ht == ItemType.STONE_HOE) return ItemType.SKELETON_SKULL;
            if (ht == ItemType.GOLDEN_HOE) return ItemType.HONEY_BLOCK;
            if (ht == ItemType.IRON_HOE) return ItemType.SPAWNER;
            if (ht == ItemType.DIAMOND_HOE) return ItemType.SLIME_BLOCK;
            if (ht == ItemType.NETHERITE_HOE) return ItemType.CHORUS_PLANT;
            return ItemType.DRAGON_EGG;
        }
    }
    public static @Nullable StaffType staff(final ItemStack stf) {
        return STF_TPS.get(stf.getType().asItemType());
    }

    public static void init() {
        SILVER.conQuest(Entries.silver, ItemType.RAW_IRON);
        for (final ItemType it : ItemTags.STAFFS) {
            new StaffType(it);
        }
    }
}
