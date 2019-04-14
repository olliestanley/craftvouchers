package pw.ollie.craftvouchers.util;

import org.bukkit.configuration.ConfigurationSection;

public final class Util {
    public static ConfigurationSection getOrCreateSection(ConfigurationSection parent, String name) {
        ConfigurationSection result = parent.getConfigurationSection(name);
        if (result == null) {
            result = parent.createSection(name);
        }
        return result;
    }

    private Util() {
        throw new UnsupportedOperationException();
    }
}
