// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.NoctiView;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

public class PluginConfig {

    private boolean particleEnabled = true;
    private String particleType = "GLOW";
    private int particleCount = 10;
    private double particleOffsetX = 0.5;
    private double particleOffsetY = 1.0;
    private double particleOffsetZ = 0.5;
    private double particleSpeed = 0.1;

    private boolean soundEnabled = true;
    private String soundType = "ENTITY_EXPERIENCE_ORB_PICKUP";
    private double soundVolume = 1.0;
    private double soundPitch = 1.0;

    private int effectDuration = 999999;
    private int effectAmplifier = 0;

    private final Set<String> enabledWorlds = new HashSet<>();

    private String noPermissionMsg = "&cYou don't have permission to use this feature!";
    private String playersOnlyMsg = "&cOnly players can use this command!";
    private String reloadedMsg = "&aConfiguration reloaded!";
    private String playerNotFoundMsg = "&cPlayer not found!";
    private String worldNotFoundMsg = "&cWorld not found!";
    private String worldEnabledMsg = "&aEnabled night vision in {world}!";
    private String worldDisabledMsg = "&cDisabled night vision in {world}!";
    private String worldHeaderMsg = "&6=== Night Vision World Settings ===";
    private String worldNotEnabledSelfMsg = "&cNight vision is not enabled in this world!";
    private String worldNotEnabledOtherMsg = "&cNight vision is not enabled in {world}!";
    private String nightVisionEnabledMsg = "&aNight vision enabled!";
    private String nightVisionDisabledMsg = "&cNight vision disabled!";
    private String nightVisionEnabledForMsg = "&aEnabled night vision for {player}!";
    private String nightVisionDisabledForMsg = "&cDisabled night vision for {player}!";
    private String nightVisionEnabledByMsg = "&eNight vision enabled by {sender}!";
    private String nightVisionDisabledByMsg = "&eNight vision disabled by {sender}!";
    private String usageEnableWorldMsg = "&cUsage: /nightvision enableworld <world>";
    private String usageDisableWorldMsg = "&cUsage: /nightvision disableworld <world>";

    public PluginConfig(NoctiView plugin) {
    }

    public void loadFromConfig(ConfigurationSection config) {
        ConfigurationSection particle = config.getConfigurationSection("particle");
        if (particle != null) {
            particleEnabled = particle.getBoolean("enabled", particleEnabled);
            particleType = particle.getString("type", particleType);
            particleCount = particle.getInt("count", particleCount);
            particleOffsetX = particle.getDouble("offset_x", particleOffsetX);
            particleOffsetY = particle.getDouble("offset_y", particleOffsetY);
            particleOffsetZ = particle.getDouble("offset_z", particleOffsetZ);
            particleSpeed = particle.getDouble("speed", particleSpeed);
        }

        ConfigurationSection sound = config.getConfigurationSection("sound");
        if (sound != null) {
            soundEnabled = sound.getBoolean("enabled", soundEnabled);
            soundType = sound.getString("type", soundType);
            soundVolume = sound.getDouble("volume", soundVolume);
            soundPitch = sound.getDouble("pitch", soundPitch);
        }

        ConfigurationSection effect = config.getConfigurationSection("effect");
        if (effect != null) {
            effectDuration = effect.getInt("duration", effectDuration);
            effectAmplifier = effect.getInt("amplifier", effectAmplifier);
        }

        enabledWorlds.clear();
        ConfigurationSection worlds = config.getConfigurationSection("worlds");
        if (worlds != null) {
            for (String worldName : worlds.getKeys(false)) {
                if (worlds.getBoolean(worldName, false)) {
                    enabledWorlds.add(worldName);
                }
            }
        }

        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages != null) {
            noPermissionMsg = messages.getString("no_permission", noPermissionMsg);
            playersOnlyMsg = messages.getString("players_only", playersOnlyMsg);
            reloadedMsg = messages.getString("reloaded", reloadedMsg);
            playerNotFoundMsg = messages.getString("player_not_found", playerNotFoundMsg);
            worldNotFoundMsg = messages.getString("world_not_found", worldNotFoundMsg);
            worldEnabledMsg = messages.getString("world_enabled", worldEnabledMsg);
            worldDisabledMsg = messages.getString("world_disabled", worldDisabledMsg);
            worldHeaderMsg = messages.getString("world_header", worldHeaderMsg);
            worldNotEnabledSelfMsg = messages.getString("world_not_enabled_self", worldNotEnabledSelfMsg);
            worldNotEnabledOtherMsg = messages.getString("world_not_enabled_other", worldNotEnabledOtherMsg);
            nightVisionEnabledMsg = messages.getString("night_vision_enabled", nightVisionEnabledMsg);
            nightVisionDisabledMsg = messages.getString("night_vision_disabled", nightVisionDisabledMsg);
            nightVisionEnabledForMsg = messages.getString("night_vision_enabled_for", nightVisionEnabledForMsg);
            nightVisionDisabledForMsg = messages.getString("night_vision_disabled_for", nightVisionDisabledForMsg);
            nightVisionEnabledByMsg = messages.getString("night_vision_enabled_by", nightVisionEnabledByMsg);
            nightVisionDisabledByMsg = messages.getString("night_vision_disabled_by", nightVisionDisabledByMsg);
            usageEnableWorldMsg = messages.getString("usage_enableworld", usageEnableWorldMsg);
            usageDisableWorldMsg = messages.getString("usage_disableworld", usageDisableWorldMsg);
        }
    }

    public boolean isParticleEnabled() {
        return particleEnabled;
    }

    public String getParticleType() {
        return particleType;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public double getParticleOffsetX() {
        return particleOffsetX;
    }

    public double getParticleOffsetY() {
        return particleOffsetY;
    }

    public double getParticleOffsetZ() {
        return particleOffsetZ;
    }

    public double getParticleSpeed() {
        return particleSpeed;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public String getSoundType() {
        return soundType;
    }

    public double getSoundVolume() {
        return soundVolume;
    }

    public double getSoundPitch() {
        return soundPitch;
    }

    public int getEffectDuration() {
        return effectDuration;
    }

    public int getEffectAmplifier() {
        return effectAmplifier;
    }

    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.contains(worldName);
    }

    public String getNoPermissionMsg() {
        return noPermissionMsg;
    }

    public String getPlayersOnlyMsg() {
        return playersOnlyMsg;
    }

    public String getReloadedMsg() {
        return reloadedMsg;
    }

    public String getPlayerNotFoundMsg() {
        return playerNotFoundMsg;
    }

    public String getWorldNotFoundMsg() {
        return worldNotFoundMsg;
    }

    public String getWorldEnabledMsg() {
        return worldEnabledMsg;
    }

    public String getWorldDisabledMsg() {
        return worldDisabledMsg;
    }

    public String getWorldHeaderMsg() {
        return worldHeaderMsg;
    }

    public String getWorldNotEnabledSelfMsg() {
        return worldNotEnabledSelfMsg;
    }

    public String getWorldNotEnabledOtherMsg() {
        return worldNotEnabledOtherMsg;
    }

    public String getNightVisionEnabledMsg() {
        return nightVisionEnabledMsg;
    }

    public String getNightVisionDisabledMsg() {
        return nightVisionDisabledMsg;
    }

    public String getNightVisionEnabledForMsg() {
        return nightVisionEnabledForMsg;
    }

    public String getNightVisionDisabledForMsg() {
        return nightVisionDisabledForMsg;
    }

    public String getNightVisionEnabledByMsg() {
        return nightVisionEnabledByMsg;
    }

    public String getNightVisionDisabledByMsg() {
        return nightVisionDisabledByMsg;
    }

    public String getUsageEnableWorldMsg() {
        return usageEnableWorldMsg;
    }

    public String getUsageDisableWorldMsg() {
        return usageDisableWorldMsg;
    }
}