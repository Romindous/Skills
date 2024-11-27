package ru.romindous.skills.transfers.modules;

public class Feed /*extends CuBlock*/ {/*
	
	protected ItemStack item;

	public Feed(final Slime cube, final boolean load) {
		super(cube, CuBType.FEED, load);
    	this.item = cube.getEquipment().getHelmet();
    	this.item = item == null ? ItemUtil.air.clone() : item;
	}
	
	@Override
	public String getUpdName() {
		return "§dГолодный Костер";
	}
	
	@Override
	public void transferTick(final World in) {
        if (!ItemUtil.isBlank(item, false)) {
        	final int sub = burnAmt(item.getType());

    		final Location lc = getLoc(in);
    		if (sub != 0) {
    			if (sub > 0) {
    				if (souls + sub <= cbt.maxSouls) {
                		item.setAmount(item.getAmount() - 1);
                		updateItem(item);
                    	changeSouls(sub);
                        in.spawnParticle(Particle.SOUL_FIRE_FLAME, lc.add(0d, 0.5d, 0d), 20, 0.2d, 0.25d, 0.2d, 0d, null, false);
                        in.playSound(lc, Sound.BLOCK_BIG_DRIPLEAF_PLACE, 1f, 0.6f);
    				}
    			} else {
                	if (item.getAmount() >= sub && souls < cbt.maxSouls) {
                		item.setAmount(item.getAmount() + sub);
                		updateItem(item);
                    	changeSouls(1);
                        in.spawnParticle(Particle.SOUL_FIRE_FLAME, lc.add(0d, 0.5d, 0d), 20, 0.2d, 0.25d, 0.2d, 0d, null, false);
                        in.playSound(lc, Sound.BLOCK_BIG_DRIPLEAF_PLACE, 1f, 0.6f);
                	}
				}
    		} else {
        		updateItem(item);
            	in.spawnParticle(Particle.SQUID_INK, lc.add(0d, 0.5d, 0d), 2, 0.2d, 0.1d, 0.2d, 0d, null, false);
			}
        }
        
        super.transferTick(in);
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
		if (!ItemUtil.isBlank(item, false)) {
    		ent.getWorld().dropItem(cbl.add(0.5d, 0.5d, 0.5d), item);
		}
		for (final Player p : cInv.getManager().getOpenedPlayers(cInv)) {
			p.closeInventory();
		}
		ent.remove();
	}

	public static int burnAmt(final Material mt) {
    	switch (mt) {
		case ROTTEN_FLESH:
			return -2;
		case SPIDER_EYE, PHANTOM_MEMBRANE, DRIED_KELP:
			return 1;
		case COD, SALMON, TROPICAL_FISH, PUFFERFISH:
			return 2;
		case CHICKEN, MUTTON, RABBIT:
			return 4;
		case BEEF, PORKCHOP:
			return 5;
		case COOKED_COD, COOKED_SALMON, 
		COOKED_CHICKEN, COOKED_MUTTON, COOKED_RABBIT:
			return 6;
		case COOKED_BEEF, COOKED_PORKCHOP, DRIED_KELP_BLOCK:
			return 8;
		default:
			return 0;
		}
	}
*/}
