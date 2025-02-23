package ru.romindous.skills.menus;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.skills.Main;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.guides.Entries;
import ru.romindous.skills.skills.Rarity;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.survs.SM;
import ru.romindous.skills.survs.Survivor;

public class UpgradeMenu implements InventoryProvider {

    private static final int SOUL_MUL = SM.value("upg_soul_mul", 4);
    private static final int RARITY_MUL = SM.value("upg_rarity_mul", 2);
    private static final int LVL_MUL = ConfigVars.get(SM.PREFIX + "upg_lvl_mul", 1);

    private final Scroll.State state;

    private UpgradeMenu(final Scroll.State state) {
        this.state = state;
    }

    public static void ask(final Player p) {
        p.playSound(p, Sound.BLOCK_CONDUIT_ACTIVATE, 1f, 1.4f);
        p.sendMessage(TCUtil.form(Main.prefix + "Создай <gray>Наковальню" + TCUtil.N
            + ", и нажми по ней " + TCUtil.P + "ПКМ " + TCUtil.N + "скрижальями!"));
        ScreenUtil.sendTitle(p, "", TCUtil.N + "Нажми ПКМ на <gray>Наковальню");
        p.closeInventory();
    }

    public static boolean tryOpen(final Player p) {
        final Survivor sv = PM.getOplayer(p, Survivor.class);
        final ItemStack hand = p.getInventory().getItem(EquipmentSlot.HAND);
        final PersistentDataContainerView pdc = hand.getPersistentDataContainer();
        final Integer lvl = pdc.get(OStrap.key(Scroll.LVL), PersistentDataType.INTEGER);
        if (lvl == null) return false;
        if (hand.getAmount() < 2) {
            p.sendMessage(TCUtil.form(Main.prefix + "<red>Нужно иметь более одной такой скрижали!"));
            return true;
        }
        final Selector sl = Selector.VALUES.get(pdc.get(OStrap.key(Selector.data), PersistentDataType.STRING));
        if (sl != null) {
            final Role rl = sl.role();
            if (rl != null && rl != sv.role) {
                p.sendMessage(TCUtil.form(Main.prefix + sl.rarity().color() + sl.name()
                    + " <red>может прокачать только роль " + rl.disName()));
                return true;
            }
            final Selector.SelState sls = new Selector.SelState(sl, lvl);
            sv.change(sls, 2);
            hand.setAmount(hand.getAmount() - 2);
            p.getInventory().setItem(EquipmentSlot.HAND, hand);
            open(p, sls);
            return true;
        }
        final Ability ab = Ability.VALUES.get(pdc.get(OStrap.key(Ability.data), PersistentDataType.STRING));
        if (ab != null) {
            final Role rl = ab.role();
            if (rl != null && rl != sv.role) {
                p.sendMessage(TCUtil.form(Main.prefix + ab.rarity().color() + ab.name()
                    + " <red>может прокачать только роль " + rl.disName()));
                return true;
            }
            final Ability.AbilState abs = new Ability.AbilState(ab, lvl);
            sv.change(abs, 2);
            hand.setAmount(hand.getAmount() - 2);
            p.getInventory().setItem(EquipmentSlot.HAND, hand);
            open(p, abs);
            return true;
        }
        final Modifier md = Modifier.VALUES.get(pdc.get(OStrap.key(Modifier.data), PersistentDataType.STRING));
        if (md != null) {
            final Role rl = md.role();
            if (rl != null && rl != sv.role) {
                p.sendMessage(TCUtil.form(Main.prefix + md.rarity().color() + md.name()
                    + " <red>может прокачать только роль " + rl.disName()));
                return true;
            }
            final Modifier.ModState mds = new Modifier.ModState(md, lvl);
            sv.change(mds, 2);
            hand.setAmount(hand.getAmount() - 2);
            p.getInventory().setItem(EquipmentSlot.HAND, hand);
            open(p, mds);
            return true;
        }
        return false;
    }

    private static void open(final Player p, final Scroll.State st) {
        SmartInventory.builder()
            .id("Upgrade "+p.getName())
            .provider(new UpgradeMenu(st))
            .type(InventoryType.ANVIL)
            .title(TCUtil.P + " §lПрокачка Скрижали")
            .build().open(p);
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p, Sound.BLOCK_BONE_BLOCK_BREAK, 1f, 0.6f);

        final Survivor sv = PM.getOplayer(p, Survivor.class);
        final Scroll scr = state.val();
        final int inl = state.lvl();
        its.set(0, ClickableItem.of(new ItemBuilder(scr.display(inl)).deLore()
            .lore(scr.next(inl)).lore(TCUtil.P + "Клик - отмена").amount(2).build(), e -> {
            sv.skillInv.open((Player) e.getWhoClicked());
        }));
        final int req = ((scr.rarity().ordinal() + 1) * RARITY_MUL
            + NumUtil.square(inl + 1)) * SOUL_MUL;
        final int lvl = ((scr.rarity().ordinal() + 1) * RARITY_MUL
            + NumUtil.square(inl + 1)) * LVL_MUL;
        if ((int) sv.mana() < req) {
            final String name = "<red>Нужно еще " + Main.manaClr + (req - (int) sv.mana()) + " душ<red>!";
            its.set(1, ClickableItem.empty(new ItemBuilder(Rarity.EMT_SOUL).name(name).build()));
            its.set(2, ClickableItem.empty(new ItemBuilder(ItemType.GRAY_DYE).name(name).build()));
            return;
        }
        its.set(1, ClickableItem.empty(new ItemBuilder(scr.rarity().soul())
            .name(TCUtil.N + "Есть " + Main.manaClr + req + " душ" + TCUtil.N + "!").build()));
        its.set(2, sv.getLevel() < lvl ? ClickableItem.empty(new ItemBuilder(ItemType.GRAY_DYE)
            .name(TCUtil.P + "Нужно достигнуть " + sv.role.color() + lvl + " уровня" + TCUtil.P + "!").build())
            : ClickableItem.of(new ItemBuilder(scr.display(inl + 1))
            .lore(TCUtil.N + "Есть " + sv.role.color() + "уровень " + TCUtil.N + "для прокачки!")
            .lore("<apple>Клик - соеденить").build(), e -> {
            if ((int) sv.mana() < req) {
                reopen(p, its);
                return;
            }
            if (sv.count(state) < 2) {
                sv.skillInv.open((Player) e.getWhoClicked());
                return;
            }
            final int more = switch (state) {
                case final Selector.SelState ss -> sv.change(new Selector
                    .SelState(ss.val(), ss.lvl() + 1), 1);
                case final Ability.AbilState as -> sv.change(new Ability
                    .AbilState(as.val(), as.lvl() + 1), 1);
                case final Modifier.ModState ms -> sv.change(new Modifier
                    .ModState(ms.val(), ms.lvl() + 1), 1);
                default -> 0;
            };
            if (more == 0) return;
            sv.change(state, -2);
            sv.chgMana(e.getWhoClicked(), -req);
            Entries.combine.complete(p, sv, false);
            p.sendMessage(TCUtil.form(Main.prefix + "Обе скрижали " + scr.rarity().color()
                + "ур. " + Scroll.toINums(inl) + TCUtil.N + " теперь"));
            p.sendMessage(TCUtil.form(TCUtil.N + "соеденены в " + TCUtil.sided(scr.name(inl + 1),
                    scr.side()) + TCUtil.N + " (" + TCUtil.P + more + TCUtil.N + ")"));
            p.playSound(p, Sound.BLOCK_SMITHING_TABLE_USE, 1f, 0.8f);
            sv.skillInv.open((Player) e.getWhoClicked());
        }));
    }

}
