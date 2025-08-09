package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
        
        plugin.broadcast(textComponent);

        return true;
    }
}
