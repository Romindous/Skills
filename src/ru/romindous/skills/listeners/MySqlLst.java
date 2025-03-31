package ru.romindous.skills.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Data;
import ru.komiss77.events.FriendTeleportEvent;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.events.QuestCompleteEvent;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.*;
import ru.romindous.skills.Main;
import ru.romindous.skills.SubServer;
import ru.romindous.skills.guides.Entries;
import ru.romindous.skills.guides.Entry;
import ru.romindous.skills.menus.WorldMenu;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.survs.SM;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.survs.Survivor;


public class MySqlLst implements Listener {

    public static final String eq = "=";

    /*@EventHandler (priority = EventPriority.MONITOR)
    public void onJoin (final PlayerJoinEvent e) {
        Ostrov.sync(() -> {
            final Player pl = e.getPlayer();
            final Oplayer op = PM.getOplayer(pl);
            op.firstJoin = true;
            new LocalDataLoadEvent(pl, op, pl.getLocation()).callEvent();
        }, 20);
    }*/

    @EventHandler (priority = EventPriority.MONITOR)
    public void onQuest (final QuestCompleteEvent e) {
        if (!(e.getQuest() instanceof final Entry en)
            || en.sec == null || en.page == null) return;
        final Player pl = e.getPlayer();
        PM.getOplayer(pl, Survivor.class).unread.add(en);
        Ostrov.sync(() -> pl.sendMessage(TCUtil.form(TCUtil.N
            + "Есть новая запись в '" + TCUtil.P + "Заметках" + TCUtil.N + "'!")), 2);
        pl.playSound(pl, Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 2f, 0.8f);
    }
    
    @EventHandler (priority = EventPriority.MONITOR)
    public void onDataLoad (final LocalDataLoadEvent e) {
        final Player p = e.getPlayer();
        final Survivor sv = (Survivor) e.getOplayer();
//        int fstHp = 1;
        
        if (e.hasSqlError()) {
            Ostrov.log_err(p.getName()+":LocalDataLoadEvent-hasSqlError!");
            p.sendMessage("§cОшибка загрузки скилл, при выходе данные не будут сохраняться!");
            ScreenUtil.sendBossbarDirect(p, "§4Ошибка загрузки",
                20, Color.RED, BossBar.Overlay.NOTCHED_6);
            return;
        }

        final Location save = LocUtil.stringToLoc(sv.world_positions.get(p.getWorld().getName()), false, true);
        
        if (save == null) {
            SM.randomJoin(p, sv.firstJoin);
        } else {
            MoveUtil.safeTP(p, save);//p.teleport(save);
        }

        Main.diary.give(p);
        sv.tag.visible(true);

        if (sv.firstJoin) {
            hasNoSkill(p, sv);
            return;
        }

        for (final Map.Entry<String, String> en : e.getData().entrySet()) {
            if (en.getValue().isBlank()) continue;
            switch (en.getKey()) {
                case "role":
                    sv.role = Role.get(en.getValue()); //ключ skill точно будет, наличие проверяется выше!
                    if (sv.role == null) {
                        Ostrov.log_warn("decode role data: no role -> "+en.getValue());
                        hasNoSkill(p, sv);
                        break;
                    }
                    break;
                case "resps":
                    final String wnm = p.getWorld().getName();
                    for (final String s : en.getValue().split(StringUtil.SPLIT_0)) {
                        if (!s.startsWith(wnm)) continue;
                        p.setRespawnLocation(BVec.parse(s)
                            .center(p.getWorld()), false);
                    }
                    break;
                case "stats":
                    for (final String s : en.getValue().split(StringUtil.SPLIT_0)) {
                        final int eqn = s.indexOf(eq);
                        if (eqn == -1) {
                            Ostrov.log_err("decode stats error -> "+s);
                            return;
                        }
                        final Stat st = Stat.get(s.substring(0, eqn));
                        if (st==null) {
                            Ostrov.log_err("decode skill data stats: Stats==null -> "+s);
                            continue;
                        }
                        final int val = NumUtil.intOf(s.substring(eqn + 1), 0);
                        sv.setStat(st, val);
                    }
                    break;
                case "skills":
                    sv.skills.clear();
                    for (final String sk : en.getValue().split(StringUtil.SPLIT_0)) {
//                        p.sendMessage("skill- '" + sk + "'");
                        final String[] skl = sk.split(eq);
                        final List<Selector.SelState> sels = new ArrayList<>();
                        final List<Ability.AbilState> abils = new ArrayList<>();
                        final List<Modifier.ModState> mods = new ArrayList<>();
                        Trigger trig = Trigger.UNKNOWN;
                        double cd = 0;
                        final Skill fsk;
                        switch (skl.length) {
                            default:
                                cd = NumUtil.doubleOf(skl[5], 0d);
                            case 5:
                                if (!skl[4].isBlank()) {
                                    for (final String sl : skl[4].split(StringUtil.SPLIT_1)) {
                                        final int eqn = sl.indexOf(StringUtil.SPLIT_2);
                                        if (eqn == -1) {
                                            Ostrov.log_err("decode skill mods error -> "+sl);
                                            continue;
                                        }
                                        final Modifier m = Modifier.VALUES.get(sl.substring(0, eqn));
                                        if (m == null) {
                                            Ostrov.log_err("decode skill mods id -> "+sl);
                                            continue;
                                        }
                                        mods.add(new Modifier.ModState(m,
                                            NumUtil.intOf(sl.substring(eqn + 1), 0)));
                                    }
                                }
                            case 4:
                                if (!skl[3].isBlank()) {
                                    for (final String ab : skl[3].split(StringUtil.SPLIT_1)) {
                                        final int eqn = ab.indexOf(StringUtil.SPLIT_2);
                                        if (eqn == -1) {
                                            Ostrov.log_err("decode skill abils error -> "+ab);
                                            continue;
                                        }
                                        final Ability a = Ability.VALUES.get(ab.substring(0, eqn));
                                        if (a == null) {
                                            Ostrov.log_err("decode skill abils id -> "+ab);
                                            continue;
                                        }
                                        abils.add(new Ability.AbilState(a,
                                            NumUtil.intOf(ab.substring(eqn + 1), 0)));
                                    }
                                }
                            case 3:
                                if (!skl[2].isBlank()) {
                                    for (final String md : skl[2].split(StringUtil.SPLIT_1)) {
                                        final int eqn = md.indexOf(StringUtil.SPLIT_2);
                                        if (eqn == -1) {
                                            Ostrov.log_err("decode skill sels error -> "+md);
                                            continue;
                                        }
                                        final Selector m = Selector.VALUES.get(md.substring(0, eqn));
                                        if (m == null) {
                                            Ostrov.log_err("decode skill sels id -> "+md);
                                            continue;
                                        }
                                        sels.add(new Selector.SelState(m,
                                            NumUtil.intOf(md.substring(eqn + 1), 0)));
                                    }
                                }
                            case 2:
                                trig = Trigger.get(skl[1]);
                            case 1:
                                fsk = new Skill(skl[0], trig, sels.toArray(new Selector.SelState[0]),
                                    abils.toArray(new Ability.AbilState[0]), mods.toArray(new Modifier.ModState[0]));
                                fsk.setCD(cd);
                                sv.skills.add(fsk);
                                break;
                            case 0:
                        }
                    }
                    break;
                case "sels":
                    sv.sels.clear();
                    for (final String sl : en.getValue().split(StringUtil.SPLIT_0)) {
                        final int eqn1 = sl.indexOf(eq);
                        if (eqn1 == -1) {
                            Ostrov.log_err("decode sels 1st split error -> "+sl);
                            continue;
                        }
                        final int amt = NumUtil.intOf(sl.substring(0, eqn1), 0);
                        if (amt == 0) {
                            Ostrov.log_err("decode sels num is 0 -> "+sl);
                            continue;
                        }
                        final String sss = sl.substring(eqn1 + 1);
                        final int eqn2 = sss.indexOf(eq);
                        if (eqn2 == -1) {
                            Ostrov.log_err("decode sels 2nd split error -> "+sss);
                            continue;
                        }
                        final Selector s = Selector.VALUES.get(sss.substring(0, eqn2));
                        if (s == null) {
                            Ostrov.log_err("decode sels id -> "+sl);
                            continue;
                        }
                        sv.sels.put(new Selector.SelState(s,
                            NumUtil.intOf(sss.substring(eqn2 + 1), 0)), amt);
                    }
                    sv.sels.put(Selector.SAME_ST, 1);
                    sv.sels.put(Selector.CASTER_ST, 1);
                    break;
                case "abils":
                    sv.abils.clear();
                    for (final String ab : en.getValue().split(StringUtil.SPLIT_0)) {
                        final int eqn1 = ab.indexOf(eq);
                        if (eqn1 == -1) {
                            Ostrov.log_err("decode abils 1st split error -> "+ab);
                            continue;
                        }
                        final int amt = NumUtil.intOf(ab.substring(0, eqn1), 0);
                        if (amt == 0) {
                            Ostrov.log_err("decode abils num is 0 -> "+ab);
                            continue;
                        }
                        final String ass = ab.substring(eqn1 + 1);
                        final int eqn2 = ass.indexOf(eq);
                        if (eqn2 == -1) {
                            Ostrov.log_err("decode abils 2nd split error -> "+ass);
                            continue;
                        }
                        final Ability a = Ability.VALUES.get(ass.substring(0, eqn2));
                        if (a == null) {
                            Ostrov.log_err("decode abils id -> "+ab);
                            continue;
                        }
                        sv.abils.put(new Ability.AbilState(a,
                            NumUtil.intOf(ass.substring(eqn2 + 1), 0)), amt);
                    }
                    break;
                case "mods":
                    sv.mods.clear();
                    for (final String md : en.getValue().split(StringUtil.SPLIT_0)) {
                        final int eqn1 = md.indexOf(eq);
                        if (eqn1 == -1) {
                            Ostrov.log_err("decode mods 1st split error -> "+md);
                            continue;
                        }
                        final int amt = NumUtil.intOf(md.substring(0, eqn1), 0);
                        if (amt == 0) {
                            Ostrov.log_err("decode mods num is 0 -> "+md);
                            continue;
                        }
                        final String mss = md.substring(eqn1 + 1);
                        final int eqn2 = mss.indexOf(eq);
                        if (eqn2 == -1) {
                            Ostrov.log_err("decode mods 2nd split error -> "+mss);
                            continue;
                        }
                        final Modifier m = Modifier.VALUES.get(mss.substring(0, eqn2));
                        if (m == null) {
                            Ostrov.log_err("decode mods id -> "+md);
                            continue;
                        }
                        sv.mods.put(new Modifier.ModState(m,
                            NumUtil.intOf(mss.substring(eqn2 + 1), 0)), amt);
                    }
                    break;
                /*case "unread":
                    sv.unread.clear();
                    for (final String md : en.getValue().split(StringUtil.SPLIT_0)) {
                        if (md.isEmpty()) continue;
                        final Entry uren = Entry.get(md.charAt(0));
                        if (uren == null) {
                            Ostrov.log_err("no entry for char-> "+md.charAt(0));
                            continue;
                        }
                        sv.unread.add(uren);
                    }
                    break;*/
                case "data":
                    for (final String dt : en.getValue().split(StringUtil.SPLIT_0)) {
                        final int eqn = dt.indexOf(eq);
                        if (eqn == -1) {
                            Ostrov.log_err("decode data error -> "+dt);
                            continue;
                        }

                        switch (dt.substring(0, eqn)) {
                            case "exp":
                                sv.setXp(p, NumUtil.intOf(dt.substring(eqn + 1), 0));
                                break;
                            case "mana":
                                sv.setMana(p, NumUtil.intOf(dt.substring(eqn + 1), 0));
                                break;
                            case "statPoints":
                                sv.statsPoints = NumUtil.intOf(dt.substring(eqn + 1), 0);
                                break;
                            case "worldOpen":
                                sv.worldOpen = NumUtil.intOf(dt.substring(eqn + 1), 0);
                                break;
                            case "roleStamp":
                                sv.roleStamp = NumUtil.intOf(dt.substring(eqn + 1), 0);
                                break;
                            case "board":
                                sv.showScoreBoard = Boolean.parseBoolean(dt.substring(eqn + 1));
                                break;
                            case "acBar":
                                sv.showActionBar = Boolean.parseBoolean(dt.substring(eqn + 1));
                                break;
                            default:
                                Ostrov.log_warn("decode data nonexist -> "+dt);
                                break;
                        }
                    }
                    break;
            }
        }

        sv.recalcStats(p);
//        p.setHealth(fstHp < sv.maxHP ? fstHp : sv.maxHP);
        if (!sv.isWorldOpen(SubServer.WASTES)) {
            sv.unlockWorld(SubServer.WASTES);
        }

        if (Main.subServer==SubServer.WASTES) {
        	final String lac = e.getData().get("lastActivity");
        	if (lac != null && Timer.secTime() - Integer.parseInt(lac) > 2) {
                final String lgLc = sv.world_positions.get("logoutLoc");
                final XYZ svl = lgLc == null ? null : XYZ.fromString(lgLc.replace(':', ','));
                if (svl != null && !svl.worldName.equals(p.getWorld().getName())) {
    				final SubServer ss = SubServer.parse(svl.worldName);
    				if (ss != null) {
    					WorldMenu.moveTo(p, ss, false);
    					return;
    				}
                }
        	}
            p.setPlayerTime(6000l, true);
        }

        sv.setData(Data.FRIEND_JUMP_INFO, "Для ТП нужно быть в мире " + Main.subServer.disName);
        
    }
    
    private void hasNoSkill(final Player p, final Survivor sv) {
//        sv.showActionBar = true; //у новичков вкл. по умолчанию
//        sv.showScoreBoard = true;
        Entries.WASTES.complete(p, sv, true);
        sv.setData(Data.FRIEND_JUMP_INFO, "Для ТП нужно быть в мире " + Main.subServer.disName);
        sv.unlockWorld(SubServer.WASTES);
        sv.recalcStats(p);
    }



    @EventHandler (priority = EventPriority.LOW)
    public void onFriendTp (final FriendTeleportEvent e) {
        //обработка нужна только в пределах одного мира
        //между серверами банжик не даст перейти в другой мир с помощью PM.getOplayer(p).setData(Data.FRIEND_JUMP_INFO)
//        final Survivor svFrom = SM.getSurvivor(e.source);
        e.setCanceled(true, Main.prefix + "Друзья не могут ТП здесь!");
        /*if (svFrom!=null && svFrom.currentPlyTime<600) {
            e.setCanceled(true, Main.prefix + "Друзья могут ТП после 10 минут выживания!");
            return;
        }
        final Survivor svTo = SM.getSurvivor(e.target);
        if (svTo!=null && svTo.currentPlyTime<600) {
            e.setCanceled(true, Main.prefix + "Друзья могут ТП после 10 минут выживания!");
            //return;
        }*/
    	//e.setCanceled(true, Main.prefix + "Тут телепорт отключен!");
//Ostrov.log(e.Is_canceled() + "");
    }
}
