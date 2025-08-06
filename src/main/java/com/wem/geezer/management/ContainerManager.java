package com.wem.geezer.management;

import com.wem.geezer.Geezer;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.TileState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ContainerManager {

    private final NamespacedKey containerIdKey;

    public ContainerManager(Geezer plugin) {
        this.containerIdKey = new NamespacedKey(plugin, "container_id");
    }

    public UUID getOrCreateContainerUUID(BlockState state) {
        BlockState primaryState = getPrimaryState(state);
        if (!(primaryState instanceof TileState)) {
            return null;
        }
        TileState tileState = (TileState) primaryState;
        PersistentDataContainer container = tileState.getPersistentDataContainer();

        String uuidString = container.get(containerIdKey, PersistentDataType.STRING);
        if (uuidString != null) {
            return UUID.fromString(uuidString);
        } else {
            UUID newUUID = UUID.randomUUID();
            container.set(containerIdKey, PersistentDataType.STRING, newUUID.toString());
            tileState.update();
            return newUUID;
        }
    }
    
    public UUID getOrCreateContainerUUID(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String uuidString = container.get(containerIdKey, PersistentDataType.STRING);
        if (uuidString != null) {
            return UUID.fromString(uuidString);
        } else {
            UUID newUUID = UUID.randomUUID();
            container.set(containerIdKey, PersistentDataType.STRING, newUUID.toString());
            itemStack.setItemMeta(meta);
            return newUUID;
        }
    }

    public void setContainerUUID(ItemStack itemStack, UUID uuid) {
        if (itemStack == null || uuid == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(containerIdKey, PersistentDataType.STRING, uuid.toString());
        itemStack.setItemMeta(meta);
    }

    public void setContainerUUID(BlockState state, UUID uuid) {
        BlockState primaryState = getPrimaryState(state);
        if (!(primaryState instanceof TileState) || uuid == null) return;
        TileState tileState = (TileState) primaryState;
        tileState.getPersistentDataContainer().set(containerIdKey, PersistentDataType.STRING, uuid.toString());
        tileState.update();
    }

    private BlockState getPrimaryState(BlockState state) {
        if (state instanceof Chest) {
            Chest chestState = (Chest) state;
            Inventory inv = chestState.getInventory();
            if (inv.getHolder() instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) inv.getHolder();
                Chest leftSide = (Chest) doubleChest.getLeftSide();
                return leftSide.getBlock().getState();
            }
        }
        return state;
    }
}