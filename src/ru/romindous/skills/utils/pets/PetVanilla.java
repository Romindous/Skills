package ru.romindous.skills.utils.pets;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.romindous.skills.Main;
import ru.romindous.skills.Survivor;

public class PetVanilla implements IPetManager {

    @Override
    public boolean petCmd(final Player p, final Survivor sv) {
        p.sendMessage(Main.prefix + "Питомцы пока-что отключены!");
        return false;
    }

    @Override
    public ClickableItem getMenuItem(final Player p) {
        return ClickableItem.empty(new ItemBuilder(Material.RABBIT_HIDE).name("§8§kk§7 Твой Питомец §8§kk")
                .lore("")
                .lore("§7Питомцы недоступны на данный момент...")
                .build());
    }

    @Override
    public void removePet(Player p) {
    }
    
    @Override
    public List<String> getDebugInfo(List<String> lore) {
        return lore;
    }

}