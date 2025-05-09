package ru.romindous.skills;

import javax.annotation.Nullable;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemType;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.listeners.worlds.WastesLst;

public enum SubServer {

    WASTES("§cПустошь", "block/coarse_dirt", ItemType.COARSE_DIRT, EntityType.HUSK, 0.75f, EntityType.PILLAGER, new WastesLst()),
    KRIOLITE("§bКриолит", "block/calcite", ItemType.CALCITE, EntityType.STRAY, 0.90f, null, null),
    LOCUS("§2Локус", "block/azalea_leaves", ItemType.MOSS_BLOCK, EntityType.ZOMBIE_VILLAGER, 1.05f, EntityType.WITCH, null),
    INFERNAL("§4Инфернал", "block/nether_bricks", ItemType.CRIMSON_NYLIUM, EntityType.WITHER_SKELETON, 1.15f, EntityType.PIGLIN_BRUTE, null),
    AQUAROD("§9Акварод", "block/dark_prismarine", ItemType.WARPED_HYPHAE, EntityType.DROWNED, 1.25f, null, null),
    KALEUM("§5Калеум", "block/obsidian", ItemType.END_STONE_BRICKS, EntityType.SKELETON, 1.30f, null, null),
    TERRA("§6Терра", "block/honeycomb_block", ItemType.GRASS_BLOCK, EntityType.ZOMBIE, 1.35f, EntityType.WANDERING_TRADER, null);
	
	public static int size = SubServer.values().length;
	
    //для кодироки worldOpen
    public final String disName;
    public final String bGrndTxtr;
    public final ItemType displayMat;
    public final EntityType mobType;
    public final float bfr;
	public final @Nullable EntityType taskNPC;
    public final @Nullable Listener lst;

    SubServer(final String dName, final String bGrndTxtr, final ItemType displayMat, final EntityType mobType, final float bfr, final @Nullable EntityType taskNPC, final @Nullable Listener lst) {
        this.disName = dName;
        this.bGrndTxtr = bGrndTxtr;
        this.displayMat = displayMat;
        this.mobType = mobType;
        this.bfr = bfr;
        this.taskNPC = taskNPC;
        this.lst = lst;
    }
    
    public static SubServer get() {
        try {return SubServer.valueOf(TCUtil.strip(Bukkit.motd())
                .replaceFirst("sedna_", "").toUpperCase());}
        catch (IllegalArgumentException ex) {return WASTES;}
    }
    
    public static @Nullable SubServer parse(final String serv) {
        try {return SubServer.valueOf(serv.toUpperCase(Locale.ROOT));}
        catch (IllegalArgumentException e) {return null;}
    }

    public static void init() {
        final World w = Bukkit.getWorlds().getFirst();
        if (Main.subServer.lst != null)
            Bukkit.getPluginManager().registerEvents(Main.subServer.lst, Main.main);

        w.setGameRule(GameRule.DO_MOB_LOOT, true);
        w.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
        w.setGameRule(GameRule.MOB_GRIEFING, true);
        w.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 0);
        w.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 0);
        w.setGameRule(GameRule.SPAWN_RADIUS, 0);
        switch (Main.subServer) {
            case WASTES:
                w.setTime(4000l);
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                w.setGameRule(GameRule.RANDOM_TICK_SPEED, 2);
                w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                w.setGameRule(GameRule.DO_TRADER_SPAWNING, true);
                break;
            case KRIOLITE:
                w.setTime(16000l);
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                w.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
                w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
                w.setThundering(true);
                w.setStorm(true);
                w.setWeatherDuration(Integer.MAX_VALUE);
                w.setThunderDuration(Integer.MAX_VALUE);
                break;
            case LOCUS, TERRA:
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                w.setGameRule(GameRule.RANDOM_TICK_SPEED, 4);
                w.setGameRule(GameRule.DO_PATROL_SPAWNING, true);
                w.setGameRule(GameRule.DO_TRADER_SPAWNING, true);
                break;
            case AQUAROD, KALEUM, INFERNAL:
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                w.setGameRule(GameRule.RANDOM_TICK_SPEED, 2);
                w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
                break;
            default:
                break;
        }
    }
}
