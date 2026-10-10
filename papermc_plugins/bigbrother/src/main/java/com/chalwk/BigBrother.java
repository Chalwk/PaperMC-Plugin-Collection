// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.BigBrotherCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.listeners.SpyListener;
import com.chalwk.managers.SpyManager;
import com.chalwk.util.MessageHelper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for BigBrother.
 *
 * <p>Owns the plugin lifecycle, wires the command and listener into the server,
 * and keeps hold of the shared config and spy state managers so other classes
 * can reach them through the plugin instance.</p>
 */
public class BigBrother extends JavaPlugin {

    private ConfigManager configManager;
    private SpyManager spyManager;
    private BukkitAudiences audiences;
    private MessageHelper messageHelper;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.spyManager = new SpyManager(this);

        // BukkitAudiences is the bridge that lets us send Adventure Components on
        // both Spigot and Paper. It holds some internal state, so we create it here
        // and close it in onDisable rather than making it a one-shot per call.
        this.audiences = BukkitAudiences.create(this);
        this.messageHelper = new MessageHelper(audiences);

        configManager.loadConfig();

        // plugin.yml could be missing the command if someone edited it or if the
        // resource substitution failed during build. Bail early rather than NPE.
        PluginCommand command = getCommand("bigbrother");
        if (command == null) {
            getLogger().severe("Command 'bigbrother' is not registered in plugin.yml! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(new BigBrotherCommand(this, messageHelper));

        getServer().getPluginManager().registerEvents(new SpyListener(this, messageHelper), this);

        getLogger().info("BigBrother enabled! Monitoring player activity.");
    }

    @Override
    public void onDisable() {
        // Closing audiences releases the internal packet listeners that
        // adventure-platform registers. Skipping this is what causes weird
        // behaviour after /reload on some server implementations.
        if (audiences != null) {
            audiences.close();
        }
        getLogger().info("BigBrother disabled!");
    }

    /**
     * Reloads config from disk. Called by {@code /bigbrother reload}.
     *
     * <p>Does not touch the spy manager's per-player state. Players keep
     * whatever they had enabled before the reload.</p>
     */
    public void reload() {
        configManager.reloadConfig();
        getLogger().info("Configuration reloaded!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SpyManager getSpyManager() {
        return spyManager;
    }
}