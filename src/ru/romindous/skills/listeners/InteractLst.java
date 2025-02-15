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
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Main;
import ru.romindous.skills.items.Groups;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.items.ItemTags;
import ru.romindous.skills.skills.Scroll;
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
                    } /*else if (ItemUtil.is(hand, ItemType.WOODEN_PICKAXE)) {
                        p.sendMessage(Registry.BLOCK.getTag(BlockTypeTagKeys.LOGS).resolve(Registry.BLOCK).getClass().getSimpleName());
                        p.sendMessage(Registry.BLOCK.getTag(BlockTypeTagKeys.LOGS).getClass().getName());
                        final long tm = System.currentTimeMillis();
                        final WXYZ lc = new WXYZ(e.getClickedBlock());
                        final int dst = 10;
                        final World w = lc.w();
                        final Set<BlockPosition> toChop = new HashSet<>();
                        final Set<BlockPosition> checked = new HashSet<>();
                        for (int x = -dst + 1; x != dst; x++) {
                            for (int z = -dst + 1; z != dst; z++) {
                                final WXYZ start = lc.clone().add(x, 0, z);
                                if (checked.contains(start)) continue;
                                if (!LOGS.contains(Nms.fastType(start))) continue;
                                final BlockPosition stp = Position.block(start.x, start.y, start.z);
                                checked.add(stp); toChop.add(stp);
                                checkTree(stp, toChop, checked, w);
                            }
                        }
                        p.sendMessage("tm1-" + (System.currentTimeMillis() - tm));
                        for (final BlockPosition chop : toChop) {
                            w.getBlockAt(chop.blockX(), chop.blockY(), chop.blockZ()).setBlockData(BlockUtil.air, true);
                        }
                        p.sendMessage("tm2-" + (System.currentTimeMillis() - tm));
                    }*/
                }
                if (p.isSneaking()) sv.trigger(Trigger.SHIFT_LEFT, e, p);
                break;
            case RIGHT_CLICK_AIR:
                if (p.isSneaking()) sv.trigger(Trigger.SHIFT_RIGHT, e, p);
                if (hand != null) {
                    if (p.hasCooldown(hand)) {
                        e.setUseItemInHand(Result.DENY);
                        return;
                    }

                    final Groups.StaffType st = Groups.staff(hand);
                    if (st != null) {
                        p.getWorld().playSound(p, Sound.ENTITY_SHULKER_SHOOT, 1f, 1.4f);
                        final Snowball prj = p.launchProjectile(Snowball.class,
                            p.getEyeLocation().getDirection().multiply(st.spd), pr -> {
                                pr.setItem(new ItemBuilder(st.shell).build());
                                pr.setGravity(false);
                            });
                        p.setCooldown(hand.getType(), 8);
                        p.damageItemStack(e.getHand(), 1);
                        ShotLst.damage(prj, Stat.ranged(STAFF_DMG * st.dmg, sv.getStat(Stat.ACCURACY)));
                        break;
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

                    if (ItemType.GLASS_BOTTLE.equals(hm.asItemType())) {
                        switch (b.getType()) {
                            case CAULDRON, WATER_CAULDRON, BEE_NEST, BEEHIVE:
                                break;
                            case CAVE_VINES_PLANT:
                                final CaveVinesPlant cpd = (CaveVinesPlant) b.getBlockData();
                                if (cpd.isBerries()) {
                                    cpd.setBerries(false);
                                    b.setBlockData(cpd, false);
                                    hand.setAmount(hand.getAmount() - 1);
                                    ItemUtil.giveItemsTo(p, new ItemBuilder(ItemType.POTION).basePotion(clr).build());
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

    /*private static final Set<BlockType> LOGS = new HashSet<>(Registry.BLOCK.getTag(BlockTypeTagKeys.LOGS).resolve(Registry.BLOCK));
    private void checkTree(final BlockPosition start, final Set<BlockPosition> toChop, final Set<BlockPosition> checked, final World w) {
        final BlockFace[] topped = {BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST,
            BlockFace.SELF, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
        final BlockFace[] sided = {BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
        final Set<BlockPosition> current = new HashSet<>();
        final Set<BlockPosition> next = new HashSet<>();
        next.add(start);
        do {
            current.clear();
            current.addAll(next);
            next.clear();
            for (final BlockPosition prnt : current) {
                for (final BlockFace bf : sided) {
                    final BlockPosition nps = Position.block(prnt.blockX() + bf.getModX(),
                        prnt.blockY(), prnt.blockZ() + bf.getModZ());
                    if (!checked.add(nps)) continue;
                    if (!LOGS.contains(Nms.fastType(w, nps.blockX(), nps.blockY(), nps.blockZ()))) continue;
                    toChop.add(nps);
                    next.add(nps);
                }
                for (final BlockFace bf : topped) {
                    final BlockPosition nps = Position.block(prnt.blockX() + bf.getModX(),
                        prnt.blockY() + 1, prnt.blockZ() + bf.getModZ());
                    if (!checked.add(nps)) continue;
                    if (!LOGS.contains(Nms.fastType(w, nps.blockX(), nps.blockY(), nps.blockZ()))) continue;
                    toChop.add(nps);
                    next.add(nps);
                }
            }
        } while (!next.isEmpty());
    }*/

    private void claim(final ItemStack hand, final Player p, final Survivor sv, final EquipmentSlot slot) {
        final PersistentDataContainerView pdc = hand.getPersistentDataContainer();
        final Integer lvl = pdc.get(OStrap.key(Scroll.LVL), PersistentDataType.INTEGER);
        if (lvl == null) return;
        final Selector sl = Selector.VALUES.get(pdc.get(OStrap.key(Selector.data), PersistentDataType.STRING));
        if (sl != null) {
            final Role rl = sl.role();
            if (rl != null && rl != sv.role) {
                p.sendMessage(TCUtil.form(Main.prefix + sl.rarity().color() + sl.name()
                    + " <red>можно присвоить только роли " + rl.disName()));
                return;
            }
            sv.giveScroll(p, sl, lvl);
            hand.setAmount(hand.getAmount() - 1);
            p.getInventory().setItem(slot, hand);
            return;
        }
        final Ability ab = Ability.VALUES.get(pdc.get(OStrap.key(Ability.data), PersistentDataType.STRING));
        if (ab != null) {
            final Role rl = ab.role();
            if (rl != null && rl != sv.role) {
                p.sendMessage(TCUtil.form(Main.prefix + ab.rarity().color() + ab.name()
                    + " <red>можно присвоить только роли " + rl.disName()));
                return;
            }
            sv.giveScroll(p, ab, lvl);
            hand.setAmount(hand.getAmount() - 1);
            p.getInventory().setItem(slot, hand);
            return;
        }
        final Modifier md = Modifier.VALUES.get(pdc.get(OStrap.key(Modifier.data), PersistentDataType.STRING));
        if (md != null) {
            final Role rl = md.role();
            if (rl != null && rl != sv.role) {
                p.sendMessage(TCUtil.form(Main.prefix + md.rarity().color() + md.name()
                    + " <red>можно присвоить только роли " + rl.disName()));
                return;
            }
            sv.giveScroll(p, md, lvl);
            hand.setAmount(hand.getAmount() - 1);
            p.getInventory().setItem(slot, hand);
        }
    }

    @EventHandler
    public void onEat(final PlayerItemConsumeEvent e) {
        final Player p = e.getPlayer();
        final ItemStack it = e.getItem();
        if (ItemUtil.is(it, ItemType.POTION)) {
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
            if (is.getType() == ItemType.FLINT_AND_STEEL && is.hasItemMeta()) {
        		final Survivor sv = PM.getOplayer(p, Survivor.class);
                QM.tryCompleteQuest(p, Quest.LightPortal, 1, true);
                switch (SkillMats.getCstmItm(is.getItemMeta())) {
                    case SPARK:
                        if (!sv.isWorldOpen(SubServer.KRIOLITE)) {
                            sv.unlockWorld(SubServer.KRIOLITE);
                            p.sendMessage(Main.prefix + "Вы открыли мир " + SubServer.KRIOLITE.disName);
                            p.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                            p.getInventory().setItemInMainHand(Main.air);
                        }
                        break;
                    case UNDEAD:
                        if (!sv.isWorldOpen(SubServer.LOCUS)) {
                            sv.unlockWorld(SubServer.LOCUS);
                            p.sendMessage(Main.prefix + "Вы открыли мир " + SubServer.LOCUS.disName);
                            p.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                            p.getInventory().setItemInMainHand(Main.air);
                        }
                        break;
                    case ACID:
                        if (!sv.isWorldOpen(SubServer.INFERNAL)) {
                            sv.unlockWorld(SubServer.INFERNAL);
                            p.sendMessage(Main.prefix + "Вы открыли мир " + SubServer.INFERNAL.disName);
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

    /*@EventHandler (priority = EventPriority.LOW, ignoreCancelled = false)
    public void onPick(final EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof final Player pl)) return;
        final Survivor sv = PM.getOplayer(pl, Survivor.class);
        final ItemStack it = e.getItem().getItemStack();
        final ItemType tp = it.getType().asItemType();
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