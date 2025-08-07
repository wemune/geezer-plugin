package com.wem.geezer.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;

@DatabaseTable(tableName = "container_logs")
public class ContainerLog {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, index = true)
    private UUID containerUUID;

    @DatabaseField(canBeNull = false)
    private UUID playerUUID;

    @DatabaseField(canBeNull = false)
    private String playerName;

    @DatabaseField(canBeNull = false)
    private String itemMaterial;

    @DatabaseField
    private String itemDisplayName;

    @DatabaseField
    private String itemLore;

    @DatabaseField
    private String enchantments;

    @DatabaseField(canBeNull = false)
    private int quantityChange;

    @DatabaseField(canBeNull = false)
    private Date timestamp;

    public ContainerLog() {
    }

    public ContainerLog(UUID containerUUID, UUID playerUUID, String playerName, String itemMaterial, String itemDisplayName, String itemLore, String enchantments, int quantityChange, Date timestamp) {
        this.containerUUID = containerUUID;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.itemMaterial = itemMaterial;
        this.itemDisplayName = itemDisplayName;
        this.itemLore = itemLore;
        this.enchantments = enchantments;
        this.quantityChange = quantityChange;
        this.timestamp = timestamp;
    }

    public UUID getContainerUUID() { return containerUUID; }
    public UUID getPlayerUUID() { return playerUUID; }
    public String getPlayerName() { return playerName; }
    public String getItemMaterial() { return itemMaterial; }
    public String getItemDisplayName() { return itemDisplayName; }
    public String getEnchantments() { return enchantments; }
    public int getQuantityChange() { return quantityChange; }
    public Date getTimestamp() { return timestamp; }
}
