package ru.romindous.skills.transfers.modules;

public class Repair /*extends CuBlock*/ {/*
	
	protected ItemStack item;

	public Repair(final Slime cube, final boolean load) {
		super(cube, CuBType.REPAIR, load);
    	this.item = cube.getEquipment().getHelmet();
    	this.item = item == null ? Main.air.clone() : item;
	}
	
	@Override
	public String getUpdName() {
		return "§aСтол Подношений";
	}
	
	@Override
	public void transferTick(final World in) {
        if (!ItemUtil.isBlankItem(item, true) && item.getItemMeta() instanceof Damageable) {
            final Damageable db = (Damageable) item.getItemMeta();
            if (db.getDamage() > 0) {
                final int repBy = Math.max(1, souls >> 1);
                final int sSub = db.getDamage() - repBy < 0 ? db.getDamage() : repBy;
        		final Location lc = getLoc(in);
                if (souls >= sSub) {
                    db.setDamage(db.getDamage() - sSub);
                    item.setItemMeta(db);
                    updateItem(item);
                	changeSouls(-sSub);
                    in.spawnParticle(Particle.TOTEM, lc.add(0d, 0.5d, 0d), 20, 0.2d, 0.1d, 0.2d);
                    in.playSound(lc, Sound.BLOCK_BIG_DRIPLEAF_PLACE, 1f, 0.6f);
                } else {
                    updateItem(item);
                	in.spawnParticle(Particle.SQUID_INK, lc.add(0d, 0.5d, 0d), 2, 0.2d, 0.1d, 0.2d, 0d, null, false);
				}
            } else {
                updateItem(item);
            }
        }
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
	public void remove(final Entity ent) {
		SM.cublocks.remove(ent.getEntityId());
		final Location cbl = new Location(ent.getWorld(), loc.x, loc.y, loc.z);
		cbl.getBlock().setBlockData(Main.AIR_DATA);
		ent.getWorld().dropItemNaturally(cbl, cbt.blockItem);
		if (!ItemUtil.isBlankItem(item, false)) {
    		ent.getWorld().dropItem(cbl.add(0.5d, 0.5d, 0.5d), item);
		}
		for (final Player p : cInv.getManager().getOpenedPlayers(cInv)) {
			p.closeInventory();
		}
		ent.remove();
	}
*/}
