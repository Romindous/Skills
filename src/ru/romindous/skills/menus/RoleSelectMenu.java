package ru.romindous.skills.menus;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.enums.Role;

public class RoleSelectMenu implements InventoryProvider {

    public static final SmartInventory skillSelect = SmartInventory.builder()
        .id("SkillSelect")
        .provider(new RoleSelectMenu())
        .size(3, 9)
        .title("§9§lВыбери Роль")
        .build();

    private static final ItemStack cyan = new ItemBuilder(ItemType.CYAN_STAINED_GLASS_PANE).name("§0.").build();
    private static final ItemStack gray = new ItemBuilder(ItemType.GRAY_STAINED_GLASS_PANE).name("§0.").build();

    @Override
    public void init(final Player p, final InventoryContent content) {

        p.playSound(p.getEyeLocation(), Sound.ITEM_BOOK_PAGE_TURN, 2f, 1f);

        final Inventory inv = content.getInventory();
        int i = 0;
        for (; i < 27; i++) {
            inv.setItem(i, i < 9 ? cyan : gray);
        }
        i = 9;

        final Survivor sv = PM.getOplayer(p, Survivor.class);
        if (sv == null) {
            p.sendMessage("§csv==null!");
            p.closeInventory();
            return;
        }

        final int timeLeft = 86400 - (ApiOstrov.currentTimeSec() - sv.roleStamp);

        if (timeLeft > 0 && !ApiOstrov.isLocalBuilder(p)) {
            for (final Role rl : Role.values()) {
                content.set((i & 1) == 1 ? i : i + 9,
                    ClickableItem.empty(new ItemBuilder(rl.getIcon()).lore(sv.role == rl ? "§fТвой класс сейчас" : "")
                        .lore("§cДо смены: " + TimeUtil.secondToTime(timeLeft)).build()));
                i += i == 12 ? 2 : 1;
            }
        } else {
            for (final Role rl : Role.values()) {
                final boolean eq = sv.role == rl;
                content.set((i & 1) == 1 ? i : i + 9,
                    ClickableItem.from(new ItemBuilder(rl.getIcon()).lore(eq ? "§fТвой класс сейчас" : "")
                        .lore(!eq || sv.role == null ? "" : "§7Перевыбор класса - §cполный сброс §7характеристик!")
                        .build(), e -> {
                        p.closeInventory();
                        p.performCommand("skill select " + rl.name());
                    }));
                i += i == 12 ? 2 : 1;
            }

        }

        content.set(4, ClickableItem.empty(new ItemBuilder(ItemType.NETHER_STAR).name("§7<-- §fДоступные Классы §7-->").build()));
    }

}
