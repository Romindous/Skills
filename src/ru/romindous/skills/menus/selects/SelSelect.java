package ru.romindous.skills.menus.selects;

import java.util.Map;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.sels.Selector;

public class SelSelect extends SvSelect {

    private static final ItemStack[] empty;
    private static final ItemStack rail; //окантовка

    static {
        rail = new ItemBuilder(ItemType.ACTIVATOR_RAIL).name("§0.").build();
        empty = new ItemStack[54];
        for (int i = 0; i < 54; i++) {
            switch (i % 9) {
                case 0, 8:
                    empty[i] = rail;
                    break;
                default:
                    break;
            }
        }
    }

    private static final int NEW_SKILL_LVL = 10;
    private static final int NEW_ABIL_LVL = 6;
    private static final int NEW_MOD_LVL = 4;
    private static final ItemType BACK = ItemType.CRIMSON_DOOR;

    private final int abSlot;

    public SelSelect(final Survivor sv, final int skIx, final Skill sk, final int abSlot) {
        super(sv, skIx, sk);
        this.abSlot = abSlot;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.8f);
        final Inventory inv = its.getInventory();
        if (inv != null) inv.setContents(empty);

        //content.getInventory().setItem(49, new ItemBuilder(sv.skill.mat).name(sv.skill.color+sv.skill.name()).build());
        int slot = 0;
        for (final Map.Entry<Selector.SelState, Integer> en : sv.sels.entrySet()) {
            while (switch (slot % 9) {case 0, 8 -> true; default -> false;}) slot++;
            final Selector.SelState sl = en.getKey();
            if (!sv.canUse(sl.sel()) || sl.sel().equals(Selector.CASTER)) continue;
            if (Selector.SAME.equals(sl.sel())) {
                its.set(slot, ClickableItem.from(new ItemBuilder(sl.sel().display(sl.lvl()))
                    .amount(en.getValue()).lore("<dark_gray>(клик - выбор)").build(), e -> {
                        if (sk == null) {
                            openLast(p);
                            return;
                        }

                        sv.setSkillSel(p, sl, abSlot, skIx);
                        openLast(p);
                    }
                ));
                continue;
            }
            its.set(slot, ClickableItem.from(new ItemBuilder(sl.sel().display(sl.lvl())).lore("<dark_gray>(клик - выбор)")
                .lore(TCUtil.A + TCUtil.bind(TCUtil.Input.DROP) + TCUtil.N + " - Выдать").build(), e -> {
                    switch (e.getClick()) {
                        case DROP, CONTROL_DROP:
                            if (sv.change(sl, -1) < 0) return;
                            ItemUtil.giveItemsTo(p, sl.sel().drop(sl.lvl()));
                            reopen(p, its);
                            return;
                        default:
                            if (sk == null) {
                                openLast(p);
                                return;
                            }

                            sv.setSkillSel(p, sl, abSlot, skIx);
                            openLast(p);
                    }
                }
            ));
            slot++;
        }

        its.set(49, ClickableItem.from(new ItemBuilder(ItemType.DAYLIGHT_DETECTOR)
            .name(TCUtil.sided("<red>Отмена")).build(), e -> openLast(p)));
    }

}
