package ru.romindous.skills.tasks;

public class KillTask /*implements Task*/ {/*

    private final Survivor sv;
    private final Mob npc;
    private final EntityType target;
    private final World w;
    private final BossBar timeBar;
    private final Component[] msgs;
    private final TaskType type;
    private final int encode;
    private final int maxTime;
    private int toKill;
    private int time;

    public static EntityType target = EntityType.ZOMBIE_VILLAGER;

    public KillTask(final Survivor sv, final Mob npc, final Player p, final int encode) {
        this.sv = sv;
        this.npc = npc;
        final Block b = npc.getLocation().getBlock();
        this.w = b.getWorld();
        this.type = TaskType.KILL;
        this.encode = encode;
        this.maxTime = type.time;
        this.time = 0;
        this.target = target;
        this.toKill = (sv.level >> 1) + 4;
        p.showBossBar(this.timeBar = BossBar.bossBar(Component.text("§7Осталось убить: §c" + toKill + " §7моб(ов)"), 1f, Color.RED, Overlay.PROGRESS));
        npc.setAI(true);
        Bukkit.getMobGoals().removeAllGoals(npc);
        this.msgs = new Component[]{
            Component.text("§7Опа, привет §cдруг§7, у меня к тебе"),
            Component.text("§7есть поручение. Убей около §c" + toKill + " §7мобов"),
            Component.text("§7типа §c" + Main.nrmlzStr(target.toString()) + " §7за след."),
            Component.text("§7пару минут, и будешь §cвознагражден§7!")};
        npc.setCustomNameVisible(true);
        ScreenUtil.sendTitle(p, "", "§7Задача начата: §cУбийство");
    }

    @Override
    public void tryComplete() {
        final Player p = Bukkit.getPlayer(sv.nik);
        final Location loc = p.getLocation().add(0d, 1d, 0d);
        p.hideBossBar(timeBar);
        p.spawnParticle(Particle.VILLAGER_HAPPY, loc, 40, 0.6d, 1.4d, 0.6d);
        p.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 2f, 0.8f);
        ScreenUtil.sendTitle(p, "", "§7Задача выполнена: §cУбийство");
        p.hideBossBar(timeBar);
        sv.addXp(p, (int) (type.exp * Main.subServer.bfr * (1f + (sv.getStat(Stats.Харизма) * charFct))));
        end(null);
    }

    @Override
    public boolean end(final Player p) {
        if (p != null) {
            ScreenUtil.sendTitle(p, "", "§7Задача провалена: §cУбийство");
            p.hideBossBar(timeBar);
            p.sendMessage(Main.prefix + "§7Время вышло! Осталось убийств: §c" + toKill);
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
            npc.customName(Component.text("§7Осталось §c" + rm + " §7секунд!"));
            timeBar.progress(Math.max((float) rm / maxTime, 0f));
            npc.getPathfinder().moveTo(Bukkit.getPlayer(sv.nik));
            switch (rm) {
                case 60:
                case 30:
                case 10:
                    ScreenUtil.sendTitle(Bukkit.getPlayer(sv.nik), "", "§7Осталось §c" + rm + " §7секунд!");
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

    public boolean checkKill(final Player p, final EntityType et) {
        if (et == target) {
            if ((toKill--) > 1) {
                timeBar.name(Component.text("§7Осталось убить: §c" + toKill + " §7моб(ов)"));
                return true;
            }
            tryComplete();
            return true;
        }
        return false;
    }
*/}
