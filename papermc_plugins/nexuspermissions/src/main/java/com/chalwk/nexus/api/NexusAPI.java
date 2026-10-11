// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Public read API for NexusPermissions.
 *
 * <p>Plugins that want to query Nexus directly should grab this via
 * {@code Bukkit.getServicesManager().load(NexusAPI.class)} after a
 * {@code softdepend} on NexusPermissions. Everything here is read-only.
 * Mutations go through the {@code /nexus} commands.</p>
 */
public interface NexusAPI {

    /** Effective permissions for a player in the given contexts, resolved and merged. */
    Map<String, Boolean> getEffectivePermissions(UUID player, List<String> contexts);

    /** Effective permissions for a player as they apply in the named world. */
    Map<String, Boolean> getEffectivePermissions(UUID player, String world);

    /** Convenience: single node check in the named world. */
    boolean hasPermission(UUID player, String node, String world);

    /** Resolves an option such as prefix, walking the parent chain by weight. */
    String getOption(UUID player, String key);

    /** The player's highest priority group name, or null if they have none. */
    String getPrimaryGroup(UUID player);

    /** Every group the player effectively belongs to, sorted by weight. */
    List<String> getGroups(UUID player);

    /** Names of users whose resolved permissions grant the given node. */
    List<String> getUsersWithPermission(String node);

    /** Names of groups whose resolved permissions grant the given node. */
    List<String> getGroupsWithPermission(String node);
}