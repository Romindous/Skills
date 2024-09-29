package ru.romindous.skills.skills;

import java.util.Collection;
import java.util.Collections;
import net.kyori.adventure.bossbar.BossBar;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Stat;
import ru.romindous.skills.enums.Trigger;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;

public class Skill {//скилл

    public final String name;
    public final Trigger trig;
    public final BossBar cdBar;
    public final Selector.SelState[] sels;
    public final Ability.AbilState[] abils;
    public final Modifier.ModState[] mods;
    public final MutableFloat mana;
    private final int maxCD;

    private int coolDown;
    private int manaMod = 0;
    private int useStamp = 0;

    public Skill(final String name, final Trigger trig, final Selector.SelState[] sels,
        final Ability.AbilState[] abils, final Modifier.ModState[] mods, final MutableFloat mana) {
        this.name = name;
        this.trig = trig;
        this.sels = sels;
        this.abils = abils;
        this.mods = mods;
        this.mana = mana;

        /*orgManas = new int[abils.length];
        for (int i = 0; i != abils.length; i++) {
            final Ability.AbilState as = abils[i];
            orgManas[i] = (int) as.abil().MANA.modify(this, as.lvl(), );
        }*/

        int cd = 0;
        for (final Ability.AbilState as : abils)
            cd += (int) as.abil().CD.calc(as.lvl());
        maxCD = cd;
        coolDown = maxCD;
        cdBar = BossBar.bossBar(TCUtil.form(TCUtil.N + "Перезарядка " + TCUtil.P + name),
            0f, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_6, Collections.emptySet());
    }

    public boolean isReady() {
        return ApiOstrov.currentTimeSec() - useStamp > coolDown;
    }

    public int getCoolDown() {
        return useStamp + coolDown - ApiOstrov.currentTimeSec();
    }

    public void setCoolDown(final int cd) {
        coolDown = cd;
        useStamp = ApiOstrov.currentTimeSec();
    }

    /*public Trigger posTrig(final int at) {
        if (at == 0) return trig;
        return abils[at > abils.length
            ? abils.length - 1 : at - 1].abil().finish();
    }*/

    public void updateKd(final Player p) {
        if (coolDown < 2) return;
        final int tm = getCoolDown();
        if (tm < 1) {
            cdBar.progress(0f);
            p.hideBossBar(cdBar);
        } else {
            cdBar.progress((float) tm / coolDown);
            p.showBossBar(cdBar);
        }
    }

    public void attempt(final Trigger tr, final Event e, final LivingEntity caster, final int agility, final int spirit) {
        if (trig == tr && abils.length != 0) {
            final int cd = getCoolDown();
            if (cd > 0) {
                if (!(caster instanceof final Player pl)) return;
                ScreenUtil.sendActionBar(pl, TCUtil.N + "Способность " + TCUtil.P + name
                    + TCUtil.N + " на перезарядке еще " + TCUtil.A + cd + TCUtil.N + "сек!");
                return;
            }
            if (manaMod != spirit) manaMod = spirit;
            final Ability.AbilState fas = abils[0];
            if (sels[0].sel().equals(Selector.SAME)) {
                final double useMana = Stat.skillMana(modifyAll(Chastic.MANA,
                    fas.abil().MANA.calc(fas.lvl()), e), manaMod);
                if (mana.intValue() < useMana) {
                    if (!(caster instanceof final Player pl)) return;
                    ScreenUtil.sendActionBar(pl, TCUtil.N + "Не хватает " + TCUtil.A + (useMana - mana.intValue())
                        + " душ" + TCUtil.N + " чтобы использовать " + TCUtil.P + name);
                    return;
                }
            }

            if (step(Chain.of(this, caster, e, 0))) {
                setCoolDown((int) Stat.skillCD(modifyAll(Chastic.COOLDOWN, maxCD, e), agility));
            }
        }
    }

    public boolean step(final Chain link) {
        final int curr = link.curr();
        if (abils.length <= curr) return false;
        final Ability.AbilState abs = abils[curr];
        final float useMana = (float) Stat.skillMana(modifyAll(Chastic.MANA,
            abs.abil().MANA.calc(abs.lvl()), link.event()), manaMod);
        final Ability ab = abs.abil();
        final EquipmentSlot swing = ab.equip().test(link.caster().getEquipment());
        if (swing == null) return false;
        final Chain ch = link.curr(curr + 1);
        final Selector.SelState ss = sels[curr];
        if (ab.selfCast()) {
            if (useMana > mana.intValue()) return false;
            if (!ab.cast(ch.target(ch.caster()), abs.lvl())) return false;
            mana.subtract(useMana);
            if (swing.isHand()) Nms.swing(ch.caster(), swing);
            return true;
        }
        final Collection<LivingEntity> tgts = ss.sel().select(ch, ss.lvl());
        if (tgts.isEmpty()) return false;
        for (final LivingEntity tgt : tgts) {
            if (useMana > mana.intValue()) return false;
            if (!ab.cast(ch.target(tgt), abs.lvl())) continue;
            mana.subtract(useMana);
        }
        if (swing.isHand()) Nms.swing(ch.caster(), swing);
        return true;
    }

    public double modifyAll(final Chastic ch, double def, final Event e) {
        for (final Modifier.ModState md : mods)
            def = md.mod().modify(ch, def, md.lvl(), e);
        return def;
    }
}
