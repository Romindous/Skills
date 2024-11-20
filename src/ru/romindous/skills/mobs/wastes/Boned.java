package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.romindous.skills.Main;
import ru.romindous.skills.mobs.SednaMob;

public class Boned extends SednaMob {

    public String biome() {
        return "iron_hills";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return Skeleton.class;
    }

    private final double scale = mobConfig("scale", 1d);
    private final double min_scl = mobConfig("min_scl", 0.6d);
    @Override
    public Map<Attribute, Double> attributes() {
        atts.put(Attribute.SCALE, (min_scl - scale) * Main.srnd.nextDouble() + scale);
        return atts;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_bone", new ItemStack(Material.BONE), 2, 1), 1)
        .add(new ItemRoll(key().value() + "_meal", new ItemStack(Material.BONE_MEAL), 1, 1, 1), 2)
        .build(1, 2);

    @Override
    public RollTree loot() {
        return drop;
    }
}
