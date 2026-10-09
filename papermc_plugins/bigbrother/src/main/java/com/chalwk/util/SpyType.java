// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.util;

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

    public static SpyType fromCommand(String command) {
        for (SpyType type : values()) {
            if (type.getCommand().equalsIgnoreCase(command)) {
                return type;
            }
        }
        return null;
    }

    public String getCommand() {
        return command;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getPermission() {
        return permission;
    }

    public String getPermissionOthers() {
        return permissionOthers;
    }
}