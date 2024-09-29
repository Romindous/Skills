package ru.romindous.skills.menus;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.enums.Stat;


public class StatsMenu implements InventoryProvider {
    
    

    @Override
    public void init(final Player p, final InventoryContent content) {

        final Survivor sv = PM.getOplayer(p, Survivor.class);
        
        p.playSound(p.getLocation(), Sound.ITEM_AXE_SCRAPE, 0.3f, 1);
        
        for (final Stat st : Stat.values()) {
            
            final int currentValue = sv.getStat(st);
            
            /*final List<String>lore = new ArrayList<>();
            
            if (sv.role.stat==st) {
                lore.add("§f§nОснова класса");
                lore.add("");
            }
            lore.add(st.getName());
            lore.add("");
            lore.add("§7Сейчас: §b§l"+currentValue);
            //lore.add(currentValue>=SM.MAX_STAT_VALUE ? "§6Перед улучшения" : "");
            
            if (sv.statsPoints >0) {
                lore.add("§7Доступно очков статистики: §b"+sv.statsPoints);
                lore.add("§7ЛКМ - §6улучшить на 1");
                if (sv.statsPoints > 1) {
                    lore.add("§7Шифт+ЛКМ - §6улучшить на все");
                    lore.add("§7Шифт+ПКМ - §6автораспределение");
                } else {
                    
                }
            } else {
                lore.add("§7Для развития нужны");
                lore.add("§6Очки статистики");
                lore.add("§7(добавляются с повышением уровня)");
            }*/
            
            
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
                        p.playSound(p.getLocation(), "ui_loom_take_result", 0.3f, 2);

                        reopen(p, content);
                    }

                    /*case SHIFT_LEFT -> {
                        final int add = sv.statsPoints;
                        sv.setStat(st, currentValue+add);
                        sv.statsPoints -= add;
                        sv.recalcStats(p);
                        p.playSound(p.getLocation(), "ui_loom_take_result", 0.3f, 2);
                        p.playSound(p.getLocation(), "ui_loom_take_result", 0.3f, 1.5f);
                    }

                    case SHIFT_RIGHT -> {
                        if (sv.statsPoints <2) return;
                        while (sv.statsPoints >0) {
                            for (final Stats st_ : Stats.values()) {
                               sv.setStat(st_, sv.getStat(st_)+1);
                               sv.statsPoints--;
                            }
                        }
                        sv.recalcStats(p);
                        p.playSound(p.getLocation(), "ui_loom_select_pattern", 0.3f, .5f);
                    }*/
                }
            })); 
            
        }
    }
    
    
}
