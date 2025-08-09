package com.wem.geezer.management;

import com.wem.geezer.Geezer;
import com.wem.geezer.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestartManager {

    private final Geezer plugin;
    private final List<BukkitTask> scheduledTasks = new ArrayList<>();
    private boolean isRestartScheduled = false;
    private ZonedDateTime nextRestartTime = null;

    public RestartManager(Geezer plugin) {
        this.plugin = plugin;
    }

    public void scheduleRestart() {
        cancelRestart();

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("auto-restart.enabled", true)) {
            Logger.info("Auto-restart is disabled in the config.");
            isRestartScheduled = false;
            return;
        }

        String restartTimeString = config.getString("auto-restart.restart-time", "04:00");
        ZoneId zoneId = plugin.getZoneId();

        LocalTime restartTime;
        try {
            restartTime = LocalTime.parse(restartTimeString);
        } catch (Exception e) {
            Logger.severe("Invalid restart-time format in config.yml. Please use HH:mm format. Disabling auto-restart.");
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        this.nextRestartTime = now.with(restartTime);
        if (now.isAfter(nextRestartTime) || now.isEqual(nextRestartTime)) {
            this.nextRestartTime = nextRestartTime.plusDays(1);
        }

        long totalSecondsUntilRestart = Duration.between(now, this.nextRestartTime).getSeconds();
        Logger.info("Next server restart is scheduled for " + restartTime + " " + zoneId + " (in " + formatDuration(totalSecondsUntilRestart) + ").");

        BukkitTask mainTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Logger.info("Executing scheduled server restart...");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
        }, totalSecondsUntilRestart * 20L);
        scheduledTasks.add(mainTask);

        List<Integer> warningMinutes = config.getIntegerList("auto-restart.warning-minutes");
        if (warningMinutes.isEmpty()) {
            warningMinutes = Arrays.asList(60, 30, 15, 5, 1);
        }
        for (int minutes : warningMinutes) {
            if (minutes <= 0) continue;
            long secondsBeforeRestart = (long) minutes * 60;
            long warningDelaySeconds = totalSecondsUntilRestart - secondsBeforeRestart;

            if (warningDelaySeconds > 0) {
                BukkitTask warningTask = Bukkit.getScheduler().runTaskLater(plugin, () -> broadcastRestartWarning(minutes), warningDelaySeconds * 20L);
                scheduledTasks.add(warningTask);
            }
        }
        isRestartScheduled = true;
    }

    private void broadcastRestartWarning(int minutes) {
        String timeString = minutes + " " + (minutes > 1 ? "minutes" : "minute");
        Component message = Component.text("Server is restarting in " + timeString + ".", NamedTextColor.RED, TextDecoration.BOLD);
        plugin.broadcast(message);
    }

    public void startManualRestart() {
        cancelRestart();
        Component message = Component.text("Server is restarting in 10 seconds.", NamedTextColor.RED, TextDecoration.BOLD);
        plugin.broadcast(message);
        BukkitTask mainTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Logger.info("Executing manual server restart...");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
        }, 20L * 10);
        scheduledTasks.add(mainTask);
    }

    public boolean cancelRestart() {
        if (!scheduledTasks.isEmpty()) {
            for (BukkitTask task : scheduledTasks) {
                task.cancel();
            }
            scheduledTasks.clear();
            Logger.info("Scheduled server restart and all warnings have been cancelled.");
            isRestartScheduled = false;
            nextRestartTime = null;
            return true;
        }
        isRestartScheduled = false;
        nextRestartTime = null;
        return false;
    }

    public boolean isRestartScheduled() {
        return isRestartScheduled;
    }

    public ZonedDateTime getNextRestartTime() {
        return nextRestartTime;
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }
}