// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.HandbookCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.listeners.PlayerJoinListener;
import com.chalwk.managers.HandbookManager;
import com.chalwk.util.MessageHelper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for Handbook.
 *
 * <p>
 * Gives new players a written book guide to the server, and exposes
 * {@code /handbook} for re-issuing and administration.
 * </p>
 */
public class Handbook extends JavaPlugin {

    private ConfigManager configManager;
    private HandbookManager handbookManager;
    private BukkitAudiences audiences;
    private MessageHelper messageHelper;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.handbookManager = new HandbookManager(this);
        this.audiences = BukkitAudiences.create(this);
        this.messageHelper = new MessageHelper(audiences);

        configManager.loadConfig();

        PluginCommand command = getCommand("handbook");
        if (command == null) {
            getLogger().severe("Command 'handbook' is not registered in plugin.yml! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(new HandbookCommand(this, messageHelper));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getLogger().info("Handbook enabled!");
    }

    @Override
    public void onDisable() {
        if (audiences != null) {
            audiences.close();
        }
        getLogger().info("Handbook disabled!");
    }

    public void reload() {
        configManager.reloadConfig();
        getLogger().info("Configuration reloaded!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HandbookManager getHandbookManager() {
        return handbookManager;
    }
}