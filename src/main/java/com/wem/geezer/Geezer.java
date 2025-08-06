package com.wem.geezer;

import com.wem.geezer.commands.*;
import com.wem.geezer.database.DatabaseManager;
import com.wem.geezer.database.PlayerStats;
import com.wem.geezer.database.StatsManager;
import com.wem.geezer.listeners.AnvilListener;
import com.wem.geezer.listeners.CommandListener;
import com.wem.geezer.listeners.LootListener;
import com.wem.geezer.listeners.PlayerListener;
import com.wem.geezer.listeners.SignListener;
import com.wem.geezer.management.*;
import com.wem.geezer.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class Geezer extends JavaPlugin {

    public static final Component PREFIX = LegacyComponentSerializer.legacyAmpersand().deserialize("&b[Geezer] &r");

    private DatabaseManager databaseManager;
    private StatsManager statsManager;
    private RestartManager restartManager;
    private PlayerListManager playerListManager;
    private AFKManager afkManager;
    private BackupManager backupManager;
    private ContainerManager containerManager;
    private ZoneId zoneId;
    private final Map<UUID, Long> joinTimes = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> lastMessageSender = new ConcurrentHashMap<>();
    private final Map<UUID, Long> coordsCooldowns = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            String tz = getConfig().getString("timezone", "Europe/Stockholm");
            this.zoneId = ZoneId.of(tz);
            Logger.info("Using timezone: " + this.zoneId);
        } catch (ZoneRulesException e) {
            Logger.warn("Invalid timezone in config.yml. Defaulting to Europe/Stockholm.");
            this.zoneId = ZoneId.of("Europe/Stockholm");
        }
        
        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.initialize();
            Logger.info("Database initialized successfully.");
        } catch (SQLException e) {
            Logger.severe("Failed to initialize database! Disabling plugin.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        statsManager = new StatsManager(this);
        restartManager = new RestartManager(this);
        playerListManager = new PlayerListManager(this);
        afkManager = new AFKManager(this);
        backupManager = new BackupManager(this);
        containerManager = new ContainerManager(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this, playerListManager), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
        getServer().getPluginManager().registerEvents(new LootListener(this), this);
        getServer().getPluginManager().registerEvents(afkManager, this);

        Objects.requireNonNull(getCommand("playtime")).setExecutor(new PlaytimeCommand(this));
        Objects.requireNonNull(getCommand("stats")).setExecutor(new StatsCommand(this));
        Objects.requireNonNull(getCommand("uptime")).setExecutor(new UptimeCommand(this));
        Objects.requireNonNull(getCommand("ping")).setExecutor(new PingCommand(this));
        Objects.requireNonNull(getCommand("seen")).setExecutor(new SeenCommand(this));
        Objects.requireNonNull(getCommand("coords")).setExecutor(new CoordsCommand(this));
        Objects.requireNonNull(getCommand("msg")).setExecutor(new MsgCommand(this));
        Objects.requireNonNull(getCommand("reply")).setExecutor(new ReplyCommand(this));
        Objects.requireNonNull(getCommand("time")).setExecutor(new TimeCommand(this));
        Objects.requireNonNull(getCommand("help")).setExecutor(new HelpCommand(this));
        Objects.requireNonNull(getCommand("backup")).setExecutor(new BackupCommand(this));
        Objects.requireNonNull(getCommand("togglerestart")).setExecutor(new ToggleRestartCommand(this));

        Bukkit.getScheduler().runTaskTimer(this, () -> statsManager.saveAll(), 1200L, 1200L);
        restartManager.scheduleRestart();
        playerListManager.start();
        afkManager.start();
        backupManager.scheduleBackup();

        Logger.info("Geezer plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (restartManager != null) {
            restartManager.cancelRestart();
        }
        if (statsManager != null) {
            statsManager.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        Logger.info("Geezer plugin has been disabled.");
    }

    public void sendMessage(CommandSender sender, Component message) {
        sender.sendMessage(PREFIX.append(message));
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }

    public RestartManager getRestartManager() {
        return restartManager;
    }

    public ContainerManager getContainerManager() {
        return containerManager;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public Map<UUID, Long> getJoinTimes() {
        return joinTimes;
    }

    public Map<UUID, UUID> getLastMessageSender() {
        return lastMessageSender;
    }

    public Map<UUID, Long> getCoordsCooldowns() {
        return coordsCooldowns;
    }

    public CompletableFuture<PlayerStats> getOfflinePlayerStats(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return databaseManager.getPlayerStatsDao().queryForId(playerUUID);
            } catch (SQLException e) {
                Logger.severe("Database error fetching offline player stats for " + playerUUID);
                e.printStackTrace();
                return null;
            }
        });
    }
}