package ru.romindous.skills.items;

import javax.annotation.Nullable;
import java.util.List;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.items.ItemGroup;

public class Crawler extends ItemGroup {
    Crawler(final ItemStack... its) {
        super(its);
    }

    public void before() {
    }

    public @Nullable List<Data<?>> data() {
        return null;
    }

    protected void onAttack(final EquipmentSlot[] es, final EntityDamageByEntityEvent e) {
    }

    protected void onDefense(final EquipmentSlot[] es, final EntityDamageEvent e) {
    }

    protected void onShoot(final EquipmentSlot[] es, final ProjectileLaunchEvent e) {
    }

    protected void onInteract(final EquipmentSlot[] es, final PlayerInteractEvent e) {
    }

    protected void onBreak(final EquipmentSlot[] es, final BlockBreakEvent e) {
    }

    protected void onPlace(final EquipmentSlot[] es, final BlockPlaceEvent e) {
    }

    protected void onExtra(final EquipmentSlot[] es, final PlayerEvent e) {
    }
}
