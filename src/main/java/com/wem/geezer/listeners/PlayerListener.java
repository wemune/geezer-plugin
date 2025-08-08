package com.wem.geezer.listeners;

import com.wem.geezer.Geezer;
import com.wem.geezer.database.PlayerStats;
import com.wem.geezer.database.StatsManager;
import com.wem.geezer.management.PlayerListManager;
import com.wem.geezer.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PlayerListener implements Listener {

    private final Geezer plugin;
    private final StatsManager statsManager;
    private final Map<UUID, Long> joinTimes;
    private final PlayerListManager playerListManager;
    private final Map<Material, Consumer<PlayerStats>> blockBreakHandlers = new EnumMap<>(Material.class);
    private final Set<Location> processedVeins = new HashSet<>();

    public PlayerListener(Geezer plugin, PlayerListManager playerListManager) {
        this.plugin = plugin;
        this.statsManager = plugin.getStatsManager();
        this.joinTimes = plugin.getJoinTimes();
        this.playerListManager = playerListManager;
        initializeBlockBreakHandlers();
    }

    private void initializeBlockBreakHandlers() {
        blockBreakHandlers.put(Material.DIAMOND_ORE, PlayerStats::incrementDiamondOreMined);
        blockBreakHandlers.put(Material.DEEPSLATE_DIAMOND_ORE, PlayerStats::incrementDiamondOreMined);
        blockBreakHandlers.put(Material.ANCIENT_DEBRIS, PlayerStats::incrementAncientDebrisMined);
        blockBreakHandlers.put(Material.GOLD_ORE, PlayerStats::incrementGoldOreMined);
        blockBreakHandlers.put(Material.DEEPSLATE_GOLD_ORE, PlayerStats::incrementGoldOreMined);
        blockBreakHandlers.put(Material.IRON_ORE, PlayerStats::incrementIronOreMined);
        blockBreakHandlers.put(Material.DEEPSLATE_IRON_ORE, PlayerStats::incrementIronOreMined);
        blockBreakHandlers.put(Material.EMERALD_ORE, PlayerStats::incrementEmeraldOreMined);
        blockBreakHandlers.put(Material.DEEPSLATE_EMERALD_ORE, PlayerStats::incrementEmeraldOreMined);
        blockBreakHandlers.put(Material.LAPIS_ORE, PlayerStats::incrementLapisOreMined);
        blockBreakHandlers.put(Material.DEEPSLATE_LAPIS_ORE, PlayerStats::incrementLapisOreMined);
        blockBreakHandlers.put(Material.REDSTONE_ORE, PlayerStats::incrementRedstoneOreMined);
        blockBreakHandlers.put(Material.DEEPSLATE_REDSTONE_ORE, PlayerStats::incrementRedstoneOreMined);
        blockBreakHandlers.put(Material.COAL_ORE, PlayerStats::incrementCoalOreMined);
        blockBreakHandlers.put(Material.DEEPSLATE_COAL_ORE, PlayerStats::incrementCoalOreMined);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        joinTimes.put(player.getUniqueId(), System.currentTimeMillis());
        playerListManager.setPlayerList(player);

        statsManager.loadPlayerStats(player, stats -> {
            if (stats != null) {
                sendMotd(player, stats.getLastSeen());
            }
        });

        if (plugin.getConfig().getBoolean("join-and-quit-messages.enabled", true)) {
            event.joinMessage(null);

            String joinMessageFormat = plugin.getConfig().getString("join-and-quit-messages.join", "&f%player_name% &7has &ajoined&7.");
            String formattedMessage = joinMessageFormat.replace("%player_name%", player.getName());
            Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(formattedMessage);

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (!onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                    plugin.sendMessage(onlinePlayer, component);
                }
            }
        }
    }

    private void sendMotd(Player player, Date lastSeen) {
        boolean isFirstJoin = (lastSeen == null);
        String motdPath = isFirstJoin ? "first-join-motd" : "motd";

        if (!plugin.getConfig().getBoolean(motdPath + ".enabled", true)) {
            return;
        }

        List<String> lines = plugin.getConfig().getStringList(motdPath + ".lines");

        if (lines.isEmpty()) {
            if (isFirstJoin) {
                lines = Arrays.asList(
                        "&b&lWelcome to Geezer World, &f%player_name%&b&l!",
                        "",
                        "&7We're happy to have you here!",
                        "",
                        "&7Type &b/help &7to see all available commands."
                );
            } else {
                lines = Arrays.asList(
                        "&b&lWelcome back to Geezer World, &f%player_name%&b&l!",
                        "",
                        "&7Last Seen: &f%last_seen%",
                        "&7Total Playtime: &f%playtime%",
                        "",
                        "&7Type &b/help &7to see all available commands."
                );
            }
        }

        PlayerStats stats = statsManager.getStats(player.getUniqueId());
        if (stats == null) {
            return;
        }

        String lastSeenStr = formatTimeDifference(lastSeen);
        String playtime = formatPlaytime(stats.getPlayTimeSeconds());

        for (String line : lines) {
            String formattedLine = line
                    .replace("%player_name%", player.getName())
                    .replace("%last_seen%", lastSeenStr)
                    .replace("%playtime%", playtime);
            Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(formattedLine);
            player.sendMessage(component);
        }
    }

    private String formatPlaytime(long totalSeconds) {
        long totalHours = TimeUnit.SECONDS.toHours(totalSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;

        StringBuilder sb = new StringBuilder();
        if (totalHours > 0) {
            sb.append(totalHours).append("h ");
        }
        if (minutes > 0 || sb.length() == 0) {
            sb.append(minutes).append("m");
        }

        return sb.toString().trim();
    }

    private String formatTimeDifference(Date lastSeenDate) {
        if (lastSeenDate == null) {
            return "Earlier today";
        }

        long diffMillis = System.currentTimeMillis() - lastSeenDate.getTime();
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis);

        if (totalSeconds < 60) {
            return "Just now";
        }

        long days = TimeUnit.SECONDS.toDays(totalSeconds);
        long hours = TimeUnit.SECONDS.toHours(totalSeconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
            sb.append(hours).append("h");
        } else if (hours > 0) {
            sb.append(hours).append("h ");
            sb.append(minutes).append("m");
        } else {
            sb.append(minutes).append("m");
        }

        return sb.append(" ago").toString();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        PlayerStats stats = statsManager.getStats(playerUUID);

        if (stats != null) {
            Long joinTime = joinTimes.remove(playerUUID);
            if (joinTime != null) {
                long sessionDurationMillis = System.currentTimeMillis() - joinTime;
                long sessionDurationSeconds = TimeUnit.MILLISECONDS.toSeconds(sessionDurationMillis);
                stats.incrementPlayTime(sessionDurationSeconds);
            }
            stats.setLastSeen(new java.util.Date());
        }

        statsManager.unloadPlayerStats(playerUUID);

        if (plugin.getConfig().getBoolean("join-and-quit-messages.enabled", true)) {
            String quitMessageFormat = plugin.getConfig().getString("join-and-quit-messages.quit", "&f%player_name% &7has &cleft&7.");
            String formattedMessage = quitMessageFormat.replace("%player_name%", player.getName());
            Component component = Geezer.PREFIX.append(LegacyComponentSerializer.legacyAmpersand().deserialize(formattedMessage));
            event.quitMessage(component);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = statsManager.getStats(player.getUniqueId());
        if (stats == null) return;

        Block block = event.getBlock();
        Material blockType = block.getType();

        Consumer<PlayerStats> handler = blockBreakHandlers.get(blockType);
        if (handler != null) {
            handler.accept(stats);
            Logger.info(player.getName() + " mined " + blockType.toString().replace("_", " "));
        }

        if (plugin.getConfig().getBoolean("rare-ore-announcements.enabled", true)) {
            boolean isRareOre = (blockType == Material.DIAMOND_ORE || blockType == Material.DEEPSLATE_DIAMOND_ORE || blockType == Material.ANCIENT_DEBRIS);

            if (isRareOre && !processedVeins.contains(block.getLocation())) {
                Set<Location> vein = scanVein(block.getLocation());
                processedVeins.addAll(vein);
                int veinSize = vein.size();

                String messageFormat = null;
                if (blockType == Material.DIAMOND_ORE || blockType == Material.DEEPSLATE_DIAMOND_ORE) {
                    messageFormat = plugin.getConfig().getString("rare-ore-announcements.diamond-message", "&f%player_name% &7has found &b%count% Diamonds&7!");
                    if (veinSize == 1) {
                        messageFormat = messageFormat.replace("Diamonds", "Diamond");
                    }
                } else if (blockType == Material.ANCIENT_DEBRIS) {
                    messageFormat = plugin.getConfig().getString("rare-ore-announcements.ancient-debris-message", "&f%player_name% &7has found &c%count% Ancient Debris&7!");
                }

                if (messageFormat != null) {
                    messageFormat = messageFormat.replace("%count%", String.valueOf(veinSize));

                    String finderMsg = messageFormat
                            .replace("%player_name%", "You")
                            .replace("&7has found", "&7have found");

                    String othersMsg = messageFormat.replace("%player_name%", player.getName());

                    Component finderComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(finderMsg);
                    Component othersComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(othersMsg);

                    for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                        if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                            plugin.sendMessage(onlinePlayer, finderComponent);
                        } else {
                            plugin.sendMessage(onlinePlayer, othersComponent);
                        }
                    }
                }
            }
        }
    }

    private Set<Location> scanVein(Location start) {
        Set<Location> vein = new HashSet<>();
        Queue<Location> toCheck = new LinkedList<>();
        Material oreType = start.getBlock().getType();

        toCheck.add(start);
        vein.add(start);

        while (!toCheck.isEmpty()) {
            Location current = toCheck.poll();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Location neighbor = current.clone().add(x, y, z);
                        if (neighbor.getBlock().getType() == oreType && !vein.contains(neighbor)) {
                            vein.add(neighbor);
                            toCheck.add(neighbor);
                        }
                    }
                }
            }
        }
        return vein;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerStats stats = statsManager.getStats(player.getUniqueId());
        if (stats != null) {
            stats.incrementDeaths();
            Logger.info(player.getName() + " has died.");
        }

        if (plugin.getConfig().getBoolean("death-messages.enabled", true)) {
            Component originalDeathMessageComp = event.deathMessage();
            if (originalDeathMessageComp == null) return;

            String legacyDeathMessage = LegacyComponentSerializer.legacySection().serialize(originalDeathMessageComp);

            String messageForDeceasedStr = legacyDeathMessage.replace(player.getName(), "You");
            String messageForOthersStr = legacyDeathMessage;

            Location loc = player.getLocation();
            String deathCoordsStr = String.format("§fYou §7died at §bX: §f%d, §bY: §f%d, §bZ: §f%d §7in §f%s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());

            event.deathMessage(null);

            Component deathMessage = LegacyComponentSerializer.legacySection().deserialize("§c" + messageForDeceasedStr);
            Component coordsMessage = LegacyComponentSerializer.legacySection().deserialize(deathCoordsStr);

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_GHAST_SCREAM, 0.5f, 1.0f);

                if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                    plugin.sendMessage(onlinePlayer, deathMessage);
                    plugin.sendMessage(onlinePlayer, coordsMessage);
                } else {
                    String processedMessageForOthersStr = messageForOthersStr.replace(player.getName(), "§f" + player.getName() + "§c");
                    plugin.sendMessage(onlinePlayer, LegacyComponentSerializer.legacySection().deserialize("§c" + processedMessageForOthersStr));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            PlayerStats stats = statsManager.getStats(killer.getUniqueId());
            if (stats != null) {
                stats.incrementMobsKilled();
                Logger.info(killer.getName() + " killed a " + event.getEntityType().toString().replace("_", " "));
            }
        }
    }
}
