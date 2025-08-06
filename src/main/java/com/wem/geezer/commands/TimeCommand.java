package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TimeCommand implements CommandExecutor {

    private final Geezer plugin;

    public TimeCommand(Geezer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();
        long ticks = world.getTime();

        long hours = ((ticks / 1000) + 6) % 24;
        long minutes = (ticks % 1000) * 60 / 1000;
        String ampm = hours >= 12 ? "PM" : "AM";
        long displayHours = hours % 12;
        if (displayHours == 0) {
            displayHours = 12;
        }
        String timeString = String.format("%02d:%02d %s", displayHours, minutes, ampm);

        String event;
        long ticksUntilEvent;
        if (ticks >= 0 && ticks < 12000) { // Day time
            event = "Sunset";
            ticksUntilEvent = 12000 - ticks;
        } else { // Night time
            event = "Sunrise";
            ticksUntilEvent = (24000 - ticks);
        }

        long realSecondsUntilEvent = ticksUntilEvent / 20;
        long realMinutes = realSecondsUntilEvent / 60;
        long realSeconds = realSecondsUntilEvent % 60;
        String timeUntilEventString = String.format("%dm %ds", realMinutes, realSeconds);

        Component timeMessage = LegacyComponentSerializer.legacySection().deserialize("§7Server time: §f" + timeString);
        Component eventMessage = LegacyComponentSerializer.legacySection().deserialize("§7" + event + " in: §f" + timeUntilEventString);

        plugin.sendMessage(player, timeMessage);
        plugin.sendMessage(player, eventMessage);

        return true;
    }
}