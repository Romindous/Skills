package ru.romindous.skills.items.groups;

import java.util.Arrays;
import java.util.List;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.rolls.Roll;
import ru.komiss77.utils.EntityUtil;
import ru.romindous.skills.items.SkillGroup;
import ru.romindous.skills.listeners.DamageLst;

public class Medaline extends SkillGroup {
    private int chance;
    private float gold_speed_mul;
    private float gold_dur_mul;
    private double thorns;

    public Medaline(final ItemStack... its) {
        super(its);
    }

    public void before() {
        chance = itemConfig("chance", 2);
        gold_speed_mul = (float) itemConfig("gold_speed_mul", 0.8d);
        gold_dur_mul = (float) itemConfig("gold_dur_mul", 1.4d);
        thorns = itemConfig("thorn_dmg", 1.2d);
    }

    public List<Data<?>> data() {
        return Arrays.asList(
            new Data<Tool>(DataComponentTypes.TOOL, tl -> {
                final Tool.Builder tb = Tool.tool().defaultMiningSpeed(tl
                    .defaultMiningSpeed()).damagePerBlock(tl.damagePerBlock());
                for (final Tool.Rule rl : tl.rules()) {
                    if (rl.speed() == null) {
                        tb.addRule(rl);
                        continue;
                    }
                    tb.addRule(Tool.rule(rl.blocks(), rl.speed() * gold_speed_mul, rl.correctForDrops()));
                }
                return tb.build();
            }),
            new Data<Integer>(DataComponentTypes.MAX_DAMAGE, md -> (int) (md * gold_dur_mul)));
    }

    protected void onAttack(final EquipmentSlot[] es, final EntityDamageByEntityEvent e) {
        final DamageSource ds = e.getDamageSource();
        if (DamageLst.DIRECT.contains(ds.getDamageType())
            && SkillGroup.has(es, EquipmentSlot.HAND) && Roll.roll(chance)) {
            e.setDamage(e.getDamage() + thorns);
            EntityUtil.effect(e.getEntity(), Sound.ENTITY_ENDER_EYE_DEATH, 1.6f, Particle.ELECTRIC_SPARK);
        }
    }

    protected void onDefense(final EquipmentSlot[] es, final EntityDamageEvent e) {
        int i = 0;
        for (final EquipmentSlot s : es) {
            switch (s) {
                case HEAD, CHEST, LEGS, FEET, BODY:
                    if (Roll.roll(chance)) i++;
                default:
                    break;
            }
        }
        if (i == 0) return;
        final DamageSource ds = e.getDamageSource();
        if (DamageLst.DIRECT.contains(ds.getDamageType()) && ds.getCausingEntity() instanceof final LivingEntity le) {
            le.damage(thorns * i, DamageSource.builder(DamageType.THORNS)
                .withCausingEntity(e.getEntity()).withDirectEntity(e.getEntity()).build());
            EntityUtil.effect(le, Sound.ENTITY_ENDER_EYE_DEATH, 1.6f, Particle.ELECTRIC_SPARK);
        }
    }

    protected void onShoot(final EquipmentSlot[] es, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot[] es, final PlayerInteractEvent e) {}
}
