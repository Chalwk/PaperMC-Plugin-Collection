// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.api;

import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.model.Group;
import com.chalwk.nexus.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link NexusAPI}, wired in on plugin enable.
 *
 * <p>Thin wrapper over {@link PermissionManager}. Lookups by UUID fall back
 * to a transient user if no entry exists, matching how the we treat
 * players who've never been edited.</p>
 */
public class NexusAPIImpl implements NexusAPI {

    private final PermissionManager pm;

    public NexusAPIImpl(PermissionManager pm) {
        this.pm = pm;
    }

    @Override
    public Map<String, Boolean> getEffectivePermissions(UUID player, List<String> contexts) {
        return pm.resolvePermissions(userFor(player), contexts);
    }

    @Override
    public Map<String, Boolean> getEffectivePermissions(UUID player, String world) {
        List<String> contexts = new ArrayList<>(pm.getServerContexts());
        if (world != null && !world.isBlank()) {
            contexts.add("world:" + world.toLowerCase(Locale.ROOT));
        }
        return pm.resolvePermissions(userFor(player), contexts);
    }

    @Override
    public boolean hasPermission(UUID player, String node, String world) {
        if (node == null) return false;
        return getEffectivePermissions(player, world).getOrDefault(node.toLowerCase(Locale.ROOT), false);
    }

    @Override
    public String getOption(UUID player, String key) {
        return pm.getOption(userFor(player), key);
    }

    @Override
    public String getPrimaryGroup(UUID player) {
        List<Group> parents = pm.getEffectiveParents(userFor(player));
        return parents.isEmpty() ? null : parents.get(0).getName();
    }

    @Override
    public List<String> getGroups(UUID player) {
        List<String> out = new ArrayList<>();
        for (Group g : pm.getEffectiveParents(userFor(player))) out.add(g.getName());
        return out;
    }

    @Override
    public List<String> getUsersWithPermission(String node) {
        List<String> out = new ArrayList<>();
        for (User u : pm.getUsers()) {
            if (pm.resolvePermissions(u, pm.getServerContexts())
                    .getOrDefault(node.toLowerCase(Locale.ROOT), false)) {
                out.add(u.getName());
            }
        }
        return out;
    }

    @Override
    public List<String> getGroupsWithPermission(String node) {
        List<String> out = new ArrayList<>();
        for (Group g : pm.getGroups()) {
            if (pm.resolvePermissions(g, pm.getServerContexts())
                    .getOrDefault(node.toLowerCase(Locale.ROOT), false)) {
                out.add(g.getName());
            }
        }
        return out;
    }

    /**
     * Resolves a UUID to a User, falling back to a transient view if there's
     * no stored entry. That way callers don't have to null-check when asking
     * about a player who's never been edited.
     */
    private User userFor(UUID uuid) {
        if (uuid == null) return new User("__unknown__");
        User stored = pm.getUserByUuid(uuid);
        return stored != null ? stored : new User(uuid.toString());
    }
}