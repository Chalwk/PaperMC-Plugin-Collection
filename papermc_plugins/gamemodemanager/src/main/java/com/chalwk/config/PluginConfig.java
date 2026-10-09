// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.GameModeManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;

/**
 * Parsed view of config.yml. Fields default to the same values as the
 * bundled config so a missing section doesn't break anything.
 */
public class PluginConfig {
    private String noPermissionMsg = "&cYou don't have permission to use this feature!";
    private String reloadedMsg = "&aConfiguration reloaded!";
    private String unknownSubcommandMsg = "&cUnknown subcommand. Use &6/gmmanage help";
    private List<String> helpLines = List.of(
            "&6/gmmanage reload &7- Reload the configuration",
            "&6/gmmanage help &7- Show this help"
    );

    public PluginConfig(GameModeManager plugin) {}

    public void loadFromConfig(ConfigurationSection config) {
        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages == null) return;

        noPermissionMsg = messages.getString("no_permission", noPermissionMsg);
        reloadedMsg = messages.getString("reloaded", reloadedMsg);
        unknownSubcommandMsg = messages.getString("unknown_subcommand", unknownSubcommandMsg);

        List<String> lines = messages.getStringList("help");
        if (!lines.isEmpty()) {
            // Immutable so callers can't accidentally mutate the config list.
            helpLines = Collections.unmodifiableList(lines);
        }
    }

    public String getNoPermissionMsg() {
        return noPermissionMsg;
    }

    public String getReloadedMsg() {
        return reloadedMsg;
    }

    public String getUnknownSubcommandMsg() {
        return unknownSubcommandMsg;
    }

    public List<String> getHelpLines() {
        return helpLines;
    }
}