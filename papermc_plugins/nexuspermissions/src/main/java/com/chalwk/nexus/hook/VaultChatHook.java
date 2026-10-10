// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.hook;

import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.model.Subject;
import com.chalwk.nexus.model.User;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import java.util.Locale;

/**
 * Vault chat provider: prefixes, suffixes and free-form options.
 *
 * <p>Vault splits "who has what" ({@link VaultHook}) from "what does this
 * player look like in chat" (this class), and chat or tab plugins ask the
 * second one for prefixes. Without it they would still see empty strings.</p>
 *
 * <p>Everything resolves through {@link PermissionManager#getOption}, so a
 * user's own value wins and then parents are checked by weight. Options
 * have no context support in Nexus, so the world argument is ignored.
 * Prefix and suffix come back as {@code ""} rather than null because plenty
 * of callers concatenate them straight into a string. Read-only, like the
 * permission hook.</p>
 */
public class VaultChatHook extends Chat {

    private final PermissionManager pm;

    /** @param perms the permission provider Vault's group helpers on {@link Chat} delegate to. */
    public VaultChatHook(PermissionManager pm, Permission perms) {
        super(perms);
        this.pm = pm;
    }

    private static UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException(
                "NexusPermissions is read-only through Vault. Use the /nexus command instead (see /nexus help).");
    }

    @Override
    public String getName() {
        return "NexusPermissions";
    }

    /** Always true, see {@link VaultHook#isEnabled()}. */
    @Override
    public boolean isEnabled() {
        return true;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Stored user if there is one, otherwise a transient view that falls back to the default group. */
    private User user(String name) {
        if (name == null) return null;
        User stored = pm.getUser(name);
        return stored != null ? stored : new User(name);
    }

    /**
     * Resolves an option, trying the key as given and then lowercased.
     * Commands always lowercase keys, but a hand-edited YAML file might not.
     */
    private String optionOf(Subject subject, String key) {
        if (subject == null || key == null) return null;
        String v = pm.getOption(subject, key);
        return v != null ? v : pm.getOption(subject, key.toLowerCase(Locale.ROOT));
    }

    private static String orEmpty(String s) {
        return s == null ? "" : s;
    }

    // ------------------------------------------------------------------
    // Prefix and suffix
    // ------------------------------------------------------------------

    @Override
    public String getPlayerPrefix(String world, String name) {
        return orEmpty(optionOf(user(name), "prefix"));
    }

    @Override
    public String getPlayerSuffix(String world, String name) {
        return orEmpty(optionOf(user(name), "suffix"));
    }

    @Override
    public String getGroupPrefix(String world, String name) {
        return orEmpty(optionOf(pm.getGroup(name), "prefix"));
    }

    @Override
    public String getGroupSuffix(String world, String name) {
        return orEmpty(optionOf(pm.getGroup(name), "suffix"));
    }

    // ------------------------------------------------------------------
    // Free-form options
    // ------------------------------------------------------------------

    @Override
    public int getPlayerInfoInteger(String world, String name, String node, int defaultValue) {
        String raw = optionOf(user(name), node);
        if (raw == null) return defaultValue;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public double getPlayerInfoDouble(String world, String name, String node, double defaultValue) {
        String raw = optionOf(user(name), node);
        if (raw == null) return defaultValue;
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getPlayerInfoBoolean(String world, String name, String node, boolean defaultValue) {
        String raw = optionOf(user(name), node);
        if (raw == null) return defaultValue;
        String t = raw.trim();
        if (t.equalsIgnoreCase("true")) return true;
        if (t.equalsIgnoreCase("false")) return false;
        return defaultValue;
    }

    @Override
    public String getPlayerInfoString(String world, String name, String node, String defaultValue) {
        String raw = optionOf(user(name), node);
        return raw != null ? raw : defaultValue;
    }

    @Override
    public int getGroupInfoInteger(String world, String name, String node, int defaultValue) {
        String raw = optionOf(pm.getGroup(name), node);
        if (raw == null) return defaultValue;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public double getGroupInfoDouble(String world, String name, String node, double defaultValue) {
        String raw = optionOf(pm.getGroup(name), node);
        if (raw == null) return defaultValue;
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getGroupInfoBoolean(String world, String name, String node, boolean defaultValue) {
        String raw = optionOf(pm.getGroup(name), node);
        if (raw == null) return defaultValue;
        String t = raw.trim();
        if (t.equalsIgnoreCase("true")) return true;
        if (t.equalsIgnoreCase("false")) return false;
        return defaultValue;
    }

    @Override
    public String getGroupInfoString(String world, String name, String node, String defaultValue) {
        String raw = optionOf(pm.getGroup(name), node);
        return raw != null ? raw : defaultValue;
    }

    // ------------------------------------------------------------------
    // Writes, all blocked
    // ------------------------------------------------------------------

    @Override
    public void setPlayerInfoInteger(String world, String name, String node, int value) {
        throw readOnly();
    }

    @Override
    public void setPlayerInfoDouble(String world, String name, String node, double value) {
        throw readOnly();
    }

    @Override
    public void setPlayerInfoBoolean(String world, String name, String node, boolean value) {
        throw readOnly();
    }

    @Override
    public void setPlayerInfoString(String world, String name, String node, String value) {
        throw readOnly();
    }

    @Override
    public void setGroupInfoInteger(String world, String name, String node, int value) {
        throw readOnly();
    }

    @Override
    public void setGroupInfoDouble(String world, String name, String node, double value) {
        throw readOnly();
    }

    @Override
    public void setGroupInfoBoolean(String world, String name, String node, boolean value) {
        throw readOnly();
    }

    @Override
    public void setGroupInfoString(String world, String name, String node, String value) {
        throw readOnly();
    }

    @Override
    public void setPlayerPrefix(String world, String name, String value) {
        throw readOnly();
    }

    @Override
    public void setPlayerSuffix(String world, String name, String value) {
        throw readOnly();
    }

    @Override
    public void setGroupPrefix(String world, String name, String value) {
        throw readOnly();
    }

    @Override
    public void setGroupSuffix(String world, String name, String value) {
        throw readOnly();
    }
}
