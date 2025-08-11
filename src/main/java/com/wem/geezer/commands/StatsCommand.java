package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.database.PlayerStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class StatsCommand implements CommandExecutor {

    private final Geezer plugin;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public StatsCommand(Geezer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.sendMessage(sender, Component.text("Please specify a player's name.", NamedTextColor.RED));
                return true;
            }
            Player player = (Player) sender;
            PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());
            displayStats(sender, stats, player, "Your");
        } else {
            String targetName = args[0];
            Player target = Bukkit.getPlayerExact(targetName);
            if (target != null) {
                PlayerStats stats = plugin.getStatsManager().getStats(target.getUniqueId());
                displayStats(sender, stats, target, target.getName() + "'s");
            } else {
                displayOfflineStats(sender, targetName);
            }
        }
        return true;
    }

    private void displayOfflineStats(CommandSender sender, String targetName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
            plugin.getOfflinePlayerStats(targetPlayer.getUniqueId()).thenAccept(stats -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (stats == null) {
                        plugin.sendMessage(sender, Component.text("Could not find stats for '" + targetName + "'. They may have never played.", NamedTextColor.RED));
                        return;
                    }
                    displayStats(sender, stats, targetPlayer, targetPlayer.getName() + "'s");
                });
            });
        });
    }

    private void displayStats(CommandSender sender, PlayerStats stats, OfflinePlayer player, String namePrefix) {
        if (stats == null) {
            plugin.sendMessage(sender, Component.text("No stats found for this player.", NamedTextColor.RED));
            return;
        }

        long cm = 0;
        cm += player.getStatistic(Statistic.WALK_ONE_CM);
        cm += player.getStatistic(Statistic.SPRINT_ONE_CM);
        cm += player.getStatistic(Statistic.CROUCH_ONE_CM);
        cm += player.getStatistic(Statistic.SWIM_ONE_CM);
        cm += player.getStatistic(Statistic.FALL_ONE_CM);
        cm += player.getStatistic(Statistic.FLY_ONE_CM);
        cm += player.getStatistic(Statistic.AVIATE_ONE_CM);
        cm += player.getStatistic(Statistic.BOAT_ONE_CM);
        cm += player.getStatistic(Statistic.MINECART_ONE_CM);
        cm += player.getStatistic(Statistic.HORSE_ONE_CM);
        cm += player.getStatistic(Statistic.PIG_ONE_CM);
        cm += player.getStatistic(Statistic.STRIDER_ONE_CM);
        double km = cm / 100000.0;

        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§3--- " + namePrefix + " Stats ---"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§7Mobs Killed: §f" + stats.getMobsKilled()));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§7Deaths: §f" + stats.getDeaths()));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§7Distance Traveled: §f" + df.format(km) + " km"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§3--- Ores Mined ---"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§bDiamonds: §f" + stats.getDiamondOreMined()));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§4Ancient Debris: §f" + stats.getAncientDebrisMined()));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§6Gold: §f" + stats.getGoldOreMined()));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§eIron: §f" + stats.getIronOreMined()));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§aEmeralds: §f" + stats.getEmeraldOreMined()));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§9Lapis: §f" + stats.getLapisOreMined()));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§cRedstone: §f" + stats.getRedstoneOreMined()));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§8Coal: §f" + stats.getCoalOreMined()));
    }
}