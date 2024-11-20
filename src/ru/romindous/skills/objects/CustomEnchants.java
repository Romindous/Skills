package ru.romindous.skills.objects;

public class CustomEnchants {
    /*
    case "aerowdynamic":
        prj.setVelocity(prj.getVelocity().multiply(0.2d * en.getValue() + 1d));
        break;
    case "curse_of_fragmentation":
        if (Main.srnd.nextInt(4) == 0) {
            final Damageable id = (Damageable) pi.getItemMeta();
            id.setDamage(id.getDamage() + 1);
            pi.setItemMeta(id);
        }
        break;
    case "curse_of_rotting":
        if (Main.srnd.nextInt(12) == 0 && prj.getShooter() instanceof LivingEntity) {
            ((LivingEntity) prj.getShooter()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            ((LivingEntity) prj.getShooter()).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1));
        }
        break;

    case "aerowdynamic":
        prj.setVelocity(prj.getVelocity().multiply(0.1d * en.getValue() + 1d));
        store = true;
        break;
    case "spectral":
        if (Main.srnd.nextInt(6) < en.getValue()) {
            final Vector vc = prj.getVelocity().clone().multiply(0.1d * en.getValue() + 0.5d);
            new BukkitRunnable() {
                @Override
                public void run() {
                    final Projectile np = e.getEntity().launchProjectile(prj.getClass(), vc);
                    if (np instanceof AbstractArrow) {
                        ((AbstractArrow) np).setPickupStatus(PickupStatus.CREATIVE_ONLY);
                        SM.projWeapons.put(prj.getEntityId(), bow);
                    }
                }
            }.runTaskLater(Main.main, 4);
        }
        store = true;
        break;
    case "reinstation":
        if (Main.srnd.nextInt(10) < en.getValue() && (!(prj instanceof AbstractArrow) || ((AbstractArrow) prj).getPickupStatus() == PickupStatus.ALLOWED)) {
            e.setConsumeItem(true);
            final ItemStack it = e.getConsumable().clone();
            if (prj instanceof AbstractArrow) {
                ((AbstractArrow) prj).setPickupStatus(PickupStatus.CREATIVE_ONLY);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (e.getEntity() instanceof HumanEntity) {
                        it.setAmount(1);
                        ((HumanEntity) e.getEntity()).getInventory().addItem(it);
                    }
                }
            }.runTaskLater(Main.main, 2);
        }
        store = true;
        break;
    case "curse_of_fragmentation":
        if (Main.srnd.nextInt(4) == 0) {
            final Damageable id = (Damageable) bow.getItemMeta();
            id.setDamage(id.getDamage() + 1);
            bow.setItemMeta(id);
        }
        store = true;
        break;
    case "curse_of_rotting":
        if (Main.srnd.nextInt(12) == 0) {
            e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1));
        }
        store = true;
        break;

    case "discharge":
        if (Main.srnd.nextInt(4) < en.getValue()) {
            w.createExplosion(prj, 0.2f * en.getValue() + 0.8f, false, false);
        }
        break;
    case "baloon":
        if (Main.srnd.nextInt(4) < en.getValue()) {
            targetLe.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20 * en.getValue(), 0));
            final FallingBlock fb = w.spawnFallingBlock(targetLe.getEyeLocation(), Material.CHORUS_FLOWER.createBlockData());
            fb.setHurtEntities(false);
            fb.setDropItem(false);
            targetLe.addPassenger(fb);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (fb != null && fb.isValid()) {
                        fb.remove();
                    }
                }
            }.runTaskLater(Main.main, 20 * en.getValue());
        }
        break;
    case "aquatic":
        switch (targetLe.getType()) {
            case ENDERMAN:
                prj.remove();
                targetLe.damage(vec.lengthSquared() * (0.1d * en.getValue() + 0.6d), shoter);
                break;
            case BLAZE, MAGMA_CUBE, ENDERMITE, PHANTOM:
                vec.multiply(0.4d * en.getValue() + 1d);
                w.spawnParticle(Particle.FALLING_DRIPSTONE_WATER, prj.getLocation(), 40, 0.4d, 0.4d, 0.4d);
                break;
            default:
                break;
        }
        break;
    case "curse_of_lagging":
        e.setCancelled(true);
        prj.remove();
        break;
    case "curse_of_the_dead":
        if (Main.srnd.nextInt(16) == 0) {
            final Location loc = prj.getLocation();
            w.spawnEntity(loc, Main.subServer.mobType, false);//, false);
        }
        break;
    case "flame":
        if (boom != 0) {
            w.createExplosion(prj.getLocation(), boom - 1, true, false, shoter);
        }
        break;
    default:
        break;

    case "aquatic":
        switch (target.getType()) {
            case ENDERMAN:
                target.damage(2d * en.getValue() + 6d, (LivingEntity) prj.getShooter());
                break;
            case BLAZE:
            case MAGMA_CUBE:
            case ENDERMITE:
            case PHANTOM:
                damage += 2 + 0.6d * en.getValue();
                prj.getWorld().spawnParticle(Particle.FALLING_DRIPSTONE_WATER, prj.getLocation(), 20, 0.2d, 0.2d, 0.2d);
                break;
            default:
                break;
        }
        break;
    case "vampirism":
        final LivingEntity le = (LivingEntity) prj.getShooter();
        Main.addHp(le, damage * 0.02d * en.getValue(), false);
        w.playSound(le.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.6f, 1.4f);
        w.spawnParticle(Particle.DAMAGE_INDICATOR, target.getEyeLocation(), 4);
        break;
    case "freeze":
        if (Main.srnd.nextInt(4) < en.getValue()) {
            final HashSet<BaseBlockPosition> ics = new HashSet<>();
            final BoundingBox bx = target.getBoundingBox();
            target.setAI(false);
            w.playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 2f, 0.8f);
            for (int x = (int) Math.floor(bx.getMinX()); x <= bx.getMaxX(); x++) {
                for (int y = (int) bx.getMinY(); y <= bx.getMaxY(); y++) {
                    for (int z = (int) Math.floor(bx.getMinZ()); z <= bx.getMaxZ(); z++) {
                        final Block b = w.getBlockAt(x, y, z);
                        if (b.getType().isAir()) {
                            b.setType(Material.PACKED_ICE);
                            ics.add(new BaseBlockPosition(x, y, z));
                        }
                    }
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    target.setAI(true);
                    for (final BaseBlockPosition l : ics) {
                        final Block bl = w.getBlockAt(l.u(), l.v(), l.w());
                        bl.breakNaturally();
                        bl.getWorld().spawnParticle(Particle.BLOCK_CRACK, bl.getLocation().add(0.5, 0.5, 0.5), 15, 0.6, 0.6, 0.6, SM.iceBlockData);
                    }
                }
            }.runTaskLater(Main.main, en.getValue() * 20);
        }
        break;
    case "antillager":
        if (target.getCategory() == EntityCategory.ILLAGER) {
            damage += 1.5d * en.getValue();
            w.spawnParticle(Particle.VILLAGER_ANGRY, target.getEyeLocation(), 2);
        }
        break;
    case "phantomic":
        if (target.getEyeLocation().getBlock().getLightLevel() < 4) {
            damage += 0.25d * en.getValue();
            w.playSound(target.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 2f, 0.4f);
        }
        break;
    case "curse_of_lagging":
        if (Main.srnd.nextInt(10) == 0) {
            target.damage(damage);
            damage = 0;
        }
        break;
    case "curse_of_the_dead":
        if (Main.srnd.nextInt(16) == 0) {
            target.getWorld().spawnEntity(target.getLocation(), Main.subServer.mobType, false);//, false);
        }
        break;

    case "vampirism":
        final double mx = damager.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        final double hl = damage * 0.02d * en.getValue();//e.getFinalDamage() * 0.1d * en.getValue();
        damager.setHealth(damager.getHealth() + hl > mx ? mx : damager.getHealth() + hl);
        w.playSound(dloc, Sound.ENTITY_WITCH_DRINK, 0.4f, 1.4f);
        w.spawnParticle(Particle.DAMAGE_INDICATOR, target.getEyeLocation(), 4);
        break;
    case "freeze":
        if (Main.srnd.nextInt(4) < en.getValue()) {
            //final HashSet<BaseBlockPosition> ics = new HashSet<>();
            final BoundingBox bx = target.getBoundingBox();
            target.setAI(false);
            w.playSound(tloc, Sound.ENTITY_PLAYER_HURT_FREEZE, 2f, 0.8f);
            final Cuboid cuboid = new Cuboid((int)bx.getWidthX()+1, (int)bx.getHeight()+1, (int)bx.getWidthZ()+1);
            cuboid.allign(target.getEyeLocation());
            for (final Block b : cuboid.getBlocks(target.getWorld())) {
                if (b.getType().isAir()) {
                    b.setType(Material.PACKED_ICE);
                }
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    target.setAI(true);
                    for (final Block b : cuboid.getBlocks(target.getWorld())) {
                        if (b.getType()==Material.PACKED_ICE) {
                            b.breakNaturally();
                            b.getWorld().spawnParticle(Particle.BLOCK_CRACK, b.getLocation().add(0.5, 0.5, 0.5), 15, 0.6, 0.6, 0.6, SM.iceBlockData);
                        }
                    }
                }
            }.runTaskLater(Main.main, en.getValue() * 20);
        }
        break;
    case "antillager":
        if (target.getCategory() == EntityCategory.ILLAGER) {
            damage += 1.5d * en.getValue();//e.setDamage(1.5d * en.getValue() + e.getDamage());
            w.spawnParticle(Particle.VILLAGER_ANGRY, target.getEyeLocation(), 2);
        }
        break;
    case "phantomic":
        if (EffectUtil.getLight(tloc.getBlock()) < 4) {
            damage += 0.25d * en.getValue();//e.setDamage(0.25d * en.getValue() + e.getDamage());
            w.playSound(dloc, Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 2f, 0.4f);
        }
        break;
    case "nimble":
        damager.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 30 - (5 * en.getValue()), en.getValue(), true, false, false));
        break;
    case "curse_of_lagging":
        if (Main.srnd.nextInt(10) == 0) {
            damage = 0;//e.setDamage(0);
        }
        break;
    case "curse_of_fragmentation":
        if (Main.srnd.nextInt(4) == 0) {
            final Damageable id = (Damageable) hand.getItemMeta();
            id.setDamage(id.getDamage() + 1);
            hand.setItemMeta(id);
        }
        break;
    case "curse_of_the_dead":
        if (Main.srnd.nextInt(16) == 0) {
            w.spawnEntity(damager.getLocation(), Main.subServer.mobType, false);
        }
        break;
    case "curse_of_rotting":
        if (Main.srnd.nextInt(12) == 0) {
            damager.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            damager.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1));
        }
        break;
    default:
        break;

    case "repultion":
        if (pls < entry.getValue() && Main.srnd.nextInt(10) < entry.getValue()) {
            ((HumanEntity) target).setShieldBlockingDelay(50);
            pls = entry.getValue().byteValue();
        }
        break;
    case "withered":
        if (entry.getValue() > wa) {
            wa = entry.getValue().byteValue();
        }
        if (Main.srnd.nextInt(4) < entry.getValue()) {
            wd += entry.getValue();
        }
        break;
    case "curse_of_lagging":
        lg = lg ? lg : Main.srnd.nextInt(10) == 0;
        break;
    case "curse_of_fragmentation":
        if (Main.srnd.nextInt(4) == 0) {
            final Damageable id = (Damageable) ofh.getItemMeta();
            id.setDamage(id.getDamage() + 1);
            ofh.setItemMeta(id);
        }
        break;
    case "curse_of_the_dead":
        if (Main.srnd.nextInt(16) == 0) {
            w.spawnEntity(tloc, Main.subServer.mobType, false);//, false);
        }
        break;
    case "curse_of_rotting":
        rt = rt ? rt : Main.srnd.nextInt(12) == 0;
        break;
    default:
        break;
    */
}
