package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

public class UptimeCommand implements CommandExecutor {

    private final Geezer plugin;

    public UptimeCommand(Geezer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        Component message = LegacyComponentSerializer.legacySection().deserialize("§7Server uptime: §f" + formatTime(uptimeMillis));
        plugin.sendMessage(sender, message);
        return true;
    }

    private String formatTime(long totalMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(totalMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(totalMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalMillis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString().trim();
    }
}
