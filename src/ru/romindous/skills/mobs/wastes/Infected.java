package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import org.bukkit.Color;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.NARoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.romindous.skills.mobs.SednaMob;

public class Infected extends SednaMob {

    public String biome() {
        return "ruined_city";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return ZombieVillager.class;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of(EquipmentSlot.HAND, ClassUtil.rndElmt(
                new ItemBuilder(ItemType.WOODEN_HOE).build(),
                new ItemBuilder(ItemType.WOODEN_SHOVEL).build(),
                new ItemBuilder(ItemType.WOODEN_PICKAXE).build(),
                ItemUtil.air),

            EquipmentSlot.HEAD, ClassUtil.rndElmt(
                new ItemBuilder(ItemType.LEATHER_HELMET).color(Color.GREEN).build(),
                new ItemBuilder(ItemType.SLIME_BLOCK).build(),
                new ItemBuilder(ItemType.AZALEA).build(),
                new ItemBuilder(ItemType.GREEN_STAINED_GLASS).build(),
                new ItemBuilder(ItemType.CACTUS).build(), ItemUtil.air),
            EquipmentSlot.CHEST, ClassUtil.rndElmt(
                new ItemBuilder(ItemType.LEATHER_CHESTPLATE).color(Color.GRAY).build(), ItemUtil.air),
            EquipmentSlot.LEGS, ClassUtil.rndElmt(
                new ItemBuilder(ItemType.LEATHER_LEGGINGS).color(Color.GREEN).build(), ItemUtil.air),
            EquipmentSlot.FEET, ClassUtil.rndElmt(
                new ItemBuilder(ItemType.LEATHER_BOOTS).color(Color.GRAY).build(), ItemUtil.air));
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_flesh", new ItemBuilder(ItemType.ROTTEN_FLESH).build(), 1, 1), 2)
        .add(RollTree.of(key().value() + "_extra")
            .add(new ItemRoll(key().value() + "_paper", new ItemBuilder(ItemType.PAPER).build(), 1, 1), 2)
            .add(new ItemRoll(key().value() + "_hide", new ItemBuilder(ItemType.RABBIT_HIDE).build(), 1, 0), 2)
            .add(new ItemRoll(key().value() + "_seeds", new ItemBuilder(ItemType.WHEAT_SEEDS).build(), 1, 0), 1)
            .add(new ItemRoll(key().value() + "_kelp", new ItemBuilder(ItemType.DRIED_KELP).build(), 1, 2), 2)
            .add(new ItemRoll(key().value() + "_coal", new ItemBuilder(ItemType.CHARCOAL).build(), 1, 0), 2)
            .add(new ItemRoll(key().value() + "_flint", new ItemBuilder(ItemType.FLINT).build(), 1, 0), 1)
            .build(1, 0), 1)
        .add(new NARoll(), 2)
        .build(1, 0);

    @Override
    public RollTree loot() {
        return drop;
    }
}
