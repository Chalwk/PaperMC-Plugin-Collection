// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.AdminChatCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.listeners.AdminChatListener;
import com.chalwk.managers.AdminChatManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AdminChat extends JavaPlugin {

    private ConfigManager configManager;
    private AdminChatManager chatManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.chatManager = new AdminChatManager(this);

        configManager.loadConfig();

        AdminChatCommand command = new AdminChatCommand(this);
        getCommand("achat").setExecutor(command);
        getCommand("adminchat").setExecutor(command);

        getServer().getPluginManager().registerEvents(new AdminChatListener(this), this);

        getLogger().info("AdminChat enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AdminChat disabled!");
    }

    public void reload() {
        configManager.reloadConfig();
        getLogger().info("Configuration reloaded!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public AdminChatManager getChatManager() {
        return chatManager;
    }
}