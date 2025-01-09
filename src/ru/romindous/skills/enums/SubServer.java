package ru.romindous.skills.enums;

import javax.annotation.Nullable;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import ru.romindous.skills.Main;

public enum SubServer {

    WASTES("§cПустошь", "textures/block/cobbled_deepslate.png", ItemType.COARSE_DIRT, EntityType.HUSK, 0.75f, EntityType.PILLAGER),
    KRIOLITE("§bКриолит", "textures/block/tuff.png", ItemType.CALCITE, EntityType.STRAY, 0.90f, null),
    LOCUS("§2Локус", "textures/block/muddy_mangrove_roots_side.png", ItemType.MOSS_BLOCK, EntityType.ZOMBIE_VILLAGER, 1.05f, EntityType.WITCH),
    INFERNAL("§4Инфернал", "textures/block/nether_bricks.png", ItemType.CRIMSON_NYLIUM, EntityType.WITHER_SKELETON, 1.15f, EntityType.PIGLIN_BRUTE),
    AQUAROD("§9Акварод", "textures/block/dark_prismarine.png", ItemType.WARPED_HYPHAE, EntityType.DROWNED, 1.25f, null),
    KALEUM("§5Калеум", "textures/block/obsidian.png", ItemType.END_STONE_BRICKS, EntityType.SKELETON, 1.30f, null),
    TERRA("§6Терра", "textures/block/dark_prismarine.png", ItemType.GRASS_BLOCK, EntityType.ZOMBIE, 1.35f, EntityType.WANDERING_TRADER);
	
	public static int size = SubServer.values().length;
	
    //для кодироки worldOpen
    public final String disName;
    public final String bGrndTxtr;
    public final ItemType displayMat;
    public final EntityType mobType;
    public final float bfr;
	public final EntityType taskNPC;

    SubServer(final String dName, final String bGrndTxtr, final ItemType displayMat, final EntityType mobType, final float bfr, final EntityType taskNPC) {
        this.disName = dName;
        this.bGrndTxtr = bGrndTxtr;
        this.displayMat = displayMat;
        this.mobType = mobType;
        this.bfr = bfr;
        this.taskNPC = taskNPC;
    }
    
    public static SubServer getForThis() {
        try {
        return SubServer.valueOf(((TextComponent) Bukkit.motd()).content().replaceFirst("sedna_", "").toUpperCase());
        } catch (IllegalArgumentException ex) {
            return WASTES;
        }
    }
    
    public static @Nullable SubServer parseSubServer(final String serv) {
    	for (final SubServer ss : values()) {
    		if (ss.toString().equalsIgnoreCase(serv)) return ss;
    	}
    	return null;
    }

    public static void init() {
        final World w = Bukkit.getWorlds().getFirst();

        w.setGameRule(GameRule.DO_MOB_LOOT, true);
        w.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
        w.setGameRule(GameRule.MOB_GRIEFING, true);
        w.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 0);
        switch (Main.subServer) {
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
    }
}
