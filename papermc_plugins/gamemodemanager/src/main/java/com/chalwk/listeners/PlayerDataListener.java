// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.listeners;

import com.chalwk.GameModeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataListener implements Listener {
    private final GameModeManager plugin;

    public PlayerDataListener(GameModeManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getInventoryManager().loadPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getInventoryManager().savePlayer(player);
        plugin.getInventoryManager().removePlayerData(player.getUniqueId());
        plugin.clearPendingGameMode(player.getUniqueId());
    }
}