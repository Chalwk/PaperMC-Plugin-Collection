// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.AdminChatCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.listeners.AdminChatListener;
import com.chalwk.managers.AdminChatManager;
import com.chalwk.util.MessageHelper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class AdminChat extends JavaPlugin {

    private ConfigManager configManager;
    private AdminChatManager chatManager;
    private BukkitAudiences audiences;
    private MessageHelper messageHelper;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.audiences = BukkitAudiences.create(this);
        this.messageHelper = new MessageHelper(audiences);
        this.chatManager = new AdminChatManager(this, messageHelper);

        configManager.loadConfig();

        // Both commands share the same executor. Either being absent is a
        // plugin.yml problem worth bailing out on.
        PluginCommand achat = getCommand("achat");
        PluginCommand adminchat = getCommand("adminchat");
        if (achat == null || adminchat == null) {
            getLogger().severe("Commands 'achat' or 'adminchat' are not registered in plugin.yml! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        AdminChatCommand command = new AdminChatCommand(this, messageHelper);
        achat.setExecutor(command);
        adminchat.setExecutor(command);

        getServer().getPluginManager().registerEvents(new AdminChatListener(this, messageHelper), this);

        getLogger().info("AdminChat enabled!");
    }

    @Override
    public void onDisable() {
        if (audiences != null) {
            audiences.close();
        }
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