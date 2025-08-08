package com.wem.geezer.management;

import com.wem.geezer.Geezer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DaylightManager {

    private final Geezer plugin;
    private final Map<UUID, Boolean> announcedSunset = new HashMap<>();
    private final Map<UUID, Boolean> announcedSunrise = new HashMap<>();

    public DaylightManager(Geezer plugin) {
        this.plugin = plugin;
        start();
    }

    private void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() == World.Environment.NORMAL) {
                        long ticks = world.getTime();
                        UUID worldId = world.getUID();

                        if (ticks >= 12000 && ticks < 13000) {
                            if (!announcedSunset.getOrDefault(worldId, false)) {
                                plugin.getServer().broadcast(Geezer.PREFIX.append(Component.text("The sun begins to set.", NamedTextColor.GRAY)));
                                announcedSunset.put(worldId, true);
                                announcedSunrise.put(worldId, false);
                            }
                        } else {
                            announcedSunset.put(worldId, false);
                        }

                        if (ticks >= 23000 && ticks < 24000) {
                            if (!announcedSunrise.getOrDefault(worldId, false)) {
                                plugin.getServer().broadcast(Geezer.PREFIX.append(Component.text("The sun begins to rise.", NamedTextColor.GRAY)));
                                announcedSunrise.put(worldId, true);
                                announcedSunset.put(worldId, false);
                            }
                        } else {
                            announcedSunrise.put(worldId, false);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }
}
