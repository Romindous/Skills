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
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.8f);
        final Inventory inv = its.getInventory();
        if (inv != null) inv.setContents(empty);

        //content.getInventory().setItem(49, new ItemBuilder(sv.skill.mat).name(sv.skill.color+sv.skill.name()).build());
        int slot = 0;
        final Chastic[] chs = getChs(sk);
        for (final Map.Entry<Modifier.ModState, Integer> en : sv.mods.entrySet()) {
            while (switch (slot % 9) {case 0, 8 -> true; default -> false;}) slot++;
            final Modifier.ModState md = en.getKey();
            if (!sv.canUse(md.mod())) continue;
            its.set(slot, ClickableItem.from(new ItemBuilder(md.mod().display(md.lvl()))
                .amount(en.getValue()).lore("<dark_gray>(клик - выбор)").lore(relate(md.mod(), chs))
                .lore(TCUtil.A + TCUtil.bind(TCUtil.Input.DROP) + TCUtil.N + " - Выдать").build(), e -> {
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

        its.set(49, ClickableItem.from(new ItemBuilder(ItemType.DAYLIGHT_DETECTOR)
            .name(TCUtil.sided("<red>Отмена")).build(), e -> openLast(p)));
    }

    private static Chastic[] getChs(final Skill sk) {
        if (sk == null) return new Chastic[0];
        final Set<Chastic> chs = EnumSet.noneOf(Chastic.class);
        chs.add(Chastic.MANA); chs.add(Chastic.COOLDOWN);
        for (final Selector.SelState sls : sk.sels) {
            for (final ChasMod cm : sls.sel().stats()) {
                chs.add(cm.chs);
            }
        }
        for (final Ability.AbilState abs : sk.abils) {
            for (final ChasMod cm : abs.abil().stats()) {
                chs.add(cm.chs);
            }
        }
        final Chastic[] cha = chs.toArray(new Chastic[0]);
        Arrays.sort(cha);
        return cha;
    }

    private static final String[] UNRELATED = {"<red>Этот " + TCUtil.P + "модификатор " + "<red>не имеет общих статов",
        "<red>с " + TCUtil.P + "подборниками " + "<red>или " + TCUtil.P + "способностями " + "<red>навыка!"};
    private static String[] relate(final Modifier md, final Chastic[] chs) {
        if (chs.length == 0) return UNRELATED;
        final List<String> fnd = new ArrayList<>();
        for (final Chastic ch : md.chastics()) {
            if (Arrays.binarySearch(chs, ch) < 0) continue;
            fnd.add(ch.disName());
        }
        if (fnd.isEmpty()) return UNRELATED;
        return new String[]{"<apple>Общие " + TCUtil.P + "статы " + "<apple>с навыком:",
            String.join(TCUtil.N + ", ", fnd)};
    }

}
