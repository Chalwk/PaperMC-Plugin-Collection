// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.commands;

import com.chalwk.nexus.NexusPermissions;
import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.model.Group;
import com.chalwk.nexus.model.User;
import com.chalwk.nexus.util.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Root command dispatcher for /nexus.
 *
 * <p>Subcommands are delegated to {@link UserCommand} and
 * {@link GroupCommand}. The promote/demote/rank commands live here directly
 * because they operate on ladders rather than a single subject.</p>
 */
public class NexusCommand implements TabExecutor {

    private static final List<String> SUBCOMMANDS =
            List.of("help", "reload", "user", "group", "promote", "demote", "rank", "debug");

    private final NexusPermissions plugin;
    private final PermissionManager pm;
    private final MessageHelper messageHelper;
    private final UserCommand userCommand;
    private final GroupCommand groupCommand;

    public NexusCommand(NexusPermissions plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.pm = plugin.getPermissionManager();
        this.messageHelper = messageHelper;
        this.userCommand = new UserCommand(plugin, messageHelper);
        this.groupCommand = new GroupCommand(plugin, messageHelper);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("nexus.command")) {
            messageHelper.noPermission(sender);
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }

        // Strip the subcommand and pass the tail onward.
        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> help(sender);
            case "reload" -> reload(sender);
            case "user" -> userCommand.execute(sender, rest);
            case "group" -> groupCommand.execute(sender, rest);
            case "promote" -> changeRank(sender, rest, +1);
            case "demote" -> changeRank(sender, rest, -1);
            case "rank" -> rank(sender, rest);
            case "debug" -> debug(sender);
            default -> messageHelper.error(sender, "Unknown subcommand. Use /nexus help.");
        }
        return true;
    }

    private void help(CommandSender sender) {
        messageHelper.header(sender, "NexusPermissions");
        messageHelper.line(sender, "/nexus reload", "Reload all configuration files");
        messageHelper.line(sender, "/nexus user <user> info", "Show a user's groups, permissions and options");
        messageHelper.line(sender, "/nexus user <user> perm <node> <true|false|unset> [context]", "Set a user permission");
        messageHelper.line(sender, "/nexus user <user> parent <add|remove> <group>", "Change a user's groups");
        messageHelper.line(sender, "/nexus user <user> option <key> <value|unset>", "Set a user option (e.g. prefix)");
        messageHelper.line(sender, "/nexus group <group> create", "Create a group");
        messageHelper.line(sender, "/nexus group <group> info", "Show a group");
        messageHelper.line(sender, "/nexus group <group> perm <node> <true|false|unset> [context]", "Set a group permission");
        messageHelper.line(sender, "/nexus group <group> parent <add|remove> <group>", "Change group inheritance");
        messageHelper.line(sender, "/nexus group <group> option <key> <value|unset>", "Set a group option");
        messageHelper.line(sender, "/nexus group <group> weight <number>", "Set weight (lower = higher priority)");
        messageHelper.line(sender, "/nexus promote|demote <user> [ladder]", "Move a user along a ladder");
        messageHelper.line(sender, "/nexus rank <ladder> add <group>", "Add a group to a ladder");
        messageHelper.line(sender, "/nexus debug", "Toggle debug mode");
        messageHelper.info(sender, "Contexts: use world_name, world:name or server:name as the optional context argument.");
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("nexus.reload")) {
            messageHelper.noPermission(sender);
            return;
        }
        plugin.reloadAll();
        messageHelper.success(sender, "Reloaded config.yml, groups.yml and users.yml.");
    }

    private void debug(CommandSender sender) {
        if (!sender.hasPermission("nexus.debug")) {
            messageHelper.noPermission(sender);
            return;
        }
        boolean now = !plugin.getPluginConfig().isDebug();
        plugin.getPluginConfig().setDebug(now);
        messageHelper.success(sender, "Debug mode " + (now ? "enabled" : "disabled") + ".");
    }

    // ------------------------------------------------------------------
    // Ladders
    // ------------------------------------------------------------------

    /** Wrapper around {@link PermissionManager#changeRank} for promote and demote. */
    private void changeRank(CommandSender sender, String[] args, int direction) {
        if (!sender.hasPermission(direction > 0 ? "nexus.promote" : "nexus.demote")) {
            messageHelper.noPermission(sender);
            return;
        }
        String verb = direction > 0 ? "promote" : "demote";
        if (args.length < 1) {
            messageHelper.error(sender, "Usage: /nexus " + verb + " <user> [ladder]");
            return;
        }
        String userName = args[0];
        if (!PermissionManager.isValidName(userName)) {
            messageHelper.error(sender, "Invalid user name.");
            return;
        }

        String ladder = resolveLadder(userName, args.length > 1 ? args[1] : null);
        if (ladder == null) {
            messageHelper.error(sender, "Couldn't pick a ladder automatically. Specify one: "
                    + String.join(", ", plugin.getPluginConfig().getLadders().keySet()));
            return;
        }

        // Prefer the online spelling of the name if the player is present,
        // otherwise fall back to whatever's stored.
        Player online = Bukkit.getPlayerExact(userName);
        String exact = online != null ? online.getName() : (pm.getUser(userName) != null ? pm.getUser(userName).getName() : userName);

        try {
            String[] result = pm.changeRank(exact, ladder, direction);
            messageHelper.success(sender, (direction > 0 ? "Promoted " : "Demoted ") + exact + ": "
                    + result[0] + " -> " + result[1] + " (ladder " + ladder + ").");
        } catch (IllegalStateException e) {
            messageHelper.error(sender, e.getMessage());
        }
    }

    /**
     * Picks which ladder to act on when one isn't specified.
     *
     * <p>If only one ladder exists, it's unambiguous. Otherwise we look at
     * the user's current groups and find the first ladder they're a member
     * of. If they're on none of them, the caller gets null and reports the
     * list of available ladders.</p>
     */
    private String resolveLadder(String userName, String requested) {
        Map<String, List<String>> ladders = plugin.getPluginConfig().getLadders();
        if (requested != null) {
            return ladders.containsKey(requested.toLowerCase(Locale.ROOT)) ? requested.toLowerCase(Locale.ROOT) : requested;
        }
        if (ladders.size() == 1) return ladders.keySet().iterator().next();

        User existing = pm.getUser(userName);
        Set<String> groups = pm.getEffectiveParentNames(existing != null ? existing : new User(userName));
        for (Map.Entry<String, List<String>> e : ladders.entrySet()) {
            for (String g : e.getValue()) {
                if (groups.contains(g)) return e.getKey();
            }
        }
        return null;
    }

    /** Appends a group to the top of a ladder. Persisted to config.yml immediately. */
    private void rank(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.rank.manage")) {
            messageHelper.noPermission(sender);
            return;
        }
        if (args.length < 3 || !args[1].equalsIgnoreCase("add")) {
            messageHelper.error(sender, "Usage: /nexus rank <ladder> add <group>");
            return;
        }
        String ladder = args[0].toLowerCase(Locale.ROOT);
        String group = args[2].toLowerCase(Locale.ROOT);
        if (!PermissionManager.isValidName(ladder)) {
            messageHelper.error(sender, "Invalid ladder name.");
            return;
        }
        if (pm.getGroup(group) == null) {
            messageHelper.error(sender, "Group '" + group + "' does not exist.");
            return;
        }
        if (!plugin.getPluginConfig().addToLadder(ladder, group)) {
            messageHelper.error(sender, group + " is already on ladder " + ladder + ".");
            return;
        }
        messageHelper.success(sender, "Added " + group + " to the top of ladder " + ladder + ".");
    }

    // ------------------------------------------------------------------
    // Tab completion
    // ------------------------------------------------------------------

    /**
     * Completion is context-aware: the suggestions at each position depend
     * on what was typed earlier in the command. Group names, user names,
     * ladder names, and context names all feed into this.
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("nexus.command")) return List.of();

        int n = args.length;
        String cur = args[n - 1];

        if (n == 1) return filter(SUBCOMMANDS, cur);

        List<String> groupNames = pm.getGroups().stream().map(Group::getName).toList();
        Set<String> ladderNames = plugin.getPluginConfig().getLadders().keySet();

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "user" -> {
                if (n == 2) return filter(knownUsers(), cur);
                if (n == 3) return filter(List.of("info", "perm", "parent", "option"), cur);
                String action = args[2].toLowerCase(Locale.ROOT);
                if (action.equals("parent")) {
                    if (n == 4) return filter(List.of("add", "remove"), cur);
                    if (n == 5) return filter(groupNames, cur);
                }
                if (action.equals("perm")) {
                    if (n == 5) return filter(List.of("true", "false", "unset"), cur);
                    if (n == 6) return filter(contextSuggestions(), cur);
                }
            }
            case "group" -> {
                if (n == 2) return filter(groupNames, cur);
                if (n == 3) return filter(List.of("create", "info", "perm", "parent", "option", "weight"), cur);
                String action = args[2].toLowerCase(Locale.ROOT);
                if (action.equals("parent")) {
                    if (n == 4) return filter(List.of("add", "remove"), cur);
                    if (n == 5) return filter(groupNames, cur);
                }
                if (action.equals("perm")) {
                    if (n == 5) return filter(List.of("true", "false", "unset"), cur);
                    if (n == 6) return filter(contextSuggestions(), cur);
                }
            }
            case "promote", "demote" -> {
                if (n == 2) return filter(knownUsers(), cur);
                if (n == 3) return filter(new ArrayList<>(ladderNames), cur);
            }
            case "rank" -> {
                if (n == 2) return filter(new ArrayList<>(ladderNames), cur);
                if (n == 3) return filter(List.of("add"), cur);
                if (n == 4) return filter(groupNames, cur);
            }
            default -> {
            }
        }
        return List.of();
    }

    /** Online players first, then anyone with a stored entry in users.yml. */
    private List<String> knownUsers() {
        Set<String> names = new LinkedHashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
        for (User u : pm.getUsers()) names.add(u.getName());
        return new ArrayList<>(names);
    }

    /**
     * Global first, then every loaded world, then the configured server
     * context. Order matters for tab cycling: the most likely choices come
     * first.
     */
    private List<String> contextSuggestions() {
        List<String> out = new ArrayList<>();
        out.add("global");
        for (World w : Bukkit.getWorlds()) out.add(w.getName());
        out.add("server:" + plugin.getPluginConfig().getServerName());
        return out;
    }

    private List<String> filter(Collection<String> options, String prefix) {
        String p = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase(Locale.ROOT).startsWith(p)) out.add(o);
        }
        return out;
    }
}