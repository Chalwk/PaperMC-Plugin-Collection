// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.AutoMessages;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PluginConfig {

    private int interval = 600;
    private final List<List<String>> messages = new ArrayList<>();

    private String noPermissionMsg = "&cYou don't have permission to use this feature!";
    private String reloadedMsg = "&aConfiguration reloaded and scheduler restarted!";
    private String unknownCommandMsg = "&cUnknown command. Use /automessages help.";
    private String statusHeaderMsg = "&eAutoMessages Status:";
    private String statusIntervalMsg = "&7- Interval: &f{interval}s";
    private String statusTotalMsg = "&7- Total messages: &f{total}";
    private String statusNextMsg = "&7- Next message index: &f{next}/{total}";

    public PluginConfig(AutoMessages plugin) {
    }

    public void loadFromConfig(ConfigurationSection config) {
        interval = config.getInt("interval", interval);

        messages.clear();
        ConfigurationSection broadcasts = config.getConfigurationSection("broadcasts");
        if (broadcasts != null) {
            Map<Integer, List<String>> sorted = new TreeMap<>();
            for (String key : broadcasts.getKeys(false)) {
                try {
                    int index = Integer.parseInt(key);
                    List<String> lines = broadcasts.getStringList(key);
                    if (!lines.isEmpty()) {
                        sorted.put(index, lines);
                    }
                } catch (NumberFormatException ignored) {
                    // Skip non-numeric keys (e.g. comments-in-section artefacts)
                }
            }
            messages.addAll(sorted.values());
        }

        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages != null) {
            noPermissionMsg = messages.getString("no_permission", noPermissionMsg);
            reloadedMsg = messages.getString("reloaded", reloadedMsg);
            unknownCommandMsg = messages.getString("unknown_command", unknownCommandMsg);
            statusHeaderMsg = messages.getString("status_header", statusHeaderMsg);
            statusIntervalMsg = messages.getString("status_interval", statusIntervalMsg);
            statusTotalMsg = messages.getString("status_total", statusTotalMsg);
            statusNextMsg = messages.getString("status_next", statusNextMsg);
        }
    }

    public int getInterval() {
        return interval;
    }

    public List<List<String>> getMessages() {
        return messages;
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
}