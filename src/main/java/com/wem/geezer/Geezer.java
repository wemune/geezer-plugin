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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

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

    private final Queue<Component> announcementQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isAnnouncementRunning = new AtomicBoolean(false);
    private BukkitTask announcerTask;

    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

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
        new DaylightManager(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this, playerListManager), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
        getServer().getPluginManager().registerEvents(new LootListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(afkManager, this);

        if (getCommand("playtime") != null) getCommand("playtime").setExecutor(new PlaytimeCommand(this)); else Logger.warn("Command 'playtime' missing from plugin.yml");
        if (getCommand("stats") != null) getCommand("stats").setExecutor(new StatsCommand(this)); else Logger.warn("Command 'stats' missing from plugin.yml");
        if (getCommand("uptime") != null) getCommand("uptime").setExecutor(new UptimeCommand(this)); else Logger.warn("Command 'uptime' missing from plugin.yml");
        if (getCommand("ping") != null) getCommand("ping").setExecutor(new PingCommand(this)); else Logger.warn("Command 'ping' missing from plugin.yml");
        if (getCommand("seen") != null) getCommand("seen").setExecutor(new SeenCommand(this)); else Logger.warn("Command 'seen' missing from plugin.yml");
        if (getCommand("coords") != null) getCommand("coords").setExecutor(new CoordsCommand(this)); else Logger.warn("Command 'coords' missing from plugin.yml");
        if (getCommand("msg") != null) getCommand("msg").setExecutor(new MsgCommand(this)); else Logger.warn("Command 'msg' missing from plugin.yml");
        if (getCommand("reply") != null) getCommand("reply").setExecutor(new ReplyCommand(this)); else Logger.warn("Command 'reply' missing from plugin.yml");
        if (getCommand("time") != null) getCommand("time").setExecutor(new TimeCommand(this)); else Logger.warn("Command 'time' missing from plugin.yml");
        if (getCommand("help") != null) getCommand("help").setExecutor(new HelpCommand(this)); else Logger.warn("Command 'help' missing from plugin.yml");
        if (getCommand("backup") != null) getCommand("backup").setExecutor(new BackupCommand(this)); else Logger.warn("Command 'backup' missing from plugin.yml");
        if (getCommand("togglerestart") != null) getCommand("togglerestart").setExecutor(new ToggleRestartCommand(this)); else Logger.warn("Command 'togglerestart' missing from plugin.yml");
        if (getCommand("colors") != null) getCommand("colors").setExecutor(new ColorsCommand(this)); else Logger.warn("Command 'colors' missing from plugin.yml");
        if (getCommand("broadcast") != null) getCommand("broadcast").setExecutor(new BroadcastCommand(this)); else Logger.warn("Command 'broadcast' missing from plugin.yml");

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> statsManager.saveAll(), 1200L, 1200L);
        restartManager.scheduleRestart();
        playerListManager.start();
        afkManager.start();
        backupManager.scheduleBackup();

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

        try {
            dbExecutor.shutdownNow();
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
            }.runTaskTimer(this, 0L, 100L);
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

     public ZoneId getZoneId() {
         return zoneId;
     }

     public Map<UUID, Long> getJoinTimes() {
         return Collections.unmodifiableMap(joinTimes);
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