package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.commands.api.BaseCommand;
import com.wem.geezer.management.BackupManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BackupCommand extends BaseCommand {

    private final Geezer plugin;

    public BackupCommand(Geezer plugin) {
        super("backup");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("geezer.backup")) {
            plugin.sendMessage(sender, Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        BackupManager backupManager = plugin.getBackupManager();
        if (backupManager.startManualBackup()) {
            plugin.sendMessage(sender, Component.text("Starting manual backup. This will run in the background.", NamedTextColor.GRAY));
        } else {
            plugin.sendMessage(sender, Component.text("Could not start backup. A backup is already in progress.", NamedTextColor.RED));
        }

        return true;
    }
}