package com.wem.geezer.management;

import com.wem.geezer.Geezer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

public class PlayerListManager {

    private final Geezer plugin;
    private String header;
    private String footer;

    public PlayerListManager(Geezer plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        this.header = plugin.getConfig().getString("player-list.header", "&b&lGeezer World\n&7Welcome, &f%player_name%&7!");
        this.footer = plugin.getConfig().getString("player-list.footer", "\n&7Players Online: &f%online%/%max_players%\n&7Uptime: &f%uptime% &8| &7Ping: &f%ping%ms");
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateAllPlayerLists, 0L, 20L);
    }

    public void updateAllPlayerLists() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setPlayerList(player);
        }
    }

    public void setPlayerList(Player player) {
        Component formattedHeader = formatPlaceholders(header, player);
        Component formattedFooter = formatPlaceholders(footer, player);
        player.sendPlayerListHeaderAndFooter(formattedHeader, formattedFooter);
    }

    private Component formatPlaceholders(String text, Player player) {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        String uptime = formatUptime(uptimeMillis);

        String formattedText = text
                .replace("%player_name%", player.getName())
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("%uptime%", uptime)
                .replace("%ping%", String.valueOf(player.getPing()));

        return LegacyComponentSerializer.legacyAmpersand().deserialize(formattedText);
    }

    private String formatUptime(long totalMillis) {
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(totalMillis);
        long hours = TimeUnit.SECONDS.toHours(totalSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm %ds", minutes, seconds);
        }
    }
}