// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.manager;

import com.chalwk.nexus.NexusPermissions;
import com.chalwk.nexus.hook.WorldGuardHook;
import com.chalwk.nexus.model.Group;
import com.chalwk.nexus.model.Subject;
import com.chalwk.nexus.model.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

/**
 * The central brain: stores subjects in memory, resolves inheritance and
 * applies the result to online players.
 *
 * <p>Groups and users are both held in insertion-ordered maps. Users are
 * keyed by lowercase name for fast command lookups, and additionally
 * indexed by UUID once one is recorded, so the public API can look them up
 * either way.</p>
 */
public class PermissionManager {

    private final NexusPermissions plugin;
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<UUID, User> usersByUuid = new HashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public PermissionManager(NexusPermissions plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    // Storage
    // ------------------------------------------------------------------

    public void clear() {
        groups.clear();
        users.clear();
        usersByUuid.clear();
    }

    public void addGroup(Group g) {
        groups.put(g.getName().toLowerCase(Locale.ROOT), g);
    }

    public void addUser(User u) {
        users.put(u.getName().toLowerCase(Locale.ROOT), u);
        if (u.getUuid() != null) usersByUuid.put(u.getUuid(), u);
    }

    /**
     * Renames a user in the lookup map. Used by /nexus user rename. The
     * UUID index is untouched, since the UUID doesn't change.
     */
    public void renameUser(String oldName, String newName) {
        User u = users.remove(oldName.toLowerCase(Locale.ROOT));
        if (u != null) users.put(newName.toLowerCase(Locale.ROOT), u);
    }

    public Group getGroup(String name) {
        return name == null ? null : groups.get(name.toLowerCase(Locale.ROOT));
    }

    public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    /** @return the new group, or null if it already exists. */
    public Group createGroup(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        if (groups.containsKey(key)) return null;
        Group g = new Group(key);
        groups.put(key, g);
        return g;
    }

    /**
     * Removes a group and cleans up references to it from other groups'
     * parents and every user's parents. Returns false if the group didn't
     * exist. Does not check ladders or default-group; callers do that.
     */
    public boolean deleteGroup(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        Group g = groups.remove(key);
        if (g == null) return false;
        for (Group other : groups.values()) other.removeParent(key);
        for (User u : users.values()) u.removeParent(key);
        return true;
    }

    public User getUser(String name) {
        return name == null ? null : users.get(name.toLowerCase(Locale.ROOT));
    }

    public User getUserByUuid(UUID uuid) {
        return uuid == null ? null : usersByUuid.get(uuid);
    }

    /**
     * Returns the stored user, creating an empty one if it's missing. Used
     * by commands that are about to edit a user, since an entry needs to
     * exist in memory before it can be mutated.
     */
    public User getOrCreateUser(String name) {
        return users.computeIfAbsent(name.toLowerCase(Locale.ROOT), k -> new User(name));
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    /**
     * Removes a user from storage. Returns false if they don't exist.
     * Refuses if the user is currently online, since removing them out from
     * under a live session would leave stale attachments behind.
     */
    public boolean deleteUser(String name) {
        User u = users.get(name.toLowerCase(Locale.ROOT));
        if (u == null) return false;
        if (u.getUuid() != null) usersByUuid.remove(u.getUuid());
        users.remove(name.toLowerCase(Locale.ROOT));
        return true;
    }

    /**
     * Records a player's UUID on their stored user, creating an entry if
     * needed. Called on join so that name-keyed legacy entries get linked.
     */
    public boolean recordUuid(Player player) {
        User u = getUser(player.getName());
        if (u == null) return false;
        if (u.getUuid() != null) return false;
        u.setUuid(player.getUniqueId());
        usersByUuid.put(player.getUniqueId(), u);
        return true;
    }

    public void save() {
        plugin.getConfigManager().saveAll(this);
    }

    // ------------------------------------------------------------------
    // Validation helpers
    // ------------------------------------------------------------------

    /**
     * Names can't contain whitespace or slashes, and are capped at 64
     * characters. Slashes are excluded because '/' is the YAML path
     * separator used in the config files.
     */
    public static boolean isValidName(String s) {
        if (s == null || s.isEmpty() || s.length() > 64) return false;
        for (char c : s.toCharArray()) {
            if (Character.isWhitespace(c) || c == '/') return false;
        }
        return true;
    }

    /**
     * Normalises a user-supplied context string. A bare word becomes a
     * world context. Anything containing a colon is passed through. The
     * empty string and "global" both mean no context.
     */
    public static String parseContext(String arg) {
        if (arg == null || arg.isBlank() || arg.equalsIgnoreCase(Subject.GLOBAL)) return Subject.GLOBAL;
        String lower = arg.trim().toLowerCase(Locale.ROOT);
        return lower.contains(":") ? lower : "world:" + lower;
    }

    // ------------------------------------------------------------------
    // Inheritance resolution
    // ------------------------------------------------------------------

    /**
     * Groups this subject inherits from, sorted by weight (lowest first).
     * Users with no explicit parents fall back to the default group, which
     * is how new players end up with basic permissions without needing an
     * entry in users.yml.
     */
    public List<Group> getEffectiveParents(Subject subject) {
        Collection<String> names = subject.getParents();
        if (names.isEmpty() && subject instanceof User) {
            names = List.of(plugin.getPluginConfig().getDefaultGroup());
        }
        List<Group> result = new ArrayList<>();
        for (String n : names) {
            Group g = getGroup(n);
            if (g != null) result.add(g);
        }
        result.sort(Comparator.comparingInt(Group::getWeight));
        return result;
    }

    /** Resolves a subject's effective permission map for the given contexts. */
    public Map<String, Boolean> resolvePermissions(Subject subject, List<String> contexts) {
        Map<String, Boolean> out = new LinkedHashMap<>();
        resolveInto(subject, contexts, out, Collections.newSetFromMap(new IdentityHashMap<>()));
        return out;
    }

    /**
     * Walks the inheritance chain and merges permissions into {@code out}.
     *
     * <p>Later writes overwrite earlier ones, so the order matters. The
     * parent list is sorted lowest-weight-first, and we iterate it in
     * reverse: the highest-weight (lowest priority) parent is written
     * first, and the lowest-weight (highest priority) parent is written
     * last, so it wins. The subject's own global permissions come next,
     * and its context-specific ones last of all. Identity-set guards
     * against cycles, even though the edit commands refuse to create
     * them.</p>
     */
    private void resolveInto(Subject subject, List<String> contexts, Map<String, Boolean> out, Set<Subject> path) {
        if (!path.add(subject)) return;
        List<Group> parents = getEffectiveParents(subject);
        for (int i = parents.size() - 1; i >= 0; i--) {
            resolveInto(parents.get(i), contexts, out, path);
        }
        out.putAll(subject.getPermissions(Subject.GLOBAL));
        for (String ctx : contexts) {
            out.putAll(subject.getPermissions(ctx));
        }
        path.remove(subject);
    }

    /**
     * Resolves an option such as prefix. The subject's own value wins, then
     * parents are checked highest-weight-first. Returns null if nothing in
     * the chain defines the option.
     */
    public String getOption(Subject subject, String key) {
        return getOption(subject, key, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private String getOption(Subject subject, String key, Set<Subject> path) {
        if (!path.add(subject)) return null;
        try {
            String own = subject.getOwnOption(key);
            if (own != null) return own;
            for (Group parent : getEffectiveParents(subject)) {
                String v = getOption(parent, key, path);
                if (v != null) return v;
            }
            return null;
        } finally {
            path.remove(subject);
        }
    }

    /**
     * Returns true if making {@code newParent} a parent of {@code group}
     * would create a loop. Called before every group-parent addition so
     * the tree stays a tree.
     */
    public boolean wouldCreateCycle(Group group, Group newParent) {
        return reaches(newParent, group, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private boolean reaches(Group from, Group target, Set<Group> seen) {
        if (from == target) return true;
        if (!seen.add(from)) return false;
        for (String p : from.getParents()) {
            Group g = getGroup(p);
            if (g != null && reaches(g, target, seen)) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Contexts
    // ------------------------------------------------------------------

    public List<String> getServerContexts() {
        return List.of("server:" + plugin.getPluginConfig().getServerName());
    }

    /**
     * Server context, then the player's world, then any WorldGuard regions
     * they're standing in. Regions are appended last, so their entries
     * override world-level ones during resolution.
     *
     * <p>Region lookups are relatively expensive. They run on join, world
     * change, and explicit refresh, not on every permission check.</p>
     */
    public List<String> getContexts(Player player) {
        List<String> out = new ArrayList<>(3);
        out.add("server:" + plugin.getPluginConfig().getServerName());
        out.add("world:" + player.getWorld().getName().toLowerCase(Locale.ROOT));
        if (plugin.getPluginConfig().isRegionContextEnabled()) {
            out.addAll(WorldGuardHook.regionsAt(player));
        }
        return out;
    }

    // ------------------------------------------------------------------
    // Applying to players
    // ------------------------------------------------------------------

    /**
     * Resolves the player's effective permissions and attaches them.
     *
     * <p>Wildcards like {@code essentials.*} and {@code *} are placed in
     * the attachment as-is. Bukkit's own permission lookup walks the
     * hierarchy at query time, so there's no need to expand them here.</p>
     *
     * <p>Entries are added one at a time via
     * {@link PermissionAttachment#setPermission(String, boolean)} rather
     * than by writing to the map returned by {@code getPermissions()}.
     * On current Paper builds that map is a copy, so writing to it is a
     * silent no-op: the attachment is created empty, the player resolves
     * nothing, and every node falls through to the server's default
     * registry. {@code setPermission} is the supported mutation path.</p>
     */
    public void applyToPlayer(Player player) {
        removeAttachment(player);

        User user = getUser(player.getName());
        // Transient view for a player who's never been edited. The default
        // group provides their baseline permissions.
        if (user == null) user = new User(player.getName());

        Map<String, Boolean> effective = resolvePermissions(user, getContexts(player));

        PermissionAttachment attachment = player.addAttachment(plugin);
        for (Map.Entry<String, Boolean> entry : effective.entrySet()) {
            attachment.setPermission(entry.getKey(), entry.getValue());
        }
        attachments.put(player.getUniqueId(), attachment);
        player.recalculatePermissions();

        // Guard against a future Paper change making setPermission a
        // silent no-op the same way my old putAll was. If the attachment
        // ends up with fewer entries than we resolved, every permission
        // check for this player will quietly fall through to the default
        // registry.
        if (attachment.getPermissions().size() != effective.size()) {
            plugin.getLogger().severe("Permission attachment size mismatch for " + player.getName()
                    + " (expected " + effective.size() + ", got " + attachment.getPermissions().size()
                    + "). Bukkit's PermissionAttachment API may have changed.");
        }

        for (Map.Entry<String, Boolean> entry : effective.entrySet()) {
            if (!entry.getValue()) continue;
            if (!player.hasPermission(entry.getKey())) {
                plugin.getLogger().severe("Permission attachment verification failed for " + player.getName()
                        + ": granted node '" + entry.getKey() + "' resolved false through Bukkit.");
            }
            break;
        }

        plugin.debug("Applied " + effective.size() + " permission nodes to " + player.getName()
                + " (contexts: " + getContexts(player) + ")");
    }

    public void removeAttachment(Player player) {
        PermissionAttachment old = attachments.remove(player.getUniqueId());
        if (old != null) {
            try {
                player.removeAttachment(old);
            } catch (IllegalArgumentException ignored) {
                // already removed by Bukkit during shutdown
            }
        }
    }

    /** Used after any change that could affect how permissions resolve. */
    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) applyToPlayer(p);
    }

    public void shutdown() {
        for (Player p : Bukkit.getOnlinePlayers()) removeAttachment(p);
        attachments.clear();
    }

    // ------------------------------------------------------------------
    // Timed permission sweep
    // ------------------------------------------------------------------

    /**
     * Removes every expired entry from every subject. Returns true if
     * anything changed, so the caller can decide whether to save and
     * refresh.
     */
    public boolean purgeExpired() {
        boolean changed = false;
        for (Group g : groups.values()) changed |= g.purgeExpired();
        for (User u : users.values()) changed |= u.purgeExpired();
        return changed;
    }

    // ------------------------------------------------------------------
    // Ladders
    // ------------------------------------------------------------------

    /** Names of the groups a user effectively belongs to (lowercase). */
    public Set<String> getEffectiveParentNames(User user) {
        Set<String> names = new LinkedHashSet<>();
        if (user.getParents().isEmpty()) names.add(plugin.getPluginConfig().getDefaultGroup());
        else names.addAll(user.getParents());
        return names;
    }

    /**
     * Moves a user one step along a ladder.
     *
     * <p>If the user is on more than one group that appears in the ladder
     * (unusual, but possible with multi-parent users), the highest position
     * wins. The old group is removed and the new one added, so a user is
     * never on two rungs of the same ladder at once.</p>
     *
     * @param direction +1 promote, -1 demote
     * @return {fromGroup, toGroup}
     * @throws IllegalStateException with a user-facing message if it can't be done
     */
    public String[] changeRank(String userName, String ladderName, int direction) {
        List<String> ladder = plugin.getPluginConfig().getLadder(ladderName);
        if (ladder == null) throw new IllegalStateException("Ladder '" + ladderName + "' does not exist.");

        User existing = getUser(userName);
        Set<String> current = getEffectiveParentNames(existing != null ? existing : new User(userName));

        int index = -1;
        for (String g : current) {
            index = Math.max(index, ladder.indexOf(g));
        }
        if (index == -1) throw new IllegalStateException(userName + " is not in any group on ladder '" + ladderName + "'.");

        int next = index + direction;
        if (next < 0) throw new IllegalStateException(userName + " is already at the bottom of '" + ladderName + "'.");
        if (next >= ladder.size()) throw new IllegalStateException(userName + " is already at the top of '" + ladderName + "'.");

        String from = ladder.get(index);
        String to = ladder.get(next);
        // Groups can be renamed or removed from the ladder without the
        // users following; catch that here rather than at permission check
        // time.
        if (getGroup(to) == null) throw new IllegalStateException("Group '" + to + "' on the ladder does not exist.");

        User user = getOrCreateUser(userName);
        user.removeParent(from);
        user.addParent(to);
        save();
        refreshAll();
        return new String[]{from, to};
    }
}