package ru.romindous.skills.survs;

import javax.annotation.Nullable;
import java.util.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.notes.OverrideMe;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Main;
import ru.romindous.skills.SubServer;
import ru.romindous.skills.guides.Entry;
import ru.romindous.skills.guides.Section;
import ru.romindous.skills.menus.SkillMenu;
import ru.romindous.skills.skills.Caster;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.tasks.Task;

import static ru.romindous.skills.listeners.MySqlLst.eq;

public class Survivor extends Oplayer implements Caster/*, Transfer*/ {
    //сохраняемое-загружаемое
    public Role role;
    public int exp, roleStamp, statsPoints, worldOpen;
    public boolean showScoreBoard, showActionBar; //показывать - не показывать
    public final int transId; //transfer id

    //динамические
    /*public int tickAsync; //тики с момента входа. p.getTicksLived() не подходит, выхватывает числа не по порядку
    public int currentPlyTime; //игровое время в секундах с момента входа
    public int currentLiveSec; //секунды текущей жизни*/
    public int acBarPause; //пауза ActionBar
    public int maxMana = 10, maxHP = 20;
    private float mana = 0f;
    public Input jump;
    public final StringBuffer abBuffer = new StringBuffer(); //для построения строки актионбар
    private final int[] stats = new int[Stat.VALUES.length];
    public final List<Skill> skills = new ArrayList<>();
    public final Set<Entry> unread = new HashSet<>();
    public final HashMap<Selector.SelState, Integer> sels = new HashMap<>();
    public final HashMap<Ability.AbilState, Integer> abils = new HashMap<>();
    public final HashMap<Modifier.ModState, Integer> mods = new HashMap<>();
    public final SmartInventory skillInv;
    public final SkillMenu skillMenu;
    //private WeakReference<Transfer>[] to;
    public Task miniQuestTask;
    public @Nullable Section section;

    public Survivor(final HumanEntity pl) {
        super(pl);
        for (final Stat st : Stat.values()) {
            stats[st.ordinal()] = 0;
        }
        transId = SM.tId++;
        skillMenu = new SkillMenu(this);
        skillInv = SmartInventory.builder()
            .id("Skill "+pl.getName())
            .provider(skillMenu)
            .size(3, 9)
            .title("         §5§lНавыки Класса")
            .build();
        sels.put(Selector.SAME_ST, 1);
        sels.put(Selector.CASTER_ST, 1);
    }

    //===================== ТАБЛОИДЫ =====================

    public void updateBar(final Player p) {
        if (!showActionBar) return;

        abBuffer.setLength(0);
        //❤ : 50(100)   🔥 : 10(100)
        abBuffer.append(getHeartIcon((int) Math.round(p.getHealth())))
            .append(TCUtil.N).append("<dark_gray>/").append(maxHP)
        .append(Stat.MAGIC.color()).append("   🔥 ").append((int) mana)
            .append(TCUtil.N).append("<dark_gray>/").append(maxMana);

        if (role == null) {
            abBuffer.append(TCUtil.N).append("   Роль не выбрана!");
        }
        p.sendActionBar(TCUtil.form(abBuffer.toString()));
    }

    private static final int ACBAR_PAUSE_TIME = 2;
    public void inform(final LivingEntity le, final String msg) {
        le.sendActionBar(TCUtil.form(msg));
        acBarPause = ACBAR_PAUSE_TIME;
    }

    private String getHeartIcon(final int hp) {
        return switch (hp * 5 / maxHP) {
            case 0 -> SM.HEART_LOW + SM.HEART_CLR + hp;
            case 1 -> SM.HEART_LESS + SM.HEART_CLR + hp;
            case 2 -> SM.HEART_HALF + SM.HEART_CLR + hp;
            case 3 -> SM.HEART_FULL + SM.HEART_CLR + hp;
            default -> SM.HEART_MAX + SM.HEART_CLR + hp;
        };
    }

    private static final String BOARD_LVL = "lvl";
    private static final String BOARD_HP = "hp";
    private static final String BOARD_MANA = "mana";

    private void resetBoard(final HumanEntity pl) {
        score.getSideBar().reset().title(role == null ? "§8Не Выбран" : role.disName())
            .add("<dark_gray>🢗🢗🢗🢗🢗🢗🢗🢗🢗🢗🢗🢗")
            .add(BOARD_LVL, TCUtil.N + "Уровень: " + TCUtil.A + getLevel())
            .add(BOARD_HP, TCUtil.N + "ХП: "
                + getHeartIcon((int) pl.getHealth()) + "<dark_gray>/" + maxHP)
            .add(BOARD_MANA, TCUtil.N + "Душ: " + Stat.MAGIC.color()
                + "🔥 " + (int) mana + "<dark_gray>/" + maxMana)
            .add("<dark_gray>🢕🢕🢕🢕🢕🢕🢕🢕🢕🢕🢕🢕")
            .add("§e ostrov77.ru").build();
    }

    public void updateBoard(final Player p, final SM.Info type) {
        if (!showScoreBoard) return;
        switch (type) {
            case ALL:
                resetBoard(p);
                break;
            case LEVEL:
                score.getSideBar().update(BOARD_LVL,
                    TCUtil.N + "Уровень: " + TCUtil.A + getLevel());
                break;
            case HEALTH:
                score.getSideBar().update(BOARD_HP, TCUtil.N + "ХП: "
                    + getHeartIcon((int) Math.round(p.getHealth()))
                    + "<dark_gray>/" + maxHP);
                break;
            case MANA:
                score.getSideBar().update(BOARD_MANA, TCUtil.N + "Душ: " + Stat.MAGIC.color()
                    + "🔥 " + (int) mana + "<dark_gray>/" + maxMana);
                break;
        }
    }
    //======================================================

    public int count(final Scroll.State as) {
        final Map<? extends Scroll.State, Integer> stm = stateMap(as);
        if (stm == null) return 0;
        return stm.getOrDefault(as, 0);
    }

    public int change(final Scroll.State as, final int inc) {
//        new IllegalArgumentException().printStackTrace();
        final Map<? extends Scroll.State, Integer> stm = stateMap(as);
        if (stm == null) return 0;
        if (Scroll.DEFAULT.contains(as.val())) return 1;
        final Integer amt = stm.remove(as);
        if (amt == null) {
            if (inc <= 0) return 0;
            set(stm, as, inc);
            return inc;
        } else {
            final int fam = amt + inc;
            if (fam <= 0) return 0;
            set(stm, as, fam);
            return fam;
        }
    }

    public @Nullable Map<? extends Scroll.State, Integer> stateMap(final Scroll.State as) {
        return switch (as) {
            case final Selector.SelState ignored -> sels;
            case final Ability.AbilState ignored -> abils;
            case final Modifier.ModState ignored -> mods;
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private static <S extends Scroll.State>
    void set(final Map<S, Integer> stm, final Object st, final int n) {
        stm.put((S) st, n);
    }

    public boolean owns(final Scroll sc) {
        return switch (sc) {
            case final Selector sl -> {
                for (final Selector.SelState ss : sels.keySet())
                    if (ss.val().equals(sl)) yield true;
                for (final Skill sk : skills)
                    for (final Selector.SelState ss : sk.sels)
                        if (ss.val().equals(sl)) yield true;
                yield false;
            }
            case final Ability ab -> {
                for (final Ability.AbilState as : abils.keySet())
                    if (as.val().equals(ab)) yield true;
                for (final Skill sk : skills)
                    for (final Ability.AbilState as : sk.abils)
                        if (as.val().equals(ab)) yield true;
                yield false;
            }
            case final Modifier md -> {
                for (final Modifier.ModState ms : mods.keySet())
                    if (ms.val().equals(md)) yield true;
                for (final Skill sk : skills)
                    for (final Modifier.ModState ms : sk.mods)
                        if (ms.val().equals(md)) yield true;
                yield false;
            }
            default -> false;
        };
    }

    public boolean canUse(final Scroll sc) {
        return sc.role() == null || sc.role() == role;
    }

    public void setSkillAbil(final Player p, final @Nullable Ability.AbilState nas, final int abPos, final int skPos) {
        final Skill skill = skills.get(skPos);
        final List<Ability.AbilState> nasl = new ArrayList<>(Arrays.asList(skill.abils));
        final List<Selector.SelState> nssl = new ArrayList<>(Arrays.asList(skill.sels));

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
                if (nas.val().selfCast()) {
                    change(nssl.get(abPos), 1);
                    nssl.set(abPos, Selector.CASTER_ST);
                } else if (Selector.CASTER.equals(nssl.get(abPos).val()))
                    nssl.set(abPos, Selector.SAME_ST);
            }
            change(curr, 1);
        } else {
            //new val
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
            nssl.add(nas.val().selfCast() ? Selector.CASTER_ST : Selector.SAME_ST);
        }

        setSkill(p, skPos, new Skill(skill.name, skill.trig, nssl.toArray(new Selector.SelState[0]),
            nasl.toArray(new Ability.AbilState[0]), skill.mods));
    }

    public void setSkillSel(final Player p, final @Nullable Selector.SelState nss, final int abPos, final int skPos) {
        final Skill skill = skills.get(skPos);
        final List<Selector.SelState> nmsl = new ArrayList<>(Arrays.asList(skill.sels));
        if (abPos >= skill.abils.length) {
            Ostrov.log_warn("Tried setting selector at pos " + abPos
                + ", skill only has " + (skill.abils.length - 1));
            return;
        }

        final Selector.SelState curr = skill.sels[abPos];
        if (nss == null) {
            nmsl.set(abPos, Selector.SAME_ST);
        } else {
            if (!nss.val().equals(Selector.SAME) && change(nss, -1) < 0) {
                p.sendMessage("<red>У тебя нет такого подборника!");
                return;
            }
            nmsl.set(abPos, nss);
        }
        change(curr, 1);

        setSkill(p, skPos, new Skill(skill.name, skill.trig, nmsl.toArray(new Selector.SelState[0]),
            skill.abils, skill.mods));
    }

    public void addSkillMod(final Player p, final Modifier.ModState nms, final int skPos) {
        final Skill skill = skills.get(skPos);
        final List<Modifier.ModState> nmsl = new ArrayList<>(Arrays.asList(skill.mods));
        if (change(nms, -1) < 0) {
            p.sendMessage("<red>У тебя нет такого модификатора!");
            return;
        }
        nmsl.add(nms);
        setSkill(p, skPos, new Skill(skill.name, skill.trig, skill.sels,
            skill.abils, nmsl.toArray(new Modifier.ModState[0])));
    }

    public void remSkillMod(final Player p, final int mdPos, final int skPos) {
        final Skill skill = skills.get(skPos);
        if (mdPos >= skill.mods.length) {
            Ostrov.log_warn("Tried setting null val at pos " + mdPos
                + ", skill only has " + skill.abils.length);
            return;
        }
        final List<Modifier.ModState> nmsl = new ArrayList<>(Arrays.asList(skill.mods));
        change(nmsl.remove(mdPos), 1);
        setSkill(p, skPos, new Skill(skill.name, skill.trig, skill.sels,
            skill.abils, nmsl.toArray(new Modifier.ModState[0])));
    }

    public void setSkill(final Player p, final int skPos, final @Nullable Skill nsk) {
        if (skPos < skills.size()) {
            if (nsk == null) {
                skills.remove(skPos);
                return;
            }
            final Skill skill = skills.get(skPos);
            p.hideBossBar(skill.cdBar);
            nsk.setCD(skill.currCD());
            skills.set(skPos, nsk);
        } else {
            if (nsk == null) return;
            skills.add(nsk);
        }
    }

    public void giveScroll(final Player p, final Scroll sc, final int lvl) {
        final boolean first = !owns(sc) && p.getGameMode() == GameMode.SURVIVAL;
        if (first) {
            ScreenUtil.sendTitle(p, "", TCUtil.sided(sc.name(lvl), sc.side()), 4, 20, 16);
            Nms.totemPop(p, sc.icon().createItemStack());
        }
        final int cnt;
        switch (sc) {
            case final Selector sl:
                cnt = change(new Selector.SelState(sl, lvl), 1);
                if (first) {
                    p.sendMessage(TCUtil.form(Main.prefix + "Принят подборник " + sl.rarity().color() + sl.name()
                        + TCUtil.N + " ур. " + TCUtil.P + (lvl + 1) + TCUtil.N + ",\nможешь добавлять " + TCUtil.P + "его "
                        + TCUtil.N + "себе в " + TCUtil.P + "навыки" + TCUtil.N + "!"));
                    return;
                }
                p.sendMessage(TCUtil.form(Main.prefix + "Подборник " + sl.rarity().color() + sl.name()
                    + TCUtil.N + " ур. " + TCUtil.P + (lvl + 1) + TCUtil.N + " принят,\nтеперь у тебя их "
                    + TCUtil.A + cnt + " шт." + TCUtil.N + "!"));
                EntityUtil.effect(p, Sound.ITEM_ARMOR_EQUIP_WOLF, 1.2f, Particle.HAPPY_VILLAGER);
                break;
            case final Ability ab:
                cnt = change(new Ability.AbilState(ab, lvl), 1);
                if (first) {
                    p.sendMessage(TCUtil.form(Main.prefix + "Принята способность " + ab.rarity().color() + ab.name()
                        + TCUtil.N + " ур. " + TCUtil.P + (lvl + 1) + TCUtil.N + ",\nможешь добавлять " + TCUtil.P + "ее "
                        + TCUtil.N + "себе в " + TCUtil.P + "навыки" + TCUtil.N + "!"));
                    return;
                }
                p.sendMessage(TCUtil.form(Main.prefix + "Способность " + ab.rarity().color() + ab.name()
                    + TCUtil.N + " ур. " + TCUtil.P + (lvl + 1) + TCUtil.N + " принята,\nтеперь у тебя их "
                    + TCUtil.A + cnt + " шт." + TCUtil.N + "!"));
                EntityUtil.effect(p, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.2f, Particle.HAPPY_VILLAGER);
                break;
            case final Modifier md:
                cnt = change(new Modifier.ModState(md, lvl), 1);
                if (first) {
                    p.sendMessage(TCUtil.form(Main.prefix + "Принят модификатор " + md.rarity().color() + md.name()
                        + TCUtil.N + " ур. " + TCUtil.P + (lvl + 1) + TCUtil.N + ",\nможешь добавлять " + TCUtil.P + "его "
                        + TCUtil.N + "себе в " + TCUtil.P + "навыки" + TCUtil.N + "!"));
                    return;
                }
                p.sendMessage(TCUtil.form(Main.prefix + "Модификатор " + md.rarity().color() + md.name()
                    + TCUtil.N + " ур. " + TCUtil.P + (lvl + 1) + TCUtil.N + " принят,\nтеперь у тебя их "
                    + TCUtil.A + cnt + " шт." + TCUtil.N + "!"));
                EntityUtil.effect(p, Sound.ITEM_ARMOR_EQUIP_TURTLE, 1.2f, Particle.HAPPY_VILLAGER);
                break;
            default:
                break;
        }
    }

    //===================== СТАТА =====================

    public void setStat(final Stat st, final int num) {
        stats[st.ordinal()] = num;
    }

    public int getStat(final Stat st) {
        return stats[st.ordinal()];
    }

    //- после загрузки
    //- при изменении уровня
    //- при выборе/смене скила
    //- из меню StatsMenu при изменении стат
    private static final int START_MAX_MANA = 20, START_MAX_HP = 20;
    private static final double START_MAX_SPEED = 0.2d;
    public void recalcStats(final Player p) {
        maxMana = (int) Stat.mana(START_MAX_MANA, getStat(Stat.MAGIC));
        maxHP = (int) Stat.health(START_MAX_HP, getStat(Stat.STRENGTH));
        eqStatPoint(getLevel());
        applySkill(p);
    }

    //из recalcStats и PlayerRespawnEvent
    public void applySkill(final Player p) {
        p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHP);
        p.setHealthScaled(true);
        p.setHealthScale((maxHP << 1) / SM.HP_PER_HEART);

        final float walkSpeed = (float) Stat.speed(START_MAX_SPEED, getStat(Stat.AGILITY));
        if (p.getWalkSpeed() != walkSpeed) p.setWalkSpeed(walkSpeed);
        updateBoard(p, SM.Info.ALL);
    }

    public static final float EXP_DEL = (float) SM.value("delimit", 6d);
    public static final float LVL_DEL = 1f / EXP_DEL;

    public void setXp(final Player p, final int ammount) {
        exp = ammount;
        if (exp == 0) {
            p.setLevel(0);
            p.setExp(0f);
            updateBoard(p, SM.Info.LEVEL);
            return;
        }

        final int currLvl = NumUtil.sqrt((int) (exp * LVL_DEL));
        final int lvlSq = currLvl * currLvl;
        p.setLevel(currLvl);
        p.setExp(Math.clamp((exp - lvlSq * EXP_DEL) /
            ((NumUtil.square(currLvl + 1) - lvlSq) * EXP_DEL), 0f, 1f));
        updateBoard(p, SM.Info.LEVEL);
        eqStatPoint(currLvl);
    }

    public void addXp(final Player p, final int ammount) {
        if (role == null || ammount == 0) return;
        final int currExp = exp + p.applyMending(ammount);
        final int oldLvl = (int) Math.sqrt(exp * LVL_DEL);
        final int currLvl = (int) Math.sqrt(currExp * LVL_DEL);
        final int lvlSq = currLvl * currLvl;
        exp = currExp;
        p.setLevel(currLvl);
        p.setExp(Math.clamp((currExp - lvlSq * EXP_DEL) /
            ((NumUtil.square(currLvl + 1) - lvlSq) * EXP_DEL), 0f, 1f));

        if (currLvl - oldLvl < 1) return;
        //уровень добавляется
        p.sendMessage(TCUtil.form(Main.prefix + "Достигнут уровень " + role.color() + currLvl));
        ScreenUtil.sendTitle(p, TCUtil.N + "Новый Уровень " + role.color() + currLvl,
            TCUtil.N + ClassUtil.rndElmt(SM.CONGRATS));
        p.playSound(p.getEyeLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 2f, 0.6f);
        updateBoard(p, SM.Info.LEVEL);
        eqStatPoint(currLvl);
    }

    public void eqStatPoint(final int lvl) {
        int st = lvl;
        for (final Stat s : Stat.VALUES) {
            st -= stats[s.ordinal()];
        }
        statsPoints = st < 0 ? 0 : st;
    }

    public int getLevel() {
        return NumUtil.sqrt((int) (exp * LVL_DEL));
    }

    public int nextLevelXp() {
        final int lvl = getLevel();
        return (int) ((NumUtil.square(lvl + 1) - lvl * lvl) * EXP_DEL);
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
    }

    public void tryRegen(final Player pl, final double amt) {
        if (pl != null && pl.isValid() && amt > 0d && pl.getHealth() > 0d) { //реген только если не сдох
            final EntityRegainHealthEvent regenEvent = new EntityRegainHealthEvent(pl, amt, EntityRegainHealthEvent.RegainReason.CUSTOM);
            regenEvent.callEvent();
            addHp(pl, regenEvent.getAmount());
        }
    }

    public float mana() {
        return mana;
    }

    public void setMana(final Player p, final float amt) {
        if (amt < 0 || role == null) return;
        mana = amt;
        updateBoard(p, SM.Info.MANA);
    }

    public void chgMana(final LivingEntity le, final float amt) {
        if (role == null) return;
        mana = Math.min(mana + amt, maxMana);
        if (!(le instanceof Player p)) return;
        updateBoard(p, SM.Info.MANA);
    }

    public boolean isWorldOpen(final SubServer ss) {
        return (worldOpen & (1 << (ss.ordinal() + 1))) == (1 << (ss.ordinal() + 1));
    }

    public void unlockWorld(final SubServer ss) {
        worldOpen = worldOpen | (1 << (ss.ordinal() + 1));
    }

    public void trigger(final Trigger tr, final Event e, final LivingEntity caster) {
        for (final Skill sk : skills) sk.attempt(tr, e, caster, this);
    }

    @Override
    public int hashCode() {
        return nik.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj instanceof final Survivor srv) {
            return nik.equals(srv.nik);
        }
        return false;
    }

    @Override
    public String toString() {
        return nik + ", skill=" + role.disName() + ", lvl=" + getLevel() + ", souls=" + mana;
    }

    private static final float lvlFactor = (float) SM.value("lvlFactor", 0.05d);
    private static final double mobSpeed = SM.value("mobSpeed", 0.2d);
    private static final double mobKbRes = SM.value("mobKbRes", 0.5d);
    private static final double mobFollow = SM.value("mobFollow", 1d);
    private static final double mobDamage = SM.value("mobDamage", 0.6d);
    private static final double mobHealth = SM.value("mobHealth", 1.2d);

    public float getMobCoof() {
        return getLevel() * lvlFactor * Main.subServer.bfr;
    }

    public void setMobChars(final Mob mob) {
        //прокачка характеристик в зависимости от мира
        final float cf = getMobCoof();
        scaleAtr(mob.getAttribute(Attribute.MOVEMENT_SPEED), cf * mobSpeed);
        scaleAtr(mob.getAttribute(Attribute.WATER_MOVEMENT_EFFICIENCY), cf * mobSpeed);
        scaleAtr(mob.getAttribute(Attribute.SNEAKING_SPEED), cf * mobSpeed);
        scaleAtr(mob.getAttribute(Attribute.FLYING_SPEED), cf * mobSpeed);
        scaleAtr(mob.getAttribute(Attribute.KNOCKBACK_RESISTANCE), cf * mobKbRes);
        scaleAtr(mob.getAttribute(Attribute.FOLLOW_RANGE), cf * mobFollow);
        scaleAtr(mob.getAttribute(Attribute.TEMPT_RANGE), cf * mobFollow);
        scaleAtr(mob.getAttribute(Attribute.ATTACK_DAMAGE), cf * mobDamage);
        scaleAtr(mob.getAttribute(Attribute.MAX_HEALTH), cf * mobHealth);
        scaleAtr(mob.getAttribute(Attribute.MAX_ABSORPTION), cf * mobHealth);
        mob.setHealth(mob.getAttribute(Attribute.MAX_HEALTH).getBaseValue());

        Stat.modMob(mob, getStat(Stat.CONTROL));
    }

    public static boolean scaleAtr(final AttributeInstance ais, final double vl) {
        if (ais != null) {
            ais.setBaseValue(Math.max((ais.getBaseValue() + 0.1d * vl) * (1d + vl), 0d));
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
            sb.append(StringUtil.SPLIT_0).append(st.name().toLowerCase(Locale.ROOT))
                    .append(eq).append(getStat(st));
        }
        mysqlData.put("stats", sb.substring(StringUtil.SPLIT_0.length()));

        sb.setLength(0);
        final StringBuilder sb2 = new StringBuilder();
        for (final Skill sk : skills) {
            sb.append(StringUtil.SPLIT_0).append(sk.name).append(eq)
                .append(sk.trig.name().toLowerCase(Locale.ROOT)).append(eq);

            sb2.setLength(0);
            for (final Selector.SelState sl : sk.sels) {
                sb2.append(StringUtil.SPLIT_1).append(sl.val().id())
                    .append(StringUtil.SPLIT_2).append(sl.lvl());
            }
            sb.append(sb2.length() == 0 ? "" : sb2.substring(StringUtil.SPLIT_1.length())).append(eq);

            sb2.setLength(0);
            for (final Ability.AbilState ab : sk.abils) {
                sb2.append(StringUtil.SPLIT_1).append(ab.val().id())
                    .append(StringUtil.SPLIT_2).append(ab.lvl());
            }
            sb.append(sb2.length() == 0 ? "" : sb2.substring(StringUtil.SPLIT_1.length())).append(eq);

            sb2.setLength(0);
            for (final Modifier.ModState md : sk.mods) {
                sb2.append(StringUtil.SPLIT_1).append(md.val().id())
                    .append(StringUtil.SPLIT_2).append(md.lvl());
            }
            sb.append(sb2.length() == 0 ? "" : sb2.substring(StringUtil.SPLIT_1.length())).append(eq)
                .append(Math.max(sk.currCD(), 0d));
        }
        mysqlData.put("skills", sb.length() == 0 ? "" : sb.substring(StringUtil.SPLIT_0.length()));

        sb.setLength(0);
        for (final Map.Entry<Selector.SelState, Integer> en : sels.entrySet()) {
            final Selector.SelState ss = en.getKey();
            if (Scroll.DEFAULT.contains(ss.val())) continue;
            sb.append(StringUtil.SPLIT_0).append((int) en.getValue()).append(eq)
                .append(ss.val().id()).append(eq).append(ss.lvl());
        }
        mysqlData.put("sels", sb.length() == 0 ? "" : sb.substring(StringUtil.SPLIT_0.length()));

        sb.setLength(0);
        for (final Map.Entry<Ability.AbilState, Integer> en : abils.entrySet()) {
            final Ability.AbilState as = en.getKey();
            if (Scroll.DEFAULT.contains(as.val())) continue;
            sb.append(StringUtil.SPLIT_0).append((int) en.getValue()).append(eq)
                .append(as.val().id()).append(eq).append(as.lvl());
        }
        mysqlData.put("abils", sb.length() == 0 ? "" : sb.substring(StringUtil.SPLIT_0.length()));

        sb.setLength(0);
        for (final Map.Entry<Modifier.ModState, Integer> en : mods.entrySet()) {
            final Modifier.ModState ms = en.getKey();
            if (Scroll.DEFAULT.contains(ms.val())) continue;
            sb.append(StringUtil.SPLIT_0).append((int) en.getValue()).append(eq)
                .append(ms.val().id()).append(eq).append(ms.lvl());
        }
        mysqlData.put("mods", sb.length() == 0 ? "" : sb.substring(StringUtil.SPLIT_0.length()));

        /*sb.setLength(0);
        for (final Entry en : unread) sb.append(StringUtil.SPLIT_0).append(en.parent.code);
        mysqlData.put("unread", sb.length() == 0 ? "" : sb.substring(StringUtil.SPLIT_0.length()));*/

        sb.setLength(0);
        sb.append("exp").append(eq).append(exp).append(StringUtil.SPLIT_0)
            .append("mana").append(eq).append((int) mana).append(StringUtil.SPLIT_0)
            .append("statPoints").append(eq).append(statsPoints).append(StringUtil.SPLIT_0)
            .append("worldOpen").append(eq).append(worldOpen).append(StringUtil.SPLIT_0)
            .append("roleStamp").append(eq).append(roleStamp).append(StringUtil.SPLIT_0)
            .append("board").append(eq).append(showScoreBoard).append(StringUtil.SPLIT_0)
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
            final Transfer tr = to[i].val();
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
            final Transfer t = to[i].val();
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
            final Transfer t = to[i].val();
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
            final Transfer t = to[i].val();
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
            final Transfer tr = to[i].val();
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