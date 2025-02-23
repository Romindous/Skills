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
import ru.romindous.skills.Main;
import ru.romindous.skills.guides.Entries;
import ru.romindous.skills.menus.UpgradeMenu;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.abils.InvCondition;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.survs.Survivor;

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

    public AbilSelect(final Survivor sv, final int skIx, final Skill sk, final int abSlot) {
        super(sv, skIx, sk);
        this.abSlot = abSlot;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        if (sk == null) {
            openLast(p);
            return;
        }
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.8f);
        final Inventory inv = its.getInventory();
        if (inv != null) inv.setContents(empty);

        int slot = 0;
        for (final Map.Entry<Ability.AbilState, Integer> en : sv.abils.entrySet()) {
            while (switch (slot % 9) {case 0, 8 -> true; default -> false;}) slot++;
            final Ability.AbilState ab = en.getKey();
            if (!sv.canUse(ab.val())) continue;
            if (sk.trig == null) continue;
            final Trigger trg = abSlot == 0 ? sk.trig : Trigger.ABIL_CAST;
            final Trigger rt = ab.val().trig();
            final boolean trc = rt != null && rt != trg;
            final String[] check = trc ? new String[] {"<red>Нужен тригер "
                + rt.disName() + "<red>.", "<red>Сейчас - " + trg.disName()} : checkInvCond(ab.val().equip());
            final int amt = en.getValue();
            its.set(slot, trc ? ClickableItem.from(new ItemBuilder(ab.val().display(ab.lvl())).amount(amt)
                .lore(check).lore(amt < 2 ? "" : TCUtil.A + "ПКМ" + TCUtil.N + " - Прокачка (" + amt + "/2)")
                .lore(TCUtil.A + TCUtil.bind(TCUtil.Input.DROP) + TCUtil.N + " - Выдать").build(), e -> {
                final int cnt = sv.count(ab);
                if (cnt < 1) {openLast(p); return;}
                final ItemStack drop;
                switch (e.getClick()) {
                    case DROP:
                        sv.change(ab, -1);
                        drop = ab.val().drop(ab.lvl());
                        ItemUtil.giveItemsTo(p, drop);
                        reopen(p, its);
                        return;
                    case CONTROL_DROP:
                        sv.change(ab, -cnt);
                        drop = ab.val().drop(ab.lvl());
                        drop.setAmount(cnt);
                        ItemUtil.giveItemsTo(p, drop);
                        reopen(p, its);
                        return;
                    case RIGHT, SHIFT_RIGHT:
                        if (cnt < 2) return;
                        sv.change(ab, -2);
                        drop = ab.val().drop(ab.lvl());
                        drop.setAmount(2);
                        ItemUtil.giveItemTo(p, drop,
                            p.getInventory().getHeldItemSlot(), true);
                        UpgradeMenu.ask(p);
                        return;
                }

                p.sendMessage(TCUtil.form(Main.prefix + "<red>Способность "
                    + TCUtil.P + ab.val().name() + " <red>должна следовать"));
                p.sendMessage(TCUtil.form(Main.prefix + "<red>тригеру " + rt.disName() + "<red>. Сейчас - "
                    + trg.disName() + (abSlot == 0 ? "" : " <dark_gray>(пред. способность)")));
                reopen(p, its);
            }) : ClickableItem.from(new ItemBuilder(ab.val().display(ab.lvl()))
                .amount(amt).lore(check).lore("<dark_gray>(клик - выбор)")
                .lore(amt < 2 ? "" : TCUtil.A + "ПКМ" + TCUtil.N + " - Прокачка (" + amt + "/2)")
                .lore(TCUtil.A + TCUtil.bind(TCUtil.Input.DROP) + TCUtil.N + " - Выдать").build(), e -> {
                final int cnt = sv.count(ab);
                if (cnt < 1) {openLast(p); return;}
                final ItemStack drop;
                switch (e.getClick()) {
                    case DROP:
                        sv.change(ab, -1);
                        drop = ab.val().drop(ab.lvl());
                        ItemUtil.giveItemsTo(p, drop);
                        reopen(p, its);
                        return;
                    case CONTROL_DROP:
                        sv.change(ab, -cnt);
                        drop = ab.val().drop(ab.lvl());
                        drop.setAmount(cnt);
                        ItemUtil.giveItemsTo(p, drop);
                        reopen(p, its);
                        return;
                    case RIGHT, SHIFT_RIGHT:
                        if (cnt < 2) return;
                        sv.change(ab, -2);
                        drop = ab.val().drop(ab.lvl());
                        drop.setAmount(2);
                        ItemUtil.giveItemTo(p, drop,
                            p.getInventory().getHeldItemSlot(), true);
                        UpgradeMenu.ask(p);
                        return;
                }

                if (abSlot != 0) Entries.new_abil.complete(p, sv, false);
                sv.setSkillAbil(p, ab, abSlot, skIx);
                openLast(p);
            }));
            slot++;
        }

        its.set(49, ClickableItem.from(new ItemBuilder(ItemType.DAYLIGHT_DETECTOR)
            .name(TCUtil.sided("<red>Отмена")).build(), e -> openLast(p)));
    }

    private static final String[] MATCH_SINGLE = {"<apple>Можно выбрать!"};
    private static final String[] MATCH_MULTI = {"<apple>Нужная " + TCUtil.P + "экипировка "
        + "<apple>совпадает", "<apple>с остальными способностями!"};
    private String[] checkInvCond(final @Nullable InvCondition ic) {
        if (sk == null || sk.abils.length < 2) return MATCH_SINGLE;
        if (ic == null) return MATCH_MULTI;
        for (int i = 0; i != sk.abils.length; i++) {
            if (i == abSlot) continue;
            final Ability.AbilState ab = sk.abils[i];
            final InvCondition ric = ab.val().equip();
            if (ric == null || ic.equals(ric)) continue;
            return new String[]{"<gold>Требуемое " + TCUtil.P + "снаряжение " + "<gold>этой способности",
                "<gold>(" + ic.describe() + "<gold>)", "<gold>отличается от снаряжения для " + ab.val().name(ab.lvl()),
                "<gold>(" + ric.describe() + "<gold>)"};
        }
        return MATCH_MULTI;
    }

}
