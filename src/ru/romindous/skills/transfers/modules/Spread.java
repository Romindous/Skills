package ru.romindous.skills.transfers.modules;

public class Spread /*extends CuBlock*/ {/*
	
	private static final int dst = 5;
	private static final BlockData air, vine, sens, shrk;
	
	static {
		air = Material.AIR.createBlockData();
		sens = Material.SCULK_SENSOR.createBlockData();
		final SculkVein sv = (SculkVein) (vine = Material.SCULK_VEIN.createBlockData());
		for (final BlockFace bf : sv.getAllowedFaces()) sv.setFace(bf, false);
		sv.setFace(BlockFace.DOWN, true);
		((SculkShrieker) (shrk = Material.SCULK_SHRIEKER.createBlockData())).setCanSummon(true);
	}
	
	public Spread(final Slime cube, final boolean load) {
		super(cube, CuBType.SPREAD, load);
	}
	
	@Override
	public String getUpdName() {
		return "§5Зараженный Катализатор";
	}
	
	@Override
	public void transferTick(final World in) {
		if (souls > 0) {
			int total = 0;
			for (int i = (souls >> 2) + 1; i > 0; i--) {
				final XYZ lc = loc.add(Main.getRndPlusMinusNum(0, dst), 
					Main.getRndPlusMinusNum(0, dst), Main.getRndPlusMinusNum(0, dst));
				final Material mat = VM.getNmsServer().getFastMat(in, lc.x, lc.y, lc.z);
				if (Tag.SCULK_REPLACEABLE.isTagged(mat)) {
					final Location bl = new Location(in, lc.x + 0.5d, lc.y + 0.5d, lc.z + 0.5d);
					final BlockData bd;
					switch (Main.srnd.nextInt(10)) {
					case 0:
						bd = vine;
						break;
					case 1:
						bd = sens;
						break;
					case 2:
						bd = shrk;
						break;
					case 3, 4, 5, 6, 7:
						bd = air;
						break;
					default:
						bl.getBlock().breakNaturally(Main.air, true);
						in.playSound(bl, Sound.BLOCK_FUNGUS_BREAK, 1f, 0.8f);
						in.spawnParticle(Particle.SCULK_SOUL, bl, 16, 0.2d, 0.2d, 0.2d, 0d);
						continue;
					}
					final Block b = bl.getBlock();
					b.breakNaturally(Main.air, true);
					b.setType(Material.SCULK, false);
					in.playSound(bl, Sound.BLOCK_SCULK_PLACE, 2f, 0.8f);
					if (VM.getNmsServer().getFastMat(in, lc.x, lc.y + 1, lc.z).isAir()) {
						in.getBlockAt(lc.x, lc.y + 1, lc.z).setBlockData(bd, false);
						in.spawnParticle(Particle.SCULK_SOUL, bl, 24, 0.4d, 0.6d, 0.4d, 0d);
						continue;
					}
					in.spawnParticle(Particle.SCULK_SOUL, bl, 16, 0.4d, 0.4d, 0.4d, 0d);
					total++;
				}
			}
			
			if (total != 0) {
				final Location sl = getLoc(in);
				in.playSound(sl, Sound.BLOCK_SCULK_PLACE, 1f, 0.8f);
				in.spawnParticle(Particle.SCULK_CHARGE_POP, sl, total << 1, 0.4d, 0.5d, 0.4d, 0d);
				changeSouls(-total);
			}
		}
		
		super.transferTick(in);
	}
*/}
