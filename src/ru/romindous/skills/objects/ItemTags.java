package ru.romindous.skills.objects;

import java.util.Arrays;
import java.util.Collection;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemType;
import ru.komiss77.OStrap;

public interface ItemTags {

    Collection<ItemType> SWORDS = tag("swords", ItemType.DIAMOND_SWORD, ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD,
        ItemType.WOODEN_SWORD, ItemType.STONE_SWORD, ItemType.NETHERITE_SWORD, ItemType.TRIDENT);

    Collection<ItemType> AXES = tag(ItemTypeTagKeys.AXES, ItemType.DIAMOND_AXE, ItemType.GOLDEN_AXE,
        ItemType.IRON_AXE, ItemType.STONE_AXE, ItemType.NETHERITE_AXE, ItemType.WOODEN_AXE);

    Collection<ItemType> MELEE = tag("melee", ItemType.DIAMOND_SWORD, ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD,
        ItemType.WOODEN_SWORD, ItemType.STONE_SWORD, ItemType.NETHERITE_SWORD, ItemType.TRIDENT, ItemType.DIAMOND_AXE,
        ItemType.GOLDEN_AXE, ItemType.IRON_AXE, ItemType.STONE_AXE, ItemType.NETHERITE_AXE, ItemType.WOODEN_AXE);

    Collection<ItemType> STAFFS = tag(ItemTypeTagKeys.HOES, ItemType.DIAMOND_HOE, ItemType.GOLDEN_HOE,
        ItemType.IRON_HOE, ItemType.STONE_HOE, ItemType.NETHERITE_HOE, ItemType.WOODEN_HOE);

    Collection<ItemType> RANGED = tag("ranged", ItemType.BOW, ItemType.CROSSBOW);

    static Collection<ItemType> tag(final String name, ItemType... types) {
        return OStrap.regTag(TagKey.create(RegistryKey.ITEM, OStrap.key(name)), Arrays.asList(types)).resolve(Registry.ITEM);
    }

    static Collection<ItemType> tag(final TagKey<ItemType> key, ItemType... types) {
        return OStrap.regTag(key, Arrays.asList(types)).resolve(Registry.ITEM);
    }
}
