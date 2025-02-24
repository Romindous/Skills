package ru.romindous.skills;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.listeners.ShotLst;
import ru.romindous.skills.skills.Skill;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.survs.SM;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.utils.EffectUtil;


public class MainTask implements Runnable {
	public static int tick;
	public static int proccesTime;
	public static final PotionEffect BLIND = new PotionEffect(PotionEffectType.BLINDNESS, 48, 1);
    private final World w = Bukkit.getWorlds().getFirst();
	private static final List<LivingEntity> noDmg = new LinkedList<>();

	public static final int PRJ_DMG_SEC = 8;
	public static final int UPD_TCK_PER = 2;

    @Override
    public void run() {
        final long start = System.currentTimeMillis();
        tick++;
        
        if ((tick & 15) == 0) {
            //скримеры 8191
            if ((tick & 8191) == 0) {
            	final List<Player> pls = w.getPlayers();
            	if (!pls.isEmpty()) {
                	final Player pl = pls.get(Main.srnd.nextInt(pls.size()));
                	final Location loc;
                	switch (Main.srnd.nextInt(16)) {
    				case 6:
    	            	switch (Main.subServer) {
    					case LOCUS:
    					case KALEUM:
    						loc = pl.getEyeLocation();
    						final Block b = loc.getBlock();
    						if (EffectUtil.getLight(b) < 8 && Math.abs(w.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) - b.getY()) < 5) {
    							pl.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 8, true, false, false));
    							pl.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 2, true, false, false));
    							pl.playSound(pl.getLocation(), Sound.ENTITY_GLOW_SQUID_SQUIRT, 1f, 0.6f);
    							final Entity[] ents = new Entity[16];
    							for (int i = ents.length - 1; i >= 0; i--) {
    								final Location nr = new Location(w, loc.getX() + NumUtil.rndSignNum(4, 4),
										loc.getY(), loc.getZ() + NumUtil.rndSignNum(4, 4));
    								nr.setY(w.getHighestBlockYAt(nr.getBlockX(), nr.getBlockZ()) + 1);
    								w.spawnParticle(Particle.SQUID_INK, nr, 20, 0.4d, 1d, 0.4d, 0d, null, false);
    								final Entity e = w.spawnEntity(nr, Main.subServer.mobType, false);
    								e.customName(TCUtil.form("§8§kdec re sta"));
    								e.setCustomNameVisible(false);
    								ents[i] = e;
    							}
    							
    							Ostrov.sync(() -> {
    								for (final Entity e : ents) {
    									if (e != null && e.isValid()) {
    										w.spawnParticle(Particle.SOUL, e.getLocation(), 40, 0.4d, 1d, 0.4d);
    										e.remove();
    									}
    								}
    							}, 50);
    						}
    						break;
    					default:
    						break;
    					}
    					break;
    				case 5, 4:
    	            	switch (Main.subServer) {
    					case INFERNAL:
    					case TERRA:
    						final PlayerInventory inv = pl.getInventory();
    						pl.playSound(pl.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 0.6f, 0.6f);
    						new BukkitRunnable() {
    							int i = 8;
    							@Override
    							public void run() {
    								if (!pl.isValid() || (i--) == 0) {
    									cancel();
    								}
    								shiftHotBar(inv);
    							}
    						}.runTaskTimer(Main.main, 2, 2);
    						break;
    					default:
    						break;
    					}
    					break;
    				case 3, 2:
    	            	switch (Main.subServer) {
    					case LOCUS:
    					case AQUAROD:
    						loc = pl.getEyeLocation();
    						if (EffectUtil.getLight(loc.getBlock()) < 6) {
    							pl.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 2, true, false, false));
    							pl.playSound(loc, Sound.ENTITY_WITCH_CELEBRATE, 0.4f, 0.4f);
    							pl.playSound(loc, Sound.ENTITY_WITCH_CELEBRATE, 0.4f, 0.4f);
    						}
    						break;
    					default:
    						break;
    					}
    					break;
    				case 1:
    	            	switch (Main.subServer) {
    					case INFERNAL:
    					case KALEUM:
    					case TERRA:
    						loc = pl.getEyeLocation();
    						if (EffectUtil.getLight(loc.getBlock()) < 4) {
    							pl.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 160, 2, true, false, false));
    							pl.playSound(loc, Sound.ENTITY_ENDERMAN_STARE, 0.6f, 1.4f);
    							Ostrov.sync(() -> ScreenUtil.sendTitle(pl, "", "§.я.. .", 10, 20, 10), 20);
    							Ostrov.sync(() -> ScreenUtil.sendTitle(pl, "", "§.. .тебя ..", 10, 20, 10), 60);
    							Ostrov.sync(() -> ScreenUtil.sendTitle(pl, "", "§. ..вижу. .", 10, 20, 10), 120);
    						}
    						break;
    					default:
    						break;
    					}
    					break;
    				case 0:
    	            	switch (Main.subServer) {
    					case KALEUM:
    					case TERRA:
    						loc = pl.getEyeLocation();
    						/*final Location far = w.getHighestBlockAt(
								loc.getBlockX() + ApiOstrov.rndSignNum(32, 20),
								loc.getBlockX() + ApiOstrov.rndSignNum(32, 20))
    							.getLocation().add(0.5d, 1.2d, 0.5d);
    						final Bot bt = new Bot(far, BotType.REDACTED);
    						w.spawnParticle(Particle.SQUID_INK, far, 40, 0.6d, 1d, 0.6d, 0d, null, false);
    						//pl.playSound(far, Sound.BLOCK_END_PORTAL_SPAWN, 10f, 1.4f);
    						bt.fakeMob.teleport(far.setDirection(loc.subtract(far).toVector()));
    						bt.fakeMob.setAI(false);
    						Ostrov.sync(() -> {
    							if (Bots.npcs.containsKey(bt.fakeMobID)) {
    								bt.remove(true);
    							}
    						}, 120);*/
    						break;
    					default:
    						break;
    					}
    					break;
    				default:
    					break;
    				}
            	}
            }
            
            /*final Iterator<Entry<Integer, CuBlock>> it = cublocks.entrySet().iterator();
            Entry<Integer, CuBlock> en;
            Slime sm;
            while (it.hasNext()) {
                en = it.next();
                sm = en.getValue().getCube();
                if (sm != null && sm.isValid()) {
                	en.getValue().transferTick(sm.getWorld());
                } else {
                	it.remove();
                }
            }*/
        }

		for (final LivingEntity e : noDmg)
			if (e.isValid()) e.setNoDamageTicks(0);
		noDmg.clear();

		final List<Ability.InterNext> nxs = new ArrayList<>();
		final Iterator<Ability.InterNext> nit = Ability.nexts.iterator();
		while (nit.hasNext()) {
			final Ability.InterNext in = nit.next();
			if (in == null || in.time() + Ability.stepCd < tick) continue;
			nxs.add(in); nit.remove();
		}
		for (final Ability.InterNext in : nxs) {
			in.ch().target().setNoDamageTicks(0);
			in.run();
		}

		final int sec = tick / 20;
		final boolean isSec = sec * 20 == tick;
		if (isSec) {
			ShotLst.projDmg.values().removeIf(dmgPrj ->
				dmgPrj.startSec() + PRJ_DMG_SEC < sec);
		}

		final boolean upd = tick % UPD_TCK_PER == 0;
        for (final Player p : Bukkit.getOnlinePlayers()) {
            final Survivor sv = PM.getOplayer(p, Survivor.class);
            if (sv == null) continue;
            //каждую секунду с рабросом по тикам для игроков
            if (isSec) {
                //задания
                if (sv.miniQuestTask != null && sv.miniQuestTask.secondTick()) {
                    sv.miniQuestTask = null;
                }
//                sv.transferTick(p.getWorld());
				if (sv.acBarPause > 0) sv.acBarPause--;
				else sv.updateBar(p);

				if (isDark(EntityUtil.center(p))) {
					p.addPotionEffect(BLIND);
					p.playSound(p, Sound.ENTITY_WARDEN_HEARTBEAT, 1f, 0.8f);
					sv.inform(p, "<gray>Здесь слишком темно...");
				}
            }

			if (!upd || sv.role == null) continue;
			for (final Skill sk : sv.skills) {
				sk.updateKd(p);
			}
			sv.updateBoard(p, SM.Info.HEALTH);
			// --- конец блока каждую секунду игрока ---
        }
        // --- конец блока каждый тик игрока ---

        proccesTime = (int) (System.currentTimeMillis() - start);
    }

	private static final int LIGHT = SM.value("light_level", 2);
	private boolean isDark(final Location loc) {
		return EffectUtil.getLight(loc.getBlock()) < LIGHT;
	}

	private void shiftHotBar(final PlayerInventory inv) {
    	final ItemStack fst = inv.getItem(0) == null ? null : inv.getItem(0).clone();
    	for (int i = 0; i < 8; i++) {
    		inv.setItem(i, inv.getItem(i + 1));
    	}
		inv.setItem(8, fst);
    }

	public static void unDTick(final LivingEntity e) {
		if (!e.isValid()) return;
		noDmg.add(e);
	}
    
}
