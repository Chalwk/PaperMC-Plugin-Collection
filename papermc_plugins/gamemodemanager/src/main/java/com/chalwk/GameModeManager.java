// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk;

import com.chalwk.commands.GameModeCommand;
import com.chalwk.config.ConfigManager;
import com.chalwk.listeners.GameModeListener;
import com.chalwk.listeners.PlayerDataListener;
import com.chalwk.listeners.WorldSwitchListener;
import com.chalwk.managers.InventoryManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameModeManager extends JavaPlugin {

    private final Map<UUID, GameMode> pendingWorldSwitchGameMode = new HashMap<>();
    private ConfigManager configManager;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.inventoryManager = new InventoryManager(this);

        configManager.loadConfig();

        getCommand("gmmanage").setExecutor(new GameModeCommand(this));

        getServer().getPluginManager().registerEvents(new GameModeListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldSwitchListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);

        getLogger().info("GameModeManager enabled! Inventories and player states are now gamemode-specific.");
    }

    @Override
    public void onDisable() {
        if (inventoryManager != null) {
            inventoryManager.saveAllPlayers();
        }
        getLogger().info("GameModeManager disabled!");
    }

    public void reload() {
        configManager.reloadConfig();
        getLogger().info("Configuration reloaded!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public void setPendingGameMode(Player player, GameMode gameMode) {
        pendingWorldSwitchGameMode.put(player.getUniqueId(), gameMode);
    }

    public GameMode consumePendingGameMode(Player player) {
        return pendingWorldSwitchGameMode.remove(player.getUniqueId());
    }

    public void clearPendingGameMode(UUID uuid) {
        pendingWorldSwitchGameMode.remove(uuid);
    }
}