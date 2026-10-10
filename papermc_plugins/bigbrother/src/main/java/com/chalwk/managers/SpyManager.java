// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.BigBrother;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.SpyType;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Holds per-player spy state.
 *
 * <p>State is intentionally in-memory only. Nothing here is persisted across
 * restarts or reconnects, so a player always starts from whatever
 * {@code enabled_by_default} says when they log in. This is by design: a
 * restart is a clean slate, which is what most servers actually want.</p>
 *
 * <p>Keys are player UUIDs. Entries are removed on quit by
 * {@link #forget(UUID)}, called from the spy listener.</p>
 */
public class SpyManager {

    private final BigBrother plugin;

    // Two maps rather than a single "PlayerState" record because the two
    // toggles have different lifetimes in practice: the global switch is
    // almost never touched, while individual spy flags flip during a session.
    private final Map<UUID, Set<SpyType>> playerSpies = new HashMap<>();
    private final Map<UUID, Boolean> globalToggles = new HashMap<>();

    public SpyManager(BigBrother plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the player's spy set, creating it lazily from the
     * {@code enabled_by_default} config value the first time we see them.
     */
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

    /**
     * Flips the player's global on/off switch. Does not affect their
     * per-spy toggles: those are remembered and restored when they switch
     * the global back on.
     */
    public boolean toggleGlobal(Player player) {
        UUID id = player.getUniqueId();
        boolean current = globalToggles.getOrDefault(id, true);
        boolean newState = !current;
        globalToggles.put(id, newState);
        return newState;
    }

    /**
     * Toggles the given spy type for the target player.
     *
     * @return the new state: {@code true} if the spy is now enabled,
     *         {@code false} if it is now disabled.
     */
    public boolean toggleSpyForPlayer(Player target, SpyType type) {
        Set<SpyType> set = getOrCreateSpySet(target);
        if (set.contains(type)) {
            set.remove(type);
            return false;
        }
        set.add(type);
        return true;
    }

    public boolean toggleSpy(Player player, SpyType type) {
        return toggleSpyForPlayer(player, type);
    }

    /**
     * Returns an unmodifiable view of the player's currently enabled spies.
     * Mutate via {@link #toggleSpy(Player, SpyType)}.
     */
    public Set<SpyType> getEnabledSpies(Player player) {
        return Collections.unmodifiableSet(getOrCreateSpySet(player));
    }

    /**
     * Drops all per-player state for the given UUID. Called on quit so
     * the maps don't grow without bound on long-running servers.
     */
    public void forget(UUID id) {
        playerSpies.remove(id);
        globalToggles.remove(id);
    }

    /**
     * Builds the multi-line string shown by {@code /bigbrother status}.
     * Returns the empty-state message when nothing is on.
     */
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

    /**
     * The hot-path check called by every spy event handler.
     *
     * <p>Short-circuits in order of cheapest to most expensive: server-wide
     * disable first, then the player's personal switch, then the per-spy
     * lookup. On a busy server this method runs many times per tick, so the
     * order matters.</p>
     */
    public boolean isSpyEnabled(Player player, SpyType type) {
        if (!plugin.getConfigManager().getConfig().isGlobalEnabled()) {
            return false;
        }
        if (!globalToggles.getOrDefault(player.getUniqueId(), true)) {
            return false;
        }
        return getOrCreateSpySet(player).contains(type);
    }
}