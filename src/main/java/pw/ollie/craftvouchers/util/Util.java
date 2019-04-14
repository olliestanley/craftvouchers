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

    public static String[] subArray(String[] array, int startIndex, int endIndex) {
        String[] newArray = new String[endIndex - startIndex];
        System.arraycopy(array, startIndex, newArray, 0, newArray.length);
        return newArray;
    }

    private Util() {
        throw new UnsupportedOperationException();
    }
}
