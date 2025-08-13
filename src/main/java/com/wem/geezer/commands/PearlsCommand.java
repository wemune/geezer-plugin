package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.commands.api.BaseCommand;
import com.wem.geezer.management.PearlManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PearlsCommand extends BaseCommand {

    private final Geezer plugin;

    public PearlsCommand(Geezer plugin) {
        super("pearls");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        PearlManager pearlManager = plugin.getPearlManager();

        if (pearlManager.isPearlPickupDisabled(player)) {
            pearlManager.enablePearlPickup(player);
            plugin.sendMessage(player, Component.text("You will now pick up ender pearls.", NamedTextColor.GRAY));
        } else {
            pearlManager.disablePearlPickup(player);
            plugin.sendMessage(player, Component.text("You will no longer pick up ender pearls.", NamedTextColor.GRAY));
        }

        return true;
    }
}