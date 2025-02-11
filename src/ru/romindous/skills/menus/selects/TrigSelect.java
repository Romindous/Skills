package ru.romindous.skills.menus.selects;

import javax.annotation.Nullable;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.guides.Entries;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;

public class TrigSelect extends SvSelect {

    /*private static final ItemStack[] empty;
    private static final ItemStack rail; //окантовка

    static {
        rail = new ItemBuilder(ItemType.ACTIVATOR_RAIL).name("§0.").build();
        empty = new ItemStack[18];
        for (int i = 0; i < 18; i++) {
            switch (i % 9) {
                case 0, 8:
                    empty[i] = rail;
                    break;
                default:
                    break;
            }
        }
    }*/

    private static final int NEW_SKILL_LVL = 10;
    private static final int NEW_ABIL_LVL = 6;
    private static final int NEW_MOD_LVL = 4;
    private static final ItemType BACK = ItemType.CRIMSON_DOOR;

    public TrigSelect(final Survivor sv, final int skIx, final @Nullable Skill sk) {
        super(sv, skIx, sk);
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.8f);
//        final Inventory inv = its.getInventory();
//        if (inv != null) inv.setContents(empty);

        //content.getInventory().setItem(49, new ItemBuilder(sv.skill.mat).name(sv.skill.color+sv.skill.name()).build());
        int slot = 0;
        for (final Trigger tr : Trigger.values()) {
            if (tr == Trigger.UNKNOWN) continue;
            while (switch (slot) {
                case 4, 11, 12, 13, 14, 15 -> true;
                default -> false;
            }) slot++;
            its.set(slot, ClickableItem.from(new ItemBuilder(tr.icon())
                .lore("").lore(Trigger.color + "Клик - Выбрать").build(), e -> {
                    Entries.trig.complete(p, sv, false);
                    if (sk == null) {
                        sv.setSkill(skIx, new Skill("Навык-" + skIx, tr, new Selector.SelState[0],
                            new Ability.AbilState[0], new Modifier.ModState[0]));
                        openLast(p);
                        return;
                    }

                    sv.setSkill(skIx, new Skill(sk.name, tr, sk.sels, sk.abils, sk.mods));
                    openLast(p);
                }
            ));
            slot++;
        }

        its.set(13, ClickableItem.from(new ItemBuilder(ItemType.DAYLIGHT_DETECTOR)
            .name(TCUtil.sided("<red>Отмена")).build(), e -> openLast(p)));
    }

}
