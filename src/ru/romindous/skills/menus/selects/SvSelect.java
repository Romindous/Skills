package ru.romindous.skills.menus.selects;

import javax.annotation.Nullable;
import org.bukkit.entity.Player;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.skills.Skill;

public abstract class SvSelect implements InventoryProvider {

    protected final Survivor sv;
    protected final @Nullable Skill sk;
    protected final int skIx;

    protected SvSelect(final Survivor sv, final int skIx, final @Nullable Skill sk) {
        this.sv = sv;
        this.sk = sk;
        this.skIx = skIx;
    }

    public void openLast(final Player p) {
        sv.skillMenu.skillIx = skIx;
        p.closeInventory();
        sv.skillInv.open(p);
    }
}
