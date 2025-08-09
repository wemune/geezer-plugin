package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class BroadcastCommand implements CommandExecutor {

    private final Geezer plugin;

    public BroadcastCommand(Geezer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("geezer.broadcast")) {
            plugin.sendMessage(sender, Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            plugin.sendMessage(sender, Component.text("Usage: /broadcast <message>", NamedTextColor.RED));
            return true;
        }

        String message = String.join(" ", args);
        Component textComponent = Component.text(message, NamedTextColor.RED, TextDecoration.BOLD);
        Component broadcastMessage = Geezer.PREFIX.append(textComponent);

        plugin.getServer().broadcast(broadcastMessage);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        }

        new BukkitRunnable() {
            private int count = 0;
            private final int durationInSeconds = 5;

            @Override
            public void run() {
                if (count >= durationInSeconds) {
                    this.cancel();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(textComponent);
                }
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }
}
