package ru.romindous.skills.mobs;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.MobGoals;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.entities.CustomEntity;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.modules.world.AreaSpawner;
import ru.komiss77.modules.world.LocFinder;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.SM;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.listeners.DeathLst;
import ru.romindous.skills.listeners.DamageLst;

public abstract class SednaMob extends CustomEntity {

    private static final LinkedHashMap<Integer, Integer> spawnLimiter = new LinkedHashMap<>();
    private static final MobGoals MOB_GOALS = Bukkit.getMobGoals();
    private static final int LOC_ENCD = 10;
    private static final int COORD_DEL = 6;
    private static final int REM_SPAWN = 10;
    private static final int PER_PLAYER = 4;

    private static final String prefix = "mob.";

    protected final AreaSpawner.SpawnCondition COND_EMPTY = new AreaSpawner.SpawnCondition(0, CreatureSpawnEvent.SpawnReason.CUSTOM);

    protected final AreaSpawner.SpawnCondition cond;
    protected AreaSpawner spawn;
    protected SednaMob() {
        super();
        cond = new AreaSpawner.SpawnCondition(mobConfig("amount", 1),
            CreatureSpawnEvent.SpawnReason.NATURAL);
        Ostrov.log("Registered mob " + key().value());
    }

    protected String prefix() {
        return prefix;
    }

    protected AreaSpawner.SpawnCondition condition() {
        return cond;
    }

    public final int mana = mobConfig("mana", 1);

    protected static boolean limit(final WXYZ loc) {
        final int encd = ((loc.x >> COORD_DEL) << LOC_ENCD) + (loc.z >> COORD_DEL);
        final int bfr = spawnLimiter.getOrDefault(encd, REM_SPAWN) - 1;
        if (spawnLimiter.size() > loc.w.getPlayers().size() * PER_PLAYER) {
            spawnLimiter.pollLastEntry();
        }
        if (bfr == 0) return false;
        spawnLimiter.put(encd, bfr);
        return true;
    }

    @Override
    protected AreaSpawner spawner() {
        return spawn == null ? spawn = new Spawner() {
            protected boolean extra(final WXYZ loc) {return limit(loc);}
        } : spawn;
    }

    @Override
    protected int spawnCd() {
        return mobConfig("tick_cd", 40);
    }

    @Override
    protected boolean canBe(final Entity entity, final CreatureSpawnEvent.SpawnReason reason) {
        return reason == condition().reason();
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
                final Attribute at = en.getKey();
                if (mb.getAttribute(at) == null) mb.registerAttribute(at);
                mb.getAttribute(at).setBaseValue(en.getValue());
            }
            final EntityEquipment eq = mb.getEquipment();
            for (final Map.Entry<EquipmentSlot, ItemStack> en : equipment().entrySet()) {
                eq.setItem(en.getKey(), en.getValue());
                eq.setDropChance(en.getKey(), 0f);
            }
            sv.setMobChars(mb);

            final Goal<Mob> gl = goal(mb);
            if (gl != null) {
//                for (final GoalType gt : gl.getTypes())
//                    MOB_GOALS.removeAllGoals(mb, gt);
                MOB_GOALS.addGoal(mb, 1, gl);
            }
            return;
        }
        ent.remove();
    }

    public abstract String biome();

    public abstract RollTree loot();

    private final Attributable DEFAULT_ATTS = Nms.typeByClass(getEntClass()).getDefaultAttributes();

    protected final Map<Attribute, Double> atts = modAttr(Map.of(
        Attribute.MAX_HEALTH, "max_hp", Attribute.ARMOR, "armor",
        Attribute.ARMOR_TOUGHNESS, "tough", Attribute.ATTACK_DAMAGE, "damage",
        Attribute.ATTACK_SPEED, "atk_spd", Attribute.ATTACK_KNOCKBACK, "atk_kb",
        Attribute.MOVEMENT_SPEED, "move_spd", Attribute.FOLLOW_RANGE, "follow",
        Attribute.SCALE, "scale", Attribute.ENTITY_INTERACTION_RANGE, "range"));

    public Map<Attribute, Double> attributes() {
        return atts;
    }

    public abstract Map<EquipmentSlot, ItemStack> equipment();

    public @Nullable Goal<Mob> goal(final Mob mb) {
        return null;
    }

    @Override
    protected void onAttack(final EntityDamageByEntityEvent e) {
        DamageLst.onCustomAttack(e, this);
    }

    @Override
    protected void onHurt(final EntityDamageEvent e) {
        DamageLst.onCustomDefense(e, this);
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

    public int mobConfig(final String id, final int val) {
        return ConfigVars.get(prefix() + key().value() + "." + id, val);
    }

    public double mobConfig(final String id, final double val) {
        return ConfigVars.get(prefix() + key().value() + "." + id, val);
    }

    private Map<Attribute, Double> modAttr(final Map<Attribute, String> atts) {
        final Map<Attribute, Double> map = new HashMap<>();
        atts.forEach((at, nm) -> {
            final AttributeInstance ai = DEFAULT_ATTS.getAttribute(at);
            if (ai == null) return;
            map.put(at, mobConfig(nm, ai.getBaseValue()));
        });
        return map;
    }

    protected abstract class Spawner extends AreaSpawner {
        public final int radius = mobConfig("radius", 40);
        public final int offset = mobConfig("offset", 16);

        @Override
        protected int radius() {
            return radius;
        }

        @Override
        protected int offset() {
            return offset;
        }

        @Override
        protected int yDst() {
            return 1;
        }

        @Override
        protected LocFinder.Check[] checks() {
            return LocFinder.DEFAULT_CHECKS;
        }

        public final SpawnCondition condition(final WXYZ loc) {
            if (!Nms.getBiomeKey(loc).equals(biome())) return NONE;
            return LocUtil.getChEnts(loc, NumUtil.abs(radius - offset),
                    getEntClass(), e -> SednaMob.this.equals(CustomEntity.get(e)))
                .isEmpty() && extra(loc) ? cond : NONE;
        }

        protected abstract boolean extra(final WXYZ loc);
    }
}
