// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.MagnetCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.managers.MagnetManager;
import com.chalwk.util.EconomyHelper;
import com.chalwk.util.MessageHelper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class VacuLoot extends JavaPlugin {

    private ConfigManager configManager;
    private MagnetManager magnetManager;
    private EconomyHelper economyHelper;
    private BukkitAudiences audiences;
    private MessageHelper messageHelper;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.magnetManager = new MagnetManager(this);
        this.audiences = BukkitAudiences.create(this);
        this.messageHelper = new MessageHelper(audiences);

        configManager.loadConfig();

        if (configManager.getConfig().isEconomyEnabled()) {
            this.economyHelper = new EconomyHelper(this);
            if (!economyHelper.setupEconomy()) {
                getLogger().warning("Economy plugin not found! Disabling economy features.");
            }
        }

        PluginCommand command = getCommand("magnet");
        if (command == null) {
            getLogger().severe("Command 'magnet' is not registered in plugin.yml! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(new MagnetCommand(this, messageHelper));

        magnetManager.startMagnetTask();

        getLogger().info("VacuLoot enabled! Item magnet is now available.");
    }

    @Override
    public void onDisable() {
        if (magnetManager != null) {
            magnetManager.stopMagnetTask();
        }
        if (audiences != null) {
            audiences.close();
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