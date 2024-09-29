package ru.romindous.skills.transfers.modules;

public class Fuse /*extends CuBlock*/ {/*
	
	protected ItemStack item;

	public Fuse(final Slime cube, final boolean load) {
		super(cube, CuBType.FUSE, load);
	}
	
	@Override
	public String getUpdName() {
		return "§5Пробужденный Котел";
	}
	
	@Override
	public void setItem(final ItemStack itm) {
		if (cube.get() != null) {
			item = itm;
			cube.get().getEquipment().setHelmet(itm);
		}
	}
	
	@Override
	public ItemStack getItem() {
		return item;
	}
	
	@Override
	public void transferTick(final World in) {
		super.transferTick(in);
	}
	
	@Override
	public void remove(final Entity ent) {
		SM.cublocks.remove(ent.getEntityId());
		final Location cbl = new Location(ent.getWorld(), loc.x, loc.y, loc.z);
		cbl.getBlock().setType(Material.AIR);
		ent.getWorld().dropItemNaturally(cbl, cbt.blockItem);
		if (!ItemUtil.isBlankItem(item, false)) {
    		ent.getWorld().dropItem(cbl.add(0.5d, 0.5d, 0.5d), item);
		}
		for (final Player p : cInv.getManager().getOpenedPlayers(cInv)) {
			p.closeInventory();
		}
		ent.remove();
	}

	public static int fuseAmt(final Material mt) {
		if (mt == null) return 0;
		switch (mt) {
		case SEA_LANTERN:
			return 4;
		default:
			return 0;
		}
	}
*/}
