package com.wem.geezer.listeners;

import com.wem.geezer.Geezer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldListener implements Listener {

    public WorldListener() {
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String fromWorldName = event.getFrom().getName();
        World toWorld = player.getWorld();
        String toWorldName = toWorld.getName();

        Component youMessage = Component.text()
                .append(Component.text("You", NamedTextColor.WHITE))
                .append(Component.text(" have traveled from ", NamedTextColor.GRAY))
                .append(Component.text(fromWorldName, getWorldColor(event.getFrom().getEnvironment())))
                .append(Component.text(" to ", NamedTextColor.GRAY))
                .append(Component.text(toWorldName, getWorldColor(toWorld.getEnvironment())))
                .build();
        player.sendMessage(Geezer.PREFIX.append(youMessage));

        Component broadcastMessage = Component.text()
                .append(player.name().color(NamedTextColor.WHITE))
                .append(Component.text(" has traveled from ", NamedTextColor.GRAY))
                .append(Component.text(fromWorldName, getWorldColor(event.getFrom().getEnvironment())))
                .append(Component.text(" to ", NamedTextColor.GRAY))
                .append(Component.text(toWorldName, getWorldColor(toWorld.getEnvironment())))
                .build();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.sendMessage(Geezer.PREFIX.append(broadcastMessage));
            }
        }
    }

    private NamedTextColor getWorldColor(World.Environment environment) {
        switch (environment) {
            case NORMAL:
                return NamedTextColor.GREEN;
            case NETHER:
                return NamedTextColor.RED;
            case THE_END:
                return NamedTextColor.LIGHT_PURPLE;
            default:
                return NamedTextColor.WHITE;
        }
    }
}
