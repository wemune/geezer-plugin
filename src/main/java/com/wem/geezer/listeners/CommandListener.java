package com.wem.geezer.listeners;

import com.wem.geezer.Geezer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    private final Geezer plugin;

    public CommandListener(Geezer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();

        if (command.equals("/restart")) {
            event.setCancelled(true);

            if (!event.getPlayer().hasPermission("geezer.restart")) {
                plugin.sendMessage(event.getPlayer(), Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return;
            }

            plugin.getRestartManager().startManualRestart();
        }
    }
}