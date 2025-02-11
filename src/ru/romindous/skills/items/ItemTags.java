package ru.romindous.skills.items;

import java.util.Arrays;
import java.util.Set;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.boot.OStrap;

public interface ItemTags {

    Set<ItemType> SWORDS = OStrap.getAll(tag("swords", ItemType.DIAMOND_SWORD, ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD,
        ItemType.WOODEN_SWORD, ItemType.STONE_SWORD, ItemType.NETHERITE_SWORD, ItemType.TRIDENT).tagKey(), RegistryKey.ITEM);

    Set<ItemType> AXES = OStrap.getAll(ItemTypeTagKeys.AXES, RegistryKey.ITEM);

    Set<ItemType> MELEE = OStrap.getAll(tag("melee", ItemType.DIAMOND_SWORD, ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD,
        ItemType.WOODEN_SWORD, ItemType.STONE_SWORD, ItemType.NETHERITE_SWORD, ItemType.TRIDENT, ItemType.DIAMOND_AXE,
        ItemType.GOLDEN_AXE, ItemType.IRON_AXE, ItemType.STONE_AXE, ItemType.NETHERITE_AXE, ItemType.WOODEN_AXE).tagKey(), RegistryKey.ITEM);

    Set<ItemType> STAFFS = OStrap.getAll(ItemTypeTagKeys.HOES, RegistryKey.ITEM);

    Set<ItemType> RANGED = OStrap.getAll(tag("ranged", ItemType.BOW, ItemType.CROSSBOW).tagKey(), RegistryKey.ITEM);

    static Tag<@NotNull ItemType> tag(final String name, ItemType... types) {
        return OStrap.regTag(TagKey.create(RegistryKey.ITEM, OStrap.key(name)), Arrays.asList(types));
    }
}
