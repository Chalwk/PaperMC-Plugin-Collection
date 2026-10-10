// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.NightVisionCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.managers.NightVisionManager;
import com.chalwk.util.MessageHelper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class NoctiView extends JavaPlugin {

    private ConfigManager configManager;
    private NightVisionManager nightVisionManager;
    private BukkitAudiences audiences;
    private MessageHelper messageHelper;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.audiences = BukkitAudiences.create(this);
        this.messageHelper = new MessageHelper(audiences);
        this.nightVisionManager = new NightVisionManager(this, messageHelper);

        configManager.loadConfig();

        PluginCommand command = getCommand("nightvision");
        if (command == null) {
            getLogger().severe("Command 'nightvision' is not registered in plugin.yml! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(new NightVisionCommand(this, messageHelper));

        getLogger().info("NoctiView enabled!");
    }

    @Override
    public void onDisable() {
        if (nightVisionManager != null) {
            nightVisionManager.removeAllNightVision();
        }
        if (audiences != null) {
            audiences.close();
        }
        getLogger().info("NoctiView disabled!");
    }

    public void reload() {
        configManager.reloadConfig();
        getLogger().info("Configuration reloaded!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public NightVisionManager getNightVisionManager() {
        return nightVisionManager;
    }
}