// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.GameModeManager;
import org.bukkit.configuration.ConfigurationSection;

public class PluginConfig {
    private String noPermissionMsg = "&cYou don't have permission to use this feature!";
    private String reloadedMsg = "&aConfiguration reloaded!";

    public PluginConfig(GameModeManager plugin) {
    }

    public void loadFromConfig(ConfigurationSection config) {
        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages != null) {
            noPermissionMsg = messages.getString("no_permission", noPermissionMsg);
            reloadedMsg = messages.getString("reloaded", reloadedMsg);
        }
    }

    public String getNoPermissionMsg() {
        return noPermissionMsg;
    }

    public String getReloadedMsg() {
        return reloadedMsg;
    }
}