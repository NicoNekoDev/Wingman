package ro.nicuch.wingman;


import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

public class WingmanPlayerData {

    private final UUID uuid;
    private final String name;
    private long flyTime;
    private boolean fallProtection;

    public WingmanPlayerData(UUID uuid, String name, int flyTime) {
        this.uuid = uuid;
        this.name = name;
        this.flyTime = flyTime;
    }

    public UUID getPlayerUUID() {
        return this.uuid;
    }

    public String getPlayerName() {
        return this.name;
    }

    public long getFlyTime() {
        return this.flyTime;
    }

    public boolean addFlyTime(long flyTime) {
        if (flyTime < 0)
            return false;
        this.flyTime += flyTime;
        return true;
    }

    public boolean removeFlyTime(long flyTime) {
        if (flyTime < 0)
            return false;
        if (flyTime > this.flyTime)
            this.flyTime = 0;
        this.flyTime -= flyTime;
        return true;
    }

    public void resetFlyTime() {
        this.flyTime = 0;
    }

    protected void tickFlyTime() {
        if (this.flyTime < 0)
            this.flyTime = 0;
        if (this.flyTime == 0)
            return;
        this.flyTime--;
    }

    protected void save() {
        WingmanPlugin plugin = ((WingmanPlugin) Bukkit.getPluginManager().getPlugin("Wingman"));
        try {
            if (plugin.getSettings().getBoolean("settings.online-mode", true))
                plugin.getAPI().getConnection().prepareStatement("UPDATE " + plugin.getSettings().getString("database.table_prefix", "") + "flytime SET time='" + this.flyTime + "' WHERE uuid='" + this.uuid.toString() + "';").executeUpdate();
            else
                plugin.getAPI().getConnection().prepareStatement("UPDATE " + plugin.getSettings().getString("database.table_prefix", "") + "flytime SET time='" + this.flyTime + "' WHERE name='" + this.name + "';").executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setFallProtection() {
        this.fallProtection = true;
    }

    public void removeFallProtection() {
        this.fallProtection = false;
    }

    public boolean hasFallProtection() {
        return this.fallProtection;
    }
}
