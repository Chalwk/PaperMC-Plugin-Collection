// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.hook;

import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.model.Group;
import com.chalwk.nexus.model.User;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Vault permission provider backed by {@link PermissionManager}.
 *
 * <p>Strictly read-only. Every mutating method throws, so another plugin
 * can never quietly edit Nexus data through Vault. Edits go through the
 * {@code /nexus} commands, which also take care of saving and refreshing
 * online players.</p>
 *
 * <p>Lookups are context-aware. With a world, the server context and
 * {@code world:<name>} are used. Without one, only the server context is.
 * The world is deliberately taken from the argument and not from the
 * player's current location, so a caller asking about {@code nether} gets
 * the nether answer even if the player is standing in {@code creative}.</p>
 *
 * <p>Prefixes and suffixes are not part of Vault's {@code Permission} API.
 * Those live in {@link VaultChatHook}.</p>
 */
public class VaultHook extends Permission {

    private final PermissionManager pm;

    public VaultHook(PermissionManager pm) {
        this.pm = pm;
    }

    /** One message for every blocked write, so the fix is obvious from a stack trace. */
    private static UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException(
                "NexusPermissions is read-only through Vault. Use the /nexus command instead (see /nexus help).");
    }

    // ------------------------------------------------------------------
    // Provider info
    // ------------------------------------------------------------------

    @Override
    public String getName() {
        return "NexusPermissions";
    }

    /**
     * Always true. Bukkit unregisters a plugin's services when it disables,
     * so this hook can't outlive Nexus.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean hasSuperPermsCompat() {
        return true;
    }

    @Override
    public boolean hasGroupSupport() {
        return true;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Stored user if there is one, otherwise a transient view that falls back to the default group. */
    private User user(String name) {
        User stored = pm.getUser(name);
        return stored != null ? stored : new User(name);
    }

    /** Server context always, plus the world context when a world was given. */
    private List<String> contexts(String world) {
        List<String> out = new ArrayList<>(pm.getServerContexts());
        if (world != null && !world.isBlank()) {
            out.add("world:" + world.trim().toLowerCase(Locale.ROOT));
        }
        return out;
    }

    /**
     * Looks a node up in a resolved map. Exact match first, then the
     * closest wildcard ({@code a.b.*}, then {@code a.*}, then {@code *}).
     *
     * <p>Bukkit expands wildcards for online players, but the resolved map
     * for an offline player or an arbitrary world never touches Bukkit, so
     * the walk has to happen here. A node nobody mentions is denied.</p>
     */
    private static boolean check(Map<String, Boolean> perms, String node) {
        if (node == null) return false;
        String key = node.toLowerCase(Locale.ROOT);
        Boolean exact = perms.get(key);
        if (exact != null) return exact;

        String probe = key;
        int dot;
        while ((dot = probe.lastIndexOf('.')) > 0) {
            probe = probe.substring(0, dot);
            Boolean wild = perms.get(probe + ".*");
            if (wild != null) return wild;
        }
        Boolean all = perms.get("*");
        return all != null && all;
    }

    // ------------------------------------------------------------------
    // Permission checks
    // ------------------------------------------------------------------

    @Override
    public boolean playerHas(String world, String player, String permission) {
        if (player == null) return false;
        return check(pm.resolvePermissions(user(player), contexts(world)), permission);
    }

    @Override
    public boolean groupHas(String world, String group, String permission) {
        Group g = pm.getGroup(group);
        if (g == null) return false;
        return check(pm.resolvePermissions(g, contexts(world)), permission);
    }

    // ------------------------------------------------------------------
    // Group membership
    // ------------------------------------------------------------------

    /**
     * Counts the implicit default group too, since that is what the user
     * really gets. Only direct effective parents are checked, not their
     * ancestors. Groups are global, so the world is ignored.
     */
    @Override
    public boolean playerInGroup(String world, String player, String group) {
        if (player == null || group == null) return false;
        for (Group g : pm.getEffectiveParents(user(player))) {
            if (g.getName().equalsIgnoreCase(group)) return true;
        }
        return false;
    }

    @Override
    public String[] getPlayerGroups(String world, String player) {
        if (player == null) return new String[0];
        return pm.getEffectiveParents(user(player)).stream().map(Group::getName).toArray(String[]::new);
    }

    /**
     * First effective parent, which is the lowest weight and so the highest
     * priority. Null only if none of the user's groups exist, which the
     * default group guard in ConfigManager makes very unlikely.
     */
    @Override
    public String getPrimaryGroup(String world, String player) {
        if (player == null) return null;
        List<Group> parents = pm.getEffectiveParents(user(player));
        return parents.isEmpty() ? null : parents.get(0).getName();
    }

    @Override
    public String[] getGroups() {
        return pm.getGroups().stream().map(Group::getName).toArray(String[]::new);
    }

    // ------------------------------------------------------------------
    // Writes, all blocked
    // ------------------------------------------------------------------

    @Override
    public boolean playerAdd(String world, String player, String permission) {
        throw readOnly();
    }

    @Override
    public boolean playerRemove(String world, String player, String permission) {
        throw readOnly();
    }

    @Override
    public boolean groupAdd(String world, String group, String permission) {
        throw readOnly();
    }

    @Override
    public boolean groupRemove(String world, String group, String permission) {
        throw readOnly();
    }

    @Override
    public boolean playerAddGroup(String world, String player, String group) {
        throw readOnly();
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group) {
        throw readOnly();
    }

    // Vault's default transient methods attach permissions straight to the
    // player behind Nexus's back, so they are blocked as well. All eight
    // overloads are listed because the defaults don't all funnel into one.

    @Override
    public boolean playerAddTransient(OfflinePlayer player, String permission) {
        throw readOnly();
    }

    @Override
    public boolean playerAddTransient(Player player, String permission) {
        throw readOnly();
    }

    @Override
    public boolean playerAddTransient(String worldName, OfflinePlayer player, String permission) {
        throw readOnly();
    }

    @Override
    public boolean playerAddTransient(String worldName, Player player, String permission) {
        throw readOnly();
    }

    @Override
    public boolean playerRemoveTransient(OfflinePlayer player, String permission) {
        throw readOnly();
    }

    @Override
    public boolean playerRemoveTransient(Player player, String permission) {
        throw readOnly();
    }

    @Override
    public boolean playerRemoveTransient(String worldName, OfflinePlayer player, String permission) {
        throw readOnly();
    }

    @Override
    public boolean playerRemoveTransient(String worldName, Player player, String permission) {
        throw readOnly();
    }
}
