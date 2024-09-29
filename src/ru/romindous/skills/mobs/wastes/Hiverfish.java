package ru.romindous.skills.mobs.wastes;

import org.bukkit.Location;
import org.bukkit.Material;
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
import ru.komiss77.Ostrov;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.items.ItemRoll;
import ru.romindous.skills.objects.SkillMats;
import ru.romindous.skills.mobs.Mobs;
import ru.romindous.skills.mobs.SednaMob;

import java.util.Map;

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
        Mobs.CLUTCHER.spawn(e.getEntity().getLocation());
    }

    private static final float THORN = 0.4f;

    @Override
    protected void onHurt(final EntityDamageEvent e) {
        super.onHurt(e);
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof final Mob mb)) return;
        if (e.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr && Ostrov.random.nextFloat() < THORN) {
            dmgr.damage(mb.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue() * THORN,
                DamageSource.builder(DamageType.THORNS).withCausingEntity(mb).withDirectEntity(mb).build());
        }

    }

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        super.onDeath(e);
        if (!(e.getEntity() instanceof final Mob mb)) return;
        final Location loc = mb.getLocation();
        for (int i = Ostrov.random.nextInt(3) + 1; i != 0; i--) {
            Mobs.CLUTCHER.spawn(loc);
        }
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_bone", new ItemStack(Material.BONE), 1, 1), 2)
        .add(new ItemRoll(key().value() + "_scales", SkillMats.SILVER.getItem(Material.PHANTOM_MEMBRANE), 2, 1, 1), 1)
        .add(new ItemRoll(key().value() + "_meal", new ItemStack(Material.BONE_MEAL), 2, 1), 1)
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
