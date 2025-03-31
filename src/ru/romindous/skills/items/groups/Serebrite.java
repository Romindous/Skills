package ru.romindous.skills.items.groups;

import java.util.Arrays;
import java.util.List;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.Tool;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.rolls.Roll;
import ru.komiss77.utils.EntityUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.items.SkillGroup;
import ru.romindous.skills.listeners.DamageLst;

public class Serebrite extends SkillGroup {
    private int chance;
    private int arm_tough;
    private float iron_dur_mul;
    private float iron_speed_mul;

    public Serebrite(final ItemStack... its) {
        super(its);
    }

    public void before() {
        chance = itemConfig("chance", 4);
        arm_tough = itemConfig("arm_tough", 1);
        iron_dur_mul = (float) itemConfig("iron_dur_mul", 0.8d);
        iron_speed_mul = (float) itemConfig("iron_speed_mul", 1.2d);
    }

    public List<Data<?>> data() {
        return Arrays.asList(
            new Data<>(DataComponentTypes.TOOL, tl -> {
                final Tool.Builder tb = Tool.tool().defaultMiningSpeed(tl
                    .defaultMiningSpeed()).damagePerBlock(tl.damagePerBlock());
                for (final Tool.Rule rl : tl.rules()) {
                    if (rl.speed() == null) {
                        tb.addRule(rl);
                        continue;
                    }
                    tb.addRule(Tool.rule(rl.blocks(), rl.speed() * iron_speed_mul, rl.correctForDrops()));
                }
                return tb.build();
            }),
            new Data<>(DataComponentTypes.ATTRIBUTE_MODIFIERS, ats -> {
                final ItemAttributeModifiers.Builder tb = ItemAttributeModifiers.itemAttributes();
                for (final ItemAttributeModifiers.Entry en : ats.modifiers()) {
                    tb.addModifier(en.attribute(), en.modifier(), en.getGroup());
                    if (Attribute.ARMOR.equals(en.attribute())) {
                        final AttributeModifier am = en.modifier();
                        tb.addModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(OStrap.key(key()),
                            arm_tough, AttributeModifier.Operation.ADD_NUMBER, am.getSlotGroup()), en.getGroup());
                    }
                }
                return tb.build();
            }),
            new Data<>(DataComponentTypes.MAX_DAMAGE, md -> (int) (md * iron_dur_mul)));
    }

    private static final float MIN_CD = 5.0f;

    protected void onAttack(final EquipmentSlot[] es, final EntityDamageByEntityEvent e) {
        final DamageSource ds = e.getDamageSource();
        if (DamageLst.DIRECT.contains(ds.getDamageType()) && SkillGroup.has(es, EquipmentSlot.HAND) && Roll.roll(chance)
            && e.getEntity() instanceof final LivingEntity tgt && ds.getCausingEntity() instanceof final LivingEntity dmgr) {
            if (dmgr instanceof final Player pl && (pl.getAttackCooldown() != 1f || pl.getCooldownPeriod() <= MIN_CD)) return;
            final LivingEntity mini = Main.mobs.MINI_SILVERFISH.spawn(e.getEntity().getLocation(), dmgr);
            EntityUtil.effect(tgt, Sound.ENTITY_SILVERFISH_AMBIENT, 0.6f, Particle.CLOUD);
            if (mini instanceof final Mob mm) mm.setTarget(tgt);
            mini.setNoDamageTicks(10);
        }
    }

    protected void onDefense(final EquipmentSlot[] es, final EntityDamageEvent e) {
        boolean spawn = false;
        for (final EquipmentSlot s : es) {
            switch (s) {
                case HEAD, CHEST, LEGS, FEET:
                    if (!spawn) spawn = Roll.roll(chance);
                default: break;
            }
        }
        final DamageSource ds = e.getDamageSource();
        if (spawn && DamageLst.DIRECT.contains(ds.getDamageType())
            && e.getEntity() instanceof final LivingEntity tgt
            && ds.getCausingEntity() instanceof final LivingEntity dmgr) {
            final LivingEntity mini = Main.mobs.MINI_SILVERFISH.spawn(tgt.getLocation(), tgt);
            EntityUtil.effect(tgt, Sound.ENTITY_SILVERFISH_AMBIENT, 0.6f, Particle.CLOUD);
            if (mini instanceof final Mob mm) mm.setTarget(dmgr);
            mini.setNoDamageTicks(10);
        }
    }

    protected void onShoot(final EquipmentSlot[] es, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot[] es, final PlayerInteractEvent e) {}
}
