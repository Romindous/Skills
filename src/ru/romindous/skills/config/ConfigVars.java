package ru.romindous.skills.config;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import ru.komiss77.OConfig;
import ru.romindous.skills.Main;

public class ConfigVars {

    private static final String conDir = "mobs/vars.yml";

    private static final Map<String, Double> vars = new HashMap<>();

    public static void load() {
        loadKeys(Main.configManager.getNewConfig(conDir).getConfigurationSection(""));
    }

    private static void loadKeys(final ConfigurationSection cs) {
        for (final String key : cs.getKeys(false)) {
            final ConfigurationSection child = cs.getConfigurationSection(key);
            if (child == null) vars.put(cs.getName() + "." + key, cs.getDouble(key));
            else loadKeys(child);
        }
    }

    public static int get(final String id, final int value) {
        final Double d = vars.get(id);
        if (d == null) {
            vars.put(id, (double) value);
            final OConfig config = Main.configManager.getNewConfig(conDir);
            config.set(id, (double) value);
            config.saveConfig();
            return value;
        }
        return d.intValue();
    }

    public static double get(final String id, final double value) {
        final Double f = vars.get(id);
        if (f == null) {
            vars.put(id, value);
            final OConfig config = Main.configManager.getNewConfig(conDir);
            config.set(id, value);
            config.saveConfig();
            return value;
        }
        return f;
    }
}