// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.listeners;

import com.chalwk.GameModeManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class GameModeListener implements Listener {
    private final GameModeManager plugin;

    public GameModeListener(GameModeManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode newGm = event.getNewGameMode();

        plugin.getInventoryManager().switchGamemode(player, newGm);
    }
}