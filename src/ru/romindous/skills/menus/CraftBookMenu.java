package ru.romindous.skills.menus;

public class CraftBookMenu /*implements InventoryProvider*/ {/*
    
    private ItemStack recType;
    //private ItemStack world;
    private int page;
    
    private static final int pgSize = 36;
    private static final ItemStack[] empty = getEmptInv(36 + 18);
    private static final Recipe[] none = new Recipe[0];
    
    public CraftBookMenu() {
    	recType = new ItemBuilder(ItemType.BEACON).name("§bВсе Типы Крафтов").lore("§bЛКМ §7- отсортировать по", "§7другому способу крафта").build();
    	//world = new ItemBuilder(ItemType.PEARLESCENT_FROGLIGHT).name("§aВсе Миры").lore("§aЛКМ §7- сменить мир").build();
    	page = 0;
	}

	@Override
    public void init(final Player p, final InventoryContent its) {
    	final Inventory inv = its.getInventory();
    	if (inv != null) {
        	inv.setContents(empty);
    	}
    	
    	its.set(1, ClickableItem.from(recType, e -> {
			final ItemMeta im;
    		switch (recType.getType()) {
			case BEACON:
			default:
				recType.setType(ItemType.CHEST);
				im = recType.getItemMeta();
				im.displayName(Component.text("§dФормированые"));
				break;
			case CHEST:
				recType.setType(ItemType.ENDER_CHEST);
				im = recType.getItemMeta();
				im.displayName(Component.text("§5Безформенные"));
				break;
			case ENDER_CHEST:
				recType.setType(ItemType.FURNACE);
				im = recType.getItemMeta();
				im.displayName(Component.text("§6Печевые"));
				break;
			case FURNACE:
				recType.setType(ItemType.SMITHING_TABLE);
				im = recType.getItemMeta();
				im.displayName(Component.text("§fКующие"));
				break;
			case SMITHING_TABLE:
				recType.setType(ItemType.STONECUTTER);
				im = recType.getItemMeta();
				im.displayName(Component.text("§7Режущие"));
				break;
			case STONECUTTER:
				recType.setType(ItemType.BEACON);
				im = recType.getItemMeta();
				im.displayName(Component.text("§bВсе Типы Крафтов"));
				break;
			}
    		recType.setItemMeta(im);
    		page = 0;
    		reopen(p, its);
    	}));
    	
    	its.set(3, ClickableItem.empty(new ItemBuilder(Main.subServer.displayMat).name(Main.subServer.displayName).build()));
    	
    	final HashSet<ItemStack> results = new HashSet<>();
    	final Material craftMat = recType.getType();
    	final LinkedList<Recipe> recList = new LinkedList<>();
    	for (final Entry<SubServer, Map<NamespacedKey, Recipe>> en : Crafts.crafts.entrySet()) {
    		if (en.getKey().ordinal() > Main.subServer.ordinal() && !ApiOstrov.isLocalBuilder(p, false)) continue;
    		if (craftMat == ItemType.BEACON) {
            	for (final NamespacedKey key : en.getValue().keySet()) {
            		//p.sendMessage(key.getKey());
            		final Recipe rec = Bukkit.getRecipe(key);
            		if (rec != null && results.add(rec.getResult().asOne())) {
            			recList.add(rec);
            		}
            	}
    		} else {
            	for (final NamespacedKey key : en.getValue().keySet()) {
            		//p.sendMessage(key.getKey());
            		final Recipe rec = Bukkit.getRecipe(key);
            		if (rec != null && getMatForType(rec) == craftMat && results.add(rec.getResult().asOne())) {
            			recList.add(rec);
            		}
            	}
			}
    	}
    	
    	final Recipe[] recs = recList.toArray(none);
    	final int startRec = page * pgSize;
    	if (recs.length > startRec) {
    		if (page != 0) {
    			its.set(pgSize + 10, ClickableItem.from(new ItemBuilder(ItemType.SPRUCE_PRESSURE_PLATE).name("§eКлик §7- пред. страница").build(), e -> {
    				page--;
    				reopen(p, its);
    			}));
    		}

        	if (recs.length > startRec + pgSize) {
    			its.set(pgSize + 16, ClickableItem.from(new ItemBuilder(ItemType.OAK_PRESSURE_PLATE).name("§eКлик §7- след. страница").build(), e -> {
    				page++;
    				reopen(p, its);
    			}));
        		for (int ri = pgSize - 1; ri >= 0; ri--) {
        			final Recipe rc = recs[startRec + ri];
        			its.set(ri + 9, ClickableItem.from(rc.getResult().asOne(), e -> {
        				p.closeInventory();
        				p.performCommand((ApiOstrov.isLocalBuilder(p, false) ? "craft edit " : "craft view ") + getKeyForType(rc, ItemType.BEACON).getKey());
        			}));
        		}
        	} else {
        		for (int ri = recs.length - startRec - 1; ri >= 0; ri--) {
        			final Recipe rc = recs[startRec + ri];
        			its.set(ri + 9, ClickableItem.from(rc.getResult().asOne(), e -> {
        				p.closeInventory();
        				p.performCommand((ApiOstrov.isLocalBuilder(p, false) ? "craft edit " : "craft view ") + getKeyForType(rc, ItemType.BEACON).getKey());
        			}));
        		}
			}
    	}
    }

	private static NamespacedKey getKeyForType(final Recipe rp, final Material tp) {
    	if (tp == ItemType.BEACON) {
    		if (rp instanceof CookingRecipe) {
    			return ((CookingRecipe<?>) rp).getKey();
    		} else if (rp instanceof ShapedRecipe) {
    			return ((ShapedRecipe) rp).getKey();
    		} else if (rp instanceof ShapelessRecipe) {
    			return ((ShapelessRecipe) rp).getKey();
    		} else if (rp instanceof StonecuttingRecipe) {
    			return ((StonecuttingRecipe) rp).getKey();
    		} else if (rp instanceof SmithingRecipe) {
    			return ((SmithingRecipe) rp).getKey();
    		}
    	} else {
    		if (rp instanceof CookingRecipe) {
    			switch (tp) {
				case SMOKER, BLAST_FURNACE, CAMPFIRE, FURNACE:
	    			return ((CookingRecipe<?>) rp).getKey();
				default:
					return null;
				}
    		} else if (rp instanceof ShapedRecipe && tp == ItemType.CHEST) {
    			return ((ShapedRecipe) rp).getKey();
    		} else if (rp instanceof ShapelessRecipe && tp == ItemType.ENDER_CHEST) {
    			return ((ShapelessRecipe) rp).getKey();
    		} else if (rp instanceof StonecuttingRecipe && tp == ItemType.STONECUTTER) {
    			return ((StonecuttingRecipe) rp).getKey();
    		} else if (rp instanceof SmithingRecipe && tp == ItemType.SMITHING_TABLE) {
    			return ((SmithingRecipe) rp).getKey();
    		}
		}
		return null;
	}

	private static Material getMatForType(final Recipe rp) {
		if (rp instanceof CookingRecipe) {
			return ItemType.FURNACE;
		} else if (rp instanceof ShapedRecipe) {
			return ItemType.CHEST;
		} else if (rp instanceof ShapelessRecipe) {
			return ItemType.ENDER_CHEST;
		} else if (rp instanceof StonecuttingRecipe) {
			return ItemType.STONECUTTER;
		} else if (rp instanceof SmithingRecipe) {
			return ItemType.SMITHING_TABLE;
		}
		return null;
	}

	private static ItemStack[] getEmptInv(final int size) {
    	final ItemStack[] its = new ItemStack[size];
    	for (int i = 0; i < size; i++) {
    		if (i < 9) {
    			its[i] = new ItemBuilder((i & 1) == 1 ? ItemType.BROWN_STAINED_GLASS_PANE : ItemType.YELLOW_STAINED_GLASS_PANE).name("§0.").build();
    		} else if (i > size - 10) {
    			its[i] = new ItemBuilder((i & 1) == 0 ? ItemType.BROWN_STAINED_GLASS_PANE : ItemType.YELLOW_STAINED_GLASS_PANE).name("§0.").build();
			}
    	}
		return its;
	}
    
*/}
