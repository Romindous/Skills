package ru.romindous.skills.utils.pets;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.romindous.skills.survs.Survivor;

public interface IPetManager extends Listener {
	
    boolean petCmd(final Player p, final Survivor sv);

    ClickableItem getMenuItem(final Player p);

    void removePet(final Player p);
    
    List<String> getDebugInfo(List<String> bossLore);

}
