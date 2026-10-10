// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.listeners;

import com.chalwk.Handbook;
import com.chalwk.config.PluginConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Gives the handbook to qualifying players on join.
 *
 * <p>
 * Two independent gates decide whether a player receives the book:
 * </p>
 * <ul>
 * <li>{@code only_first_join} - if true, skips anyone who's joined before</li>
 * <li>{@code hasReceived} - skips anyone who already has one from us</li>
 * </ul>
 */
public class PlayerJoinListener implements Listener {

    private final Handbook plugin;

    public PlayerJoinListener(Handbook plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!config.isGiveOnFirstJoin())
            return;

        Player player = event.getPlayer();

        if (config.isOnlyFirstJoin() && player.hasPlayedBefore())
            return;
        if (plugin.getHandbookManager().hasReceived(player))
            return;

        plugin.getHandbookManager().giveToPlayer(player);
        plugin.getHandbookManager().markReceived(player);
    }
}