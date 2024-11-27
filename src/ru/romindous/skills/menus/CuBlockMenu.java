package ru.romindous.skills.menus;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.Damageable;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SlotPos;
import ru.romindous.skills.transfers.CuBlock;

public class CuBlockMenu implements InventoryProvider {

    private final CuBlock cube;
    private final boolean opn;

    public CuBlockMenu(final CuBlock cube) {
        this.cube = cube;
        switch (cube.cbt) {
		case FEED, FUSE, REPAIR:
			opn = true;
			break;
		default:
			opn = false;
			break;
        }
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        its.setEditable(new SlotPos(0, 2), opn);
        update(p, its);
    }
    
    @Override
    public void update(final Player p, final InventoryContent its) {
        final ItemStack it = cube.getItem();
        final ItemBuilder bld;
        if (ItemUtil.isBlank(it, false)) {
        	bld = new ItemBuilder(ItemType.GRAY_STAINED_GLASS_PANE).name("§7Передача душ§7!");
        } else if (cube.souls == cube.cbt.maxSouls) {
    		bld = new ItemBuilder(ItemType.BLUE_STAINED_GLASS_PANE).name("§7Души §9заполнены§7!");
        } else {
        	final int sub;
            switch (cube.cbt) {
                case REPAIR:
                    if (it.hasItemMeta() && it.getItemMeta() instanceof final Damageable db) {
                        if (db.getDamage() > 0) {
                        	bld = cube.souls < Math.min(Math.max(1, cube.souls >> 1), db.getDamage())
                                ? new ItemBuilder(ItemType.BROWN_STAINED_GLASS_PANE).name("§7Недостаточно §4душ§7!")//(§9" + cube.souls + "✞§7)
                                : new ItemBuilder(ItemType.GREEN_STAINED_GLASS_PANE).name("§7Проходит §2починка§7!");
                        } else {
                        	bld = new ItemBuilder(ItemType.LIME_STAINED_GLASS_PANE).name("§7Инструмент §aцелен§7!");
                        }
                    } else {
                    	bld = new ItemBuilder(ItemType.GRAY_STAINED_GLASS_PANE).name("§7Передача душ§7!");
                    }
                    break;
                case FEED:
                	sub = /*Feed.burnAmt(it.getType())*/0;
                	
                	if (sub == 0) {
                		bld = new ItemBuilder(ItemType.GRAY_STAINED_GLASS_PANE).name("§7Ожидание §4пищи§7!");
                		break;
                	}
                	
                	if (sub > 0) {
                		bld = it.getAmount() < sub
                            ? new ItemBuilder(ItemType.BROWN_STAINED_GLASS_PANE).name("§7Недостаточно §4пищи§7!")
                            : new ItemBuilder(ItemType.RED_STAINED_GLASS_PANE).name("§7Поедание §cпищи§7!");
                	} else {
                		bld = new ItemBuilder(ItemType.RED_STAINED_GLASS_PANE).name("§7Поедание §cпищи§7!");
                	}
                    break;
                case FUSE:
                    sub = /*Feed.fuseAmt(it.getType())*/0;
                	
                	if (sub == 0) {
                		bld = new ItemBuilder(ItemType.GRAY_STAINED_GLASS_PANE).name("§7Ожидание §5сосуда§7!");
                		break;
                	}

            		bld = cube.souls < sub 
                        ? new ItemBuilder(ItemType.BROWN_STAINED_GLASS_PANE).name("§7Недостаточно §4душ§7!")
                        : new ItemBuilder(ItemType.RED_STAINED_GLASS_PANE).name("§7Наполнение §dсосуда§7!");
            		
                    break;
                default:
                	bld = new ItemBuilder(ItemType.GRAY_STAINED_GLASS_PANE).name("§7Передача душ§7!");
                    break;
            }
        }
        
        its.fillRow(0, ClickableItem.empty(bld.lore(Arrays.asList(" ", "§7Кол-во душ: §9" + cube.souls + "✞§7/§9"
            + cube.cbt.maxSouls + "✞", "§7Макс. ко-во целей", "§7передачи душ: §3" + cube.cbt.maxTeth)).build()));
        
        if (opn) {
            its.set(2, ClickableItem.from(it, e -> {
                p.playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 0.8f, 0.8f);
                if (e.getEvent() instanceof final InventoryClickEvent ev) {
                    final ItemStack nit;
                    ev.setResult(Result.DENY);
            		switch (ev.getClick()) {
                    case SWAP_OFFHAND:
                    	nit = p.getInventory().getItemInOffHand().clone();
                    	p.getInventory().setItemInOffHand(cube.getItem());
                        break;
                    case NUMBER_KEY:
                    	nit = p.getInventory().getItem(ev.getHotbarButton()).clone();
                    	p.getInventory().setItem(ev.getHotbarButton(), cube.getItem());
                        break;
                    default:
                    	nit = ev.getCursor().clone();
                        ev.getWhoClicked().setItemOnCursor(cube.getItem());
                        break;
            		}
                	cube.setItem(nit);
            		update(p, its);
                }
        	}));
        }
    }

    public void reopen(final Player p, final InventoryContent its, final boolean all) {
        if (all) {
            for (final HumanEntity he : new ArrayList<>
                (its.getInventory().getViewers())) {
                reopen((Player) he, its, false);
            }
            return;
        }
        reopen(p, its);
    }
}
