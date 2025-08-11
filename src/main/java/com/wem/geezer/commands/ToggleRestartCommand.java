package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.commands.api.BaseCommand;
import com.wem.geezer.management.RestartManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ToggleRestartCommand extends BaseCommand {

    private final Geezer plugin;
    private final RestartManager restartManager;

    public ToggleRestartCommand(Geezer plugin) {
        super("togglerestart");
        this.plugin = plugin;
        this.restartManager = plugin.getRestartManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("geezer.togglerestart")) {
            Component permissionMessage = Component.text("You do not have permission to use this command.", NamedTextColor.RED);
            plugin.sendMessage(sender, permissionMessage);
            return true;
        }

        if (restartManager.isRestartScheduled()) {
            if (restartManager.cancelRestart()) {
                Component successMessage = Component.text("The scheduled server restart has been DISABLED.", NamedTextColor.RED, TextDecoration.BOLD);
                plugin.broadcast(successMessage);
            }
        } else {
            restartManager.scheduleRestart();
            Component successMessage = Component.text("The scheduled server restart has been ENABLED.", NamedTextColor.RED, TextDecoration.BOLD);
            plugin.broadcast(successMessage);
        }

        return true;
    }
}