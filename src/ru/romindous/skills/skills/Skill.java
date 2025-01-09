package ru.romindous.skills.skills;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Main;
import ru.romindous.skills.enums.Chastic;
import ru.romindous.skills.enums.Stat;
import ru.romindous.skills.enums.Trigger;
import ru.romindous.skills.objects.Caster;
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
    private final int maxCD;

    @Nullable
    private Caster cst;

    private int coolDown;
    private int useStamp = 0;

    public Skill(final String name, final Trigger trig, final Selector.SelState[] sels,
        final Ability.AbilState[] abils, final Modifier.ModState[] mods) {
        this.name = name;
        this.trig = trig;
        this.sels = sels;
        this.abils = abils;
        this.mods = mods;
        this.cst = null;

        int cd = 0;
        for (int i = 0; i != abils.length; i++) {
            final Ability.AbilState as = abils[i]; final Selector.SelState ss = sels[i];
            cd += (int) (as.abil().CD.calc(as.lvl()) * ss.sel().cdMul.calc(ss.lvl()));
        }
        coolDown = maxCD = cd;
        cdBar = BossBar.bossBar(TCUtil.form(TCUtil.N + "Перезарядка " + TCUtil.P + name),
            0f, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_6, Collections.emptySet());
    }

    public List<String> describe(final Caster cs) {
        final List<String> desc = new ArrayList<>(8);
        double avgMana = 0, uses = 1;
        final List<String> sets = new ArrayList<>(abils.length << 2);
        for (int i = 0; i != abils.length; i++) {
            final Ability.AbilState as = abils[i];
            final Selector.SelState ss = sels[i];
            final int nus = ss.sel().avgAmount(ss.lvl());
            avgMana += uses * nus * Stat.skillMana(modifyAll(Chastic.MANA, as.abil().MANA.calc(as.lvl())
                * ss.sel().manaMul.calc(ss.lvl())), cs.getStat(Stat.SPIRIT));
            uses = nus;
            sets.add("<dark_gray>" + (i + 1) + ")=-=-=-=-=-=-=-=-=-=-");
            sets.add(TCUtil.sided("<u>" + ss.sel().name(ss.lvl()) + "</u>", ss.sel().side()) + " <dark_gray>выбирает:");
            sets.addAll(ss.sel().context(this, ss.lvl()));
            sets.add(TCUtil.sided("<u>" + as.abil().name(as.lvl()) + "</u>", as.abil().side()) + " <dark_gray>на деле:");
            sets.addAll(as.abil().context(this, as.lvl()));
        }
        desc.add(TCUtil.N + "Расходует (в среднем): " + Main.manaClr + Math.round(avgMana) + " душ");
        desc.add(TCUtil.N + "Перезаряжается: " + Main.cdClr + getCDFor(cs) + " сек");
        desc.add("<dark_gray>Реагирует на: " + TCUtil.sided(trig.disName(), "🟃"));
        desc.addAll(sets);
        return desc;
    }

    public boolean isReady() {
        return ApiOstrov.currentTimeSec() - useStamp > coolDown;
    }

    public int getCDFor(final Caster cs) {
        return (int) Stat.skillCD(modifyAll(Chastic.COOLDOWN, maxCD), cs.getStat(Stat.AGILITY));
    }

    public int currCD() {
        return useStamp + coolDown - ApiOstrov.currentTimeSec();
    }

    public void setCD(final int cd) {
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
        final int tm = currCD() - 1;
        if (tm < 0) {
            cdBar.progress(0f);
            p.hideBossBar(cdBar);
            return;
        }
        cdBar.progress((float) (tm + 1) / coolDown);
        p.showBossBar(cdBar);
    }

    public void attempt(final Trigger tr, final Event e, final LivingEntity caster, final Caster cs) {
        if (trig == tr && abils.length != 0) {
            final int cd = currCD();
            if (cd > 0) {
                if (!(caster instanceof final Player pl)) return;
                cs.inform(pl, TCUtil.N + "Навык " + TCUtil.P + name
                    + TCUtil.N + " на перезарядке " + TCUtil.A + cd + " сек" + TCUtil.N + "!");
                return;
            }
            this.cst = cs;
            if (step(Chain.of(this, caster, e, 0))) {
                setCD(getCDFor(cs));
                if (caster instanceof final Player pl) updateKd(pl);
            }
        }
    }

    public boolean step(final Chain link) {
        if (cst == null) return false;
        final int curr = link.curr();
        if (abils.length <= curr) return false;
        final Ability.AbilState abs = abils[curr];
        final Selector.SelState sls = sels[curr];
        final float useMana = (float) Stat.skillMana(modifyAll(Chastic.MANA,
            abs.abil().MANA.calc(abs.lvl()) * sls.sel().manaMul.calc(sls.lvl())), cst.getStat(Stat.SPIRIT));
        final Ability ab = abs.abil();
        final EquipmentSlot swing = ab.equip().test(link.caster().getEquipment());
        if (swing == null) return false;
        final Selector.SelState ss = sels[curr];
        final Chain ch = link.curr(curr + 1);
        if (ab.selfCast()) {
            if (useMana > cst.mana()) {
                if (ch.caster() instanceof final Player pl)
                    cst.inform(pl, "<red>Недостаточно " + TCUtil.A + "душ "
                        + TCUtil.N + "<red>для активации " + TCUtil.P + name);
                return false;
            }
            if (!ab.cast(ch.target(ch.caster()), abs.lvl())) return false;
            if (ch.caster() instanceof final Player pl) cst.chgMana(pl, -useMana);
            if (swing.isHand()) Nms.swing(ch.caster(), swing);
            return true;
        }
        final Collection<LivingEntity> tgts = ss.sel().select(ch, ss.lvl());
        if (tgts.isEmpty()) return false;
        for (final LivingEntity tgt : tgts) {
            if (useMana > cst.mana()) {
                if (ch.caster() instanceof final Player pl)
                    cst.inform(pl, "<red>Недостаточно " + TCUtil.A + "душ "
                        + TCUtil.N + "<red>для активации " + TCUtil.P + name);
                return false;
            }
            if (!ab.cast(ch.target(tgt), abs.lvl())) continue;
            if (ch.caster() instanceof final Player pl) cst.chgMana(pl, -useMana);
        }
        if (swing.isHand()) Nms.swing(ch.caster(), swing);
        return true;
    }

    public double modifyAll(final Chastic ch, double def) {
        for (final Modifier.ModState md : mods)
            def = md.mod().modify(ch, def, md.lvl(), null);
        return def;
    }

    public double modifyAll(final Chastic ch, double def, final Chain info) {
        for (final Modifier.ModState md : mods)
            def = md.mod().modify(ch, def, md.lvl(), info);
        return def;
    }
}
