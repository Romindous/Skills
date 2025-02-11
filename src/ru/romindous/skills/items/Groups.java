package ru.romindous.skills.items;

import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
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

    public static void init() {
        SILVER.conQuest(Entries.silver, ItemType.RAW_IRON);
    }
}
