package ru.romindous.skills.effects;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import ru.komiss77.modules.effects.CustomEffect;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.utils.EntityUtil;
import ru.romindous.skills.objects.Effects;

public class Stagger extends CustomEffect<Stagger.Stag> {

    protected void onStart(final LivingEntity tgt, final Stag in) {
        final BlockData bd = tgt.getLocation().getBlock().getBlockData();
        EntityUtil.effect(tgt, bd.getSoundGroup().getStepSound(),
            0.8f, Particle.DUST_PILLAR, bd);
    }

    public void apply(final LivingEntity tgt, final Vector dir, final int ticks, final double power) {
        apply(tgt, new Stag(dir, ticks, power, true));
    }

    public void apply(final LivingEntity tgt, final Vector dir, final int ticks, final double power, final boolean visible) {
        apply(tgt, new Stag(dir, ticks, power, visible));
    }

    protected void affect(final LivingEntity tgt, final Stag in) {
        tgt.setVelocity(in.dir.clone().multiply(in.power()));
        final BlockData bd = tgt.getLocation().getBlock().getBlockData();
        EntityUtil.effect(tgt, bd.getSoundGroup().getStepSound(),
            0.8f, Particle.DUST_PILLAR, bd);
    }

    protected int period(final LivingEntity tgt, final Stag in) {
        return 2;
    }

    protected Color color() {
        return Color.GRAY;
    }

    public Class<Stag> inst() {
        return Stag.class;
    }

    public class Stag extends CustomEffect<Stag>.Instance {

        private final Vector dir;

        private Stag(final Vector dir, final int ticks, final double power, final boolean visible) {
            super(ticks, power, visible);
            this.dir = dir.normalize();
        }

        @Override
        public Stag merge(final Stag in) {
            final int last = in.ticks();
            if (last == 0) return this;
            return new Stag(dir, ticks(), in.power() * ticks() / last + pow, vis);
        }

        @Override
        public CustomEffect<Stag> effect() {
            return Effects.STAG;
        }
    }
}
