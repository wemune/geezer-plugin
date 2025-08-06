package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class CoordsCommand implements CommandExecutor {

    private final Geezer plugin;
    private final Map<UUID, Long> cooldowns;
    private static final long COOLDOWN_SECONDS = 30;

    public CoordsCommand(Geezer plugin) {
        this.plugin = plugin;
        this.cooldowns = plugin.getCoordsCooldowns();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (cooldowns.containsKey(playerUUID)) {
            long secondsLeft = ((cooldowns.get(playerUUID) / 1000) + COOLDOWN_SECONDS) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                plugin.sendMessage(player, Component.text("You must wait " + secondsLeft + " more seconds before using this command again.", NamedTextColor.RED));
                return true;
            }
        }

        Location loc = player.getLocation();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        if (args.length == 0) {
            Component messageForOthers = LegacyComponentSerializer.legacySection().deserialize(String.format("§f%s §7is at §bX: §f%d, §bY: §f%d, §bZ: §f%d §7in §f%s", player.getName(), x, y, z, loc.getWorld().getName()));
            Component messageForSelfLine1 = LegacyComponentSerializer.legacySection().deserialize("§7Your coordinates have been broadcasted.");
            Component messageForSelfLine2 = LegacyComponentSerializer.legacySection().deserialize(String.format("§bX: §f%d, §bY: §f%d, §bZ: §f%d §7in §f%s", x, y, z, loc.getWorld().getName()));

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
                if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                    plugin.sendMessage(onlinePlayer, messageForSelfLine1);
                    plugin.sendMessage(onlinePlayer, messageForSelfLine2);
                } else {
                    plugin.sendMessage(onlinePlayer, messageForOthers);
                }
            }
        } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                plugin.sendMessage(player, Component.text("Player '" + args[0] + "' not found or is not online.", NamedTextColor.RED));
                return true;
            }

            if (player.equals(target)) {
                plugin.sendMessage(player, Component.text("You cannot send your coordinates to yourself. Use /coords to broadcast them.", NamedTextColor.RED));
                return true;
            }

            Component messageToTarget = LegacyComponentSerializer.legacySection().deserialize(String.format("§7[§f%s §b-> §fYou§7] §fMy coordinates are: §bX: §f%d, §bY: §f%d, §bZ: §f%d", player.getName(), x, y, z));
            plugin.sendMessage(target, messageToTarget);
            target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);

            Component sentConfirmation = LegacyComponentSerializer.legacySection().deserialize("§7Your coordinates have been sent to §f" + target.getName() + "§7.");
            plugin.sendMessage(player, sentConfirmation);

            Component messageForSelf = LegacyComponentSerializer.legacySection().deserialize(String.format("§bX: §f%d, §bY: §f%d, §bZ: §f%d §7in §f%s", x, y, z, loc.getWorld().getName()));
            plugin.sendMessage(player, messageForSelf);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
        } else {
            plugin.sendMessage(player, Component.text("Usage: /coords [player]", NamedTextColor.RED));
            return true;
        }

        cooldowns.put(playerUUID, System.currentTimeMillis());
        return true;
    }
}