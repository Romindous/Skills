package ru.romindous.skills.menus;

import java.util.List;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.romindous.skills.Main;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.enums.SubServer;


public class WorldMenu implements InventoryProvider {
	
    @Override
    public void init(final Player p, final InventoryContent content) {

        final Survivor sv = PM.getOplayer(p, Survivor.class);

        for (final SubServer ss : SubServer.values()) {

            if (!sv.isWorldOpen(ss)) {
                content.set(ss.ordinal() / 3 + ss.ordinal(), ClickableItem.from(
                    new ItemBuilder(ItemType.FIREWORK_STAR)
                        .name("§7§k"+ss.disName.substring(2))
                        .lore("§eДля открытия мира")
                        .lore("§eдобудь или скрафти ключ!")
                        .lore(ApiOstrov.isLocalBuilder(p)? "§f*Билдер: открыть" : "")
                        .build(), e-> {
                            if (ApiOstrov.isLocalBuilder(p)) {
                                sv.unlockWorld(ss);
                                reopen(p, content);
                            } else {
                                PM.soundDeny(p);
                            }
                        }
                ));
            } else {
                if (Main.subServer == ss) {
                    content.set(ss.ordinal()/3 + ss.ordinal(), ClickableItem.from(new ItemBuilder(ss.displayMat)
                        .name(TCUtil.sided(ss.disName, "⛨")).lore(TCUtil.N + "Ты здесь")
                        .build(), e -> p.performCommand("skill")));
                } else {
                    content.set(ss.ordinal()/3 + ss.ordinal(), ClickableItem.from(new ItemBuilder(ss.displayMat)
                        .name(TCUtil.sided(ss.disName, "⛨")).build(), e -> moveTo(p, ss, true)));
                }
            }
        }
    }

    public static void moveTo(final Player p, final SubServer ss, final boolean dry) {
        if (dry && dryWaterInv(p.getInventory())) {
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1f, 0.8f);
        }
        LocalDB.saveLocalData(p, PM.getOplayer(p));
        new PlayerQuitEvent(p, TCUtil.form(p.getName() + " перешел на " + ss.name()), QuitReason.DISCONNECTED).callEvent();
        Ostrov.sync(() -> ApiOstrov.sendToServer(p, "sedna_"+ss.name().toLowerCase(), ""), 8);
    }
    
    private static boolean dryWaterInv(final Inventory inv) {
    	boolean fnd = false;
    	for (int i = inv.getSize() - 1; i >= 0; i--) {
            final ItemStack it = inv.getItem(i);
    		if (ItemUtil.isBlank(it, false)) continue;
    		final ItemMeta im;
            switch (it.getType()) {
                case WATER_BUCKET, AXOLOTL_BUCKET, COD_BUCKET,
                PUFFERFISH_BUCKET, SALMON_BUCKET, TROPICAL_FISH_BUCKET:
                	inv.setItem(i, ItemType.BUCKET.createItemStack(it.getAmount()));
                    fnd = true;
                    break;
                case POTION:
                    if (((PotionMeta) it.getItemMeta()).getBasePotionType() == PotionType.WATER) {
                        inv.setItem(i, ItemType.GLASS_BOTTLE.createItemStack(it.getAmount()));
                        fnd = true;
                    }
                    break;
                case WET_SPONGE:
                    inv.setItem(i, ItemType.SPONGE.createItemStack(it.getAmount()));
                    fnd = true;
                    break;
                case ICE:
                    inv.setItem(i, ItemType.GLASS.createItemStack(it.getAmount()));
                    fnd = true;
                    break;
                case BUNDLE:
                	im = it.getItemMeta();
                    boolean inFnd = false;
                	final List<ItemStack> bts = ((BundleMeta) im).getItems();
                    for (int j = bts.size() - 1; j >= 0; j--) {
                        final ItemStack st = bts.get(j);
                        if (ItemUtil.isBlank(st, false)) continue;
                        switch (st.getType()) {
                        case WATER_BUCKET, AXOLOTL_BUCKET, COD_BUCKET,
                         PUFFERFISH_BUCKET, SALMON_BUCKET, TROPICAL_FISH_BUCKET:
                            bts.set(i, ItemType.BUCKET.createItemStack(st.getAmount()));
                            inFnd = true;
                            break;
                        case POTION:
                            if (((PotionMeta) it.getItemMeta()).getBasePotionType() == PotionType.WATER) {
                                bts.set(i, ItemType.GLASS_BOTTLE.createItemStack(st.getAmount()));
                                inFnd = true;
                            }
                            break;
                        case WET_SPONGE:
                            bts.set(i, ItemType.SPONGE.createItemStack(st.getAmount()));
                            inFnd = true;
                            break;
                        case ICE:
                            bts.set(i, ItemType.GLASS.createItemStack(st.getAmount()));
                            inFnd = true;
                            break;
                        default:
                        	break;
                        }
                    
                	}
                	
                	if (inFnd) {
                		((BundleMeta) im).setItems(bts);
                		it.setItemMeta(im);
                	}
                    break;
                /*case SHULKER_BOX, BLACK_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX,
                CYAN_SHULKER_BOX, GRAY_SHULKER_BOX, GREEN_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX, 
                LIGHT_GRAY_SHULKER_BOX, LIME_SHULKER_BOX, MAGENTA_SHULKER_BOX, ORANGE_SHULKER_BOX, 
                PINK_SHULKER_BOX, PURPLE_SHULKER_BOX, RED_SHULKER_BOX, WHITE_SHULKER_BOX, YELLOW_SHULKER_BOX:
                    break;*/
                default:
                	if (Tag.SHULKER_BOXES.isTagged(it.getType())) {
                    	im = it.getItemMeta();
                    	if (dryWaterInv(((ShulkerBox) ((BlockStateMeta) im).getBlockState()).getInventory())) {
                        	fnd = true;
                        	it.setItemMeta(im);
                    	}
                	}
                	break;
            }
		
    	}
    	return fnd;
    }
    
}
