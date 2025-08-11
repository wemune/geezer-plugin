package com.wem.geezer.management;

import com.wem.geezer.Geezer;
import com.wem.geezer.commands.api.BaseCommand;
import com.wem.geezer.util.Logger;
import org.bukkit.command.PluginCommand;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;

public class CommandManager {

    private final Geezer plugin;

    public CommandManager(Geezer plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        Reflections reflections = new Reflections("com.wem.geezer.commands");
        Set<Class<? extends BaseCommand>> commandClasses = reflections.getSubTypesOf(BaseCommand.class);

        for (Class<? extends BaseCommand> cmdClass : commandClasses) {
            if (Modifier.isAbstract(cmdClass.getModifiers())) {
                continue;
            }

            try {
                BaseCommand commandInstance = cmdClass.getConstructor(Geezer.class).newInstance(plugin);
                PluginCommand pluginCommand = plugin.getCommand(commandInstance.getName());

                if (pluginCommand != null) {
                    pluginCommand.setExecutor(commandInstance);
                } else {
                    Logger.warn("Command '" + commandInstance.getName() + "' is not defined in plugin.yml and could not be registered.");
                }
            } catch (Exception e) {
                Logger.warn("Could not register command " + cmdClass.getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}
