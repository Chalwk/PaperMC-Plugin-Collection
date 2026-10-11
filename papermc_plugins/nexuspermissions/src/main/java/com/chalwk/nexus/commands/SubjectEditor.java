// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.commands;

import com.chalwk.nexus.NexusPermissions;
import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.model.Group;
import com.chalwk.nexus.model.Subject;
import com.chalwk.nexus.model.User;
import com.chalwk.nexus.util.AuditLogger;
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
 * <p>Every mutating method persists, re-applies to online players, and
 * writes an audit line if the audit log is enabled.</p>
 */
final class SubjectEditor {

    private final PermissionManager pm;
    private final MessageHelper messageHelper;
    private final AuditLogger audit;
    private final String basePerm;

    SubjectEditor(NexusPermissions plugin, String basePerm, MessageHelper messageHelper) {
        this.pm = plugin.getPermissionManager();
        this.messageHelper = messageHelper;
        this.audit = plugin.getAuditLogger();
        this.basePerm = basePerm;
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

    /**
     * args: {@code <node> <true|false|unset> [context] [temp:7d]}
     *
     * <p>Context and temp are both optional. The parser accepts them in
     * either order as long as temp has the {@code temp:} prefix. Duration
     * suffixes are s, m, h, d, w.</p>
     */
    void perm(CommandSender sender, Subject subject, String[] args) {
        if (!sender.hasPermission(basePerm + ".perm.set")) {
            messageHelper.noPermission(sender);
            return;
        }
        if (args.length < 2) {
            messageHelper.error(sender, "Usage: /nexus " + kind(subject) + " <name> perm <node> <true|false|unset> [context] [temp:7d]");
            return;
        }
        String node = args[0].toLowerCase(Locale.ROOT);
        // A leading "-" is the on-disk negation marker, so it's not valid
        // on the command line. Negation at the CLI is done with "false".
        if (!PermissionManager.isValidName(node) || node.startsWith("-")) {
            messageHelper.error(sender, "Invalid permission node.");
            return;
        }

        String value = args[1].toLowerCase(Locale.ROOT);
        String contextArg = null;
        String tempArg = null;
        for (int i = 2; i < args.length; i++) {
            if (args[i].toLowerCase(Locale.ROOT).startsWith("temp:")) {
                tempArg = args[i].substring(5);
            } else if (contextArg == null) {
                contextArg = args[i];
            }
        }
        String context = PermissionManager.parseContext(contextArg);
        String where = context.equals(Subject.GLOBAL) ? "globally" : "in " + context;

        long expires = 0L;
        if (tempArg != null) {
            Long parsed = parseDuration(tempArg);
            if (parsed == null) {
                messageHelper.error(sender, "Invalid duration. Use a number followed by s, m, h, d or w (for example temp:7d).");
                return;
            }
            expires = parsed;
        }

        String auditSuffix = expires > 0 ? " (until " + expires + ")" : "";

        switch (value) {
            case "true", "false" -> {
                subject.setPermission(context, node, Boolean.parseBoolean(value), expires);
                String suffix = expires > 0 ? " until " + formatExpiry(expires) : "";
                messageHelper.success(sender, "Set " + node + " to " + value + " for " + subject.getName() + " " + where + suffix + ".");
                audit.log(sender, kind(subject) + " " + subject.getName() + " perm " + node + " " + value + " " + context + auditSuffix);
            }
            case "unset", "remove" -> {
                if (!subject.unsetPermission(context, node)) {
                    messageHelper.error(sender, subject.getName() + " has no " + node + " entry " + where + ".");
                    return;
                }
                messageHelper.success(sender, "Removed " + node + " from " + subject.getName() + " " + where + ".");
                audit.log(sender, kind(subject) + " " + subject.getName() + " perm " + node + " unset " + context);
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
                audit.log(sender, kind(subject) + " " + subject.getName() + " parent add " + groupName);
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
                audit.log(sender, kind(subject) + " " + subject.getName() + " parent remove " + groupName);
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
            audit.log(sender, kind(subject) + " " + subject.getName() + " option " + key + " unset");
        } else {
            subject.setOption(key, value);
            messageHelper.success(sender, "Set option " + key + " for " + subject.getName() + ".");
            audit.log(sender, kind(subject) + " " + subject.getName() + " option " + key + " " + value);
        }
        commit();
    }

    /**
     * Parses a duration like {@code 7d} or {@code 30m} and returns the epoch
     * millis at which it expires. Returns null if the format is bad.
     */
    private static Long parseDuration(String s) {
        if (s == null || s.isEmpty()) return null;
        char unit = s.charAt(s.length() - 1);
        long multiplier;
        switch (Character.toLowerCase(unit)) {
            case 's' -> multiplier = 1000L;
            case 'm' -> multiplier = 60_000L;
            case 'h' -> multiplier = 3_600_000L;
            case 'd' -> multiplier = 86_400_000L;
            case 'w' -> multiplier = 604_800_000L;
            default -> { return null; }
        }
        try {
            long value = Long.parseLong(s.substring(0, s.length() - 1));
            return System.currentTimeMillis() + (value * multiplier);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String formatExpiry(long epochMillis) {
        return java.time.Instant.ofEpochMilli(epochMillis).toString();
    }
}