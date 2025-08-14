package com.wem.geezer.management;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PearlManager {

    private final Set<UUID> pearlPickupDisabled = new HashSet<>();

    public void load() {
        // No loading needed - session-only storage
    }

    public boolean isPearlPickupDisabled(Player player) {
        return pearlPickupDisabled.contains(player.getUniqueId());
    }

    public void disablePearlPickup(Player player) {
        pearlPickupDisabled.add(player.getUniqueId());
    }

    public void enablePearlPickup(Player player) {
        pearlPickupDisabled.remove(player.getUniqueId());
    }
}