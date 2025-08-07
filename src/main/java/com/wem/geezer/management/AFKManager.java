package com.wem.geezer.management;

import com.wem.geezer.Geezer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AFKManager implements Listener {

    private final Geezer plugin;
    private final Map<UUID, Long> lastMoveTimes = new ConcurrentHashMap<>();
    private final Set<UUID> afkPlayers = new HashSet<>();
    private static final long AFK_TIME_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(5);

    public AFKManager(Geezer plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkAfkStatus, 0L, 200L);
    }

    private void checkAfkStatus() {
        long now = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            if (afkPlayers.contains(playerUUID)) {
                continue;
            }

            long lastMoveTime = lastMoveTimes.getOrDefault(playerUUID, now);
            if (now - lastMoveTime > AFK_TIME_THRESHOLD_MS) {
                setPlayerAfk(player);
            }
        }
    }

    private void setPlayerAfk(Player player) {
        afkPlayers.add(player.getUniqueId());
        Component message = LegacyComponentSerializer.legacySection().deserialize(String.format("§f%s §7is now AFK.", player.getName()));
        Component selfMessage = LegacyComponentSerializer.legacySection().deserialize("§fYou §7are now AFK.");

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                plugin.sendMessage(onlinePlayer, selfMessage);
            } else {
                plugin.sendMessage(onlinePlayer, message);
            }
        }
    }

    private void setPlayerActive(Player player) {
        afkPlayers.remove(player.getUniqueId());
        Component message = LegacyComponentSerializer.legacySection().deserialize(String.format("§f%s §7is no longer AFK.", player.getName()));
        Component selfMessage = LegacyComponentSerializer.legacySection().deserialize("§fYou §7are no longer AFK.");

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                plugin.sendMessage(onlinePlayer, selfMessage);
            } else {
                plugin.sendMessage(onlinePlayer, message);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        lastMoveTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        lastMoveTimes.remove(playerUUID);
        afkPlayers.remove(playerUUID);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // The PlayerMoveEvent fires for both position and rotation changes.
        // We check if the player has actually moved their position or camera.
        if (event.hasChangedPosition() || event.hasChangedOrientation()) {
            Player player = event.getPlayer();
            lastMoveTimes.put(player.getUniqueId(), System.currentTimeMillis());

            if (afkPlayers.contains(player.getUniqueId())) {
                setPlayerActive(player);
            }
        }
    }
}
