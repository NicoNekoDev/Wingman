package ro.nicuch.wingman;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WingmanFlyCommand implements TabExecutor {
    private final WingmanPlugin plugin;
    private final WingmanAPI api;

    public WingmanFlyCommand(WingmanPlugin plugin) {
        api = (this.plugin = plugin).getAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            boolean toggle;
            if (this.api.hasPermission(sender, "wingman.command.fly.other")) {
                Optional<Player> player = Optional.ofNullable(Bukkit.getPlayer(args[0]));
                if (player.isPresent()) {
                    if (args.length > 1) {
                        if ("on".equals(args[1]))
                            toggle = true;
                        else if ("off".equals(args[1]))
                            toggle = false;
                        else
                            toggle = !player.get().getAllowFlight();
                    } else
                        toggle = !player.get().getAllowFlight();
                    player.get().setAllowFlight(toggle);
                    sender.sendMessage(toggle ? this.plugin.getMessage("lang.fly_enabled_for_other", ConfigDefaults.fly_enabled_for_other).replace("%player_name%", player.get().getName())
                            : this.plugin.getMessage("lang.fly_disabled_for_other", ConfigDefaults.fly_disabled_for_other).replace("%player_name%", player.get().getName()));
                } else
                    sender.sendMessage(this.plugin.getMessage("lang.player_not_found", ConfigDefaults.player_not_found));
            } else
                sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
        } else {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(this.plugin.getMessage("lang.console_cannot_use_command", ConfigDefaults.console_cannot_use_command));
                return true;
            }
            if (this.api.hasPermission(sender, "wingman.command.fly")) {
                Player player = (Player) sender;
                if (this.api.hasPermission(sender, "wingman.fly.permanent")) {
                    boolean toggle = !player.getAllowFlight();
                    player.setAllowFlight(toggle);
                    sender.sendMessage(toggle ? this.plugin.getMessage("lang.fly_enabled", ConfigDefaults.fly_enabled)
                            : this.plugin.getMessage("lang.fly_disabled", ConfigDefaults.fly_disabled));
                } else {
                    Optional<WingmanPlayerData> data = Optional.ofNullable(this.api.getPlayerData(player.getUniqueId()));
                    if (data.isPresent()) {
                        if (data.get().getFlyTime() > 0) {
                            boolean toggle = !player.getAllowFlight();
                            player.setAllowFlight(toggle);
                            sender.sendMessage(toggle ? this.plugin.getMessage("lang.fly_enabled", ConfigDefaults.fly_enabled)
                                    : this.plugin.getMessage("lang.fly_disabled", ConfigDefaults.fly_disabled));
                        } else
                            sender.sendMessage(this.plugin.getMessage("lang.flytime_not_enough", ConfigDefaults.flytime_not_enough));
                    } else {
                        sender.sendMessage(ChatColor.RED + "Wingman generated an error. This should not happen. Please contact an Administrator ASAP and tell him about this!");
                        this.plugin.getLogger().severe("Player data was not loaded but the player is online.");
                        this.plugin.getLogger().severe("This means that there's an error with your database or with the plugin!");
                        this.plugin.getLogger().severe("Please contact nicuch on Spigot ASAP!");
                    }
                }
            } else
                sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();

        if (args.length == 1) {
            if (this.api.hasPermission(sender, "wingman.command.fly.other"))
                Bukkit.getOnlinePlayers().forEach(p -> commands.add(p.getName()));
            if (this.api.hasPermission(sender, "wingman.command.fly")) {
                commands.add("on");
                commands.add("off");
            }
            StringUtil.copyPartialMatches(args[0], commands, completions);
        }
        if (args.length == 2) {
            if (this.api.hasPermission(sender, "wingman.command.fly.other")) {
                commands.add("on");
                commands.add("off");
            }
            StringUtil.copyPartialMatches(args[0], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }
}
