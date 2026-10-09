// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.AutoMessagesCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.managers.MessageScheduler;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoMessages extends JavaPlugin {

    private ConfigManager configManager;
    private MessageScheduler messageScheduler;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.messageScheduler = new MessageScheduler(this);

        configManager.loadConfig();

        getCommand("automessages").setExecutor(new AutoMessagesCommand(this));

        messageScheduler.start();

        getLogger().info("AutoMessages enabled!");
    }

    @Override
    public void onDisable() {
        if (messageScheduler != null) {
            messageScheduler.stop();
        }
        getLogger().info("AutoMessages disabled!");
    }

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