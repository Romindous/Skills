package ru.romindous.skills.menus;

public class DebugBossSpawn /*implements InventoryProvider*/ {/*
	
    @Override
    public void init(final Player p, final InventoryContent content) {

        for (final BossType bt : BossType.values()) {

                content.add( ClickableItem.from(
                        new ItemBuilder(Material.FIREWORK_STAR)
                            .name(bt.displayName)
                            .build(), e-> {
                                p.closeInventory();
                                Main.bossMgr.spawn(p, p.getLocation(), bt, null);
                            }
                ));


        }

        content.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e -> {
                PM.getOplayer(p).setup.lastEdit = "Debug";
                SkillCmd.openDebugMenu(p);
            }
        ));
        

    }
      
    
    
*/}
