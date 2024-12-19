package ru.romindous.skills.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Data;
import ru.komiss77.events.FriendTeleportEvent;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.MoveUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.SM;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.enums.Role;
import ru.romindous.skills.enums.Stat;
import ru.romindous.skills.enums.SubServer;
import ru.romindous.skills.enums.Trigger;
import ru.romindous.skills.menus.WorldMenu;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;


public class MySqlLst implements Listener {

    public static final String split = "∬";
    public static final String and = "&";
    public static final String lvl = "#";
    public static final String eq = "=";

    @EventHandler (priority = EventPriority.MONITOR)
    public void onJoin (final PlayerJoinEvent e) {
        Ostrov.sync(() -> {
            final Player pl = e.getPlayer();
            final Oplayer op = PM.getOplayer(pl);
            op.firstJoin = true;
            new LocalDataLoadEvent(pl, op, pl.getLocation()).callEvent();
        }, 20);
    }
    
    @EventHandler (priority = EventPriority.MONITOR)
    public void onDataLoad (final LocalDataLoadEvent e) {
        final Player p = e.getPlayer();
        final Survivor sv = (Survivor) e.getOplayer();
//        int fstHp = 1;
        
        if (sv.mysqlError) {
            Ostrov.log_err(p.getName()+":LocalDataLoadEvent-hasSqlError!");
            p.sendMessage("§cОшибка загрузки скилл, при выходе данные не будут сохраняться!");
            sv.setBarName("§8Ошибка загрузки");
            sv.setBarProgress(0);
            sv.setBarColor(Color.RED);
            sv.showBossBar(p, 20);
            return;
        }

        final Location save = LocUtil.stringToLoc(sv.world_positions.get(p.getWorld().getName()), false, true);
        
        if (save == null) {
            SM.randomJoin(p, sv.firstJoin);
        } else {
            MoveUtil.safeTP(p, save);//p.teleport(save);
        }

        Main.diary.give(p);

        if (sv.firstJoin) {
            hasNoSkill(p, sv);
            return;
        }

        for (final Entry<String, String> en : e.getData().entrySet()) {
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
                    for (final String s : en.getValue().split(split)) {
                        if (!s.startsWith(wnm)) continue;
                        p.setRespawnLocation(XYZ.fromString(s)
                            .getCenterLoc(p.getWorld()), false);
                    }
                    break;
                case "stats":
                    for (final String s : en.getValue().split(split)) {
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
                    for (final String sk : en.getValue().split(split)) {
                        final String[] skl = sk.split(eq);
                        final List<Selector.SelState> sels = new ArrayList<>();
                        final List<Ability.AbilState> abils = new ArrayList<>();
                        final List<Modifier.ModState> mods = new ArrayList<>();
                        Trigger trig = Trigger.UNKNOWN;
                        int cd = 0;
                        final Skill fsk;
                        switch (skl.length) {
                            default:
                                cd = NumUtil.intOf(skl[5], 0);
                            case 5:
                                for (final String md : skl[4].split(and)) {
                                    final int eqn = md.indexOf(lvl);
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
                            case 4:
                                for (final String md : skl[3].split(and)) {
                                    final int eqn = md.indexOf(lvl);
                                    if (eqn == -1) {
                                        Ostrov.log_err("decode skill mods error -> "+md);
                                        continue;
                                    }
                                    final Modifier m = Modifier.VALUES.get(md.substring(0, eqn));
                                    if (m == null) {
                                        Ostrov.log_err("decode skill mods id -> "+md);
                                        continue;
                                    }
                                    mods.add(new Modifier.ModState(m,
                                        NumUtil.intOf(md.substring(eqn + 1), 0)));
                                }
                            case 3:
                                for (final String ab : skl[2].split(and)) {
                                    final int eqn = ab.indexOf(lvl);
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
                            case 2:
                                trig = Trigger.get(skl[1]);
                            case 1:
                                fsk = new Skill(skl[0], trig, sels.toArray(new Selector.SelState[0]),
                                    abils.toArray(new Ability.AbilState[0]), mods.toArray(new Modifier.ModState[0]), sv.mana);
                                fsk.setCoolDown(cd);
                                sv.skills.add(fsk);
                                break;
                            case 0:
                        }
                        /*final int eqn = sk.indexOf(eq);
                        if (eqn == -1) {
                            Ostrov.log_err("decode abils error -> "+sk);
                            continue;
                        }
                        final Ability a = Ability.VALUES.get(sk.substring(0, eqn));
                        if (a == null) {
                            Ostrov.log_err("decode abils id -> "+sk);
                            continue;
                        }
                        sv.abils.add(new Ability.AbilState(a,
                                NumUtil.intOf(sk.substring(eqn + 1), 0)));*/
                    }
                    break;
                case "sels":
                    sv.sels.clear();
                    for (final String md : en.getValue().split(split)) {
                        final int eqn1 = md.indexOf(eq);
                        if (eqn1 == -1) {
                            Ostrov.log_err("decode sels 1st split error -> "+md);
                            continue;
                        }
                        final int amt = NumUtil.intOf(md.substring(0, eqn1), 0);
                        if (amt == 0) {
                            Ostrov.log_err("decode sels num is 0 -> "+md);
                            continue;
                        }
                        final String sss = md.substring(eqn1 + 1);
                        final int eqn2 = sss.indexOf(eq);
                        if (eqn2 == -1) {
                            Ostrov.log_err("decode sels 2nd split error -> "+sss);
                            continue;
                        }
                        final Selector s = Selector.VALUES.get(sss.substring(0, eqn2));
                        if (s == null) {
                            Ostrov.log_err("decode sels id -> "+md);
                            continue;
                        }
                        sv.sels.put(new Selector.SelState(s,
                            NumUtil.intOf(md.substring(eqn2 + 1), 0)), amt);
                    }
                    break;
                case "abils":
                    sv.abils.clear();
                    for (final String ab : en.getValue().split(split)) {
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
                            NumUtil.intOf(ab.substring(eqn2 + 1), 0)), amt);
                    }
                    break;
                case "mods":
                    sv.mods.clear();
                    for (final String md : en.getValue().split(split)) {
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
                            NumUtil.intOf(md.substring(eqn2 + 1), 0)), amt);
                    }
                    break;
                case "data":
                    for (final String dt : en.getValue().split(split)) {
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
                            case "mobKills":
                                sv.mobKills = NumUtil.intOf(dt.substring(eqn + 1), 0);
                                break;
                            case "deaths":
                                sv.deaths = NumUtil.intOf(dt.substring(eqn + 1), 0);
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

        //splits = e.getData().get("ability").splits(split);
        /*Ability ab;
        String abiltyRaw = "";

        if (e.getData().containsKey("ab")) {
            abiltyRaw = e.getData().get("ab");
            if (abiltyRaw.length()%2==0) {
                for (int idx =0; idx<abiltyRaw.length(); idx+=2) {
                    ab = Ability.getByChar(abiltyRaw.charAt(idx));
                    if (ab==null) {
                        Ostrov.log_err("decode skill data ability: Ability==null -> "+abiltyRaw.charAt(idx));
                        continue;
                    }
                    try {
                        value = Character.getNumericValue(abiltyRaw.charAt(idx+1));
                    } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException ex) {
                        Ostrov.log_err("decode skill data ability: Ability value wrong -> idx="+(idx+1)+","+abiltyRaw);
                        continue;
                    }
                    sv.abilityInfo.put(ab, new AbInfo(ab, sv, value));
                }
            } else {
                Ostrov.log_err("decode skill data: Ability length()%2!=0 -> "+abiltyRaw);
            }
        }

        //final int currTime = ApiOstrov.currentTimeSec();
        if (e.getData().containsKey("abStamp")) {
            splits = e.getData().get("abStamp").split(split);
            for (final String s : splits) {
                if (s.length()<2) continue;
                ab = Ability.getByChar(abiltyRaw.charAt(0));
                if (ab==null) {
                    Ostrov.log_err("decode skill data abilityStamp: Ability==null -> "+s);
                    continue;
                }
                if (sv.abilityInfo.containsKey(ab)) {
                    try {
                        value = Integer.parseInt(s.substring(1));
                    } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException ex) {
                        Ostrov.log_err("decode skill data abilityStamp: Stats value wrong -> "+s);
                        continue;
                    }
                    //if (currTime-value>ab.recarge) { //еще перезарядка
                    sv.abilityInfo.get(ab).useStamp = value;
                    //}
                }
            }
        }

        if (e.getData().containsKey("abOn")) {
            for (char c : e.getData().get("abOn").toCharArray()) {
                ab = Ability.getByChar(c);
                if (ab==null) {
                    Ostrov.log_err("decode skill data abilityOn: Ability==null -> "+e.getData().get("abilityOn"));
                    continue;
                }
                if (sv.abilityInfo.containsKey(ab)) {
                    sv.abilityInfo.get(ab).on = true;
                }
            }
        }

        if (e.getData().containsKey("quests")) {
            splits = e.getData().get("quests").split(split);
            for (final String s : splits) {
                if (s.length() > 1) {
                    sv.questInfo.put(Quest.byCode(s.charAt(0)), Integer.parseInt(s.substring(1)));
                }
            }
        }*/

        sv.recalcStats(p);
//        p.setHealth(fstHp < sv.maxHP ? fstHp : sv.maxHP);
        if (!sv.isWorldOpen(SubServer.WASTES)) {
            sv.unlockWorld(SubServer.WASTES);
        }

        if (Main.subServer==SubServer.WASTES) {
        	final String lac = e.getData().get("lastActivity");
        	if (lac != null && Timer.getTime() - Integer.parseInt(lac) > 2) {
                final String lgLc = sv.world_positions.get("logoutLoc");
                final XYZ svl = lgLc == null ? null : XYZ.fromString(lgLc.replace(':', ','));
                if (svl != null && !svl.worldName.equals(p.getWorld().getName())) {
    				final SubServer ss = SubServer.parseSubServer(svl.worldName);
    				if (ss != null) {
    					WorldMenu.moveTo(p, ss, false);
    					return;
    				}
                }
        	}
            p.setPlayerTime(6000l, true);
        }

        sv.setData(Data.FRIEND_JUMP_INFO, "Для ТП на седну нужно быть в мире "+Main.subServer.displayName);
        
    }
    
    private void hasNoSkill(final Player p, final Survivor sv) {
        ScreenUtil.sendBossbar(p, "", 4, Color.WHITE, BossBar.Overlay.NOTCHED_12);
        sv.showActionBar = true; //у новичков вкл. по умолчанию
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
