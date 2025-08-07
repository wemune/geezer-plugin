package com.wem.geezer.listeners;

import com.wem.geezer.Geezer;
import com.wem.geezer.database.ContainerLog;
import com.wem.geezer.management.ContainerManager;
import com.wem.geezer.ui.LogUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class LootListener implements Listener {

    private final Geezer plugin;
    private final ContainerManager containerManager;
    private final Map<UUID, InventorySnapshot> openInventories = new HashMap<>();
    private final Map<UUID, LogView> viewingLogs = new HashMap<>();

    public LootListener(Geezer plugin) {
        this.plugin = plugin;
        this.containerManager = plugin.getContainerManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.startsWith("Container Log")) return;

        Inventory inventory = event.getInventory();
        UUID containerId;

        if (inventory.getType() == InventoryType.ENDER_CHEST) {
            containerId = player.getUniqueId();
        } else {
            if (inventory.getHolder() == null || inventory.getLocation() == null) return;
            BlockState state = inventory.getLocation().getBlock().getState();
            if (!(state instanceof Container)) return;
            containerId = containerManager.getOrCreateContainerUUID(state);
        }

        if (containerId == null) return;
        openInventories.put(player.getUniqueId(), new InventorySnapshot(inventory, containerId));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.startsWith("Container Log")) {
            viewingLogs.remove(playerUUID);
            return;
        }

        if (!openInventories.containsKey(playerUUID)) return;

        InventorySnapshot snapshot = openInventories.remove(playerUUID);
        logChanges(player, snapshot.getContainerId(), snapshot.getItems(), getItemsFromInventory(event.getInventory()));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.getPlayer().isSneaking() || event.getClickedBlock() == null) {
            return;
        }

        BlockState blockState = event.getClickedBlock().getState();
        Player player = event.getPlayer();
        UUID containerId;

        if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
            containerId = player.getUniqueId();
        } else if (blockState instanceof Container) {
            containerId = containerManager.getOrCreateContainerUUID(blockState);
        } else {
            return;
        }

        event.setCancelled(true);
        if (containerId != null) {
            openLogUI(player, containerId, 1);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.startsWith("Container Log")) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.BARRIER) {
            return;
        }
        
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        LogView logView = viewingLogs.get(player.getUniqueId());
        if (logView == null) return;

        String displayName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        int newPage = logView.getPage();

        if (displayName.contains("Next Page")) newPage++;
        else if (displayName.contains("Previous Page")) newPage--;
        else return;

        openLogUI(player, logView.getContainerId(), newPage);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        BlockState state = event.getBlock().getState();
        if (!(state instanceof ShulkerBox)) {
            return;
        }

        UUID containerId = containerManager.getOrCreateContainerUUID(state);
        if (containerId == null) {
            return;
        }

        event.setDropItems(false);

        ShulkerBox shulkerState = (ShulkerBox) state;
        ItemStack shulkerItem = new ItemStack(state.getType());
        BlockStateMeta bsm = (BlockStateMeta) shulkerItem.getItemMeta();
        
        if (bsm != null) {
            bsm.getPersistentDataContainer().set(new NamespacedKey(plugin, "container_id"), PersistentDataType.STRING, containerId.toString());
            ShulkerBox itemShulkerState = (ShulkerBox) bsm.getBlockState();
            itemShulkerState.getInventory().setContents(shulkerState.getInventory().getContents());
            bsm.setBlockState(itemShulkerState);
            shulkerItem.setItemMeta(bsm);
        }

        state.getWorld().dropItemNaturally(state.getLocation(), shulkerItem);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        BlockState state = event.getBlock().getState();
        if (!(state instanceof ShulkerBox)) return;

        ItemStack itemInHand = event.getItemInHand();
        UUID containerId = containerManager.getOrCreateContainerUUID(itemInHand);
        if (containerId != null) {
            containerManager.setContainerUUID(state, containerId);
        }
    }

    private void openLogUI(Player player, UUID containerId, int page) {
        if (page < 1) {
            return;
        }
        try {
            List<ContainerLog> logs = plugin.getDatabaseManager().getContainerLogDao().queryBuilder()
                    .orderBy("timestamp", false)
                    .where().eq("containerUUID", containerId).query();

            LogUI logUI = new LogUI(plugin.getZoneId());
            player.openInventory(logUI.getLogInventory(logs, page));
            viewingLogs.put(player.getUniqueId(), new LogView(containerId, page));
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(Component.text("An error occurred while fetching the logs.", NamedTextColor.RED));
        }
    }

    private void logChanges(Player player, UUID containerId, Map<ItemSignature, Integer> initial, Map<ItemSignature, Integer> finalItems) {
        final Map<ItemSignature, Integer> changes = new HashMap<>();
        initial.forEach((item, count) -> {
            int finalCount = finalItems.getOrDefault(item, 0);
            if (count != finalCount) changes.put(item, finalCount - count);
        });
        finalItems.forEach((item, count) -> {
            if (!initial.containsKey(item)) changes.put(item, count);
        });

        changes.forEach((item, count) -> {
            if (count == 0) return;
            ContainerLog log = new ContainerLog(containerId, player.getUniqueId(), player.getName(), item.getMaterial(), item.getDisplayName(), item.getLore(), item.getEnchantments(), count, new Date());
            try {
                plugin.getDatabaseManager().getContainerLogDao().create(log);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private Map<ItemSignature, Integer> getItemsFromInventory(Inventory inventory) {
        Map<ItemSignature, Integer> items = new HashMap<>();
        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                ItemSignature sig = new ItemSignature(item);
                items.put(sig, items.getOrDefault(sig, 0) + item.getAmount());
            }
        }
        return items;
    }

    private static class InventorySnapshot {
        private final UUID containerId;
        private final Map<ItemSignature, Integer> items;

        public InventorySnapshot(Inventory inventory, UUID containerId) {
            this.containerId = containerId;
            this.items = getItems(inventory);
        }

        public UUID getContainerId() { return containerId; }
        public Map<ItemSignature, Integer> getItems() { return items; }

        private Map<ItemSignature, Integer> getItems(Inventory inventory) {
            Map<ItemSignature, Integer> itemMap = new HashMap<>();
            for (ItemStack item : inventory.getContents()) {
                if (item != null) {
                    ItemSignature sig = new ItemSignature(item);
                    itemMap.put(sig, itemMap.getOrDefault(sig, 0) + item.getAmount());
                }
            }
            return itemMap;
        }
    }

    private static class LogView {
        private final UUID containerId;
        private final int page;

        public LogView(UUID containerId, int page) {
            this.containerId = containerId;
            this.page = page;
        }

        public UUID getContainerId() { return containerId; }
        public int getPage() { return page; }
    }

    private static class ItemSignature {
        private final String material;
        private final String displayName;
        private final String lore;
        private final String enchantments;

        public ItemSignature(ItemStack item) {
            this.material = item.getType().toString();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                this.displayName = meta.hasDisplayName() ? LegacyComponentSerializer.legacyAmpersand().serialize(meta.displayName()) : null;
                this.lore = meta.hasLore() ? meta.lore().stream().map(line -> LegacyComponentSerializer.legacyAmpersand().serialize(line)).collect(Collectors.joining("\n")) : null;
                this.enchantments = meta.hasEnchants() ? meta.getEnchants().entrySet().stream()
                        .map(entry -> entry.getKey().getKey().getKey() + ":" + entry.getValue())
                        .collect(Collectors.joining(",")) : null;
            } else {
                this.displayName = null;
                this.lore = null;
                this.enchantments = null;
            }
        }

        public String getMaterial() { return material; }
        public String getDisplayName() { return displayName; }
        public String getLore() { return lore; }
        public String getEnchantments() { return enchantments; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemSignature that = (ItemSignature) o;
            return Objects.equals(material, that.material) &&
                    Objects.equals(displayName, that.displayName) &&
                    Objects.equals(lore, that.lore) &&
                    Objects.equals(enchantments, that.enchantments);
        }

        @Override
        public int hashCode() {
            return Objects.hash(material, displayName, lore, enchantments);
        }
    }
}
