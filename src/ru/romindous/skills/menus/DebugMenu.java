package ru.romindous.skills.menus;

public class DebugMenu /*implements InventoryProvider*/ {/*
    
    private static final ItemStack sm;
    private static final List<String>smLore = Arrays.asList("","","","","","","","","","");
    private static final ItemStack bots;
    private static final ItemStack land;
    private static final List<String>landLore = Arrays.asList("","","","","","","","","","");
    private static final List<String>botsLore = Arrays.asList("","","","","","","","","","");
    private static final ItemStack boss;
    private static final List<String>bossLore = Arrays.asList("","","","","","","","","","");
    private static final ItemStack pets;
    private static final List<String>petsLore = Arrays.asList("","","","","","","","","","");
    private static final ItemStack adv;
    private static final List<String>advLore = Arrays.asList("","","","","","","","","","");
    

    static {
        sm = new ItemBuilder(Material.NETHER_STAR)
                .name("§7SM")
                .lore(smLore)
                .build();
        
        land = new ItemBuilder(Material.WARPED_NYLIUM)
                .name("§7Land ")
                .lore(landLore)
                .build();
        
        bots = new ItemBuilder(Material.PLAYER_HEAD)
                .name("§7Боты")
                .lore(botsLore)
                .build();
        
        boss = new ItemBuilder(Material.DRAGON_HEAD)
                .name("§7Боссы " + (Main.bossMgr instanceof Bosses ? "§6(ванилька)" : "§b(мистика)") )
                .lore(bossLore)
                .build();
        
        pets = new ItemBuilder(Material.LEAD)
                .name("§7Питомцы " + (Main.petMgr instanceof PetVanilla ? "§6(ванилька)" : "§b(MyPet)") )
                .lore(petsLore)
                .build();
        
        adv = new ItemBuilder(Material.BEACON)
                .name("§7Ачивки " + (Main.advMgr instanceof AdvanceVanilla ? "§6(ванилька)" : "§b(CrazyAdv)") )
                .lore(advLore)
                .build();
    }
	
    
    
    
    
    
    
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        
         content.set(0, ClickableItem.from(
            sm, e-> {

                }
        ));

        content.set(1, ClickableItem.of(
            land, e-> {
               
            }
        ));

        content.set(2, ClickableItem.of(
            bots, e -> {
                switch (e.getClick()) {
                    case LEFT:
                        if (p.getTargetBlockExact(10)==null) {
                            p.sendMessage("смотри на точку спавна!");
                            PM.soundDeny(p);
                            return;
                        }
                        if (Bots.botListener != null) {
                            PM.getOplayer(p).setup.lastEdit = "DebugBotSpawn";
                            SkillCmd.openDebugMenu(p);
                        } else {
                            PM.soundDeny(p);
                        }
                        break;
                    case SHIFT_RIGHT:
                        if (Bots.botListener != null) {
                            Bots.disable();
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, .3f);
                        } else {
                            PM.soundDeny(p);
                        }
                        break;
                    case SHIFT_LEFT:
                        Bots.init(Main.main);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, .3f);
                        break;
					default:
						break;
                }

            }
        ));

        content.set(3, ClickableItem.of(
            boss, e-> {
                switch (e.getClick()) {
                    case LEFT:
                        PM.getOplayer(p).setup.lastEdit = "DebugBossSpawn";
                        SkillCmd.openDebugMenu(p);
                        break;
                    case RIGHT:
                        if (Main.bossMgr instanceof MythBoss) {
                            PM.getOplayer(p).setup.lastEdit = "DebugMythSpawn";
                            SkillCmd.openDebugMenu(p);
                        }
                        break;
                    case SHIFT_RIGHT:
                        if (Main.bossMgr.isOn()) {
                            Main.bossMgr.disable();
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, .3f);
                        }
                        break;
                    case SHIFT_LEFT:
                        if (!Main.bossMgr.isOn()) {
                            Main.bossMgr.init(Main.main);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, .3f);
                        }
                        break;
					default:
						break;
                }
            }
        ));

        
        content.set(4, ClickableItem.of(
            pets, e-> {
               
            }
        ));

        content.set(5, ClickableItem.of(
            adv, e-> {
               
            }
        ));

        
        
        
        
        
        
        
        
        content.set(8, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e -> {
                PM.getOplayer(p).setup.lastEdit = "";
                p.performCommand("builder");
            }
        ));
        
        
        


    }

    
    
    
        
	@Override
    @SuppressWarnings("deprecation")
    public void update(final Player p, final InventoryContent content) {
        
        sm.setLore(SM.getDebugInfo(smLore));
        content.updateItem(0, sm);
        
        land.setLore(Land.getDebugInfo(landLore));
        content.updateItem(1, land);
        
        bots.setLore(Bots.getDebugInfo(botsLore));
        content.updateItem(2, bots);
        
        boss.setLore(Main.bossMgr.getDebugInfo(bossLore));
        content.updateItem(3, boss);
        
        pets.setLore(Main.petMgr.getDebugInfo(petsLore));
        content.updateItem(4, pets);
        
        adv.setLore(Main.advMgr.getDebugInfo(advLore));
        content.updateItem(5, adv);
        
    }
       
    
    
    
    
*/}
