package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ReplyCommand implements CommandExecutor {

    private final Geezer plugin;

    public ReplyCommand(Geezer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            plugin.sendMessage(sender, Component.text("Usage: /r <message>", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        UUID targetUUID = plugin.getLastMessageSender().get(player.getUniqueId());

        if (targetUUID == null) {
            plugin.sendMessage(sender, Component.text("You have no one to reply to.", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(targetUUID);

        if (target == null || !target.isOnline()) {
            plugin.sendMessage(sender, Component.text("The player you are replying to is no longer online.", NamedTextColor.RED));
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (String arg : args) {
            messageBuilder.append(arg).append(" ");
        }
        String message = messageBuilder.toString().trim();

        Component toSender = LegacyComponentSerializer.legacySection().deserialize(String.format("§7[§fYou §b-> §f%s§7] §f%s", target.getName(), message));
        Component toTarget = LegacyComponentSerializer.legacySection().deserialize(String.format("§7[§f%s §b-> §fYou§7] §f%s", player.getName(), message));

        plugin.sendMessage(player, toSender);
        plugin.sendMessage(target, toTarget);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);

        plugin.getLastMessageSender().put(target.getUniqueId(), player.getUniqueId());
        plugin.getLastMessageSender().put(player.getUniqueId(), target.getUniqueId());

        Logger.info(String.format("[PM] %s -> %s: %s", player.getName(), target.getName(), message));

        return true;
    }
}
