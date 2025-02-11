package ru.romindous.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.ConfirmationGUI;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.survs.SM;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.menus.MainMenu;
import ru.romindous.skills.menus.RoleSelectMenu;
import ru.romindous.skills.menus.StatsMenu;
import ru.romindous.skills.menus.WorldMenu;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.survs.Survivor;


public class SkillCmd implements CommandExecutor, TabCompleter {

    private static final List <String> subCmd;

    static {
        subCmd = Arrays.asList("select", "menu", "stats", "ability", "world", "give", "add", "reset", "debug", "pet");
    }



    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] arg) {

        if (arg.length==1) return subCmd;

        final List <String> sugg = new ArrayList<>();
//System.out.println("l="+strings.length+" 0="+strings[0]);
        switch (arg.length) {

            //case 1:
            //0- пустой (то,что уже введено)
            //return subCmd;
            //for (Player p : Bukkit.getOnlinePlayers()) {
            //    if (p.getName().startsWith(arg[0])) sugg.add(p.getName());
            //}
            //break;

            case 2:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
//                final String arg1 = arg[1].toUpperCase();
                switch (arg[0]) {
                    case "select":
                        for (final Role rl : Role.values()) {
                            sugg.add(rl.name().toLowerCase(Locale.ROOT));
                        }
                        break;
                    case "add":
                        if (ApiOstrov.isLocalBuilder(cs)) {
                            sugg.add("xp");
                            sugg.add("souls");
                            sugg.add("stat");
                        }
                        break;
                    case "give":
                        if (ApiOstrov.isLocalBuilder(cs)) {
                            sugg.add("sel");
                            sugg.add("abil");
                            sugg.add("mod");
                        }
                        break;
                    case "reset":
                        break;
                    case "world":
                        if (ApiOstrov.isLocalBuilder(cs)) {
                            for (final SubServer ss : SubServer.values()) {
                                sugg.add(ss.name());
                            }
                        }
                        break;
                }
                //if (strings[0].equalsIgnoreCase("build") || strings[0].equalsIgnoreCase("destroy") ) {
                //for (final String s : argList) {
                //    if (s.startsWith(arg[0])) sugg.add(s);
                //}
                //sugg.add("читы");
                //sugg.add("гриф");
                //sugg.add("неадекват");
                //}
                break;


            case 3:
                //2-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                switch (arg[0].toLowerCase()) {
                    case "add":
                        sugg.add("5");
                        sugg.add("10");
                        sugg.add("100");
                        sugg.add("1000");
                        break;
                    case "give":
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
                        }
                        break;
                }
                break;

            case 4:
                //3-то,что вводится (обновляется после каждой буквы
                switch (arg[0].toLowerCase()) {
                    case "add":
                        if (cs instanceof ConsoleCommandSender) {
                            sugg.addAll(PM.getOplayersNames());
                        }
                        break;
                    case "give":
                        sugg.add("1");
                        break;
                }
                break;
        }

        return sugg;
    }










    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] arg) {

        final Player p = cs instanceof Player ? (Player) cs : null;
        final Survivor sv = p==null ? null : PM.getOplayer(p, Survivor.class);

        if (p!=null && (sv==null || sv.mysqlError) ) { //командует игрок, но его данные не загружены - отказ
            p.sendMessage(TCUtil.form("§cДанные не были загружены, команда отключена!"));
            return true;
        }






        //обработка команд, возможных от консоли и билдера

        if (arg.length>=1) switch (arg[0]) {

            case "add" -> {
                if (!ApiOstrov.isLocalBuilder(cs, true)) {
                    return true;
                }
                if (arg.length<3) {
                    cs.sendMessage("§c/skill add xp <ammount> [ник]");
                    return true;
                }
                if (cs instanceof ConsoleCommandSender && arg.length<4) {
                    cs.sendMessage("§cНужно указать ник!");
                    return true;
                }
                final Player pl;
                final Survivor srv;
                if (arg.length==4) {
                    pl = Bukkit.getPlayer(arg[3]);
                    if (pl==null) {
                        cs.sendMessage("§cИгрока "+arg[3]+" нет на сервере!");
                        return true;
                    }
                    srv = PM.getOplayer(pl, Survivor.class);
                } else {
                    pl = p;
                    srv = sv;
                }
                if (srv==null) {
                    cs.sendMessage("§c"+arg[3]+": Survivor==null!");
                    return true;
                }
                if (srv.mysqlError) {
                    cs.sendMessage("§cОшибка загрузки данных игрока "+arg[3]+" : loadError");
                    return true;
                }
                final int ammount = NumUtil.intOf(arg[2], 1);
                if (ammount<1 || ammount>10000) {
                    cs.sendMessage("§cammount от 1 до 10000");
                    return true;
                }
                switch (arg[1].toLowerCase()) {
                    case "xp":
                        srv.addXp(pl, ammount);
                        break;
                    case "souls":
                        srv.chgMana(pl, ammount);
                        break;
                    case "stat":
                        srv.statsPoints +=ammount;
                        break;
                    default:
                        cs.sendMessage("§c"+arg[1]+" - нет такого параметра");
                        return true;
                }
                cs.sendMessage("§aДобавлено §b"+ammount+" §aк §b"+arg[1]);
                return true;
            }

            case "give" -> {
                if (!ApiOstrov.isLocalBuilder(cs, true)) {
                    return true;
                }
                if (arg.length<4) {
                    cs.sendMessage("§c/skill give <sel|abil|mod> <name> <lvl>");
                    return true;
                }
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§cНе консольная комманда!");
                    return true;
                }
                final Survivor srv = PM.getOplayer(pl, Survivor.class);
                if (srv==null) {
                    cs.sendMessage("§c"+arg[3]+": Survivor==null!");
                    return true;
                }
                if (srv.mysqlError) {
                    cs.sendMessage("§cОшибка загрузки данных игрока "+arg[3]+" : loadError");
                    return true;
                }
                final Scroll sc;
                switch (arg[1].toLowerCase()) {
                    case "sel":
                        sc = Selector.VALUES.get(arg[2].toLowerCase());
                        break;
                    case "abil":
                        sc = Ability.VALUES.get(arg[2].toLowerCase());
                        break;
                    case "mod":
                        sc = Modifier.VALUES.get(arg[2].toLowerCase());
                        break;
                    default:
                        cs.sendMessage("§c"+arg[1]+" - нет такого параметра");
                        return true;
                }
                if (sc == null) {
                    cs.sendMessage("§cНет свитка с названием " + arg[2]);
                    return true;
                }
                pl.getWorld().dropItem(EntityUtil.center(pl), sc.drop(NumUtil.intOf(arg[3], 1) - 1));
                cs.sendMessage("§aВыдан свиток §b"+sc.id()+" §aк §b"+pl.getName());
                return true;
            }

            case "reset" -> {
               /*if (!ApiOstrov.isLocalBuilder(cs, true)) {
                   //return true;
               }*/
                if (cs instanceof ConsoleCommandSender && arg.length<2) {
                    cs.sendMessage("§cНужно указать ник!");
                    return true;
                }
                final Player pl;
                final Survivor srv;
                if (arg.length==2) {
                    pl = Bukkit.getPlayer(arg[1]);
                    if (pl==null) {
                        cs.sendMessage("§cИгрока "+arg[1]+" нет на сервере!");
                        return true;
                    }
                    srv = PM.getOplayer(pl, Survivor.class);
                } else {
                    pl = p;
                    srv = sv;
                }
                if (srv==null) {
                    cs.sendMessage("§c"+arg[1]+": Survivor==null!");
                    return true;
                }
               /*if (srv.mysqlError) {
                   cs.sendMessage("§cОшибка загрузки данных игрока "+arg[1]+" : loadError");
                   return true;
               }*/
                if (p != null && p.getEntityId() == pl.getEntityId()) {
                    ConfirmationGUI.open(p, "Весь ваш прогресс сбросится?", b -> {
                        if (b) SM.resetPlayer(cs, pl);
                    });
                } else if (ApiOstrov.isLocalBuilder(cs, true)) {
                    if (p == null) {
                        SM.resetPlayer(cs, pl);
                    } else {
                        ConfirmationGUI.open(p, "Сбросить прогресс " + pl.getName() + "?", b -> {
                            if (b) SM.resetPlayer(cs, pl);
                        });
                    }
                }
                return true;
            }

        }








        //ниже только команды игрока

        if (p == null) {
            cs.sendMessage("§c"+arg[0]+" с консоли не работает");
            return true;
        }


        if (arg.length==0 || arg[0].equalsIgnoreCase("menu")) {
//p.sendMessage("open main menu");
            if (sv.role ==null) {
                RoleSelectMenu.skillSelect.open(p);
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







        switch (arg[0]) {


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
                    RoleSelectMenu.skillSelect.open(p);  //меню должно парсить команду /skill select <скилл>
                } else if (arg.length==2) {
                    final Role role = Role.get(arg[1]);
                    if (role==null) {
                        p.sendMessage(Main.prefix + "§cНет класса "+arg[1]+"!");
                        return true;
                    }
                    if (sv.role == role) {
                        p.sendMessage(Main.prefix + "§cТы и так "+arg[1]+"!");
                        return true;
                    }
                    if (sv.role !=null) {
                        if (ApiOstrov.isLocalBuilder(p)) {
                            p.sendMessage("§e*билдер-смена без задержки");
                        } else {
                            final int timeLeft = Math.max(0, 86400 - (ApiOstrov.currentTimeSec()-sv.roleStamp));
                            if (timeLeft>0){
                                p.sendMessage(Main.prefix + "Смена возможна через §4" + TimeUtil.secondToTime(timeLeft));
                                return true;
                            }
                        }

                        for (final Stat st : Stat.values()) {
                            sv.statsPoints += sv.getStat(st);
                            sv.setStat(st, 0);
                        }
                    }

                    sv.roleStamp = ApiOstrov.currentTimeSec();
                    sv.showScoreBoard = true;
                    sv.showActionBar = true;
                    sv.role = role;
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




            case "stats" -> {
                if (sv.role ==null) {
                    RoleSelectMenu.skillSelect.open(p);
                    return true;
                }
                SmartInventory.builder()
                    .type(InventoryType.DISPENSER)
                    .id("Stats "+p.getName())
                    .provider(new StatsMenu())
                    .title("§3§l   Прокачка Статистики")
                    .build().open(p);
                return true;
            }


            case "ability" -> {
                if (sv.role ==null) {
                    RoleSelectMenu.skillSelect.open(p);
                    return true;
                }
                sv.skillInv.open(p);
                return true;
            }


            case "pet" -> {
                Main.petMgr.petCmd(p, sv);
                return true;
            }
                
            /*case "debug" -> {
                if (ApiOstrov.isLocalBuilder(cs, true)) {
                    if (PM.getOplayer(p).setup==null) {
//Ostrov.log("++INIT builder ");
                        p.performCommand("builder");
                        Ostrov.sync(()->p.performCommand("skill debug"), 20);
                        return true;
                    }
                    PM.getOplayer(p).setup.lastEdit = "Debug";
                    openDebugMenu(p);
                }
                return true;
            }                */
        }










        return true;
    }

    /*public static void openDebugMenu(final Player p) {
        
        final SetupMode setupMode = PM.getOplayer(p).setup;
        
        switch (setupMode.lastEdit) {
            
            case "Debug","LocalGame" -> //LocalGame - иконка в штатном меню строителя
                SmartInventory.builder()
                .type(InventoryType.CHEST)
                .id(setupMode.lastEdit)
                .provider(new DebugMenu())
                .size(1, 9)
                .title("Меню отладчика")
                .updateFrequency(5)
                .build()
                .open(p);
            
            case "DebugBossSpawn" ->
                SmartInventory.builder()
                .type(InventoryType.CHEST)
                .id(setupMode.lastEdit)
                .provider(new DebugBossSpawn())
                .size(6, 9)
                .title("Спавн боссов")
                .build()
                .open(p);
                
            case "DebugMythSpawn" ->
                SmartInventory.builder()
                .type(InventoryType.CHEST)
                .id(setupMode.lastEdit)
                .provider(new DebugMythSpawn())
                .size(6, 9)
                .title("Спавн Myth")
                .build()
                .open(p);
            
            case "DebugBotSpawn" ->
                SmartInventory.builder()
                .type(InventoryType.CHEST)
                .id(setupMode.lastEdit)
                .provider(new DebugBotSpawn())
                .size(6, 9)
                .title("Спавн Bots")
                .build()
                .open(p);
            
        }
        
        
                
    }*/



}
