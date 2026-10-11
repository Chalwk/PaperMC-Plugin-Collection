// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.model;

import java.util.*;

/**
 * Base entity that can hold permissions, options and parents.
 *
 * <p>Permissions are stored per context, each with an optional expiry. The
 * public read methods filter expired entries out automatically, so a timed
 * grant stops applying the moment it expires, regardless of whether the
 * sweep task has run.</p>
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
    // context -> (node -> entry)
    private final Map<String, Map<String, PermissionEntry>> permissions = new LinkedHashMap<>();
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
        setPermission(context, node, value, 0L);
    }

    public void setPermission(String context, String node, boolean value, long expiresAt) {
        permissions.computeIfAbsent(normalizeContext(context), k -> new LinkedHashMap<>())
                .put(node.toLowerCase(Locale.ROOT), new PermissionEntry(value, expiresAt));
    }

    /**
     * Removes a node from a context. Drops the whole context entry if that
     * was the last node in it, so empty maps don't accumulate.
     *
     * @return true if something was removed.
     */
    public boolean unsetPermission(String context, String node) {
        String ctx = normalizeContext(context);
        Map<String, PermissionEntry> map = permissions.get(ctx);
        if (map == null) return false;
        boolean removed = map.remove(node.toLowerCase(Locale.ROOT)) != null;
        if (map.isEmpty()) permissions.remove(ctx);
        return removed;
    }

    /**
     * Nodes for one context, with expired entries filtered out. Returns an
     * empty map if the context has none.
     */
    public Map<String, Boolean> getPermissions(String context) {
        Map<String, PermissionEntry> raw = permissions.get(normalizeContext(context));
        if (raw == null || raw.isEmpty()) return Collections.emptyMap();
        Map<String, Boolean> out = new LinkedHashMap<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, PermissionEntry> e : raw.entrySet()) {
            PermissionEntry entry = e.getValue();
            if (entry.expiresAt() > 0 && now > entry.expiresAt()) continue;
            out.put(e.getKey(), entry.value());
        }
        return Collections.unmodifiableMap(out);
    }

    /** All contexts, filtered as above. Read-only view for commands. */
    public Map<String, Map<String, Boolean>> getAllPermissions() {
        Map<String, Map<String, Boolean>> out = new LinkedHashMap<>();
        for (String ctx : permissions.keySet()) {
            Map<String, Boolean> filtered = getPermissions(ctx);
            if (!filtered.isEmpty()) out.put(ctx, filtered);
        }
        return Collections.unmodifiableMap(out);
    }

    /** Raw, unfiltered view. Used by the save path so expiries survive a round-trip. */
    public Map<String, Map<String, PermissionEntry>> getAllRawPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    /** The expiry timestamp of a node in a context, or zero if permanent or absent. */
    public long getExpiry(String context, String node) {
        Map<String, PermissionEntry> map = permissions.get(normalizeContext(context));
        if (map == null) return 0L;
        PermissionEntry entry = map.get(node.toLowerCase(Locale.ROOT));
        return entry == null ? 0L : entry.expiresAt();
    }

    /**
     * Removes every expired entry from every context.
     *
     * @return true if anything was removed.
     */
    public boolean purgeExpired() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        Iterator<Map.Entry<String, Map<String, PermissionEntry>>> ctxIt = permissions.entrySet().iterator();
        while (ctxIt.hasNext()) {
            Map<String, PermissionEntry> map = ctxIt.next().getValue();
            Iterator<Map.Entry<String, PermissionEntry>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                PermissionEntry entry = it.next().getValue();
                if (entry.expiresAt() > 0 && now > entry.expiresAt()) {
                    it.remove();
                    changed = true;
                }
            }
            if (map.isEmpty()) ctxIt.remove();
        }
        return changed;
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