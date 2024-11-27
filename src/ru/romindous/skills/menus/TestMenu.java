package ru.romindous.skills.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class TestMenu implements InventoryProvider {
    
    private static InventoryType current;
    
	
    @Override
    public void init(final Player p, final InventoryContent content) {

        if (current==null || current==InventoryType.CHEST) {
            
            for (final InventoryType it : InventoryType.values()) {

                content.add( ClickableItem.from(new ItemBuilder(ItemType.SLIME_BALL)
                    .name(it.name())
                    .lore("§fклик - открыть в этом типе")
                    .build(), e-> {
                        current = it;
                        SmartInventory.builder()
                            .id("TestMenu "+it.name())
                            .provider(new TestMenu())
                            //.size(6, 9)
                            .title("§b§l"+it.name())
                            .type(it)
                            .build()
                            .open(p);
                    }
                ));

            }
            
        } else {
            
            content.add( ClickableItem.from(new ItemBuilder(ItemType.SLIME_BALL)
                .name("назад")
                .build(), e-> {
                    current = InventoryType.CHEST;
                    SmartInventory.builder()
                        .id("TestMenu ")
                        .provider(new TestMenu())
                        //.size(6, 9)
                        .title("§b§lTestMenu")
                        .type(current)
                        .build()
                        .open(p);
                }
            ));
            
        }
        
        /*for (final InventoryType it : InventoryType.values()) {

            content.add( ClickableItem.of(new ItemBuilder(ItemType.SLIME_BALL)
                .name(it.name())
                .lore("§fклик - открыть в этом типе")
                .build(), e-> {
                    SmartInventory.builder()
                        .id("TestMenu "+it.name())
                        .provider(new TestMenu())
                        //.size(6, 9)
                        .title("§b§l"+it.name())
                        .type(it)
                        .build()
                        .open(p);
                }
            ));

        }*/


    }

        
 
}
