package ru.romindous.skills.menus;

public class DebugMythSpawn /*implements InventoryProvider*/ {/*
    
    private static final ClickableItem[] ci;
    private static ActiveMob debugMob;
    private static int page;
    public static ModeledEntity modeledEntity;
    public static final List<String> modelNames; //для смены модельки по клику
    public static int modelIndex = -1;
    
    static {
        final  ArrayList<ClickableItem> menuEntry = new ArrayList<>(); 
        final SortedSet <String> types = new TreeSet<>();
        
        for (MythicMob mm : MythBoss.mobMgr.getMobTypes()) {
            types.add(mm.getInternalName());
        }
        
        for (final String mmob : types) {
            final MythicMob mm = MythBoss.mobMgr.getMythicMob(mmob).get();
            
            menuEntry.add( ClickableItem.from(
                    new ItemBuilder(ItemType.PIG_SPAWN_EGG)
                        .name(mm.getInternalName())
                        .lore("§bMythicMobs")
                        .lore(mm.getdisName()==null ? "§8nodisName" : mm.getdisName().get())
                        .lore(mm.getHealth()==null ? "§8noHealth" :"§7health="+mm.getHealth().get())
                        .lore(mm.getDamage()==null ? "§8noDamage" :"§7damage="+mm.getDamage().get())
                        .lore("§7")
                        .lore("§7")
                        .lore("§7")
                        .lore("§7")
                        .lore("§7ЛКМ - §aспавн")
                        .build(), e-> {
                            if (e.getClick()==ClickType.LEFT) {
                                spawn(e.getPlayer(), mm);
                            } else if (e.getClick()==ClickType.RIGHT) {
                            }
                        }
            ));
        }
        
        types.clear();
        //for ( String modelName : ModelEngineAPI.api.getModelManager().getModelRegistry().getRegisteredModel().keySet()) {
            //types.addAll(MythModel.modelMgr.getModelRegistry().getRegisteredModel().keySet());
        types.addAll(ModelEngineAPI.api.getModelRegistry().getAllBlueprintId());
        //}
        modelNames = new ArrayList<String>(types);
        
        //boolean current;
        for (final String modelName : modelNames) {
//Ostrov.log("idx="+idx+" index="+modelNames.indexOf(modelName));
            //current = idx == modelNames.indexOf(modelName); //modelNames.get(idx)!=null && modelNames.get(idx).equals(modelName);
        
            menuEntry.add(ClickableItem.from(new ItemBuilder(ItemType.RABBIT_HIDE)
                    .name(modelName)
                    .lore("§dModel")
                    .lore("§7")
                    //.lore(current ? "§6Выбрана" : "§7ЛКМ - §aспавн")
                    .lore("§7ЛКМ - §aспавн")
                    .build(), e-> {
                        if (e.getClick()==ClickType.LEFT) {
                            setModel(e.getPlayer(), modelName);
                        }
                    }
            ));
        }  
        
        ci = menuEntry.toArray(new ClickableItem[menuEntry.size()]);
            
    }

    
    
    
    
    
    
    
    
    
    
    
    public static void setModel(final Player p, final String modelName) {
//Ostrov.log("setModel "+modelName+" ActiveModel="+model);
        modelIndex = modelNames.indexOf(modelName);
        p.closeInventory();
        
        if (modeledEntity==null) {
            final Entity mob = p.getWorld().spawnEntity(p.getLocation().add(0, 2, 0), EntityType.HUSK, CreatureSpawnEvent.SpawnReason.CUSTOM);
            mob.setGravity(false);
            mob.setInvulnerable(true);
            mob.setSilent(true);
            ((LivingEntity)mob).setAI(false);
            modeledEntity = ModelEngineAPI.createModeledEntity(mob);
            modeledEntity.setBaseEntityVisible(false);
            p.sendMessage("§f*§bЛКМ на модель рукой - §eследующая§b, предметом - §eпредыдущая");
        } else {
            modeledEntity.destroy();
            //modeledEntity.clearModels();
        }
        ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(modelName);
        final ActiveModel activeModel = ModelEngineAPI.createActiveModel(blueprint);
        modeledEntity.addModel(activeModel, true);
        //modeledEntity.addActiveModel(model);
        //modeledEntity.detectPlayers();
        //modeledEntity.setInvisible(true);
        //modeledEntity.setWalking(false);    
    }
    
    
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, .3f);
        final SetupMode setupMode = PM.getOplayer(p).setup;
        setupMode.lastEdit = "DebugMythSpawn";
        
        
        int from = page*44;
        int to = page*44+45;
        if (to>=ci.length) to=ci.length;
        
        int index;
        
        for (int i=from; i < to; i++) {
            final ClickableItem menuItem = ci[i];
            if (menuItem.getItem().getType()==ItemType.RABBIT_HIDE || menuItem.getItem().getType()==ItemType.SLIME_BALL) {
                index = modelNames.indexOf(((TextComponent) menuItem.getItem().getItemMeta().disName()).content());
                if (index>=0) {
                    if (index==modelIndex) {
                        menuItem.getItem().setType(ItemType.SLIME_BALL);
                    } else if (menuItem.getItem().getType()==ItemType.SLIME_BALL) {
                        menuItem.getItem().setType(ItemType.RABBIT_HIDE);
                    }
                }
            } 
            
            //menuItem.getItem().setType(ItemType.STONE);
            content.add(menuItem);
        }    
            
       *//* Pagination pagination = content.pagination();
        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(45); 
        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));        
        pagination.page(page);*//*
        
        
        
       if (to<ci.length) {
            content.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e -> {
                    page++;
                    reopen(p, content);
                })
            );
        }

       if (page>0) {
            content.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e -> {
                    page--;
                    reopen(p, content);
                })
            );
        }

        
        
        content.set(5, 4, ClickableItem.of(new ItemBuilder(ItemType.OAK_DOOR).name("назад").build(), e -> {
                PM.getOplayer(p).setup.lastEdit = "Debug";
                SkillCmd.openDebugMenu(p);
            }
        ));
        
        if (debugMob!=null || modeledEntity!=null) {
            content.set(5, 6, ClickableItem.of(new ItemBuilder(ItemType.REDSTONE).name("Убрать моба").build(), e -> {
                if (debugMob!=null) {
                    debugMob.remove();
                    debugMob = null;
                }
                if (modeledEntity!=null) {
                    modeledEntity.destroy();
                    //modeledEntity.clearModels();
                    ((Entity)modeledEntity.getBase().getOriginal()).remove();
                    //modeledEntity.getEntity().remove();
                    modeledEntity = null;
                }
                    reopen(p, content);
                }
            ));
        }


    }


    
    
    private static void spawn(final Player p, final MythicMob mm) {
        p.closeInventory();
        if (debugMob!=null) {
            debugMob.remove();
        }
        final AbstractLocation location = BukkitAdapter.adapt(p.getLocation().add(0, 1, 0));
        debugMob = mm.spawn(location, 0, SpawnReason.SUMMON);
    }
    
*/}
