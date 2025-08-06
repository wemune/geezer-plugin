package com.wem.geezer.util;

import org.bukkit.Bukkit;

public class Logger {

    private static final String PREFIX = "[Geezer] ";
    private static final java.util.logging.Logger BUKKIT_LOGGER = Bukkit.getLogger();

    public static void info(String message) {
        BUKKIT_LOGGER.info(PREFIX + message);
    }

    public static void warn(String message) {
        BUKKIT_LOGGER.warning(PREFIX + message);
    }

    public static void severe(String message) {
        BUKKIT_LOGGER.severe(PREFIX + message);
    }
}
