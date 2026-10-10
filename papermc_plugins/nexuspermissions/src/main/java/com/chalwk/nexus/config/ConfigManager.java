// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.config;

import com.chalwk.nexus.NexusPermissions;
import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.model.Group;
import com.chalwk.nexus.model.Subject;
import com.chalwk.nexus.model.User;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Loads and saves groups.yml and users.yml.
 *
 * <p>Saving is disabled after a failed load, so a YAML syntax error can't
 * turn the next command invocation into a wipe of the real data. Fix the
 * file and run /nexus reload to unlock.</p>
 */
public class ConfigManager {

    private final NexusPermissions plugin;
    private final File groupsFile;
    private final File usersFile;
    /** True after a failed load: saving is blocked so a YAML typo can't wipe the real data. */
    private boolean locked = false;

    public ConfigManager(NexusPermissions plugin) {
        this.plugin = plugin;
        this.groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        this.usersFile = new File(plugin.getDataFolder(), "users.yml");
        if (!groupsFile.exists()) plugin.saveResource("groups.yml", false);
        if (!usersFile.exists()) plugin.saveResource("users.yml", false);
    }

    /**
     * A fresh YAML instance with '/' as the path separator. The default
     * separator is '.', which would mangle any group or user whose name
     * contains a period. NexusPermissions doesn't allow those, but it's
     * cheap insurance.
     */
    private YamlConfiguration newYaml() {
        YamlConfiguration yml = new YamlConfiguration();
        yml.options().pathSeparator('/');
        return yml;
    }

    private YamlConfiguration loadYaml(File file) {
        YamlConfiguration yml = newYaml();
        try {
            yml.load(file);
            return yml;
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load " + file.getName(), e);
            return null;
        }
    }

    /** @return true if data was loaded successfully. */
    public boolean load(PermissionManager manager) {
        YamlConfiguration groupsYml = loadYaml(groupsFile);
        YamlConfiguration usersYml = loadYaml(usersFile);
        if (groupsYml == null || usersYml == null) {
            locked = true;
            plugin.getLogger().severe("Data NOT reloaded and saving is disabled until the YAML is fixed and /nexus reload succeeds.");
            return false;
        }
        locked = false;
        manager.clear();

        ConfigurationSection groups = groupsYml.getConfigurationSection("groups");
        if (groups != null) {
            for (String key : groups.getKeys(false)) {
                ConfigurationSection sec = groups.getConfigurationSection(key);
                Group g = new Group(key.toLowerCase(Locale.ROOT));
                if (sec != null) readSubject(g, sec);
                manager.addGroup(g);
            }
        }

        ConfigurationSection users = usersYml.getConfigurationSection("users");
        if (users != null) {
            for (String key : users.getKeys(false)) {
                ConfigurationSection sec = users.getConfigurationSection(key);
                User u = new User(key);
                if (sec != null) readSubject(u, sec);
                manager.addUser(u);
            }
        }

        // The default group has to exist, or new players would fall through
        // to nothing at all. Create an empty one rather than abort.
        String def = plugin.getPluginConfig().getDefaultGroup();
        if (manager.getGroup(def) == null) {
            manager.createGroup(def);
            plugin.getLogger().warning("Default group '" + def + "' was missing and has been created (empty).");
        }
        plugin.debug("Loaded " + manager.getGroups().size() + " groups and " + manager.getUsers().size() + " users.");
        return true;
    }

    public void saveAll(PermissionManager manager) {
        if (locked) {
            plugin.getLogger().severe("Refusing to save: data failed to load. Fix the YAML and run /nexus reload.");
            return;
        }
        saveGroups(manager);
        saveUsers(manager);
    }

    private void saveGroups(PermissionManager manager) {
        YamlConfiguration yml = newYaml();
        yml.options().setHeader(List.of("NexusPermissions Groups"));
        ConfigurationSection root = yml.createSection("groups");
        for (Group g : manager.getGroups()) {
            writeSubject(root.createSection(g.getName()), g);
        }
        write(yml, groupsFile);
    }

    private void saveUsers(PermissionManager manager) {
        YamlConfiguration yml = newYaml();
        yml.options().setHeader(List.of("NexusPermissions Users", "Only users with specific modifications are saved here."));
        ConfigurationSection root = yml.createSection("users");
        for (User u : manager.getUsers()) {
            // Empty users are transient entries created during command
            // handling and shouldn't end up on disk.
            if (u.isEmpty()) continue;
            writeSubject(root.createSection(u.getName()), u);
        }
        write(yml, usersFile);
    }

    private void write(YamlConfiguration yml, File file) {
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + file.getName(), e);
        }
    }

    // ---- (de)serialisation ----

    private void readSubject(Subject s, ConfigurationSection sec) {
        ConfigurationSection opts = sec.getConfigurationSection("options");
        if (opts != null) {
            for (String k : opts.getKeys(false)) {
                Object v = opts.get(k);
                if (v != null) s.setOption(k, String.valueOf(v));
            }
        }
        readPerms(s, Subject.GLOBAL, sec.getStringList("permissions"));

        // Per-context permissions live in a nested section, one list per
        // context name.
        ConfigurationSection ctxs = sec.getConfigurationSection("contexts");
        if (ctxs != null) {
            for (String ctx : ctxs.getKeys(false)) readPerms(s, ctx, ctxs.getStringList(ctx));
        }
        for (String p : sec.getStringList("parents")) s.addParent(p);
    }

    /**
     * Reads a permission list. A leading '-' on a line means the node is
     * negated, which is how NexusPermissions stores explicit denials on
     * disk. Everything else is a grant.
     */
    private void readPerms(Subject s, String context, List<String> list) {
        for (String raw : list) {
            if (raw == null || raw.isBlank()) continue;
            String node = raw.trim();
            boolean value = true;
            if (node.startsWith("-")) {
                value = false;
                node = node.substring(1);
            }
            if (!node.isEmpty()) s.setPermission(context, node, value);
        }
    }

    private void writeSubject(ConfigurationSection sec, Subject s) {
        if (!s.getOptions().isEmpty()) {
            ConfigurationSection opts = sec.createSection("options");
            for (Map.Entry<String, String> e : s.getOptions().entrySet()) {
                // Store whole numbers as numbers, so weight and rank don't
                // come back as strings after a round-trip.
                opts.set(e.getKey(), asNumberIfPossible(e.getValue()));
            }
        }
        sec.set("permissions", toList(s.getPermissions(Subject.GLOBAL)));

        ConfigurationSection ctxs = null;
        for (Map.Entry<String, Map<String, Boolean>> e : s.getAllPermissions().entrySet()) {
            if (e.getKey().equals(Subject.GLOBAL)) continue;
            if (ctxs == null) ctxs = sec.createSection("contexts");
            ctxs.set(e.getKey(), toList(e.getValue()));
        }
        sec.set("parents", new ArrayList<>(s.getParents()));
    }

    /** Granted nodes go on the list plain, denied ones get a leading '-'. */
    private List<String> toList(Map<String, Boolean> perms) {
        List<String> out = new ArrayList<>();
        perms.forEach((node, value) -> out.add(value ? node : "-" + node));
        return out;
    }

    private Object asNumberIfPossible(String v) {
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return v;
        }
    }
}