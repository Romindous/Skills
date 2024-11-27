package ru.romindous.skills.menus.selects;

import javax.annotation.Nullable;
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
import ru.romindous.skills.Survivor;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.mods.Modifier;

public class ModSelect extends SvSelect {

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

    private final int mdSlot;

    public ModSelect(final Survivor sv, final int skIx, final @Nullable Skill sk, final int abSlot) {
        super(sv, skIx, sk);
        this.mdSlot = abSlot;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.8f);
        final Inventory inv = its.getInventory();
        if (inv != null) inv.setContents(empty);

        //content.getInventory().setItem(49, new ItemBuilder(sv.skill.mat).name(sv.skill.color+sv.skill.name()).build());
        int slot = 0;
        for (final Map.Entry<Modifier.ModState, Integer> en : sv.mods.entrySet()) {
            switch (slot / 9) {
                case 0, 8:
                    slot++;
                    continue;
            }
            final Modifier.ModState md = en.getKey();
            if (!sv.canUse(md.mod())) continue;
            its.set(slot, ClickableItem.from(new ItemBuilder(md.mod().display(md.lvl())).amount(en.getValue()).lore("")
                .lore(TCUtil.P + "Клик - Выбрать").lore("<red>Выброс" + TCUtil.P + " - Выдать предметом").build(), e -> {
                    switch (e.getClick()) {
                        case DROP, CONTROL_DROP:
                            if (sv.change(md, -1) < 0) return;
                            ItemUtil.giveItemsTo(p, md.mod().drop(md.lvl()));
                            reopen(p, its);
                            return;
                    }
                    if (sk == null) {
                        openLast(p);
                        return;
                    }

                    final Modifier.ModState ms = mdSlot < sk.mods.length ? sk.mods[mdSlot] : null;
                    if (ms != null) sv.remSkillMod(p, mdSlot, skIx);
                    sv.addSkillMod(p, md, skIx);
                    openLast(p);
                }
            ));
            slot++;
        }

        its.set(17, ClickableItem.from(new ItemBuilder(sv.role.getIcon())
            .name("<red>Отмена Выбора").build(), e -> {
                openLast(p);
            }
        ));
    }

}
