package com.wem.geezer.management;

import com.wem.geezer.Geezer;
import com.wem.geezer.database.DatabaseManager;
import com.wem.geezer.database.PlayerStats;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PearlManager {

    private final Geezer plugin;
    private final DatabaseManager databaseManager;
    private final Set<UUID> pearlPickupDisabled = new HashSet<>();

    public PearlManager(Geezer plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void load() {
        try {
            List<PlayerStats> allStats = databaseManager.getPlayerStatsDao().queryForAll();
            for (PlayerStats stats : allStats) {
                if (stats.isPearlsDisabled()) {
                    pearlPickupDisabled.add(stats.getPlayerUUID());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load pearl settings from database");
            e.printStackTrace();
        }
    }

    public boolean isPearlPickupDisabled(Player player) {
        return pearlPickupDisabled.contains(player.getUniqueId());
    }

    public void disablePearlPickup(Player player) {
        pearlPickupDisabled.add(player.getUniqueId());
        updatePlayerSetting(player.getUniqueId(), true);
    }

    public void enablePearlPickup(Player player) {
        pearlPickupDisabled.remove(player.getUniqueId());
        updatePlayerSetting(player.getUniqueId(), false);
    }

    private void updatePlayerSetting(UUID playerUUID, boolean disabled) {
        try {
            PlayerStats stats = databaseManager.getPlayerStatsDao().queryForId(playerUUID);
            if (stats != null) {
                stats.setPearlsDisabled(disabled);
                databaseManager.getPlayerStatsDao().update(stats);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update pearl setting for player " + playerUUID);
            e.printStackTrace();
        }
    }
}
