package ru.romindous.skills.objects;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.Tool;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.ItemGroup;
import ru.komiss77.modules.rolls.Roll;
import ru.komiss77.utils.EntityUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.config.ConfigVars;

public class SkillGroups {

    private static final String prefix = "item.";

    public static class Crawler extends ItemGroup {
        private Crawler(final ItemStack... its) {super(its);}

        public void before() {}

        public @Nullable List<Data<?>> data() {
            return null;
        }

        protected void onAttack(final EquipmentSlot[] es, final EntityDamageByEntityEvent e) {}
        protected void onDefense(final EquipmentSlot[] es, final EntityDamageEvent e) {}
        protected void onShoot(final EquipmentSlot[] es, final ProjectileLaunchEvent e) {}
        protected void onInteract(final EquipmentSlot[] es, final PlayerInteractEvent e) {}
        protected void onBreak(final EquipmentSlot[] es, final BlockBreakEvent e) {}
        protected void onPlace(final EquipmentSlot[] es, final BlockPlaceEvent e) {}
        protected void onExtra(final EquipmentSlot[] es, final PlayerEvent e) {}
    }
    public static final Crawler CRAWLER = new Crawler(new ItemBuilder(ItemType.MUTTON).name("§fПолзунина").build());

    public static class Medaline extends ItemGroup {
        private int chance;
        private float gold_speed_mul;
        private float gold_dur_mul;
        private double thorns;

        private Medaline(final ItemStack... its) {super(its);}

        public void before() {
            chance = itemConfig(this, "chance", 2);
            gold_speed_mul = (float) itemConfig(this, "gold_speed_mul", 0.8d);
            gold_dur_mul = (float) itemConfig(this, "gold_dur_mul", 1.4d);
            thorns = itemConfig(this, "thorn_dmg", 1.2d);
        }

        public List<Data<?>> data() {
            return Arrays.asList(
                new Data<Tool>(DataComponentTypes.TOOL, tl -> {
                    final Tool.Builder tb = Tool.tool().defaultMiningSpeed(tl
                        .defaultMiningSpeed()).damagePerBlock(tl.damagePerBlock());
                    for (final Tool.Rule rl : tl.rules()) {
                        if (rl.speed() == null) {tb.addRule(rl); continue;}
                        tb.addRule(Tool.rule(rl.blocks(), rl.speed() * gold_speed_mul, rl.correctForDrops()));
                    }
                    return tb.build();
                }),
                new Data<Integer>(DataComponentTypes.MAX_DAMAGE, md -> (int) (md * gold_dur_mul)));
        }

        protected void onAttack(final EquipmentSlot[] es, final EntityDamageByEntityEvent e) {
            if (has(es, EquipmentSlot.HAND) && Roll.roll(chance)) {
                e.setDamage(e.getDamage() + thorns);
                EntityUtil.effect(e.getEntity(), Sound.ENTITY_ENDER_EYE_DEATH, 1.6f, Particle.ELECTRIC_SPARK);
            }
        }

        private static final Set<DamageType> DIRECT = Set.of(DamageType.PLAYER_ATTACK, DamageType.GENERIC, DamageType.STING,
            DamageType.MOB_ATTACK, DamageType.MOB_ATTACK_NO_AGGRO, DamageType.PLAYER_EXPLOSION, DamageType.EXPLOSION, DamageType.MACE_SMASH);

        protected void onDefense(final EquipmentSlot[] es, final EntityDamageEvent e) {
            int i = 0;
            for (final EquipmentSlot s : es) {
                switch (s) {
                    case HEAD, CHEST, LEGS, FEET, BODY:
                        if (Roll.roll(chance)) i++;
                    default: break;
                }
            }
            if (i == 0) return;
            final DamageSource ds = e.getDamageSource();
            if (DIRECT.contains(ds.getDamageType()) && ds.getCausingEntity() instanceof final LivingEntity le) {
                le.damage(thorns * i, DamageSource.builder(DamageType.THORNS)
                    .withCausingEntity(e.getEntity()).withDirectEntity(e.getEntity()).build());
                EntityUtil.effect(le, Sound.ENTITY_ENDER_EYE_DEATH, 1.6f, Particle.ELECTRIC_SPARK);
            }
        }

        protected void onShoot(final EquipmentSlot[] es, final ProjectileLaunchEvent e) {}
        protected void onInteract(final EquipmentSlot[] es, final PlayerInteractEvent e) {}
        protected void onBreak(final EquipmentSlot[] es, final BlockBreakEvent e) {}
        protected void onPlace(final EquipmentSlot[] es, final BlockPlaceEvent e) {}
        protected void onExtra(final EquipmentSlot[] es, final PlayerEvent e) {}
    }
    public static final Medaline MEDAL = new Medaline(
        new ItemBuilder(ItemType.GOLD_NUGGET).name("§fКусочек медалина").build(),
        new ItemBuilder(ItemType.GLOWSTONE_DUST).name("§fМедалиновая пыль").build(),
        new ItemBuilder(ItemType.GOLDEN_SWORD).name("§fМедалиновый меч").build(),
        new ItemBuilder(ItemType.GOLDEN_PICKAXE).name("§fМедалиновая кирка").build(),
        new ItemBuilder(ItemType.GOLDEN_AXE).name("§fМедалиновый топор").build(),
        new ItemBuilder(ItemType.GOLDEN_HOE).name("§fМедалиновый посох").build(),
        new ItemBuilder(ItemType.GOLDEN_SHOVEL).name("§fМедалиновая лопата").build(),
        new ItemBuilder(ItemType.GOLDEN_HELMET).name("§fМедалиновый шлем").build(),
        new ItemBuilder(ItemType.GOLDEN_CHESTPLATE).name("§fМедалиновый нагрудник").build(),
        new ItemBuilder(ItemType.GOLDEN_LEGGINGS).name("§fМедалиновые поножи").build(),
        new ItemBuilder(ItemType.GOLDEN_BOOTS).name("§fМедалиновые ботинки").build());

    public static class Serebrite extends ItemGroup {
        private int chance;
        private int arm_tough;
        private float iron_dur_mul;
        private float iron_speed_mul;

        private Serebrite(final ItemStack... its) {super(its);}

        public void before() {
            chance = itemConfig(this, "chance", 8);
            arm_tough = itemConfig(this, "arm_tough", 1);
            iron_dur_mul = (float) itemConfig(this, "iron_dur_mul", 0.8d);
            iron_speed_mul = (float) itemConfig(this, "iron_speed_mul", 1.2d);
        }

        public List<Data<?>> data() {
            return Arrays.asList(
                new Data<Tool>(DataComponentTypes.TOOL, tl -> {
                    final Tool.Builder tb = Tool.tool().defaultMiningSpeed(tl
                        .defaultMiningSpeed()).damagePerBlock(tl.damagePerBlock());
                    for (final Tool.Rule rl : tl.rules()) {
                        if (rl.speed() == null) {tb.addRule(rl); continue;}
                        tb.addRule(Tool.rule(rl.blocks(), rl.speed() * iron_speed_mul, rl.correctForDrops()));
                    }
                    return tb.build();
                }),
                new Data<ItemAttributeModifiers>(DataComponentTypes.ATTRIBUTE_MODIFIERS, ats -> {
                    final ItemAttributeModifiers.Builder tb = ItemAttributeModifiers
                        .itemAttributes().showInTooltip(ats.showInTooltip());
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
                new Data<Integer>(DataComponentTypes.MAX_DAMAGE, md -> (int) (md * iron_dur_mul)));
        }

        protected void onAttack(final EquipmentSlot[] es, final EntityDamageByEntityEvent e) {
            if (has(es, EquipmentSlot.HAND) && Roll.roll(chance)
                && e.getDamageSource().getCausingEntity() instanceof final LivingEntity le) {
                final LivingEntity mini = Main.mobs.MINI_SILVERFISH.spawn(e.getEntity().getLocation(), le);
                EntityUtil.effect(le, Sound.ENTITY_SILVERFISH_AMBIENT, 0.6f, Particle.CAMPFIRE_COSY_SMOKE);
                mini.setNoDamageTicks(10);
            }
        }
        protected void onDefense(final EquipmentSlot[] es, final EntityDamageEvent e) {
            boolean spawn = false;
            for (final EquipmentSlot s : es) {
                switch (s) {
                    case HEAD, CHEST, LEGS, FEET:
                        if (!spawn && Roll.roll(chance)) spawn = true;
                    default: break;
                }
            }
            if (spawn && e.getEntity() instanceof final LivingEntity le) {
                final LivingEntity mini = Main.mobs.MINI_SILVERFISH.spawn(le.getLocation(), le);
                EntityUtil.effect(le, Sound.ENTITY_SILVERFISH_AMBIENT, 0.6f, Particle.CAMPFIRE_COSY_SMOKE);
                mini.setNoDamageTicks(10);
            }
        }
        protected void onShoot(final EquipmentSlot[] es, final ProjectileLaunchEvent e) {}
        protected void onInteract(final EquipmentSlot[] es, final PlayerInteractEvent e) {}
        protected void onBreak(final EquipmentSlot[] es, final BlockBreakEvent e) {}
        protected void onPlace(final EquipmentSlot[] es, final BlockPlaceEvent e) {}
        protected void onExtra(final EquipmentSlot[] es, final PlayerEvent e) {}
    }
    public static final Serebrite SILVER = new Serebrite(
        new ItemBuilder(ItemType.IRON_NUGGET).name("§fКусочек серебрита").glint(true).build(),
        new ItemBuilder(ItemType.IRON_INGOT).name("§fСеребритовый слиток").glint(true).build(),
        new ItemBuilder(ItemType.RAW_IRON).name("§fРудный серебрит").glint(true).build(),
        new ItemBuilder(ItemType.SUGAR).name("§fСеребритовая пыль").glint(true).build(),
        new ItemBuilder(ItemType.IRON_SWORD).name("§fСеребритовая меч").glint(true).build(),
        new ItemBuilder(ItemType.IRON_PICKAXE).name("§fСеребритовая кирка").glint(true).build(),
        new ItemBuilder(ItemType.IRON_AXE).name("§fСеребритовый топор").glint(true).build(),
        new ItemBuilder(ItemType.IRON_HOE).name("§fСеребритовый посох").glint(true).build(),
        new ItemBuilder(ItemType.IRON_SHOVEL).name("§fСеребритовая лопата").glint(true).build(),
        new ItemBuilder(ItemType.CHAINMAIL_HELMET).name("§fСеребритовый шлем").glint(true).build(),
        new ItemBuilder(ItemType.CHAINMAIL_CHESTPLATE).name("§fСеребритовый нагрудник").glint(true).build(),
        new ItemBuilder(ItemType.CHAINMAIL_LEGGINGS).name("§fСеребритовые поножи").glint(true).build(),
        new ItemBuilder(ItemType.CHAINMAIL_BOOTS).name("§fСеребритовые ботинки").glint(true).build(),
        new ItemBuilder(ItemType.PHANTOM_MEMBRANE).name("§fСеребритовая чешуя").glint(true).build());


    public static boolean has(final EquipmentSlot[] ess, final EquipmentSlot e) {
        for (final EquipmentSlot es : ess) if (es == e) return true;
        return false;
    }

    public static int itemConfig(final ItemGroup cm, final String id, final int val) {
        return ConfigVars.get(prefix + cm.key().value() + "." + id, val);
    }

    public static double itemConfig(final ItemGroup cm, final String id, final double val) {
        return ConfigVars.get(prefix + cm.key().value() + "." + id, val);
    }

    /*DEFAULT(),//10
    MEDALINE("GOLD_NUGGET&1&§fКусочек медалина&&11", 
    		"GLOWSTONE_DUST&1&§fМедалиновый порошок&&11"),//11
    SEREBRITE("RAW_IRON&1&§fРудный серебрит&&12&G", 
    		"IRON_INGOT&1&§fCеребритовый слиток&&12&G", 
    		"IRON_NUGGET&1&§fКусочек серебрита&&12&G", 
    		"GUNPOWDER&1&§fСеребритовый порошок&&12&G"),//12
    SPARK("GUNPOWDER&1&§eЗаряженый порох&;§7Издаёт §eискры;§7падает с босса&13&G", 
    		"FLINT&1&§eЭлектро-Кремень&&13&G"),//13
    GRENADE,//14
    AZULITE("PRISMARINE_SHARD&1&§eАзулит&&15&G"),//15
    BONED("PHANTOM_MEMBRANE&1&§fКостяная пластина&&16"),//16
    UNDEAD("BONE_MEAL&1&§eОживший прах&;§7До сих пор §eшевелится;§7падает с босса&17&G", 
    		"BONE&1&§eОживший реликт&&17&G"),//17
    LUCKOR("RAW_GOLD&1&§eРудная лукория&&18&G", 
    		"GOLD_INGOT&1&§bЛукориевый слиток&&18&G", 
    		"GOLD_NUGGET&1&§bЛукориевый кусочек&&18&G", 
    		"GLOWSTONE_DUST&1&§bЛукориевый порошок&&18&G", 
    		"ENDER_PEARL&1&§bАлчный жемчуг&&18&G"),//18
    WAXED("ORANGE_DYE&1&§fВосковая гуща&&19", 
    		"CLAY_BALL&1&§eРудный восковит&&19", 
    		"BRICK&1&§eВосковитовый слиток&&19"),//19
    AMETHYST("POPPED_CHORUS_FRUIT&1&§eАметистовая пластина&&20"),//20
    BRAID("STRING&1&§fРастительное волокно&&21"),//21
    EXTRA("GLOWSTONE_DUST&1&§fЗолотой порошок&&22", 
    		"GUNPOWDER&1&§fЖелезный порошок&&22", 
    		"GREEN_DYE&1&§eСвязка бамбука&&22"),//22
    ACID("FERMENTED_SPIDER_EYE&1&§bКатализатор&;§7Ускоряет внутренние;§bреакции§7, падает с босса&23&G"),//23
    HELLITE("NETHER_BRICK&1&§eХеллитовый слиток&&24"),//24
    BLAZE("BLAZE_ROD&1&§dОкисленный стержень&&25&G", 
    		"COPPER_INGOT&1&§bПолыхающий слиток&&25&G", 
    		"ENDER_EYE&1&§dОко жадности&&25&G"),//25
    RESTILE("AMETHYST_SHARD&1&§bОсколок ристалии&&26&G"),//26
    WITHER("CHARCOAL&1&§bВизерит&&27"),//27
    BARATRA("RABBIT_HIDE&1&§dШкура зверя&;§dПрочная §7и §dчерствая;§7падает с босса&28&G", 
    		"BRICK&1&§dБаратровый слиток&&28&G"),//28
    TURTLE(),//29
    SIPHOR("BLACK_DYE&1&§bТемная связка&&30&G", 
    		"GLOW_INK_SAC&1&§dФрагмент эха&&30", 
    		"HEART_OF_THE_SEA&1&§5Яремное эхо&;§7Заключенный §5звук;§7охваченый кристалами&30"),//30
    CHITIN("RED_DYE&1&§dХитиновая чешуя&&31&G"),//31
    VOID("PRISMARINE_CRYSTALS&1&§dГлубинный кристал&;§7Взывает к §dпустоте;§7падает с босса&32&G", 
    		"ENDER_EYE&1&§5Жемчуг пустоты&&32&G", 
    		"ECHO_SHARD&1&§5Стержень пустоты&&32&G"),//32
    SHULK("LIGHT_GRAY_DYE&1&§bЭссенция шалка&&33", 
    		"WHITE_DYE&1&§dШалковая связка&&33&G", 
    		"GHAST_TEAR&1&§dШалковый экстракт&&33&G"),//33
    CALYX("POPPED_CHORUS_FRUIT&1&§5Калексовая пластина&&34&G"),//34
    IRRITE("PHANTOM_MEMBRANE&1&§dПлотная клетчатка&;§dЛегкая §7как перо;§7падает с босса&35&G", 
    		"NETHERITE_SCRAP&1&§5Ирритовая пластина&&35&G"),//34
    ;

    private static final EnumMap<CustomItems, ItemStack[]> materials = new EnumMap<>(CustomItems.class);

    static {
        for (final CustomItems ci : CustomItems.values()) {
            if (ci.mats != null && ci.mats.length != 0) {
                final ItemStack[] its = new ItemStack[ci.mats.length], 0; i < its.length; i++) {
                    its[i] = Crafts.getItemStack(ci.mats[i]);
                }
                materials.put(ci, its);
            }
        }
    }

    private String[] mats;

    private CustomItems(final String... mats) {
        this.mats = mats;
    }
    
    public static CustomItems getCstmItm(final ItemMeta im) {
        if (im != null && im.hasCustomModelData()) {
            final int model = im.getCustomModelData() - 10;
            if (model>0 && model<values().length) {
                return values()[model];
            }
        }
        return DEFAULT;
    }
    
    public static CustomItems getCstmItm(final Integer cmd) {
        if (cmd == null) return null, cmd - 10;
        if (model>0 && model<values().length) {
            return values()[model];
        }
        return DEFAULT;
    }

    public static ItemStack[] getCustomMats(final ItemMeta im) {
        return getCustomMats(getCstmItm(im));
    }

    private static final ItemStack[] eis = new ItemStack[0];
    public static ItemStack[] getCustomMats(final CustomItems ci) {
    	final ItemStack[] is = materials.get(ci);
        return is == null ? eis : is;
    }

    public float getPercentArmor(final ItemStack[] armor) {
        int part = 0;
        switch (armor.length) {
            case 4:
                if (!ItemUtil.isBlankItem(armor[3], true) && getCstmItm(armor[3].getItemMeta()) == this) {
                    part += 2;
                }
            case 3:
                if (!ItemUtil.isBlankItem(armor[2], true) && getCstmItm(armor[2].getItemMeta()) == this) {
                    part += 4;
                }
            case 2:
                if (!ItemUtil.isBlankItem(armor[1], true) && getCstmItm(armor[1].getItemMeta()) == this) {
                    part += 3;
                }
            case 1:
                if (!ItemUtil.isBlankItem(armor[0], true) && getCstmItm(armor[0].getItemMeta()) == this) {
                    part += 1;
                }
                return part * 0.1f;
            default:
                return 0f;
        }
    }*/
}
