package ru.romindous.skills.mobs.minons;

import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.world.AreaSpawner;
import ru.romindous.skills.mobs.Minion;

public class MiniRat extends Minion {

    @Override
    protected AreaSpawner.SpawnCondition condition() {
        return COND_EMPTY;
//        return new AreaSpawner.SpawnCondition(mobConfig("amount", 2), CreatureSpawnEvent.SpawnReason.NATURAL);
    }

    @Override
    protected MiniGoal miniGoal(final Mob mb, final LivingEntity owner) {
        return new MSGoal(mb, owner);
    }

    public String biome() {
        return "ruined_city";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return ZombieVillager.class;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_flesh", new ItemStack(Material.ROTTEN_FLESH), 1, 1, 1), 1)
        .build(1, 2);

    @Override
    public RollTree loot() {
        return drop;
    }

    private class MSGoal extends MiniGoal {
        private MSGoal(final Mob mb, final LivingEntity owner) {
            super(mb, owner);
        }

        @Override
        public void tack(final LivingEntity owner) {
            //
        }
    }
}
