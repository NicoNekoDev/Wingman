package ro.nicuch.wingman;

import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_15_R1.ChatMessageType;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WingmanAPI {
    private final Pattern timePattern = Pattern.
            compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" +
                    "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" +
                    "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);

    private Connection connection;
    private final WingmanPlugin plugin;
    private final Map<UUID, WingmanPlayerData> online_data = new HashMap<>();
    private final Map<String, WingmanPlayerData> offline_data = new HashMap<>();

    public WingmanAPI(WingmanPlugin plugin) {
        this.plugin = plugin;
        this.startDatabase();
        Bukkit.getOnlinePlayers().forEach(this::loadPlayerData);
    }

    protected void startDatabase() {
        YamlConfiguration settings = this.plugin.getSettings();
        File file = new File(this.plugin.getDataFolder() + File.separator + settings.getString("database.file_name", "wingman.db"));
        String host = settings.getString("database.host", "localhost");
        String port = settings.getString("database.port", "3306");
        String name = settings.getString("database.name", "name");
        String user = settings.getString("database.user", "user");
        String password = settings.getString("database.password", "");
        boolean ssl = settings.getBoolean("database.ssl", true); //default = true
        String url = settings.getString("database.type", "SQL").equalsIgnoreCase("MYSQL") ? ("jdbc:mysql://" + host + ":" + port + "/" + name + "?user=" + user + (password.isEmpty() ? "" : "&password=" + password) + "&useSSL=" + ssl + "&autoReconnect=true") : "jdbc:sqlite:" + file.getAbsolutePath();
        try {
            this.connection = DriverManager.getConnection(url);
            this.connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + settings.getString("database.table_prefix", "") + "flytime(uuid VARCHAR(36) PRIMARY KEY, time BIGINT);").executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void stopDatabase() {
        try {
            if (this.connection != null) this.connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected Connection getConnection() {
        return this.connection;
    }

    protected void loadPlayerData(Player player) {
        if (this.plugin.getSettings().getBoolean("settings.async", true)) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try {
                    if (this.plugin.getSettings().getBoolean("settings.online-mode", true)) {
                        ResultSet rs = this.connection
                                .prepareStatement(
                                        "SELECT time FROM " + this.plugin.getSettings().getString("database.table_prefix", "Wingman_") + "flytime WHERE uuid='" + player.getUniqueId().toString() + "';")
                                .executeQuery();
                        if (!rs.next()) {
                            this.connection.prepareStatement("INSERT INTO " + this.plugin.getSettings().getString("database.table_prefix", "") + "flytime(uuid, name, time) VALUES('"
                                    + player.getName() + "', '" + player.getUniqueId().toString() + "', '" + 0 + "');").executeUpdate();
                            this.online_data.put(player.getUniqueId(), new WingmanPlayerData(player.getUniqueId(), player.getName(), 0));
                        } else
                            this.online_data.put(player.getUniqueId(), new WingmanPlayerData(player.getUniqueId(), player.getName(), rs.getInt("time")));
                    } else {
                        ResultSet rs = this.connection
                                .prepareStatement(
                                        "SELECT time FROM " + this.plugin.getSettings().getString("database.table_prefix", "Wingman_") + "flytime WHERE name='" + player.getName() + "';")
                                .executeQuery();
                        if (!rs.next()) {
                            this.connection.prepareStatement("INSERT INTO " + this.plugin.getSettings().getString("database.table_prefix", "") + "flytime(uuid, name, time) VALUES('"
                                    + player.getName() + "', '" + player.getUniqueId().toString() + "', '" + 0 + "');").executeUpdate();
                            this.offline_data.put(player.getName(), new WingmanPlayerData(player.getUniqueId(), player.getName(), 0));
                        } else
                            this.offline_data.put(player.getName(), new WingmanPlayerData(player.getUniqueId(), player.getName(), rs.getInt("time")));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        } else {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                try {
                    if (this.plugin.getSettings().getBoolean("settings.online-mode", true)) {
                        ResultSet rs = this.connection
                                .prepareStatement(
                                        "SELECT time FROM " + this.plugin.getSettings().getString("database.table_prefix", "Wingman_") + "flytime WHERE uuid='" + player.getUniqueId().toString() + "';")
                                .executeQuery();
                        if (!rs.next()) {
                            this.connection.prepareStatement("INSERT INTO " + this.plugin.getSettings().getString("database.table_prefix", "") + "flytime(uuid, name, time) VALUES('"
                                    + player.getName() + "', '" + player.getUniqueId().toString() + "', '" + 0 + "');").executeUpdate();
                            this.online_data.put(player.getUniqueId(), new WingmanPlayerData(player.getUniqueId(), player.getName(), 0));
                        } else
                            this.online_data.put(player.getUniqueId(), new WingmanPlayerData(player.getUniqueId(), player.getName(), rs.getInt("time")));
                    } else {
                        ResultSet rs = this.connection
                                .prepareStatement(
                                        "SELECT time FROM " + this.plugin.getSettings().getString("database.table_prefix", "Wingman_") + "flytime WHERE name='" + player.getName() + "';")
                                .executeQuery();
                        if (!rs.next()) {
                            this.connection.prepareStatement("INSERT INTO " + this.plugin.getSettings().getString("database.table_prefix", "") + "flytime(uuid, name, time) VALUES('"
                                    + player.getName() + "', '" + player.getUniqueId().toString() + "', '" + 0 + "');").executeUpdate();
                            this.offline_data.put(player.getName(), new WingmanPlayerData(player.getUniqueId(), player.getName(), 0));
                        } else
                            this.offline_data.put(player.getName(), new WingmanPlayerData(player.getUniqueId(), player.getName(), rs.getInt("time")));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    protected void unloadPlayerData(Player player) {
        if (this.plugin.getSettings().getBoolean("settings.async", true)) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                WingmanPlayerData playerData;
                if (this.plugin.getSettings().getBoolean("settings.online-mode", true)) {
                    playerData = this.online_data.get(player.getUniqueId());
                    try {
                        this.connection.prepareStatement("UPDATE " + plugin.getSettings().getString("database.table_prefix", "Wingman_") + "flytime SET time='" + playerData.getFlyTime() + "' WHERE uuid='" + player.getUniqueId() + "';").executeUpdate();
                        this.online_data.remove(player.getUniqueId());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    playerData = this.offline_data.get(player.getName());
                    try {
                        this.connection.prepareStatement("UPDATE " + plugin.getSettings().getString("database.table_prefix", "Wingman_") + "flytime SET time='" + playerData.getFlyTime() + "' WHERE name='" + player.getName() + "';").executeUpdate();
                        this.offline_data.remove(player.getName());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                WingmanPlayerData playerData;
                if (this.plugin.getSettings().getBoolean("settings.online-mode", true)) {
                    playerData = this.online_data.get(player.getUniqueId());
                    try {
                        this.connection.prepareStatement("UPDATE " + plugin.getSettings().getString("database.table_prefix", "Wingman_") + "flytime SET time='" + playerData.getFlyTime() + "' WHERE uuid='" + player.getUniqueId() + "';").executeUpdate();
                        this.online_data.remove(player.getUniqueId());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    playerData = this.offline_data.get(player.getName());
                    try {
                        this.connection.prepareStatement("UPDATE " + plugin.getSettings().getString("database.table_prefix", "Wingman_") + "flytime SET time='" + playerData.getFlyTime() + "' WHERE name='" + player.getName() + "';").executeUpdate();
                        this.offline_data.remove(player.getName());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }


    }

    protected boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player) {
            Optional<Permission> vaultPerms = this.plugin.useVault() ? Optional.of(this.plugin.getVaultPerms()) : Optional.empty(); //If vault not enabled this will return empty
            return (vaultPerms.isPresent() && vaultPerms.get().has(sender, permission)) || sender.hasPermission(permission);
        }
        return true;
    }

    public String secondsToString(long time, String str) { //str is the text to be formatted
        long total_years = time / 31556926;
        long total_months = time / 2592000; //Rough estimation of a 30 days month
        long total_days = time / 86400;
        long total_hours = time / 3600;
        long total_minutes = time / 60;

        long leftover_years = time % 31556926;
        long leftover_months = leftover_years % 2592000; //Rough estimation of a 30 days month
        long leftover_days = leftover_months % 86400;
        long leftover_minutes = leftover_days % 3600;
        long leftover_seconds = leftover_minutes % 60;

        long rounded_months = leftover_years / 2592000; //Rough estimation of a 30 days month
        long rounded_days = leftover_months / 86400;
        long rounded_hours = leftover_days / 3600;
        long rounded_minutes = leftover_minutes / 60;
        long rounded_seconds = leftover_seconds % 60;

        return str
                .replace("%rounded_months%", String.format("%02d", rounded_months))
                .replace("%rounded_days%", String.format("%02d", rounded_days))
                .replace("%rounded_hours%", String.format("%02d", rounded_hours))
                .replace("%rounded_minutes%", String.format("%02d", rounded_minutes))
                .replace("%rounded_seconds%", String.format("%02d", rounded_seconds))
                .replace("%total_years%", String.format("%02d", total_years))
                .replace("%total_months%", String.format("%02d", total_months))
                .replace("%total_days%", String.format("%02d", total_days))
                .replace("%total_hours%", String.format("%02d", total_hours))
                .replace("%total_minutes%", String.format("%02d", total_minutes))
                .replace("%total_seconds%", String.format("%02d", time));
    }

    public WingmanPlayerData getPlayerDataOnline(UUID uuid) {
        if (this.online_data.containsKey(uuid))
            return this.online_data.get(uuid);
        else {
            try {
                ResultSet rs = this.connection
                        .prepareStatement(
                                "SELECT time FROM " + this.plugin.getSettings().getString("database.table_prefix", "") + "flytime WHERE uuid='" + uuid.toString() + "';")
                        .executeQuery();
                if (rs.next())
                    return new WingmanPlayerData(uuid, null, rs.getInt("time"));
                else
                    return null;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public WingmanPlayerData getPlayerDataOffline(String name) {
        if (this.offline_data.containsKey(name))
            return this.offline_data.get(name);
        else {
            try {
                ResultSet rs = this.connection
                        .prepareStatement(
                                "SELECT time FROM " + this.plugin.getSettings().getString("database.table_prefix", "") + "flytime WHERE name='" + name + "';")
                        .executeQuery();
                if (rs.next())
                    return new WingmanPlayerData(null, name, rs.getInt("time"));
                else
                    return null;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public WingmanPlayerData getPlayerData(OfflinePlayer player) {
        return this.plugin.getSettings().getBoolean("settings.online-mode", true) ? this.getPlayerDataOnline(player.getUniqueId()) : this.getPlayerDataOffline(player.getName());
    }

    public Set<WingmanPlayerData> getAllDataForOnline() {
        return new HashSet<>(this.online_data.values());
    }

    public Set<WingmanPlayerData> getAllDataForOffline() {
        return new HashSet<>(this.offline_data.values());
    }


    //TODO 1.8 to 1.15
    protected void sendActionBar(Player p, String msg) {
        if (!this.plugin.getSettings().getBoolean("settings.flytime_actionbar", true))
            return;
        IChatBaseComponent cs = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + msg + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cs, ChatMessageType.GAME_INFO);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
    }

    // Using Essentials Date parser
    public long parseDateDiff(String time) {
        Matcher m = this.timePattern.matcher(time);
        int years = 0;
        int months = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (m.find()) {
            if (m.group() == null || m.group().isEmpty()) {
                continue;
            }
            for (int i = 0; i < m.groupCount(); i++) {
                if (m.group(i) != null && !m.group(i).isEmpty()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                if (m.group(1) != null && !m.group(1).isEmpty()) {
                    years = Integer.parseInt(m.group(1));
                }
                if (m.group(2) != null && !m.group(2).isEmpty()) {
                    months = Integer.parseInt(m.group(2));
                }
                if (m.group(3) != null && !m.group(3).isEmpty()) {
                    days = Integer.parseInt(m.group(3));
                }
                if (m.group(4) != null && !m.group(4).isEmpty()) {
                    hours = Integer.parseInt(m.group(4));
                }
                if (m.group(5) != null && !m.group(5).isEmpty()) {
                    minutes = Integer.parseInt(m.group(5));
                }
                if (m.group(6) != null && !m.group(6).isEmpty()) {
                    seconds = Integer.parseInt(m.group(6));
                }
                break;
            }
        }
        return (years * 31556926) + (months * 2592000) + (days * 86400) + (hours * 3600) + (minutes * 60) + seconds;
    }
}
