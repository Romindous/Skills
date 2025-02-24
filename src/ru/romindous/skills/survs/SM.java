package ru.romindous.skills.survs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.ApiOstrov;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.TprCmd;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.MainTask;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.transfers.CuBlock;
import ru.romindous.skills.transfers.TransferType;




public class SM {
    private static BukkitTask mainTask;
//    private static BukkitTask mainAsyncTask;
    public static final String[] CONGRATS;

//    public static final int LEVEL_XP_MULTIPLER = 50;
//    public static final int MAX_ABILITY_LEVEL = 4;
    public static final int HP_PER_HEART = 4;
//    public static final float DJ_FALL_DST = 10000f;
    public static final String PREFIX = "surv";
    public static final String HEART_MAX = "§2❤ ";
    public static final String HEART_FULL = "§a❤ ";
    public static final String HEART_HALF = "§e❤ ";
    public static final String HEART_LESS = "§6❤ ";
    public static final String HEART_LOW = "§c❤ ";
    public static final String HEART_CLR = "<red>";
    public static final Location JOIN_LOC;
    
    //на переделку
	public static int tId = 0;
    public static final HashMap<Integer, CuBlock> cublocks = new HashMap<>();
    public static final int NEW_SKILL_LVL = value("new_skill_lvl", 14);
    public static final int NEW_ABIL_LVL = value("new_abil_lvl", 8);
    public static final int NEW_MOD_LVL = value("new_mod_lvl", 4);
    //public static final ArrayList<LentJob> jobs = new ArrayList<>();
    
    static {
        CONGRATS = new String[]{"поздравляем!", "хорошая игра!", "так держать!", "замечательно!"};
        JOIN_LOC = Bukkit.getWorlds().getFirst().getSpawnLocation().clone().add(0, 1000, 0);
    }

    public static List<String> getDebugInfo(List<String> lore) {
        lore.set(1, "§7Тики работы: "+ MainTask.tick);
        lore.set(2, "§7Время работы mainTask: §6"+MainTask.proccesTime+" ms");
        lore.set(8, "§7cublocks: "+cublocks.size());
        return lore;
    }
    
    
    public static void init() {
        mainTask = Bukkit.getScheduler().runTaskTimer(Main.main, new MainTask(), 25, 1);
//        mainTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.main, new MainAsyncTask(), 25, 1);
    }

    public static void disable() {
        if (mainTask != null) {
            mainTask.cancel();
        }
        /*if (mainAsyncTask != null) {
            mainAsyncTask.cancel();
        }*/
    }
    
    

    
    
    
    

    public static @Nullable Survivor getNearestSurvivor(final Location loc, final int mxd) {
        Player p = null;
        int d = Integer.MAX_VALUE;
        final int x = loc.getBlockX();
        final int z = loc.getBlockZ();
        for (final Player pl : loc.getWorld().getPlayers()) {
            final int dd = Math.abs(pl.getLocation().getBlockX() - x)
                + Math.abs(pl.getLocation().getBlockZ() - z);
            if (dd < d) {d = dd; p = pl;}
        }
        return p == null || d > mxd ? null : PM.getOplayer(p, Survivor.class);
    }
    
    public static void randomJoin(final Player p, final boolean nevv) {
        if (p.getGameMode() == GameMode.SPECTATOR) {
            p.setGameMode(GameMode.SURVIVAL); //могли ливнуть в ГМ3 с серв.
        }
        if (nevv) {
            ScreenUtil.sendTitle(p, "§4Выживи", "");
            p.getInventory().setItem(0, ItemType.BREAD.createItemStack(16));
            p.setNoDamageTicks(100);
//            p.sendMessage(Main.prefix + "У тебя неуязвимость на след. 5 сек!");
        }
        p.teleport(JOIN_LOC); //тп в небо
        p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 1));
        TprCmd.runCommand(p, p.getWorld(), 5000, true, true, pl -> {
            pl.playSound(pl.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1, 5);
            pl.removePotionEffect(PotionEffectType.LEVITATION);
        });
    }

    public static void resetPlayer(final CommandSender cs, final Player p) {
        p.closeInventory();
        final String name = p.getName();
        p.sendMessage(TCUtil.form(Main.prefix + "§6Игровые данные сброшены, перезайди на сервер"));
        ApiOstrov.sendToServer(p, "lobby0", "");
        cs.sendMessage("§6Данные " + p.getName() + " сброшены!");
        p.playSound(p.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 0.5f, 0.85f);
        //нужно удалять запись, или сохраняются точки выхода, инвентарь и тд!
        Ostrov.async( ()-> LocalDB.executePstAsync(Bukkit.getConsoleSender(),
            "DELETE FROM `playerData` WHERE `name` = '"+name+"';"), 20);
    }

    public enum Info {
        ALL,
        LEVEL,
        HEALTH,
        MANA,
    }
    
    public static List<CuBlock> getTypeTransfers(final TransferType type) {
        final List<CuBlock> trns = new ArrayList<>();
        for (final CuBlock tr : cublocks.values()) {
            if (tr.getTransType() == type) trns.add(tr);
        }
        return trns;
    }
	
	public static void setSpawn(final Player p, @Nullable final Location loc) {
		final Location prv = p.getRespawnLocation();
		if (prv != null && loc != null) {
			if (prv.getBlockX() == loc.getBlockX() 
			&& prv.getBlockY() == loc.getBlockY() 
			&& prv.getBlockZ() == loc.getBlockZ())
				return;
		}
		final Survivor sv = PM.getOplayer(p, Survivor.class);
		final String sps = sv.mysqlData.get("resps");
		if (sps == null) {
			if (loc != null) {
                sv.mysqlData.put("resps", new XYZ(loc.getWorld().getName(),
                    loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).toString());
            }
		} else {
			final ArrayList<String> spa = new ArrayList<>(Arrays.asList(sps.split(StringUtil.SPLIT_0)));
			spa.removeIf(e -> e.startsWith(p.getWorld().getName()));
			if (loc != null) {
				spa.add(new XYZ(loc.getWorld().getName(), 
					loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).toString());
			}
			final StringBuilder sb = new StringBuilder();
			for (final String s : spa) {
				sb.append(StringUtil.SPLIT_0).append(s);
			}
			sv.mysqlData.put("resps", sb.isEmpty() ? null : sb.substring(StringUtil.SPLIT_0.length()));
		}
		final Location last = p.getRespawnLocation();
        if (loc != null && last != null && BVec.of(last).distAbs(BVec.of(loc)) == 0) return;
		p.setRespawnLocation(loc, false);
		if (loc == null) {
			p.sendMessage(TCUtil.form(Main.prefix + "Точка спавна " + TCUtil.P + "очищена" + TCUtil.N + ", переставь свой респавн!"));
		} else {
			p.sendMessage(TCUtil.form(Main.prefix + "Точка спавна поставлена на (" + TCUtil.P + loc.getBlockX() + TCUtil.N
                + ", " + TCUtil.P + loc.getBlockY() + TCUtil.N + ", " + TCUtil.P + loc.getBlockZ() + TCUtil.P + ")"));
		}
	}

    public static int value(final String id, final int val) {
        return ConfigVars.get(SM.PREFIX + "." + id, val);
    }

    public static double value(final String id, final double val) {
        return ConfigVars.get(SM.PREFIX + "." + id, val);
    }
}
