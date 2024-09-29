package ru.romindous.skills;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.komiss77.OConfigManager;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.modules.menuItem.MenuItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.enums.SubServer;
import ru.romindous.skills.mobs.Minion;
import ru.romindous.skills.utils.pets.IPetManager;
import ru.romindous.skills.utils.pets.PetVanilla;



public class Main extends JavaPlugin {

    public static Main main;
    public static final String N = "<indigo>";
    public static final String P = "<maroon>";
    public static final String A = "<dark_purple>";
    public static final String manaClr = "§9🔥 ";
    public static final String cdClr = "§б⏰⌚ ";
    public static final String prefix = Main.N + "[" + Main.A + "SN" + Main.N + "] ";
    public static final SecureRandom srnd = new SecureRandom();
    public static final SubServer subServer = SubServer.getForThis();
    public static OConfigManager configManager;
    public static MenuItem diary;
    public static IPetManager petMgr;
    public static final Path configDir = Path.of(Path.of(Bukkit.getPluginsFolder().toURI())
        .toAbsolutePath().getParent().getParent().toString(), "skills");
    
    private static final boolean turnOffAddons = false;

    @Override
    public void onEnable() {
        //OSTROV stuff
        TCUtil.N = "§ф";
        TCUtil.P = "§к";
        TCUtil.A = "§5";
        PM.setOplayerFun(Survivor::new, true);

        main = this;
        configManager = new OConfigManager(main);
        Ostrov.log_ok("Skills loading...");
        Ostrov.log_ok("§bПодсервер седны: " + subServer.displayName);
        
        if (Bukkit.getPluginManager().getPlugin("MyPet") == null || turnOffAddons) {
            petMgr = new PetVanilla();
        } else {
            petMgr = new PetVanilla();
//            petMgr = new PetManager();
//            Bukkit.getPluginManager().registerEvents(petMgr, Main.main);
        }
        
        /*if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null || turnOffAddons) {
            bossMgr = new Bosses();
            modelMgr = new VanilaModel();
        } else {
            bossMgr = new MythBoss();
            modelMgr = new VanilaModel();
            //modelMgr = new MythModel();
        }*/

//        bossMgr.init(main);//bosses = new Bosses(main); //+getCommand("boss").setExecutor(new BossCmd());
        SM.init();
        SubServer.init();
        
        //подгружает protectionInfo из бд
        //подгрузка area.yml (пока пустой)
//        new Land();
        //Builds.loadBuilds();
        pathServer();

        for (final Class<?> clazz : ClassUtil.getClasses(main.getFile(), "ru.romindous.skills.listeners")) {
            try {
                getServer().getPluginManager().registerEvents((Listener) clazz.getDeclaredConstructor().newInstance(), this);
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                Ostrov.log_err("Инициализация listener " + clazz.getName() + " -> " + ex.getMessage());
            }
        }

        /*for (final Player p : Bukkit.getOnlinePlayers()) {
            final Oplayer op = PM.getOplayer(p);
            final LocalDataLoadEvent e = new LocalDataLoadEvent(p, op, p.getLocation());
            Bukkit.getPluginManager().callEvent(e);
        }*/

        getCommand("skill").setExecutor(new SkillCmd());
        
        Ostrov.log_ok("Skills done!");
    }

    @Override
    public void onDisable() {
//        bossMgr.disable();
        SM.disable();
        HandlerList.unregisterAll(this);
        Ostrov.log_ok("§4Sedna is off!");
    }
    
    private void pathServer() {
        final World w = getServer().getWorlds().getFirst();
        
        w.setGameRule(GameRule.DO_MOB_LOOT, true);
        w.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
        w.setGameRule(GameRule.MOB_GRIEFING, true);
        w.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 0);
        switch (subServer) {
            case WASTES:
                w.setTime(18000l);
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                w.setGameRule(GameRule.RANDOM_TICK_SPEED, 2);
                w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                break;
            case KRIOLITE:
                w.setTime(16000l);
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                w.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
                w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                w.setThundering(true);
                w.setStorm(true);
                w.setWeatherDuration(Integer.MAX_VALUE);
                w.setThunderDuration(Integer.MAX_VALUE);
                break;
            case LOCUS, TERRA:
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                w.setGameRule(GameRule.RANDOM_TICK_SPEED, 4);
                w.setGameRule(GameRule.DO_PATROL_SPAWNING, true);
                break;
            case AQUAROD, KALEUM, INFERNAL:
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                w.setGameRule(GameRule.RANDOM_TICK_SPEED, 2);
                w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                break;
            default:
				break;
        }

        diary = new MenuItemBuilder("diary",
            new ItemBuilder(Material.WRITTEN_BOOK)
                .name("§6Дневник")
                .lore(Arrays.asList(" ", "§7С двойкой домой", "§7не приходить!"))
                .enchant(Enchantment.INFINITY).build())
            .slot(8)
            .giveOnJoin(false)
            .giveOnRespavn(true)
            .giveOnWorld_change(false)
            .anycase(false)
            .canDrop(false)
            .canMove(true)
            .canPickup(false)
            .duplicate(false)
            .rightClickCmd("role")
            .leftClickCmd("menu")
            .create();
    }

	public static boolean addHp(final LivingEntity le, final double hlth, final boolean goOver) {
        final AttributeInstance hpa = le.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (hpa.getBaseValue() < le.getHealth() + hlth) {
			if (goOver) {
				hpa.setBaseValue((int) le.getHealth() + hlth + 1);
			} else {
				le.setHealth(hpa.getBaseValue());
				return false;
			}
		}
		le.setHealth(le.getHealth() + hlth);
		return true;
		//Bukkit.broadcast(Component.text("gained " + hlth + ", now " + le.getHealth() + hlth));
	}

    public static boolean canAttack(final LivingEntity dmgr, final Entity tgt, final boolean tell) {
        if (dmgr.getEntityId() == tgt.getEntityId() || Minion.isOwner(tgt, dmgr)) return false;
        if (dmgr instanceof final Player dpl) {
            final Survivor dsv = PM.getOplayer(dpl, Survivor.class);
            if (dsv == null) return false;

            if (tgt instanceof final Player tpl) {
                final Survivor tsv = PM.getOplayer(tpl, Survivor.class);
                if (tsv == null) return false;

                if (tell && FastMath.mulDiff(dsv.exp, dsv.exp) != 0) {
                    dmgr.sendMessage(Main.prefix + "§cТы не можешь ударить этого игрока, разница в вашем уровне слишком велика!");
                    return false;
                }
            }
            return true;
        }
        return true;
    }

	/*public static String nrmlzStr(final String s) {
		final char[] ss = s.toLowerCase().toCharArray();
		ss[0] = Character.toUpperCase(ss[0]);
		for (byte i = (byte) (ss.length - 1); i > 0; i--) {
			switch (ss[i]) {
			case '_':
				ss[i] = ' ';
			case ' ':
				ss[i + 1] = Character.toUpperCase(ss[i + 1]);
				break;
			default:
				break;
			}
		}
		return String.valueOf(ss);
	}

	public static String toSigFigs(final float n, final byte sf) {
		final String nm = String.valueOf(n);
		return nm.indexOf('.') + sf + 1 < nm.length() ? nm.substring(0, nm.indexOf('.') + sf + 1) : nm;
	}*/
}
