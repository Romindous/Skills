package ru.romindous.skills.menus;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.enums.Trigger;
import ru.romindous.skills.menus.selects.AbilSelect;
import ru.romindous.skills.menus.selects.ModSelect;
import ru.romindous.skills.menus.selects.SelSelect;
import ru.romindous.skills.menus.selects.TrigSelect;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;


public class SkillMenu implements InventoryProvider {

    private static final ItemStack[] empty;
    private static final ItemStack purple; //окантовка
    private static final ItemStack red; //окантовка
    private static final ItemStack chain; //не изучено 1,2,3
    private static final ItemStack arrow;
    /*private static final ItemStack black; //не изучено 1,2,3
    private static final ItemStack gray; //не изучено 4
    private static final ItemStack know1;  //изучено 1
    private static final ItemStack know2;  //изучено 2
    private static final ItemStack know3;  //изучено 3
    private static final ItemStack know4;  //изучено 4
    private static final ItemStack charge1;  //перезарядка 1,2,3
    private static final ItemStack charge2;  //перезарядка 4*/

    static {
        arrow = new ItemBuilder(ItemType.IRON_NUGGET).name("<black>.").build();
        purple = new ItemBuilder(ItemType.PURPLE_STAINED_GLASS_PANE).name("§0.").build();
        red = new ItemBuilder(ItemType.RED_STAINED_GLASS_PANE).name("§0.").build();
        chain = new ItemBuilder(ItemType.CHAIN).name("§0.").build();
        empty = new ItemStack[54];
        for (int i = 0; i < 54; i++) {
            //empty[i] = new ItemBuilder(i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8 ? ((i & 1) == 0 ? ItemType.PURPLE_STAINED_GLASS_PANE : ItemType.RED_STAINED_GLASS_PANE) : ItemType.GRAY_STAINED_GLASS_PANE).name("§0.").build();
            //empty[i] = i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8  ?  ( (i & 1) == 0 ? purple : red) : gray;
            if (i < 9 || i > 44) {
                empty[i] = (i & 1) == 0 ? purple : red;
            } else {
                switch (i % 9) {
                    case 0, 8:
                        empty[i] = (i & 1) == 0 ? purple : red;
                        break;
                    case 1, 7:
                        empty[i] = chain;
                        break;
                    default:
                        break;
                }
            }
        }
        /*black = new ItemBuilder(ItemType.BLACK_STAINED_GLASS_PANE).name("§8не изучено").build();
        gray = new ItemBuilder(ItemType.GRAY_STAINED_GLASS_PANE).name("§8не изучено").build();
        know1 = new ItemBuilder(ItemType.GREEN_STAINED_GLASS_PANE).name("§fУров.1").build();
        know2 = new ItemBuilder(ItemType.GREEN_STAINED_GLASS_PANE).name("§fУров.2").build();
        know3 = new ItemBuilder(ItemType.GREEN_STAINED_GLASS_PANE).name("§fУров.3").build();
        know4 = new ItemBuilder(ItemType.LIME_STAINED_GLASS_PANE).name("§fПолностью изучено!").build();
        charge1 = new ItemBuilder(ItemType.BLUE_STAINED_GLASS_PANE).name("§cНабирает силу..").build();
        charge2 = new ItemBuilder(ItemType.CYAN_STAINED_GLASS_PANE).name("§cНабирает силу..").build();*/
    }

    private static final int NEW_SKILL_LVL = 10;
    private static final int NEW_ABIL_LVL = 6;
    private static final int NEW_MOD_LVL = 4;
    private static final ItemType EMPTY_TRIG = ItemType.HOPPER;
    private static final ItemType EMPTY_ABIL = ItemType.DRIED_KELP;
    private static final ItemType EMPTY_MOD = ItemType.GRAY_DYE;

    private final Survivor sv;

    public int skillIx;

    public SkillMenu(final Survivor sv) {
        this.sv = sv;
        this.skillIx = 0;
    }
    
    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 0.6f, skillIx * 0.2f);
        final Inventory inv = its.getInventory();
        if (inv != null) inv.setContents(empty);

        //content.getInventory().setItem(49, new ItemBuilder(sv.skill.mat).name(sv.skill.color+sv.skill.name()).build());
        its.set(49, ClickableItem.from(
            new ItemBuilder(sv.role.getIcon()).lore("").lore(TCUtil.P + "Клик - Главное меню").build(), e-> {
                p.performCommand("skill");
            }
        ));

        final Skill sk = skillIx < sv.skills.size() ? sv.skills.get(skillIx) : null;
        if (sk == null) {
            if (skillIx > sv.skills.size()) skillIx = sv.skills.size();
            its.set(10, ClickableItem.from(new ItemBuilder(EMPTY_TRIG).name(Trigger.color + "Триггер")
                .lore("").lore(TCUtil.P + "Клик " + TCUtil.N + "- выбрать").build(), e -> {
                SmartInventory.builder()
                    .id("Trig "+p.getName())
                    .provider(new TrigSelect(sv,  skillIx, null))
                    .size(2)
                    .title(Trigger.color + "         §lВыбор Тригера")
                    .build();
            }));
            return;
        }

        its.set(10, ClickableItem.from(new ItemBuilder(sk.trig.getIcon())
            .lore("").lore(TCUtil.P + "Клик " + TCUtil.N + "- заменить")
            .lore(TCUtil.A + "Выброс " + TCUtil.N + "- разобрать скилл").build(), e -> {
            switch (e.getClick()) {
                case DROP, CONTROL_DROP:
                    for (final Ability.AbilState as : sk.abils) {
                        sv.change(as, 1);
                    }
                    for (final Modifier.ModState ms : sk.mods) {
                        sv.change(ms, 1);
                    }
                    for (final Selector.SelState ss : sk.sels) {
                        sv.change(ss, 1);
                    }
                    sv.setSkill(skillIx, null);
                    reopen(p, its);
                    return;
            }

            SmartInventory.builder()
                .id("Trig "+p.getName())
                .provider(new TrigSelect(sv,  skillIx, sk))
                .size(2)
                .title(Trigger.color + "         §lВыбор Тригера")
                .build();
        }));

        final int lvl = sv.getLevel();
        final int remLvl = lvl - (NEW_SKILL_LVL * skillIx);
        int slot = 0;
        for (; slot != sk.abils.length; slot++) {
            final int pos = slot;
            final int spc = (pos << 1);
            final Ability.AbilState as = sk.abils[slot];
            its.set(28 + spc, ClickableItem.from(new ItemBuilder(as.abil().display(as.lvl()))
                .lore("").lore(TCUtil.P + "Клик " + TCUtil.N + "- заменить")
                .lore(TCUtil.A + "Q " + TCUtil.N + "- убрать способность").build(), e -> {
                switch (e.getClick()) {
                    case DROP, CONTROL_DROP:
                        sv.setSkillAbil(p, null, pos, skillIx);
                        return;
                }

                SmartInventory.builder()
                    .id("Abil "+p.getName())
                    .provider(new AbilSelect(sv,  skillIx, sk, pos))
                    .size(6, 9)
                    .title(TCUtil.P + "         §lВыбор Способности")
                    .build();
            }));
            final Selector.SelState ss = sk.sels[slot];
            if (Selector.CASTER.equals(ss.sel())) {
                its.set(28 + spc - 9, ClickableItem.empty(ss.sel().display(ss.lvl())));
            } else {
                its.set(28 + spc - 9, ClickableItem.from(new ItemBuilder(ss.sel().display(ss.lvl()))
                    .lore("").lore(TCUtil.P + "Клик " + TCUtil.N + "- заменить").build(), e -> {
                    switch (e.getClick()) {
                        case DROP, CONTROL_DROP:
                            sv.setSkillAbil(p, null, pos, skillIx);
                            return;
                    }

                    SmartInventory.builder()
                        .id("Sel "+p.getName())
                        .provider(new SelSelect(sv,  skillIx, sk, pos))
                        .size(6, 9)
                        .title(TCUtil.P + "         §lВыбор Подборника")
                        .build();
                }));
            }
            if (slot == 0) continue;
            its.set(28 + spc - 1, ClickableItem.empty(arrow));
        }

        final int maxAbils = Math.min((remLvl / NEW_ABIL_LVL) + 1, 4);
        if (slot < maxAbils) {
            final int pos = slot + 1;
            its.set(28 + (pos << 1), ClickableItem.from(new ItemBuilder(EMPTY_ABIL).name(TCUtil.N + "Способность")
                .lore("").lore(TCUtil.P + "Клик " + TCUtil.N + "- выбрать").build(), e -> {
                SmartInventory.builder()
                    .id("Abil "+p.getName())
                    .provider(new AbilSelect(sv,  skillIx, sk, pos))
                    .size(6, 9)
                    .title(TCUtil.P + "         §lВыбор Способности")
                    .build();
            }));
        }

        slot = 0;
        for (; slot != sk.mods.length; slot++) {
            final int pos = slot;
            final Modifier.ModState as = sk.mods[slot];
            its.set(46 + pos, ClickableItem.from(new ItemBuilder(as.mod().display(as.lvl()))
                .lore("").lore(TCUtil.P + "Клик " + TCUtil.N + "- заменить")
                .lore(TCUtil.A + "Q " + TCUtil.N + "- убрать модификатор").build(), e -> {
                switch (e.getClick()) {
                    case DROP, CONTROL_DROP:
                        sv.remSkillMod(p, pos, skillIx);
                        return;
                }

                SmartInventory.builder()
                    .id("Mod "+p.getName())
                    .provider(new ModSelect(sv,  skillIx, sk, pos))
                    .size(6, 9)
                    .title(TCUtil.P + "         §lВыбор Модификатора")
                    .build();
            }));
        }

        final int maxMods = Math.min((remLvl / NEW_MOD_LVL) + 1, 6);
        if (slot < maxMods) {
            final int pos = slot + 1;
            its.set(46 + slot, ClickableItem.from(new ItemBuilder(EMPTY_ABIL).name(TCUtil.N + "Способность")
                .lore("").lore(TCUtil.P + "Клик " + TCUtil.N + "- выбрать").build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    SmartInventory.builder()
                        .id("Mod "+p.getName())
                        .provider(new ModSelect(sv,  skillIx, sk, pos))
                        .size(6, 9)
                        .title(TCUtil.P + "         §lВыбор Модификатора")
                        .build();
                }
            }));
        }

        if (skillIx != 0) {
            its.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e -> {
                    skillIx--; reopen(p, its);
                })
            );
        }

        if (skillIx < lvl / NEW_SKILL_LVL) {
            its.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e -> {
                    skillIx++; reopen(p, its);
                })
            );
        }
    }
}






       
        /*
        
        for (final Ability ab : Ability.values()) {
            
            if (ab.skill != sv.skill) continue;
            final List<String> lore = new ArrayList<>();
            lore.add("");
            final boolean on = sv.isOn(ab);
            final int abLevel = sv.getAbilityLvl(ab);
            for (final String s : ab.descr) {
                final int lvl = s.charAt(0) - 48;
                if (lvl < 10) {
                    lore.add((abLevel < lvl || !on ? "§c" : "§a") + s);
                } else {
                    lore.add(s);
                }
            }

            lore.add("§8=-=-=-=-=-=-=-=-=-");
            final StringBuffer sb = new StringBuffer(24);
            if (abLevel == 0) {
                lore.add(" ");
                final Ability first = null;;
                final Ability second = null;;
                switch (ab.slot) {
                    case 13:
                        //first = Ability.getBySkill(sv.skill, 20);
                        //second = Ability.getBySkill(sv.skill, 24);
                        break;
                    case 20:
                        //first = Ability.getBySkill(sv.skill, 37);
                        //second = Ability.getBySkill(sv.skill, 31);
                        break;
                    case 24:
                        //first = Ability.getBySkill(sv.skill, 43);
                        //second = Ability.getBySkill(sv.skill, 31);
                        break;
                    default:
                        //first = null;
                        //second = null;
                        break;
                }
                if (first != null && sv.getAbilityLvl(first) == 0) {
                    sb.append(first.name());
                }
                if (second != null && sv.getAbilityLvl(second) == 0) {
                    sb.append(second.name());
                }
                if (sb.length() == 0) {
                    lore.add("§7Изучить - §3100 душ");
                } else {
                    lore.add("§cСначала изучите:" + sb.toString());
                }
            } else {
                if (on) {
                    if (ab.showPercent != null) {
                        //lore.add("§7сейчас: §d" + dft.format(ab.shwPct ? ab.bfr * sv.getStat(ab.affectStat) * abLevel * 100.0f + "%" : ab.bfr * sv.getStat(ab.affectStat) * abLevel));
                        lore.add("§7сейчас: §d" +   (ab.showPercent ? (ab.bfr*sv.getStat(ab.affectStat)*abLevel*100+"%") : (ab.bfr*sv.getStat(ab.affectStat)*abLevel))  );
                        lore.add(" ");
                    }
                    lore.add("§aВключена§7, ПКМ - §cВыкл.");
                } else {
                    lore.add("§cВыключена§7, ПКМ - §aВкл.");
                }

                lore.add(" ");
                lore.add(abLevel == 4 ? "§7Макс. §dлвл§7!" : "§7Прокачать: §3" + (abLevel * 100 + 100) + " душ");
            }
            final String color;
            switch (ab.slot >> 3) {
                case 2:
                    color = "§c§l";
                    break;
                case 1:
                case 0:
                    color = "§d§l";
                    break;
                default:
                    color = "§e§l";
                    break;
            }
            content.set(ab.slot, ClickableItem.of(new ItemBuilder(abLevel == 0 ? ItemType.GUNPOWDER : (sv.isOn(ab) ? ItemType.GLOWSTONE_DUST : ItemType.REDSTONE))
            .name(color+ab.name() + (abLevel == 0 ? "" : " §d" + abLevel))
                    .lore(lore)
                    .build(), e -> {
                if (e.isLeftClick()) {
                    if (!sb.isEmpty()) {
                        final Inventory inv = content.getInventory();
                        final ItemStack fst;
                        final ItemStack scd;
                        switch (e.getSlot()) {
                            case 13:
                                fst = inv.getItem(20);
                                scd = inv.getItem(24);
                                break;
                            case 20:
                                fst = inv.getItem(37);
                                scd = inv.getItem(31);
                                break;
                            case 24:
                                fst = inv.getItem(43);
                                scd = inv.getItem(31);
                                break;
                            default:
                                fst = null;
                                scd = null;
                                break;
                        }
                        if (fst != null && fst.getType() == ItemType.GUNPOWDER) {
                            fst.setType(ItemType.CRIMSON_ROOTS);
                            Ostrov.sync(() -> fst.setType(ItemType.GUNPOWDER), 4);
                        }
                        if (scd != null && scd.getType() == ItemType.GUNPOWDER) {
                            scd.setType(ItemType.CRIMSON_ROOTS);
                            Ostrov.sync(() -> scd.setType(ItemType.GUNPOWDER), 4);
                        }
                        return;
                    }

                    if (sv.souls < abLevel * 100 + 100) {
                        p.sendMessage(Main.prefix + "§cНакопите больше §3душ §cдля изучения!");
                        return;
                    }
                    if (abLevel == 4) {
                        p.sendMessage(Main.prefix + "§cЭта способность уже полностью изучена!");
                        return;
                    }
                    sv.souls -= abLevel * 100 + 100;
                    sv.upgradeAbility(p, ab);
                } else {
                    sv.toggle(p, ab);
                }
                reopen(p, content);
            }));
        }*/
        