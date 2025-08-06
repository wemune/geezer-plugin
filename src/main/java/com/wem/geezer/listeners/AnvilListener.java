package com.wem.geezer.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilListener implements Listener {

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        AnvilInventory anvilInventory = event.getInventory();
        String renameText = anvilInventory.getRenameText();

        if (result == null || renameText == null || renameText.isEmpty()) {
            return;
        }

        ItemMeta meta = result.getItemMeta();
        if (meta == null) {
            return;
        }

        Component coloredName = LegacyComponentSerializer.legacyAmpersand().deserialize(renameText);
        meta.displayName(coloredName);
        result.setItemMeta(meta);
    }
}
