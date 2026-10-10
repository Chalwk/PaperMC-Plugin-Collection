// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.PerkMenu;
import com.chalwk.util.Perk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads perk definitions from config and answers questions about them.
 *
 * <p>Ownership is purely permission-based: a player owns a perk when they
 * hold every permission node listed on it. No economy or store API is
 * consulted.</p>
 */
public class PerkManager {

    private final PerkMenu plugin;
    private final Map<String, Perk> perks = new LinkedHashMap<>();

    public PerkManager(PerkMenu plugin) {
        this.plugin = plugin;
    }

    public void loadFromConfig(ConfigurationSection config) {
        perks.clear();

        ConfigurationSection section = config.getConfigurationSection("perks");
        if (section == null) {
            plugin.getLogger().warning("No perks configured under 'perks:' in config.yml.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection perkSection = section.getConfigurationSection(id);
            if (perkSection == null) continue;

            String name = perkSection.getString("name", id);
            String category = perkSection.getString("category", "Miscellaneous");
            double price = perkSection.getDouble("price", 0.0);
            String packageId = perkSection.getString("package_id", "");
            List<String> description = List.copyOf(perkSection.getStringList("description"));
            List<String> permissions = List.copyOf(perkSection.getStringList("permissions"));

            if (permissions.isEmpty()) {
                plugin.getLogger().warning("Perk '" + id + "' has no permissions and can never be owned. Skipping.");
                continue;
            }

            perks.put(id.toLowerCase(), new Perk(id, name, category, price, packageId, description, permissions));
        }

        plugin.getLogger().info("Loaded " + perks.size() + " perks.");
    }

    public List<Perk> getAll() {
        return new ArrayList<>(perks.values());
    }

    /**
     * Finds a perk by its config key (case-insensitive) or by its display
     * name (also case-insensitive). Returns null if nothing matches.
     */
    public Perk find(String query) {
        if (query == null) return null;
        Perk byId = perks.get(query.toLowerCase());
        if (byId != null) return byId;
        for (Perk perk : perks.values()) {
            if (perk.name().equalsIgnoreCase(query)) return perk;
        }
        return null;
    }

    /**
     * A player owns a perk when they hold every permission node the perk
     * declares. Wildcards like {@code cmi.colors.*} are resolved by the
     * server's permission plugin, not by us.
     */
    public boolean isOwnedBy(Perk perk, Player player) {
        for (String permission : perk.permissions()) {
            if (!player.hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    public List<Perk> getOwnedBy(Player player) {
        List<Perk> owned = new ArrayList<>();
        for (Perk perk : perks.values()) {
            if (isOwnedBy(perk, player)) owned.add(perk);
        }
        return owned;
    }

    public List<Perk> getUnownedBy(Player player) {
        List<Perk> unowned = new ArrayList<>();
        for (Perk perk : perks.values()) {
            if (!isOwnedBy(perk, player)) unowned.add(perk);
        }
        return unowned;
    }

    /**
     * The sum of listed prices for every perk the player owns. This is not
     * the amount they actually spent if prices changed over time or they
     * received perks outside the store.
     */
    public double getTotalListedValue(Player player) {
        double total = 0.0;
        for (Perk perk : perks.values()) {
            if (isOwnedBy(perk, player)) total += perk.price();
        }
        return total;
    }

    public String buildBuyUrl(Perk perk) {
        String template = plugin.getConfigManager().getConfig().getBuyUrlTemplate();
        return template.replace("{package_id}", perk.packageId());
    }
}