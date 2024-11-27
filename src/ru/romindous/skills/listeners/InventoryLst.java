package ru.romindous.skills.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import ru.komiss77.events.BuilderMenuEvent;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.menus.Enchanting;

public class InventoryLst implements Listener {
	
    @EventHandler (priority = EventPriority.MONITOR)
    public void onBuilderMenuClick(final BuilderMenuEvent e) {
//Ostrov.log("onBuilderMenuClick "+e.getSetupMode().lastEdit);
//        SkillCmd.openDebugMenu(e.getPlayer());
    }
    
    
    /*@EventHandler
	public void onInv(final InventoryClickEvent e) {
		final ItemStack it = e.getCurrentItem();
		//e.getWhoClicked().sendMessage("ite-" + it);
		if (ItemUtil.isBlank(it, false)) return;
		final CustomMats cMats = CustomMats.getCstmItm(it);
		if (cMats == null) return;
		e.setCurrentItem(cMats.getItem(it.getType())
			.asQuantity(it.getAmount()));
	}*/

	@EventHandler
	public void onEnch(final EnchantItemEvent e) {
//		QM.tryCompleteQuest(e.getEnchanter(), Quest.EnchantItem, 1, true);
	}
	
	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onOpn(final InventoryOpenEvent e) {
        switch (e.getInventory().getType()) {
            case ENCHANTING:
                e.setCancelled(true);
                SmartInventory
                    .builder()
                    .id("Enchanting " + e.getPlayer().getName())
                    .provider(new Enchanting(e.getInventory().getLocation()))
                    .size(6, 9)
                    .title("          §5§lСтол Чародея")
                    .build()
                    .open((Player) e.getPlayer());
                break;
            case ANVIL:
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cНад наковальнями еще идет работа!");
                break;
            case GRINDSTONE:
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cНад точилом еще идет работа!");
                break;
            default:
                break;
        }
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExp(final PlayerExpChangeEvent e) {
        final Player p = e.getPlayer();
        final Survivor sv = PM.getOplayer(p, Survivor.class);
        final int amt = e.getAmount();
        e.setAmount(0);
        if (sv == null) return;
        sv.addXp(p, amt);
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCls(final InventoryCloseEvent e) {
    	/*final Inventory top = e.getView().getTopInventory();
    	final Component ttl = e.getView().title();
    	if (ttl instanceof TextComponent) {
    		final ItemStack it;
    		//e.getPlayer().sendMessage(((TextComponent) ttl).content());
    		//e.getPlayer().sendMessage(Arrays.toString(top.getContents()));
    	}*/
    }

    /*@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExp(final PlayerExpChangeEvent e) {
        final Player p = e.getPlayer();
        int nxp = p.getTotalExperience() + e.getAmount();
        p.giveExp(e.getAmount());
        final int l = (int) (Math.sqrt((double) nxp / 1.5d));
        p.setLevel(l);
        p.setExp((nxp * 2f / 3f - (l * l)) / ((l + 1) * (l + 1) - (l * l)));
        e.setAmount(0);
    }*/

    /*@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTrdGet(final VillagerAcquireTradeEvent e) {
        final MerchantRecipe mr = e.getRecipe();
        final ItemStack rs = mr.getResult();
        if (rs != null && rs.getType() == ItemType.EMERALD) {
            rs.setType(ItemType.COPPER_BLOCK);
            final MerchantRecipe mc = new MerchantRecipe(rs, mr.getUses(), mr.getMaxUses(), mr.hasExperienceReward(), mr.getVillagerExperience(), mr.getPriceMultiplier(), mr.shouldIgnoreDiscounts());
            final List<ItemStack> its = mr.getIngredients();
            for (final ItemStack it : its) {
                if (it != null && it.getType() == ItemType.EMERALD) {
                    it.setType(ItemType.COPPER_BLOCK);
                }
            }
            mc.setIngredients(its);
            e.setRecipe(mc);
        } else {
            final List<ItemStack> its = mr.getIngredients();
            for (final ItemStack it : its) {
                if (it != null && it.getType() == ItemType.EMERALD) {
                    it.setType(ItemType.COPPER_BLOCK);
                }
            }
            mr.setIngredients(its);
        }
    }*/

    /*@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMinecartHopper(final InventoryMoveItemEvent e) {
    	if (e.getDestination().getType() == InventoryType.HOPPER && e.getSource().getType() == InventoryType.CHEST) {
    		final InventoryHolder ih = e.getSource().getHolder();
    		if (ih instanceof BlockInventoryHolder) {
    			if (((BlockInventoryHolder) ih).getBlock().getType() == ItemType.CHEST) {
    				e.setCancelled(true);
    			}
    		} else if (ih instanceof DoubleChest) {
				e.setCancelled(true);
			}
    	}
    }*/
}
