package com.wem.geezer.management;

import com.wem.geezer.Geezer;
import com.wem.geezer.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    private final Geezer plugin;
    private final AtomicBoolean isBackupRunning = new AtomicBoolean(false);

    public BackupManager(Geezer plugin) {
        this.plugin = plugin;
    }

    public boolean startManualBackup() {
        if (isBackupRunning.get()) {
            return false;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::createBackup);
        return true;
    }

    public void scheduleBackup() {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("auto-backup.enabled", false)) {
            Logger.info("Auto-backup is disabled in the config.");
            return;
        }

        String backupTimeString = config.getString("auto-backup.backup-time", "03:30");
        String timeZoneString = config.getString("auto-backup.time-zone", "UTC");

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZoneString);
        } catch (Exception e) {
            Logger.severe("Invalid time-zone '" + timeZoneString + "' in config.yml for backup. Disabling auto-backup.");
            return;
        }

        LocalTime backupTime;
        try {
            backupTime = LocalTime.parse(backupTimeString);
        } catch (Exception e) {
            Logger.severe("Invalid backup-time format in config.yml. Please use HH:mm format. Disabling auto-backup.");
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime nextBackupTime = now.with(backupTime);
        if (now.isAfter(nextBackupTime) || now.isEqual(nextBackupTime)) {
            nextBackupTime = nextBackupTime.plusDays(1);
        }

        long totalSecondsUntilBackup = Duration.between(now, nextBackupTime).getSeconds();
        Logger.info("Next server backup is scheduled for " + backupTime + " " + zoneId + " (in " + formatDuration(totalSecondsUntilBackup) + ").");

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::createBackup, totalSecondsUntilBackup * 20L);
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    private void createBackup() {
        if (!isBackupRunning.compareAndSet(false, true)) {
            Logger.info("Backup was triggered but another backup process was already running.");
            return;
        }

        FileConfiguration config = plugin.getConfig();
        broadcastMessage("§c§lStarting server backup...");

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                world.setAutoSave(false);
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
        });

        try {
            Thread.sleep(5000);

            String backupFolderName = config.getString("auto-backup.backup-folder", "backups");
            File backupFolder = new File(plugin.getServer().getWorldContainer(), backupFolderName);
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
            String zipFileName = "backup-" + timestamp + ".zip";
            File zipFile = new File(backupFolder, zipFileName);

            List<String> filesToBackup = config.getStringList("auto-backup.files-to-backup");
            if (filesToBackup.isEmpty()) {
                filesToBackup = Arrays.asList("world", "world_nether", "world_the_end");
            }

            try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
                for (String fileName : filesToBackup) {
                    File file = new File(plugin.getServer().getWorldContainer(), fileName);
                    if (file.exists()) {
                        addFileToZip(file, fileName, zos);
                    } else {
                        Logger.warn("Could not find file/folder to backup: " + fileName);
                    }
                }
            }

            Logger.info("Backup created successfully: " + zipFileName);
            broadcastMessage("§c§lServer backup complete.");

            cleanupOldBackups(backupFolder);

        } catch (Exception e) {
            Logger.severe("An error occurred while creating the backup!");
            e.printStackTrace();
            broadcastMessage("§c§lAn error occurred during backup. Please check the console.");
        } finally {
            isBackupRunning.set(false);
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (World world : Bukkit.getWorlds()) {
                    world.setAutoSave(true);
                }
            });
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::scheduleBackup, 20L);
        }
    }

    private void broadcastMessage(String message) {
        Component component = LegacyComponentSerializer.legacySection().deserialize(message);
        plugin.sendMessage(Bukkit.getConsoleSender(), component);
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            plugin.sendMessage(player, component);
        }
    }

    private void addFileToZip(File file, String parentPath, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                addFileToZip(childFile, parentPath + "/" + childFile.getName(), zos);
            }
        } else {
            zos.putNextEntry(new ZipEntry(parentPath));
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
            }
            zos.closeEntry();
        }
    }

    private void cleanupOldBackups(File backupFolder) {
        int keepLast = plugin.getConfig().getInt("auto-backup.keep-last", 7);
        if (keepLast <= 0) return;

        File[] backupFiles = backupFolder.listFiles((dir, name) -> name.endsWith(".zip"));
        if (backupFiles == null || backupFiles.length <= keepLast) {
            return;
        }

        Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));

        int filesToDelete = backupFiles.length - keepLast;
        for (int i = 0; i < filesToDelete; i++) {
            if (backupFiles[i].delete()) {
                Logger.info("Deleted old backup: " + backupFiles[i].getName());
            } else {
                Logger.warn("Could not delete old backup: " + backupFiles[i].getName());
            }
        }
    }
}