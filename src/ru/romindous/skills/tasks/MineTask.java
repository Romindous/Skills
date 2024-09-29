package ru.romindous.skills.tasks;

public class MineTask /*implements Task*/ {/*

    private final Survivor sv;
    private final Mob npc;
    private final Material target;
    private final World w;
    private final Component[] msgs;
    private final BossBar timeBar;
    private final TaskType type;
    private final int encode;
    private final int maxTime;
    private int toMine;
    private int time;

    private static final Material[] mts = getMineStack();

    private static Material[] getMineStack() {
        switch (Main.subServer) {//unsafe
            case INFERNAL:
                return new Material[]{Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE, Material.GLOWSTONE};
            case LOCUS:
                return new Material[]{Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.DEEPSLATE_GOLD_ORE};
            case TERRA:
                return new Material[]{Material.IRON_ORE, Material.DIAMOND_ORE, Material.GLOWSTONE};
            case WASTES:
                return new Material[]{Material.COAL_ORE, Material.IRON_ORE, Material.COPPER_ORE};
            default:
                return null;
        }
    }

    public MineTask(final Survivor sv, final Mob npc, final Player p, final int ch12) {
        this.sv = sv;
        this.npc = npc;
        final Block b = npc.getLocation().getBlock();
        this.w = b.getWorld();
        this.type = TaskType.MINE;
        this.encode = ch12;
        this.maxTime = type.time;
        this.time = 0;
        this.target = Main.rndElmt(mts);
        this.toMine = (sv.level >> 1) + 2;
        p.showBossBar(this.timeBar = BossBar.bossBar(Component.text("§7Осталось добыть: §e" + toMine + " §7блок(ов)"), 1f, Color.YELLOW, Overlay.PROGRESS));
        npc.setAI(true);
        Bukkit.getMobGoals().removeAllGoals(npc);
        this.msgs = new Component[]{
            Component.text("§7Здарова, §eигрок§7, хочу предложить"),
            Component.text("§7тебе сделку. Выкопай мне §e" + toMine + " §7блоков"),
            Component.text("§7типа §e" + Main.nrmlzStr(target.toString()) + " §7за след."),
            Component.text("§7пару минут, и будешь §eвознагражден§7!")};
        npc.setCustomNameVisible(true);
        ScreenUtil.sendTitle(p, "", "§7Задача начата: §eДобыча");
    }

    @Override
    public void tryComplete() {
        final Player p = Bukkit.getPlayer(sv.nik);
        final Location loc = p.getLocation().add(0d, 1d, 0d);
        p.hideBossBar(timeBar);
        p.spawnParticle(Particle.VILLAGER_HAPPY, loc, 40, 0.6d, 1.4d, 0.6d);
        p.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 2f, 0.8f);
        ScreenUtil.sendTitle(p, "", "§7Задача выполнена: §eДобыча");
        sv.addXp(p, (int) (type.exp * Main.subServer.bfr * (1f + (sv.getStat(Stats.Харизма) * charFct))));
        end(null);
    }

    @Override
    public boolean end(final Player p) {
        if (p != null) {
            ScreenUtil.sendTitle(p, "", "§7Задача провалена: §eДобыча");
            p.hideBossBar(timeBar);
            p.sendMessage(Main.prefix + "§7Время вышло! Осталось выкопать: §c" + toMine);
        }
        npc.remove();
        sv.miniQuestTask = null; //SM.tasks.remove(sv);
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
            npc.customName(Component.text("§7Осталось §e" + rm + " §7секунд!"));
            timeBar.progress(Math.max((float) rm / maxTime, 0f));
            npc.getPathfinder().moveTo(Bukkit.getPlayer(sv.nik));
            switch (rm) {
                case 60:
                case 30:
                case 10:
                    ScreenUtil.sendTitle(Bukkit.getPlayer(sv.nik), "", "§7Осталось §e" + rm + " §7секунд!");
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

    public boolean checkBreak(final Player p, final Material mt) {
        if (mt == target) {
            if ((toMine--) > 1) {
                timeBar.name(Component.text("§7Осталось выкопать: §e" + toMine + " §7блок(ов)"));
                return true;
            }
            tryComplete();
            return true;
        }
        return false;
    }
*/}
