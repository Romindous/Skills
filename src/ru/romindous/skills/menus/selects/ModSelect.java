package ru.romindous.skills.menus.selects;

import java.util.*;
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
import ru.romindous.skills.menus.UpgradeMenu;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.skills.chas.ChasMod;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;

public class ModSelect extends SvSelect {

    private static final ItemStack[] empty;
    private static final ItemStack rail; //окантовка

    static {
        rail = new ItemBuilder(ItemType.DETECTOR_RAIL).name("§0.").build();
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

    public ModSelect(final Survivor sv, final int skIx, final Skill sk, final int abSlot) {
        super(sv, skIx, sk);
        this.mdSlot = abSlot;
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

        //content.getInventory().setItem(49, new ItemBuilder(sv.skill.mat).name(sv.skill.color+sv.skill.name()).build());
        int slot = 0;
        final Chastic[] chs = getChs(sk);
        for (final Map.Entry<Modifier.ModState, Integer> en : sv.mods.entrySet()) {
            while (switch (slot % 9) {case 0, 8 -> true; default -> false;}) slot++;
            final Modifier.ModState md = en.getKey();
            if (!sv.canUse(md.val())) continue;
            final List<String> fnd = new ArrayList<>();
            if (chs.length != 0) {
                for (final Chastic ch : md.val().chastics()) {
                    if (Arrays.binarySearch(chs, ch) < 0) continue;
                    fnd.add(ch.disName());
                }
            }
            final int amt = en.getValue();
            its.set(slot, fnd.isEmpty() ? ClickableItem.from(new ItemBuilder(md.val().display(md.lvl())).amount(amt)
                .lore(relate(fnd)).lore(amt < 2 ? "" : TCUtil.A + "ПКМ" + TCUtil.N + " - Прокачка (" + amt + "/2)")
                .lore(TCUtil.A + TCUtil.bind(TCUtil.Input.DROP) + TCUtil.N + " - Выдать").build(), e -> {
                final int cnt = sv.count(md);
                if (cnt < 1) {openLast(p); return;}
                final ItemStack drop;
                switch (e.getClick()) {
                    case DROP:
                        sv.change(md, -1);
                        drop = md.val().drop(md.lvl());
                        ItemUtil.giveItemsTo(p, drop);
                        reopen(p, its);
                        return;
                    case CONTROL_DROP:
                        sv.change(md, -cnt);
                        drop = md.val().drop(md.lvl());
                        drop.setAmount(cnt);
                        ItemUtil.giveItemsTo(p, drop);
                        reopen(p, its);
                        return;
                    case RIGHT, SHIFT_RIGHT:
                        if (cnt < 2) return;
                        sv.change(md, -2);
                        drop = md.val().drop(md.lvl());
                        drop.setAmount(2);
                        ItemUtil.giveItemTo(p, drop,
                            p.getInventory().getHeldItemSlot(), true);
                        UpgradeMenu.ask(p);
                        return;
                }

                p.sendMessage(TCUtil.form(Main.prefix + sk.name + "<red> не использует:"));
                for (final Chastic ch : md.val().chastics()) fnd.add(ch.disName());
                p.sendMessage(TCUtil.form(String.join(TCUtil.N + ", ", fnd)));
                reopen(p, its);
            }) : ClickableItem.from(new ItemBuilder(md.val().display(md.lvl()))
                .amount(amt).lore("<dark_gray>(клик - выбор)").lore(relate(fnd))
                .lore(amt < 2 ? "" : TCUtil.A + "ПКМ" + TCUtil.N + " - Прокачка (" + amt + "/2)")
                .lore(TCUtil.A + TCUtil.bind(TCUtil.Input.DROP) + TCUtil.N + " - Выдать").build(), e -> {
                final int cnt = sv.count(md);
                if (cnt < 1) {openLast(p); return;}
                final ItemStack drop;
                switch (e.getClick()) {
                    case DROP:
                        sv.change(md, -1);
                        drop = md.val().drop(md.lvl());
                        ItemUtil.giveItemsTo(p, drop);
                        reopen(p, its);
                        return;
                    case CONTROL_DROP:
                        sv.change(md, -cnt);
                        drop = md.val().drop(md.lvl());
                        drop.setAmount(cnt);
                        ItemUtil.giveItemsTo(p, drop);
                        reopen(p, its);
                        return;
                    case RIGHT, SHIFT_RIGHT:
                        if (cnt < 2) return;
                        sv.change(md, -2);
                        drop = md.val().drop(md.lvl());
                        drop.setAmount(2);
                        ItemUtil.giveItemTo(p, drop,
                            p.getInventory().getHeldItemSlot(), true);
                        UpgradeMenu.ask(p);
                        return;
                }

                final Modifier.ModState ms = mdSlot < sk.mods.length ? sk.mods[mdSlot] : null;
                if (ms != null) sv.remSkillMod(p, mdSlot, skIx);
                sv.addSkillMod(p, md, skIx);
                openLast(p);
            }));
            slot++;
        }

        its.set(49, ClickableItem.from(new ItemBuilder(ItemType.DAYLIGHT_DETECTOR)
            .name(TCUtil.sided("<red>Отмена")).build(), e -> openLast(p)));
    }

    private static Chastic[] getChs(final Skill sk) {
        if (sk == null) return new Chastic[0];
        final Set<Chastic> chs = EnumSet.noneOf(Chastic.class);
        chs.add(Chastic.MANA); chs.add(Chastic.COOLDOWN);
        for (final Selector.SelState sls : sk.sels) {
            for (final ChasMod cm : sls.val().stats()) {
                chs.add(cm.chs());
            }
        }
        for (final Ability.AbilState abs : sk.abils) {
            for (final ChasMod cm : abs.val().stats()) {
                chs.add(cm.chs());
            }
        }
        final Chastic[] cha = chs.toArray(new Chastic[0]);
        Arrays.sort(cha);
        return cha;
    }

    private static final String[] UNRELATED = {"<red>Этот " + TCUtil.P + "модификатор " + "<red>не имеет общих статов",
        "<red>с " + TCUtil.P + "подборниками " + "<red>или " + TCUtil.P + "способностями " + "<red>навыка!"};
    private static String[] relate(final List<String> fnd) {
        if (fnd.isEmpty()) return UNRELATED;
        final List<String> same = new LinkedList<>();
        same.addFirst("<apple>Общие " + TCUtil.P + "статы " + "<apple>с навыком:");
        for (final String d : fnd) same.add(TCUtil.N + "◇ " + d);
        return same.toArray(new String[0]);
    }

}
