// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.AdminChat;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginConfig {

    private String defaultChannel = "admin";
    private int cooldown = 2;

    private final Map<String, Channel> channels = new HashMap<>();

    private boolean joinNotificationEnabled = true;
    private String joinMessage = "&7[&a+&7] &f{player} &7has joined the server";
    private boolean quitNotificationEnabled = true;
    private String quitMessage = "&7[&c-&7] &f{player} &7has left the server";

    private String noPermissionMsg = "&cYou don't have permission to use this channel!";
    private String noChannelMsg = "&cChannel not found!";
    private String toggledOnMsg = "&aAdmin chat visibility enabled!";
    private String toggledOffMsg = "&cAdmin chat visibility disabled!";
    private String toggledForMsg = "&aAdmin chat visibility {state} for {player}!";
    private String cooldownMsg = "&cPlease wait {seconds} seconds before sending another message!";
    private String reloadedMsg = "&aConfiguration reloaded!";
    private String usageMsg = "&eUsage: /achat [channel] [message] or /achat [channel] (to toggle) or /achat toggle";
    private String usageToggledMsg = "&eYou are in {channel} chat mode. Just type your message, or use /achat off to exit.";
    private String channelOnMsg = "&aYou are now in {channel} chat mode. Your messages will be sent to this channel. Type /achat off to exit.";
    private String channelOffMsg = "&cYou have exited {channel} chat mode.";
    private String playerNotFoundMsg = "&cPlayer not found!";
    private String playersOnlyMsg = "&cOnly players can use this command!";
    private String noToggledChannelMsg = "&cYou don't have any channel toggled!";
    private String unknownCommandMsg = "&cUnknown command. Use /achat help.";
    private String visibilityEnabledByMsg = "&eYour admin chat visibility was enabled by {sender}";
    private String visibilityDisabledByMsg = "&eYour admin chat visibility was disabled by {sender}";

    private List<String> helpLines = List.of(
            "&6&lAdminChat Help",
            "&e/achat <message> &7- Send to your default/toggled channel",
            "&e/achat <channel> <message> &7- Send to a specific channel",
            "&e/achat <channel> &7- Toggle a channel on",
            "&e/achat off &7- Exit your toggled channel",
            "&e/achat toggle [player] &7- Toggle visibility",
            "&e/achat reload &7- Reload the configuration",
            "&e/achat help &7- Show this help");

    public PluginConfig(AdminChat plugin) {
    }

    public void loadFromConfig(ConfigurationSection config) {
        defaultChannel = config.getString("default_channel", defaultChannel);
        cooldown = config.getInt("cooldown", cooldown);

        channels.clear();
        ConfigurationSection channelsSection = config.getConfigurationSection("channels");
        if (channelsSection != null) {
            for (String name : channelsSection.getKeys(false)) {
                ConfigurationSection section = channelsSection.getConfigurationSection(name);
                if (section == null)
                    continue;

                String permission = section.getString("permission", "adminchat.channel." + name);
                String format = section.getString("format", "&8[{channel}] &f{sender}&8: &7{message}");
                String prefix = section.getString("prefix", "&8[{channel}]");
                boolean soundEnabled = section.getBoolean("sound.enabled", true);
                String soundType = section.getString("sound.type", "BLOCK_NOTE_BLOCK_PLING");
                double soundVolume = section.getDouble("sound.volume", 0.5);
                double soundPitch = section.getDouble("sound.pitch", 1.5);

                channels.put(name.toLowerCase(), new Channel(
                        permission, format, prefix,
                        soundEnabled, soundType, soundVolume, soundPitch));
            }
        }

        ConfigurationSection notifications = config.getConfigurationSection("notifications");
        if (notifications != null) {
            joinNotificationEnabled = notifications.getBoolean("join_notification", joinNotificationEnabled);
            joinMessage = notifications.getString("join_message", joinMessage);
            quitNotificationEnabled = notifications.getBoolean("quit_notification", quitNotificationEnabled);
            quitMessage = notifications.getString("quit_message", quitMessage);
        }

        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages != null) {

            List<String> help = messages.getStringList("help");
            if (!help.isEmpty()) {
                helpLines = List.copyOf(help);
            }

            noPermissionMsg = messages.getString("no_permission", noPermissionMsg);
            noChannelMsg = messages.getString("no_channel", noChannelMsg);
            toggledOnMsg = messages.getString("toggled_on", toggledOnMsg);
            toggledOffMsg = messages.getString("toggled_off", toggledOffMsg);
            toggledForMsg = messages.getString("toggled_for", toggledForMsg);
            cooldownMsg = messages.getString("cooldown", cooldownMsg);
            reloadedMsg = messages.getString("reloaded", reloadedMsg);
            usageMsg = messages.getString("usage", usageMsg);
            usageToggledMsg = messages.getString("usage_toggled", usageToggledMsg);
            channelOnMsg = messages.getString("channel_on", channelOnMsg);
            channelOffMsg = messages.getString("channel_off", channelOffMsg);
            playerNotFoundMsg = messages.getString("player_not_found", playerNotFoundMsg);
            playersOnlyMsg = messages.getString("players_only", playersOnlyMsg);
            noToggledChannelMsg = messages.getString("no_toggled_channel", noToggledChannelMsg);
            unknownCommandMsg = messages.getString("unknown_command", unknownCommandMsg);
            visibilityEnabledByMsg = messages.getString("visibility_enabled_by", visibilityEnabledByMsg);
            visibilityDisabledByMsg = messages.getString("visibility_disabled_by", visibilityDisabledByMsg);
        }
    }

    public List<String> getHelpLines() {
        return helpLines;
    }

    public String getDefaultChannel() {
        return defaultChannel;
    }

    public int getCooldown() {
        return cooldown;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    public boolean channelExists(String channel) {
        return channels.containsKey(channel.toLowerCase());
    }

    public Channel getChannel(String channel) {
        return channels.get(channel.toLowerCase());
    }

    public String getChannelPermission(String channel) {
        Channel c = channels.get(channel.toLowerCase());
        return c != null ? c.permission() : null;
    }

    public String getChannelFormat(String channel) {
        Channel c = channels.get(channel.toLowerCase());
        return c != null ? c.format() : null;
    }

    public boolean isSoundEnabled(String channel) {
        Channel c = channels.get(channel.toLowerCase());
        return c != null && c.soundEnabled();
    }

    public String getSoundType(String channel) {
        Channel c = channels.get(channel.toLowerCase());
        return c != null ? c.soundType() : null;
    }

    public double getSoundVolume(String channel) {
        Channel c = channels.get(channel.toLowerCase());
        return c != null ? c.soundVolume() : 0.5;
    }

    public double getSoundPitch(String channel) {
        Channel c = channels.get(channel.toLowerCase());
        return c != null ? c.soundPitch() : 1.0;
    }

    public boolean isJoinNotificationEnabled() {
        return joinNotificationEnabled;
    }

    public String getJoinMessage() {
        return joinMessage;
    }

    public boolean isQuitNotificationEnabled() {
        return quitNotificationEnabled;
    }

    public String getQuitMessage() {
        return quitMessage;
    }

    public String getNoPermissionMsg() {
        return noPermissionMsg;
    }

    public String getNoChannelMsg() {
        return noChannelMsg;
    }

    public String getToggledOnMsg() {
        return toggledOnMsg;
    }

    public String getToggledOffMsg() {
        return toggledOffMsg;
    }

    public String getToggledForMsg() {
        return toggledForMsg;
    }

    public String getCooldownMsg() {
        return cooldownMsg;
    }

    public String getReloadedMsg() {
        return reloadedMsg;
    }

    public String getUsageMsg() {
        return usageMsg;
    }

    public String getUsageToggledMsg() {
        return usageToggledMsg;
    }

    public String getChannelOnMsg() {
        return channelOnMsg;
    }

    public String getChannelOffMsg() {
        return channelOffMsg;
    }

    public String getPlayerNotFoundMsg() {
        return playerNotFoundMsg;
    }

    public String getPlayersOnlyMsg() {
        return playersOnlyMsg;
    }

    public String getNoToggledChannelMsg() {
        return noToggledChannelMsg;
    }

    public String getUnknownCommandMsg() {
        return unknownCommandMsg;
    }

    public String getVisibilityEnabledByMsg() {
        return visibilityEnabledByMsg;
    }

    public String getVisibilityDisabledByMsg() {
        return visibilityDisabledByMsg;
    }

    public record Channel(String permission, String format, String prefix,
            boolean soundEnabled, String soundType,
            double soundVolume, double soundPitch) {
    }
}