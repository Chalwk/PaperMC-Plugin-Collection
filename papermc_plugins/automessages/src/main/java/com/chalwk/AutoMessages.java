// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.AutoMessagesCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.managers.MessageScheduler;
import com.chalwk.util.MessageHelper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for AutoMessages.
 *
 * <p>
 * Owns the config manager, the broadcast scheduler, and the Adventure
 * audiences bridge. Everything else reaches them through getters or
 * constructor injection.
 * </p>
 */
public class AutoMessages extends JavaPlugin {

    private ConfigManager configManager;
    private MessageScheduler messageScheduler;
    private BukkitAudiences audiences;
    private MessageHelper messageHelper;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);

        // BukkitAudiences needs to live for the plugin's whole lifetime and be
        // closed cleanly on disable, so we set it up before anything that
        // might send a message.
        this.audiences = BukkitAudiences.create(this);
        this.messageHelper = new MessageHelper(audiences);

        this.messageScheduler = new MessageScheduler(this, messageHelper);

        configManager.loadConfig();

        PluginCommand command = getCommand("automessages");
        if (command == null) {
            getLogger().severe("Command 'automessages' is not registered in plugin.yml! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(new AutoMessagesCommand(this, messageHelper));

        messageScheduler.start();

        getLogger().info("AutoMessages enabled!");
    }

    @Override
    public void onDisable() {
        if (messageScheduler != null) {
            messageScheduler.stop();
        }
        // Release the internal packet listeners Adventure registers. Without
        // this, /reload on some server implementations leaves dangling state.
        if (audiences != null) {
            audiences.close();
        }
        getLogger().info("AutoMessages disabled!");
    }

    /**
     * Reloads config and restarts the scheduler. Called by
     * {@code /automessages reload}.
     */
    public void reload() {
        configManager.reloadConfig();
        messageScheduler.restart();
        getLogger().info("Configuration reloaded and scheduler restarted!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageScheduler getMessageScheduler() {
        return messageScheduler;
    }
}