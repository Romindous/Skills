package ru.romindous.skills.items.groups;

import javax.annotation.Nullable;
import java.util.List;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.romindous.skills.items.SkillGroup;

public class Crawler extends SkillGroup {

    public Crawler(final ItemStack... its) {
        super(its);
    }

    public void before() {}

    public @Nullable List<Data<?>> data() {
        return null;
    }

    protected void onAttack(final EquipmentSlot[] es, final EntityDamageByEntityEvent e) {}
    protected void onDefense(final EquipmentSlot[] es, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot[] es, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot[] es, final PlayerInteractEvent e) {}
}
