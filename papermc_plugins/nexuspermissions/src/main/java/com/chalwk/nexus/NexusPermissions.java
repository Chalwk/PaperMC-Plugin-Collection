// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus;

import com.chalwk.nexus.api.NexusAPI;
import com.chalwk.nexus.api.NexusAPIImpl;
import com.chalwk.nexus.commands.NexusCommand;
import com.chalwk.nexus.config.ConfigManager;
import com.chalwk.nexus.config.PluginConfig;
import com.chalwk.nexus.hook.NexusPlaceholderExpansion;
import com.chalwk.nexus.hook.VaultChatHook;
import com.chalwk.nexus.hook.VaultHook;
import com.chalwk.nexus.listener.PlayerListener;
import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.util.AuditLogger;
import com.chalwk.nexus.util.MessageHelper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin entry point.
 *
 * <p>Wires together the config manager, the permission manager, the
 * command handler, the join/quit listener, and the three optional hooks
 * (Vault, PlaceholderAPI, WorldGuard). All hooks are guarded so the plugin
 * loads and works with none of them present.</p>
 */
public final class NexusPermissions extends JavaPlugin {

    private PluginConfig pluginConfig;
    private ConfigManager configManager;
    private PermissionManager permissionManager;
    private AuditLogger auditLogger;
    private BukkitAudiences audiences;
    private MessageHelper messageHelper;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.audiences = BukkitAudiences.create(this);
        this.messageHelper = new MessageHelper(audiences);

        pluginConfig = new PluginConfig(this);
        pluginConfig.load();

        auditLogger = new AuditLogger(this);
        auditLogger.setEnabled(pluginConfig.isAuditLogEnabled());

        permissionManager = new PermissionManager(this);
        configManager = new ConfigManager(this);

        // If loading fails, ConfigManager locks saving so a broken YAML
        // can't wipe live data. The plugin still starts so that an admin
        // can fix the file and run /nexus reload, but mutating commands
        // will refuse to persist until the lock clears.
        if (!configManager.load(permissionManager)) {
            getLogger().severe("NexusPermissions started in a locked state. "
                    + "Fix the YAML and run /nexus reload before making changes.");
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        PluginCommand command = getCommand("nexus");
        if (command != null) {
            NexusCommand handler = new NexusCommand(this, messageHelper);
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        } else {
            getLogger().severe("Command 'nexus' is missing from plugin.yml! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerApi();

        // Vault is a soft dependency: only touch its classes if it's there.
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            hookVault();
        }

        // PlaceholderAPI expansion, if PAPI is present.
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new NexusPlaceholderExpansion(this).register();
                getLogger().info("Registered PlaceholderAPI expansion 'nexus'.");
            } catch (LinkageError e) {
                getLogger().warning("PlaceholderAPI is installed but the expansion could not be registered: " + e);
            }
        }

        scheduleTimedPermissionSweep();

        // Covers /reload confirm, where players stay connected across a
        // disable/enable cycle and would otherwise be left unattached.
        permissionManager.refreshAll();

        getLogger().info("NexusPermissions enabled.");
    }

    /** Publishes the read API so other plugins can query Nexus directly. */
    private void registerApi() {
        NexusAPIImpl api = new NexusAPIImpl(permissionManager);
        getServer().getServicesManager().register(NexusAPI.class, api, this, ServicePriority.Normal);
    }

    /**
     * Registers Nexus as Vault's permission and chat provider.
     *
     * <p>Kept in its own method so Vault classes are only resolved when this
     * actually runs. Highest priority makes Nexus win over any other
     * provider. Bukkit drops the registrations on its own when the plugin
     * disables, so there's nothing to undo in onDisable.</p>
     */
    private void hookVault() {
        try {
            VaultHook perms = new VaultHook(permissionManager);
            getServer().getServicesManager().register(
                    net.milkbowl.vault.permission.Permission.class, perms, this, ServicePriority.Highest);
            getServer().getServicesManager().register(
                    net.milkbowl.vault.chat.Chat.class, new VaultChatHook(permissionManager, perms), this, ServicePriority.Highest);
            getLogger().info("Vault found, registered as permission and chat provider.");
        } catch (LinkageError e) {
            // Vault is present but too old or broken to link against.
            getLogger().warning("Vault is installed but could not be hooked: " + e);
        }
    }

    /**
     * Runs a periodic task that prunes expired timed permissions. When the
     * sweep removes anything, it saves and refreshes online players so the
     * change takes effect immediately. Interval comes from config.
     */
    private void scheduleTimedPermissionSweep() {
        long periodTicks = pluginConfig.getTimedPermissionSweepSeconds() * 20L;
        getServer().getScheduler().runTaskTimer(this, () -> {
            boolean changed = permissionManager.purgeExpired();
            if (changed) {
                configManager.saveAll(permissionManager);
                permissionManager.refreshAll();
            }
        }, periodTicks, periodTicks);
    }

    @Override
    public void onDisable() {
        if (permissionManager != null) permissionManager.shutdown();
        if (audiences != null) audiences.close();
    }

    /** Reloads config.yml, groups.yml and users.yml, then re-applies to online players. */
    public void reloadAll() {
        reloadConfig();
        pluginConfig.load();
        auditLogger.setEnabled(pluginConfig.isAuditLogEnabled());
        configManager.load(permissionManager);
        permissionManager.refreshAll();
    }

    public void debug(String message) {
        if (pluginConfig != null && pluginConfig.isDebug()) getLogger().info("[debug] " + message);
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public AuditLogger getAuditLogger() {
        return auditLogger;
    }

    public MessageHelper getMessageHelper() {
        return messageHelper;
    }
}