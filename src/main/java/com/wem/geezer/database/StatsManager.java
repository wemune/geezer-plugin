package com.wem.geezer.database;

import com.wem.geezer.Geezer;
import com.wem.geezer.util.Logger;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class StatsManager {

    private final Geezer plugin;
    private final ConcurrentHashMap<UUID, PlayerStats> statsCache = new ConcurrentHashMap<>();

    public StatsManager(Geezer plugin) {
        this.plugin = plugin;
    }

    public void loadPlayerStats(Player player, Consumer<PlayerStats> callback) {
        plugin.getOfflinePlayerStats(player.getUniqueId()).thenAccept(loadedStats -> {
            final PlayerStats finalStats;
            if (loadedStats == null) {
                finalStats = new PlayerStats(player.getUniqueId());
            } else {
                finalStats = loadedStats;
            }
            statsCache.put(player.getUniqueId(), finalStats);

            if (callback != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(finalStats));
            }
        });
    }

    public void unloadPlayerStats(UUID playerUUID) {
        PlayerStats stats = statsCache.remove(playerUUID);
        if (stats != null) {
            saveStatsToDatabase(stats);
        }
    }

    public PlayerStats getStats(UUID playerUUID) {
        return statsCache.get(playerUUID);
    }

    public void saveStatsToDatabase(PlayerStats stats) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getDatabaseManager().getPlayerStatsDao().createOrUpdate(stats);
            } catch (SQLException e) {
                Logger.severe("Failed to save stats for " + stats.getPlayerUUID() + ": " + e.getMessage());
            }
        });
    }

    private int saveCount = 0;
    private long lastLogTime = System.currentTimeMillis();

    public void saveAll() {
        saveCount++;
        long now = System.currentTimeMillis();
        if (now - lastLogTime > Geezer.STATS_SAVE_LOG_INTERVAL_MS) {
            Logger.info("Saved all player data " + saveCount + " times in the last 30 minutes.");
            lastLogTime = now;
            saveCount = 0;
        }

        for (PlayerStats stats : statsCache.values()) {
            stats.setLastSeen(new Date());
            saveStatsToDatabase(stats);
        }
    }
}
