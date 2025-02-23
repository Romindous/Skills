package ru.romindous.skills.events;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;

public class EntityCastEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final LivingEntity tgt;
    private final Location loc;
    private final Ability ab;

    private boolean cancel;

    public EntityCastEvent(final Chain ch, final Ability abil) {
        super(ch.caster());
        tgt = ch.target();
        loc = ch.at();
        ab = abil;
    }

    public LivingEntity getEntity() {
        return (LivingEntity) this.entity;
    }

    public LivingEntity getTarget() {
        return this.tgt;
    }

    public Location getLocation() {
        return this.loc;
    }

    public Ability getAbility() {
        return ab;
    }

    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
