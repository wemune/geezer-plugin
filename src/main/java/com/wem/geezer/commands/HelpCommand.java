package com.wem.geezer.commands;

import com.wem.geezer.Geezer;
import com.wem.geezer.commands.api.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HelpCommand extends BaseCommand {

    private final Geezer plugin;

    public HelpCommand(Geezer plugin) {
        super("help");
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize("§b--- Geezer Commands ---"));

        PluginDescriptionFile desc = plugin.getDescription();
        Map<String, Map<String, Object>> commands = desc.getCommands();

        for (Map.Entry<String, Map<String, Object>> entry : commands.entrySet()) {
            String cmdName = entry.getKey();
            Map<String, Object> cmdData = entry.getValue();

            if (cmdName.equalsIgnoreCase("help")) {
                continue;
            }

            String permission = (String) cmdData.get("permission");
            if (permission != null && !sender.hasPermission(permission)) {
                continue;
            }

            String usageFromYml = (String) cmdData.getOrDefault("usage", "/" + cmdName);
            String description = (String) cmdData.getOrDefault("description", "No description available.");

            String finalUsage;
            int firstSpace = usageFromYml.indexOf(' ');
            if (firstSpace != -1) {
                String arguments = usageFromYml.substring(firstSpace);
                finalUsage = "/" + cmdName + arguments;
            } else {
                finalUsage = "/" + cmdName;
            }

            Component message = LegacyComponentSerializer.legacySection().deserialize("§f" + finalUsage + " §7- " + description);
            plugin.sendMessage(sender, message);
        }
        return true;
    }
}