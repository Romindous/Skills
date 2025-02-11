package ru.romindous.skills.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.skills.trigs.Trigger;

public interface Caster {

    int getStat(final Stat st);

    void inform(final LivingEntity le, final String msg);

    void chgMana(final LivingEntity le, final float amt);

    void trigger(final Trigger tr, final Event e, final LivingEntity caster);

    float mana();
}
