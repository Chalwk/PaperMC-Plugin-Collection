// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.listener;

import com.chalwk.nexus.NexusPermissions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Keeps permission attachments in sync with the player's presence and
 * current world.
 *
 * <p>Also records the player's UUID on their stored user the first
 * time they join.</p>
 */
public class PlayerListener implements Listener {

    private final NexusPermissions plugin;

    public PlayerListener(NexusPermissions plugin) {
        this.plugin = plugin;
    }

    /** LOWEST so our attachment is in place before other plugins query it. */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        boolean linked = plugin.getPermissionManager().recordUuid(event.getPlayer());
        if (linked) plugin.debug("Recorded UUID for " + event.getPlayer().getName());

        plugin.getPermissionManager().applyToPlayer(event.getPlayer());
    }

    /**
     * A world change can alter which context-specific permissions apply, so
     * the player's attachment has to be rebuilt against the new world.
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        plugin.getPermissionManager().applyToPlayer(event.getPlayer());
    }

    /** MONITOR so cleanup happens after anything else has run on the event. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPermissionManager().removeAttachment(event.getPlayer());
    }
}