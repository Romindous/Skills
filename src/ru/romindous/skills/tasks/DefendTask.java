package ru.romindous.skills.tasks;

public class DefendTask /*implements Task*/ {/*

    public final Survivor sv;
    private final Mob npc;
    final World w;
    private final Component[] msgs;
    private final BossBar timeBar;
    private final XYZ[] spawns;
    private final EntityType[] mobs;
    private final TaskType type;
    private final int encode;
    private final int maxTime;
    private int time;

    private static final EntityType[] ents = getEntityStack();

    private static EntityType[] getEntityStack() {
        return switch (Main.subServer) {
            case INFERNAL -> new EntityType[]{EntityType.PIGLIN, EntityType.WITHER_SKELETON, EntityType.HOGLIN};
            case LOCUS -> new EntityType[]{EntityType.ZOMBIE_VILLAGER, EntityType.WITCH, EntityType.CAVE_SPIDER};
            case TERRA -> new EntityType[]{EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER};
            case WASTES -> new EntityType[]{EntityType.ZOGLIN, EntityType.HUSK, EntityType.CREEPER};
            default -> null;
        }; //unsafe
    }

    public DefendTask(final Survivor sv, final Mob npc, final Player p, final int encode) {
        this.sv = sv;
        this.npc = npc;
        final Block b = npc.getLocation().getBlock();
        this.w = b.getWorld();
        this.spawns = new XYZ[10];
        //Ostrov.async(() -> {
        int i = spawns.length - 1;
        while (i>=0) { //for (int i = spawns.length - 1; i >= 0; i--) {
            final Location spawn = LocationUtil.getHighestLoc(w, b.getX() + Main.getRndPlusMinusNum(6, 10), b.getZ() + Main.getRndPlusMinusNum(6, 10));
            if (spawn==null) {
                continue;
            }
            //spawns[i] = new XYZ( LocUtil.getHighestBlock(w.getBlockAt(b.getX() + Main.getRndPlusMinusNum(20), 250, b.getZ() + Main.getRndPlusMinusNum(20))) );
            spawns[i] = new XYZ(spawn);
            i--;
        }
        //}, 0);
        this.type = TaskType.DEFEND;
        this.encode = encode;
        this.mobs = ents;
        this.maxTime = type.time;
        this.time = 0;
        npc.setAI(true);
        Bukkit.getMobGoals().removeAllGoals(npc);
        p.showBossBar(this.timeBar = BossBar.bossBar(Component.text("§7Защищайте §bНПС §7от мобов"), 1f, Color.BLUE, Overlay.PROGRESS));
        this.msgs = new Component[]{
            Component.text("§7О, привет, §bстранник§7! Не поможешь?"),
            Component.text("§7Мне надо §bсрочно §7от сюда свалить,"),
            Component.text("§7но этот тп-заклятие §bпривлекает §7кучу"),
            Component.text("§7мобов, §bзащити §7меня на протяжении"),
            Component.text("§7след §b" + (maxTime / 60) + " §7мин, и будешь вознагражден!")};
        npc.setCustomNameVisible(true);
        ScreenUtil.sendTitle(p, "", "§7Задача начата: §bЗащита");
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
        p.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 40, 0.6d, 1.4d, 0.6d);
        p.playSound(loc, snd, 1f, 1f);
        ScreenUtil.sendTitle(p, "", "§7Задача выполнена: §bЗащита");
        sv.addXp(p, (int) (type.exp * Main.subServer.bfr * (1f + (sv.getStat(Stats.Харизма) * charFct))));
        end(null);
    }

    @Override
    public boolean end(final Player p) {
        if (p != null) {
            ScreenUtil.sendTitle(p, "", "§7Задача провалена: §bЗащита");
            p.hideBossBar(timeBar);
            p.sendMessage(Main.prefix + "§7Вам не удалось §bзащитить §7НПС!");
        }
        npc.remove();
        sv.miniQuestTask = null; //SM.tasks.remove(sv);
        Task.occupied.remove(encode);
        return false;
    }

    @Override
    public boolean secondTick() {
        if ((time++) > maxTime) {
            tryComplete();
            return true;
        }

        if (SM.getSurvivor(sv.nik) == null) {
            end(null);
            return true;
        }

        if (npc == null || !npc.isValid()) {
            end(Bukkit.getPlayer(sv.nik));
            return true;
        }

        if ((time & 7) == 0) {
            for (int i = Math.min(time >> 4, 8); i >= 0; i--) {
                final XYZ loc = spawns[i];
                final Mob mb = (Mob) w.spawnEntity(new Location(w, loc.x + 0.5d, loc.y + 0.1d, loc.z + 0.5d), Main.rndElmt(mobs), false);//, false);
                mb.setTarget(npc);
            }
        }

        if (time > msgs.length) {
            final int rm = maxTime - time;
            npc.customName(Component.text("§7Осталось §b" + rm + " §7секунд!"));
            npc.getPathfinder().moveTo(Bukkit.getPlayer(sv.nik));
            timeBar.progress(Math.max((float) rm / maxTime, 0f));
            w.spawnParticle(Particle.PORTAL, npc.getLocation().add(0d, 1d, 0d), time >> 1, 0.4d, 1.2d, 0.4d);
            switch (rm) {
                case 60:
                case 30:
                case 10:
                    ScreenUtil.sendTitle(Bukkit.getPlayer(sv.nik), "", "§7Осталось §b" + rm + " §7секунд!");
                    break;
                default:
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
