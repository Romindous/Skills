package ru.romindous.skills.guides;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.quests.Quest;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.skills.survs.Survivor;

public enum Section {

    CRAFTS(ItemType.GLOBE_BANNER_PATTERN, "<beige>Усвоение Ремесел",
        ItemType.CYAN_STAINED_GLASS_PANE, ItemType.MAGENTA_STAINED_GLASS_PANE),
    SKILLS(ItemType.FLOW_BANNER_PATTERN, "<indigo>Создание Навыков",
        ItemType.CYAN_STAINED_GLASS_PANE, ItemType.MAGENTA_STAINED_GLASS_PANE),
    MOBS(ItemType.SKULL_BANNER_PATTERN, "<amber>Наблюдения о Сущностях",
        ItemType.CYAN_STAINED_GLASS_PANE, ItemType.MAGENTA_STAINED_GLASS_PANE),
    MATS(ItemType.FIELD_MASONED_BANNER_PATTERN, "<mithril>Изготовление Материалов",
        ItemType.CYAN_STAINED_GLASS_PANE, ItemType.MAGENTA_STAINED_GLASS_PANE);

    private static final ItemStack journal = new ItemBuilder(ItemType.BOOK).name("<stale><obf>k</obf> " + TCUtil.A + "Твои Заметки <stale><obf>k").build();
//        .lore(TCUtil.N + "Записи полезных знаний,", TCUtil.N + "собранных на твоем пути").lore("").build();

    private final ItemStack it;
    private final String disName;
    private final ItemStack fst, scd;
    Section(final ItemType icon, final String name, final ItemType fst, final ItemType scd) {
        this.it = new ItemBuilder(icon).name(name).build();
        this.disName = name;
        this.fst = fst.createItemStack();
        this.scd = scd.createItemStack();
    }

    public Quest parent() {
        return switch (this) {
            case CRAFTS -> Entries.table;
            case SKILLS -> Entries.trig;
            case MOBS -> Entries.mob;
            case MATS -> Entries.iron;
        };
    }

    public void open(final Player p, final Survivor sv) {
        sv.section = this;
        SmartInventory.builder().title(TCUtil.A + "    " + disName)
            .id("Section " + p.getName()).size(6, 9).provider((pl, its) -> {
                pl.playSound(pl, Sound.ITEM_ARMOR_EQUIP_WOLF, 1f, 0.6f);
                final Inventory inv = its.getInventory();
                final ClickableItem cFst = ClickableItem.empty(fst), cScd = ClickableItem.empty(scd);
                for (int i = inv.getSize() - 1; i >= 0; i--) {
                    if (i > 44 || switch (i % 9) {case 0, 8 -> true; default -> false;}) {
                        its.set(i, (i & 1) == 0 ? cFst : cScd);
                    }
                }

                its.set(inv.getSize() - 5, ClickableItem.of(it, e -> {
                    sv.section = null;
                    journal(pl, sv);
                }));

                for (final Entry en : Entry.getAll(this))
                    if (en.isComplete(sv)) its.add(en.item(pl, sv));
            }).build().open(p);
    }

    public static ItemStack jrIt(final Survivor sv) {
        final boolean urd = !sv.unread.isEmpty();
        return new ItemBuilder(journal).glint(urd).lore(urd ?
            TCUtil.A + "Клик §7- есть новые записи!" : "§7Клик - пролистать журнал").build();
    }

    public static void journal(final Player p, final Survivor sv) {
        if (sv.section != null) {
            sv.section.open(p, sv);
            return;
        }
        final Set<Section> unops = EnumSet.noneOf(Section.class);
        for (final Entry en : sv.unread)
            if (en.sec != null) unops.add(en.sec);
        SmartInventory.builder().id("Journal " + p.getName())
            .title(TCUtil.A + "        <stale><b>Твои Заметки")
            .type(InventoryType.HOPPER).provider((pl, its) -> {
                pl.playSound(pl, Sound.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.6f);
                its.set(2, ClickableItem.from(new ItemBuilder(sv.role.getIcon())
                    .name(TCUtil.sided(TCUtil.P + "Главное Меню", "🢙")).deLore().build(), e -> {
                    pl.performCommand("skill");
                }));
                for (final Section sc : Section.values()) {
                    if (sc.parent().isComplete(sv)) {
                        its.add(ClickableItem.of(new ItemBuilder(sc.it)
                            .glint(unops.contains(sc)).build(), e -> sc.open(pl, sv)));
                        continue;
                    }
                    its.add(ClickableItem.empty(new ItemBuilder(ItemType.GRAY_DYE)
                        .name("<dark_gray><obf>kkkkkkk").build()));
                }
        }).build().open(p);
    }
}
