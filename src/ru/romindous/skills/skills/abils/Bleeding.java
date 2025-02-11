package ru.romindous.skills.skills.abils;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.utils.EntityUtil;

public class Bleeding {

    public static final IntHashMap<Bleeding> bleeds = new IntHashMap<>();
    public static final BlockData blood = BlockType.NETHER_WART_BLOCK.createBlockData();

    private final WeakReference<LivingEntity> drf;
    private final WeakReference<LivingEntity> trf;
    private final double dmg;

    private int time;

    private Bleeding(final LivingEntity tgt, final double dmg, final int time, final LivingEntity dmgr) {
        this.drf = new WeakReference<>(dmgr);
        this.trf = new WeakReference<>(tgt);
        this.dmg = dmg;

        this.time = time;
    }

    public boolean endTick() {
        if (time-- == 0) return true;
        final LivingEntity tgt = trf.get();
        if (tgt == null || !tgt.isValid()) return true;
        final LivingEntity dmgr = drf.get();
        tgt.damage(dmg, dmgr == null || !dmgr.isValid() ? DamageSource.builder(DamageType.INDIRECT_MAGIC).build()
            : DamageSource.builder(DamageType.INDIRECT_MAGIC).withDirectEntity(dmgr).withCausingEntity(dmgr).build());
        EntityUtil.effect(tgt, Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 0.8f, Particle.DAMAGE_INDICATOR);
        return false;
    }

    public static void bleed(final LivingEntity tgt, final double dmg, final double sec) {
        bleed(tgt, dmg, sec, null);
    }

    public static void bleed(final LivingEntity tgt, final double dmg, final double time, final @Nullable LivingEntity dmgr) {
        final int id = tgt.getEntityId();
        final Bleeding bd = bleeds.get(id);
        final int sec = (int) Math.round(time);
        bleeds.put(id, bd == null ? new Bleeding(tgt, dmg, sec, dmgr)
            : new Bleeding(tgt, dmg + (bd.dmg * bd.time / sec), sec, dmgr));
    }

    public static void effect(final LivingEntity tgt) {
        EntityUtil.effect(tgt, Sound.ITEM_AXE_SCRAPE, 0.6f, Particle.BLOCK, Bleeding.blood);
    }
}
