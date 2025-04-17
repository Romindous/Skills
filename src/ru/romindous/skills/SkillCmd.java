package ru.romindous.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Timer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.ConfirmationGUI;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.skills.menus.MainMenu;
import ru.romindous.skills.menus.RoleMenu;
import ru.romindous.skills.menus.WorldMenu;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.survs.SM;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.survs.Survivor;


public class SkillCmd implements CommandExecutor, TabCompleter {

    private static final List <String> subCmd;

    static {
        subCmd = Arrays.asList("select", "menu", "world");
    }



    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] arg) {

        if (arg.length==1) return subCmd;

        final List <String> sugg = new ArrayList<>();
        switch (arg.length) {
            case 2:
                switch (arg[0]) {
                    case "select":
                        for (final Role rl : Role.values()) {
                            sugg.add(rl.name().toLowerCase(Locale.ROOT));
                        }
                        break;
                    case "give":
                        if (ApiOstrov.isLocalBuilder(cs)) {
                            sugg.add("xp");
                            sugg.add("souls");
                            sugg.add("stat");
                            sugg.add("sel");
                            sugg.add("abil");
                            sugg.add("mod");
                        }
                        break;
                    case "world":
                        if (ApiOstrov.isLocalBuilder(cs)) {
                            for (final SubServer ss : SubServer.values()) {
                                sugg.add(ss.name());
                            }
                        }
                        break;
                }
                break;
            case 3:
                if (arg[0].equalsIgnoreCase("give")) {
                    switch (arg[1].toLowerCase()) {
                        case "sel":
                            sugg.addAll(Selector.VALUES.keySet());
                            break;
                        case "abil":
                            sugg.addAll(Ability.VALUES.keySet());
                            break;
                        case "mod":
                            sugg.addAll(Modifier.VALUES.keySet());
                            break;
                        default:
                            sugg.add("10");
                            sugg.add("100");
                            sugg.add("1000");
                            break;
                    }
                }
                break;
            case 4:
                if (arg[0].equalsIgnoreCase("give")) {
                    if (cs instanceof ConsoleCommandSender) {
                        sugg.addAll(PM.getOplayersNames());
                    }
                }
                break;
        }
        return sugg;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] arg) {

        final Player p = cs instanceof Player ? (Player) cs : null;
        final Survivor sv = p==null ? null : PM.getOplayer(p, Survivor.class);

        if (p!=null && (sv==null || sv.dbError != null) ) { //командует игрок, но его данные не загружены - отказ
            p.sendMessage(TCUtil.form("§cДанные не были загружены, команда отключена!"));
            return true;
        }

        //обработка команд, возможных от консоли и билдера

        if (arg.length>=1) switch (arg[0]) {

            case "give" -> {
                if (!ApiOstrov.isLocalBuilder(cs, true)) {
                    return false;
                }
                if (arg.length<3) {
                    cs.sendMessage("§c/skill add xp|stat|souls/val|val|val ammount/scroll [ник]");
                    return true;
                }
                final Player pl;
                final Survivor srv;
                if (arg.length<4) {
                    if (cs instanceof ConsoleCommandSender) {
                        cs.sendMessage("§cНужно указать ник!");
                        return true;
                    }
                    pl = p;
                    srv = sv;
                } else {
                    pl = Bukkit.getPlayer(arg[3]);
                    if (pl==null) {
                        cs.sendMessage("§cИгрока "+arg[3]+" нет на сервере!");
                        return true;
                    }
                    srv = PM.getOplayer(pl, Survivor.class);
                }

                if (srv==null) {
                    cs.sendMessage("§cSurvivor==null!");
                    return true;
                }
                if (srv.dbError != null) {
                    cs.sendMessage("§cОшибка загрузки данных игрока");
                    return true;
                }
                final int amt;
                final Scroll sc;
                switch (arg[1].toLowerCase()) {
                    case "xp":
                        amt = NumUtil.intOf(arg[2], 1);
                        srv.addXp(pl, amt);
                        cs.sendMessage("§aДобавлено §b"+amt+" §aк §b"+arg[1]);
                        return true;
                    case "souls":
                        amt = NumUtil.intOf(arg[2], 1);
                        srv.chgMana(pl, amt);
                        cs.sendMessage("§aДобавлено §b"+amt+" §aк §b"+arg[1]);
                        return true;
                    case "stat":
                        amt = NumUtil.intOf(arg[2], 1);
                        srv.statsPoints += amt;
                        cs.sendMessage("§aДобавлено §b"+amt+" §aк §b"+arg[1]);
                        return true;
                    case "sel":
                        sc = Selector.VALUES.get(arg[2].toLowerCase());
                        if (sc == null) {
                            cs.sendMessage("§cНет свитка с названием " + arg[2]);
                            return false;
                        }
                        pl.getWorld().dropItem(EntityUtil.center(pl), sc.drop(0));
                        cs.sendMessage("§aВыдан свиток §b"+sc.id()+" §aк §b"+pl.getName());
                        return true;
                    case "abil":
                        sc = Ability.VALUES.get(arg[2].toLowerCase());
                        if (sc == null) {
                            cs.sendMessage("§cНет свитка с названием " + arg[2]);
                            return false;
                        }
                        pl.getWorld().dropItem(EntityUtil.center(pl), sc.drop(0));
                        cs.sendMessage("§aВыдан свиток §b"+sc.id()+" §aк §b"+pl.getName());
                        return true;
                    case "mod":
                        sc = Modifier.VALUES.get(arg[2].toLowerCase());
                        if (sc == null) {
                            cs.sendMessage("§cНет свитка с названием " + arg[2]);
                            return false;
                        }
                        pl.getWorld().dropItem(EntityUtil.center(pl), sc.drop(0));
                        cs.sendMessage("§aВыдан свиток §b"+sc.id()+" §aк §b"+pl.getName());
                        return true;
                    default:
                        cs.sendMessage("§c"+arg[1]+" - нет такого параметра");
                        return false;
                }
            }

            case "reset" -> {
                if (!ApiOstrov.isLocalBuilder(cs, true)) {
                    return false;
                }
                final Player pl;
                final Survivor srv;
                if (arg.length<2) {
                    if (p == null) {
                        cs.sendMessage("§cНужно указать ник!");
                        return false;
                    }
                    pl = p;
                    srv = sv;
                } else {
                    pl = Bukkit.getPlayer(arg[1]);
                    if (pl==null) {
                        cs.sendMessage("§cИгрока "+arg[1]+" нет на сервере!");
                        return false;
                    }
                    srv = PM.getOplayer(pl, Survivor.class);
                }
                if (srv==null) {
                    cs.sendMessage("§cSurvivor==null!");
                    return false;
                }

                if (p == null) {
                    SM.resetPlayer(cs, pl);
                    return true;
                }
                ConfirmationGUI.open(p, "Сбросить прогресс " + pl.getName() + "?", b -> {
                    if (b) SM.resetPlayer(cs, pl);
                });
                return true;
            }

        }

        //ниже только команды игрока

        if (p == null) {
            cs.sendMessage("§c"+arg[0]+" с консоли не работает");
            return true;
        }

        if (arg.length==0) return false;
        switch (arg[0]) {
            case "menu" -> {
                if (sv.role ==null) {
                    RoleMenu.skillSelect.open(p);
                } else {
//p.sendMessage("открыть меню соотв.классу");
                    SmartInventory.builder()
                        .id("Menu"+p.getName())
                        .provider(new MainMenu())
                        .size(6, 9)
                        .title("          §c§lГлавное Меню")
                        .build()
                        .open(p);
                }
                return true;
            }
            case "world" -> {
                if (PM.inBattle(p.getName())) {
                    p.sendMessage(Main.prefix + "§cВы не можете сменить мир во время битвы!");
                    return true;
                }
                if (arg.length==2) {
                    final SubServer ss = SubServer.parse(arg[1]);
                    if (!ApiOstrov.isLocalBuilder(cs)) {
                        p.sendMessage(Main.prefix + "§cМенять мир командой могут только билдеры!");
                        return true;
                    }

                    if (ss==null) {
                        p.sendMessage(Main.prefix + "§cНет мира "+arg[1]+"!");
                        return true;
                    }

                    WorldMenu.moveTo(p, ss, false);
                } else {
                    SmartInventory.builder()
                        .id("World " + p.getName())
                        .provider(new WorldMenu())
                        .size(1, 9)
                        .title("           §6§lВыбери Мир")
                        .build()
                        .open(p);
                }
                return true;
            }

            case "select" -> {
                if (arg.length==1) {
                    RoleMenu.skillSelect.open(p);  //меню должно парсить команду /skill select <скилл>
                } else if (arg.length==2) {
                    final Role role = Role.get(arg[1]);
                    if (role==null) {
                        p.sendMessage(TCUtil.form(Main.prefix + "§cНет класса "+arg[1]+"!"));
                        return true;
                    }
                    if (sv.role == role) {
                        p.sendMessage(TCUtil.form(Main.prefix + "§cТы и так "+role.disName()+TCUtil.N+"!"));
                        return true;
                    }
                    if (sv.role !=null) {
                        if (ApiOstrov.isLocalBuilder(p)) {
                            p.sendMessage("§e*билдер-смена без задержки");
                        } else {
                            final int timeLeft = Math.max(0, 86400 - (Timer.secTime()-sv.roleStamp));
                            if (timeLeft>0){
                                p.sendMessage(Main.prefix + "Смена возможна через §4" + TimeUtil.secondToTime(timeLeft));
                                return true;
                            }
                        }

                        if (Perm.isRank(sv, 1)) {
                            for (final Stat st : Stat.values()) {
                                sv.statsPoints += sv.getStat(st);
                                sv.setStat(st, 0);
                            }
                            p.sendMessage(TCUtil.form(Main.prefix + "Очки статы возмещены,"));
                            p.sendMessage(TCUtil.form(TCUtil.N + "можешь распределить их обратно!"));
                        } else {
                            for (final Stat st : Stat.values()) {
                                sv.setStat(st, 0);
                            }
                            sv.setXp(p, 0);
                            p.sendMessage(TCUtil.form(Main.prefix + "Очки статы анулированы,"));
                            p.sendMessage(TCUtil.form(TCUtil.N + "и твой уровень упал до 0!"));
                        }
                    }

                    sv.roleStamp = Timer.secTime();
                    sv.showScoreBoard = true;
                    sv.showActionBar = true;
                    sv.role = role;
                    for (final Skill sk : sv.skills) {
                        for (final Ability.AbilState as : sk.abils) sv.change(as, 1);
                        for (final Modifier.ModState ms : sk.mods) sv.change(ms, 1);
                        for (final Selector.SelState ss : sk.sels) sv.change(ss, 1);
                    }
                    sv.skills.clear();
                    sv.sels.entrySet().removeIf(en -> {
                        final Selector.SelState ss = en.getKey();
                        final boolean keep = sv.canUse(ss.val());
                        if (keep) return false;
                        final ItemStack it = ss.val().drop(ss.lvl());
                        it.setAmount(en.getValue());
                        ItemUtil.giveItemsTo(p, it);
                        return true;
                    });
                    sv.abils.entrySet().removeIf(en -> {
                        final Ability.AbilState as = en.getKey();
                        final boolean keep = sv.canUse(as.val());
                        if (keep) return false;
                        final ItemStack it = as.val().drop(as.lvl());
                        it.setAmount(en.getValue());
                        ItemUtil.giveItemsTo(p, it);
                        return true;
                    });
                    sv.mods.entrySet().removeIf(en -> {
                        final Modifier.ModState ms = en.getKey();
                        final boolean keep = sv.canUse(ms.val());
                        if (keep) return false;
                        final ItemStack it = ms.val().drop(ms.lvl());
                        it.setAmount(en.getValue());
                        ItemUtil.giveItemsTo(p, it);
                        return true;
                    });
//                    QM.tryCompleteQuest(p, Quest.ClassChoose, 1, true);
                    p.sendMessage(TCUtil.form(Main.prefix + "Теперь ты - " + role.disName()));
                    ScreenUtil.sendTitle(p, TCUtil.N + "Теперь ты - " + role.disName(), " ");
                    sv.recalcStats(p);
//                    sv.setBarName("§7Класс: "+role.getName()+"§7, Уровень: "+role.stat.color()+sv.getLevel());
//                    sv.setBarProgress(sv.nextLevelScale());
//                    sv.showBossBar(p, 10);
                }
                return true;
            }
        }
        return true;
    }
}
