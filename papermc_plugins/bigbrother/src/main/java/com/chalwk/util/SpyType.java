// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.util;

/**
 * The spy types BigBrother knows about.
 *
 * <p>Each constant bundles the four strings that have to stay in sync:
 * the subcommand the user types, the key in {@code config.yml}, the
 * permission needed to toggle it, and the permission needed to toggle it
 * for someone else.</p>
 *
 * <p>Adding a new spy is a matter of adding a constant here, a config
 * section under {@code spy:} in config.yml, and the corresponding
 * permissions in plugin.yml. The command, help output, status listing, and
 * tab completion all iterate over {@link #values()} and pick up the new
 * entry automatically.</p>
 */
public enum SpyType {
    COMMAND("commands", "command", "bigbrother.commandspy.toggle", "bigbrother.commandspy.toggle.others"),
    SIGN("signs", "sign", "bigbrother.signspy.toggle", "bigbrother.signspy.toggle.others"),
    ANVIL("anvils", "anvil", "bigbrother.anvilspy.toggle", "bigbrother.anvilspy.toggle.others"),
    BOOK("books", "book", "bigbrother.bookspy.toggle", "bigbrother.bookspy.toggle.others"),
    PORTAL("portals", "portal", "bigbrother.portalspy.toggle", "bigbrother.portalspy.toggle.others");

    private final String command;
    private final String configKey;
    private final String permission;
    private final String permissionOthers;

    SpyType(String command, String configKey, String permission, String permissionOthers) {
        this.command = command;
        this.configKey = configKey;
        this.permission = permission;
        this.permissionOthers = permissionOthers;
    }

    /**
     * Resolves a user-typed subcommand back to a spy type.
     *
     * @return the matching type, or {@code null} if the input isn't a spy
     *         subcommand (which lets the caller fall through to the
     *         unknown-command message).
     */
    public static SpyType fromCommand(String command) {
        for (SpyType type : values()) {
            if (type.getCommand().equalsIgnoreCase(command)) {
                return type;
            }
        }
        return null;
    }

    /** The subcommand a user types, e.g. {@code signs}. */
    public String getCommand() {
        return command;
    }

    /** The key under {@code spy:} in config.yml, e.g. {@code sign}. */
    public String getConfigKey() {
        return configKey;
    }

    /** Permission to toggle this spy for yourself. */
    public String getPermission() {
        return permission;
    }

    /** Permission to toggle this spy for other players. */
    public String getPermissionOthers() {
        return permissionOthers;
    }
}