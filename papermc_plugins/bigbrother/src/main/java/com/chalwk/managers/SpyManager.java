// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.BigBrother;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.SpyType;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpyManager {

    private final BigBrother plugin;
    private final Map<UUID, Set<SpyType>> playerSpies = new HashMap<>();
    private final Map<UUID, Boolean> globalToggles = new HashMap<>();

    public SpyManager(BigBrother plugin) {
        this.plugin = plugin;
    }

    private Set<SpyType> getOrCreateSpySet(Player player) {
        UUID id = player.getUniqueId();
        Set<SpyType> set = playerSpies.get(id);
        if (set == null) {
            boolean defaultEnabled = plugin.getConfigManager().getConfig().isEnabledByDefault();
            set = defaultEnabled ? EnumSet.allOf(SpyType.class) : EnumSet.noneOf(SpyType.class);
            playerSpies.put(id, set);
        }
        return set;
    }

    public boolean toggleGlobal(Player player) {
        UUID id = player.getUniqueId();
        boolean current = globalToggles.getOrDefault(id, true);
        boolean newState = !current;
        globalToggles.put(id, newState);
        return newState;
    }

    public void toggleSpyForPlayer(Player target, SpyType type) {
        Set<SpyType> set = getOrCreateSpySet(target);
        if (set.contains(type)) {
            set.remove(type);
        } else {
            set.add(type);
        }
        // Keep the (possibly empty) set so "disabled everything" is preserved.
    }

    public void toggleSpy(Player player, SpyType type) {
        toggleSpyForPlayer(player, type);
    }

    public Set<SpyType> getEnabledSpies(Player player) {
        return getOrCreateSpySet(player);
    }

    public String getStatusMessage(Player player) {
        Set<SpyType> enabled = getEnabledSpies(player);
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (enabled.isEmpty()) {
            return config.getStatusEmptyMsg();
        }

        StringBuilder builder = new StringBuilder(config.getStatusHeaderMsg());
        for (SpyType type : enabled) {
            builder.append("\n").append(config.getStatusEntryMsg().replace("{spy}", type.getConfigKey()));
        }
        return builder.toString();
    }

    public boolean isSpyEnabled(Player player, SpyType type) {
        if (!plugin.getConfigManager().getConfig().isGlobalEnabled()) {
            return false;
        }
        if (!globalToggles.getOrDefault(player.getUniqueId(), true)) {
            return false;
        }
        return getEnabledSpies(player).contains(type);
    }
}