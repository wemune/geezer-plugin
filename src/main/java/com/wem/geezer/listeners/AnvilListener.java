package com.wem.geezer.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
        if (result == null) {
            return;
        }

        AnvilInventory anvilInventory = event.getInventory();
        String renameText = anvilInventory.getRenameText();
        ItemStack firstItem = anvilInventory.getFirstItem();

        if (renameText.isEmpty()) {
            return;
        }

        ItemMeta resultMeta = result.getItemMeta();
        if (resultMeta == null) {
            return;
        }

        Component finalName;

        if (firstItem != null && firstItem.hasItemMeta()) {
            ItemMeta firstItemMeta = firstItem.getItemMeta();
            if (firstItemMeta.hasDisplayName()) {
                String plainFirstName = PlainTextComponentSerializer.plainText().serialize(firstItemMeta.displayName());
                if (renameText.equals(plainFirstName)) {
                    finalName = firstItemMeta.displayName();
                } else {
                    finalName = LegacyComponentSerializer.legacyAmpersand().deserialize(renameText);
                }
            } else {
                finalName = LegacyComponentSerializer.legacyAmpersand().deserialize(renameText);
            }
        } else {
            finalName = LegacyComponentSerializer.legacyAmpersand().deserialize(renameText);
        }

        resultMeta.displayName(finalName);
        result.setItemMeta(resultMeta);
    }
}