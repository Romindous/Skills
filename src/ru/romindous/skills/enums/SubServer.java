package ru.romindous.skills.enums;

import javax.annotation.Nullable;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import ru.romindous.skills.Main;

public enum SubServer {

    WASTES("§cПустошь", "textures/block/cobbled_deepslate.png", Material.COARSE_DIRT, EntityType.HUSK, 0.75f, EntityType.PILLAGER),
    KRIOLITE("§bКриолит", "textures/block/tuff.png", Material.CALCITE, EntityType.STRAY, 0.90f, null),
    LOCUS("§2Локус", "textures/block/muddy_mangrove_roots_side.png", Material.MOSS_BLOCK, EntityType.ZOMBIE_VILLAGER, 1.05f, EntityType.WITCH),
    INFERNAL("§4Инфернал", "textures/block/nether_bricks.png", Material.CRIMSON_NYLIUM, EntityType.WITHER_SKELETON, 1.15f, EntityType.PIGLIN_BRUTE),
    AQUAROD("§9Акварод", "textures/block/dark_prismarine.png", Material.WARPED_HYPHAE, EntityType.DROWNED, 1.25f, null),
    KALEUM("§5Калеум", "textures/block/obsidian.png", Material.END_STONE_BRICKS, EntityType.SKELETON, 1.30f, null),
    TERRA("§6Терра", "textures/block/dark_prismarine.png", Material.GRASS_BLOCK, EntityType.ZOMBIE, 1.35f, EntityType.WANDERING_TRADER);
	
	public static int size = SubServer.values().length;
	
    //для кодироки worldOpen
    public final String displayName;
    public final String bGrndTxtr;
    public final Material displayMat;
    public final EntityType mobType;
    public final float bfr;
	public final EntityType taskNPC;

    SubServer(final String dName, final String bGrndTxtr, final Material displayMat, final EntityType mobType, final float bfr, final EntityType taskNPC) {
        this.displayName = dName;
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
        switch (Main.subServer) {
            case WASTES:
                break;
            case KRIOLITE:
                break;
            case LOCUS:
                break;
            case INFERNAL:
                break;
            case AQUAROD:
                break;
            case KALEUM:
                break;
            case TERRA:
                break;
        }
    }
}
