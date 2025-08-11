package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.commands.api.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PingCommand extends BaseCommand {

    private final Geezer plugin;

    public PingCommand(Geezer plugin) {
        super("ping");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.sendMessage(sender, Component.text("Please specify a player's name to check their ping.", NamedTextColor.RED));
                return true;
            }
            Player player = (Player) sender;
            Component message = LegacyComponentSerializer.legacySection().deserialize("§7Your ping is: §f" + player.getPing() + "ms");
            plugin.sendMessage(sender, message);
        } else {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                plugin.sendMessage(sender, Component.text("Player '" + args[0] + "' is not online.", NamedTextColor.RED));
                return true;
            }
            Component message = LegacyComponentSerializer.legacySection().deserialize("§7" + target.getName() + "'s ping is: §f" + target.getPing() + "ms");
            plugin.sendMessage(sender, message);
        }
        return true;
    }
}