package ru.romindous.skills.menus;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.romindous.skills.Main;
import ru.romindous.skills.SM;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.enums.Stat;


public class MainMenu implements InventoryProvider {

    private static final ItemStack[] empty;

    private static final ItemStack orange;
    private static final ItemStack red;
    private static final ItemStack vines;
    private static final ItemStack redstone;
    private static final ItemStack shroom;

    private static final ClickableItem ability;
    private static final ClickableItem worlds;
    
    static {
        orange = new ItemBuilder(ItemType.ORANGE_STAINED_GLASS_PANE).name("§0.").build();
        red = new ItemBuilder(ItemType.RED_STAINED_GLASS_PANE).name("§0.").build();
        vines = new ItemBuilder(ItemType.WEEPING_VINES).name("§0.").build();
        redstone = new ItemBuilder(ItemType.REDSTONE_BLOCK).name("§0.").build();
        shroom = new ItemBuilder(ItemType.SHROOMLIGHT).name("§0.").build();
        
        empty = new ItemStack[54];
        for (int i = 0; i < 54; i++) {
            if (i / 9 == 0) {
                empty[i] = (i & 1) == 0 ? orange : red;//new ItemBuilder((i & 1) == 0 ? ItemType.ORANGE_STAINED_GLASS_PANE : ItemType.RED_STAINED_GLASS_PANE).name("§0.").build();
            } else {
                switch (i % 9) {
                    case 0:
                    case 8:
                        empty[i] = vines; //ew ItemBuilder(ItemType.WEEPING_VINES).name("§0.").build();
                        break;
                    default:
                        break;
                }
            }
        }
        empty[0] = redstone;                      empty[8] = redstone;
        empty[45] = shroom;                       empty[53] = shroom;
        
        worlds = ClickableItem.from(new ItemBuilder(ItemType.RAW_COPPER_BLOCK).name("§4<obf>k</obf>§6 Мировое Смещение §4<obf>k")
            .lore("§6ЛКМ §7- открытые миры")
            .build(), e -> {
                if (e.getEvent() instanceof final InventoryClickEvent ev) {
                    ((Player) ev.getWhoClicked()).performCommand("skill world");
                }
        });
        
        ability = ClickableItem.from(new ItemBuilder(ItemType.SWEET_BERRIES).name("§4<obf>k</obf>§c Кластер Навыков §4<obf>k")
            .lore("§6ЛКМ §7- навыки класса")
            .build(), e -> {
                if (e.getEvent() instanceof final InventoryClickEvent ev) {
                    ((Player) ev.getWhoClicked()).performCommand("skill ability");
                }
        });
    }

    
    
    

    @Override
    public void init(final Player p, final InventoryContent content) {


        p.playSound(p.getEyeLocation(), Sound.ITEM_BOOK_PAGE_TURN, 2f, 1f);
        p.playSound(p.getEyeLocation(), Sound.BLOCK_WEEPING_VINES_STEP, 0.6f, 0.6f);
        
        final Survivor sv = PM.getOplayer(p, Survivor.class);

        if (content.getInventory() != null) {
            content.getInventory().setContents(empty);
        }

        content.set(13, ability);
        /*content.set(13, ClickableItem.of(new ItemBuilder(ItemType.FIRE_CORAL)
                .name("§4<obf>k</obf>§c Древо Навыков §4<obf>k")
                .lore("§6ЛКМ §7- навыки класса")
                .build(), e -> {
                    //p.closeInventory();
            //if (e.isLeftClick()) {
                p.performCommand("skill ability");
            //} else {
            //    p.performCommand("sedna skill ВСЕ");
            //}
        }));   */     
        
        
        
        content.set(20, ClickableItem.from(new ItemBuilder(ItemType.TOTEM_OF_UNDYING).name("§6<obf>k</obf>§e Статистика §6<obf>k")
            .lore("")
            .lore(sv.statsPoints >0 ? "§f§lДоступно очков" : "§8Доступно очков")
            .lore(sv.statsPoints >0 ? "§f§lстатистики : §b§l"+sv.statsPoints : "§8статистики : §70")
            .lore("")
            .lore("§6ЛКМ §7- статистика")
            .build(), e -> {
                p.performCommand("skill stats");
        }));
        
        

        content.set(24, worlds);
        
        content.set(31, Main.petMgr.getMenuItem(p));
        

        //табло
        content.set(38, ClickableItem.from(new ItemBuilder(sv.showScoreBoard ? ItemType.GLOW_ITEM_FRAME : ItemType.ITEM_FRAME).name("§7Отображение Табло")
                .lore("")
                .lore(sv.showScoreBoard ? "§aВключено" : "§5Выключено")
                .lore(sv.showScoreBoard ? "§7ЛКМ - выключить" : "§7ЛКМ - включить")
                .lore("")
                .build(), e -> {
                	if (e.getEvent() instanceof InventoryClickEvent) {
                        if (((InventoryClickEvent) e.getEvent()).isLeftClick()) {
                            if (sv.showScoreBoard) {
                                sv.showScoreBoard = false;
                                sv.score.getSideBar().reset();
                            } else {
                                sv.showScoreBoard = true;
                                sv.updateBoard(p, SM.infoType.ALL);
                            }
            				reopen(p, content);
                            //content.getInventory().getItem(38)
                        }
                	}
        }));         
        
//табло
        content.set(39, ClickableItem.from(new ItemBuilder(sv.showActionBar ? ItemType.GLOW_ITEM_FRAME : ItemType.ITEM_FRAME).name("§7Отображение Строки")
            .lore("")
            .lore(sv.showActionBar ? "§aВключено" : "§5Выключено")
            .lore(sv.showActionBar ? "§7ЛКМ - выключить" : "§7ЛКМ - включить")
            .lore("")
            .build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    if (((InventoryClickEvent) e.getEvent()).isLeftClick()) {
//                            sv.abShowTime = 0;
//                            sv.abShowTime = 20;
                        sv.showActionBar = !sv.showActionBar;
                        reopen(p, content);
                    }
                }
        }));
        
        
        
        
        
        
        
        /*content.set(42, ClickableItem.from(new ItemBuilder(ItemType.WRITABLE_BOOK).name("§c<obf>k</obf>§e Книга Изделий §c<obf>k")
            .lore("")
            .lore("§6ЛКМ §7- посмотреть крафты")
            .lore("")
            .build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    if (((InventoryClickEvent) e.getEvent()).isLeftClick()) {
                        p.closeInventory();
                        p.performCommand("craft book");
                    }
                }
        }));*/
        
        
        final List<String> lore = new ArrayList<>();
        final int level = sv.getLevel();
        lore.add("§7Уровень: §f"+ level);
        lore.add(StringUtil.getPercentBar(20, level, false));
        lore.add("");
        lore.add("§7Души: §3" + sv.mana.intValue());
        lore.add("§7Очки статы: §6" + sv.statsPoints);
        lore.add("");
        lore.add("    §8Статистика");
        for (Stat st : Stat.values()) {
            lore.add(st.getName()+(sv.role.stat==st?": §l":": ")+sv.getStat(st));
        }
        lore.add("");
        
        final int timeLeft = ApiOstrov.isLocalBuilder(p) ? 0 : 86400 - (ApiOstrov.currentTimeSec()-sv.roleStamp);
        //p.sendMessage("tm=" + timeLeft);
        if (timeLeft>0) {
            
            lore.add("§7До смены класса:");
            lore.add("§c"+ TimeUtil.secondToTime(timeLeft));
            content.set(49, ClickableItem.empty(new ItemBuilder(ItemType.CAMPFIRE)
                .name("§c<obf>k</obf> "+sv.role.getName()+" §c<obf>k") //.name("§c<obf>k</obf>§6 Статистика Игры §c<obf>k")
                .lore(lore)
                .build()
                )
            );
            
        } else {
            
            if (PM.getOplayer(p).hasGroup("legend")) {
                lore.add("§6ЛКМ §7- сменить без штрафа");
            } else {
                lore.add("§6ЛКМ §7- сменить с потерей 50%");
                lore.add("§8*(легенда меняет без штрафа)");
            }
            
            content.set(49, ClickableItem.from(new ItemBuilder(ItemType.CAMPFIRE)
                    .name("§c<obf>k</obf> "+sv.role.getName()+" §c<obf>k") //.name("§c<obf>k</obf>§6 Статистика Игры §c<obf>k")
                    .lore(lore)
                    .build(), e-> {
                        p.performCommand("skill select");
                    }
                )
            );            
        }
        


        
        
        
    }



    
    
}
