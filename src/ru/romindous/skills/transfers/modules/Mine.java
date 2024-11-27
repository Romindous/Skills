package ru.romindous.skills.transfers.modules;

public class Mine /*extends CuBlock*/ {/*
	
	private static final int dst = 4;
	
	public Mine(final Slime cube, final boolean load) {
		super(cube, CuBType.MINE, load);
		//Bukkit.broadcast(Component.text("lol", TextColor.color(200, 120, 80)).toBuilder().hoverEvent(HoverEvent.showText(Component.text("kek"))).build());
	}
	
	@Override
	public String getUpdName() {
		return "§bУпитанная Скала";
	}
	
	@Override
	public void transferTick(final World in) {
		if (souls < cbt.maxSouls) {
			final XYZ lc = loc.clone().add(Main.getRndPlusMinusNum(0, dst), 
				Main.getRndPlusMinusNum(0, dst), Main.getRndPlusMinusNum(0, dst));
			final int gain;
			switch (VM.getNmsServer().getFastMat(in, lc.x, lc.y, lc.z)) {
			case COBBLESTONE, COBBLED_DEEPSLATE, ANDESITE, DIORITE, GRANITE, TUFF:
				gain = -2;
				break;
			case STONE_BRICKS, DEEPSLATE_BRICKS, BRICKS, END_STONE, CALCITE:
				gain = 2;
				break;
			case NETHER_BRICKS, RED_NETHER_BRICKS, AMETHYST_BLOCK:
				gain = 4;
				break;
			case OBSIDIAN, CRYING_OBSIDIAN:
				gain = 8;
				break;
			default:
				gain = 0;
				break;
			}
			
			if (gain != 0) {
				final int amt = gain < 0 ? (Main.srnd.nextInt(-gain) == 0 ? 1 : 0) : gain;
				if (amt + souls <= cbt.maxSouls) {
					final Location bl = new Location(in, lc.x + 0.5d, lc.y + 0.5d, lc.z + 0.5d);
					final Block b = bl.getBlock();
					final BlockData bd = b.getBlockData();
					b.setBlockData(Main.AIR_DATA, false);
					in.spawnParticle(Particle.BLOCK_CRACK, bl, 32, 0.4d, 0.4d, 0.4d, 0d, bd);
					in.playSound(bl, bd.getSoundGroup().getBreakSound(), 1f, 1f);
					in.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, getLoc(in), 16, 0.4d, 0.6d, 0.4d, 0d);
					if (amt > 0) {
						in.spawnParticle(Particle.SCRAPE, bl, 24, 0.4d, 0.6d, 0.4d, 0d);
						in.playSound(bl, Sound.ITEM_AXE_SCRAPE, 1f, 0.6f);
						changeSouls(amt);
					}
				}
			}
		}
		
		super.transferTick(in);
	}
*/}
