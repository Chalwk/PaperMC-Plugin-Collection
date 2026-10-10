// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.PerkMenuCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.managers.PerkManager;
import com.chalwk.util.MessageHelper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for PerkMenu.
 *
 * <p>Reads perks from config, determines ownership via permission nodes,
 * and serves a paginated chat menu through /perks.</p>
 */
public class PerkMenu extends JavaPlugin {

    private ConfigManager configManager;
    private PerkManager perkManager;
    private BukkitAudiences audiences;
    private MessageHelper messageHelper;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.perkManager = new PerkManager(this);
        this.audiences = BukkitAudiences.create(this);
        this.messageHelper = new MessageHelper(audiences);

        configManager.loadConfig();

        PluginCommand command = getCommand("perks");
        if (command == null) {
            getLogger().severe("Command 'perks' is not registered in plugin.yml! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(new PerkMenuCommand(this, messageHelper));

        getLogger().info("PerkMenu enabled!");
    }

    @Override
    public void onDisable() {
        if (audiences != null) {
            audiences.close();
        }
        getLogger().info("PerkMenu disabled!");
    }

    public void reload() {
        configManager.reloadConfig();
        getLogger().info("Configuration reloaded!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PerkManager getPerkManager() {
        return perkManager;
    }
}