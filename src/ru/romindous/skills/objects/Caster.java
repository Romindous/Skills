package ru.romindous.skills.objects;

import org.bukkit.entity.LivingEntity;
import ru.romindous.skills.enums.Stat;

public interface Caster {

    int getStat(final Stat st);

    void inform(final LivingEntity le, final String msg);

    void chgMana(final LivingEntity le, final float amt);

    float mana();
}
