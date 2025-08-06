package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.management.BackupManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BackupCommand implements CommandExecutor {

    private final Geezer plugin;

    public BackupCommand(Geezer plugin) {
        this.plugin = plugin;
    }

        @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
