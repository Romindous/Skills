package ru.romindous.skills.transfers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.InventoryManager;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.romindous.skills.Main;
import ru.romindous.skills.SM;
import ru.romindous.skills.enums.CuBType;
import ru.romindous.skills.enums.TransferType;
import ru.romindous.skills.menus.CuBlockMenu;

public class CuBlock implements Transfer {

    protected final WeakReference<Slime> cube;
    protected final WeakReference<Transfer>[] to;
    public final XYZ loc;
    public final CuBType cbt;
    public final int id;
    public int souls;
	public final SmartInventory cInv;
	
	@SuppressWarnings("unchecked")
    public CuBlock(final Slime cube, final CuBType ft, final boolean load) {
        this.cube = new WeakReference<>(cube);
        final Location lc = cube.getLocation();
        this.loc = new XYZ(lc.getWorld().getName(), lc.getBlockX(), lc.getBlockY(), lc.getBlockZ());
        this.id = SM.tId++;
        this.cbt = ft;
        switch (ft.trans) {
		case GIVE, BOTH:
        	this.to = new WeakReference[ft.maxTeth];
        	Arrays.fill(to, etr);
			break;
		default:
			this.to = null;
			break;
		}
        cube.setCollidable(false);
    	cube.setAI(false);
    	cube.setInvisible(true);
    	cube.setSilent(true);
    	cube.setSize(2);
    	cube.setGravity(false);
    	cube.setCustomNameVisible(true);
    	cube.setRemoveWhenFarAway(false);
    	cube.customName(Component.text(getUpdName()));
    	if (load) {
    		Bukkit.getConsoleSender().sendMessage("loading with-" + cube.getMaxFireTicks());
            this.souls = cube.getMaximumAir();
    	} else {
            this.souls = 0;
		}
    	this.cInv = SmartInventory.builder()
    		.type(InventoryType.HOPPER)
    		.manager(new InventoryManager())
            .id("Cube" + cube.getEntityId())
            .provider(new CuBlockMenu(this))
            .title("         " + getUpdName())
            .build();
    }

    public boolean isBlockOf(final Block b) {
        return loc.x == b.getX() && loc.y == b.getY() && loc.z == b.getZ() && b.getType().asBlockType().equals(cbt.mat);
    }

    public Slime getCube() {
        return cube.get();
    }

	@Override
	public boolean transferSouls(final World in) {
        if (to == null) return false;
        int total = souls >> 1;
        if (total == 0) return false;
		final ArrayList<Transfer> real = new ArrayList<>();
        for (int i = 0; i < to.length; i++) {
        	final Transfer tr = to[i].get();
			if (Transfer.validate(tr) && 
				tr.getSouls() < tr.getMaxSouls()) {
				real.add(tr);
			} else {
				to[i] = new WeakReference<Transfer>(null);
			}
		}
        if (real.isEmpty()) return false;
		final int each = total / real.size();
		total = each * real.size();
		if (each == 0) return false; 
		for (final Transfer tr : real) {
			if (tr instanceof final CuBlock cb) {
                final int amt = each > cb.cbt.maxSouls - cb.souls
					? cb.cbt.maxSouls - cb.souls : each;
				total -= each - amt;
				if (amt == 0) continue;
				tr.changeSouls(amt);
			} else {
				tr.changeSouls(each);
			}
			tetherTo(tr, in);
		}
		
		changeSouls(-total);
		return true;
	}

	@Override
	public void changeSouls(final int amt) {
    	if (amt == 0) return;
		souls += amt;
		final Slime cb = getCube();
		if (cb != null) {
			EntityUtil.indicate(loc.getCenterLoc(),
				(amt < 0 ? "§9" : "§3+") + amt + "✞", cb.getWorld().getPlayers());
			cb.setMaximumAir(souls);
		}
	}
	
	@Override
	public void tetherTo(final Transfer to, final World w) {
		final Location loc = getLoc(w);
		final Location step = to.getLoc(w).subtract(loc);
		final int length = (int) step.length();
		step.multiply(0.25d/length);
		new BukkitRunnable() {
			int i = length << 2;
			@Override
			public void run() {
                w.spawnParticle(Particle.SOUL, loc.add(step), 1, 0.1d, 0.1d, 0.1d, 0d);
                if ((i & 3) == 0) w.playSound(loc, Sound.BLOCK_SCULK_CHARGE, 1f, 0.8f);
                
				if ((i--) < 0) cancel();
			}
		}.runTaskTimer(Main.main, 2, 2);
	}

	@Override
	public int getSouls() {
		return souls;
	}

	@Override
	public int getMaxSouls() {
		return cbt.maxSouls;
	}

	@Override
	public int getTId() {
		return id;
	}

	@Override
	public Location getLoc(final World w) {
		return new Location(w, loc.x + 0.5d, loc.y + 0.5d, loc.z + 0.5d);
	}

	@Override
	public TransferType getTransType() {
		return cbt.trans;
	}
	
	@Override
	public void transferTick(final World in) {
        if (to == null) return;
    	if (souls == 0) {
			Arrays.fill(to, etr);
            return;
    	}
    	
    	int slots = 0;
    	final Location loc = getLoc(in);
        for (int i = 0; i < to.length; i++) {
        	final Transfer tr = to[i].get();
            if (Transfer.validate(tr)) {
            	if (tr.getLoc(in).distanceSquared(loc) < distSQ) continue;
                to[i] = etr;
                slots++;
            } else slots++;
    	}
    	
        for (final CuBlock cb : SM.getTypeTransfers(TransferType.TAKE)) {
        	if (slots != 0 && cb.souls < cb.cbt.maxSouls && 
        		cb.getLoc(in).distanceSquared(loc) < distSQ && addTo(cb)) {
        		slots--;
        	}
        }
    	
        for (final CuBlock cb : SM.getTypeTransfers(TransferType.BOTH)) {
        	if (slots != 0 && cb.souls < cb.cbt.maxSouls && 
        		cb.getLoc(in).distanceSquared(loc) < distSQ && addTo(cb)) {
        		slots--;
        	}
        }
    	
        /*for (final Survivor sv : PM.getOplayers(Survivor.class)) {
        	if (slots != 0 && sv.getLoc(in).distanceSquared(loc) < distSQ && addTo(sv)) {
        		slots--;
        	}
        }*/

        transferSouls(in);
	}

	@Override
	public boolean addTo(final Transfer tr) {
		final int id = tr.getTId();
		if (getTId() == id || this.to == null) return false;
		int spot = to.length;
        for (int i = 0; i < to.length; i++) {
			final Transfer t = to[i].get();
			if (Transfer.validate(t)) {
				if (t.getTId() == id) return false;
			} else spot = Math.min(spot, i);
		}
        if (spot == to.length || tr.hasTo(this)) return false;
		to[spot] = new WeakReference<>(tr);
		return true;
	}

	@Override
	public boolean hasTo(final Transfer tr) {
		final int id = tr.getTId();
		if (getTId() == id || this.to == null) return false;
        for (int i = 0; i < to.length; i++) {
			final Transfer t = to[i].get();
			if (Transfer.validate(t) && t.getTId() == id) {
		        return true;
			}
		}
        return false;
	}

	@Override
	public boolean rmvTo(final Transfer tr) {
		boolean fnd = false;
		final int id = tr.getTId();
		if (getTId() == id || this.to == null) return false;
        for (int i = 0; i < to.length; i++) {
			final Transfer t = to[i].get();
			if (Transfer.validate(t) && t.getTId() == id) {
				to[i] = etr;
				fnd = true;
			}
		}
        return fnd;
	}

	public String getUpdName() {
		return " ";
	}
	
	@Override
	public boolean equals(final Object o) {
		return o instanceof CuBlock && ((CuBlock) o).loc.equals(loc);
	}
	
	@Override
	public int hashCode() {
		return loc.hashCode();
	}

	/*public static CuBlock createOf(final Slime cube, final CuBType type, final boolean load) {
        return switch (type) {
            case REPAIR -> new Repair(cube, load);
            case RELAY -> new Relay(cube, load);
            case FEED -> new Feed(cube, load);
            case FUSE -> new Fuse(cube, load);
            case MINE -> new Mine(cube, load);
            case SPREAD -> new Spread(cube, load);
            case STORE -> new Store(cube, load);
        };
	}*/

	public void remove(final Entity ent) {
		SM.cublocks.remove(ent.getEntityId());
		final Location cbl = getLoc(ent.getWorld());
		cbl.getBlock().setType(Material.AIR, false);
		cbl.getWorld().dropItem(cbl, cbt.blockItem);
		ent.remove();
	}

	public ItemStack getItem() {
		return ItemUtil.air.clone();
	}

	public void setItem(final ItemStack item) {}
	
	public void updateItem(final ItemStack itm) {
		setItem(itm);
		for (final Player p : InventoryManager.getOpenedPlayers(cInv)) {
			cInv.getProvider().update(p, InventoryManager.getContents(p).get());
		}
	}
	
	@Override
	public String toString() {
		return "id-" + id + ", type-" + cbt.name();
	}
}
