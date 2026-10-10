// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.AdminChat;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AdminChatManager {

    private final AdminChat plugin;
    private final MessageHelper messageHelper;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Map<UUID, String> toggledChannels = new HashMap<>();

    public AdminChatManager(AdminChat plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    public void toggleChannel(Player player, String channel) {
        UUID playerId = player.getUniqueId();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!config.channelExists(channel)) {
            messageHelper.sendMessage(player, config.getNoChannelMsg());
            return;
        }

        String permission = config.getChannelPermission(channel);
        if (!player.hasPermission(permission)) {
            messageHelper.sendMessage(player, config.getNoPermissionMsg());
            return;
        }

        if (toggledChannels.containsKey(playerId) && toggledChannels.get(playerId).equals(channel)) {
            toggledChannels.remove(playerId);
            messageHelper.sendMessage(player, config.getChannelOffMsg()
                    .replace("{channel}", channel));
        } else {
            toggledChannels.put(playerId, channel);
            messageHelper.sendMessage(player, config.getChannelOnMsg()
                    .replace("{channel}", channel));
        }
    }

    public String getToggledChannel(Player player) {
        return toggledChannels.get(player.getUniqueId());
    }

    public boolean hasToggledChannel(Player player) {
        return toggledChannels.containsKey(player.getUniqueId());
    }

    public void clearToggledChannel(Player player) {
        toggledChannels.remove(player.getUniqueId());
    }

    public void sendMessage(Player sender, String channel, String message) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (hasCooldown(sender)) {
            int remaining = getRemainingCooldown(sender);
            messageHelper.sendMessage(sender,
                    config.getCooldownMsg().replace("{seconds}", String.valueOf(remaining)));
            return;
        }

        if (!config.channelExists(channel)) {
            messageHelper.sendMessage(sender, config.getNoChannelMsg());
            return;
        }

        String permission = config.getChannelPermission(channel);
        if (!sender.hasPermission(permission)) {
            messageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return;
        }

        String format = config.getChannelFormat(channel)
                .replace("{sender}", sender.getName())
                .replace("{channel}", channel)
                .replace("{message}", message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("adminchat.use") && !isHidden(player)) {
                messageHelper.sendMessage(player, format);

                if (config.isSoundEnabled(channel)) {
                    try {
                        Sound sound = Sound.valueOf(config.getSoundType(channel));
                        player.playSound(
                                player.getLocation(),
                                sound,
                                (float) config.getSoundVolume(channel),
                                (float) config.getSoundPitch(channel));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid sound type for channel " + channel);
                    }
                }
            }
        }

        messageHelper.sendMessage(Bukkit.getConsoleSender(), format);

        setCooldown(sender);
    }

    public void toggleVisibility(Player player) {
        UUID playerId = player.getUniqueId();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (hiddenPlayers.contains(playerId)) {
            hiddenPlayers.remove(playerId);
            messageHelper.sendMessage(player, config.getToggledOnMsg());
        } else {
            hiddenPlayers.add(playerId);
            messageHelper.sendMessage(player, config.getToggledOffMsg());
        }
    }

    public void toggleVisibilityForPlayer(Player target, Player executor) {
        UUID targetId = target.getUniqueId();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (hiddenPlayers.contains(targetId)) {
            hiddenPlayers.remove(targetId);
            String message = config.getToggledForMsg()
                    .replace("{state}", "enabled")
                    .replace("{player}", target.getName());
            messageHelper.sendMessage(executor, message);
            messageHelper.sendMessage(target, config.getVisibilityEnabledByMsg()
                    .replace("{sender}", executor.getName()));
        } else {
            hiddenPlayers.add(targetId);
            String message = config.getToggledForMsg()
                    .replace("{state}", "disabled")
                    .replace("{player}", target.getName());
            messageHelper.sendMessage(executor, message);
            messageHelper.sendMessage(target, config.getVisibilityDisabledByMsg()
                    .replace("{sender}", executor.getName()));
        }
    }

    public boolean isHidden(Player player) {
        return hiddenPlayers.contains(player.getUniqueId());
    }

    private boolean hasCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId()))
            return false;

        long lastMessage = cooldowns.get(player.getUniqueId());
        int cooldown = plugin.getConfigManager().getConfig().getCooldown();

        return (System.currentTimeMillis() - lastMessage) < (cooldown * 1000L);
    }

    private int getRemainingCooldown(Player player) {
        long lastMessage = cooldowns.get(player.getUniqueId());
        int cooldown = plugin.getConfigManager().getConfig().getCooldown();
        long remaining = (cooldown * 1000L) - (System.currentTimeMillis() - lastMessage);

        return (int) Math.ceil(remaining / 1000.0);
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        new BukkitRunnable() {
            @Override
            public void run() {
                cooldowns.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, plugin.getConfigManager().getConfig().getCooldown() * 20L);
    }
}