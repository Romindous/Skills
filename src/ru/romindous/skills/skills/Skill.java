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
import ru.komiss77.Timer;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Main;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.guides.Entries;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.Chain;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.skills.trigs.Trigger;

public class Skill {//скилл

    public static final byte SIG_FIGS = 1;

    public final String name;
    public final Trigger trig;
    public final BossBar cdBar;
    public final Selector.SelState[] sels;
    public final Ability.AbilState[] abils;
    public final Modifier.ModState[] mods;
    private final double maxCD;

    @Nullable
    private Caster cst;

    private double coolDown;
    private double useStamp = 0;

    public Skill(final String name, final Trigger trig, final Selector.SelState[] sels,
        final Ability.AbilState[] abils, final Modifier.ModState[] mods) {
        this.name = name;
        this.trig = trig;
        this.sels = sels;
        this.abils = abils;
        this.mods = mods;
        this.cst = null;

        double cd = 0;
        for (int i = 0; i != abils.length; i++) {
            final Ability.AbilState as = abils[i]; final Selector.SelState ss = sels[i];
            cd += as.abil().CD.calc(as.lvl()) * ss.sel().cdMul.calc(ss.lvl());
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
        desc.add(TCUtil.N + "Расходует (в среднем): " + Main.manaClr
            + StringUtil.toSigFigs(avgMana, SIG_FIGS) + " душ");
        desc.add(TCUtil.N + "Перезаряжается: " + Main.cdClr
            + StringUtil.toSigFigs(getCDFor(cs), SIG_FIGS) + " сек");
        desc.add("<dark_gray>Реагирует на: " + TCUtil.sided(trig.disName(), "🟃"));
        desc.addAll(sets);
        return desc;
    }

    public boolean isReady() {
        return Timer.tickTime() * 0.05d - useStamp > coolDown;
    }

    public double getCDFor(final Caster cs) {
        return Stat.skillCD(modifyAll(Chastic.COOLDOWN, maxCD), cs.getStat(Stat.AGILITY));
    }

    public double currCD() {
        return useStamp + coolDown - Timer.tickTime() * 0.05d;
    }

    public void setCD(final double cd) {
        coolDown = cd;
        useStamp = Timer.tickTime() * 0.05d;
    }

    /*public Trigger posTrig(final int at) {
        if (at == 0) return trig;
        return abils[at > abils.length
            ? abils.length - 1 : at - 1].abil().finish();
    }*/

    public void updateKd(final Player p) {
        if (coolDown < 2) return;

        final double tm = currCD() - 0.1d;
        if (tm < 0d) {
            cdBar.progress(0f);
            p.hideBossBar(cdBar);
            return;
        }
        cdBar.progress((float) (tm / coolDown));
        p.showBossBar(cdBar);
    }

    public void attempt(final Trigger tr, final Event e, final LivingEntity caster, final Caster cs) {
        if (trig != tr || abils.length == 0) return;
        final double cd = currCD();
        if (cd > 0d) {
            if (!(caster instanceof final Player pl)) return;
            cs.inform(pl, TCUtil.P + name + TCUtil.N + " на перезарядке "
                + TCUtil.A + StringUtil.toSigFigs(cd, SIG_FIGS) + " сек" + TCUtil.N + "!");
            return;
        }
        this.cst = cs;
        if (!step(Chain.of(this, caster, e, 0))) return;
        setCD(getCDFor(cs));
        if (!(caster instanceof final Player pl)) return;
        updateKd(pl);
        if (!(cs instanceof final Survivor sv)) return;
        Entries.skill.complete(pl, sv, false);
    }

    public boolean step(final Chain link) {
        if (cst == null) return false;
        final int curr = link.curr();
        if (abils.length <= curr) return false;
        final Ability.AbilState abs = abils[curr];
        final Selector.SelState sls = sels[curr];
        final float useMana = (float) Stat.skillMana(modifyAll(Chastic.MANA, abs.abil().MANA.calc(abs.lvl())
            * sls.sel().manaMul.calc(sls.lvl())), cst.getStat(Stat.SPIRIT));
        final Ability ab = abs.abil();
        final EquipmentSlot swing = ab.equip().result(link.caster().getEquipment());
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
        boolean casted = false;
        for (final LivingEntity tgt : tgts) {
            if (useMana > cst.mana()) {
                if (ch.caster() instanceof final Player pl)
                    cst.inform(pl, "<red>Недостаточно " + TCUtil.A + "душ "
                        + TCUtil.N + "<red>для активации " + TCUtil.P + name);
                return false;
            }
            if (!ab.cast(ch.target(tgt), abs.lvl())) continue;
            if (ch.caster() instanceof final Player pl) cst.chgMana(pl, -useMana);
            casted = true;
        }
        if (!casted) return false;
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
