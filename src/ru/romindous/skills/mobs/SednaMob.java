package ru.romindous.skills.mobs;

import javax.annotation.Nullable;
import java.util.Map;
import com.destroystokyo.paper.entity.ai.Goal;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.entities.CustomEntity;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.world.AreaSpawner;
import ru.komiss77.modules.world.LocFinder;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.SM;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.listeners.DeathLst;
import ru.romindous.skills.listeners.EntityDamageLst;
import ru.romindous.skills.config.ConfigVars;

public abstract class SednaMob extends CustomEntity {

    private static final String prefix = "mob.";

    protected final AreaSpawner.SpawnCondition cond = condition();

    protected AreaSpawner.SpawnCondition condition() {
        return new AreaSpawner.SpawnCondition(mobConfig("amount", 1), CreatureSpawnEvent.SpawnReason.NATURAL);
    }

    public final int mana = mobConfig("mana", 1);

    protected final AreaSpawner spawn = new AreaSpawner() {
        @Override
        protected int radius() {
            return mobConfig("radius", 40);
        }

        @Override
        protected int offset() {
            return mobConfig("offset", 16);
        }

        @Override
        protected int yDst() {
            return 1;
        }

        @Override
        protected LocFinder.Check[] checks() {
            return LocFinder.DEFAULT_CHECKS;
        }

        @Override
        public <E extends LivingEntity> SpawnCondition getCondition(final WXYZ loc, final Class<E> cls) {
            return LocUtil.getChEnts(loc, offset(), cls, null).size() < cond.amt()
                && Nms.getBiomeKey(loc).equals(biome()) ? cond : NONE;
        }
    };

    @Override
    protected AreaSpawner spawner() {
        return spawn;
    }

    @Override
    protected int spawnCd() {
        return mobConfig("tick_cd", 40);
    }

    @Override
    protected boolean canBe(final Entity entity, final CreatureSpawnEvent.SpawnReason reason) {
        return reason == cond.reason();
    }

    @Override
    protected void modify(final Entity ent) {
        if (ent instanceof final Mob mb) {
            final Survivor sv = SM.getNearestSurvivor(mb.getLocation(), 240);
            if (sv == null) { //спавн далеко от игроков не нужен
                mb.remove();
                return;
            }
            for (final Map.Entry<Attribute, Double> en : attributes().entrySet()) {
                mb.getAttribute(en.getKey()).setBaseValue(en.getValue());
            }
            final EntityEquipment eq = mb.getEquipment();
            for (final Map.Entry<EquipmentSlot, ItemStack> en : equipment().entrySet()) {
                eq.setItem(en.getKey(), en.getValue());
                eq.setDropChance(en.getKey(), 0f);
            }
            sv.setMobChars(mb);

            final Goal<Mob> gl = goal(mb);
            if (gl != null) Bukkit.getMobGoals().addGoal(mb, 1, gl);
            return;
        }
        ent.remove();
    }

    public abstract String biome();

    public abstract RollTree loot();

    private static final Attributable DEFAULT_ATTS = EntityType.ZOMBIFIED_PIGLIN.getDefaultAttributes();
    protected final Map<Attribute, Double> atts = Map.of(
        Attribute.GENERIC_MAX_HEALTH, mobConfig("max_hp",
            DEFAULT_ATTS.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()),
        Attribute.GENERIC_ARMOR, mobConfig("armor",
            DEFAULT_ATTS.getAttribute(Attribute.GENERIC_ARMOR).getBaseValue()),
        Attribute.GENERIC_ARMOR_TOUGHNESS, mobConfig("tough",
            DEFAULT_ATTS.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getBaseValue()),
        Attribute.GENERIC_ATTACK_DAMAGE, mobConfig("damage",
            DEFAULT_ATTS.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue()),
        Attribute.GENERIC_ATTACK_SPEED, mobConfig("atk_spd",
            DEFAULT_ATTS.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getBaseValue()),
        Attribute.GENERIC_ATTACK_KNOCKBACK, mobConfig("atk_kb",
            DEFAULT_ATTS.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK).getBaseValue()),
        Attribute.GENERIC_MOVEMENT_SPEED, mobConfig("move_spd",
            DEFAULT_ATTS.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue()),
        Attribute.GENERIC_FOLLOW_RANGE, mobConfig("follow",
            DEFAULT_ATTS.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getBaseValue()),
        Attribute.GENERIC_SCALE, mobConfig("scale",
            DEFAULT_ATTS.getAttribute(Attribute.GENERIC_SCALE).getBaseValue()));

    public Map<Attribute, Double> attributes() {
        return atts;
    }

    public abstract Map<EquipmentSlot, ItemStack> equipment();

    public @Nullable Goal<Mob> goal(final Mob mb) {
        return null;
    }

    @Override
    protected void onAttack(final EntityDamageByEntityEvent e) {
        EntityDamageLst.onCustomAttack(e, this);
    }

    @Override
    protected void onHurt(final EntityDamageEvent e) {
        EntityDamageLst.onCustomDefense(e, this);
    }

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        e.getDrops().clear();
        e.setDroppedExp(0);
        DeathLst.onCustomDeath(e, this);
    }

    @Override
    protected void onTarget(final EntityTargetEvent e) {}

    @Override
    protected void onShoot(final ProjectileLaunchEvent e) {}

    @Override
    protected void onPot(final EntityPotionEffectEvent e) {}

    @Override
    protected void onExtra(final EntityEvent e) {}

    public int mobConfig(final String id, final int val) {
        return ConfigVars.get(prefix + key().value() + "." + id, val);
    }

    public double mobConfig(final String id, final double val) {
        return ConfigVars.get(prefix + key().value() + "." + id, val);
    }
}
