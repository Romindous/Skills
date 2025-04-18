package ru.romindous.skills.menus;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Timer;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.skills.guides.Section;
import ru.romindous.skills.survs.SM;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.survs.Survivor;


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
            .lore("§6Клик §7- открытые миры")
            .build(), e -> {
            if (e.getEvent() instanceof final InventoryClickEvent ev) {
                ((Player) ev.getWhoClicked()).performCommand("skill world");
            }
        });

        ability = ClickableItem.from(new ItemBuilder(ItemType.SWEET_BERRIES).name("§4<obf>k</obf>§c Кластер Навыков §4<obf>k")
            .lore("§6Клик §7- навыки класса")
            .build(), e -> {
            if (e.getEvent() instanceof final InventoryClickEvent ev) {
                if (ev.getWhoClicked() instanceof final Player p) {
                    final Survivor sv = PM.getOplayer(p, Survivor.class);
                    if (sv == null || sv.role == null) {
                        RoleMenu.skillSelect.open(p);
                        return;
                    }
                    sv.skillInv.open(p);
                }
            }
        });
    }

    public static void open(final Player p) {
        SmartInventory.builder()
            .id("Menu"+p.getName())
            .provider(new MainMenu())
            .size(6, 9)
            .title("          §c§lГлавное Меню")
            .build()
            .open(p);
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

        content.set(20, ClickableItem.of(new ItemBuilder(ItemType.TOTEM_OF_UNDYING).name("§6<obf>k</obf>§e Статистика §6<obf>k")
            .lore("")
            .lore(sv.statsPoints >0 ? "§f§lДоступно очков" : "§8Доступно очков")
            .lore(sv.statsPoints >0 ? "§f§lстатистики : §b§l"+sv.statsPoints : "§8статистики : §70")
            .lore("")
            .lore("§6Клик §7- статистика")
            .build(), e -> {
            if (sv.role ==null) {
                RoleMenu.skillSelect.open(p);
                return;
            }
            SmartInventory.builder()
                .type(InventoryType.DISPENSER)
                .id("Stats "+p.getName())
                .provider(new StatsMenu())
                .title("§3§l   Прокачка Статистики")
                .build().open(p);
            p.performCommand("skill stats");
        }));



        content.set(24, worlds);

//        content.set(31, Main.petMgr.getMenuItem(p));
        content.set(31, ClickableItem.from(Section.jrIt(sv), e -> Section.journal(p, sv)));

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
                        sv.score.getSideBar().reset().title("");
                    } else {
                        sv.showScoreBoard = true;
                        sv.updateBoard(p, SM.Info.ALL);
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
            .lore("§6Клик §7- посмотреть крафты")
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
        lore.add("");
        lore.add("§7Души: §3" + (int) sv.mana());
        lore.add("§7Очки статы: §6" + sv.statsPoints);
        lore.add("");
        for (Stat st : Stat.values()) {
            lore.add(st.disName()+(sv.role.stat==st?": §l":": ")+sv.getStat(st));
        }
        lore.add("");

        final int timeLeft = ApiOstrov.isLocalBuilder(p) ? 0 : 86400 - (Timer.secTime()-sv.roleStamp);
        //p.sendMessage("tm=" + timeLeft);
        if (timeLeft>0) {
            lore.add("§7До смены класса:");
            lore.add("§c"+ TimeUtil.secondToTime(timeLeft));
            content.set(49, ClickableItem.empty(new ItemBuilder(ItemType.CAMPFIRE)
                .name("§c<obf>k</obf> "+sv.role.disName()+" §c<obf>k").lore(lore).build()));
        } else {
            if (Perm.isRank(sv, 1)) {
                lore.add("§6Клик §7- сменить без штрафа");
            } else {
                lore.add("§6Клик §7- сменить с потерей опыта");
                lore.add("§8*(Легенда - меняет без штрафа)");
            }

            content.set(49, ClickableItem.from(new ItemBuilder(ItemType.CAMPFIRE)
                .name("§c<obf>k</obf> "+sv.role.disName()+" §c<obf>k").lore(lore).build(), e -> {
                    if (e.getClick().isLeftClick()) p.performCommand("skill select");
            }));
        }
    }
}
