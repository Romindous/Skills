package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.ItemUtil;
import ru.romindous.skills.objects.SkillMats;
import ru.romindous.skills.mobs.Mobs;
import ru.romindous.skills.mobs.SednaMob;

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
                SkillMats.MEDAL.getItem(Material.GOLDEN_AXE),
                SkillMats.MEDAL.getItem(Material.GOLDEN_SWORD),
                new ItemStack(Material.WOODEN_PICKAXE),
                ItemUtil.air),

            EquipmentSlot.HEAD, ClassUtil.rndElmt(
                SkillMats.MEDAL.getItem(Material.GOLDEN_HELMET),
                new ItemStack(Material.SHROOMLIGHT),
                new ItemStack(Material.RED_STAINED_GLASS),
                new ItemStack(Material.CRIMSON_HYPHAE),
                new ItemStack(Material.NETHER_WART_BLOCK), ItemUtil.air),
            EquipmentSlot.CHEST, ClassUtil.rndElmt(SkillMats.MEDAL.getItem(Material.GOLDEN_CHESTPLATE), ItemUtil.air),
            EquipmentSlot.LEGS, ClassUtil.rndElmt(SkillMats.MEDAL.getItem(Material.GOLDEN_LEGGINGS), ItemUtil.air),
            EquipmentSlot.FEET, ClassUtil.rndElmt(SkillMats.MEDAL.getItem(Material.GOLDEN_BOOTS), ItemUtil.air));
    }

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        super.onDeath(e);
        if (!(e.getEntity() instanceof final Mob mb)) return;
        Mobs.BONED.spawn(mb.getLocation());
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_flesh", new ItemStack(Material.ROTTEN_FLESH), 2, 1), 4)
        .add(new ItemRoll(key().value() + "_nugget", SkillMats.MEDAL.getItem(Material.GOLD_NUGGET), 1, 1, 3), 2)
        .add(new ItemRoll(key().value() + "_ingot", new ItemStack(Material.COPPER_INGOT), 2, 1), 1)
        .add(new ItemRoll(key().value() + "_pork", new ItemStack(Material.PORKCHOP), 1, 1), 1)
        .build(1, 2);

    @Override
    public RollTree loot() {
        return drop;
    }

    /*private static class WebGoal implements Goal<Mob> {

        private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Ostrov.instance, "web"));

        private final Mob mob;

        private WebGoal(final Mob mob) {
            this.mob = mob;
        }

        @Override
        public boolean shouldActivate() {
            return true;
        }

        @Override
        public boolean shouldStayActive() {
            return true;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void tick() {
        }

        @Override
        public @NotNull
        GoalKey<Mob> getKey() {
            return key;
        }

        @Override
        public @NotNull
        EnumSet<GoalType> getTypes() {
            return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
        }
    }*/
}
