/*

   CitizensBooks
   Copyright (c) 2018 @ Drăghiciu 'nicuch' Nicolae

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package ro.nicuch.wingman;

import java.util.Arrays;
import java.util.List;

public class ConfigDefaults {
    public static final String header = "&f[&6Wingman&f] &r";
    public static final String no_permission = "&cYou don't have permission!";
    public static final String config_reloaded = "&aConfig reloaded.";
    public static final String console_cannot_use_command = "&cYou have to be a player if you want to use this command!";
    public static final String new_version_available = "&aA new version of Wingman is available!";
    public static final String player_not_found = "&cPlayer not found!";

    public static final String usage_add = "&f&oUsage&f: &6/wman add <player> <time>";
    public static final String usage_remove = "&f&oUsage&f: &6/wman remove <player> <time>";
    public static final String usage_reset = "&f&oUsage&f: &6/wman reset <player/*/**> [time]";
    public static final String usage_check = "&f&oUsage&f: &6/wman check [player] &8or &6/flytime [player]";
    public static final String usage_fly = "&f&oUsage&f: &f/fly [player] [on/off]";

    public static final List<String> help = Arrays.asList(
            "&m        &f[&6Wingman Help&f]&m        ",
            "&8> &6/wman about",
            "&f&oInformations about plugin",
            "&8> &6/wman add <player> <time>",
            "&f&oAdds flytime to a player",
            "&8> &6/wman remove <player> <time>",
            "&f&oRemoves flytime of a player",
            "&8> &6/wman reset <player/*/**> [time]",
            "&f&oResets a player (* for all online players, ** for all online and offline players) flytime to 0 (or the given time)",
            "&8> &6/wman check [player] &8or &6/flytime [player]",
            "&f&oChecks your (or a given player) flytime",
            "&8> &6/wman reload",
            "&f&oReload the config file",
            "&8> &6/fly [player] [on/off]",
            "&f&oToggle fly for you (or a give player)",
            "&m                                      "
    );

    public static final String flytime_not_enough = "&cYou don't have enough flytime.";
    public static final String fly_enabled = "&aYou activated fly.";
    public static final String fly_disabled = "&cYou dezactivated fly.";
    public static final String fly_enabled_for_other = "&aYou activated fly for %player_name%.";
    public static final String fly_disabled_for_other = "&cYou dezactivated fly for %player_name%.";
    public static final String fly_deactivated = "&cYou run out of flytime!";
    public static final String flytime_check = "&aFlytime left: &b%total_hours%h:%rounded_minutes%m:%rounded_seconds%s";
    public static final String flytime_remaining_format = "&aFlytime left: &b%total_hours%h:%rounded_minutes%m:%rounded_seconds%s";
    public static final String flytime_permanent_format = "&aYour flytime is permanent.";
    public static final String flytime_set_success = "&aThe time has been added!";
    public static final String flytime_remove_success = "&aThe time has been removed!";
    public static final String flytime_reset_success = "The time has been reseted!";
}