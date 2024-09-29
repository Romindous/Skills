package ru.romindous.skills.mobs.wastes;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.items.ItemRoll;
import ru.romindous.skills.mobs.SednaMob;

import java.util.Map;

public class Steed extends SednaMob {

    public String biome() {
        return "dry_ocean";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return ZombieHorse.class;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_flesh", new ItemStack(Material.ROTTEN_FLESH), 1, 2, 2), 4)
        .add(new ItemRoll(key().value() + "_kelp", new ItemStack(Material.DRIED_KELP), 1, 1, 2), 2)
        .add(new ItemRoll(key().value() + "_leather", new ItemStack(Material.LEATHER), 2, 1), 1)
        .build(1, 2);

    @Override
    protected void onAttack(final EntityDamageByEntityEvent e) {
        super.onAttack(e);
        if (e.isCancelled()) return;
        if (!(e.getDamageSource().getCausingEntity() instanceof final Mob mb)) return;
        mb.setTarget(null);
    }

    @Override
    protected void onHurt(final EntityDamageEvent e) {
        super.onHurt(e);
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof final Mob mb)) return;
        if (e.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr) {
            mb.setTarget(dmgr);
        }
    }

    @Override
    public RollTree loot() {
        return drop;
    }
}
