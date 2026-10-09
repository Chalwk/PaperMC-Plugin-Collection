// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.BigBrother;
import com.chalwk.util.SpyType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PluginConfig {

    private boolean globalToggle = true;
    private boolean enabledByDefault = true;

    private final Map<SpyType, Boolean> spyEnabled = new EnumMap<>(SpyType.class);
    private final Map<SpyType, String> spyMessages = new EnumMap<>(SpyType.class);

    private List<String> excludedCommands = new ArrayList<>();
    private List<String> excludedPlayers = new ArrayList<>();
    private List<String> excludedWorlds = new ArrayList<>();

    private String noPermissionMsg = "&cYou don't have permission to use this feature!";
    private String playersOnlyMsg = "&cOnly players can use this command!";
    private String playerNotFoundMsg = "&cPlayer not found!";
    private String reloadedMsg = "&aConfiguration reloaded!";
    private String unknownCommandMsg = "&cUnknown command. Use /bigbrother help.";
    private String globalEnabledMsg = "&aBigBrother enabled! Use subcommands to configure specific spy features.";
    private String globalDisabledMsg = "&cBigBrother disabled! All spy features are now off.";
    private String statusHeaderMsg = "&aEnabled spy features:";
    private String statusEmptyMsg = "&7No spy features are currently enabled.";
    private String statusEntryMsg = "&7- &e{spy}";
    private String spyToggledSelfMsg = "&a{spy} spy has been {state}!";
    private String spyToggledOtherMsg = "&a{spy} spy has been {state} for {player}!";

    public PluginConfig(BigBrother plugin) {
        for (SpyType type : SpyType.values()) {
            spyEnabled.put(type, true);
            spyMessages.put(type, "&7[&eBigBrother&7] &f{player} &7{action}");
        }
    }

    public void loadFromConfig(ConfigurationSection config) {
        globalToggle = config.getBoolean("global_toggle", globalToggle);
        enabledByDefault = config.getBoolean("enabled_by_default", enabledByDefault);

        ConfigurationSection spy = config.getConfigurationSection("spy");
        if (spy != null) {
            for (SpyType type : SpyType.values()) {
                ConfigurationSection typeSection = spy.getConfigurationSection(type.getConfigKey());
                if (typeSection != null) {
                    spyEnabled.put(type, typeSection.getBoolean("enabled", true));
                    spyMessages.put(type, typeSection.getString("message", spyMessages.get(type)));
                }
            }
        }

        ConfigurationSection filters = config.getConfigurationSection("filters");
        if (filters != null) {
            excludedCommands = new ArrayList<>(filters.getStringList("excluded_commands"));
            excludedPlayers = new ArrayList<>(filters.getStringList("excluded_players"));
            excludedWorlds = new ArrayList<>(filters.getStringList("excluded_worlds"));
        }

        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages != null) {
            noPermissionMsg = messages.getString("no_permission", noPermissionMsg);
            playersOnlyMsg = messages.getString("players_only", playersOnlyMsg);
            playerNotFoundMsg = messages.getString("player_not_found", playerNotFoundMsg);
            reloadedMsg = messages.getString("reloaded", reloadedMsg);
            unknownCommandMsg = messages.getString("unknown_command", unknownCommandMsg);
            globalEnabledMsg = messages.getString("global_enabled", globalEnabledMsg);
            globalDisabledMsg = messages.getString("global_disabled", globalDisabledMsg);
            statusHeaderMsg = messages.getString("status_header", statusHeaderMsg);
            statusEmptyMsg = messages.getString("status_empty", statusEmptyMsg);
            statusEntryMsg = messages.getString("status_entry", statusEntryMsg);
            spyToggledSelfMsg = messages.getString("spy_toggled_self", spyToggledSelfMsg);
            spyToggledOtherMsg = messages.getString("spy_toggled_other", spyToggledOtherMsg);
        }
    }

    public boolean isGlobalEnabled() {
        return globalToggle;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public boolean isSpyEnabled(SpyType type) {
        return spyEnabled.getOrDefault(type, true);
    }

    public String getSpyMessage(SpyType type) {
        return spyMessages.getOrDefault(type, "&7[&eBigBrother&7] &f{player} &7{action}");
    }

    public boolean isCommandExcluded(String command) {
        for (String excluded : excludedCommands) {
            if (command.toLowerCase().startsWith(excluded.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerExcluded(String playerName) {
        return excludedPlayers.contains(playerName.toLowerCase());
    }

    public boolean isWorldExcluded(String worldName) {
        return excludedWorlds.contains(worldName);
    }

    public String getNoPermissionMsg() {
        return noPermissionMsg;
    }

    public String getPlayersOnlyMsg() {
        return playersOnlyMsg;
    }

    public String getPlayerNotFoundMsg() {
        return playerNotFoundMsg;
    }

    public String getReloadedMsg() {
        return reloadedMsg;
    }

    public String getUnknownCommandMsg() {
        return unknownCommandMsg;
    }

    public String getGlobalEnabledMsg() {
        return globalEnabledMsg;
    }

    public String getGlobalDisabledMsg() {
        return globalDisabledMsg;
    }

    public String getStatusHeaderMsg() {
        return statusHeaderMsg;
    }

    public String getStatusEmptyMsg() {
        return statusEmptyMsg;
    }

    public String getStatusEntryMsg() {
        return statusEntryMsg;
    }

    public String getSpyToggledSelfMsg() {
        return spyToggledSelfMsg;
    }

    public String getSpyToggledOtherMsg() {
        return spyToggledOtherMsg;
    }
}