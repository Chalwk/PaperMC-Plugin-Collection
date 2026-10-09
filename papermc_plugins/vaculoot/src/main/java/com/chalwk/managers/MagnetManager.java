// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.VacuLoot;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.EconomyHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MagnetManager {

    private final VacuLoot plugin;
    private final Map<UUID, Boolean> magnetStates = new HashMap<>();
    private final Map<UUID, String> magnetTiers = new HashMap<>();
    private final Map<UUID, Long> lastToggleTime = new HashMap<>();
    private BukkitTask magnetTask;

    public enum ToggleResult {
        SUCCESS,
        COOLDOWN,
        INSUFFICIENT_FUNDS
    }

    public MagnetManager(VacuLoot plugin) {
        this.plugin = plugin;
    }

    public void startMagnetTask() {
        PluginConfig config = plugin.getConfigManager().getConfig();
        int interval = config.getMagnetInterval();

        magnetTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isMagnetActive(player)) {
                        attractItems(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    public void stopMagnetTask() {
        if (magnetTask != null)
            magnetTask.cancel();
    }

    private void attractItems(Player player) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        String tier = getPlayerTier(player);
        double range = config.getMagnetRange(tier);

        if (!isWorldAllowed(player.getWorld()))
            return;

        Location playerLoc = player.getLocation();
        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(playerLoc, range, range, range);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Item item) {
                if (!config.isAttractItems())
                    continue;
                if (!canPickupItem(player))
                    continue;
                if (!canAttractItem(item))
                    continue;

                moveItemTowardsPlayer(item, playerLoc, config.getAttractionSpeed(tier));
            } else if (config.isAttractExperience() && entity.getType().toString().contains("EXPERIENCE")) {
                moveEntityTowardsPlayer(entity, playerLoc, config.getAttractionSpeed(tier));
            }
        }
    }

    private void moveItemTowardsPlayer(Item item, Location playerLoc, double speed) {
        Location itemLoc = item.getLocation();
        Vector direction = playerLoc.toVector().subtract(itemLoc.toVector());
        if (direction.lengthSquared() > 0) {
            direction.normalize().multiply(speed);

            if (!hasClearPath(itemLoc, playerLoc))
                direction = findAlternativePath(itemLoc, playerLoc, speed);

            Vector currentVelocity = item.getVelocity();
            Vector newVelocity = currentVelocity.multiply(0.3).add(direction.multiply(0.7));
            item.setVelocity(newVelocity);
        }
    }

    private void moveEntityTowardsPlayer(Entity entity, Location playerLoc, double speed) {
        Location entityLoc = entity.getLocation();
        Vector direction = playerLoc.toVector().subtract(entityLoc.toVector());

        if (direction.lengthSquared() > 0) {
            direction.normalize().multiply(speed);
            entity.setVelocity(direction);
        }
    }

    private boolean hasClearPath(Location from, Location to) {
        World world = from.getWorld();
        double distance = from.distance(to);
        Vector direction = to.toVector().subtract(from.toVector()).normalize();

        for (double i = 0.5; i < distance; i += 0.5) {
            Location checkLoc = from.clone().add(direction.clone().multiply(i));
            if (world.getBlockAt(checkLoc).getType().isSolid())
                return false;
        }
        return true;
    }

    private Vector findAlternativePath(Location from, Location to, double speed) {
        Vector direction = to.toVector().subtract(from.toVector());
        direction.setY(direction.getY() + 0.5);
        return direction.normalize().multiply(speed);
    }

    private boolean canPickupItem(Player player) {
        return player.getInventory().firstEmpty() != -1;
    }

    private boolean canAttractItem(Item item) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        String materialName = item.getItemStack().getType().name();
        if (config.getBlacklistedMaterials().contains(materialName))
            return false;

        return !item.hasMetadata("no-magnet");
    }

    private boolean isWorldAllowed(World world) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        List<String> allowedWorlds = config.getAllowedWorlds();

        if (allowedWorlds.isEmpty())
            return true;

        return allowedWorlds.contains(world.getName());
    }

    public ToggleResult toggleMagnet(Player player) {
        UUID playerId = player.getUniqueId();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (hasCooldown(player))
            return ToggleResult.COOLDOWN;

        if (config.isEconomyEnabled() && config.getToggleCost() > 0) {
            EconomyHelper economy = plugin.getEconomyHelper();
            if (economy != null) {
                if (!economy.hasEnough(player, config.getToggleCost())) {
                    return ToggleResult.INSUFFICIENT_FUNDS;
                }
            }
        }

        boolean currentState = magnetStates.getOrDefault(playerId, false);
        boolean newState = !currentState;

        if (newState && config.isEconomyEnabled() && config.getToggleCost() > 0) {
            EconomyHelper economy = plugin.getEconomyHelper();
            if (economy != null)
                economy.withdraw(player, config.getToggleCost());
        }

        magnetStates.put(playerId, newState);
        lastToggleTime.put(playerId, System.currentTimeMillis());

        return ToggleResult.SUCCESS;
    }

    public void setMagnetState(Player player, boolean state) {
        magnetStates.put(player.getUniqueId(), state);
    }

    public boolean isMagnetActive(Player player) {
        return magnetStates.getOrDefault(player.getUniqueId(), false);
    }

    public String getPlayerTier(Player player) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (magnetTiers.containsKey(player.getUniqueId())) {
            return magnetTiers.get(player.getUniqueId());
        }

        for (String tier : config.getMagnetTiers().keySet()) {
            if (player.hasPermission("magnet.tier." + tier)) {
                return tier;
            }
        }

        return config.getDefaultTier();
    }

    public void setPlayerTier(Player player, String tier) {
        magnetTiers.put(player.getUniqueId(), tier);
    }

    public boolean hasCooldown(Player player) {
        if (!lastToggleTime.containsKey(player.getUniqueId()))
            return false;

        long lastToggle = lastToggleTime.get(player.getUniqueId());
        int cooldown = plugin.getConfigManager().getConfig().getToggleCooldown();

        return (System.currentTimeMillis() - lastToggle) < (cooldown * 1000L);
    }

    public int getRemainingCooldown(Player player) {
        long lastToggle = lastToggleTime.get(player.getUniqueId());
        int cooldown = plugin.getConfigManager().getConfig().getToggleCooldown();
        long remaining = (cooldown * 1000L) - (System.currentTimeMillis() - lastToggle);

        return (int) Math.ceil(remaining / 1000.0);
    }
}