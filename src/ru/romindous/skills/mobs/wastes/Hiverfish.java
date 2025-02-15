package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.NARoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.romindous.skills.Main;
import ru.romindous.skills.items.Groups;
import ru.romindous.skills.mobs.SednaMob;

public class Hiverfish extends SednaMob {

    public String biome() {
        return "iron_hills";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return Silverfish.class;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    @Override
    protected void onAttack(final EntityDamageByEntityEvent e) {
        super.onAttack(e);
        if (e.isCancelled()) return;
        if (!(e.getDamageSource().getCausingEntity() instanceof final Mob mb)) return;
        if (DamageType.THORNS.equals(e.getDamageSource().getDamageType())) return;
        Main.mobs.CLUTCHER.spawn(e.getEntity().getLocation());
    }

    private static final float THORN = 0.4f;

    @Override
    protected void onHurt(final EntityDamageEvent e) {
        super.onHurt(e);
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof final Mob mb)) return;
        if (e.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr && Ostrov.random.nextFloat() < THORN) {
            dmgr.damage(mb.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue() * THORN,
                DamageSource.builder(DamageType.THORNS).withCausingEntity(mb).withDirectEntity(mb).build());
        }

    }

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        super.onDeath(e);
        if (!(e.getEntity() instanceof final Mob mb)) return;
        final Location loc = mb.getLocation();
        for (int i = Ostrov.random.nextInt(3) + 1; i != 0; i--) {
            Main.mobs.CLUTCHER.spawn(loc);
        }
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_bone", new ItemBuilder(ItemType.BONE).build(), 1, 0), 2)
        .add(new ItemRoll(key().value() + "_scales", Groups.SILVER.item(ItemType.PHANTOM_MEMBRANE), 1, 0), 1)
        .add(new ItemRoll(key().value() + "_meal", new ItemBuilder(ItemType.BONE_MEAL).build(), 1, 1), 4)
        .add(new NARoll(), 4).build(1, 1);

    @Override
    public RollTree loot() {
        return drop;
    }
}
