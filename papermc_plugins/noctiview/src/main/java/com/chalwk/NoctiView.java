// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.NightVisionCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.managers.NightVisionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class NoctiView extends JavaPlugin {

    private ConfigManager configManager;
    private NightVisionManager nightVisionManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.nightVisionManager = new NightVisionManager(this);

        configManager.loadConfig();

        getCommand("nightvision").setExecutor(new NightVisionCommand(this));

        getLogger().info("NoctiView enabled!");
    }

    @Override
    public void onDisable() {
        if (nightVisionManager != null) {
            nightVisionManager.removeAllNightVision();
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