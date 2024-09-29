package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.ItemBuilder;
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
                new ItemStack(Material.WOODEN_HOE),
                new ItemStack(Material.WOODEN_SHOVEL),
                new ItemStack(Material.WOODEN_PICKAXE),
                ItemUtil.air),

            EquipmentSlot.HEAD, ClassUtil.rndElmt(
                new ItemBuilder(Material.LEATHER_HELMET).color(Color.GRAY).build(),
                new ItemStack(Material.SLIME_BLOCK),
                new ItemStack(Material.AZALEA),
                new ItemStack(Material.GREEN_STAINED_GLASS),
                new ItemStack(Material.CACTUS), ItemUtil.air),
            EquipmentSlot.CHEST, ClassUtil.rndElmt(
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.GRAY).build(), ItemUtil.air),
            EquipmentSlot.LEGS, ClassUtil.rndElmt(
                new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.GREEN).build(), ItemUtil.air),
            EquipmentSlot.FEET, ClassUtil.rndElmt(
                new ItemBuilder(Material.LEATHER_BOOTS).color(Color.GRAY).build(), ItemUtil.air));
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_flesh", new ItemStack(Material.ROTTEN_FLESH), 1, 1, 1), 1)
        .add(RollTree.of(key().value() + "_extra")
            .add(new ItemRoll(key().value() + "_paper", new ItemStack(Material.PAPER), 2, 1, 1), 4)
            .add(new ItemRoll(key().value() + "_hide", new ItemStack(Material.RABBIT_HIDE), 2, 1), 2)
            .add(new ItemRoll(key().value() + "_leather", new ItemStack(Material.LEATHER), 4, 1), 1)
            .add(new ItemRoll(key().value() + "_seeds", new ItemStack(Material.WHEAT_SEEDS), 2, 1), 1)
            .add(new ItemRoll(key().value() + "_kelp", new ItemStack(Material.DRIED_KELP), 1, 1, 2), 3)
            .add(new ItemRoll(key().value() + "_seeds", new ItemStack(Material.CHARCOAL), 1, 1), 2)
            .add(new ItemRoll(key().value() + "_seeds", new ItemStack(Material.FLINT), 2, 1), 1)
            .build(1, 1), 1)
        .build(1, 2);

    @Override
    public RollTree loot() {
        return drop;
    }
}
