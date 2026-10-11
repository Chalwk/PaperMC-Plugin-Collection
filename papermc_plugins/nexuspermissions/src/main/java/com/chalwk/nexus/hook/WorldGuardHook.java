// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Looks up the WorldGuard regions a player is currently standing in.
 *
 * <p>Every region becomes a {@code region:<name>} context during permission
 * resolution. That lets you grant a permission to everyone inside a region
 * without touching individual players or groups.</p>
 *
 * <p>Availability is cached after the first check. Any throwable during a
 * lookup disables the hook for the rest of the session, so a broken WorldGuard
 * install can't crash permission resolution on every move.</p>
 */
public final class WorldGuardHook {

    private static Boolean available;

    private WorldGuardHook() {
    }

    public static boolean isAvailable() {
        if (available == null) {
            available = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
        }
        return available;
    }

    /**
     * Returns the lowercase IDs of every region the player is standing in,
     * each prefixed with {@code region:}. Empty list if WorldGuard is absent
     * or the lookup fails.
     */
    public static List<String> regionsAt(Player player) {
        if (!isAvailable()) return List.of();
        try {
            RegionManager mgr = WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer()
                    .get(BukkitAdapter.adapt(player.getWorld()));
            if (mgr == null) return List.of();

            // getApplicableRegions takes a BlockVector3, so convert the
            // player's Location rather than the raw WorldEdit Location.
            ApplicableRegionSet set = mgr.getApplicableRegions(
                    BukkitAdapter.asBlockVector(player.getLocation()));
            List<String> out = new ArrayList<>();
            for (ProtectedRegion region : set) {
                out.add("region:" + region.getId().toLowerCase(Locale.ROOT));
            }
            return out;
        } catch (Throwable t) {
            available = false;
            return List.of();
        }
    }
}