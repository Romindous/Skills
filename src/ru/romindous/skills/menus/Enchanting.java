package ru.romindous.skills.menus;

import java.util.*;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Candle;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Main;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.config.ConfigVars;

public class Enchanting implements InventoryProvider {

    private static final ItemStack[] empty = fillEnchInv();
    private static final BlockFace[] SIDES = {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};
    private static final Enchantment[] ENCHS;
    private static final Enchantment[] CRSS;

    static {
        final List<Enchantment> enchs = new ArrayList<>();
        final List<Enchantment> crss = new ArrayList<>();
        OStrap.retrieveAll(RegistryKey.ENCHANTMENT).stream().forEach(e -> {
            if (e.isCursed()) enchs.add(e);
            else crss.add(e);
        });
        ENCHS = enchs.toArray(new Enchantment[0]);
        CRSS = crss.toArray(new Enchantment[0]);
    }

    private static ItemStack[] fillEnchInv() {
        final ItemStack[] its = new ItemStack[54];
        for (byte i = 0; i < 54; i++) {
            if (i < 9 || i > 44) {
                if (i != 4) {
                    if (i == 49) {
                        its[i] = new ItemBuilder(ItemType.FILLED_MAP).name("§7Страница")/*.modelData(SkillMats.AZULITE.ordinal() + 10)*/.build();
                    } else {
                        its[i] = (i & 1) == 0 ? new ItemBuilder(ItemType.MAGENTA_STAINED_GLASS_PANE).name("§0.").build()
                            : new ItemBuilder(ItemType.YELLOW_STAINED_GLASS_PANE).name("§0.").build();
                    }
                }
            } else {
                switch (i % 9) {
                    case 0:
                    case 8:
                        its[i] = (i & 1) == 0 ? new ItemBuilder(ItemType.MAGENTA_STAINED_GLASS_PANE).name("§0.").build()
                            : new ItemBuilder(ItemType.YELLOW_STAINED_GLASS_PANE).name("§0.").build();
                        break;
                    default:
                        its[i] = new ItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build();
                        break;
                }
            }
            its[12] = new ItemBuilder(ItemType.MAGENTA_STAINED_GLASS_PANE).name("§0.").build();
            its[13] = new ItemBuilder(ItemType.YELLOW_STAINED_GLASS_PANE).name("§0.").build();
            its[14] = new ItemBuilder(ItemType.MAGENTA_STAINED_GLASS_PANE).name("§0.").build();
        }
        return its;
    }

    private static final String prefix = "enchant.";
    private static final float CURSE_CH_MUL = ConfigVars.get(prefix + "crsChance", 1000);
    private static final float DIS_EXP = 0.6f;
    private static final int SLOT = 4;
//    private static final float CURSE_NUM_MUL = CURSE_CH_MUL / 10f;
    
    private final Location loc;
    private final float costDecrease;
    private ItemStack item;
    
    public Enchanting(final Location loc) {
    	this.loc = loc;
    	int finBoost = 0;
    	final World w = loc.getWorld();
    	for (final BlockFace bf : SIDES) {
            final WXYZ bsh = new WXYZ(w, (bf.getModX() << 1) + loc.getBlockX(),
                (bf.getModY() << 1) + loc.getBlockY(), (bf.getModZ() << 1) + loc.getBlockZ());
    		if (Nms.fastType(w, loc.getBlockX() + bf.getModX(),
                loc.getBlockY() + bf.getModY(), loc.getBlockZ() + bf.getModZ()).isAir()) {
        		final int boost = bookBuff(Nms.fastData(bsh));
                if (boost == 0) continue;
                finBoost += boost;
                final BlockData ud = w.getBlockAt(bsh.x, bsh.y + 1, bsh.z).getBlockData();
                if (ud instanceof final Candle cd && cd.isLit()) {
                    finBoost += cd.getCandles();
                }
    		}
    	}
    	this.costDecrease = 1f - (0.0125f * finBoost);
    	this.item = ItemUtil.air.clone();
	}

    private int bookBuff(final BlockData bd) {
        if (BlockType.BOOKSHELF.equals(bd.getMaterial().asBlockType())) return 4;
        if (bd instanceof final ChiseledBookshelf cb) {
            return cb.getOccupiedSlots().size();
        }
        return 0;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.8f);
        final Inventory inv = its.getInventory();
        if (inv != null) inv.setContents(empty);

        //p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_WORK_LIBRARIAN, 2, 1);
        its.setEditable(new SlotPos(0, SLOT), true);
        its.set(SLOT, ClickableItem.from(inv.getItem(4), e -> {
            p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2f);
            if (e.getEvent() instanceof final InventoryClickEvent ev) {
                final PlayerInventory pinv = p.getInventory();
                switch (e.getClick()) {
		            case MIDDLE, CREATIVE, UNKNOWN:
		            	p.sendMessage("?");
		            	return;
    	            case SWAP_OFFHAND:
    	            	item = pinv.getItemInOffHand();
    	                break;
    	            case NUMBER_KEY:
    	            	item = pinv.getItem(ev.getHotbarButton());
    	                break;
    	            default:
    	        		item = ev.getCursor().clone();
    	                break;
                }
            	//p.sendMessage(item.toString());
                ev.setResult(Result.ALLOW);
    			shwEnchPg(its, p, (byte) 0);
            }
        }));
    }

    private void shwEnchPg(final InventoryContent its, final Player p, final byte pg) {
        final Inventory inv = its.getInventory();
        final ItemStack prv = inv.getItem(4);
        inv.setContents(empty);
        inv.setItem(4, prv);
    	//p.sendMessage(item.toString());
        inv.setItem(12, new ItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build());
        inv.setItem(13, new ItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build());
        inv.setItem(14, new ItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build());
        inv.getItem(49).setAmount(pg + 1);
        if (pg != 0) {
            its.set(45, ClickableItem.of(new ItemBuilder(ItemType.CRIMSON_TRAPDOOR).name("§5<<===").build(), e -> {
                shwEnchPg(its, p, (byte) (pg - 1));
            }));
        }
        if (item == null || item.getType().getMaxStackSize() != 1) {
        	inv.setItem(12, new ItemBuilder(ItemType.MAGENTA_STAINED_GLASS_PANE).name("§0.").build());
        	inv.setItem(13, new ItemBuilder(ItemType.YELLOW_STAINED_GLASS_PANE).name("§0.").build());
        	inv.setItem(14, new ItemBuilder(ItemType.MAGENTA_STAINED_GLASS_PANE).name("§0.").build());
        	return;
        }
        final ItemMeta im = item.getItemMeta();
        int n = 0;
        final int nn = pg * 4;
        final int enchSize = im.getEnchants().size();
        for (final Enchantment e : ENCHS) {
            if (e.canEnchantItem(item)) {
                n++;
                if (n > nn) {
                    if (n - nn > 4) {
                        its.set(53, ClickableItem.of(new ItemBuilder(ItemType.ACACIA_TRAPDOOR).name("§5===>>").build(), ev -> {
                        	//p.sendMessage(item.toString());
                            shwEnchPg(its, p, (byte) (pg + 1));
                        }));
                        break;
                    } else {
                        final int i = (n - nn) * 9 + 1;
                        int l = im.getEnchantLevel(e);
                    	//Bukkit.broadcast(Component.text("enchant " + e.getKey().getKey() + " l " + l));
                        final int lv = l;
                        
                        final ItemBuilder it = new ItemBuilder(ItemType.ENCHANTED_BOOK)
                        	.name("§e" + StringUtil.nrmlzStr(e.getKey().value()));
                        final LinkedList<String> lr;
                        final int lvlCost = getCost(e);
                        final int nexp = (int) (NumUtil.square(lv + 1) * costDecrease * lvlCost);
                        if (l == 0) {
                            lr = new LinkedList<>(Arrays.asList("§dЛКМ §7-> +1 уровень",
                                l < e.getMaxLevel() ? (p.getTotalExperience() < nexp ? "§7(§cНужно §6"
                                    + (1 + (int) Math.sqrt(nexp * Survivor.LVL_DEL)) + "+§c уровень!§7)"
                                : "§7(§6Достаточно опыта!§7)") : "§7(§5Макс. уровень!§7)",
                                "§8-=-=-=-=-=-=-=-=-=-=-=-"));
                        	/*for (final String sl : eInfo.rusLore) {
                        		lr.add(sl);
                        	}*/
                        	lr.add("§8-=-=-=-=-=-=-=-=-=-=-");
                        	lr.add(costDecrease == 1f ? " " : "§7Цена снижена до: §6"
                                + StringUtil.toSigFigs(costDecrease * 100d, (byte) 1) + "%");
                        	lr.add("§7Шанс проклятия: §c" + StringUtil.toSigFigs(100d * NumUtil.square(enchSize + 1)
                                / (CURSE_CH_MUL * getMtp(item.getType())), (byte) 3) + "%");
                            it.lore(lr);
                        } else {
                        	lr = new LinkedList<>(Arrays.asList("§dЛКМ §7-> +1 уровень",
                                    l < e.getMaxLevel() ? (p.getTotalExperience() < nexp ? "§7(§cНужно §6"
                                        + (1 + (int) Math.sqrt(nexp * Survivor.LVL_DEL)) + "+§c уровень!§7)"
                                    : "§7(§6Достаточно опыта!§7)") : "§7(§5Макс. уровень!§7)", 
                                    "§dПКМ §7-> -1 уровень", 
                                    "§7Возврат §660% §7опыта!", 
                                    "§8-=-=-=-=-=-=-=-=-=-=-=-"));
//                        	for (final String sl : eInfo.rusLore) {
//                        		lr.add(sl);
//                        	}
                        	lr.add("§8-=-=-=-=-=-=-=-=-=-=-");
                        	lr.add(costDecrease == 1f ? " " : "§7Цена снижена до: §6" + StringUtil.toSigFigs(costDecrease * 100d, (byte) 1) + "%");
                        	lr.add("§7Шанс проклятия: §c" + StringUtil.toSigFigs(100f * enchSize * enchSize
                                / (CURSE_CH_MUL * getMtp(item.getType())), (byte) 3) + "%");
                            it.lore(lr);
                        }
                        its.set(i, ClickableItem.of(it.build(), ev -> {
                            for (final Enchantment ex : im.getEnchants().keySet()) {
                                if (!ex.equals(e) && (e.conflictsWith(ex) || ex.conflictsWith(e))) {
                                    return;
                                }
                            }
                            if (ev.getClick() == ClickType.RIGHT) {
                                if (lv > 0) {
                                    final int exp = (int) (lv * lv * lvlCost * DIS_EXP);
                                    final HashMap<Enchantment, Integer> mp = new HashMap<>();
                                    mp.put(e, lv - 1);
                                    final EnchantItemEvent eie = new EnchantItemEvent(p, new InventoryView() {
                                        public InventoryType getType() {return InventoryType.ENCHANTING;}
                                        public void setItem(int i, @Nullable ItemStack it) {inv.setItem(SLOT, it);}
                                        public @Nullable ItemStack getItem(int i) {return inv.getItem(SLOT);}
                                        public void setCursor(@Nullable ItemStack it) {p.setItemOnCursor(it);}
                                        public @NotNull ItemStack getCursor() {return p.getItemOnCursor();}
                                        public @Nullable Inventory getInventory(int i) {return inv;}
                                        public int convertSlot(int i) {return SLOT;}
                                        public @NotNull InventoryType.SlotType getSlotType(int i) {return InventoryType.SlotType.RESULT;}
                                        public void close() {p.closeInventory();}
                                        public int countSlots() {return 1;}
                                        @Deprecated(forRemoval = true)
                                        public boolean setProperty(@NotNull InventoryView.Property property, int i) {return false;}
                                        public Inventory getTopInventory() {return inv;}
                                        public String getTitle() {return "§5§lСтол Чародея";}
                                        public HumanEntity getPlayer() {return p;}
                                        public Inventory getBottomInventory() {return p.getInventory();}
                                        public String getOriginalTitle() {return "§5§lСтол Чародея";}
                                        public void setTitle(final @NotNull String ttl) {}
                                    }, loc.getBlock(), item, exp, mp, e, 1, exp);
                                    eie.callEvent();

                                    if (!eie.isCancelled()) {
                                        im.removeEnchant(e);
                                        if (lv > 1) im.addEnchant(e, lv - 1, true);
                                        item.setItemMeta(im);
                                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1f, 0.8f);
                                        chngExp(p, exp);
                                        inv.setItem(4, item);
                                        shwEnchPg(its, p, pg);
                                    }
                                }
                            } else if (item.getEnchantmentLevel(e) < e.getMaxLevel()) {
                                if (p.getTotalExperience() < nexp) {
                                } else {
                                    final HashMap<Enchantment, Integer> mp = new HashMap<>();
                                    mp.put(e, lv + 1);

                                    final EnchantItemEvent eie = new EnchantItemEvent(p, new InventoryView() {
                                        public InventoryType getType() {return InventoryType.ENCHANTING;}
                                        public void setItem(int i, @Nullable ItemStack it) {inv.setItem(SLOT, it);}
                                        public @Nullable ItemStack getItem(int i) {return inv.getItem(SLOT);}
                                        public void setCursor(@Nullable ItemStack it) {p.setItemOnCursor(it);}
                                        public @NotNull ItemStack getCursor() {return p.getItemOnCursor();}
                                        public @Nullable Inventory getInventory(int i) {return inv;}
                                        public int convertSlot(int i) {return SLOT;}
                                        public @NotNull InventoryType.SlotType getSlotType(int i) {return InventoryType.SlotType.RESULT;}
                                        public void close() {p.closeInventory();}
                                        public int countSlots() {return 1;}
                                        @Deprecated(forRemoval = true)
                                        public boolean setProperty(@NotNull InventoryView.Property property, int i) {return false;}
                                        public Inventory getTopInventory() {return inv;}
                                        public String getTitle() {return "§5§lСтол Чародея";}
                                        public HumanEntity getPlayer() {return p;}
                                        public Inventory getBottomInventory() {return p.getInventory();}
                                        public String getOriginalTitle() {return "§5§lСтол Чародея";}
                                        public void setTitle(final @NotNull String ttl) {}
                                    }, loc.getBlock(), item, nexp, mp, e, 3, nexp);
                                    eie.callEvent();

                                    if (!eie.isCancelled()) {
                                        im.addEnchant(e, lv + 1, true);
                                        item.setItemMeta(im);
                                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1f, 1.6f);
                                        chngExp(p, nexp);
                                        tryAddCrs(p, item);
                                        inv.setItem(4, item);
                                        shwEnchPg(its, p, pg);
                                    }
                                }
                            }
                        }));

                        if (l == 0) {
                            boolean c = true;
                            for (final Enchantment en : item.getEnchantments().keySet()) {
                                if (en.conflictsWith(e) || e.conflictsWith(en)) {
                                    l = 6;
                                    c = false;
                                    for (; l > 0; l--) {
                                        inv.setItem(i + l, new ItemBuilder(inv.getItem(i + l))
                                            .type(ItemType.RED_STAINED_GLASS_PANE).build());
                                    }
                                    break;
                                }
                            }
                            if (costDecrease * lvlCost < p.getTotalExperience() && c) {
                                inv.setItem(i + 1, new ItemBuilder(inv.getItem(i + 1))
                                    .type(ItemType.ORANGE_STAINED_GLASS_PANE).build());
                            }
                        } else {
                            l++;
                            if (costDecrease * lvlCost * l * l < p.getTotalExperience() && l <= e.getMaxLevel()) {
                                inv.setItem(i + l, new ItemBuilder(inv.getItem(i + l))
                                    .type(ItemType.ORANGE_STAINED_GLASS_PANE).build());
                            }
                            for (l--; l > 0; l--) {
                                inv.setItem(i + l, new ItemBuilder(inv.getItem(i + l))
                                    .type(ItemType.LIME_STAINED_GLASS_PANE).build());
                            }
                        }
                    }
                }
            }
        }
    }

    private int getCost(final Enchantment e) {
        final int mxLvl = e.getMaxLevel();
        return (e.getMinModifiedCost(1) + e.getMaxModifiedCost(mxLvl)) / mxLvl;
    }

    /*private void chgCstmEnch(final ItemMeta im, final CustomEnchant en, final int frm, final int to) {
        if (im.hasLore()) {
            final LinkedList<Component> lr = new LinkedList<Component>(im.lore());
            
            if (frm == 0) {
                lr.addFirst(en.disName(to));
            } else if (to == 0) {
            	int cEnchs = 0;
            	for (final Entry<Enchantment, Integer> ent : im.getEnchants().entrySet()) {
            		if (ent.getKey() instanceof CustomEnchant) {
            			if (ent.getKey().equals(CustomEnchant.GLINT)) continue;
            			if (ent.getKey().getKey().equals(en.getKey())) {
            				lr.remove(cEnchs);
            			} else {
            				lr.set(cEnchs, ent.getKey().disName(ent.getValue()));
            			}
            			cEnchs++;
            		}
            	}
            } else {
            	int cEnchs = 0;
            	for (final Entry<Enchantment, Integer> ent : im.getEnchants().entrySet()) {
            		if (ent.getKey() instanceof CustomEnchant) {
            			if (ent.getKey().equals(CustomEnchant.GLINT)) continue;
            			if (ent.getKey().getKey().equals(en.getKey())) {
            				lr.set(cEnchs, ent.getKey().disName(to));
            			} else {
            				lr.set(cEnchs, ent.getKey().disName(ent.getValue()));
            			}
            			cEnchs++;
            		}
            	}
            }
            im.lore(lr);
        } else {
            im.lore(Arrays.asList(en.disName(to)));
        }
    }*/

    private void tryAddCrs(final Player p, final ItemStack it) {
        final Map<Enchantment, Integer> ens = it.getEnchantments();
        if (Main.srnd.nextInt((int) (CURSE_CH_MUL * getMtp(it.getType()))) < ens.size() * ens.size()) {
            final Enchantment en = ClassUtil.rndElmt(CRSS);
            final ItemMeta im = it.getItemMeta();
            final Integer lvl = ens.get(en);
            if (lvl == null) {
                if (!en.canEnchantItem(it)) return;
                im.removeEnchant(en);
                p.playSound(p.getLocation(), Sound.ENTITY_DONKEY_HURT, 1f, 0.6f);
            } else if (lvl < en.getMaxLevel()) {
                im.addEnchant(en, 1, true);
                p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.6f, 1.6f);
            }
        }
    }

    private void chngExp(final Player p, final int chg) {
        int nxp = p.getTotalExperience() + chg;
        p.giveExp(chg);
        final int l = (int) (Math.sqrt((double) nxp / 1.5));
        p.setLevel(l);
        p.setExp((nxp * 2f / 3f - (l * l)) / ((l + 1) * (l + 1) - (l * l)));
    }

    private float getMtp(final Material m) {
        return switch (m) {
            case DIAMOND_SWORD, DIAMOND_PICKAXE, DIAMOND_AXE, DIAMOND_SHOVEL, DIAMOND_HOE,
                 DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS -> 1f;
            case GOLDEN_SWORD, GOLDEN_PICKAXE, GOLDEN_AXE, GOLDEN_SHOVEL, GOLDEN_HOE,
                 GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS -> 1.6f;
            case IRON_SWORD, IRON_PICKAXE, IRON_AXE, IRON_SHOVEL, IRON_HOE,
                 IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS -> 1.2f;
            case WOODEN_SWORD, WOODEN_PICKAXE, WOODEN_AXE, WOODEN_SHOVEL, WOODEN_HOE -> 1f;
            case STONE_SWORD, STONE_PICKAXE, STONE_AXE, STONE_SHOVEL, STONE_HOE -> 0.8f;
            case NETHERITE_SWORD, NETHERITE_PICKAXE, NETHERITE_AXE, NETHERITE_SHOVEL, NETHERITE_HOE,
                 NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> 1.4f;
            case TRIDENT -> 1.6f;
            case CROSSBOW -> 1.2f;
            case TURTLE_HELMET -> 1.4f;
            case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> 1f;
            case CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS -> 1.4f;
            default -> 1f;
        };
    }
    
    @Override
    public void onClose(final Player p, final InventoryContent its) {
//    	p.sendMessage("give");
//    	p.sendMessage(item.toString());
//    	p.sendMessage(Arrays.toString(p.getOpenInventory().getTopInventory().getContents()));
//    	p.sendMessage(Arrays.toString(its.getInventory().getContents()));
    	if (ItemUtil.isBlank(item, false)) return;
    	ItemUtil.giveItemsTo(p, item);
    }
}
