package ru.romindous.skills.listeners;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.skills.Main;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.events.PlayerKillEntityEvent;
import ru.romindous.skills.guides.Entries;
import ru.romindous.skills.mobs.Minion;
import ru.romindous.skills.mobs.SednaMob;
import ru.romindous.skills.skills.Rarity;
import ru.romindous.skills.skills.Scroll;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.skills.trigs.Trigger;
import ru.romindous.skills.survs.Stat;
import ru.romindous.skills.survs.Survivor;


public class DeathLst implements Listener {
    //    private static final float DROP_MUL = 0.8f;
//    private static final float SRL_CH_DEL = 10f;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDth(final PlayerDeathEvent e) {
        final Player p = e.getPlayer();
        final Location loc = p.getLocation();
        p.setLastDeathLocation(loc);
        p.sendMessage(TCUtil.form(Main.prefix + "Вы умерли в мире " + Main.subServer.disName
            + "§7,\n§7на координатах (§4" + loc.getBlockX() + "§7, §4" + loc.getBlockY() + "§7, §4" + loc.getBlockZ() + "§7)"));

        final Survivor sv = PM.getOplayer(p, Survivor.class);
        if (sv == null) return;

        if (sv.role != null) {
            p.getWorld().playSound(p.getLocation(), switch (sv.role) {
            case ARCHER -> Sound.ENTITY_SKELETON_DEATH;
            case ASSASIN -> Sound.ENTITY_GUARDIAN_DEATH;
//            case VAMPIRE -> Sound.ENTITY_ENDERMAN_DEATH;
            case STONER -> Sound.ENTITY_ELDER_GUARDIAN_DEATH;
            case MAGE -> Sound.ENTITY_WITHER_HURT;
            case PHANTOM -> Sound.ENTITY_SHULKER_DEATH;
            case WARRIOR -> Sound.BLOCK_ANVIL_DESTROY;
//            case NECROS -> Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED;
            }, 20.0F, 0.8F);
            
//            if (sv.vampireBatTime>0) SM.unmakeBat(p, sv);
        }
        p.setGameMode(GameMode.SURVIVAL);

        final Mob mb = (Mob) p.getWorld().spawnEntity(loc, Main.subServer.mobType, false);
        mb.setCanPickupItems(true);
        mb.customName(TCUtil.form(TCUtil.N + "Труп игрока " + TCUtil.P + p.getName()));
        mb.setCustomNameVisible(false);
        mb.setTicksLived(2);
        mb.setRemoveWhenFarAway(false);

        sv.trigger(Trigger.USER_DEATH, e, p);
    }

    private static final Set<DamageType> TICKED = Set.of(DamageType.ON_FIRE,
        DamageType.FREEZE, DamageType.WITHER, DamageType.INDIRECT_MAGIC);
    public static void onCustomDeath(final EntityDeathEvent e, final SednaMob sm) {
        if (!(e.getEntity() instanceof final Mob mob)) return;
        final Player killer;
        final EntityDamageEvent lde = mob.getLastDamageCause();
        if (EntityUtil.lastDamager(mob, true) instanceof final LivingEntity klr) {
            if (klr instanceof Player) killer = (Player) klr;
            else if (Minion.ownerOf(klr) instanceof final Player kp) killer = kp;
            else killer = null;
        } else if (TICKED.contains(lde.getDamageSource().getDamageType())) {
            final Player kpl = mob.getKiller();
            final LivingEntity tgt = mob.getTarget();
            killer = kpl != null && tgt instanceof final Player tpl
                && tpl.getEntityId() == kpl.getEntityId() ? tpl : null;
        } else killer = null;

        if (killer == null) return;
        //если игрок-убийца не определён, дальше не пойдёт
        final Survivor kSv = PM.getOplayer(killer, Survivor.class);

        final double mhp = mob.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        final int exp = (int) Stat.exp(mhp, kSv.getStat(Stat.ACCURACY));

        float dropNum = (float) Stat.drops(1d, kSv.getStat(Stat.PASSIVE));
        final ItemStack hnd = killer.getInventory().getItemInMainHand();
        if (!ItemUtil.isBlank(hnd, true)) {
            dropNum *= 1f + (hnd.getEnchantmentLevel(Enchantment.LOOTING) * 0.8f);
        }

        final float mana = (float) Stat.remana(sm.mana, kSv.getStat(Stat.SPIRIT));
        final PlayerKillEntityEvent el = new PlayerKillEntityEvent(killer, mob, mana, dropNum, exp);
        if (!el.callEvent()) return;
        Entries.mob.complete(killer, kSv, false);
        kSv.trigger(Trigger.KILL_ENTITY, el, killer);
        e.setDroppedExp(el.getExp());
        kSv.chgMana(killer, el.getMana());

        final Location loc = EntityUtil.center(mob);
//        final float thresh = SRL_CH_DEL / Math.max(exp, 1);
        for (int d = Ostrov.random.nextInt((int) el.getDrops()) + 1; d != 0; d--) {
            for (final ItemStack it : sm.loot().genRolls(ItemStack.class)) {
                loc.getWorld().dropItem(loc, it);
            }

//            if (Main.srnd.nextFloat() > thresh)
            if (kSv.role == null) continue;
            dropScroll(loc, killer, kSv);
        }
    }

    private static final int SC_ROLL = ConfigVars.get("drops.scroll", 1000);
    private static void dropScroll(final Location loc, final Player pl, final Survivor sv) {
        final Scroll sc;
        switch (Main.srnd.nextInt(SC_ROLL)) {
            case 0 -> sc = randScroll(sv, Selector.RARITIES);//selector
            case 1, 2 -> {
                sc = randScroll(sv, Ability.RARITIES);//ability
                Entries.abil.complete(pl, sv, false);
            }
            case 3, 4, 5 -> sc = randScroll(sv, Modifier.RARITIES);//modifier
            default -> {return;}
        }
        if (sc == null) return;
        final TextColor tc = TCUtil.getTextColor(sc.rarity().color());
        Nms.colorGlow(loc.getWorld().dropItem(loc, sc.drop(0)),
            NamedTextColor.nearestTo(tc), false);
    }

    private static final int RAR_LEN = Math.min(Rarity.values().length,
        Main.subServer.ordinal() + 2);//2 for wastes
    private static final int RAR_DEL = 1;
    private static final Rarity[] RAR_VALS = getMaxRar();

    private static Rarity[] getMaxRar() {
        final List<Rarity> rrs = new ArrayList<>();
        final Rarity[] vals = Rarity.values();
        for (int i = 0; i != RAR_LEN; i++)
            for (int j = i << RAR_DEL; j >= 0; j--)
                rrs.add(vals[RAR_LEN - i - 1]);
        return rrs.toArray(new Rarity[0]);
    }

    @Nullable
    private static <S extends Scroll> S randScroll(final Survivor sv, final IntHashMap<List<S>> values) {
        final List<S> def = List.of();
        final List<S> rsc = new ArrayList<>(values.getOrDefault(Scroll.total(ClassUtil.rndElmt(RAR_VALS), null), def));
        rsc.addAll(values.getOrDefault(Scroll.total(ClassUtil.rndElmt(RAR_VALS), sv.role), def));
        return rsc.get(Main.srnd.nextInt(rsc.size()));
    }

    /*@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDth(final EntityDeathEvent e) {
        if (e.getEntity() instanceof Mob) {
            final Mob mob = (Mob) e.getEntity();
            final Location deathLoc = mob.getLocation();
            
            *//*if (mob.getPersistentDataContainer().has(EntUtil.ownerPlName)) {
            	final Survivor os = SM.getSurvivor(mob.getPersistentDataContainer().val(EntUtil.ownerPlName, PersistentDataType.STRING));
            	if (os != null) {
            		os.minis.remove(mob.getEntityId());
            	}
                e.getDrops().clear();
                e.setDroppedExp(0);
                mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_CREEPER_HURT, 1f, 0.5f);
                mob.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, mob.getLocation(), 20, 0.4d, 0.6d, 0.4d, 0, null, false);
                return;
            } else if (mob.getPersistentDataContainer().has(EntUtil.ownerEntId)) {//чей-то миньон убит  if (SM.owners.remove(mob.getEntityId()) != null) {
                e.getDrops().clear();
                e.setDroppedExp(0);
                mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_CREEPER_HURT, 1f, 0.5f);
                mob.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, mob.getLocation(), 20, 0.4d, 0.6d, 0.4d, 0, null, false);
                return;
            }*//*

            Task.clickable.remove(mob.getEntityId());

            final Player killer;
            final EntityDamageEvent ee = mob.getLastDamageCause();
            if (ee instanceof final EntityDamageByEntityEvent event) {
                if (event.getDamager() instanceof Projectile) {
                    final ProjectileSource ps = ((Projectile) event.getDamager()).getShooter();
                    if (ps instanceof Player) {
                        killer = (Player) ps;
                    } else {
                        e.setDroppedExp(0);
                        return;
                    }
                } else {
                    switch (event.getDamager().getType()) {
                        case PRIMED_TNT:
                            if ((((TNTPrimed) event.getDamager()).getSource() instanceof final Player he)) {
                                killer = he;
                                break;
                            }
                            e.setDroppedExp(0);
                            return;
                        case PLAYER:
                            killer = (Player) event.getDamager();
                            break;
                        default:
                            if (event.getDamager() instanceof final Mob dmgr) {
                                final String plNm = dmgr.getPersistentDataContainer().val(EntUtil.ownerPlName, PersistentDataType.STRING);
                                if (plNm != null) { //чей-то миньон атакует
                                    killer = Bukkit.getPlayerExact(plNm);
                                    break;
                                }
                            }
                            e.setDroppedExp(0);
                            return;
                    }
                }
            } else {
                switch (ee.getCause()) {
                    case FIRE, FIRE_TICK, LAVA, POISON, WITHER:
                        final LivingEntity target = mob.getTarget();
                        if (target != null && target.getType() == EntityType.PLAYER && !Bots.npcs.containsKey(target.getEntityId())) {
                            killer = (Player) target;
                        } else return;
                        break;
                    default:
                        return;
                }
            }
            
            //если игрок-убийца не определён, дальше не пойдёт
            final Survivor kSv = SM.getSurvivor(killer);

            final Boss bss = Bosses.bosses.val(mob.getUniqueId());
            if (bss != null) {
            	bss.dthBss(killer);
            }
            
            if (Bots.npcs.containsKey(mob.getEntityId())) {
                e.getDrops().clear();
            } else {
                switch (mob.getType()) {
                    case BEE:
                        if (((Ageable) mob).isAdult() && Main.srnd.nextInt(4) == 0) {
                            final Bee b1 = (Bee) deathLoc.getWorld().spawnEntity(deathLoc, EntityType.BEE, false);
                            b1.setBaby();
                            b1.setAnger(1000);
                            b1.setTarget(mob.getTarget());
                            final Bee b2 = (Bee) deathLoc.getWorld().spawnEntity(deathLoc, EntityType.BEE, false);
                            b2.setBaby();
                            b2.setAnger(1000);
                            b2.setTarget(mob.getTarget());
                        }
                        break;
                    case CREEPER:
                        if (Main.srnd.nextBoolean()) {
                            ((TNTPrimed) deathLoc.getWorld().spawnEntity(deathLoc, EntityType.PRIMED_TNT, CreatureSpawnEvent.SpawnReason.CUSTOM)).setFuseTicks(50);
                        }
                    	break;
                    case ZOGLIN:
                        if (Main.subServer == SubServer.WASTES && kSv.level > 10f && Main.srnd.nextFloat() < Math.min((kSv.level - 10f) * 0.01f, 0.04f)) {
                            Main.bossMgr.spawn(killer, deathLoc, BossType.RANGER_ROT, null);
                            QM.tryCompleteQuest(killer, Quest.SummonRG, 1, true);
                        }
                        break;
                    case STRAY:
                        if (Main.subServer == SubServer.KRIOLITE && kSv.level > 30f && Main.srnd.nextFloat() < Math.min((kSv.level - 30f) * 0.01f, 0.04f)) {
                            Main.bossMgr.spawn(killer, deathLoc, BossType.MOUNTAIN, null);
                            QM.tryCompleteQuest(killer, Quest.SummonLM, 1, true);
                        }
                        break;
                    case PILLAGER:
                        if (Main.subServer == SubServer.LOCUS && kSv.level > 60f && Main.srnd.nextFloat() < Math.min((kSv.level - 60f) * 0.01f, 0.04f)) {
                            Main.bossMgr.spawn(killer, deathLoc, BossType.HORESAR, null);
                            QM.tryCompleteQuest(killer, Quest.SummonHS, 1, true);
                        }
                        break;
                    case WITHER_SKELETON:
                        if (Main.subServer == SubServer.INFERNAL && kSv.level > 100f && Main.srnd.nextFloat() < Math.min((kSv.level - 100f) * 0.01f, 0.04f)) {
                            Main.bossMgr.spawn(killer, deathLoc, BossType.PIGLIN_CHIEF, null);
                            QM.tryCompleteQuest(killer, Quest.SummonPC, 1, true);
                        }
                        break;
                    case WARDEN:
                        if (Main.subServer == SubServer.AQUAROD && kSv.level > 150f && Main.srnd.nextFloat() < Math.min((kSv.level - 150f) * 0.02f, 0.1f)) {
                            Main.bossMgr.spawn(killer, deathLoc, BossType.ELDEST_GUARDIAN, null);
                            QM.tryCompleteQuest(killer, Quest.SummonEG, 1, true);
                        }
                        break;
                    case PHANTOM:
                        if (Main.subServer == SubServer.KALEUM && kSv.level > 210f && Main.srnd.nextFloat() < Math.min((kSv.level - 210f) * 0.01f, 0.04f)) {
                            Main.bossMgr.spawn(killer, deathLoc, BossType.LEVIATHAN, null);
                            QM.tryCompleteQuest(killer, Quest.SummonLF, 1, true);
                        }
                        break;
                    default:
                        break;
                }
            }
            //создает буффет кол-ва опыта полученом в конкретном чанке, т.е.
            //если постоянно убивать мобов в одном и том же месте (ака фармилка / моб-дробилка)
            //то опыта бедут все меньше и меньше даватся
            
            //Шанс возобновить 20% маны", "§7при убийстве моба
            if (kSv.mana < kSv.manaMax && kSv.getAbilityChance((Ability.ИСТОЩЕНИЕ)) > 75) {//sv.isOn(Ability.ИСТОЩЕНИЕ) && Main.rnd.nextFloat() < sv.getAbFctr(Ability.ИСТОЩЕНИЕ, sv.getAbilityLvl(Ability.ИСТОЩЕНИЕ))) {
                kSv.addMana(killer, kSv.manaMax / 5);
            }

            //Вы можете копить §4кровь §7с лимитом,", "§7ударяя врагов, которая убывает", "§7на §41 §7каждую секунду, но вы горите", "§7находясь на солнце, если ее §40§7, наполнение"
            if (kSv.isReady(killer, Ability.ПРОКЛЯТИЕ)) {
                kSv.blood += (int) (kSv.getStatEffect(Ability.ПРОКЛЯТИЕ));
                if (kSv.blood > kSv.bloodMax) {
                    kSv.blood = kSv.bloodMax;
                    kSv.applySkill(killer);
                } else {
                    killer.playSound(deathLoc, Sound.BLOCK_SCULK_SENSOR_CLICKING, 0.8f, 0.6f);
                }
            }

            //Шифт + ПКМ мечом §7для активации", "§8-=-=-=-=-=-=-=-=-=-=-=-=-", "§7Зацепите всех сущностей рядом", "§7одним ударом, накладывая эффект", "§7свечения и замедления §dлвл§7,", 
            //"§7область и длительность эффектов", "§7зависят от §dлвл §7и §eЛОВКОСТИ", " ", "3 лвл §7- сущности получают доп. урон
            if (kSv.getAbilityChance((Ability.АДСОРБЦИЯ)) > 85) {//sv.isOn(Ability.АДСОРБЦИЯ) && Main.rnd.nextFloat() < sv.getAbFctr(Ability.АДСОРБЦИЯ, sv.getAbilityLvl(Ability.АДСОРБЦИЯ))) {
                if (!killer.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                    killer.playSound(deathLoc, Sound.ITEM_AXE_WAX_OFF, 1f, 2f);
                    killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0));
                }
            }
			
            QM.tryCompleteQuest(killer, Quest.KillMob, 1, true);

            if (kSv.miniQuestTask != null && kSv.miniQuestTask instanceof KillTask) {
                ((KillTask) kSv.miniQuestTask).checkKill(killer, mob.getType());
            }

            //создает буффет кол-ва опыта полученом в конкретном чанке, т.е.
            //если постоянно убивать мобов в одном и том же месте (ака фармилка / моб-дробилка)
            //то опыта бедут все меньше и меньше даватся
            //bfr - сам буфер
            final Location killerLoc = killer.getLocation();
            final ChunkContent cc = Land.getChunkContent(killerLoc, true);
            final int time = Timer.getTime(); //сколько секунд прошло с прошлого убийства
            if (time - cc.lastMobKillStamp < 32) {
                if ((cc.multiplier *= 0.8f) < 0.1f) {
                    e.setDroppedExp(0);
                    if (mob.customName() == null) {
                        e.getDrops().clear();
                    }
                    return;
                }
            } else {
                cc.multiplier = 1f;
            }
            cc.lastMobKillStamp = Timer.getTime(); //обновить штамп
            
            if (kSv.isReady(killer, Ability.ВЫЦВЕТКА)) {
            	final Spored sp = spores.val(mob.getEntityId());
            	final int sps = sp == null ? 1 : sp.stack, lvl = kSv.getAbilityLvl(Ability.ВЫЦВЕТКА);
            	final float fct = kSv.getStatEffect(Ability.ВЫЦВЕТКА, lvl) - 1f;
            	deathLoc.getWorld().playSound(deathLoc, Sound.BLOCK_BIG_DRIPLEAF_HIT, 0.01f * sps + 1f, 0.6f);
            	deathLoc.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, deathLoc, sps << 1, 0.4d, 0.8d, 0.4d, 0d, null, false);
                for (final LivingEntity le : LocUtil.getChEnts(deathLoc, lvl, LivingEntity.class)) {
            		if (le.getEntityId() != killer.getEntityId()) {
                    	//Bukkit.getConsoleSender().sendMessage(spores.toString());
                    	final Spored osp = spores.val(le.getEntityId());
                    	if (osp == null) {
                    		spores.put(le.getEntityId(), new Spored(le, fct, sps, 40));
                    		continue;
                    	}
                    	osp.stack = Math.min(osp.stack + sps, 10);
            		}
            	}
            }
            
            if (kSv.getAbilityChance(Ability.ПОРОЖДЕНИЕ) > 60 && mob.getAttribute(Attribute.MAX_HEALTH).getBaseValue() > 10d) {
            	for (int i = Main.srnd.nextInt(kSv.getAbilityLvl(Ability.ПОРОЖДЕНИЕ)) + 1; i > 0; i--) {
            		kSv.setUseStamp(Ability.ПОРОЖДЕНИЕ);
                    final Mob mb = (Mob) deathLoc.getWorld().spawnEntity(deathLoc, Main.subServer.mobType, false);//, false);
                    mb.setCanPickupItems(false);
                    mb.setTicksLived(2);
                    EntUtil.addMinion(mb, "§3Пустой Сосуд", killer, 20);
            	}
            }
            
            if (fbl) {
    			final int lvl = kSv.getAbilityLvl(Ability.МЕТЕОШТОРМ);
    			if (lvl > 2) {
    				final MagmaCube mc = mob.getWorld().spawn(mob.getLocation(), MagmaCube.class, SpawnReason.CUSTOM);
    				mc.setSize(lvl - 2);
    				BotListener.setMobChars(mc, kSv);
    				EntUtil.addMinion(mc, "§6Клякса", killer, kSv.getStat(Ability.МЕТЕОШТОРМ.affectStat) >> 1);
    			}
            }

            final ItemStack hnd = killer.getInventory().getItemInMainHand();
            if (!ItemUtil.isBlankItem(hnd, true)) {
                switch (CustomMats.getCstmItm(hnd.getItemMeta())) {
        		case RESTILE:
                case AZULITE:
                    if (Main.srnd.nextInt(4) == 0) {
                        hpLeft *= 2;
                    }
                    break;
                case SIPHOR:
                	killer.setRemainingAir(Math.min(killer.getMaximumAir(), killer.getRemainingAir() + mobHpBase));
                	kSv.addHp(killer, mobHpBase >> 4);
                    break;
				default:
					break;
                }
            }
            
        }
    }*/
}
