package ru.romindous.skills.listeners;

import java.util.Optional;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.quests.Quest;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryManager;
import ru.romindous.skills.guides.Entries;
import ru.romindous.skills.items.SkillGroup;
import ru.romindous.skills.menus.UpgradeMenu;
import ru.romindous.skills.survs.Survivor;

public class InventoryLst implements Listener {
    
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
        final Player pl = (Player) e.getPlayer();
        final Optional<InventoryContent> op = InventoryManager.getContents(pl);
        if (op.isPresent() && op.get().getHost().getType() == e.getInventory().getType()) return;
        switch (e.getInventory().getType()) {
            case ENCHANTING:
                e.setCancelled(true);
                pl.sendMessage("§cНад столом чар еще идет работа!");
                /*SmartInventory
                    .builder()
                    .id("Enchanting " + pl.getName())
                    .provider(new Enchanting(e.getInventory().getLocation()))
                    .size(6, 9)
                    .title("          §5§lСтол Чародея")
                    .build()
                    .open(pl);*/
                break;
            case ANVIL:
                e.setCancelled(true);
                if (UpgradeMenu.tryOpen(pl)) break;
                pl.sendMessage("§cНад наковальнями еще идет работа!");
                break;
            case GRINDSTONE:
                e.setCancelled(true);
                pl.sendMessage("§cНад точилом еще идет работа!");
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

    @EventHandler
    public void onClick(final InventoryClickEvent e) {
        final Inventory inv = e.getClickedInventory();
        if (inv == null) return;
        final ItemStack cli = switch (inv) {
            case final CraftingInventory in -> in.getResult();
            case final StonecutterInventory in -> in.getResult();
            case final FurnaceInventory in -> in.getResult();
            case final SmithingInventory in -> in.getResult();
            case final AnvilInventory in -> in.getResult();
            case final EnchantingInventory in -> in.getItem();
            case final GrindstoneInventory in -> in.getResult();
            case final CartographyInventory in -> in.getResult();
            default -> null;
        };
        if (cli == null) return;
        final ItemStack rs = e.getCurrentItem();
        if (!ItemUtil.compare(cli, rs, ItemUtil.Stat.TYPE,
            ItemUtil.Stat.NAME, ItemUtil.Stat.AMOUNT)) {
            return;
        }
        questIt((Player) e.getWhoClicked(), rs);

    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCraft(final BlockCookEvent e) {
        final Block bl = e.getBlock();
        final BlockFace bf = bl.getBlockData() instanceof
            final Directional dir ? dir.getFacing() : BlockFace.SELF;
        final Location loc = bl.getLocation().toCenterLocation()
            .add(bf.getModX() * 0.8d, bf.getModY() * 0.8d, bf.getModZ() * 0.8d);
        final ItemStack rs = e.getResult();
        bl.getWorld().dropItem(loc, rs, it -> {
            it.setCanMobPickup(false);
            it.setWillAge(false);
        }).setVelocity(new Vector());
        new ParticleBuilder(Particle.SMALL_FLAME).location(loc).count(20)
            .offset(0.2d, 0.2d, 0.2d).extra(0d).receivers(20).spawn();
        e.setResult(ItemUtil.air);
        final Player pl = LocUtil.getNearPl(BVec.of(loc), REC_DST, null);
        if (pl == null) return;
        questIt(pl, rs);
    }

    private static final int REC_DST = 100;
    private static void questIt(final Player p, final ItemStack rs) {
        final Survivor sv = PM.getOplayer(p, Survivor.class);
        if (ItemUtil.is(rs, ItemType.IRON_INGOT)) {
            Entries.iron.addProg(p, sv, rs.getAmount());
            return;
        }
        if (ItemUtil.is(rs, ItemType.CRAFTING_TABLE)) {
            Entries.table.addProg(p, sv, rs.getAmount());
            return;
        }
        if (ItemUtil.is(rs, ItemType.SMITHING_TABLE)) {
            Entries.smith.addProg(p, sv, rs.getAmount());
            return;
        }
        if (ItemUtil.is(rs, ItemType.COPPER_INGOT)) {
            Entries.copper.addProg(p, sv, rs.getAmount());
            return;
        }

        final SkillGroup ig = SkillGroup.get(rs);
        if (ig == null) return;
        final Quest qs = ig.quest(rs.getType().asItemType());
        if (qs == null) return;
        qs.addProg(p, sv, rs.getAmount());
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
