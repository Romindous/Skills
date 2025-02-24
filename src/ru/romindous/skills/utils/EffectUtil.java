package ru.romindous.skills.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import ru.romindous.skills.Main;

public class EffectUtil {

    public static void partCircle(final Location loc, final double rds, final int amt, final Particle type) {
        final float prt = 360f / (float) amt;
        for (byte i = 0; i < amt; i++) {
            loc.getWorld().spawnParticle(type, loc.getX() + (rds * Math.sin(Math.toRadians(prt * i))), loc.getY(), loc.getZ() + (rds * Math.cos(Math.toRadians(prt * i))), 2);
        }
    }

    public static int getLight(final Block b) {
        return switch (Main.subServer) {
            case WASTES -> Math.max(b.getLightFromBlocks(), b.getLightFromSky());
            case INFERNAL -> Math.max(b.getLightFromBlocks(), 4);
            case KALEUM -> Math.max(b.getLightFromBlocks(), 2);
            case KRIOLITE -> b.getLightFromBlocks();
            default -> Math.max(b.getLightFromBlocks(), b.getWorld().getTime() > 13000
                ? Math.max(0, b.getLightFromSky() - 8) : b.getLightFromSky());
        };
    }

    public static long getTime(final World w) {
        return switch (Main.subServer) {
            case KALEUM, KRIOLITE -> 18000;
            case WASTES, INFERNAL -> 6000;
            default -> w.getTime();
        };
    }

}
