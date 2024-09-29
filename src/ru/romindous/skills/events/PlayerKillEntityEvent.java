package ru.romindous.skills.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerKillEntityEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity entity;
    private float mana;
    private float drops;
    private int exp;

    public PlayerKillEntityEvent(final Player killer, final LivingEntity entity, final float mana, final float drops, final int exp) {
        super(killer);
        this.entity = entity;
        this.mana = mana;
        this.drops = drops;
        this.exp = exp;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public float getMana() {
        return mana;
    }

    public void setMana(float mana) {
        this.mana = mana;
    }

    public float getDrops() {
        return drops;
    }

    public void setDrops(final float drops) {
        this.drops = drops;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(final int exp) {
        this.exp = exp;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
