package ro.nicuch.wingman;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class WingmanCommand implements TabExecutor {
    private final WingmanPlugin plugin;
    private final WingmanAPI api;

    public WingmanCommand(WingmanPlugin plugin) {
        api = (this.plugin = plugin).getAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "about":
                    this.sendAbout(sender);
                    break;
                case "add":
                    if (this.api.hasPermission(sender, "wingman.command.add")) {
                        if (args.length > 2) {
                            Optional<OfflinePlayer> offlinePlayer = Optional.ofNullable(Bukkit.getOfflinePlayer(args[1]));
                            if (offlinePlayer.isPresent()) {
                                Optional<WingmanPlayerData> data = Optional.ofNullable(this.api.getPlayerData(offlinePlayer.get().getUniqueId()));
                                if (data.isPresent()) {
                                    data.get().addFlyTime(this.api.parseDateDiff(args[2]));
                                    sender.sendMessage(this.plugin.getMessage("lang.time_set_success", "The time has been added!"));
                                } else
                                    sender.sendMessage(this.plugin.getMessage("lang.player_not_found", ConfigDefaults.player_not_found));
                            } else
                                sender.sendMessage(this.plugin.getMessage("lang.player_not_found", ConfigDefaults.player_not_found));
                        } else
                            sender.sendMessage(this.plugin.getMessage("help.add", ConfigDefaults.help_add));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "remove":
                    if (this.api.hasPermission(sender, "wingman.command.remove")) {
                        if (args.length > 2) {
                            Optional<OfflinePlayer> offlinePlayer = Optional.ofNullable(Bukkit.getOfflinePlayer(args[1]));
                            if (offlinePlayer.isPresent()) {
                                Optional<WingmanPlayerData> data = Optional.ofNullable(this.api.getPlayerData(offlinePlayer.get().getUniqueId()));
                                if (data.isPresent()) {
                                    data.get().removeFlyTime(this.api.parseDateDiff(args[2]));
                                    sender.sendMessage(this.plugin.getMessage("lang.time_remove_success", "The time has been removed!"));
                                } else
                                    sender.sendMessage(this.plugin.getMessage("lang.player_not_found", ConfigDefaults.player_not_found));
                            } else
                                sender.sendMessage(this.plugin.getMessage("lang.player_not_found", ConfigDefaults.player_not_found));
                        } else
                            sender.sendMessage(this.plugin.getMessage("help.remove", ConfigDefaults.help_remove));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "reset":
                    if (this.api.hasPermission(sender, "wingman.command.reset")) {
                        if (args.length > 1) {
                            Optional<OfflinePlayer> offlinePlayer = Optional.ofNullable(Bukkit.getOfflinePlayer(args[1]));
                            if (offlinePlayer.isPresent()) {
                                Optional<WingmanPlayerData> data = Optional.ofNullable(this.api.getPlayerData(offlinePlayer.get().getUniqueId()));
                                if (data.isPresent()) {
                                    data.get().resetFlyTime();
                                    sender.sendMessage(this.plugin.getMessage("lang.time_reset_success", "The time has been reseted!"));
                                } else
                                    sender.sendMessage(this.plugin.getMessage("lang.player_not_found", ConfigDefaults.player_not_found));
                            } else
                                sender.sendMessage(this.plugin.getMessage("lang.player_not_found", ConfigDefaults.player_not_found));
                        } else
                            sender.sendMessage(this.plugin.getMessage("help.reset", ConfigDefaults.help_reset));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "check":
                    if (this.api.hasPermission(sender, "wingman.command.check")) {
                        if (args.length > 1) {
                            if (this.api.hasPermission(sender, "wingman.command.check.other")) {
                                Optional<OfflinePlayer> offlinePlayer = Optional.ofNullable(Bukkit.getOfflinePlayer(args[1]));
                                if (offlinePlayer.isPresent()) {
                                    Optional<WingmanPlayerData> data = Optional.ofNullable(this.api.getPlayerData(offlinePlayer.get().getUniqueId()));
                                    if (data.isPresent()) {
                                        data.get().resetFlyTime();
                                        sender.sendMessage(this.plugin.getMessage("lang.time_reset_success", "The time has been reseted!"));
                                    } else
                                        sender.sendMessage(this.plugin.getMessage("lang.player_not_found", ConfigDefaults.player_not_found));
                                } else
                                    sender.sendMessage(this.plugin.getMessage("lang.player_not_found", ConfigDefaults.player_not_found));
                            } else
                                sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                        } else {
                            WingmanPlayerData data = this.api.getPlayerData(((Player) sender).getUniqueId());
                            sender.sendMessage(this.api.secondsToString(data.getFlyTime(), this.plugin.getMessage("lang.flytime_remaining_format", "&a%hours%:%minutes%:%seconds%")));
                        }
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                default:
                    if (this.api.hasPermission(sender, "wingman.command.help"))
                        this.sendHelp(sender);
                    else
                        this.sendAbout(sender);
                    break;
            }
        } else {
            if (this.api.hasPermission(sender, "wingman.command.help"))
                this.sendHelp(sender);
            else
                this.sendAbout(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();

        if (args.length == 1) {
            if (this.api.hasPermission(sender, "wingman.command.add"))
                commands.add("add");
            if (this.api.hasPermission(sender, "wingman.command.remove"))
                commands.add("remove");
            if (this.api.hasPermission(sender, "wingman.command.reset"))
                commands.add("reset");
            if (this.api.hasPermission(sender, "wingman.command.check"))
                commands.add("check");
            if (this.api.hasPermission(sender, "wingman.command.reload"))
                commands.add("reload");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            switch (args[0]) {
                case "add":
                    if (!this.api.hasPermission(sender, "wingman.command.add"))
                        break;
                case "remove":
                    if (this.api.hasPermission(sender, "wingman.command.remove"))
                        break;
                case "reset":
                    if (this.api.hasPermission(sender, "wingman.command.reset"))
                        break;
                case "check":
                    if (this.api.hasPermission(sender, "wingman.command.check"))
                        break;
                    Bukkit.getOnlinePlayers().forEach(p -> commands.add(p.getName()));
                    break;
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        } else if (args.length == 3) {
            switch (args[0]) {
                case "add":
                    if (!this.api.hasPermission(sender, "wingman.command.add"))
                        break;
                case "remove":
                    if (this.api.hasPermission(sender, "wingman.command.remove"))
                        break;
                    if (args[2] == null || args[2].isEmpty())
                        completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9"));
                    else
                        try {
                            int i = Integer.parseInt(args[2]);
                            completions.addAll(Arrays.asList(i + "0", i + "y", i + "mo", i + "d", i + "m", i + "s"));
                        } catch (NumberFormatException ignore) {
                        }
                    break;
            }
        }
        Collections.sort(completions);
        return completions;
    }

    private void sendAbout(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "<+ Wingman +>");
        sender.sendMessage(ChatColor.GOLD + "Version: " + ChatColor.RED + this.plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GOLD + "Auhtor: " + ChatColor.RED + "nicuch");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
        sender.sendMessage("");
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.about", ConfigDefaults.help_about));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.set", ConfigDefaults.help_add));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.remove", ConfigDefaults.help_remove));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.reset", ConfigDefaults.help_reset));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.check", ConfigDefaults.help_check));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.reload", ConfigDefaults.help_reload));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
    }
}
