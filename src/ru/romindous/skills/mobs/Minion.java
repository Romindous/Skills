package ru.romindous.skills.mobs;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.AreaSpawner;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.LocUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.listeners.DamageLst;

public abstract class Minion extends SednaMob {

    private static final String prefix = "mob.mini.";

    protected Minion() {
        super();
    }

    protected String prefix() {
        return prefix;
    }

    @Override
    protected AreaSpawner spawner() {
        return null;
    }

    @Override
    protected AreaSpawner.SpawnCondition condition() {
        return COND_EMPTY;
    }

    @Override
    protected int spawnCd() {
        return -1;
    }

    @Override
    public String biome() {
        return "";
    }

    @Override
    protected void modify(final Entity ent) {
        if (ent instanceof final Mob mb) {
            final LivingEntity own = ownerOf(ent);
            final MinionSpawnEvent se = new MinionSpawnEvent(mb, this, own, cond.reason());
            if (own == null || !se.callEvent()) {
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
            final Goal<Mob> gl = miniGoal(mb, own);
            if (gl != null) Bukkit.getMobGoals().addGoal(mb, 1, gl);

            if (own instanceof final Player pl) {
                final Survivor sv = PM.getOplayer(pl, Survivor.class);
                if (sv == null) return;
                Stat.modMini(mb, sv.getStat(Stat.CONTROL));
                sv.trigger(Trigger.SPAWN_MINION, se, pl);
            }
            return;
        }
        ent.remove();
    }

    protected abstract MiniGoal miniGoal(final Mob mb, final LivingEntity owner);

    @Override
    protected void onAttack(final EntityDamageByEntityEvent e) {
        if (!(e.getDamageSource().getCausingEntity() instanceof final Mob mb)) return;
        final LivingEntity own = ownerOf(mb);
        if (own == null) {
            mb.remove();
            return;
        }
        if (own.getEntityId() == e.getEntity().getEntityId()) {
            e.setCancelled(true);
            e.setDamage(0d);
            return;
        }
        super.onAttack(e);
    }

    @Override
    protected void onHurt(final EntityDamageEvent e) {
        if (!(e.getEntity() instanceof final Mob mb)) return;
        final LivingEntity own = ownerOf(mb);
        if (own == null) {
            mb.remove();
            return;
        }
        if (!(e.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr)) return;
        if (own.getEntityId() == dmgr.getEntityId()) {
            e.setCancelled(true);
            e.setDamage(0d);
            return;
        }
        DamageLst.onCustomDefense(e, this);
    }

    @Override
    protected void onTarget(final EntityTargetEvent e) {
        if (!(e.getTarget() instanceof final LivingEntity tgt)
            || !isOwner(e.getEntity(), tgt)) return;
        e.setCancelled(true);
    }

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        e.getDrops().clear();
        e.setDroppedExp(0);
    }

    private final int life = value("life", 100);
    protected int ticksLife() {
        return life;
    }

    public LivingEntity spawn(final Location loc, final LivingEntity owner) {
        return loc.getWorld().spawn(loc, getEntClass(), CreatureSpawnEvent.SpawnReason.SPELL, false, e -> apply(e, owner));
    }

    public void apply(final Entity ent, final LivingEntity owner) {
        ent.getPersistentDataContainer().set(KEY, ID_TYPE, owner.getUniqueId());
        apply(ent);
    }

    private static final NamespacedKey KEY = new NamespacedKey(Main.main, "minion");
    private static final PersistentDataType<byte[], UUID> ID_TYPE = new PersistentDataType<>() {
        public Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }
        public Class<UUID> getComplexType() {
            return UUID.class;
        }
        public byte[] toPrimitive(final UUID uid, final PersistentDataAdapterContext cont) {
            final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uid.getMostSignificantBits());
            bb.putLong(uid.getLeastSignificantBits());
            return bb.array();
        }
        public UUID fromPrimitive(final byte[] bar, final PersistentDataAdapterContext cont) {
            final ByteBuffer bb = ByteBuffer.wrap(bar);
            final long firstLong = bb.getLong();
            final long secondLong = bb.getLong();
            return new UUID(firstLong, secondLong);
        }
    };

    @Nullable
    public static LivingEntity ownerOf(final Entity ent) {
        final UUID id = ent.getPersistentDataContainer().get(KEY, ID_TYPE);
        if (id == null) return null;
        final Entity own = ent.getWorld().getEntity(id);
        return own instanceof LivingEntity ? (LivingEntity) own : null;
    }

    public static boolean isOwner(final Entity ent, final @Nullable LivingEntity owner) {
        final UUID id = ent.getPersistentDataContainer().get(KEY, ID_TYPE);
        if (id == null) return owner == null;
        return owner != null && owner.getUniqueId().equals(id);
    }

    private static final int AGRO_DST = 40;
    public static void setAgroOf(final LivingEntity owner, final LivingEntity target) {
        final Location olc = owner.getLocation();
        //Bukkit.getConsoleSender().sendMessage("ow-" + owner.getType().name() +  " tgt1-" + target.getType().name());
        for (final Mob mb : LocUtil.getChEnts(new WXYZ(olc), AGRO_DST, Mob.class, ent -> {
            final LivingEntity tgt = ent.getTarget();
            return isOwner(ent, owner) && (tgt == null || !tgt.isValid());
        })) mb.setTarget(target);
    }

    protected abstract class MiniGoal implements Goal<Mob> {

        private static final EnumSet<GoalType> types = EnumSet.noneOf(GoalType.class);

        private final GoalKey<Mob> key = GoalKey.of(Mob.class, Minion.this.getKey());
        private final WeakReference<LivingEntity> ownRef;

        protected final Mob mb;
        protected int tick = 0;

        protected MiniGoal(final Mob mb, final LivingEntity owner) {
            this.ownRef = new WeakReference<>(owner);
            this.mb = mb;
        }

        @Override
        public boolean shouldActivate() {
            return true;
        }

        @Override
        public void tick() {
            if (!mb.isValid()) return;
            tick++;
            if (tick > ticksLife()) {
                mb.remove();
                stop();
                return;
            }
            final LivingEntity own = ownRef.get();
            if (own == null || !own.isValid()) {
                mb.remove();
                stop();
                return;
            }

            tack(own);
        }

        public abstract void tack(final LivingEntity owner);

        @Override
        public @NotNull GoalKey<Mob> getKey() {
            return key;
        }

        @Override
        public @NotNull EnumSet<GoalType> getTypes() {
            return types;
        }
    }

    public static class MinionSpawnEvent extends CreatureSpawnEvent {

        private final Minion mini;
        private final LivingEntity owner;

        public MinionSpawnEvent(final Mob ent, final Minion mini, final LivingEntity owner, final SpawnReason reason) {
            super(ent, reason);
            this.owner = owner;
            this.mini = mini;
        }

        @Override
        public Mob getEntity() {
            return (Mob) super.getEntity();
        }

        public LivingEntity getOwner() {
            return owner;
        }

        public Minion getMinion() {
            return mini;
        }
    }
}