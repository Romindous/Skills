package ru.romindous.skills.listeners;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.CaveVinesPlant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.OStrap;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.enums.Stat;
import ru.romindous.skills.enums.Trigger;
import ru.romindous.skills.objects.ItemTags;
import ru.romindous.skills.objects.Scroll;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;


public class InteractLst implements Listener {

    private static final PotionType clr = PotionType.THICK;
    private static final double STAFF_DMG = 2.0d;

    //в LockListener HIGH
    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = false)
    public void onIntr(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final Survivor sv = PM.getOplayer(p, Survivor.class);
        if (sv == null) return;
        final ItemStack hand = e.getItem();
        final Block b;
        switch (e.getAction()) {
            case LEFT_CLICK_AIR:
                if (p.isSneaking()) sv.trigger(Trigger.SHIFT_LEFT, e, p);
                break;
            case LEFT_CLICK_BLOCK:
                if (hand != null) {
                    if (ItemTags.STAFFS.contains(hand.getType().asItemType())) {
                        b = e.getClickedBlock();
                        if (b.getBlockData() instanceof Ageable) {
                            final Ageable ag = (Ageable) b.getBlockData().clone();
                            if (ag.getAge() == ag.getMaximumAge()) {
                                e.setUseInteractedBlock(Result.DENY);
                                b.breakNaturally(hand);
                                ag.setAge(0);
                                b.setBlockData(ag, false);
                                Nms.swing(p, e.getHand());
                            }
                            break;
                        }
                    }
                }
                if (p.isSneaking()) sv.trigger(Trigger.SHIFT_LEFT, e, p);
                break;
            case RIGHT_CLICK_AIR:
                if (p.isSneaking()) sv.trigger(Trigger.SHIFT_RIGHT, e, p);
                if (hand != null) {
                    final Material hm = hand.getType();
                    if (p.hasCooldown(hm)) {
                        e.setUseItemInHand(Result.DENY);
                        return;
                    }

                    if (ItemTags.STAFFS.contains(hm.asItemType())) {
                        p.getWorld().playSound(p, Sound.ENTITY_SHULKER_SHOOT, 1f, 1.4f);
                        final Snowball prj = p.launchProjectile(Snowball.class,
                            p.getEyeLocation().getDirection().multiply(getHoeFactor(hm)), pr -> {
                                pr.setItem(new ItemBuilder(switch (hm) {
                                    case STONE_HOE -> ItemType.CLAY_BALL;
                                    case IRON_HOE -> ItemType.IRON_NUGGET;
                                    case GOLDEN_HOE -> ItemType.GOLD_NUGGET;
                                    case DIAMOND_HOE -> ItemType.ENDER_EYE;
                                    case NETHERITE_HOE -> ItemType.NETHER_WART;
                                    default -> ItemType.EGG;
                                }).build());
                                pr.setGravity(false);
                            });
                        p.setCooldown(hand.getType(), 8);
                        p.damageItemStack(e.getHand(), 1);
                        ShotLst.damage(prj, Stat.ranged(STAFF_DMG * switch (hm) {
                            case STONE_HOE -> 1.2d;
                            case IRON_HOE -> 1.5d;
                            case GOLDEN_HOE -> 2.0d;
                            case DIAMOND_HOE -> 2.5d;
                            case NETHERITE_HOE -> 4.0d;
                            default -> 1.0d;
                        }, sv.getStat(Stat.ACCURACY)));
                    }

                    claim(hand, p, sv, e.getHand());
                }
                break;
            case RIGHT_CLICK_BLOCK:
                if (hand != null) {
                    final Material hm = hand.getType();
                    if (p.hasCooldown(hm)) {
                        e.setUseItemInHand(Result.DENY);
                        return;
                    }

                    b = e.getClickedBlock();
                    if (ItemTags.STAFFS.contains(hm.asItemType())) {
                        if (b.getBlockData() instanceof Ageable) {
                            final Ageable ag = (Ageable) b.getBlockData().clone();
                            if (ag.getAge() == ag.getMaximumAge()) {
                                e.setUseInteractedBlock(Result.DENY);
                                b.breakNaturally(hand);
                                ag.setAge(0);
                                b.setBlockData(ag, false);
                                Nms.swing(p, e.getHand());
                            }
                            break;
                        }
                    }

                    if (hm == Material.GLASS_BOTTLE) {
                        switch (b.getType()) {
                            case CAULDRON, WATER_CAULDRON, BEE_NEST, BEEHIVE:
                                break;
                            case CAVE_VINES_PLANT:
                                final CaveVinesPlant cpd = (CaveVinesPlant) b.getBlockData();
                                if (cpd.isBerries()) {
                                    cpd.setBerries(false);
                                    b.setBlockData(cpd, false);
                                    hand.setAmount(hand.getAmount() - 1);
                                    ItemUtil.giveItemsTo(p, new ItemBuilder(Material.POTION).basePotion(clr).build());
                                    p.getWorld().playSound(p.getEyeLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1f, 1.2f);
                                }
                                e.setCancelled(true);
                                break;
                            default:
                                e.setUseItemInHand(Result.DENY);
                                e.setUseInteractedBlock(Result.ALLOW);
                                break;
                        }
                        break;
                    }

                    claim(hand, p, sv, e.getHand());
                }

                if (p.isSneaking()) sv.trigger(Trigger.SHIFT_RIGHT, e, p);
                break;
        }
    }

    private void claim(final ItemStack hand, final Player p, final Survivor sv, final EquipmentSlot slot) {
        final PersistentDataContainerView pdc = hand.getPersistentDataContainer();
        final Integer lvl = pdc.get(OStrap.key(Scroll.LVL), PersistentDataType.INTEGER);
        if (lvl == null) return;
        final Selector sl = Selector.VALUES.get(pdc.get(OStrap.key(Selector.data), PersistentDataType.STRING));
        if (sl != null) {
            sv.change(new Selector.SelState(sl, lvl), 1);
            hand.setAmount(hand.getAmount() - 1);
            p.getInventory().setItem(slot, hand);
            return;
        }
        final Ability ab = Ability.VALUES.get(pdc.get(OStrap.key(Ability.data), PersistentDataType.STRING));
        if (ab != null) {
            sv.change(new Ability.AbilState(ab, lvl), 1);
            hand.setAmount(hand.getAmount() - 1);
            p.getInventory().setItem(slot, hand);
            return;
        }
        final Modifier md = Modifier.VALUES.get(pdc.get(OStrap.key(Modifier.data), PersistentDataType.STRING));
        if (md != null) {
            sv.change(new Modifier.ModState(md, lvl), 1);
            hand.setAmount(hand.getAmount() - 1);
            p.getInventory().setItem(slot, hand);
        }
    }

    public static float getHoeFactor(final Material hoe) {
        return switch (hoe) {
            case WOODEN_HOE -> 1f;
            case STONE_HOE -> 1.15f;
            case IRON_HOE -> 1.2f;
            case GOLDEN_HOE -> 1.25f;
            case DIAMOND_HOE -> 1.4f;
            case NETHERITE_HOE -> 1.5f;
            default -> 0f;
        };
    }

    @EventHandler
    public void onEat(final PlayerItemConsumeEvent e) {
        final Player p = e.getPlayer();
        final ItemStack it = e.getItem();
        if (it.getType() == Material.POTION) {
            final PotionMeta pm = (PotionMeta) it.getItemMeta();
            if (pm.getBasePotionType() == clr) {
                p.removePotionEffect(PotionEffectType.WEAKNESS);
                p.removePotionEffect(PotionEffectType.POISON);
            }
        }
    }

    /*@EventHandler
    public void onPortal(final PortalCreateEvent e) {
    	if (e.getEntity() != null && e.getEntity().getType() == EntityType.PLAYER) {
    		final Player p = (Player) e.getEntity();
    		final ItemStack is = p.getInventory().getItemInMainHand();
            if (is.getType() == Material.FLINT_AND_STEEL && is.hasItemMeta()) {
        		final Survivor sv = PM.getOplayer(p, Survivor.class);
                QM.tryCompleteQuest(p, Quest.LightPortal, 1, true);
                switch (SkillMats.getCstmItm(is.getItemMeta())) {
                    case SPARK:
                        if (!sv.isWorldOpen(SubServer.KRIOLITE)) {
                            sv.unlockWorld(SubServer.KRIOLITE);
                            p.sendMessage(Main.prefix + "Вы открыли мир " + SubServer.KRIOLITE.displayName);
                            p.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                            p.getInventory().setItemInMainHand(Main.air);
                        }
                        break;
                    case UNDEAD:
                        if (!sv.isWorldOpen(SubServer.LOCUS)) {
                            sv.unlockWorld(SubServer.LOCUS);
                            p.sendMessage(Main.prefix + "Вы открыли мир " + SubServer.LOCUS.displayName);
                            p.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                            p.getInventory().setItemInMainHand(Main.air);
                        }
                        break;
                    case ACID:
                        if (!sv.isWorldOpen(SubServer.INFERNAL)) {
                            sv.unlockWorld(SubServer.INFERNAL);
                            p.sendMessage(Main.prefix + "Вы открыли мир " + SubServer.INFERNAL.displayName);
                            p.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                            p.getInventory().setItemInMainHand(Main.air);
                        }
                        break;
					default:
						break;
                }
            }
    	}
    }*/

    /*@EventHandler
    public void onFrameBreak(final HangingBreakEvent e) {
    	if (e.getEntity() instanceof ItemFrame) {
			new EntityDamageEvent(e.getEntity(), DamageCause.CONTACT, 1d).callEvent();
    	}
    }*/

    @EventHandler
    public void onTeleport(final PlayerTeleportEvent e) {
        if (e.getCause() == TeleportCause.SPECTATE) {
            e.setCancelled(!ApiOstrov.isLocalBuilder(e.getPlayer()));
        }
    }

    /*@EventHandler
    public void onEnt(final PlayerInteractAtEntityEvent e) {
    	if (e.getRightClicked() instanceof Mob) {
            final Mob mb = (Mob) e.getRightClicked();
            
            if (e.getRightClicked().getType() == Main.subServer.taskNPC) {
                if (!mb.hasAI()) {
                    final Survivor sv = SM.getSurvivor(e.getPlayer());
                    final Location loc = mb.getLocation();
                    final TaskType ttp = Task.clickable.remove(mb.getEntityId());
                    if (ttp == null) {
                        return;
                    }
                    
                    if (sv.miniQuestTask!=null) {
                        e.getPlayer().sendMessage(Main.prefix + "§cВы уже выполняете задание!");
                    }
                    switch (ttp) {
                        case DEFEND:
                            sv.miniQuestTask = new DefendTask(sv, mb, e.getPlayer(), LocUtil.encdLoc2D(loc, Task.factor, Task.shift));
                            break;
                        case KILL:
                            sv.miniQuestTask = new KillTask(sv, mb, e.getPlayer(), LocUtil.encdLoc2D(loc, Task.factor, Task.shift));
                            break;
                        case MINE:
                            sv.miniQuestTask = new MineTask(sv, mb, e.getPlayer(), LocUtil.encdLoc2D(loc, Task.factor, Task.shift));
                            break;
                        case MOVE:
                            sv.miniQuestTask = new MoveTask(sv, mb, e.getPlayer(), LocUtil.encdLoc2D(loc, Task.factor, Task.shift));
                            break;
                    }
                }
            }

            if (mb.getType() == EntityType.SLIME) {
                final Slime cb = (Slime) e.getRightClicked();
                final CuBlock cbl = SM.cublocks.get(cb.getEntityId());
                final Player p = e.getPlayer();
                if (cbl != null && !Timer.has(p, "cublock")) {
                    Timer.add(p, "cublock", 2);
                    cbl.cInv.open(p);
                    *//*if (cbl.cInv.getManager().getOpenedPlayers(cbl.cInv).isEmpty()) {
                        cbl.cInv.open(p);
                    } else {
                        p.sendMessage(Main.prefix + "Кто-то уже использует этот стол!");
                    }*//*
                }
            }
    	}
    }*/
}