// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.VacuLoot;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginConfig {

    private String defaultTier = "basic";
    private int magnetInterval = 5;
    private int toggleCooldown = 5;

    private boolean attractItems = true;
    private boolean attractExperience = true;
    private double attractionSpeed = 0.3;

    private boolean economyEnabled = false;
    private double toggleCost = 10.0;

    private List<String> allowedWorlds = new ArrayList<>();
    private List<String> blacklistedMaterials = new ArrayList<>();

    private final Map<String, MagnetTier> magnetTiers = new HashMap<>();

    private String enabledMsg = "&aMagnet &e{tier} &aenabled! &7(Range: &e{range}&7 blocks)";
    private String disabledMsg = "&cMagnet disabled!";
    private String cooldownMsg = "&cPlease wait {seconds} seconds before toggling magnet again!";
    private String insufficientFundsMsg = "&cYou need ${amount} to toggle magnet!";
    private String toggledForMsg = "&aMagnet {state} for {player}!";
    private String toggledByMsg = "&aYour magnet was {state} by {sender}!";
    private String statusMsg = "&7[&bVacuLoot&7] {player}'s magnet: {status} &7(Tier: {tier}, Range: {range} blocks)";
    private String reloadedMsg = "&aConfiguration reloaded!";
    private String playerNotFoundMsg = "&cPlayer not found!";
    private String invalidTierMsg = "&cInvalid magnet tier!";
    private String tierSetMsg = "&aSet {player}'s magnet tier to {tier}";
    private String tierChangedMsg = "&aYour magnet tier has been changed to {tier}";
    private String noPermissionMsg = "&cYou don't have permission to use this command!";
    private String playersOnlyMsg = "&cOnly players can use this command!";
    private String unknownCommandMsg = "&cUnknown command. Use /magnet help";
    private String adminNoPermissionMsg = "&cYou don't have permission to use this command!";
    private String othersNoPermissionMsg = "&cYou don't have permission to toggle magnet for others!";
    private String specifyPlayerMsg = "&cYou must specify a player!";
    private String tierUsageMsg = "&cUsage: /magnet tier <player> <tier>";
    private String costLineMsg = "&7Cost: &e${amount}";
    private String helpCostMsg = "&7Cost per toggle: &e${amount}";
    private List<String> helpLines = List.of(
            "&6&lVacuLoot Help",
            "&e/magnet &7- Toggle your magnet",
            "&e/magnet toggle [player] &7- Toggle magnet for yourself or another player",
            "&e/magnet check [player] &7- Check magnet status",
            "&e/magnet tier <player> <tier> &7- Set player's magnet tier",
            "&e/magnet reload &7- Reload configuration",
            "&e/magnet help &7- Show this help");

    public PluginConfig(VacuLoot plugin) {
    }

    public void loadFromConfig(ConfigurationSection config) {
        defaultTier = config.getString("default_tier", defaultTier);
        magnetInterval = config.getInt("magnet.interval", magnetInterval);
        toggleCooldown = config.getInt("toggle.cooldown", toggleCooldown);

        attractItems = config.getBoolean("attraction.items", attractItems);
        attractExperience = config.getBoolean("attraction.experience", attractExperience);
        attractionSpeed = config.getDouble("attraction.speed", attractionSpeed);

        economyEnabled = config.getBoolean("economy.enabled", economyEnabled);
        toggleCost = config.getDouble("economy.toggle_cost", toggleCost);

        allowedWorlds = config.getStringList("worlds.allowed");
        blacklistedMaterials = config.getStringList("blacklist.materials");

        magnetTiers.clear();
        ConfigurationSection tiers = config.getConfigurationSection("tiers");
        if (tiers != null) {
            for (String tier : tiers.getKeys(false)) {
                double range = tiers.getDouble(tier + ".range", 5.0);
                double speedMultiplier = tiers.getDouble(tier + ".speed_multiplier", 1.0);
                String permission = tiers.getString(tier + ".permission", "magnet.tier." + tier);
                magnetTiers.put(tier.toLowerCase(), new MagnetTier(range, speedMultiplier, permission));
            }
        }

        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages != null) {
            List<String> help = messages.getStringList("help");
            if (!help.isEmpty()) {
                helpLines = List.copyOf(help);
            }
            enabledMsg = messages.getString("enabled", enabledMsg);
            playersOnlyMsg = messages.getString("players_only", playersOnlyMsg);
            unknownCommandMsg = messages.getString("unknown_command", unknownCommandMsg);
            adminNoPermissionMsg = messages.getString("admin_no_permission", adminNoPermissionMsg);
            othersNoPermissionMsg = messages.getString("others_no_permission", othersNoPermissionMsg);
            specifyPlayerMsg = messages.getString("specify_player", specifyPlayerMsg);
            tierUsageMsg = messages.getString("tier_usage", tierUsageMsg);
            costLineMsg = messages.getString("cost_line", costLineMsg);
            helpCostMsg = messages.getString("help_cost", helpCostMsg);
            disabledMsg = messages.getString("disabled", disabledMsg);
            cooldownMsg = messages.getString("cooldown", cooldownMsg);
            insufficientFundsMsg = messages.getString("insufficient_funds", insufficientFundsMsg);
            toggledForMsg = messages.getString("toggled_for", toggledForMsg);
            toggledByMsg = messages.getString("toggled_by", toggledByMsg);
            statusMsg = messages.getString("status", statusMsg);
            reloadedMsg = messages.getString("reloaded", reloadedMsg);
            playerNotFoundMsg = messages.getString("player_not_found", playerNotFoundMsg);
            invalidTierMsg = messages.getString("invalid_tier", invalidTierMsg);
            tierSetMsg = messages.getString("tier_set", tierSetMsg);
            tierChangedMsg = messages.getString("tier_changed", tierChangedMsg);
            noPermissionMsg = messages.getString("no_permission", noPermissionMsg);
        }
    }

    public String getPlayersOnlyMsg() {
        return playersOnlyMsg;
    }

    public String getUnknownCommandMsg() {
        return unknownCommandMsg;
    }

    public String getAdminNoPermissionMsg() {
        return adminNoPermissionMsg;
    }

    public String getOthersNoPermissionMsg() {
        return othersNoPermissionMsg;
    }

    public String getSpecifyPlayerMsg() {
        return specifyPlayerMsg;
    }

    public String getTierUsageMsg() {
        return tierUsageMsg;
    }

    public String getCostLineMsg() {
        return costLineMsg;
    }

    public String getHelpCostMsg() {
        return helpCostMsg;
    }

    public List<String> getHelpLines() {
        return helpLines;
    }

    public String getDefaultTier() {
        return defaultTier;
    }

    public int getMagnetInterval() {
        return magnetInterval;
    }

    public int getToggleCooldown() {
        return toggleCooldown;
    }

    public boolean isAttractItems() {
        return attractItems;
    }

    public boolean isAttractExperience() {
        return attractExperience;
    }

    public double getBaseAttractionSpeed() {
        return attractionSpeed;
    }

    public double getAttractionSpeed(String tier) {
        MagnetTier data = magnetTiers.get(tier);
        if (data != null) {
            return attractionSpeed * data.speedMultiplier();
        }
        return attractionSpeed;
    }

    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    public double getToggleCost() {
        return toggleCost;
    }

    public List<String> getAllowedWorlds() {
        return allowedWorlds;
    }

    public List<String> getBlacklistedMaterials() {
        return blacklistedMaterials;
    }

    public Map<String, MagnetTier> getMagnetTiers() {
        return magnetTiers;
    }

    public double getMagnetRange(String tier) {
        MagnetTier data = magnetTiers.get(tier);
        return data != null ? data.range() : 5.0;
    }

    public String getEnabledMsg() {
        return enabledMsg;
    }

    public String getDisabledMsg() {
        return disabledMsg;
    }

    public String getCooldownMsg() {
        return cooldownMsg;
    }

    public String getInsufficientFundsMsg() {
        return insufficientFundsMsg;
    }

    public String getToggledForMsg() {
        return toggledForMsg;
    }

    public String getToggledByMsg() {
        return toggledByMsg;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public String getReloadedMsg() {
        return reloadedMsg;
    }

    public String getPlayerNotFoundMsg() {
        return playerNotFoundMsg;
    }

    public String getInvalidTierMsg() {
        return invalidTierMsg;
    }

    public String getTierSetMsg() {
        return tierSetMsg;
    }

    public String getTierChangedMsg() {
        return tierChangedMsg;
    }

    public String getNoPermissionMsg() {
        return noPermissionMsg;
    }

    public record MagnetTier(double range, double speedMultiplier, String permission) {
    }
}