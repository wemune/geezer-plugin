package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.commands.api.BaseCommand;
import com.wem.geezer.database.PlayerStats;
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

public class PlaytimeCommand extends BaseCommand {

    private final Geezer plugin;

    public PlaytimeCommand(Geezer plugin) {
        super("playtime");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.sendMessage(sender, Component.text("Please specify a player's name.", NamedTextColor.RED));
                return true;
            }
            displayPlaytime(sender, (Player) sender);
        } else {
            String targetName = args[0];
            Player target = Bukkit.getPlayerExact(targetName);
            if (target != null) {
                displayPlaytime(sender, target);
            } else {
                displayOfflinePlaytime(sender, targetName);
            }
        }
        return true;
    }

    private void displayPlaytime(CommandSender sender, Player target) {
        PlayerStats stats = plugin.getStatsManager().getStats(target.getUniqueId());
        if (stats == null) {
            plugin.sendMessage(sender, Component.text("Could not find stats for " + target.getName() + ".", NamedTextColor.RED));
            return;
        }

        long savedPlaytime = stats.getPlayTimeSeconds();
        Long joinTime = plugin.getJoinTimes().get(target.getUniqueId());
        long currentSessionPlaytime = 0;
        if (joinTime != null) {
            currentSessionPlaytime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - joinTime);
        }

        long totalPlaytime = savedPlaytime + currentSessionPlaytime;
        String namePrefix = (sender == target) ? "Your" : target.getName() + "'s";
        Component message = LegacyComponentSerializer.legacySection().deserialize("§7" + namePrefix + " total playtime is: §f" + formatTime(totalPlaytime));
        plugin.sendMessage(sender, message);
    }

    private void displayOfflinePlaytime(CommandSender sender, String targetName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
            plugin.getOfflinePlayerStats(targetPlayer.getUniqueId()).thenAccept(stats -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (stats == null) {
                        plugin.sendMessage(sender, Component.text("Could not find playtime data for '" + targetName + "'. They may have never played.", NamedTextColor.RED));
                        return;
                    }
                    String namePrefix = targetPlayer.getName() + "'s";
                    Component message = LegacyComponentSerializer.legacySection().deserialize("§7" + namePrefix + " total playtime is: §f" + formatTime(stats.getPlayTimeSeconds()));
                    plugin.sendMessage(sender, message);
                });
            });
        });
    }

    private String formatTime(long totalSeconds) {
        if (totalSeconds < 60) {
            return totalSeconds + " seconds";
        }

        long totalHours = TimeUnit.SECONDS.toHours(totalSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (totalHours > 0) {
            sb.append(totalHours).append("h ");
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