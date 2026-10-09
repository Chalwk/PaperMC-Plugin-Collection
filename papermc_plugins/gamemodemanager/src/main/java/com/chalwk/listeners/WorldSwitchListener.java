// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.listeners;

import com.chalwk.GameModeManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WorldSwitchListener implements Listener {
    private final GameModeManager plugin;

    public WorldSwitchListener(GameModeManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        capturePendingGameMode(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPortal(PlayerPortalEvent event) {
        capturePendingGameMode(event);
    }

    private void capturePendingGameMode(PlayerTeleportEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getWorld() == event.getTo().getWorld()) return;

        Player player = event.getPlayer();
        plugin.setPendingGameMode(player, player.getGameMode());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        GameMode pending = plugin.consumePendingGameMode(player);

        if (pending != null && player.getGameMode() != pending) {
            player.setGameMode(pending);
        }
    }
}
