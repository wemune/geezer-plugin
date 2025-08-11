package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.commands.api.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class SeenCommand extends BaseCommand {

    private final Geezer plugin;

    public SeenCommand(Geezer plugin) {
        super("seen");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            plugin.sendMessage(sender, Component.text("Please specify a player's name.", NamedTextColor.RED));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);

        if (target != null) {
            Component message;
            if (sender instanceof Player && ((Player) sender).getName().equalsIgnoreCase(target.getName())) {
                message = Component.text("You are currently online.", NamedTextColor.GREEN);
            } else {
                message = Component.text(target.getName() + " is currently online.", NamedTextColor.GREEN);
            }
            plugin.sendMessage(sender, message);
            return true;
        }

        displayOfflineSeen(sender, targetName);
        return true;
    }

    private void displayOfflineSeen(CommandSender sender, String targetName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);

            plugin.getOfflinePlayerStats(targetPlayer.getUniqueId()).thenAccept(stats -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (stats == null || stats.getLastSeen() == null) {
                        plugin.sendMessage(sender, Component.text("Could not find any data for '" + targetName + "'. They may have never played.", NamedTextColor.RED));
                        return;
                    }
                    long timeSinceSeenMillis = System.currentTimeMillis() - stats.getLastSeen().getTime();
                    String formattedTime = formatTime(timeSinceSeenMillis) + " ago.";

                    Component message;
                    if (sender instanceof Player && ((Player) sender).getName().equalsIgnoreCase(targetPlayer.getName())) {
                        message = LegacyComponentSerializer.legacySection().deserialize("§7You were last seen: §f" + formattedTime);
                    } else {
                        message = LegacyComponentSerializer.legacySection().deserialize("§7" + targetPlayer.getName() + " was last seen: §f" + formattedTime);
                    }
                    plugin.sendMessage(sender, message);
                });
            });
        });
    }

    private String formatTime(long totalMillis) {
        if (totalMillis < 0) {
            return "just now";
        }

        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(totalMillis);
        if (totalSeconds < 60) {
            return totalSeconds + "s";
        }

        long days = TimeUnit.MILLISECONDS.toDays(totalMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(totalMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || sb.length() == 0) {
            sb.append(minutes).append("m");
        }

        return sb.toString().trim();
    }
}
