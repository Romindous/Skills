package ru.romindous.skills.menus;

public class CraftMenu /*implements InventoryProvider*/ {/*

    private static final ItemStack[] invIts;
	private static final int rad = 3;
	
    private final String key;
    private final boolean view;

    private Material tp;

    static {
        invIts = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            switch (i) {
                case 13:
                    invIts[13] = new ItemBuilder(ItemType.IRON_NUGGET).name("§7->").build();
                    break;
                case 9:
                    invIts[9] = new ItemBuilder(ItemType.CHEST).name("§dФормированый").build();
                    break;
                default:
                    invIts[i] = new ItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build();
                    break;
            }
        }
    }
    
    
    public CraftMenu(final String key, final boolean view) {
    	this.key = key;
        this.view = view;
        final Recipe rc = Crafts.getRecipe(new NamespacedKey(Crafts.space, key), Recipe.class);
        if (rc instanceof ShapelessRecipe) {
        	tp = ItemType.ENDER_CHEST;
		} else if (rc instanceof FurnaceRecipe) {
        	tp = ItemType.FURNACE;
		} else if (rc instanceof SmokingRecipe) {
        	tp = ItemType.SMOKER;
		} else if (rc instanceof BlastingRecipe) {
        	tp = ItemType.BLAST_FURNACE;
		} else if (rc instanceof CampfireRecipe) {
        	tp = ItemType.CAMPFIRE;
		} else if (rc instanceof SmithingRecipe) {
        	tp = ItemType.SMITHING_TABLE;
		} else if (rc instanceof StonecuttingRecipe) {
        	tp = ItemType.STONECUTTER;
		} else {
        	tp = ItemType.CHEST;
		}
    }
    
    @Override
    public void init(final Player p, final InventoryContent its) {
        final Inventory inv = its.getInventory();
        if (inv != null) inv.setContents(invIts);
        final Recipe rc = Crafts.getRecipe(new NamespacedKey(Crafts.space, key), Recipe.class);
        its.set(9, rc == null ? ClickableItem.of(makeIcon(tp), e -> {
	        switch (tp) {
	            case CHEST:
	            default:
	            	tp = ItemType.ENDER_CHEST;
	                break;
	            case ENDER_CHEST:
	            	tp = ItemType.FURNACE;
	                break;
	            case FURNACE:
	            	tp = ItemType.SMOKER;
	                break;
	            case SMOKER:
	            	tp = ItemType.BLAST_FURNACE;
	                break;
	            case BLAST_FURNACE:
	            	tp = ItemType.CAMPFIRE;
	                break;
	            case CAMPFIRE:
	            	tp = ItemType.SMITHING_TABLE;
	                break;
	            case SMITHING_TABLE:
	            	tp = ItemType.STONECUTTER;
	                break;
	            case STONECUTTER:
	            	tp = ItemType.CHEST;
	                break;
	        }
	        reopen(p, its);
        }) : ClickableItem.empty(makeIcon(tp)));
        its.set(16, view ? ClickableItem.empty(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE)) : 
        	ClickableItem.from(new ItemBuilder(ItemType.GREEN_CONCRETE_POWDER).name("§aГотово!").build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
            	((InventoryClickEvent) e.getEvent()).setCancelled(true);
            }
            final ItemStack rst = inv.getItem(14);
            if (ItemUtil.isBlankItem(rst, false)) {
                p.sendMessage("§cСначала закончите крафт!");
                return;
            }
            
            //запоминание крафта
        	final YamlConfiguration craftConfig = YamlConfiguration.loadConfiguration(new File(Main.configDir.getAbsolutePath() + "/crafts/craft.yml"));
        	craftConfig.set(key, null);
        	craftConfig.set(key + ".result", Crafts.itemToString(rst));
        	craftConfig.set(key + ".world", Main.subServer.toString());
        	craftConfig.set(key + ".type", getRecType(tp));
            final ConfigurationSection cs = craftConfig.getConfigurationSection(key);
            final NamespacedKey nKey = new NamespacedKey(Crafts.space, key);
        	Bukkit.getConsoleSender().sendMessage(cs.getName());
            final Recipe nrc;
            final ItemStack it;
            final String[] shp;
            switch (inv.getItem(9).getType()) {
            case SMOKER:
                it = inv.getItem(11);
                if (it == null || it.getType() == ItemType.AIR) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", Crafts.itemToString(it));
                nrc = new SmokingRecipe(nKey, rst, CMDMatChoice.of(it), 0.5f, 100);
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case BLAST_FURNACE:
                it = inv.getItem(11);
                if (it == null || it.getType() == ItemType.AIR) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", Crafts.itemToString(it));
                nrc = new BlastingRecipe(nKey, rst, CMDMatChoice.of(it), 0.5f, 100);
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case CAMPFIRE:
                it = inv.getItem(11);
                if (it == null || it.getType() == ItemType.AIR) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", Crafts.itemToString(it));
                nrc = new CampfireRecipe(nKey, rst, CMDMatChoice.of(it), 0.5f, 500);
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case FURNACE:
                it = inv.getItem(11);
                if (it == null || it.getType() == ItemType.AIR) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", Crafts.itemToString(it));
                nrc = new FurnaceRecipe(nKey, rst, CMDMatChoice.of(it), 0.5f, 200);
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case SMITHING_TABLE:
                it = inv.getItem(10);
                final ItemStack scd = inv.getItem(12);
                final ItemStack tpl = inv.getItem(2);
                if (ItemUtil.isBlankItem(it, false) || ItemUtil.isBlankItem(scd, false)) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", Crafts.itemToString(it));
                cs.set("recipe.b", Crafts.itemToString(scd));
                cs.set("recipe.c", Crafts.itemToString(tpl));
                nrc = new SmithingTransformRecipe(nKey, rst, CMDMatChoice.of(tpl), CMDMatChoice.of(it), CMDMatChoice.of(scd), false);
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case STONECUTTER:
                it = inv.getItem(11);
                if (it == null || it.getType() == ItemType.AIR) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", Crafts.itemToString(it));
                nrc = new StonecuttingRecipe(nKey, rst, CMDMatChoice.of(it));
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case ENDER_CHEST:
            	final ShapelessRecipe lrs = new ShapelessRecipe(nKey, rst);
                shp = new String[]{"abc", "def", "ghi"};
                for (byte cy = 0; cy < 3; cy++) {
                    for (byte cx = 1; cx < 4; cx++) {
                        final ItemStack ti = inv.getItem(cy * 9 + cx);
                        if (!ItemUtil.isBlankItem(ti, false)) {
                            lrs.addIngredient(CMDMatChoice.of(ti));
                            cs.set("recipe." + String.valueOf(shp[cy].charAt(cx - 1)), Crafts.itemToString(ti));
                        }
                    }
                }
                nrc = lrs;
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case CHEST:
            default://тоже магия
                final ShapedRecipe srs = new ShapedRecipe(nKey, rst);
                final ItemStack[] rcs = new ItemStack[rad*rad];
                int xMin = -1, xMax = -1, yMin = -1, yMax = -1;
                for (int cx = 0; cx < rad; cx++) {
                	for (int cy = 0; cy < rad; cy++) {
                        final ItemStack ti = inv.getItem(cy * 9 + cx + 1);
                        if (!ItemUtil.isBlankItem(ti, false)) {
                        	if (xMin == -1 || xMin > cx) xMin = cx;
                        	if (yMin == -1 || yMin > cy) yMin = cy;
                        	if (xMax < cx) xMax = cx;
                        	if (yMax < cy) yMax = cy;
                        }
                        rcs[cy*rad + cx] = ti;
                    }
                }
                
                if (xMin == -1 || yMin == -1) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                
                shp = makeShape(xMax + 1 - xMin, yMax + 1 - yMin);
                final StringBuffer sb = new StringBuffer(shp.length * (xMax + 1 - xMin));
                for (final String s : shp) {
                	sb.append(":" + s);
                }
                cs.set("shape", sb.substring(1));
                srs.shape(shp);
                
                for (int cx = xMax; cx >= xMin; cx--) {
                	for (int cy = yMax; cy >= yMin; cy--) {
                		final ItemStack ti = rcs[cy*rad + cx];
                        if (!ItemUtil.isBlankItem(ti, false)) {
                        	srs.setIngredient(shp[cy-yMin].charAt(cx-xMin), CMDMatChoice.of(ti));
                            cs.set("recipe." + String.valueOf(shp[cy-yMin].charAt(cx-xMin)), Crafts.itemToString(ti));
                        }
                	}
                }
                nrc = srs;
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(srs);
                break;

            }

            Crafts.crafts.get(Main.subServer).put(nKey, nrc);

			try {
				craftConfig.save(new File(Main.configDir.getAbsolutePath() + "/crafts/craft.yml"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
            p.sendMessage(Main.prefix + "Крафт §4" + key + " §7завершен!");
            p.closeInventory();
        }));
        //final ClickableItem cl = ClickableItem.from(Main.air, e -> e.setCurrentItem(e.getCursor().asOne()));
        final Consumer<ItemClickData> canEdit = e -> {
			if (e.getEvent() instanceof InventoryClickEvent)
				((InventoryClickEvent) e.getEvent()).setCancelled(view);
        };
        switch (tp) {
		    case SMOKER:
		    case BLAST_FURNACE:
		    case CAMPFIRE:
		    case FURNACE:
		        if (rc == null) {
		        	setEditSlot(SlotPos.of(1, 2), null, its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
		        } else {
		        	setEditSlot(SlotPos.of(1, 2), ((CMDMatChoice) ((CookingRecipe<?>) rc).getInputChoice()).getItemStack(), its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
		        }
		        break;
		    case SMITHING_TABLE:
		        if (rc == null) {
		        	setEditSlot(SlotPos.of(0, 2), null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 1), null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 3), null, its, canEdit);
		
		        	setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
		        } else {
		        	setEditSlot(SlotPos.of(0, 2), ((CMDMatChoice) ((SmithingTransformRecipe) rc).getTemplate()).getItemStack(), its, canEdit);
		        	setEditSlot(SlotPos.of(1, 1), ((CMDMatChoice) ((SmithingTransformRecipe) rc).getBase()).getItemStack(), its, canEdit);
		        	setEditSlot(SlotPos.of(1, 3), ((CMDMatChoice) ((SmithingTransformRecipe) rc).getAddition()).getItemStack(), its, canEdit);
		
		        	setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
		        }
		        break;
		    case STONECUTTER:
		        if (rc == null) {
		        	setEditSlot(SlotPos.of(1, 2), null, its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
		        } else {
		        	setEditSlot(SlotPos.of(1, 2), ((CMDMatChoice) ((StonecuttingRecipe) rc).getInputChoice()).getItemStack(), its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
		        }
		        break;
		    case ENDER_CHEST:
		        if (rc == null) {
		        	setEditSlot(SlotPos.of(0, 1), null, its, canEdit);
		        	setEditSlot(SlotPos.of(0, 2), null, its, canEdit);
		        	setEditSlot(SlotPos.of(0, 3), null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 1), null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 2), null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 3), null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 1), null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 2), null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 3), null, its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
		        } else {
		            final Iterator<RecipeChoice> rci = ((ShapelessRecipe) rc).getChoiceList().iterator();
		        	setEditSlot(SlotPos.of(0, 1), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(0, 2), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(0, 3), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 1), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 2), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 3), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 1), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 2), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 3), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
		        }
		        break;
		    case CHEST:
		    default:
		        if (rc == null) {
		        	setEditSlot(SlotPos.of(0, 1), null, its, canEdit);
		        	setEditSlot(SlotPos.of(0, 2), null, its, canEdit);
		        	setEditSlot(SlotPos.of(0, 3), null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 1), null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 2), null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 3), null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 1), null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 2), null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 3), null, its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
		        } else {
		        	final String[] shp = ((ShapedRecipe) rc).getShape();
		        	final Map<Character, RecipeChoice> rcm = ((ShapedRecipe) rc).getChoiceMap();
		        	for (int r = 0; r < rad; r++) {
		        		final String sr = shp.length > r ? shp[r] : "";
			        	for (int c = 0; c < rad; c++) {
			        		final RecipeChoice chs = rcm.get(sr.length() > c ? sr.charAt(c) : 'w');
				        	setEditSlot(SlotPos.of(r, c + 1), chs == null ? Main.air : ((CMDMatChoice) chs).getItemStack(), its, canEdit);
			        	}
		        	}
		        	
		        	setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
		        }
		        break;
		}
    }
    
    private static final String dsp = "abcdefghi";
    private static String[] makeShape(final int dX, final int dY) {
    	final String[] sp = new String[dY];
    	for (int i = 0; i < dY; i++) {
    		sp[i] = dsp.substring(i*dX, i*dX + dX);
    	}
		return sp;
	}



	private void setEditSlot(final SlotPos slot, final ItemStack it, final InventoryContent its, Consumer<ItemClickData> canEdit) {
        its.set(slot, ClickableItem.from(ItemUtil.isBlankItem(it, false) ? Main.air : it, canEdit));
        its.setEditable(slot, !view);
	}
    
    private ItemStack makeIcon(final Material mt) {
        switch (mt) {
            case CHEST:
            default:
            	return new ItemBuilder(ItemType.CHEST).name("§dФормированый").build();
            case ENDER_CHEST:
            	return new ItemBuilder(ItemType.ENDER_CHEST).name("§5Безформенный").build();
            case FURNACE:
            	return new ItemBuilder(ItemType.FURNACE).name("§6Печевой").build();
            case SMOKER:
            	return new ItemBuilder(ItemType.SMOKER).name("§cЗапекающий").build();
            case BLAST_FURNACE:
            	return new ItemBuilder(ItemType.BLAST_FURNACE).name("§7Плавильный").build();
            case CAMPFIRE:
            	return new ItemBuilder(ItemType.CAMPFIRE).name("§eКостерный").build();
            case SMITHING_TABLE:
            	return new ItemBuilder(ItemType.SMITHING_TABLE).name("§fКующий").build();
            case STONECUTTER:
            	return new ItemBuilder(ItemType.STONECUTTER).name("§7Режущий").build();
        }
    }
    
	private String getRecType(final Material m) {
        switch (m) {
            case SMOKER:
                return "smoker";
            case BLAST_FURNACE:
                return "blaster";
            case CAMPFIRE:
                return "campfire";
            case FURNACE:
                return "furnace";
            case SMITHING_TABLE:
                return "smith";
            case STONECUTTER:
                return "cutter";
            case ENDER_CHEST:
                return "noshape";
            case CHEST:
            default:
                return "shaped";
        }
    }


*/}
