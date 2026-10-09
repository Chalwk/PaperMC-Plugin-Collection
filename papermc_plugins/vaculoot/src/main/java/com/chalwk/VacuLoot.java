// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.MagnetCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.managers.MagnetManager;
import com.chalwk.util.EconomyHelper;
import org.bukkit.plugin.java.JavaPlugin;

public class VacuLoot extends JavaPlugin {

    private ConfigManager configManager;
    private MagnetManager magnetManager;
    private EconomyHelper economyHelper;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.magnetManager = new MagnetManager(this);

        configManager.loadConfig();

        if (configManager.getConfig().isEconomyEnabled()) {
            this.economyHelper = new EconomyHelper(this);
            if (!economyHelper.setupEconomy()) {
                getLogger().warning("Economy plugin not found! Disabling economy features.");
            }
        }

        getCommand("magnet").setExecutor(new MagnetCommand(this));
        magnetManager.startMagnetTask();

        getLogger().info("VacuLoot enabled! Item magnet is now available.");
    }

    @Override
    public void onDisable() {
        if (magnetManager != null) {
            magnetManager.stopMagnetTask();
        }
        getLogger().info("VacuLoot disabled!");
    }

    public void reload() {
        configManager.reloadConfig();
        getLogger().info("Configuration reloaded!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MagnetManager getMagnetManager() {
        return magnetManager;
    }

    public EconomyHelper getEconomyHelper() {
        return economyHelper;
    }
}