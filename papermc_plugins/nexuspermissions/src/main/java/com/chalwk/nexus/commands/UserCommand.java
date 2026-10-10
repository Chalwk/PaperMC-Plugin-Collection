// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.commands;

import com.chalwk.nexus.NexusPermissions;
import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.model.Group;
import com.chalwk.nexus.model.User;
import com.chalwk.nexus.util.MessageHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles {@code /nexus user <user> <action> ...}.
 *
 * <p>User entries are created lazily on first edit. A user who has never
 * been touched has no entry in users.yml and lives implicitly in the
 * default group.</p>
 */
public class UserCommand {

    private final PermissionManager pm;
    private final MessageHelper messageHelper;
    private final SubjectEditor editor;

    public UserCommand(NexusPermissions plugin, MessageHelper messageHelper) {
        this.pm = plugin.getPermissionManager();
        this.messageHelper = messageHelper;
        this.editor = new SubjectEditor(plugin, "nexus.user", messageHelper);
    }

    /** args[0] = user name, args[1] = action, rest = action arguments. */
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messageHelper.error(sender, "Usage: /nexus user <user> <info|perm|parent|option> ...");
            return;
        }
        String name = args[0];
        if (!PermissionManager.isValidName(name)) {
            messageHelper.error(sender, "Invalid user name.");
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        String[] rest = Arrays.copyOfRange(args, 2, args.length);

        switch (action) {
            case "info" -> info(sender, name);
            case "perm" -> {
                if (!sender.hasPermission("nexus.user.perm.set")) { messageHelper.noPermission(sender); return; }
                editor.perm(sender, pm.getOrCreateUser(canonicalName(name)), rest);
            }
            case "parent" -> {
                // The SubjectEditor re-checks this, but doing it here avoids
                // creating a user entry for a sender who isn't allowed to
                // edit one in the first place.
                if (rest.length > 0 && !sender.hasPermission("nexus.user.parent." + rest[0].toLowerCase(Locale.ROOT))) {
                    messageHelper.noPermission(sender);
                    return;
                }
                editor.parent(sender, pm.getOrCreateUser(canonicalName(name)), rest);
            }
            case "option" -> {
                if (!sender.hasPermission("nexus.user.option.set")) { messageHelper.noPermission(sender); return; }
                editor.option(sender, pm.getOrCreateUser(canonicalName(name)), rest);
            }
            default -> messageHelper.error(sender, "Unknown action. Use info, perm, parent or option.");
        }
    }

    /**
     * Prefers the stored name, then the online spelling, then whatever was
     * typed. This keeps capitalisation consistent if the same player is
     * referred to differently in different commands.
     */
    private String canonicalName(String input) {
        User stored = pm.getUser(input);
        if (stored != null) return stored.getName();
        Player online = Bukkit.getPlayerExact(input);
        return online != null ? online.getName() : input;
    }

    /**
     * Prints everything the plugin knows about a user: their groups, any
     * per-user options, their own permission entries by context, and the
     * total resolved node count for their current context set.
     */
    private void info(CommandSender sender, String name) {
        if (!sender.hasPermission("nexus.user.info")) {
            messageHelper.noPermission(sender);
            return;
        }
        // Build a transient view if no entry exists, so the output is still
        // useful for players who've never been edited.
        User stored = pm.getUser(name);
        User user = stored != null ? stored : new User(name);

        messageHelper.header(sender, "User: " + user.getName());

        String groups = pm.getEffectiveParents(user).stream().map(Group::getName).collect(Collectors.joining(", "));
        if (groups.isEmpty()) groups = "none";
        if (user.getParents().isEmpty()) groups += " (default group, implicit)";
        messageHelper.kv(sender, "Groups", groups);

        messageHelper.kv(sender, "Prefix", preview(pm.getOption(user, "prefix")));
        messageHelper.kv(sender, "Suffix", preview(pm.getOption(user, "suffix")));

        messageHelper.kv(sender, "Own options", user.getOptions().isEmpty() ? "none"
                : user.getOptions().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", ")));

        boolean any = false;
        for (Map.Entry<String, Map<String, Boolean>> e : user.getAllPermissions().entrySet()) {
            any = true;
            messageHelper.kv(sender, "Perms [" + e.getKey() + "]", formatPerms(e.getValue()));
        }
        if (!any) messageHelper.kv(sender, "Own permissions", "none");

        // Contexts depend on whether the player is currently online. An
        // offline player only gets the server-level context.
        Player online = Bukkit.getPlayerExact(name);
        List<String> contexts = online != null ? pm.getContexts(online) : pm.getServerContexts();
        messageHelper.kv(sender, "Effective nodes", pm.resolvePermissions(user, contexts).size()
                + " (contexts: " + String.join(", ", contexts) + ")");
    }

    /** Renders a prefix or suffix with a small sample of what it looks like. */
    private Component preview(String legacy) {
        if (legacy == null) return Component.text("none", NamedTextColor.GRAY);
        return messageHelper.legacy(legacy + "Preview")
                .append(Component.text("  (" + legacy + ")", NamedTextColor.DARK_GRAY));
    }

    /** Compact rendering: "+node" for granted, "-node" for negated. */
    static String formatPerms(Map<String, Boolean> perms) {
        return perms.entrySet().stream()
                .map(e -> (e.getValue() ? "+" : "-") + e.getKey())
                .collect(Collectors.joining(", "));
    }
}