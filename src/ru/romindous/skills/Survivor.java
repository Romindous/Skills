package ru.romindous.skills;

import javax.annotation.Nullable;
import java.util.*;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.notes.OverrideMe;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.enums.Stat;
import ru.romindous.skills.enums.SubServer;
import ru.romindous.skills.enums.Trigger;
import ru.romindous.skills.menus.SkillMenu;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.objects.Scroll;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.tasks.Task;

import static ru.romindous.skills.listeners.MySqlLst.*;

public class Survivor extends Oplayer /*implements Transfer*/ {

    //сохраняемое-загружаемое
    public Role role;
//    public int level = 1; //начинать с 1, или будут неизбежные деления на 0!
    public int exp, mobKills, deaths, roleStamp, statsPoints, worldOpen;
    public boolean showScoreBoard, showActionBar; //показывать - не показывать
//    public int totalPlyTime; //глобальное игровое время в секундах
//    private int flags; //флаги
//    private int openedArea; //открытые локации
    public final int transId; //transfer id
    public final MutableFloat mana = new MutableFloat(0f);

    //динамические
    public int tickAsync; //тики с момента входа. p.getTicksLived() не подходит, выхватывает числа не по порядку
    public int currentPlyTime; //игровое время в секундах с момента входа
    public int currentLiveSec; //секунды текущей жизни
    public int maxMana = 10,
            maxHP = 20;
//    public int abilityPage; //открываемая страничка в меню абилити
//    public int abShowTime = 10;//когда никакие статы не обновляются, через 10 сек. АБ пропадает boolean lockAB; //пропуск секунды показа инфо

    //private final EnumMap<Stats, Double> statEvoMultipler; //производная от статы и уровня, где
    public final StringBuffer abBuffer = new StringBuffer(); //для построения строки актионбар
    private final BossBar bossbar = BossBar.bossBar(Component.empty(), 0,
        BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_12, Collections.emptySet());
    private int bbTime;
    private boolean bbHidden = true;//переменные для боссбар
    public int lastCuboidId; //для playerMoveTask
    public int cuboidEntryTime = ApiOstrov.currentTimeSec(); //при входе равно текущему времени - может сразу появиться в кубоиде
//    public CuboidInfo compasstarget = CuboidInfo.DEFAULT; //ИД кубоида цели для компаса
    private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
    public final List<Skill> skills = new ArrayList<>();
    public final HashMap<Selector.SelState, Integer> sels = new HashMap<>();
    public final HashMap<Ability.AbilState, Integer> abils = new HashMap<>();
    public final HashMap<Modifier.ModState, Integer> mods = new HashMap<>();
    public final SmartInventory skillInv;
    public final SkillMenu abMenu;
    //private WeakReference<Transfer>[] to;
    public Task miniQuestTask;

    public Survivor(final HumanEntity pl) {
        super(pl);
        for (final Stat st : Stat.values()) {
            stats.put(st, 1);
        }
        transId = SM.tId++;
        resetBoard(pl);
        abMenu = new SkillMenu(this);
        skillInv = SmartInventory.builder()
            .id("Skill "+pl.getName())
            .provider(abMenu)
            .size(6, 9)
            .title("         §5§lНавыки Класса")
            .build();
    }

    //===================== ТАБЛОИДЫ =====================
    public void showBossBar(final Player p, final int second) {
        if (bbHidden) {
            p.showBossBar(bossbar);
            bbHidden = false;
        }
        bbTime = second;
    }

    public void hideBossBar(final Player p) {
        if (!bbHidden) {
            p.hideBossBar(bossbar);
            bbHidden = true;
        }
        bbTime = 0;
    }

    public void setBarName(final String text) {
        bossbar.name(Component.text(text));
    }

    public void setBarProgress(final float value) {
        bossbar.progress(value);
    }

    public void setBarColor(final BossBar.Color barColor) {
        bossbar.color(barColor);
    }

    public void updateBar(final Player p) {
        /*if (!bbHidden) {
            if (bbTime > 0) {
                bbTime--;
            } else {
                bbHidden = true;
                p.hideBossBar(bossbar);
            }
        }*/
//        if (!showActionBar || abShowTime <= 0) return;
        if (!showActionBar) return;

        abBuffer.setLength(0);
        //❤ : 50(100)   ✞ : 30   🔥 : 10(100)
        abBuffer
        .append(getHeartIcon((int) p.getHealth())).append("/").append(maxHP)
        .append(Stat.MAGIC.color()).append("   🔥 ").append(TCUtil.N)
        .append(mana.intValue()).append("/").append(maxMana);

        if (role == null) {
            abBuffer.append(TCUtil.N).append("   Роль не выбрана!");
        }
        p.sendActionBar(TCUtil.form(abBuffer.toString()));
//        abShowTime--;
    }

    private String getHeartIcon(final int hp) {
        return switch (hp / maxHP * 5) {
            case 0 -> SM.HEART_LOW + hp;
            case 1 -> SM.HEART_LESS + hp;
            case 2 -> SM.HEART_HALF + hp;
            case 3 -> SM.HEART_FULL + hp;
            default -> SM.HEART_MAX + hp;
        };
    }

    private static final String BOARD_LVL = "lvl";
    private static final String BOARD_HP = "hp";
    private static final String BOARD_MANA = "mana";

    private void resetBoard(final HumanEntity pl) {
        score.getSideBar().reset().title(role.getName())
            .add(" ")
            .add(BOARD_LVL, TCUtil.N + "Уровень: " + TCUtil.A + getLevel())
            .add(BOARD_HP, TCUtil.N + "ХП: " + getHeartIcon((int) pl.getHealth()) + "/" + maxHP)
            .add(BOARD_MANA, TCUtil.N + "Душ: " + Stat.MAGIC.color() + "🔥 " + mana.intValue() + "/" + maxMana)
            .add(" ")
            .add("§e    ostrov77.ru").build();
    }

    public void updateBoard(final Player p, final SM.infoType type) {
//        abShowTime = 10;
        if (!showScoreBoard) return;
        switch (type) {
            case ALL:
                resetBoard(p);
                break;
            case LEVEL:
                score.getSideBar().update(BOARD_LVL, TCUtil.N + "Уровень: " + TCUtil.A + getLevel());
                break;
            case HEALTH:
                score.getSideBar().update(BOARD_HP, TCUtil.N + "ХП: " + getHeartIcon((int) p.getHealth()) + "/" + maxHP);
                break;
            case MANA:
                score.getSideBar().update(BOARD_MANA, TCUtil.N + "Душ: " + Stat.MAGIC.color() + "🔥 " + mana.intValue() + "/" + maxMana);
                break;
        }
    }
    //======================================================

    public int change(final Selector.SelState as, final int inc) {
        if (Selector.DEFAULT.contains(as.sel())) return 1;
        final Integer amt = sels.remove(as);
        if (amt == null) {
            if (inc <= 0) return 0;
            sels.put(as, inc);
            return inc;
        } else {
            final int fam = amt + inc;
            if (fam <= 0) return 0;
            sels.put(as, fam);
            return fam;
        }
    }

    public int change(final Ability.AbilState as, final int inc) {
        final Integer amt = abils.remove(as);
        if (amt == null) {
            if (inc <= 0) return 0;
            abils.put(as, inc);
            return inc;
        } else {
            final int fam = amt + inc;
            if (fam <= 0) return 0;
            abils.put(as, fam);
            return fam;
        }
    }

    public int change(final Modifier.ModState as, final int inc) {
        final Integer amt = mods.remove(as);
        if (amt == null) {
            if (inc <= 0) return 0;
            mods.put(as, inc);
            return inc;
        } else {
            final int fam = amt + inc;
            if (fam <= 0) return 0;
            mods.put(as, fam);
            return fam;
        }
    }

    private static final Selector.SelState SAME_ST = new Selector.SelState(Selector.SAME, 1);
    private static final Selector.SelState CASTER_ST = new Selector.SelState(Selector.CASTER, 1);

    public void setSkillAbil(final Player p, final @Nullable Ability.AbilState nas, final int abPos, final int skPos) {
        final Skill skill = skills.get(skPos);
        final List<Ability.AbilState> nasl = Arrays.asList(skill.abils);
        final List<Selector.SelState> nssl = Arrays.asList(skill.sels);

        if (abPos < skill.abils.length) {
            final Ability.AbilState curr = skill.abils[abPos];
            if (curr.equals(nas)) {
                p.sendMessage("<red>Такая способность уже выбрана!");
                return;
            }

            if (nas == null) {
                nssl.remove(abPos);
                change(skill.sels[abPos], 1);
                nasl.remove(abPos);
            } else {
                if (change(nas, -1) < 0) {
                    p.sendMessage("<red>У тебя нет такой способности!");
                    return;
                }
                nasl.set(abPos, nas);
                if (nas.abil().selfCast()) nssl.set(abPos, CASTER_ST);
            }
            change(curr, 1);
        } else {
            //new abil
            if (nas == null) {
                Ostrov.log_warn("Tried setting null ability at pos " + abPos
                    + ", skill only has " + (skill.abils.length - 1));
                return;
            }

            if (change(nas, -1) < 0) {
                p.sendMessage("<red>У тебя нет такой способности!");
                return;
            }
            nasl.add(nas);
            nssl.add(nas.abil().selfCast() ? CASTER_ST : SAME_ST);
        }

        setSkill(skPos, new Skill(skill.name, skill.trig, nssl.toArray(new Selector.SelState[0]),
            nasl.toArray(new Ability.AbilState[0]), skill.mods, skill.mana));
    }

    public void setSkillSel(final Player p, final @Nullable Selector.SelState nss, final int abPos, final int skPos) {
        final Skill skill = skills.get(skPos);
        final List<Selector.SelState> nmsl = Arrays.asList(skill.sels);
        if (abPos >= skill.abils.length) {
            Ostrov.log_warn("Tried setting selector at pos " + abPos
                + ", skill only has " + (skill.abils.length - 1));
            return;
        }

        final Selector.SelState curr = skill.sels[abPos];
        if (nss == null) {
            nmsl.set(abPos, SAME_ST);
        } else {
            if (!nss.sel().equals(Selector.SAME) && change(nss, -1) < 0) {
                p.sendMessage("<red>У тебя нет такого подборника!");
                return;
            }
            nmsl.set(abPos, nss);
        }
        change(curr, 1);

        setSkill(skPos, new Skill(skill.name, skill.trig, nmsl.toArray(new Selector.SelState[0]),
            skill.abils, skill.mods, skill.mana));
    }

    public void addSkillMod(final Player p, final Modifier.ModState nms, final int skPos) {
        final Skill skill = skills.get(skPos);
        final List<Modifier.ModState> nmsl = Arrays.asList(skill.mods);
        if (change(nms, -1) < 0) {
            p.sendMessage("<red>У тебя нет такого модификатора!");
            return;
        }
        nmsl.add(nms);
        setSkill(skPos, new Skill(skill.name, skill.trig, skill.sels,
            skill.abils, nmsl.toArray(new Modifier.ModState[0]), skill.mana));
    }

    public void remSkillMod(final Player p, final int mdPos, final int skPos) {
        final Skill skill = skills.get(skPos);
        if (mdPos >= skill.mods.length) {
            Ostrov.log_warn("Tried setting null sel at pos " + mdPos
                + ", skill only has " + skill.abils.length);
            return;
        }
        final List<Modifier.ModState> nmsl = Arrays.asList(skill.mods);
        nmsl.remove(mdPos);
        setSkill(skPos, new Skill(skill.name, skill.trig, skill.sels,
            skill.abils, nmsl.toArray(new Modifier.ModState[0]), skill.mana));
    }

    public void setSkill(final int skPos, final @Nullable Skill nsk) {
        if (skPos < skills.size()) {
            if (nsk == null) {
                skills.remove(skPos);
                return;
            }
            final Skill skill = skills.get(skPos);
            nsk.setCoolDown(skill.getCoolDown());
            skills.set(skPos, nsk);
        } else {
            if (nsk == null) return;
            skills.add(nsk);
        }
    }


    public boolean canUse(final Scroll sc) {
        return sc.role() == null || sc.role() == role;
    }

    //===================== СТАТА =====================

    public void setStat(final Stat st, final int num) {
        stats.put(st, num);
    }

    public int getStat(final Stat st) {
        return stats.get(st);
    }

    //- после загрузки
    //- при изменении уровня
    //- при выборе/смене скила
    //- из меню StatsMenu при изменении стат
    private static final int START_MANA = 40, START_HP = 20;
    private static final double START_SPEED = 0.2d;
    public float getWalkSpeed(final HumanEntity p) { //возвращать от 0 до 1
        final float walkSpeed = (float) Stat.speed(START_SPEED, getStat(Stat.AGILITY));
        return walkSpeed < 0f ? 0f : walkSpeed > 1f ? 1f : walkSpeed;
    }

    public void recalcStats(final Player p) {
        maxMana = (int) Stat.mana(START_MANA, getStat(Stat.MAGIC));
        maxHP = (int) Stat.health(START_HP, getStat(Stat.STRENGTH));
        applySkill(p);
    }

    //из recalcStats и PlayerRespawnEvent
    public void applySkill(final Player p) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHP);
        p.setHealthScaled(true);
        p.setHealthScale((maxHP << 1) / SM.HP_PER_HEART);

        float walkSpeed = getWalkSpeed(p);
        if (p.getWalkSpeed() != walkSpeed) {
            p.setWalkSpeed(walkSpeed);
        }
        updateBoard(p, SM.infoType.ALL);
    }

    public static final float EXP_DEL = (float) ConfigVars.get("exp.delimit", 6d);
    public static final double LVL_DEL = 1d / EXP_DEL;

    public void setXp(final Player p, final int ammount) {
        exp = ammount;
        if (exp == 0) {
            p.setLevel(0);
            p.setExp(0f);
            return;
        }

        final int currLvl = (int) Math.sqrt(exp * LVL_DEL);
        final int lvlSq = currLvl * currLvl;
        p.setLevel(currLvl);
        p.setExp((exp - lvlSq * EXP_DEL) /
            ((FastMath.square(currLvl + 1) - lvlSq) * EXP_DEL));
    }

    public void addXp(final Player p, final int ammount) {
        if (role == null || ammount == 0) {
            return;
        }
        final int currExp = exp + p.applyMending(ammount);
        final int oldLvl = (int) Math.sqrt(exp * LVL_DEL);
        final int currLvl = (int) Math.sqrt(currExp * LVL_DEL);
        final int lvlSq = currLvl * currLvl;
        exp += ammount;
        p.setLevel(currLvl);
        p.setExp((currExp - lvlSq * EXP_DEL) /
            ((FastMath.square(currLvl + 1) - lvlSq) * EXP_DEL));

        final int lvlAdd = currLvl - oldLvl;
        if (lvlAdd == 0) return;
        if (lvlAdd > 0) {//уровень добавляется
            statsPoints += lvlAdd;
            p.sendMessage(Main.prefix + "Достигнут уровень " + role.color() + currLvl);
            ScreenUtil.sendTitle(p, TCUtil.N + "Новый Уровень " + role.color() + currLvl,
                TCUtil.N + ClassUtil.rndElmt(SM.congratulations));
            p.playSound(p.getEyeLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 2f, 0.6f);
//            recalcStats(p);
//            return;
        }
    }

    public int getLevel() {
        return (int) Math.sqrt(exp * LVL_DEL);
    }

    public int nextLevelXp() {
        final int lvl = getLevel();
        return (int) ((FastMath.square(lvl + 1) - lvl * lvl) * EXP_DEL);
    }

    public float nextLevelScale() { //вернуть от 0 до 1 для боссбара
        float result = (float) exp / nextLevelXp();
        return result > 1 ? 1 : result < 0 ? 0 : result;
    }

    public boolean addHp(final Player p, final double hlth) {
        if (maxHP < p.getHealth() + hlth) {
            p.setHealth(maxHP);
            return false;
        }
        p.setHealth(p.getHealth() + hlth);
        return true;
        //Bukkit.broadcast(Component.text("gained " + hlth + ", now " + le.getHealth() + hlth));
    }

    public void addFood(final Player p, final int food, final boolean sat) {
        p.setFoodLevel(Math.min(20, food + p.getFoodLevel()));
        if (sat) {
            p.setSaturation(Math.min(20, food + p.getSaturation()));
        }
    }

    public void tryRegen(final Player pl, final double amt) {
        if (pl != null && pl.isValid() && amt > 0d && pl.getHealth() > 0d) { //реген только если не сдох
            final EntityRegainHealthEvent regenEvent = new EntityRegainHealthEvent(pl, amt, EntityRegainHealthEvent.RegainReason.CUSTOM);
            regenEvent.callEvent();
            addHp(pl, regenEvent.getAmount());
        }
    }

    public void setMana(final Player p, final float amt) {
        if (amt < 0 || role == null) return;
        mana.setValue(mana.floatValue());
        updateBoard(p, SM.infoType.MANA);
    }

    public void subMana(final Player p, final float amt) {
        if (amt <= 0 || role == null) return;
        mana.setValue(Math.max(mana.floatValue() - amt, 0f));
        updateBoard(p, SM.infoType.MANA);
    }

    public void addMana(final Player p, final float amt) {
        if (amt <= 0 || role == null) return;
        mana.setValue(Math.min(mana.floatValue() + amt, maxMana));
        updateBoard(p, SM.infoType.MANA);
    }
    //======================================================    

    //===================== АБИЛИТИ =====================

    //======================================================    

    //===================== ЗЕМЛИ =====================

    public boolean isWorldOpen(final SubServer ss) {
        return (worldOpen & (1 << (ss.ordinal() + 1))) == (1 << (ss.ordinal() + 1));
    }

    public void unlockWorld(final SubServer ss) {
        worldOpen = worldOpen | (1 << (ss.ordinal() + 1));
    }

    /*public boolean isAreaDiscovered(final int areaId) {
        return (openedArea & (1 << areaId)) == (1 << areaId);
    }

    public void setAreaDiscovered(final int areaId) {
        openedArea = (openedArea | (1 << areaId));
        //LocalDB.executePstAsync(Bukkit.getConsoleSender(), "UPDATE `lobbyData` SET `openedArea` = '"+openedArea+"' WHERE `name` = '"+name+"';");
    }

    public int getOpenAreaCount() {
        int x = openedArea;
        // Collapsing partial parallel sums method
        // Collapse 32x1 bit counts to 16x2 bit counts, mask 01010101
        x = (x >>> 1 & 0x55555555) + (x & 0x55555555);
        // Collapse 16x2 bit counts to 8x4 bit counts, mask 00110011
        x = (x >>> 2 & 0x33333333) + (x & 0x33333333);
        // Collapse 8x4 bit counts to 4x8 bit counts, mask 00001111
        x = (x >>> 4 & 0x0F0F0F0F) + (x & 0x0F0F0F0F);
        // Collapse 4x8 bit counts to 2x16 bit counts
        x = (x >>> 8 & 0x00FF00FF) + (x & 0x00FF00FF);
        // Collapse 2x16 bit counts to 1x32 bit count
        return (x >>> 16) + (x & 0x0000FFFF);
    }

    public void setOpenedArea(int openedArea) {
        this.openedArea = openedArea;
    }

    public int getOpenedArea() {
        return openedArea;
    }

    public SCuboid getCuboid() {
        return Land.getCuboid(lastCuboidId);
    }*/
    //======================================================    

    //===================== ФЛАГИ =====================
    /*public boolean hasFlag(final GameFlag flag) {
        return (flags & (1 << flag.tag)) == (1 << flag.tag);//return LobbyFlag.hasFlag(flags, flag);
    }

    public void setFlag(final GameFlag flag, final boolean state) {
        flags = state ? (flags | (1 << flag.tag)) : flags & ~(1 << flag.tag);
        //LocalDB.executePstAsync(Bukkit.getConsoleSender(), "UPDATE `lobbyData` SET `flags` = '"+flags+"' WHERE `name` = '"+name+"';");
    }

    public void setFlags(final int flags) {
        this.flags = flags;
    }

    public int getFlags() {
        return flags;
    }*/
    //======================================================

    public float getMobCoof() {
        return (getLevel() * 0.04f) * Main.subServer.bfr;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj instanceof final Survivor srv) {
            return nik.equals(srv.nik);
        }
        return false;
    }

    public void trigger(final Trigger tr, final Event e, final Player caster) {
        final int agil = getStat(Stat.AGILITY);
        final int spirit = getStat(Stat.SPIRIT);
        for (final Skill sk : skills) {
            sk.attempt(tr, e, caster, agil, spirit);
        }
    }

    @Override
    public int hashCode() {
        return nik.hashCode();
    }

    @Override
    public String toString() {
        return nik + ", skill=" + role.getName() + ", lvl=" + getLevel() + ", souls=" + mana;
    }

    private static final double mobSpeed = ConfigVars.get("surv.mobSpeed", 0.2d);
    private static final double mobKbRes = ConfigVars.get("surv.mobKbRes", 0.5d);
    private static final double mobFollow = ConfigVars.get("surv.mobFollow", 1d);
    private static final double mobDamage = ConfigVars.get("surv.mobDamage", 0.4d);
    private static final double mobHealth = ConfigVars.get("surv.mobHealth", 0.6d);

    public void setMobChars(final Mob mob) {
        //прокачка характеристик в зависимости от мира
        final float cf = getMobCoof();
        scaleAtr(mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), cf * mobSpeed);
        scaleAtr(mob.getAttribute(Attribute.GENERIC_FLYING_SPEED), cf * mobSpeed);
        scaleAtr(mob.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE), cf * mobKbRes);
        scaleAtr(mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE), cf * mobFollow);
        scaleAtr(mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE), cf * mobDamage);
        scaleAtr(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH), cf * mobHealth);
        mob.setHealth(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());

        Stat.modMob(mob, getStat(Stat.CONTROL));
    }

    public static boolean scaleAtr(final AttributeInstance ais, final double vl) {
        if (ais != null) {
            ais.setBaseValue(Math.max(ais.getBaseValue() * (1d + vl), 0d));
            return true;
        }
        return false;
    }

    @OverrideMe
    public void preDataSave(final Player p, final boolean async) {
        if (mysqlError) { //при загрузке были ошибки - не срохран., чтобы не запороть данные
            Ostrov.log_err(p.getName()+":PlayerQuitEvent-Survivor loadError - не сохраняем");
            return;
        }

        mysqlData.put("role", role == null ? "" : role.name().toLowerCase(Locale.ROOT));

        final StringBuilder sb = new StringBuilder();
        for (final Stat st : Stat.values()) {
            sb.append(split).append(st.name().toLowerCase(Locale.ROOT))
                    .append(eq).append(getStat(st));
        }
        mysqlData.put("stats", sb.substring(split.length()));

        sb.setLength(0);
        final StringBuilder sb2 = new StringBuilder();
        for (final Skill sk : skills) {
            sb.append(split).append(sk.name).append(eq)
                .append(sk.trig.name().toLowerCase(Locale.ROOT)).append(eq);

            sb2.setLength(0);
            for (final Modifier.ModState md : sk.mods) {
                sb2.append(and).append(md.mod().id())
                    .append(lvl).append(md.lvl());
            }
            sb.append(sb2.length() == 0 ? "" : sb2.substring(and.length())).append(eq);

            sb2.setLength(0);
            for (final Ability.AbilState ab : sk.abils) {
                sb2.append(and).append(ab.abil().id())
                    .append(lvl).append(ab.lvl());
            }
            sb.append(sb2.length() == 0 ? "" : sb2.substring(and.length())).append(eq);

            sb2.setLength(0);
            for (final Selector.SelState sl : sk.sels) {
                sb2.append(and).append(sl.sel().id())
                    .append(lvl).append(sl.lvl());
            }
            sb.append(sb2.length() == 0 ? "" : sb2.substring(and.length())).append(eq)
                .append(Math.max(sk.getCoolDown(), 0));
        }
        mysqlData.put("skills", sb.length() == 0 ? "" : sb.substring(split.length()));

        sb.setLength(0);
        for (final Map.Entry<Selector.SelState, Integer> en : sels.entrySet()) {
            final Selector.SelState ss = en.getKey();
            sb.append(split).append((int) en.getValue()).append(eq)
                .append(ss.sel().id()).append(eq).append(ss.lvl());
        }
        mysqlData.put("sels", sb.length() == 0 ? "" : sb.substring(split.length()));

        sb.setLength(0);
        for (final Map.Entry<Ability.AbilState, Integer> en : abils.entrySet()) {
            final Ability.AbilState as = en.getKey();
            sb.append(split).append((int) en.getValue()).append(eq)
                .append(as.abil().id()).append(eq).append(as.lvl());
        }
        mysqlData.put("abils", sb.length() == 0 ? "" : sb.substring(split.length()));

        sb.setLength(0);
        for (final Map.Entry<Modifier.ModState, Integer> en : mods.entrySet()) {
            final Modifier.ModState as = en.getKey();
            sb.append(split).append((int) en.getValue()).append(eq)
                .append(as.mod().id()).append(eq).append(as.lvl());
        }
        mysqlData.put("mods", sb.length() == 0 ? "" : sb.substring(split.length()));

        sb.setLength(0);
        sb.append("exp").append(eq).append(exp).append(split)
            .append("mana").append(eq).append(mana.intValue()).append(split)
            .append("mobKills").append(eq).append(mobKills).append(split)
            .append("deaths").append(eq).append(deaths).append(split)
            .append("statPoints").append(eq).append(statsPoints).append(split)
            .append("worldOpen").append(eq).append(worldOpen).append(split)
            .append("roleStamp").append(eq).append(roleStamp).append(split)
            .append("board").append(eq).append(showScoreBoard).append(split)
            .append("acBar").append(eq).append(showActionBar);
        mysqlData.put("data", sb.toString());
    }

    /*
    @Override
    public boolean transferSouls(final World in) {
        int total = Math.min(souls, getStat(Stats.Интеллект) >> 2);
        if (total == 0) {
            return false;
        }

        final ArrayList<Transfer> real = new ArrayList<>();
        for (int i = 0; i < to.length; i++) {
            final Transfer tr = to[i].get();
            if (Transfer.validate(tr)
                    && tr.getSouls() < tr.getMaxSouls()) {
                real.add(tr);
            } else {
                to[i] = new WeakReference<Transfer>(null);
            }
        }
        if (real.isEmpty()) {
            return false;
        }
        final int each = total / real.size();
        total = each * real.size();
        if (each == 0) {
            return false;
        }
        for (final Transfer tr : real) {
            if (tr instanceof CuBlock) {
                final CuBlock cb = (CuBlock) tr;
                final int amt = each > cb.cbt.maxSouls - cb.souls
                        ? cb.cbt.maxSouls - cb.souls : each;
                total -= each - amt;
                if (amt == 0) {
                    continue;
                }
                tr.changeSouls(amt);
            } else {
                tr.changeSouls(each);
            }
            tetherTo(tr, in);
        }

        changeSouls(-total);
        return true;
    }

    @Override
    public void tetherTo(final Transfer to, final World w) {
        final Location loc = getLoc(w);
        final Location step = to.getLoc(w).subtract(loc);
        final int length = (int) step.length();
        step.multiply(0.25d / length);
        new BukkitRunnable() {
            int i = length << 2;

            @Override
            public void run() {
                w.spawnParticle(Particle.SOUL, loc.add(step), 1, 0.1d, 0.1d, 0.1d, 0d);
                if ((i & 3) == 0) {
                    w.playSound(loc, Sound.BLOCK_SCULK_CHARGE, 1f, 0.8f);
                }

                if ((i--) < 0) {
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(Main.main, 2, 2);
    }

    @Override
    public int getSouls() {
        return souls;
    }

    @Override
    public int getMaxSouls() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getTId() {
        return id;
    }

    @Override
    public void changeSouls(int amt) {
        if (amt == 0) {
            return;
        }
        souls += amt;
        EntUtil.displayArm(getPlayer().getLocation(), (amt < 0 ? "§9" : "§3+") + amt + "✞", 1);
    }

    @Override
    public TransferType getTransType() {
        return TransferType.BOTH;
    }

    @Override
    public Location getLoc(final World w) {
        return getPlayer().getEyeLocation();
    }

    @Override
    public boolean addTo(final Transfer tr) {
        int spot = to.length;
        final int id = tr.getTId();
        if (getTId() == id) {
            return false;
        }
        for (int i = 0; i < to.length; i++) {
            final Transfer t = to[i].get();
            if (Transfer.validate(t)) {
                if (t.getTId() == id) {
                    return false;
                }
            } else {
                spot = Math.min(spot, i);
            }
        }
        if (spot == to.length || tr.hasTo(this)) {
            return false;
        }
        to[spot] = new WeakReference<>(tr);
        return true;
    }

    @Override
    public boolean hasTo(final Transfer tr) {
        final int id = tr.getTId();
        if (getTId() == id) {
            return false;
        }
        for (int i = 0; i < to.length; i++) {
            final Transfer t = to[i].get();
            if (Transfer.validate(t) && t.getTId() == id) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean rmvTo(final Transfer tr) {
        boolean fnd = false;
        final int id = tr.getTId();
        if (getTId() == id) {
            return false;
        }
        for (int i = 0; i < to.length; i++) {
            final Transfer t = to[i].get();
            if (Transfer.validate(t) && t.getTId() == id) {
                to[i] = etr;
                fnd = true;
            }
        }
        return fnd;
    }
*/
    //@Override
    public void transferTick(final World in) {
      /*  if (souls == 0) {
            Arrays.fill(to, etr);
            return;
        }

        int slots = 0;
        final Location loc = getPlayer().getLocation();
        for (int i = 0; i < to.length; i++) {
            final Transfer tr = to[i].get();
            if (Transfer.validate(tr)) {
                if (tr.getLoc(in).distanceSquared(loc) < distSQ) {
                    continue;
                }
                to[i] = etr;
                slots++;
            } else {
                slots++;
            }
        }

        for (final CuBlock cb : SM.getTypeTransfers(TransferType.TAKE)) {
            if (slots != 0 && cb.souls < cb.cbt.maxSouls
                    && cb.getLoc(in).distanceSquared(loc) < distSQ && addTo(cb)) {
                slots--;
            }
        }

        for (final CuBlock cb : SM.getTypeTransfers(TransferType.BOTH)) {
            if (slots != 0 && cb.souls < cb.cbt.maxSouls
                    && cb.getLoc(in).distanceSquared(loc) < distSQ && addTo(cb)) {
                slots--;
            }
        }

        transferSouls(in);
        */
    }
}