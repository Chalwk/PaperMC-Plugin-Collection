// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.commands;

import com.chalwk.nexus.NexusPermissions;
import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.model.Group;
import com.chalwk.nexus.model.Subject;
import com.chalwk.nexus.model.User;
import com.chalwk.nexus.util.MessageHelper;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Locale;

/**
 * Shared logic for editing a subject's permissions, parents and options.
 *
 * <p>Users and groups behave identically for these three operations, so the
 * code lives here once. The only difference at call time is which
 * permission node gates the action, given by {@code basePerm}, which is
 * either {@code nexus.user} or {@code nexus.group}.</p>
 *
 * <p>Every mutating method persists and re-applies to online players
 * before returning. There's no undo, so the state on disk always matches
 * what the server is currently enforcing.</p>
 */
final class SubjectEditor {

    private final NexusPermissions plugin;
    private final PermissionManager pm;
    private final MessageHelper messageHelper;
    private final String basePerm;

    SubjectEditor(NexusPermissions plugin, String basePerm, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.pm = plugin.getPermissionManager();
        this.basePerm = basePerm;
        this.messageHelper = messageHelper;
    }

    /** Used in usage strings and error messages so they read naturally. */
    private String kind(Subject s) {
        return s instanceof User ? "user" : "group";
    }

    /** Persist and re-apply. Called after every successful change. */
    private void commit() {
        pm.save();
        pm.refreshAll();
    }

    /** args: <node> <true|false|unset> [context] */
    void perm(CommandSender sender, Subject subject, String[] args) {
        if (!sender.hasPermission(basePerm + ".perm.set")) {
            messageHelper.noPermission(sender);
            return;
        }
        if (args.length < 2) {
            messageHelper.error(sender, "Usage: /nexus " + kind(subject) + " <name> perm <node> <true|false|unset> [context]");
            return;
        }
        String node = args[0].toLowerCase(Locale.ROOT);
        // A leading "-" is the on-disk negation marker, so it's not valid
        // on the command line. Negation at the CLI is done with "false".
        if (!PermissionManager.isValidName(node) || node.startsWith("-")) {
            messageHelper.error(sender, "Invalid permission node.");
            return;
        }
        String context = PermissionManager.parseContext(args.length > 2 ? args[2] : null);
        String where = context.equals(Subject.GLOBAL) ? "globally" : "in " + context;
        String value = args[1].toLowerCase(Locale.ROOT);

        switch (value) {
            case "true", "false" -> {
                subject.setPermission(context, node, Boolean.parseBoolean(value));
                messageHelper.success(sender, "Set " + node + " to " + value + " for " + subject.getName() + " " + where + ".");
            }
            case "unset", "remove" -> {
                if (!subject.unsetPermission(context, node)) {
                    messageHelper.error(sender, subject.getName() + " has no " + node + " entry " + where + ".");
                    return;
                }
                messageHelper.success(sender, "Removed " + node + " from " + subject.getName() + " " + where + ".");
            }
            default -> {
                messageHelper.error(sender, "Value must be true, false or unset.");
                return;
            }
        }
        commit();
    }

    /**
     * args: <add|remove> <group>
     *
     * <p>Adding a parent to a group runs a cycle check first, so you can't
     * create an inheritance loop by accident. Users can't have this problem
     * because they're never someone else's parent.</p>
     */
    void parent(CommandSender sender, Subject subject, String[] args) {
        if (args.length < 2) {
            messageHelper.error(sender, "Usage: /nexus " + kind(subject) + " <name> parent <add|remove> <group>");
            return;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        String groupName = args[1].toLowerCase(Locale.ROOT);

        switch (action) {
            case "add" -> {
                if (!sender.hasPermission(basePerm + ".parent.add")) {
                    messageHelper.noPermission(sender);
                    return;
                }
                Group parent = pm.getGroup(groupName);
                if (parent == null) {
                    messageHelper.error(sender, "Group '" + groupName + "' does not exist.");
                    return;
                }
                if (subject instanceof Group g && pm.wouldCreateCycle(g, parent)) {
                    messageHelper.error(sender, "That would create an inheritance loop.");
                    return;
                }
                if (!subject.addParent(groupName)) {
                    messageHelper.error(sender, subject.getName() + " already has parent " + groupName + ".");
                    return;
                }
                messageHelper.success(sender, "Added parent " + groupName + " to " + subject.getName() + ".");
            }
            case "remove" -> {
                if (!sender.hasPermission(basePerm + ".parent.remove")) {
                    messageHelper.noPermission(sender);
                    return;
                }
                if (!subject.removeParent(groupName)) {
                    messageHelper.error(sender, subject.getName() + " doesn't have parent " + groupName + ".");
                    return;
                }
                messageHelper.success(sender, "Removed parent " + groupName + " from " + subject.getName() + ".");
            }
            default -> {
                messageHelper.error(sender, "Usage: /nexus " + kind(subject) + " <name> parent <add|remove> <group>");
                return;
            }
        }
        commit();
    }

    /**
     * args: <key> <value...>
     *
     * <p>Values can contain spaces because everything after the key is
     * joined back together. Passing "unset" as the sole value removes the
     * option entirely.</p>
     */
    void option(CommandSender sender, Subject subject, String[] args) {
        if (!sender.hasPermission(basePerm + ".option.set")) {
            messageHelper.noPermission(sender);
            return;
        }
        if (args.length < 2) {
            messageHelper.error(sender, "Usage: /nexus " + kind(subject) + " <name> option <key> <value|unset>");
            return;
        }
        String key = args[0].toLowerCase(Locale.ROOT);
        if (!PermissionManager.isValidName(key)) {
            messageHelper.error(sender, "Invalid option key.");
            return;
        }
        String value = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (value.equalsIgnoreCase("unset")) {
            if (!subject.removeOption(key)) {
                messageHelper.error(sender, subject.getName() + " has no option '" + key + "'.");
                return;
            }
            messageHelper.success(sender, "Removed option " + key + " from " + subject.getName() + ".");
        } else {
            subject.setOption(key, value);
            messageHelper.success(sender, "Set option " + key + " for " + subject.getName() + ".");
        }
        commit();
    }
}