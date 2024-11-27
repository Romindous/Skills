package ru.romindous.skills.listeners;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.SM;
import ru.romindous.skills.Survivor;
import ru.romindous.skills.enums.SubServer;
import ru.romindous.skills.enums.Stat;





public class RehabLst implements Listener {

    public static final BlockFace[] near = {BlockFace.DOWN, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBed(final PlayerSetSpawnEvent e) {
//        QM.tryCompleteQuest(e.getPlayer(), Quest.SetSpawn, 1, true);
        switch (e.getCause()) {
            case RESPAWN_ANCHOR, BED:
                SM.setSpawn(e.getPlayer(), e.getLocation());
                break;
            case PLAYER_RESPAWN:
                SM.setSpawn(e.getPlayer(), null);
                break;
            default: break;
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRsp(final PlayerRespawnEvent e) {
        final Player p = e.getPlayer();
        p.setNoDamageTicks(3*20); //три секунды чтобы разбаговаться
        final Survivor sv = PM.getOplayer(p, Survivor.class);
        
        Ostrov.sync(() -> {
            if (p.isOnline() && !p.isDead()) {
                sv.applySkill(p);
                p.setHealth(sv.maxHP);
                sv.currentLiveSec = 0;
            }
        }, 2);
        
        final Location rspLoc = p.getRespawnLocation();
        if (rspLoc == null) {
            SM.randomJoin(p, Main.subServer == SubServer.WASTES);
            return;
        }

        //было дело - пока не был насервере, постройку с кроватью снесли, и респавнило в воздухе
//        ApiOstrov.teleportSave(p, rspLoc, false);
//        e.setRespawnLocation(rspLoc);
        final Location deathLoc = p.getLastDeathLocation();
        if (deathLoc == null) return;
        if (Math.abs(deathLoc.getBlockX() - rspLoc.getBlockX()) < 2
            && Math.abs(deathLoc.getBlockY() - rspLoc.getBlockY()) < 2
            && Math.abs(deathLoc.getBlockZ() - rspLoc.getBlockZ()) < 2) {
            p.sendMessage(Main.prefix + "Похоже что ты " + Main.P + "застрял " + Main.N + "возле своей " + Main.P + "кровати " + Main.N + "("
                + Main.P + rspLoc.getBlockX() + Main.N + ", " + Main.P + rspLoc.getBlockY() + Main.N + ", " + Main.P + rspLoc.getBlockZ()
                + Main.N + ")!\n" + Main.N + "Перемещаем тебя в более " + Main.P + "безопасное " + Main.N + "место...");
            SM.setSpawn(p, null);
//            PM.getOplayer(p).world_positions.replace(p.getWorld().getName(), null);
            SM.randomJoin(p, Main.subServer == SubServer.WASTES);
        }
        
        
        //p.sendMessage(Main.prefix + "Вы неуязвимы след. 30 сек!");
    }
    
    
    /*@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRspSet(final PlayerSetSpawnEvent e) {
        switch (e.getCause()) {
		case PLAYER_RESPAWN, BED:
			e.setCancelled(true);
			e.setNotifyPlayer(false);
		case PLUGIN, UNKNOWN:
			break;
		default:
	        final Player p = e.getPlayer();
    		SM.setSpawn(p, p.getLocation());
			break;
		}
    }*/

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFood(final FoodLevelChangeEvent e) {
        final HumanEntity pl = e.getEntity();
        final ItemStack it = e.getItem();
        if (ItemUtil.isBlank(it, false)) return;
        final Survivor sv = PM.getOplayer(pl, Survivor.class);
        if (sv == null) return;
        final int food = Math.max((int) Stat.food(e.getFoodLevel() - pl.getFoodLevel(), sv.getStat(Stat.METABOLISM)), 20);
        e.setFoodLevel(food);
        final float sat = it.getItemMeta().getFood().getSaturation();
        pl.setSaturation(Math.max((float) Stat.food(sat, sv.getStat(Stat.METABOLISM)) - sat + pl.getSaturation(), food));
    }
    
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHeal(final EntityRegainHealthEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            final Player p = (Player) e.getEntity();
            final Survivor sv = PM.getOplayer(p, Survivor.class);
            if (sv==null) return;
            
            double amount=e.getAmount();
           
            switch (e.getRegainReason()) {
                //через CUSTOM добавляется в:
                // - SM по таймеру, ачивка ОБНОВЛЕНИЕ
                // - при регене ачивками в EntityDamageLst
                case CUSTOM:
                case ENDER_CRYSTAL:     //для эндэр-дракона
                case WITHER:            //во время полёта EntityWitherSkull
                case WITHER_SPAWN:      //WITHER_SPAWN
                case REGEN:             //PEACEFUL && GameRules("naturalRegeneration") - скиловый
//p.sendMessage("§aRegen §8reason="+e.getRegainReason()+" return!!!");
                    return;
                case MAGIC:             //tick EntityLiving.heal((float)Math.max(4 << i, 0), RegainReason.MAGIC);
                case MAGIC_REGEN:       //зелье регенерации
                case EATING:            //еда и вампир
                case SATIATED:          //при полной шкалы еды
                    amount = Stat.regen(amount, sv.getStat(Stat.METABOLISM));
                    break;
            }
            e.setAmount(amount); //к реальным сердечкам добавляем пересчитанноею. setHearts выполнится естественным образом
            
        }
        
    }

    
    
    

    
    
    



}
