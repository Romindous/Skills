package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import org.bukkit.Color;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.NARoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.ItemUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.mobs.SednaMob;
import ru.romindous.skills.objects.SkillGroups;

public class Rotting extends SednaMob {

    public String biome() {
        return "bloody_desert";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return PigZombie.class;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of(EquipmentSlot.HAND, ClassUtil.rndElmt(
                SkillGroups.MEDAL.item(ItemType.GOLDEN_AXE),
                SkillGroups.MEDAL.item(ItemType.GOLDEN_SWORD),
                new ItemBuilder(ItemType.WOODEN_PICKAXE).build(),
                ItemUtil.air),

            EquipmentSlot.HEAD, ClassUtil.rndElmt(
                SkillGroups.MEDAL.item(ItemType.GOLDEN_HELMET),
                new ItemBuilder(ItemType.SHROOMLIGHT).build(),
                new ItemBuilder(ItemType.RED_STAINED_GLASS).build(),
                new ItemBuilder(ItemType.CRIMSON_HYPHAE).build(),
                new ItemBuilder(ItemType.NETHER_WART_BLOCK).build(), ItemUtil.air),
            EquipmentSlot.CHEST, ClassUtil.rndElmt(SkillGroups.MEDAL.item(ItemType.GOLDEN_CHESTPLATE), ItemUtil.air,
                new ItemBuilder(ItemType.LEATHER_CHESTPLATE).color(Color.ORANGE).build(), ItemUtil.air),
            EquipmentSlot.LEGS, ClassUtil.rndElmt(SkillGroups.MEDAL.item(ItemType.GOLDEN_LEGGINGS), ItemUtil.air, ItemUtil.air,
                new ItemBuilder(ItemType.LEATHER_LEGGINGS).color(Color.ORANGE).build(), ItemUtil.air),
            EquipmentSlot.FEET, ClassUtil.rndElmt(SkillGroups.MEDAL.item(ItemType.GOLDEN_BOOTS), ItemUtil.air, ItemUtil.air,
                new ItemBuilder(ItemType.LEATHER_BOOTS).color(Color.ORANGE).build(), ItemUtil.air));
    }

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        super.onDeath(e);
        if (!(e.getEntity() instanceof final Mob mb)) return;
        final EntityEquipment oeq = mb.getEquipment();
        final LivingEntity bn = Main.mobs.BONED.spawn(mb.getLocation());
        final EntityEquipment neq = bn.getEquipment();
        for (final EquipmentSlot es : EquipmentSlot.values()) {
            neq.setItem(es, oeq.getItem(es), true);
        }
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_flesh", new ItemBuilder(ItemType.ROTTEN_FLESH).build(), 0, 2), 4)
        .add(new ItemRoll(key().value() + "_nugget", SkillGroups.MEDAL.item(ItemType.GOLD_NUGGET), 1, 2), 2)
        .add(new ItemRoll(key().value() + "_copper", new ItemBuilder(ItemType.RAW_COPPER).build(), 1, 0), 1)
        .add(new ItemRoll(key().value() + "_pork", new ItemBuilder(ItemType.PORKCHOP).build(), 1, 0), 1)
        .add(new NARoll(), 2).build(1, 1);

    @Override
    public RollTree loot() {
        return drop;
    }
}
