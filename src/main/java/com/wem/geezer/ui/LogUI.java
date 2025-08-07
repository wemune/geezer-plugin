package com.wem.geezer.ui;

import com.wem.geezer.database.ContainerLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LogUI {

    private static final int ITEMS_PER_PAGE = 45;
    private final SimpleDateFormat dateFormat;

    public LogUI(ZoneId zoneId) {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone(zoneId));
    }

    public Inventory getLogInventory(List<ContainerLog> logs, int page) {
        Component title = Component.text("Container Log - Page " + page);
        Inventory inv = Bukkit.createInventory(null, 54, title);

        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.displayName(Component.text(" "));
            borderItem.setItemMeta(borderMeta);
        }
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, borderItem);
        }

        inv.setItem(49, createBrandmark());

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, logs.size());

        if (startIndex < logs.size()) {
            List<ContainerLog> pageLogs = logs.subList(startIndex, endIndex);
            for (int i = 0; i < pageLogs.size(); i++) {
                ContainerLog log = pageLogs.get(i);
                inv.setItem(i, createLogItem(log));
            }
        }

        if (page > 1) {
            inv.setItem(45, createNavigationItem(Material.ARROW, "Previous Page", NamedTextColor.GREEN));
        } else {
            inv.setItem(45, createNavigationItem(Material.BARRIER, "(No Previous Page)", NamedTextColor.RED));
        }

        if (logs.size() > endIndex) {
            inv.setItem(53, createNavigationItem(Material.ARROW, "Next Page", NamedTextColor.GREEN));
        } else {
            inv.setItem(53, createNavigationItem(Material.BARRIER, "(No Next Page)", NamedTextColor.RED));
        }

        return inv;
    }

    private ItemStack createBrandmark() {
        ItemStack brandmark = new ItemStack(Material.CLOCK);
        ItemMeta meta = brandmark.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Geezer Log", NamedTextColor.AQUA, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Container Transaction History", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(""));
            lore.add(Component.text("Additions are green", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Removals are red", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            brandmark.setItemMeta(meta);
        }
        return brandmark;
    }

    private ItemStack createLogItem(ContainerLog log) {
        boolean wasAdded = log.getQuantityChange() > 0;
        Material material = Material.matchMaterial(log.getItemMaterial());
        if (material == null || material.isAir()) {
            material = Material.PAPER;
        }
        
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            NamedTextColor titleColor = wasAdded ? NamedTextColor.GREEN : NamedTextColor.RED;
            String action = wasAdded ? "Added" : "Removed";
            
            Component prefix = Component.text(action + ": ", titleColor);
            Component itemName;

            if (log.getItemDisplayName() != null) {
                itemName = LegacyComponentSerializer.legacyAmpersand().deserialize(log.getItemDisplayName());
            } else {
                itemName = Component.text(log.getItemMaterial());
            }

            meta.displayName(prefix.append(itemName).decoration(TextDecoration.ITALIC, false));

            if (log.getEnchantments() != null && !log.getEnchantments().isEmpty()) {
                String[] enchantPairs = log.getEnchantments().split(",");
                for (String pair : enchantPairs) {
                    String[] parts = pair.split(":");
                    if (parts.length == 2) {
                        Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(parts[0]));
                        if (ench != null) {
                            try {
                                int level = Integer.parseInt(parts[1]);
                                meta.addEnchant(ench, level, true);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }

            List<Component> lore = new ArrayList<>();
            if (log.getItemDisplayName() != null) {
                lore.add(Component.text("Base: " + log.getItemMaterial(), NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
            }
            lore.add(Component.text("Amount: " + Math.abs(log.getQuantityChange()), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("By: ", NamedTextColor.GRAY)
                    .append(Component.text(log.getPlayerName(), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Date: " + dateFormat.format(log.getTimestamp()), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("(" + formatTimeAgo(log.getTimestamp()) + ")", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createNavigationItem(Material material, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component displayName = Component.text(name, color)
                    .decoration(TextDecoration.ITALIC, false);
            meta.displayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatTimeAgo(Date date) {
        if (date == null) {
            return "a long time ago";
        }

        long diffMillis = System.currentTimeMillis() - date.getTime();
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis);

        if (totalSeconds < 1) {
            return "Just now";
        }
        if (totalSeconds < 60) {
            return totalSeconds + "s ago";
        }
        if (totalSeconds < 3600) {
            return (totalSeconds / 60) + "m ago";
        }
        if (totalSeconds < 86400) {
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            return hours + "h " + minutes + "m ago";
        }
        
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        return days + "d " + hours + "h ago";
    }
}
