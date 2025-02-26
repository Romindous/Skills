package ru.romindous.skills.menus;

import java.util.*;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.*;
import ru.romindous.skills.guides.Entries;
import ru.romindous.skills.menus.selects.AbilSelect;
import ru.romindous.skills.menus.selects.ModSelect;
import ru.romindous.skills.menus.selects.SelSelect;
import ru.romindous.skills.menus.selects.TrigSelect;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.chas.ChasMod;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.survs.SM;
import ru.romindous.skills.survs.Survivor;


public class SkillMenu implements InventoryProvider {

    private static final ItemStack[] empty;
    private static final ItemStack purple; //окантовка
    private static final ItemStack red; //окантовка
    private static final ItemStack chain; //не изучено 1,2,3
    private static final ItemStack candle;
    private static final ItemStack light;
    /*private static final ItemStack black; //не изучено 1,2,3
    private static final ItemStack gray; //не изучено 4
    private static final ItemStack know1;  //изучено 1
    private static final ItemStack know2;  //изучено 2
    private static final ItemStack know3;  //изучено 3
    private static final ItemStack know4;  //изучено 4
    private static final ItemStack charge1;  //перезарядка 1,2,3
    private static final ItemStack charge2;  //перезарядка 4*/

    static {
        purple = new ItemBuilder(ItemType.PURPLE_STAINED_GLASS_PANE).name("§0.").build();
        red = new ItemBuilder(ItemType.RED_STAINED_GLASS_PANE).name("§0.").build();
        candle = new ItemBuilder(ItemType.RED_CANDLE).name("§0.").build();
        chain = new ItemBuilder(ItemType.CHAIN).name("§0.").build();
        light = new ItemBuilder(ItemType.SHROOMLIGHT).name("§0.").build();
        empty = new ItemStack[27];
        for (int i = 0; i != 27; i++) {
            if (i / 9 != 1) empty[i] = (i & 1) == 0 ? red : purple;
        }
        empty[9] = empty[17] = chain;
//        empty[18] = empty[26] = candle;
//        empty[0] = empty[8] = light;
    }

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
//        p.sendMessage("'"  + sv.extracted(new StringBuilder()).toString() + "'");
        p.playSound(p.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 1f, skillIx * 0.2f);
        final Inventory inv = its.getInventory();
        if (inv != null) inv.setContents(empty);

        its.set(22, ClickableItem.from(new ItemBuilder(sv.role.getIcon())
            .name(TCUtil.sided(TCUtil.P + "Главное Меню", "🢙")).deLore().build(), e -> {
            MainMenu.open(p);
        }));

        final Skill sk = skillIx < sv.skills.size() ? sv.skills.get(skillIx) : null;
        if (sk == null) {
            if (skillIx > sv.skills.size()) skillIx = sv.skills.size();
            its.set(10, ClickableItem.from(new ItemBuilder(EMPTY_TRIG).name(TCUtil
                .sided(Trigger.color + "Тригер", "🟃") + " <dark_gray>(клик)").build(), e -> {
                SmartInventory.builder()
                    .id("Trig "+p.getName())
                    .provider(new TrigSelect(sv, skillIx, null))
                    .size(2)
                    .title(Trigger.color + "         §lВыбор Тригера")
                    .build().open(p);
            }));

            if (skillIx != 0) {
                its.set(2, 0, ClickableItem.of(ItemUtil.previosPage, e -> {
                    skillIx--; reopen(p, its);
                }));
            }
            return;
        }

        its.set(4, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(skillIt(skillIx))
            .name(TCUtil.sided(TCUtil.P + sk.name, TCUtil.A + "✞") + " <dark_gray>(клик - назвать)")
            .lore("<dark_gray>При использовании:").lore(sk.describe(sv)).build(), sk.name, nm -> {
            final String fnm = nm.replace(StringUtil.CHAR_0, '!')
                .replace(StringUtil.CHAR_1, '|').replace(StringUtil.CHAR_2, ':');
            sv.setSkill(p, skillIx, new Skill(fnm, sk.trig, sk.sels, sk.abils, sk.mods));
            reopen(p, its);
        }));

        its.set(10, ClickableItem.from(new ItemBuilder(sk.trig.icon()).lore("<dark_gray>(клик - заменить)")
            .lore(TCUtil.A + TCUtil.bind(TCUtil.Input.DROP) + TCUtil.N + " - Разобрать").build(), e -> {
            switch (e.getClick()) {
                case DROP, CONTROL_DROP:
                    for (final Ability.AbilState as : sk.abils) sv.change(as, 1);
                    for (final Modifier.ModState ms : sk.mods) sv.change(ms, 1);
                    for (final Selector.SelState ss : sk.sels) sv.change(ss, 1);
                    sv.setSkill(p, skillIx, null);
                    reopen(p, its);
                    return;
            }

            SmartInventory.builder()
                .id("Trig "+p.getName())
                .provider(new TrigSelect(sv, skillIx, sk))
                .size(2)
                .title(Trigger.color + "         §lВыбор Тригера")
                .build().open(p);
        }));

        final int lvl = sv.getLevel();
        final int remLvl = lvl - (SM.NEW_SKILL_LVL * skillIx);
        int slot = 0;
        for (; slot != sk.abils.length; slot++) {
            final int pos = slot;
            final Ability.AbilState as = sk.abils[pos];
            its.set(12 + (pos << 1), ClickableItem.from(new ItemBuilder(as.val().display(as.lvl()))
                .lore("<dark_gray>(клик - заменить)").lore(TCUtil.A + TCUtil.bind(TCUtil.Input.DROP)
                    + TCUtil.N + " - Убрать").build(), e -> {
                switch (e.getClick()) {
                    case DROP, CONTROL_DROP:
                        sv.setSkillAbil(p, null, pos, skillIx);
                        reopen(p, its);
                        return;
                }

                SmartInventory.builder()
                    .id("Abil "+p.getName())
                    .provider(new AbilSelect(sv, skillIx, sk, pos))
                    .size(6, 9)
                    .title(TCUtil.P + "      §lВыбор Способности")
                    .build().open(p);
            }));
            final Selector.SelState ss = sk.sels[slot];
            if (Selector.CASTER.equals(ss.val())) {
                its.set(11 + (pos << 1), ClickableItem.empty(ss.val().display(ss.lvl())));
            } else {
                its.set(11 + (pos << 1), ClickableItem.from(new ItemBuilder(ss.val().display(ss.lvl()))
                    .lore("<dark_gray>(клик - заменить)").build(), e -> {
                    SmartInventory.builder()
                        .id("Sel "+p.getName())
                        .provider(new SelSelect(sv, skillIx, sk, pos))
                        .size(6, 9)
                        .title(TCUtil.P + "         §lВыбор Подборника")
                        .build().open(p);
                }));
            }
        }

        final int maxAbils = Math.min((remLvl / SM.NEW_ABIL_LVL) + 1, 4);
        if (slot < maxAbils) {
            final int pos = slot;
            its.set(12 + (pos << 1), ClickableItem.from(new ItemBuilder(EMPTY_ABIL)
                .name(TCUtil.sided(TCUtil.N + "Способность"))
                .lore("<dark_gray>(клик - заменить)").build(), e -> {
                SmartInventory.builder()
                    .id("Abil "+p.getName())
                    .provider(new AbilSelect(sv, skillIx, sk, pos))
                    .size(6, 9)
                    .title(TCUtil.P + "      §lВыбор Способности")
                    .build().open(p);
            }));
        }

        slot = 0;
        for (; slot != sk.mods.length; slot++) {
            final int pos = slot;
            final Chastic[] chs = getChs(sk);
            final Modifier.ModState ms = sk.mods[slot];
            its.set(23 * (19 + pos) / 22, ClickableItem.from(new ItemBuilder(ms.val()
                .display(ms.lvl())).lore("<dark_gray>(клик - заменить)").lore(relate(ms.val(), chs))
                .lore(TCUtil.A + TCUtil.bind(TCUtil.Input.DROP) + TCUtil.N + " - Убрать").build(), e -> {
                switch (e.getClick()) {
                    case DROP, CONTROL_DROP:
                        sv.remSkillMod(p, pos, skillIx);
                        reopen(p, its);
                        return;
                }

                SmartInventory.builder()
                    .id("Mod "+p.getName())
                    .provider(new ModSelect(sv, skillIx, sk, pos))
                    .size(6, 9)
                    .title(TCUtil.P + "     §lВыбор Модификатора")
                    .build().open(p);
            }));
        }

        final int maxMods = Math.min((remLvl / SM.NEW_MOD_LVL) + 1, 6);
        if (slot < maxMods) {
            final int pos = slot;
            its.set(23 * (19 + pos) / 22, ClickableItem.from(new ItemBuilder(EMPTY_MOD)
                .name(TCUtil.sided(TCUtil.N + "Модификатор")).lore("<dark_gray>(клик - заменить)").build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    SmartInventory.builder()
                        .id("Mod "+p.getName())
                        .provider(new ModSelect(sv, skillIx, sk, pos))
                        .size(6, 9)
                        .title(TCUtil.P + "     §lВыбор Модификатора")
                        .build().open(p);
                }
            }));
        }

        if (skillIx != 0) {
            its.set(2, 0, ClickableItem.of(ItemUtil.previosPage, e -> {
                skillIx--; reopen(p, its);
            }));
        }

        if (skillIx < lvl / SM.NEW_SKILL_LVL) {
            its.set(2, 8, ClickableItem.of(ItemUtil.nextPage, e -> {
                Entries.new_skill.complete(p, sv, false);
                skillIx++; reopen(p, its);
            }));
        }
    }

    private static final ItemType[] IX_TYPES = {ItemType.COPPER_BULB, ItemType.EXPOSED_COPPER_BULB,
        ItemType.WEATHERED_COPPER_BULB, ItemType.OXIDIZED_COPPER_BULB};
    private ItemType skillIt(final int ix) {
        return ix < IX_TYPES.length ? IX_TYPES[ix] : IX_TYPES[IX_TYPES.length - 1];
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
    private static String[] relate(final Modifier md, final Chastic[] chs) {
        if (chs.length == 0) return UNRELATED;
        final List<String> fnd = new ArrayList<>();
        fnd.add("<apple>Общие " + TCUtil.P + "статы " + "<apple>с навыком:");
        for (final Chastic ch : md.chastics()) {
            if (Arrays.binarySearch(chs, ch) < 0) continue;
            fnd.add(TCUtil.N + "◇ " + ch.disName());
        }
        if (fnd.size() < 2) return UNRELATED;
        return fnd.toArray(new String[0]);
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
        