package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.commands.api.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ColorsCommand extends BaseCommand {

    private final Geezer plugin;

    public ColorsCommand(Geezer plugin) {
        super("colors");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&b--- Minecraft Color and Formatting Codes ---"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&7Use '&' followed by a code on signs or in an anvil."));
        sender.sendMessage(Component.text(" "));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&b--- Color Codes ---"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&0&0 Black &7| &1&1 Dark Blue &7| &2&2 Dark Green &7| &3&3 Dark Aqua"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&4&4 Dark Red &7| &5&5 Dark Purple &7| &6&6 Gold &7| &7&7 Gray"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&8&8 Dark Gray &7| &9&9 Blue &7| &a&a Green &7| &b&b Aqua"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&c&c Red &7| &d&d Light Purple &7| &e&e Yellow &7| &f&f White"));
        sender.sendMessage(Component.text(" "));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&b--- Formatting Codes ---"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&f&lBold &r&7(&l) | &f&nUnderline &r&7(&n) | &f&oItalic &r&7(&o)"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&f&mStrikethrough &r&7(&m) | &f&kObfuscated &r&7(&k)"));
        plugin.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize("&fReset &7(&r) - Resets all previous formatting."));

        return true;
    }
}
