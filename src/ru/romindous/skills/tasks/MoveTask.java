package ru.romindous.skills.tasks;

public class MoveTask /*implements Task*/ {/*

    private static final NamedTextColor clr = NamedTextColor.GREEN;

    private final Survivor sv;
    public final Mob npc;
    private final Shulker mark;
    private final XYZ target;
    private final World w;
    private final Component[] msgs;
    private final BossBar timeBar;
    private final TaskType type = TaskType.MOVE;
    private final int encode;
    private final int maxTime;
    private int time;
    private final Cuboid cuboid;

    public MoveTask(final Survivor sv, final Mob npc, final Player p, final int encode) {
        this.sv = sv;
        this.npc = npc;
        this.encode = encode;
        w = npc.getWorld();
        maxTime = type.time;
        
        //final Block b = npc.getLocation().getBlock();
        //final Location targetLoc = LocUtil.getHighestBlock(b.getRelative((int) ((Main.getRndPlusMinusNum(6 * sv.level) + 10) * Main.subServer.bfr), b.getWorld().getMaxHeight(), (int) ((Main.getRndPlusMinusNum(6 * sv.level) + 10) * Main.subServer.bfr)));
        final int xWay = npc.getLocation().getBlockX() + (int) ((Main.getRndPlusMinusNum(8, 6 * sv.level)) * Main.subServer.bfr);
        final int zWay = npc.getLocation().getBlockZ() + (int) ((Main.getRndPlusMinusNum(8, 6 * sv.level)) * Main.subServer.bfr);
        final Location targetLoc = LocationUtil.getHighestLoc(npc.getWorld(), xWay, zWay);
        target = new XYZ(targetLoc);
        //cuboid = new Cuboid( new Location(w, target.x - 2, target.y - 2, target.z - 2), new Location(w, target.x + 2, target.y + 2, target.z + 2) );
        cuboid = new Cuboid( 4, 4, 4 ); //создать кубоид, точка спавна будет в центре
        cuboid.allign(targetLoc); //переместить кубоид на локацию с совмещением спавна
        targetLoc.getBlock().setType(Material.REINFORCED_DEEPSLATE, false);//w.getBlockAt(target.x, target.y, target.z).setType(Material.REINFORCED_DEEPSLATE, false);
        targetLoc.getBlock().getRelative(BlockFace.UP).setType(Material.SCULK_SHRIEKER, false);//w.getBlockAt(target.x, target.y + 1, target.z).setType(Material.SCULK_SHRIEKER, false);
        
        mark = w.spawn(targetLoc, Shulker.class, SpawnReason.CUSTOM);//w.spawn(new Location(w, target.x + 0.5d, target.y - 0.5d, target.z + 0.5d), Shulker.class);
        mark.setAI(false);
        mark.setRemoveWhenFarAway(false);
        mark.setInvisible(true);
        mark.setInvulnerable(true);
        mark.setGlowing(true);
        mark.setGravity(false);
        
        final Scoreboard sb = p.getScoreboard();
        final Team tm = sb.getTeam("move") == null ? sb.registerNewTeam("move") : sb.getTeam("move");
        tm.addEntry(mark.getUniqueId().toString());
        tm.color(clr);
        
        npc.setAI(true);
        Bukkit.getMobGoals().removeAllGoals(npc);
        npc.getPathfinder().moveTo(p);
        msgs = new Component[]{
            Component.text("§7Здрасть, §aпутник§7! Помоги-ка мне"),
            Component.text("§7добратся на координаты (§a" + target.x + "§7, §a" + target.y + "§7, §a" + target.z + "§7)."),
            Component.text("§7У меня там телепортер стоит, дойдем"),
            Component.text("§7вовремя - получишь свою долю награды!")
        };
        npc.setCustomNameVisible(true);
        npc.setLeashHolder(p);
        
        ScreenUtil.sendTitle(p, "", "§7Задача начата: §aДоставка");
        p.showBossBar(timeBar = BossBar.bossBar(Component.text("§7Проведите §aНПС §7на (§a" + target.x + "§7, §a" + target.y + "§7, §a" + target.z + "§7)!"), 1f, Color.YELLOW, Overlay.PROGRESS));
        //p.sendMessage(Main.prefix + "Доставьте моба на (§a" + target.x + "§7,§a " + target.y + "§7,§a " + target.z + "§7)");
    }

    @Override
    public void tryComplete() {
        final Location loc = npc.getLocation().add(0d, 1d, 0d);
        final Sound snd;
        switch (npc.getType()) {
            case PILLAGER:
                snd = Sound.ENTITY_VINDICATOR_CELEBRATE;
                break;
            case PIGLIN:
            case PIGLIN_BRUTE:
                snd = Sound.ENTITY_PIGLIN_RETREAT;
                break;
            default:
            case VILLAGER:
                snd = Sound.ENTITY_VILLAGER_CELEBRATE;
                break;
        }
        final Player p = Bukkit.getPlayer(sv.nik);
        p.hideBossBar(timeBar);
        p.spawnParticle(Particle.SOUL, loc, 40, 0.4d, 1.2d, 0.4d);
        p.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 40, 0.6d, 1.4d, 0.6d);
        p.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 2f, 2f);
        p.playSound(loc, snd, 1f, 1f);
        ScreenUtil.sendTitle(p, "", "§7Задача выполнена: §bДоставка");
        sv.addXp(p, (int) (type.exp * Main.subServer.bfr * (1f + (sv.getStat(Stats.Харизма) * charFct))));
        end(null);
    }

    @Override
    public boolean end(final Player p) {
        if (p != null) {
            ScreenUtil.sendTitle(p, "", "§cЗадача провалена: §bДоставка");
            p.hideBossBar(timeBar);
            p.sendMessage(Main.prefix + "§7Время вышло, Вы не доставили §сНПС §7вовремя!");
        }
        w.getBlockAt(target.x, target.y, target.z).setType(Material.AIR);
        w.getBlockAt(target.x, target.y + 1, target.z).setType(Material.AIR);
        npc.remove();
        mark.remove();
        Task.occupied.remove(encode);
        return false;
    }

    @Override
    public boolean secondTick() {

        if (SM.getSurvivor(sv.nik) == null) {
            end(null);
            return true;
        }

        if ((time++) > maxTime || npc == null || !npc.isValid()) {
            end(Bukkit.getPlayer(sv.nik));
            return true;
        }

        if (time > msgs.length) {
            final int rm = maxTime - time;
            if (npc.getLocation().subtract(target.x, target.y, target.z).lengthSquared() < 4d) {
                tryComplete();
                return true;
            }
            npc.getPathfinder().moveTo(Bukkit.getPlayer(sv.nik));
            npc.customName(Component.text("§7Осталось §a" + rm + " §7секунд!"));
            timeBar.progress(Math.max((float) rm / maxTime, 0f));
            switch (rm) {
                case 60:
                case 30:
                case 10:
                    ScreenUtil.sendTitle(Bukkit.getPlayer(sv.nik), "", "§7Осталось §a" + rm + " §7секунд!");
                    break;
            }
        } else {
            w.playSound(npc.getLocation(), Task.getTalkSound(npc.getType()), 1f, 1f);
            Bukkit.getPlayer(sv.nik).sendMessage(msgs[time - 1]);
            npc.customName(msgs[time - 1]);
        }

        return false;
    }

    @Override
    public TaskType getType() {
        return type;
    }

    @Override
    public Survivor getSurv() {
        return sv;
    }

    @Override
    public LivingEntity getNpc() {
        return npc;
    }
*/}
