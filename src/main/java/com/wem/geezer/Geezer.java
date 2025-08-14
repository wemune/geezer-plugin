package com.wem.geezer;

import com.wem.geezer.database.DatabaseManager;
import com.wem.geezer.database.PlayerStats;
import com.wem.geezer.database.StatsManager;
import com.wem.geezer.listeners.AnvilListener;
import com.wem.geezer.listeners.CommandListener;
import com.wem.geezer.listeners.LootListener;
import com.wem.geezer.listeners.PlayerListener;
import com.wem.geezer.listeners.PearlListener;
import com.wem.geezer.listeners.SignListener;
import com.wem.geezer.listeners.WorldListener;
import com.wem.geezer.management.*;
import com.wem.geezer.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Map;
import java.util.Collections;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public final class Geezer extends JavaPlugin {

    public static final Component PREFIX = LegacyComponentSerializer.legacyAmpersand().deserialize("&b[Geezer] &r");
    
    private static final long STATS_SAVE_INTERVAL_TICKS = 1200L;
    private static final long ANNOUNCER_DELAY_TICKS = 100L;
    private static final int DB_EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 10;
    private static final int DB_EXECUTOR_FORCED_SHUTDOWN_TIMEOUT_SECONDS = 5;
    public static final long STATS_SAVE_LOG_INTERVAL_MS = 1800000L;

    private DatabaseManager databaseManager;
    private StatsManager statsManager;
    private RestartManager restartManager;
    private PlayerListManager playerListManager;
    private AFKManager afkManager;
    private BackupManager backupManager;
    private ContainerManager containerManager;
    private PearlManager pearlManager;
    private ZoneId zoneId;
    private ConfigManager configManager;

    private final Queue<Component> announcementQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isAnnouncementRunning = new AtomicBoolean(false);
    private BukkitTask announcerTask;

    private final AtomicInteger dbThreadCount = new AtomicInteger(1);
    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "geezer-db-" + dbThreadCount.getAndIncrement());
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler((thread, ex) -> {
                        java.util.logging.Logger.getLogger("Geezer").log(Level.SEVERE, "Uncaught DB executor exception", ex);
                    });
                    return t;
                }
            }
    );

    private final Map<UUID, Long> joinTimes = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> lastMessageSender = new ConcurrentHashMap<>();
    private final Map<UUID, Long> coordsCooldowns = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        try {
            String tz = configManager.getTimezone();
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
            getLogger().log(Level.SEVERE, "Exception while initializing database", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        statsManager = new StatsManager(this);
        restartManager = new RestartManager(this);
        playerListManager = new PlayerListManager(this);
        afkManager = new AFKManager(this);
        backupManager = new BackupManager(this);
        containerManager = new ContainerManager(this);
        pearlManager = new PearlManager();
        new DaylightManager(this);
        new CommandManager(this).registerCommands();

        getServer().getPluginManager().registerEvents(new PlayerListener(this, playerListManager), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
        getServer().getPluginManager().registerEvents(new LootListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new PearlListener(this), this);
        getServer().getPluginManager().registerEvents(afkManager, this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> statsManager.saveAll(), STATS_SAVE_INTERVAL_TICKS, STATS_SAVE_INTERVAL_TICKS);
        restartManager.scheduleRestart();
        playerListManager.start();
        afkManager.start();
        backupManager.scheduleBackup();
        pearlManager.load();

        Logger.info("Geezer plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        isAnnouncementRunning.set(false);
        announcementQueue.clear();
        if (announcerTask != null) announcerTask.cancel();

        if (restartManager != null) {
            restartManager.cancelRestart();
        }
        if (statsManager != null) {
            statsManager.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }

        dbExecutor.shutdown();
        try {
            if (!dbExecutor.awaitTermination(DB_EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
                if (!dbExecutor.awaitTermination(DB_EXECUTOR_FORCED_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    getLogger().log(Level.WARNING, "DB executor did not terminate after forced shutdown");
                }
            }
        } catch (InterruptedException ex) {
            dbExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            getLogger().log(Level.WARNING, "Interrupted while shutting down DB executor", ex);
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error shutting down DB executor", ex);
        }

        Logger.info("Geezer plugin has been disabled.");
    }


    public void sendMessage(CommandSender sender, Component message) {
        sender.sendMessage(PREFIX.append(message));
    }

    public void broadcast(Component message) {
        announcementQueue.add(message);
        tryStartAnnouncer();
    }

    private void tryStartAnnouncer() {
        if (isAnnouncementRunning.compareAndSet(false, true)) {
            announcerTask = new BukkitRunnable() {
                @Override
                public void run() {
                    Component messageToBroadcast = announcementQueue.poll();
                    if (messageToBroadcast != null) {
                        Component broadcastMessage = PREFIX.append(messageToBroadcast);
                        getServer().broadcast(broadcastMessage);

                        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                            player.sendActionBar(messageToBroadcast);
                        }
                    } else {
                        isAnnouncementRunning.set(false);
                        this.cancel();
                    }
                }
            }.runTaskTimer(this, 0L, ANNOUNCER_DELAY_TICKS);
        }
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

     public PearlManager getPearlManager() {
         return pearlManager;
     }

     public ConfigManager getConfigManager() {
        return configManager;
     }

     public ZoneId getZoneId() {
         return zoneId;
     }

     public Map<UUID, Long> getJoinTimes() {
         return Collections.unmodifiableMap(joinTimes);
     }

     public Map<UUID, Long> getJoinTimesInternal() {
         return joinTimes;
     }

     public Map<UUID, UUID> getLastMessageSender() {
         return Collections.unmodifiableMap(lastMessageSender);
     }

     public Map<UUID, Long> getCoordsCooldowns() {
         return Collections.unmodifiableMap(coordsCooldowns);
     }

    public CompletableFuture<PlayerStats> getOfflinePlayerStats(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return databaseManager.getPlayerStatsDao().queryForId(playerUUID);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Database error fetching offline player stats for " + playerUUID, e);
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }
}
