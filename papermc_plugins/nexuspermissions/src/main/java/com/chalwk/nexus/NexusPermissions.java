// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus;

import com.chalwk.nexus.commands.NexusCommand;
import com.chalwk.nexus.config.ConfigManager;
import com.chalwk.nexus.config.PluginConfig;
import com.chalwk.nexus.hook.VaultChatHook;
import com.chalwk.nexus.hook.VaultHook;
import com.chalwk.nexus.listener.PlayerListener;
import com.chalwk.nexus.manager.PermissionManager;
import com.chalwk.nexus.util.MessageHelper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin entry point.
 *
 * <p>Wires together the config manager, the permission manager, the
 * command handler and the join/quit listener. The BukkitAudiences instance
 * is created here and passed into the message helper, which is then handed
 * to every command so they all share the same sender bridge.</p>
 */
public final class NexusPermissions extends JavaPlugin {

    private PluginConfig pluginConfig;
    private ConfigManager configManager;
    private PermissionManager permissionManager;
    private BukkitAudiences audiences;
    private MessageHelper messageHelper;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.audiences = BukkitAudiences.create(this);
        this.messageHelper = new MessageHelper(audiences);

        pluginConfig = new PluginConfig(this);
        pluginConfig.load();

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

        // Vault is a soft dependency: only touch its classes if it's there.
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            hookVault();
        }

        // Covers /reload confirm, where players stay connected across a
        // disable/enable cycle and would otherwise be left unattached.
        permissionManager.refreshAll();

        getLogger().info("NexusPermissions enabled.");
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

    @Override
    public void onDisable() {
        if (permissionManager != null) permissionManager.shutdown();
        if (audiences != null) audiences.close();
    }

    /** Reloads config.yml, groups.yml and users.yml, then re-applies to online players. */
    public void reloadAll() {
        reloadConfig();
        pluginConfig.load();
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

    public MessageHelper getMessageHelper() {
        return messageHelper;
    }
}