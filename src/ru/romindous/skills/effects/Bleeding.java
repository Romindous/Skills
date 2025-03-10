package ru.romindous.skills.effects;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import ru.komiss77.modules.effects.CustomEffect;
import ru.komiss77.utils.EntityUtil;
import ru.romindous.skills.objects.Effects;

public class Bleeding extends CustomEffect<Bleeding.Bleed> {

    public static final BlockData blood = BlockType.NETHER_WART_BLOCK.createBlockData();

    protected void onStart(final LivingEntity tgt, final Bleed in) {
        EntityUtil.effect(tgt, Sound.ITEM_AXE_SCRAPE, 0.6f, Particle.BLOCK, Bleeding.blood);
        new ParticleBuilder(Particle.SWEEP_ATTACK).location(EntityUtil.center(tgt))
            .count(2).receivers(40).spawn();
    }

    public void apply(final LivingEntity tgt, final int ticks, final double power) {
        apply(tgt, new Bleed(ticks, power, true));
    }

    public void apply(final LivingEntity tgt, final int ticks, final double power, final boolean visible) {
        apply(tgt, new Bleed(ticks, power, visible));
    }

    protected void affect(final LivingEntity tgt, final Bleed in) {
        tgt.damage(in.power(), DamageSource.builder(DamageType.INDIRECT_MAGIC).build());
        EntityUtil.effect(tgt, Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 1.4f, Particle.DAMAGE_INDICATOR);
        tgt.setNoDamageTicks(0);
    }

    protected int period(final LivingEntity tgt, final Bleed in) {
        return 20;
    }

    protected Color color() {
        return Color.MAROON;
    }

    public Class<Bleed> inst() {
        return Bleed.class;
    }

    public class Bleed extends CustomEffect<Bleeding.Bleed>.Instance {

        private Bleed(final int ticks, final double power, final boolean visible) {
            super(ticks, power, visible);
        }

        @Override
        public Bleed merge(final Bleed in) {
            final int last = in.ticks();
            if (last == 0) return this;
            return new Bleed(ticks(), in.power() * ticks() / last + pow, vis);
        }

        @Override
        public CustomEffect<Bleeding.Bleed> effect() {
            return Effects.BLEED;
        }
    }
}
