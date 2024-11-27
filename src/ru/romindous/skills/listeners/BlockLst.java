package ru.romindous.skills.listeners;

import org.bukkit.Axis;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.version.Nms;




public class BlockLst implements Listener {

    private static final BlockFace[] nearBlockFaces = {BlockFace.DOWN, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};

    /*@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {

        final Player p = e.getPlayer();
        final Block b = e.getBlock();

        if (!canBreak(p, b, true)) {
            e.setCancelled(true);
            return;
        }

        for (final CuBlock fb : SM.cublocks.values()) {
            if (fb.isBlockOf(b)) {
                e.setCancelled(true);
                new EntityDamageEvent(fb.getCube(), DamageCause.ENTITY_ATTACK, 1d).callEvent();
                return;
            }
        }

        // ChunkContent cc = Land.getChunkContent(b.getLocation());
        //зачары
        final ItemStack inHand = p.getInventory().getItemInMainHand();
        if (!ItemUtil.isBlankItem(inHand, true)) {
        	final Location loc = b.getLocation().add(0.5d, 0.5d, 0.5d);
            for (final Entry<Enchantment, Integer> customEnchant : inHand.getEnchantments().entrySet()) {
                switch (customEnchant.getKey().getKey().getKey()) {

                    //Veining дает шанс срубить несколько похожих блоков в области, к примеру если рубишь уголь киркой, то уголь рядом может сламатся тоже
                    //Я сохранял BaseBlockPosition для обоих, ибо остальные чары на кирке (по типу удача, переплавка) тоже должны применятся на блоки сломаные через эти чары,
                    //кроме самих veining и expansion, а то будет бесконечный цыкл
                    case "veining":
                        if (!p.isSneaking() && b.isPreferredTool(inHand)) {// && !cc.deleteExpandedBlock(b) ) { //!SM.expandedBlocks.remove(new XYZ(b.getLocation()))) {
                            for (int i = customEnchant.getValue() * 2; i >= 0; i--) {
                                final Block near = b.getRelative(ClassUtil.rndElmt(nearBlockFaces));//getTrInt(), getTrInt(), getTrInt());
                                if (near.getType() == b.getType()) {
                                    //Land.addExpandedBlock(near);//SM.expandedBlocks.add(new XYZ(near.getLocation()));
                                    //final BlockBreakEvent bbe = new BlockBreakEvent(near, p);
                                    //onBrk(bbe);
                                    breakExpandBlock(e, near);
                                    if (e.isDropItems()) { //дропаем, если в ЭТОМ эвенте есть дроп bbe.isDropItems()) {
                                        near.getWorld().spawnParticle(Particle.BLOCK_CRACK, near.getLocation().add(0.5, 0.5, 0.5), 15, 0.6, 0.6, 0.6, near.getBlockData());
                                        near.breakNaturally(inHand);
                                    } else {//if (!bbe.isCancelled()) {
                                        near.setType(BlockType.AIR);
                                    }
                                }
                            }
                        }
                        break;

                    case "expansion":
                        if (!p.isSneaking() && b.isPreferredTool(inHand) && !b.isPassable()) {// && !cc.deleteExpandedBlock(b) ) { //!SM.expandedBlocks.remove(new XYZ(b.getLocation()))) {
                            final int enchantLvl = customEnchant.getValue().intValue();
                            final BlockFace bf = p.getTargetBlockFace(100);
                            if (bf == null) break;
                            Block near;
                            for (int g = -enchantLvl; g <= enchantLvl; g++) {
                                for (int j = -enchantLvl; j <= enchantLvl; j++) {
                                	if (g == 0 && j == 0) continue;
                                    near = switch (bf) {
                                    case EAST, WEST ->
                                        b.getRelative(0, g, j);
                                    case NORTH, SOUTH ->
                                        b.getRelative(g, j, 0);
                                    case UP, DOWN ->
                                        b.getRelative(g, 0, j);
                                    default ->
                                        null;
                                    };
                                    if (near != null && near.getType() == b.getType()) {
                                        breakExpandBlock(e, near);
                                    }

                                }
                            }
                        }
                        break;

                    case "smelting_touch":
                        e.setDropItems(false);
                        for (final ItemStack i : b.getDrops(inHand, p)) {
                            if (noSmltDrp(b, i)) {
                                b.getWorld().dropItem(loc, i);
                            }
                        }
                        return;

                    case "reparation":
                        if (b.isPreferredTool(inHand) && Main.srnd.nextInt(8) < customEnchant.getValue()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_STEP, 0.6f, 0.6f);
                            final Damageable damageable = (Damageable) inHand.getItemMeta();
                            //пусть будет как-то так, ну очень не хочется использовтаь отложенные задачи на такие мелочин
                            if (damageable.hasDamage()) {
                                damageable.setDamage(damageable.getDamage() - 1); //убавляем дамаж заранее
                                inHand.setItemMeta(damageable);
                            }
                        }
                        break;

                    case "nimble":
                        if (b.isPreferredTool(inHand)) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 50 - (5 * customEnchant.getValue()), customEnchant.getValue(), true, false, false));
                        }
                        break;

                    case "curse_of_lagging":
                        e.setCancelled(Main.srnd.nextInt(10) == 0);
                        break;

                    case "curse_of_fragmentation":
                        if (Main.srnd.nextInt(4) == 0) {
                            final Damageable id = (Damageable) inHand.getItemMeta();
                            id.setDamage(id.getDamage() + 1);
                            inHand.setItemMeta(id);
                        }
                        break;

                    case "curse_of_the_dead":
                        if (Main.srnd.nextInt(16) == 0) {
                            p.getWorld().spawnEntity(p.getLocation(), Main.subServer.mobType, false);//, false);
                        }
                        break;

                    case "curse_of_rotting":
                        if (Main.srnd.nextInt(12) == 0) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1));
                        }
                        break;

                }
            }

            switch (SkillMats.getCstmItm(inHand.getItemMeta())) {
			case RESTILE:
                if (e.getExpToDrop() != 0 && Main.srnd.nextInt(4) == 0) {
                	b.getWorld().spawn(loc, ThrownExpBottle.class)
                	.setVelocity(new Vector(Main.getRndPlusMinusNum(4, 6) * 0.01d, 0.2d, Main.getRndPlusMinusNum(4, 6) * 0.01d));
                }
			case AZULITE:
                if (e.getExpToDrop() != 0) {
                    e.setExpToDrop(e.getExpToDrop() * 2);
                }
				break;
			case BLAZE:
                e.setDropItems(false);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 12, 0.2d, 0.2d, 0.2d, 0d, null, false);
                for (final ItemStack i : b.getDrops(inHand, p)) {
                    if (noSmltDrp(b, i)) {
                        b.getWorld().dropItem(loc, i);
                    }
                }
				return;
			case SHULK:
                e.setDropItems(false);
                loc.getWorld().spawnParticle(Particle.END_ROD, loc, 8, 0.2d, 0.2d, 0.2d, 0d, null, false);
                final Vector vec = p.getLocation().subtract(loc).toVector();
                vec.setY(vec.getY() + 0.15d).multiply(0.12d);
                for (final ItemStack i : b.getDrops(inHand, p)) {
                	final Item it = b.getWorld().dropItem(loc, i);
                    it.setVelocity(vec);
                    it.setPickupDelay(0);
                }
				break;
			default:
				break;
			}
        }

        final Survivor sv = PM.getOplayer(p, Survivor.class);

        if (sv.miniQuestTask != null && sv.miniQuestTask instanceof MineTask) {
            ((MineTask) sv.miniQuestTask).checkBreak(p, b.getType());
        }

        if (b.getBlockData() instanceof Ageable) {
            final Ageable crp = (Ageable) b.getBlockData();
            if (crp.getAge() == crp.getMaximumAge() && sv.isReady(p, Ability.ЗЕМЛЕДЕЛИЕ)) {
                sv.addXp(p, (int) sv.getStatEffect(Ability.ЗЕМЛЕДЕЛИЕ));
            }
        }

        if (b.getType() == BlockType.SPAWNER) {
            sv.addXp(p, 64);
        }

    }*/

    /*private boolean canBreak(final Player p, final Block b, final boolean notify) {
        if (Main.bossMgr.isSpawnArea(b.getLocation())) {
            if (notify) {
                ScreenUtil.sendActionBarDirect(p, "§cТерритория здесь слишком нестабильная, чтобы ломать блоки!");
            }
            return false;
        }
        final ChunkContent cc = Land.getChunkContent(b.getLocation());
        if (cc != null) {
            if (cc.hasProtectionInfo()) {
                final ProtectionInfo pInfo = LockAPI.getProtectionInfo(cc, b);
                if (pInfo != null) {
                    if (Tag.WALL_SIGNS.isTagged(b.getType())) {
                        if (pInfo.isExpiried()) {
                            p.sendMessage("§6Ограничение доступа было просрочено!");
                            cc.removeProtectionInfo(b.getLocation());
                        } else if (pInfo.isOwner(p.getName())) {
                            p.sendMessage("§cВы сняли ограничение доступа!");
                            cc.removeProtectionInfo(b.getLocation());
                        } //else if (cc.getFaction().getRole(e.getPlayer().getName())==Role.Лидер ){
                        //    p.sendMessage("§cВы сняли ограничение доступа, наложенное "+pInfo.getOwner()+"!");
                        //    cc.removeProtectionInfo(b.getLocation());
                        //}
                        else {
                            ScreenUtil.sendActionBarDirect(p, "§cОграничитель доступа нельзя сломать!");
                            p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 0.5f);
                            return false;
                        }
                    } else {
                        ScreenUtil.sendActionBarDirect(p, "§cСначала нужно убрать ограничение доступа!");
                        p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 0.5f);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean noSmltDrp(final Block b, final ItemStack i) {
        final Iterator<Recipe> recs = Bukkit.recipeIterator();
        while (recs.hasNext()) {
            final Recipe rc = recs.next();
            if (rc instanceof FurnaceRecipe && ((FurnaceRecipe) rc).getInputChoice().test(i)) {
                final ItemStack rs = rc.getResult();
                rs.setAmount(i.getAmount());
                b.getWorld().dropItemNaturally(b.getLocation().add(Main.srnd.nextFloat(), 0, Main.srnd.nextFloat()), rs);
                return false;
            }
        }
        return true;
    }*/

    
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) //может быть cancel в BossListener
    public void onPlace(final BlockPlaceEvent e) {
//        final ItemStack is = e.getItemInHand();
//        final Player p = e.getPlayer();
        final Block b = e.getBlockPlaced();
        
        /*if (is.hasItemMeta()) {
        	final Slime cb;
            switch (SkillMats.getCstmItm(is.getItemMeta())) {
                case GRENADE:
                    e.setCancelled(true);
                    return;
                case LUCKOR:
                    final EndPortalFrame epd = (EndPortalFrame) b.getBlockData();
                    epd.setEye(true);
                    b.setBlockData(epd, false);
                    cb = b.getWorld().spawn(b.getLocation().add(0.5d, 0d, 0.5d), Slime.class);
                    SM.cublocks.put(cb.getEntityId(), CuBlock.createOf(cb, CuBType.REPAIR, false));
                    return;
                case HELLITE:
                    cb = b.getWorld().spawn(b.getLocation().add(0.5d, 0d, 0.5d), Slime.class);
                    SM.cublocks.put(cb.getEntityId(), CuBlock.createOf(cb, CuBType.RELAY, false));
                    return;
                case BLAZE:
                    cb = b.getWorld().spawn(b.getLocation().add(0.5d, 0d, 0.5d), Slime.class);
                    SM.cublocks.put(cb.getEntityId(), CuBlock.createOf(cb, CuBType.FEED, false));
                    return;
                case CHITIN:
                    cb = b.getWorld().spawn(b.getLocation().add(0.5d, 0d, 0.5d), Slime.class);
                    SM.cublocks.put(cb.getEntityId(), CuBlock.createOf(cb, CuBType.STORE, false));
                    return;
                case CALYX:
                    cb = b.getWorld().spawn(b.getLocation().add(0.5d, 0d, 0.5d), Slime.class);
                    SM.cublocks.put(cb.getEntityId(), CuBlock.createOf(cb, CuBType.SPREAD, false));
                    return;
                case WITHER:
                    cb = b.getWorld().spawn(b.getLocation().add(0.5d, 0d, 0.5d), Slime.class);
                    SM.cublocks.put(cb.getEntityId(), CuBlock.createOf(cb, CuBType.MINE, false));
                    return;
                case SIPHOR:
                    cb = b.getWorld().spawn(b.getLocation().add(0.5d, 0d, 0.5d), Slime.class);
                    SM.cublocks.put(cb.getEntityId(), CuBlock.createOf(cb, CuBType.FUSE, false));
                    return;
				default:
					break;
            }
        }*/

        switch (b.getType()) {
            case MELON_STEM, PUMPKIN_STEM, BEETROOTS, WHEAT, POTATOES, CARROTS:
            	if (BlockType.FARMLAND.equals(b.getRelative(BlockFace.DOWN).getType().asBlockType())) {
                    final Farmland fl = (Farmland) b.getRelative(BlockFace.DOWN).getBlockData();
                    if (fl.getMoisture() == 0) {
                    	e.setBuild(false);
                    }
            	}
            	break;
			default:
				break;
        }

        /*final BossType bossByMat = BossType.finBlks.get(b.getType());
            *//*if (Timer.has(p, "bossSpawn")) {
                p.sendMessage(Main.prefix + "босса можно призвать снова через "+Timer.getLeft(p, "bossSpawn"));
                return;
            }
            Timer.add(p, "bossSpawn", 10);*//*

        if (bossByMat != null) {
            //p.sendMessage("boss-" + bossByMat.toString());
            if (bossByMat.subserver != Main.subServer) {//чтоб не надоедать игрокам, пока что
                //p.playSound(b.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 1f, 2f);
                //p.sendMessage("Этот босс спавнится только в мире "+bossByMat.subserver.displayName);
                return;
            }
            
            if (!Main.bossMgr.isOn()) {
                p.sendMessage("Спавн босса "+bossByMat+" невозможен - боссы отключены!");
                return;
            }

            final Schematic schem = WE.getSchematic(Bukkit.getConsoleSender(), bossByMat.displayName);
            if (schem == null) {
                //p.sendMessage("no schem");
                Ostrov.log_err("Нет схематика для спавна босса " + bossByMat.displayName);
                e.setCancelled(true);
                return;
            }

            boolean spawn = false;
            Schematic.CompareResult cr;
            //чекаем со всеми ротациями, ищем совпадение
            for (final Rotate rotate : Schematic.Rotate.values()) {
                cr = schem.compare(b.getLocation(), rotate, true);
                if (cr.mustBe.isEmpty()) { //найдено совпадение
                    spawn = true;
                    Main.bossMgr.spawn(p, b.getLocation(), bossByMat, cr.cuboid);
                    break;
                }
            }
            if (!spawn) {
                p.sendMessage(Main.prefix + "Спавнер построен неправильно!");
                //cr.print(p, cr);
                //return;`x
            }
        }*/

    }

    /*private void breakExpandBlock(final BlockBreakEvent e, final Block near) {
        if (!canBreak(e.getPlayer(), near, false)) {
            return;
        }
        if (e.isDropItems()) { //дропаем, если в ЭТОМ эвенте есть дроп bbe.isDropItems()) {
            near.getWorld().spawnParticle(Particle.BLOCK_CRACK, near.getLocation().add(0.5, 0.5, 0.5), 15, 0.6, 0.6, 0.6, near.getBlockData());
            near.breakNaturally(e.getPlayer().getInventory().getItemInMainHand());
        } else {//if (!bbe.isCancelled()) {
            near.setType(BlockType.AIR);
        }
    }*/

    public boolean plcAtmpt(final Block b, final BlockFace bf) {
        final World w = b.getWorld();
        final XYZ loc = new XYZ(b.getLocation());
//        final XYZ rev = new XYZ(b.getWorld().getName(), bf.getOppositeFace().getModX(), bf.getOppositeFace().getModY(), bf.getOppositeFace().getModZ());

        int hight = 1;
        int width = 1;
        
        while (!Nms.fastType(w, loc.x, loc.y, loc.z).isSolid() && hight < 20) {
        	loc.y += 1;
        	hight++;
        }
        loc.y -= hight;
        
        final int dr;
        if (bf.getModX() == 0) {
        	dr = bf.getModZ();
            while (!Nms.fastType(w, loc.x, loc.y, loc.z).isSolid() && width < 20) {
            	loc.z += dr;
            	width++;
            }
        	loc.z -= dr * width;
        	
            if (!nrMinBlck(loc, w, BlockType.OBSIDIAN, 2)
                    || !nrMinBlck(new XYZ(loc.worldName, loc.x, loc.y + hight - 1, loc.z), w, BlockType.OBSIDIAN, 2)
                    || !nrMinBlck(new XYZ(loc.worldName, loc.x, loc.y, loc.z + width - 1), w, BlockType.OBSIDIAN, 2)
                    || !nrMinBlck(new XYZ(loc.worldName, loc.x, loc.y + hight - 1, loc.z + width - 1), w, BlockType.OBSIDIAN, 2)) {
                return false;
            }
            for (byte y = 1; y < hight - 1; y++) {
                if (!nrMinBlck(new XYZ(loc.worldName, loc.x, loc.y + y, loc.z), w, BlockType.OBSIDIAN, 1)
                        || !nrMinBlck(new XYZ(loc.worldName, loc.x, loc.y + y, loc.z + width - 1), w, BlockType.OBSIDIAN, 1)) {
                    return false;
                }
            }
            for (byte xz = 1; xz < width - 1; xz++) {
                if (!nrMinBlck(new XYZ(loc.worldName, loc.x, loc.y, loc.z + xz), w, BlockType.OBSIDIAN, 1)
                        || !nrMinBlck(new XYZ(loc.worldName, loc.x, loc.y + hight - 1, loc.z + xz), w, BlockType.OBSIDIAN, 1)) {
                    return false;
                }
            }
            if (width < 2 || hight < 3) {
                return false;
            }
            final Block[] pbs = new Block[width * hight];
            int j = 0;
            for (int xz = 0; xz < width; xz++) {
                for (int y = 0; y < hight; y++) {
                    switch ((pbs[j] = b.getRelative(0, y, xz)).getType()) {
                        case AIR:
                        case CAVE_AIR:
                        case FIRE:
                            j++;
                            break;
                        default:
                            return false;
                    }
                }
            }
            
            final Orientable or = BlockType.NETHER_PORTAL.createBlockData();
            or.setAxis(Axis.Z);
            for (final Block pb : pbs) pb.setBlockData(or, false);
        } else {
        	dr = bf.getModX();
            while (!Nms.fastType(w, loc.x, loc.y, loc.z).isSolid() && width < 20) {
            	loc.x += dr;
            	width++;
            }
        	loc.x -= dr * width;

        	
            if (!nrMinBlck(loc, w, BlockType.OBSIDIAN, 2)
                    || !nrMinBlck(new XYZ(loc.worldName, loc.x, loc.y + hight - 1, loc.z), w, BlockType.OBSIDIAN, 2)
                    || !nrMinBlck(new XYZ(loc.worldName, loc.x + width - 1, loc.y, loc.z), w, BlockType.OBSIDIAN, 2)
                    || !nrMinBlck(new XYZ(loc.worldName, loc.x + width - 1, loc.y + hight - 1, loc.z), w, BlockType.OBSIDIAN, 2)) {
                return false;
            }
            for (byte y = 1; y < hight - 1; y++) {
                if (!nrMinBlck(new XYZ(loc.worldName, loc.x, loc.y + y, loc.z), w, BlockType.OBSIDIAN, 1)
                        || !nrMinBlck(new XYZ(loc.worldName, loc.x + width - 1, loc.y + y, loc.z), w, BlockType.OBSIDIAN, 1)) {
                    return false;
                }
            }
            for (byte xz = 1; xz < width - 1; xz++) {
                if (!nrMinBlck(new XYZ(loc.worldName, loc.x + xz, loc.y, loc.z), w, BlockType.OBSIDIAN, 1)
                        || !nrMinBlck(new XYZ(loc.worldName, loc.x + xz, loc.y + hight - 1, loc.z), w, BlockType.OBSIDIAN, 1)) {
                    return false;
                }
            }
            if (width < 2 || hight < 3) {
                return false;
            }
            final Block[] pbs = new Block[width * hight];
            int j = 0;
            for (int xz = 0; xz < width; xz++) {
                for (int y = 0; y < hight; y++) {
                    switch ((pbs[j] = b.getRelative(xz, y, 0)).getType()) {
                    case AIR:
                    case CAVE_AIR:
                    case FIRE:
                        j++;
                        break;
                    default:
                        return false;
                    }
                }
            }

            final Orientable or = BlockType.NETHER_PORTAL.createBlockData();
            for (final Block pb : pbs) pb.setBlockData(or, false);
        }
        return true;
    }

    public boolean nrMinBlck(final XYZ bl, final World w, final BlockType bt, int amt) {
        for (final BlockFace bf : nearBlockFaces) {
            if (bt.equals(Nms.fastType(w, bl.x + bf.getModX(), bl.y + bf.getModY(), bl.z + bf.getModZ()))) {
                amt--;
            }
        }
        return amt <= 0;
    }

}
