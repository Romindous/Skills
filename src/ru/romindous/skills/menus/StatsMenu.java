package ru.romindous.skills.menus;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.survs.Stat;


public class StatsMenu implements InventoryProvider {
    
    

    @Override
    public void init(final Player p, final InventoryContent content) {

        final Survivor sv = PM.getOplayer(p, Survivor.class);
        
        p.playSound(p.getLocation(), Sound.ITEM_AXE_SCRAPE, 1f, 1f);

        final Stat[] stats = Stat.values();
        for (final Stat st : stats) {

            if (st.ordinal() == stats.length >> 1) {
                content.add(ClickableItem.from(new ItemBuilder(ItemType.NETHER_STAR)
                    .name(TCUtil.sided(TCUtil.P + "Главное Меню")).build(), e -> {
                    MainMenu.open(p);
                }));
            }
            
            final int currentValue = sv.getStat(st);

            content.add(ClickableItem.from(st.getItem(sv), e -> {
                if (sv.statsPoints < 1) {
                    PM.soundDeny(p);
                    return;
                }

                if (e.getEvent() instanceof final InventoryClickEvent ev) {
                    if (ev.getClick() == ClickType.LEFT) {
                        sv.setStat(st, currentValue + 1);
                        sv.statsPoints--;
                        sv.recalcStats(p);
                        p.playSound(p.getLocation(), Sound.UI_LOOM_TAKE_RESULT, 0.3f, 2);

                        reopen(p, content);
                    }
                }
            }));
        }
    }
    
    
}
