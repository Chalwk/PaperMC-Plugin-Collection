// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.listeners;

import com.chalwk.AdminChat;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.MessageHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AdminChatListener implements Listener {

    private final AdminChat plugin;
    private final MessageHelper messageHelper;

    public AdminChatListener(AdminChat plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (config.isJoinNotificationEnabled() && player.hasPermission("adminchat.use")) {
            String message = config.getJoinMessage().replace("{player}", player.getName());
            for (Player staff : plugin.getServer().getOnlinePlayers()) {
                if (staff.hasPermission("adminchat.use") && !plugin.getChatManager().isHidden(staff)) {
                    messageHelper.sendMessage(staff, message);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (config.isQuitNotificationEnabled() && player.hasPermission("adminchat.use")) {
            String message = config.getQuitMessage().replace("{player}", player.getName());
            for (Player staff : plugin.getServer().getOnlinePlayers()) {
                if (staff.hasPermission("adminchat.use") && !plugin.getChatManager().isHidden(staff)) {
                    messageHelper.sendMessage(staff, message);
                }
            }
        }
    }
}