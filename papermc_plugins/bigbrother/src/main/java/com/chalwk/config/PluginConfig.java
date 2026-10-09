// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.BigBrother;
import com.chalwk.util.SpyType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
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
    private String stateEnabled = "enabled";
    private String stateDisabled = "disabled";
    private String helpHeaderMsg = "&6&lBigBrother Help";
    private String helpToggleAllMsg = "&e/bigbrother &7- Toggle all spy features";
    private String helpStatusMsg = "&e/bigbrother status &7- Check your spy status";
    private String helpSpyEntryMsg = "&e/bigbrother {command} [player] &7- Toggle {spy} spy";
    private String helpReloadMsg = "&e/bigbrother reload &7- Reload configuration";
    private String helpHelpMsg = "&e/bigbrother help &7- Show this help";

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
            excludedCommands = lowercase(filters.getStringList("excluded_commands"));
            excludedPlayers = lowercase(filters.getStringList("excluded_players"));
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
            stateEnabled = messages.getString("state_enabled", stateEnabled);
            stateDisabled = messages.getString("state_disabled", stateDisabled);
            helpHeaderMsg = messages.getString("help_header", helpHeaderMsg);
            helpToggleAllMsg = messages.getString("help_toggle_all", helpToggleAllMsg);
            helpStatusMsg = messages.getString("help_status", helpStatusMsg);
            helpSpyEntryMsg = messages.getString("help_spy_entry", helpSpyEntryMsg);
            helpReloadMsg = messages.getString("help_reload", helpReloadMsg);
            helpHelpMsg = messages.getString("help_help", helpHelpMsg);
        }
    }

    private static List<String> lowercase(List<String> input) {
        List<String> out = new ArrayList<>(input.size());
        for (String s : input) {
            out.add(s.toLowerCase(Locale.ROOT));
        }
        return out;
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
        String lowered = command.toLowerCase(Locale.ROOT);
        for (String excluded : excludedCommands) {
            if (lowered.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerExcluded(String playerName) {
        return excludedPlayers.contains(playerName.toLowerCase(Locale.ROOT));
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

    public String getStateEnabled() {
        return stateEnabled;
    }

    public String getStateDisabled() {
        return stateDisabled;
    }

    public String getHelpHeaderMsg() {
        return helpHeaderMsg;
    }

    public String getHelpToggleAllMsg() {
        return helpToggleAllMsg;
    }

    public String getHelpStatusMsg() {
        return helpStatusMsg;
    }

    public String getHelpSpyEntryMsg() {
        return helpSpyEntryMsg;
    }

    public String getHelpReloadMsg() {
        return helpReloadMsg;
    }

    public String getHelpHelpMsg() {
        return helpHelpMsg;
    }
}