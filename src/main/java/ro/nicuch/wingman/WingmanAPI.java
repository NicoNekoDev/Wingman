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
            compile("(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" +
                    "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" +
                    "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);

    private Connection connection;
    private final WingmanPlugin plugin;
    private final Map<UUID, WingmanPlayerData> data = new HashMap<>();

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

    protected boolean loadPlayerData(Player player) {
        try {
            ResultSet rs = this.connection
                    .prepareStatement(
                            "SELECT time FROM " + this.plugin.getSettings().getString("database.table_prefix", "") + "flytime WHERE uuid='" + player.getUniqueId().toString() + "';")
                    .executeQuery();
            if (!rs.next()) {
                this.connection.prepareStatement("INSERT INTO " + this.plugin.getSettings().getString("database.table_prefix", "") + "flytime(uuid, time) VALUES('"
                        + player.getUniqueId().toString() + "', '" + 0 + "');").executeUpdate();
                this.data.put(player.getUniqueId(), new WingmanPlayerData(player.getUniqueId(), 0));
            } else
                this.data.put(player.getUniqueId(), new WingmanPlayerData(player.getUniqueId(), rs.getInt("time")));
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean unloadPlayerData(Player player) {
        WingmanPlayerData playerData = this.data.get(player.getUniqueId());
        try {
            this.connection.prepareStatement("UPDATE " + plugin.getSettings().getString("database.table_prefix", "") + "flytime SET time='" + playerData.getFlyTime() + "' WHERE uuid='" + player.getUniqueId().toString() + "';").executeUpdate();
            this.data.remove(player.getUniqueId());
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            this.data.remove(player.getUniqueId());
            return false;
        }
    }

    protected boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player) {
            Optional<Permission> vaultPerms = this.plugin.useVault() ? Optional.of(this.plugin.getVaultPerms()) : Optional.empty(); //If vault not enabled or luckperms is used, this will return empty
            return (vaultPerms.isPresent() && vaultPerms.get().has(sender, permission)) || sender.hasPermission(permission);
        }
        return true;
    }

    public String secondsToString(long time, String str) { //str is the text to be formatted
        return str.replace("%hours%", String.format("%02d", time / 3600))
                .replace("%minutes%", String.format("%02d", (time % 3600) / 60))
                .replace("%seconds%", String.format("%02d", ((time % 3600) % 60) % 60));
    }

    public WingmanPlayerData getPlayerData(UUID uuid) {
        if (this.data.containsKey(uuid))
            return this.data.get(uuid);
        else {
            try {
                ResultSet rs = this.connection
                        .prepareStatement(
                                "SELECT time FROM " + this.plugin.getSettings().getString("database.table_prefix", "") + "flytime WHERE uuid='" + uuid.toString() + "';")
                        .executeQuery();
                if (rs.next()) {
                    return new WingmanPlayerData(uuid, rs.getInt("time"));
                } else
                    return null;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public WingmanPlayerData getPlayerData(OfflinePlayer player) {
        return this.getPlayerData(player.getUniqueId());
    }

    public Set<WingmanPlayerData> getPlayerDataForOnlinePlayers() {
        return new HashSet<>(this.data.values());
    }

    private Class<?> getNmsClass(String nmsClassName) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + nmsClassName);
    }

    private String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    protected void sendAction(Player p, String msg) {
        IChatBaseComponent cs = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + msg + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cs, ChatMessageType.GAME_INFO);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
    }

    public long parseDateDiff(String time) {
        Matcher m = this.timePattern.matcher(time);
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
                    months = Integer.parseInt(m.group(1));
                }
                if (m.group(2) != null && !m.group(2).isEmpty()) {
                    days = Integer.parseInt(m.group(2));
                }
                if (m.group(3) != null && !m.group(3).isEmpty()) {
                    hours = Integer.parseInt(m.group(3));
                }
                if (m.group(4) != null && !m.group(4).isEmpty()) {
                    minutes = Integer.parseInt(m.group(4));
                }
                if (m.group(5) != null && !m.group(5).isEmpty()) {
                    seconds = Integer.parseInt(m.group(5));
                }
                break;
            }
        }
        return (months * 2592000) + (days * 86400) + (hours * 3600) + (minutes * 60) + seconds;
    }
}
