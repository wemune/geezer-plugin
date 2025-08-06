package com.wem.geezer.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

public class Logger {

    private static final String PREFIX = "&b[Geezer] ";

    public static void info(String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(PREFIX + "&f[INFO] " + message);
        Bukkit.getConsoleSender().sendMessage(component);
    }

    public static void warn(String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(PREFIX + "&e[WARN] " + message);
        Bukkit.getConsoleSender().sendMessage(component);
    }

    public static void severe(String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(PREFIX + "&c[SEVERE] " + message);
        Bukkit.getConsoleSender().sendMessage(component);
    }
}