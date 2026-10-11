// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.commands;

import com.chalwk.nexus.NexusPermissions;
import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.model.Group;
import com.chalwk.nexus.model.User;
import com.chalwk.nexus.util.AuditLogger;
import com.chalwk.nexus.util.MessageHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles {@code /nexus group <group> <action> ...}.
 *
 * <p>Group names are lowercased before use, matching the storage layer's own
 * normalisation. The "create" action is special-cased before the existence
 * check, since the group obviously doesn't exist yet.</p>
 */
public class GroupCommand {

    private final NexusPermissions plugin;
    private final PermissionManager pm;
    private final MessageHelper messageHelper;
    private final AuditLogger audit;
    private final SubjectEditor editor;

    public GroupCommand(NexusPermissions plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.pm = plugin.getPermissionManager();
        this.messageHelper = messageHelper;
        this.audit = plugin.getAuditLogger();
        this.editor = new SubjectEditor(plugin, "nexus.group", messageHelper);
    }

    /** args[0] = group name, args[1] = action, rest = action arguments. */
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messageHelper.error(sender, "Usage: /nexus group <group> <create|info|perm|parent|option|weight|members|delete> ...");
            return;
        }
        String name = args[0].toLowerCase(Locale.ROOT);
        if (!PermissionManager.isValidName(name)) {
            messageHelper.error(sender, "Invalid group name.");
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        String[] rest = Arrays.copyOfRange(args, 2, args.length);

        // "create" is the only action that runs before the group exists.
        if (action.equals("create")) {
            create(sender, name);
            return;
        }

        Group group = pm.getGroup(name);
        if (group == null) {
            messageHelper.error(sender, "Group '" + name + "' does not exist. Create it with /nexus group " + name + " create");
            return;
        }

        switch (action) {
            case "info" -> info(sender, group);
            case "members" -> members(sender, group);
            case "perm" -> editor.perm(sender, group, rest);
            case "parent" -> editor.parent(sender, group, rest);
            case "option" -> editor.option(sender, group, rest);
            case "weight" -> weight(sender, group, rest);
            case "delete" -> delete(sender, group);
            default -> messageHelper.error(sender, "Unknown action. Use create, info, members, perm, parent, option, weight or delete.");
        }
    }

    private void create(CommandSender sender, String name) {
        if (!sender.hasPermission("nexus.group.create")) {
            messageHelper.noPermission(sender);
            return;
        }
        if (pm.createGroup(name) == null) {
            messageHelper.error(sender, "Group '" + name + "' already exists.");
            return;
        }
        pm.save();
        messageHelper.success(sender, "Created group " + name + ".");
        audit.log(sender, "group " + name + " create");
    }

    /**
     * Deletes a group, first refusing if it's still referenced. Refuses if
     * it's the default group, if any other group has it as a parent, or if
     * it's on a ladder. That way a delete never silently breaks a config (hopefully).
     */
    private void delete(CommandSender sender, Group group) {
        if (!sender.hasPermission("nexus.group.delete")) {
            messageHelper.noPermission(sender);
            return;
        }
        String name = group.getName();

        if (name.equalsIgnoreCase(plugin.getPluginConfig().getDefaultGroup())) {
            messageHelper.error(sender, "Cannot delete the default group.");
            return;
        }

        List<String> referencing = new ArrayList<>();
        for (Group other : pm.getGroups()) {
            if (other == group) continue;
            if (other.hasParent(name)) referencing.add(other.getName());
        }
        if (!referencing.isEmpty()) {
            messageHelper.error(sender, "Group '" + name + "' is a parent of: " + String.join(", ", referencing)
                    + ". Remove those references first.");
            return;
        }

        for (Map.Entry<String, List<String>> e : plugin.getPluginConfig().getLadders().entrySet()) {
            if (e.getValue().contains(name)) {
                messageHelper.error(sender, "Group '" + name + "' is on ladder '" + e.getKey() + "'. Remove it first.");
                return;
            }
        }

        if (!pm.deleteGroup(name)) {
            messageHelper.error(sender, "Could not delete group '" + name + "'.");
            return;
        }
        pm.save();
        pm.refreshAll();
        messageHelper.success(sender, "Deleted group '" + name + "'.");
        audit.log(sender, "group " + name + " delete");
    }

    /**
     * Weight drives parent ordering during permission resolution: lower
     * weight wins, so the group with weight 1 in a chain overrides one with
     * weight 100. Changing it means re-applying to everyone, which is why
     * refreshAll() is called here.
     */
    private void weight(CommandSender sender, Group group, String[] args) {
        if (!sender.hasPermission("nexus.group.weight.set")) {
            messageHelper.noPermission(sender);
            return;
        }
        if (args.length < 1) {
            messageHelper.error(sender, "Usage: /nexus group <group> weight <number>");
            return;
        }
        try {
            group.setWeight(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            messageHelper.error(sender, "Weight must be a whole number.");
            return;
        }
        pm.save();
        pm.refreshAll();
        messageHelper.success(sender, "Set weight of " + group.getName() + " to " + group.getWeight() + " (lower = higher priority).");
        audit.log(sender, "group " + group.getName() + " weight " + group.getWeight());
    }

    /**
     * Prints the group's own entries first, then the effective node count
     * (own plus inherited, resolved against the current context set). The
     * effective count is a useful sanity check when you're chasing a
     * permission that isn't firing as expected.
     */
    private void info(CommandSender sender, Group group) {
        if (!sender.hasPermission("nexus.group.info")) {
            messageHelper.noPermission(sender);
            return;
        }
        messageHelper.header(sender, "Group: " + group.getName());
        messageHelper.kv(sender, "Weight", String.valueOf(group.getWeight()));
        messageHelper.kv(sender, "Parents", group.getParents().isEmpty() ? "none" : String.join(", ", group.getParents()));

        String prefix = pm.getOption(group, "prefix");
        messageHelper.kv(sender, "Prefix", prefix == null ? Component.text("none", NamedTextColor.GRAY)
                : messageHelper.legacy(prefix + "Preview"));

        messageHelper.kv(sender, "Own options", group.getOptions().isEmpty() ? "none"
                : group.getOptions().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", ")));

        // Group permissions are printed per context, since a group can grant
        // different nodes in different worlds.
        boolean any = false;
        for (Map.Entry<String, Map<String, Boolean>> e : group.getAllPermissions().entrySet()) {
            any = true;
            messageHelper.kv(sender, "Perms [" + e.getKey() + "]", UserCommand.formatPerms(e.getValue()));
        }
        if (!any) messageHelper.kv(sender, "Own permissions", "none");
        messageHelper.kv(sender, "Effective nodes (global)",
                String.valueOf(pm.resolvePermissions(group, java.util.List.of()).size()));
    }

    /** Lists users whose effective group set includes this group. */
    private void members(CommandSender sender, Group group) {
        if (!sender.hasPermission("nexus.group.info")) {
            messageHelper.noPermission(sender);
            return;
        }
        List<String> members = new ArrayList<>();
        for (User u : pm.getUsers()) {
            if (pm.getEffectiveParentNames(u).contains(group.getName())) members.add(u.getName());
        }
        if (members.isEmpty()) {
            messageHelper.info(sender, "Group '" + group.getName() + "' has no stored members.");
            return;
        }
        messageHelper.info(sender, "Members of '" + group.getName() + "': " + String.join(", ", members));
    }
}