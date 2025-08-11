package com.wem.geezer.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.wem.geezer.util.Logger;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private HikariDataSource dataSource;
    private Dao<PlayerStats, UUID> playerStatsDao;
    private Dao<ContainerLog, Integer> containerLogDao;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() throws SQLException {
        dataSource = new HikariDataSource();
        dataSource.setPoolName(plugin.getName() + "-HikariCP");

        File dbFile = new File(plugin.getDataFolder(), "database.db");
        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        dataSource.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setMaxLifetime(60000);
        dataSource.setMaximumPoolSize(10);

        ConnectionSource connectionSource = new DataSourceConnectionSource(dataSource, dataSource.getJdbcUrl());

        TableUtils.createTableIfNotExists(connectionSource, PlayerStats.class);
        TableUtils.createTableIfNotExists(connectionSource, ContainerLog.class);

        playerStatsDao = DaoManager.createDao(connectionSource, PlayerStats.class);
        containerLogDao = DaoManager.createDao(connectionSource, ContainerLog.class);

        performSchemaMigration();
    }

    private void performSchemaMigration() {
        try {
            playerStatsDao.queryBuilder().selectColumns("lastSeen").limit(1L).query();
        } catch (SQLException e) {
            if (e.getMessage().contains("no such column: lastSeen")) {
                try {
                    Logger.info("Performing database schema upgrade: Adding 'lastSeen' column to player_stats.");
                    playerStatsDao.executeRaw("ALTER TABLE player_stats ADD COLUMN lastSeen DATETIME;");
                    Logger.info("Database schema upgrade successful for player_stats.");
                } catch (SQLException ex) {
                    Logger.severe("Critical error during database schema upgrade for player_stats.");
                    ex.printStackTrace();
                }
            }
        }

        try {
            containerLogDao.queryBuilder().selectColumns("enchantments").limit(1L).query();
        } catch (SQLException e) {
            if (e.getMessage().contains("no such column: enchantments")) {
                try {
                    Logger.info("Performing database schema upgrade: Adding 'enchantments' column to container_logs.");
                    containerLogDao.executeRaw("ALTER TABLE container_logs ADD COLUMN enchantments VARCHAR;");
                    Logger.info("Database schema upgrade successful for container_logs.");
                } catch (SQLException ex) {
                    Logger.severe("Critical error during database schema upgrade for container_logs.");
                    ex.printStackTrace();
                }
            }
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public Dao<PlayerStats, UUID> getPlayerStatsDao() {
        return playerStatsDao;
    }

    public Dao<ContainerLog, Integer> getContainerLogDao() {
        return containerLogDao;
    }
}
