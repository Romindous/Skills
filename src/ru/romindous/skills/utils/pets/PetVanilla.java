package ru.romindous.skills.utils.pets;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.romindous.skills.Main;
import ru.romindous.skills.survs.Survivor;

public class PetVanilla implements IPetManager {

    @Override
    public boolean petCmd(final Player p, final Survivor sv) {
        p.sendMessage(Main.prefix + "Питомцы пока-что отключены!");
        return false;
    }

    @Override
    public ClickableItem getMenuItem(final Player p) {
        return ClickableItem.empty(new ItemBuilder(ItemType.RABBIT_HIDE).name("§8<obf>k</obf>§7 Твой Питомец §8<obf>k")
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
