// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PluginConfig {

    private int interval = 600;
    private final List<List<String>> broadcasts = new ArrayList<>();

    private String noPermissionMsg = "&cYou don't have permission to use this feature!";
    private String reloadedMsg = "&aConfiguration reloaded and scheduler restarted!";
    private String unknownCommandMsg = "&cUnknown command. Use /automessages help.";
    private String statusHeaderMsg = "&eAutoMessages Status:";
    private String statusIntervalMsg = "&7- Interval: &f{interval}s";
    private String statusTotalMsg = "&7- Total messages: &f{total}";
    private String statusNextMsg = "&7- Next message index: &f{next}/{total}";

    private List<String> helpMsg = List.of(
            "&6&lAutoMessages Help",
            "&e/automessages status &7- Show current status",
            "&e/automessages reload &7- Reload configuration and restart scheduler",
            "&e/automessages help &7- Show this help");

    public void loadFromConfig(ConfigurationSection config) {
        interval = config.getInt("interval", interval);

        broadcasts.clear();
        ConfigurationSection broadcastsSection = config.getConfigurationSection("broadcasts");
        if (broadcastsSection != null) {
            Map<Integer, List<String>> sorted = new TreeMap<>();
            for (String key : broadcastsSection.getKeys(false)) {
                try {
                    int index = Integer.parseInt(key);
                    List<String> lines = broadcastsSection.getStringList(key);
                    if (!lines.isEmpty()) {
                        sorted.put(index, List.copyOf(lines));
                    }
                } catch (NumberFormatException ignored) {
                    // Skip non-numeric keys
                }
            }
            broadcasts.addAll(sorted.values());
        }

        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            noPermissionMsg = messagesSection.getString("no_permission", noPermissionMsg);
            reloadedMsg = messagesSection.getString("reloaded", reloadedMsg);
            unknownCommandMsg = messagesSection.getString("unknown_command", unknownCommandMsg);
            statusHeaderMsg = messagesSection.getString("status_header", statusHeaderMsg);
            statusIntervalMsg = messagesSection.getString("status_interval", statusIntervalMsg);
            statusTotalMsg = messagesSection.getString("status_total", statusTotalMsg);
            statusNextMsg = messagesSection.getString("status_next", statusNextMsg);

            List<String> help = messagesSection.getStringList("help");
            if (!help.isEmpty()) {
                helpMsg = List.copyOf(help);
            }
        }
    }

    public int getInterval() {
        return interval;
    }

    public List<List<String>> getBroadcasts() {
        return Collections.unmodifiableList(broadcasts);
    }

    public String getNoPermissionMsg() {
        return noPermissionMsg;
    }

    public String getReloadedMsg() {
        return reloadedMsg;
    }

    public String getUnknownCommandMsg() {
        return unknownCommandMsg;
    }

    public String getStatusHeaderMsg() {
        return statusHeaderMsg;
    }

    public String getStatusIntervalMsg() {
        return statusIntervalMsg;
    }

    public String getStatusTotalMsg() {
        return statusTotalMsg;
    }

    public String getStatusNextMsg() {
        return statusNextMsg;
    }

    public List<String> getHelpMsg() {
        return helpMsg;
    }
}