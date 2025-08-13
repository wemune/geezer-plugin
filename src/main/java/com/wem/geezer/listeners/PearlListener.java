package com.wem.geezer.listeners;

import com.wem.geezer.Geezer;
import com.wem.geezer.management.PearlManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class PearlListener implements Listener {

    private final PearlManager pearlManager;

    public PearlListener(Geezer plugin) {
        this.pearlManager = plugin.getPearlManager();
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (pearlManager.isPearlPickupDisabled(player)) {
            if (event.getItem().getItemStack().getType() == Material.ENDER_PEARL) {
                event.setCancelled(true);
            }
        }
    }
}
