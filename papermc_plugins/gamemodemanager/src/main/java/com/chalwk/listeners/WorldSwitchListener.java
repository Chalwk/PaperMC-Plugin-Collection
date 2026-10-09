// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.listeners;

import com.chalwk.GameModeManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Bukkit resets the player's gamemode on world change (notably for creative
 * players entering a survival world). We record the gamemode before the
 * change and restore it once the new world is loaded.
 */
public class WorldSwitchListener implements Listener {
    private final GameModeManager plugin;

    public WorldSwitchListener(GameModeManager plugin) {
        this.plugin = plugin;
    }

    // MONITOR: we don't influence the outcome, just record what we saw.
    // PlayerPortalEvent extends PlayerTeleportEvent, so portals hit this too.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getWorld() == event.getTo().getWorld()) return;

        Player player = event.getPlayer();
        plugin.setPendingGameMode(player, player.getGameMode());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        GameMode pending = plugin.consumePendingGameMode(player);

        // Only touch it if Bukkit actually changed it on us.
        if (pending != null && player.getGameMode() != pending) {
            player.setGameMode(pending);
        }
    }
}