package com.wem.geezer.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;

@DatabaseTable(tableName = "player_stats")
public class PlayerStats {

    @DatabaseField(id = true, canBeNull = false)
    private UUID playerUUID;

    @DatabaseField(defaultValue = "0")
    private long playTimeSeconds;

    @DatabaseField
    private Date lastSeen;

    @DatabaseField(defaultValue = "0")
    private int mobsKilled;

    @DatabaseField(defaultValue = "0")
    private int deaths;

    @DatabaseField(defaultValue = "0")
    private long diamondOreMined;
    @DatabaseField(defaultValue = "0")
    private long ancientDebrisMined;
    @DatabaseField(defaultValue = "0")
    private long goldOreMined;
    @DatabaseField(defaultValue = "0")
    private long ironOreMined;
    @DatabaseField(defaultValue = "0")
    private long emeraldOreMined;
    @DatabaseField(defaultValue = "0")
    private long lapisOreMined;
    @DatabaseField(defaultValue = "0")
    private long redstoneOreMined;
    @DatabaseField(defaultValue = "0")
    private long coalOreMined;


    public PlayerStats() {
    }

    public PlayerStats(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() { return playerUUID; }
    public long getPlayTimeSeconds() { return playTimeSeconds; }
    public Date getLastSeen() { return lastSeen; }
    public int getMobsKilled() { return mobsKilled; }
    public int getDeaths() { return deaths; }
    public long getDiamondOreMined() { return diamondOreMined; }
    public long getAncientDebrisMined() { return ancientDebrisMined; }
    public long getGoldOreMined() { return goldOreMined; }
    public long getIronOreMined() { return ironOreMined; }
    public long getEmeraldOreMined() { return emeraldOreMined; }
    public long getLapisOreMined() { return lapisOreMined; }
    public long getRedstoneOreMined() { return redstoneOreMined; }
    public long getCoalOreMined() { return coalOreMined; }

    public void setLastSeen(Date lastSeen) { this.lastSeen = lastSeen; }
    public void incrementPlayTime(long seconds) { this.playTimeSeconds += seconds; }
    public void incrementMobsKilled() { this.mobsKilled++; }
    public void incrementDeaths() { this.deaths++; }
    public void incrementDiamondOreMined() { this.diamondOreMined++; }
    public void incrementAncientDebrisMined() { this.ancientDebrisMined++; }
    public void incrementGoldOreMined() { this.goldOreMined++; }
    public void incrementIronOreMined() { this.ironOreMined++; }
    public void incrementEmeraldOreMined() { this.emeraldOreMined++; }
    public void incrementLapisOreMined() { this.lapisOreMined++; }
    public void incrementRedstoneOreMined() { this.redstoneOreMined++; }
    public void incrementCoalOreMined() { this.coalOreMined++; }
}