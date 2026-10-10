// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.model;

import java.util.*;

/**
 * Base entity that can hold permissions, options and parents.
 *
 * <p>Users and groups are both subjects; the only structural difference is
 * that groups carry a weight used during inheritance resolution, and users
 * fall back to the default group when they have no explicit parents.</p>
 *
 * <p>Everything stored here is mutable in place. There's no copy-on-write
 * or snapshotting; commands that change a subject are expected to save
 * afterwards, and the caller is responsible for re-applying to online
 * players.</p>
 */
public abstract class Subject {

    /** Context key for permissions that apply regardless of world or server. */
    public static final String GLOBAL = "global";

    private final String name;
    // context -> (node -> value)
    private final Map<String, Map<String, Boolean>> permissions = new LinkedHashMap<>();
    private final Map<String, String> options = new LinkedHashMap<>();
    private final Set<String> parents = new LinkedHashSet<>();

    protected Subject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Blank and null both collapse to GLOBAL, so callers can be lazy. */
    public static String normalizeContext(String context) {
        if (context == null || context.isBlank()) return GLOBAL;
        return context.trim().toLowerCase(Locale.ROOT);
    }

    // ---- Permissions ----

    public void setPermission(String context, String node, boolean value) {
        permissions.computeIfAbsent(normalizeContext(context), k -> new LinkedHashMap<>())
                .put(node.toLowerCase(Locale.ROOT), value);
    }

    /**
     * Removes a node from a context. Drops the whole context entry if that
     * was the last node in it, so empty maps don't accumulate.
     *
     * @return true if something was removed.
     */
    public boolean unsetPermission(String context, String node) {
        String ctx = normalizeContext(context);
        Map<String, Boolean> map = permissions.get(ctx);
        if (map == null) return false;
        boolean removed = map.remove(node.toLowerCase(Locale.ROOT)) != null;
        if (map.isEmpty()) permissions.remove(ctx);
        return removed;
    }

    /** Nodes for one context. Returns an empty map if the context has none. */
    public Map<String, Boolean> getPermissions(String context) {
        Map<String, Boolean> map = permissions.get(normalizeContext(context));
        return map == null ? Collections.emptyMap() : Collections.unmodifiableMap(map);
    }

    /** All contexts, each with its own node map. */
    public Map<String, Map<String, Boolean>> getAllPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    // ---- Options ----

    public void setOption(String key, String value) {
        options.put(key, value);
    }

    public boolean removeOption(String key) {
        return options.remove(key) != null;
    }

    /**
     * Returns the option defined on this subject only. Parent inheritance
     * for options is handled by PermissionManager; this method deliberately
     * doesn't recurse.
     */
    public String getOwnOption(String key) {
        return options.get(key);
    }

    public Map<String, String> getOptions() {
        return Collections.unmodifiableMap(options);
    }

    // ---- Parents (lowercase group names) ----

    public boolean addParent(String group) {
        return parents.add(group.toLowerCase(Locale.ROOT));
    }

    public boolean removeParent(String group) {
        return parents.remove(group.toLowerCase(Locale.ROOT));
    }

    public boolean hasParent(String group) {
        return parents.contains(group.toLowerCase(Locale.ROOT));
    }

    public Set<String> getParents() {
        return Collections.unmodifiableSet(parents);
    }

    /**
     * True when the subject has no permissions, options or parents. Used to
     * skip empty users when saving.
     */
    public boolean isEmpty() {
        return permissions.isEmpty() && options.isEmpty() && parents.isEmpty();
    }
}