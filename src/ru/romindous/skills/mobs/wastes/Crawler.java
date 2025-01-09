package ru.romindous.skills.mobs.wastes;

import java.util.Map;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Spider;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.ItemRoll;
import ru.komiss77.modules.rolls.NARoll;
import ru.komiss77.modules.rolls.RollTree;
import ru.komiss77.utils.NumUtil;
import ru.romindous.skills.items.SkillGroups;
import ru.romindous.skills.mobs.SednaMob;

public class Crawler extends SednaMob {

    public String biome() {
        return "ruined_city";
    }

    @Override
    protected Class<? extends LivingEntity> getEntClass() {
        return Spider.class;
    }

    @Override
    public Map<EquipmentSlot, ItemStack> equipment() {
        return Map.of();
    }

    public static final ItemStack WEB = new ItemBuilder(ItemType.COBWEB).build();

    @Override
    protected void onTarget(final EntityTargetEvent e) {
        if (e.getTarget() instanceof LivingEntity && e.getEntity() instanceof final Mob mb) {
            final Snowball sb = mb.launchProjectile(Snowball.class, NumUtil.getShotVec(
                e.getTarget().getLocation().subtract(mb.getLocation()).toVector(), 1.2d), s -> {
                s.setItem(WEB);
            });

            Ostrov.sync(() -> {
                if (sb.isValid()) sb.remove();
            }, 100);
        }
    }

    private final RollTree drop = RollTree.of(key().value())
        .add(new ItemRoll(key().value() + "_string", new ItemBuilder(ItemType.STRING).build(), 0, 2), 4)
        .add(new ItemRoll(key().value() + "_meat", SkillGroups.CRAWLER.item(ItemType.MUTTON), 1, 0), 1)
        .add(new ItemRoll(key().value() + "_eye", new ItemBuilder(ItemType.SPIDER_EYE).build(), 1, 0), 2)
        .add(new NARoll(), 4).build(1, 1);

    @Override
    public RollTree loot() {
        return drop;
    }
}
