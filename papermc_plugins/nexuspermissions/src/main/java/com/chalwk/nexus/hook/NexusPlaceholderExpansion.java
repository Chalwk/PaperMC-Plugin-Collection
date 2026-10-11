// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.hook;

import com.chalwk.nexus.NexusPermissions;
import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.model.Group;
import com.chalwk.nexus.model.User;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Native PlaceholderAPI expansion for NexusPermissions.
 *
 * <p>Registers the {@code nexus} identifier, so chat plugins, tab list
 * plugins, and menus can read Nexus data directly through PAPI rather than
 * routing through Vault.</p>
 *
 * <p>Available placeholders:</p>
 * <ul>
 *   <li>{@code %nexus_prefix%} - resolved prefix, with section-sign colours</li>
 *   <li>{@code %nexus_suffix%} - resolved suffix, with section-sign colours</li>
 *   <li>{@code %nexus_primary_group%} - highest priority group name</li>
 *   <li>{@code %nexus_groups%} - comma separated group list, sorted by weight</li>
 *   <li>{@code %nexus_group_count%} - number of effective groups</li>
 *   <li>{@code %nexus_has_<node>%} - {@code true} or {@code false}</li>
 *   <li>{@code %nexus_option_<key>%} - any option value</li>
 * </ul>
 */
public class NexusPlaceholderExpansion extends PlaceholderExpansion {

    private static final char SECTION = '\u00A7';

    private final NexusPermissions plugin;
    private final PermissionManager pm;

    public NexusPlaceholderExpansion(NexusPermissions plugin) {
        this.plugin = plugin;
        this.pm = plugin.getPermissionManager();
    }

    @Override
    public String getIdentifier() {
        return "nexus";
    }

    @Override
    public String getAuthor() {
        return "Chalwk";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || params == null) return "";

        User user = pm.getUser(player.getName());
        if (user == null) user = new User(player.getName());

        String lower = params.toLowerCase(Locale.ROOT);

        if (lower.equals("prefix")) return translate(pm.getOption(user, "prefix"));
        if (lower.equals("suffix")) return translate(pm.getOption(user, "suffix"));

        if (lower.equals("primary_group")) {
            List<Group> parents = pm.getEffectiveParents(user);
            return parents.isEmpty() ? "" : parents.get(0).getName();
        }

        if (lower.equals("groups")) {
            return pm.getEffectiveParents(user).stream()
                    .map(Group::getName)
                    .collect(Collectors.joining(","));
        }

        if (lower.equals("group_count")) {
            return String.valueOf(pm.getEffectiveParents(user).size());
        }

        if (params.startsWith("has_")) {
            String node = params.substring(4).toLowerCase(Locale.ROOT);
            if (node.isEmpty()) return "false";
            boolean value = pm.resolvePermissions(user, pm.getServerContexts())
                    .getOrDefault(node, false);
            return String.valueOf(value);
        }

        if (params.startsWith("option_")) {
            String key = params.substring(7).toLowerCase(Locale.ROOT);
            String value = pm.getOption(user, key);
            return value == null ? "" : value;
        }

        return null;
    }

    /** Same translation the Vault chat hook uses, so PAPI consumers get the same shape. */
    private static String translate(String s) {
        if (s == null || s.isEmpty()) return "";
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(chars[i + 1]) >= 0) {
                chars[i] = SECTION;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }
}