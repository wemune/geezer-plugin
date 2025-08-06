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

public class MsgCommand implements CommandExecutor {

    private final Geezer plugin;

    public MsgCommand(Geezer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            plugin.sendMessage(sender, Component.text("Usage: /msg <player> <message>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null || !target.isOnline()) {
            plugin.sendMessage(sender, Component.text("Player '" + args[0] + "' not found or is not online.", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        if (player.equals(target)) {
            plugin.sendMessage(sender, Component.text("You cannot message yourself.", NamedTextColor.RED));
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
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
