// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.AdminChat;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.MessageHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AdminChatManager {

    private final AdminChat plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Map<UUID, String> toggledChannels = new HashMap<>();

    public AdminChatManager(AdminChat plugin) {
        this.plugin = plugin;
    }

    public void toggleChannel(Player player, String channel) {
        UUID playerId = player.getUniqueId();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!config.channelExists(channel)) {
            MessageHelper.sendMessage(player, config.getNoChannelMsg());
            return;
        }

        String permission = config.getChannelPermission(channel);
        if (!player.hasPermission(permission)) {
            MessageHelper.sendMessage(player, config.getNoPermissionMsg());
            return;
        }

        if (toggledChannels.containsKey(playerId) && toggledChannels.get(playerId).equals(channel)) {
            toggledChannels.remove(playerId);
            MessageHelper.sendMessage(player, config.getChannelOffMsg()
                    .replace("{channel}", channel));
        } else {
            toggledChannels.put(playerId, channel);
            MessageHelper.sendMessage(player, config.getChannelOnMsg()
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
            MessageHelper.sendMessage(sender,
                    config.getCooldownMsg().replace("{seconds}", String.valueOf(remaining)));
            return;
        }

        if (!config.channelExists(channel)) {
            MessageHelper.sendMessage(sender, config.getNoChannelMsg());
            return;
        }

        String permission = config.getChannelPermission(channel);
        if (!sender.hasPermission(permission)) {
            MessageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return;
        }

        String format = config.getChannelFormat(channel)
                .replace("{sender}", sender.getName())
                .replace("{channel}", channel)
                .replace("{message}", message);

        Component formattedMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(format);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("adminchat.use") && !isHidden(player)) {
                player.sendMessage(formattedMessage);

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

        Bukkit.getConsoleSender().sendMessage(formattedMessage);

        setCooldown(sender);
    }

    public void sendToggledMessage(Player sender, String message) {
        String channel = getToggledChannel(sender);
        if (channel == null) {
            channel = plugin.getConfigManager().getConfig().getDefaultChannel();
        }
        sendMessage(sender, channel, message);
    }

    public void toggleVisibility(Player player) {
        UUID playerId = player.getUniqueId();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (hiddenPlayers.contains(playerId)) {
            hiddenPlayers.remove(playerId);
            MessageHelper.sendMessage(player, config.getToggledOnMsg());
        } else {
            hiddenPlayers.add(playerId);
            MessageHelper.sendMessage(player, config.getToggledOffMsg());
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
            MessageHelper.sendMessage(executor, message);
            MessageHelper.sendMessage(target, config.getVisibilityEnabledByMsg()
                    .replace("{sender}", executor.getName()));
        } else {
            hiddenPlayers.add(targetId);
            String message = config.getToggledForMsg()
                    .replace("{state}", "disabled")
                    .replace("{player}", target.getName());
            MessageHelper.sendMessage(executor, message);
            MessageHelper.sendMessage(target, config.getVisibilityDisabledByMsg()
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