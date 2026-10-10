// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.config;

import com.chalwk.nexus.NexusPermissions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Typed view over config.yml.
 *
 * <p>Loaded once on startup and again on {@code /nexus reload}. Values are
 * cached here rather than read from Bukkit's config each time they're
 * needed, mostly so group and ladder names don't get re-lowercased on
 * every permission check.</p>
 */
public class PluginConfig {

    private final NexusPermissions plugin;
    private boolean debug;
    private String defaultGroup = "default";
    private String serverName = "default";
    private final Map<String, List<String>> ladders = new LinkedHashMap<>();

    public PluginConfig(NexusPermissions plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration c = plugin.getConfig();
        debug = c.getBoolean("debug", false);
        defaultGroup = c.getString("default-group", "default").toLowerCase(Locale.ROOT);
        serverName = c.getString("server-name", "default").toLowerCase(Locale.ROOT);

        // Ladders are stored as name -> ordered list of group names. Both
        // the ladder name and the groups inside are normalised to lowercase
        // so lookups later can be naive equals checks.
        ladders.clear();
        ConfigurationSection section = c.getConfigurationSection("ladders");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                List<String> list = new ArrayList<>();
                for (String g : section.getStringList(key)) list.add(g.toLowerCase(Locale.ROOT));
                ladders.put(key.toLowerCase(Locale.ROOT), list);
            }
        }
    }

    public boolean isDebug() {
        return debug;
    }

    /** Flips debug mode on or off and persists it back to disk. */
    public void setDebug(boolean debug) {
        this.debug = debug;
        plugin.getConfig().set("debug", debug);
        plugin.saveConfig();
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public String getServerName() {
        return serverName;
    }

    /** Unmodifiable so callers can't accidentally corrupt the loaded config. */
    public Map<String, List<String>> getLadders() {
        return Collections.unmodifiableMap(ladders);
    }

    /** @return the ordered group list for a ladder, or null if it doesn't exist. */
    public List<String> getLadder(String name) {
        List<String> l = ladders.get(name.toLowerCase(Locale.ROOT));
        return l == null ? null : Collections.unmodifiableList(l);
    }

    /**
     * Appends a group to the top of a ladder and saves config.yml. Creates
     * the ladder if it doesn't exist yet.
     *
     * @return false if the group is already on that ladder.
     */
    public boolean addToLadder(String ladder, String group) {
        String key = ladder.toLowerCase(Locale.ROOT);
        String g = group.toLowerCase(Locale.ROOT);
        List<String> list = ladders.computeIfAbsent(key, k -> new ArrayList<>());
        if (list.contains(g)) return false;
        list.add(g);
        plugin.getConfig().set("ladders." + key, new ArrayList<>(list));
        plugin.saveConfig();
        return true;
    }
}