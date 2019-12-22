package ro.nicuch.wingman;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WingmanListeners implements Listener {
    private final WingmanAPI api;

    public WingmanListeners(WingmanPlugin plugin) {
        this.api = plugin.getAPI();
    }

    @EventHandler
    public void joinEvent(PlayerJoinEvent event) {
        if (this.api.loadPlayerData(event.getPlayer()))
            if (this.api.getPlayerData(event.getPlayer()).getFlyTime() > 0) event.getPlayer().setFlying(true);
    }

    @EventHandler
    public void quitEvent(PlayerQuitEvent event) {
        this.api.unloadPlayerData(event.getPlayer());
    }
}
