package ru.romindous.skills.menus;

public class DebugBotSpawn /*implements InventoryProvider*/ {/*
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, .3f);
        final SetupMode setupMode = PM.getOplayer(p).setup;
        setupMode.lastEdit = "DebugBotSpawn";
        
        for (final BotType bt : BotType.values()) {

            content.add( ClickableItem.from(
                new ItemBuilder(ItemType.FIREWORK_STAR)
                    .name(bt.name())
                    .build(), e-> {
                        p.closeInventory();
                        Bots.spawn(p.getTargetBlockExact(10).getLocation(), bt);
                    }
            ));

        }



        
        
        content.set(5, 4, ClickableItem.of(new ItemBuilder(ItemType.OAK_DOOR).name("назад").build(), e -> {
                PM.getOplayer(p).setup.lastEdit = "Debug";
                SkillCmd.openDebugMenu(p);
            }
        ));
        



    }

    
*/}
