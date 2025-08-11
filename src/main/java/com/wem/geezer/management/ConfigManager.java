package com.wem.geezer.management;

import com.wem.geezer.Geezer;
import com.wem.geezer.util.Logger;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.zone.ZoneRulesException;
import java.util.List;

@Getter
public class ConfigManager {

    private final Geezer plugin;

    // General
    private String timezone;

    // Auto Restart
    private boolean autoRestartEnabled;
    private String autoRestartTime;
    private List<Integer> autoRestartWarningMinutes;

    // Auto Backup
    private boolean autoBackupEnabled;
    private List<String> autoBackupTimes;
    private String autoBackupFolder;
    private int autoBackupKeepLast;
    private List<String> autoBackupFiles;

    // Player List
    private String playerListHeader;
    private String playerListFooter;

    // MOTD
    private boolean motdEnabled;
    private List<String> motdLines;

    // First Join MOTD
    private boolean firstJoinMotdEnabled;
    private List<String> firstJoinMotdLines;

    // Join/Quit Messages
    private boolean joinQuitMessagesEnabled;
    private String joinMessage;
    private String quitMessage;

    // Death Messages
    private boolean deathMessagesEnabled;
    private String deathMessageFormat;

    // Rare Ore Announcements
    private boolean rareOreAnnouncementsEnabled;
    private String diamondMessage;
    private String ancientDebrisMessage;

    public ConfigManager(Geezer plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // General
        timezone = config.getString("timezone", "Europe/Stockholm");
        validateTimezone();

        // Auto Restart
        autoRestartEnabled = config.getBoolean("auto-restart.enabled", true);
        autoRestartTime = config.getString("auto-restart.restart-time", "04:00");
        validateTime(autoRestartTime, "auto-restart.restart-time");
        autoRestartWarningMinutes = config.getIntegerList("auto-restart.warning-minutes");
        validateWarningMinutes();

        // Auto Backup
        autoBackupEnabled = config.getBoolean("auto-backup.enabled", true);
        autoBackupTimes = config.getStringList("auto-backup.backup-times");
        validateBackupTimes();
        autoBackupFolder = config.getString("auto-backup.backup-folder", "backups");
        autoBackupKeepLast = config.getInt("auto-backup.keep-last", 14);
        autoBackupFiles = config.getStringList("auto-backup.files-to-backup");

        // Player List
        playerListHeader = config.getString("player-list.header", "&b&lGeezer World\n&7Welcome, &f%player_name%&7!");
        playerListFooter = config.getString("player-list.footer", "\n&7Players Online: &f%online%/%max_players%\n&7Uptime: &f%uptime% &8| &7Ping: &f%ping%ms");

        // MOTD
        motdEnabled = config.getBoolean("motd.enabled", true);
        motdLines = config.getStringList("motd.lines");

        // First Join MOTD
        firstJoinMotdEnabled = config.getBoolean("first-join-motd.enabled", true);
        firstJoinMotdLines = config.getStringList("first-join-motd.lines");

        // Join/Quit Messages
        joinQuitMessagesEnabled = config.getBoolean("join-and-quit-messages.enabled", true);
        joinMessage = config.getString("join-and-quit-messages.join", "&f%player_name% &7has &ajoined&7.");
        quitMessage = config.getString("join-and-quit-messages.quit", "&f%player_name% &7has &cleft&7.");

        // Death Messages
        deathMessagesEnabled = config.getBoolean("death-messages.enabled", true);
        deathMessageFormat = config.getString("death-messages.format", "&c%death_message%");

        // Rare Ore Announcements
        rareOreAnnouncementsEnabled = config.getBoolean("rare-ore-announcements.enabled", true);
        diamondMessage = config.getString("rare-ore-announcements.diamond-message", "&f%player_name% &7has found &b%count% Diamonds&7!");
        ancientDebrisMessage = config.getString("rare-ore-announcements.ancient-debris-message", "&f%player_name% &7has found &c%count% Ancient Debris&7!");
        
        validateKeepLastBackups();
    }
    
    private void validateTimezone() {
        try {
            ZoneId.of(timezone);
        } catch (ZoneRulesException e) {
            Logger.warn("Invalid timezone '" + timezone + "' in config.yml. Using default 'Europe/Stockholm'.");
            timezone = "Europe/Stockholm";
        }
    }
    
    private void validateTime(String time, String configPath) {
        try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            Logger.warn("Invalid time format '" + time + "' for " + configPath + ". Should be HH:mm format (e.g., '04:00').");
        }
    }
    
    private void validateWarningMinutes() {
        if (autoRestartWarningMinutes.isEmpty()) {
            Logger.warn("No restart warning minutes configured. Players will receive no restart warnings.");
        }
        for (Integer minutes : autoRestartWarningMinutes) {
            if (minutes <= 0) {
                Logger.warn("Invalid warning minute value: " + minutes + ". Warning minutes must be positive.");
            }
        }
    }
    
    private void validateBackupTimes() {
        if (autoBackupEnabled && autoBackupTimes.isEmpty()) {
            Logger.warn("Auto-backup is enabled but no backup times are configured.");
        }
        for (String time : autoBackupTimes) {
            validateTime(time, "auto-backup.backup-times");
        }
    }
    
    private void validateKeepLastBackups() {
        if (autoBackupKeepLast <= 0) {
            Logger.warn("Invalid keep-last backup value: " + autoBackupKeepLast + ". Must be positive. Using default of 14.");
            autoBackupKeepLast = 14;
        }
    }
}
