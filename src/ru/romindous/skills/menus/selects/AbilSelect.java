package ru.romindous.skills.menus.selects;

import javax.annotation.Nullable;
import java.util.Map;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;

public class AbilSelect extends SvSelect {

    private static final ItemStack[] empty;
    private static final ItemStack rail; //окантовка

    static {
        rail = new ItemBuilder(ItemType.POWERED_RAIL).name("§0.").build();
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

    public AbilSelect(final Survivor sv, final int skIx, final @Nullable Skill sk, final int abSlot) {
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
        for (final Map.Entry<Ability.AbilState, Integer> en : sv.abils.entrySet()) {
            switch (slot / 9) {case 0, 8: slot++; continue;}
            final Ability.AbilState ab = en.getKey();
            if (!sv.canUse(ab.abil())) continue;
            its.set(slot, ClickableItem.from(new ItemBuilder(ab.abil().item(ab.lvl())).amount(en.getValue()).lore("")
                .lore(TCUtil.P + "Клик - Выбрать").lore("<red>Выброс" + TCUtil.P + " - Выдать предметом").build(), e -> {
                    switch (e.getClick()) {
                        case DROP, CONTROL_DROP:
                            if (sv.change(ab, -1) < 0) return;
                            ItemUtil.giveItemsTo(p, ab.abil().item(ab.lvl()));
                            reopen(p, its);
                            return;
                    }
                    if (sk == null) {
                        openLast(p);
                        return;
                    }

                    sv.setSkillAbil(p, ab, abSlot, skIx);
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