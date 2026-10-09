// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.GameModeManager;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Holds per-player, per-gamemode state and persists it under playerdata/.
 * Only CREATIVE and SURVIVAL are tracked - anything else falls through
 * untouched.
 */
public class InventoryManager {
    private final GameModeManager plugin;
    private final Map<UUID, Map<GameMode, PlayerState>> playerData = new HashMap<>();
    private final File dataFolder;

    public InventoryManager(GameModeManager plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create playerdata folder: " + dataFolder.getAbsolutePath());
        }
    }

    private boolean isTracked(GameMode gm) {
        return gm == GameMode.CREATIVE || gm == GameMode.SURVIVAL;
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        File file = getPlayerFile(uuid);
        Map<GameMode, PlayerState> modeMap = new HashMap<>();

        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (GameMode gm : GameMode.values()) {
                if (!isTracked(gm))
                    continue;
                String path = gm.name().toLowerCase(Locale.ROOT);
                ConfigurationSection section = config.getConfigurationSection(path);
                if (section != null) {
                    modeMap.put(gm, PlayerState.deserialize(section));
                }
            }
        }

        // Any missing gamemode gets an empty slate so getState() never returns null.
        for (GameMode gm : GameMode.values()) {
            if (isTracked(gm)) {
                modeMap.putIfAbsent(gm, PlayerState.createDefault());
            }
        }

        playerData.put(uuid, modeMap);
    }

    public void savePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        Map<GameMode, PlayerState> modeMap = playerData.get(uuid);
        if (modeMap == null)
            return;

        // Snapshot whatever they're holding right now before writing.
        captureCurrentState(player);

        File file = getPlayerFile(uuid);
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<GameMode, PlayerState> entry : modeMap.entrySet()) {
            GameMode gm = entry.getKey();
            if (!isTracked(gm))
                continue;
            String path = gm.name().toLowerCase(Locale.ROOT);
            ConfigurationSection section = config.createSection(path);
            entry.getValue().serialize(section);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void saveAllPlayers() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            savePlayer(p);
        }
    }

    public void removePlayerData(UUID uuid) {
        playerData.remove(uuid);
    }

    public void captureCurrentState(Player player) {
        GameMode gm = player.getGameMode();
        if (!isTracked(gm))
            return;
        setState(player, gm, PlayerState.fromPlayer(player));
    }

    public void setState(Player player, GameMode gm, PlayerState state) {
        UUID uuid = player.getUniqueId();
        Map<GameMode, PlayerState> modeMap = playerData.computeIfAbsent(uuid, k -> new HashMap<>());
        modeMap.put(gm, state);
    }

    public PlayerState getState(Player player, GameMode gm) {
        UUID uuid = player.getUniqueId();
        Map<GameMode, PlayerState> modeMap = playerData.get(uuid);
        if (modeMap == null)
            return PlayerState.createDefault();
        return modeMap.getOrDefault(gm, PlayerState.createDefault());
    }

    public void applyState(Player player, GameMode gm) {
        getState(player, gm).applyToPlayer(player);
    }

    public void switchGamemode(Player player, GameMode newGm) {
        captureCurrentState(player);
        if (isTracked(newGm)) {
            applyState(player, newGm);
        }
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid + ".yml");
    }

    /**
     * Snapshot of a player's inventory and vitals for one gamemode. All
     * fields are final so instances are effectively immutable.
     */
    public static class PlayerState {
        private final ItemStack[] inventory; // 41 slots on 1.21
        private final double health;
        private final int food;
        private final float saturation;
        private final int totalExperience;
        private final int level;
        private final float exp;
        private final List<PotionEffect> effects;

        private PlayerState(ItemStack[] inventory, double health, int food, float saturation,
                int totalExperience, int level, float exp, List<PotionEffect> effects) {
            this.inventory = inventory.clone();
            this.health = health;
            this.food = food;
            this.saturation = saturation;
            this.totalExperience = totalExperience;
            this.level = level;
            this.exp = exp;
            this.effects = new ArrayList<>(effects);
        }

        public static PlayerState fromPlayer(Player player) {
            PlayerInventory inv = player.getInventory();
            return new PlayerState(
                    inv.getContents(),
                    player.getHealth(),
                    player.getFoodLevel(),
                    player.getSaturation(),
                    player.getTotalExperience(),
                    player.getLevel(),
                    player.getExp(),
                    new ArrayList<>(player.getActivePotionEffects()));
        }

        public static PlayerState createDefault() {
            return new PlayerState(
                    new ItemStack[41],
                    20.0, 20, 5.0f, 0, 0, 0.0f,
                    Collections.emptyList());
        }

        // Map serialization is deprecated in favor of byte arrays, but the
        // YAML stays readable this way. Fine for per-player files.
        @SuppressWarnings("deprecation")
        public static PlayerState deserialize(ConfigurationSection section) {
            List<?> rawInv = section.getList("inventory");
            ItemStack[] inv = new ItemStack[41];
            if (rawInv != null) {
                int size = Math.min(rawInv.size(), 41);
                for (int i = 0; i < size; i++) {
                    Object obj = rawInv.get(i);
                    if (obj instanceof Map<?, ?> rawMap) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) rawMap;
                        inv[i] = ItemStack.deserialize(map);
                    } else {
                        inv[i] = null;
                    }
                }
            }

            // Clamp everything - a hand-edited or corrupted file shouldn't
            // let us call setHealth(0) and blow up at runtime.
            double health = section.getDouble("health", 20.0);
            if (!Double.isFinite(health) || health <= 0)
                health = 20.0;

            int food = clampInt(section.getInt("food", 20), 0, 20);
            float saturation = clampFloat((float) section.getDouble("saturation", 5.0), 0f, 20f);
            int totalExp = Math.max(0, section.getInt("totalExperience", 0));
            int level = Math.max(0, section.getInt("level", 0));
            float exp = clampFloat((float) section.getDouble("exp", 0.0), 0f, 1f);

            List<PotionEffect> effects = new ArrayList<>();
            List<?> rawEffects = section.getList("potionEffects");
            if (rawEffects != null) {
                for (Object obj : rawEffects) {
                    if (!(obj instanceof Map<?, ?> rawMap))
                        continue;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) rawMap;

                    // New format stores the namespaced key under "type".
                    // Legacy format stored the display name under "effect" -
                    // still supported so existing player files load cleanly.
                    String keyString = map.get("type") instanceof String s ? s : null;
                    PotionEffectType type;
                    if (keyString != null) {
                        NamespacedKey key = NamespacedKey.fromString(keyString);
                        type = key == null ? null : Registry.EFFECT.get(key);
                    } else {
                        String legacy = (String) map.get("effect");
                        type = legacy == null
                                ? null
                                : Registry.EFFECT.get(NamespacedKey.minecraft(legacy.toLowerCase(Locale.ROOT)));
                    }
                    if (type == null) {
                        continue;
                    }

                    int duration = toInt(map.get("duration"), 0);
                    int amplifier = toInt(map.get("amplifier"), 0);
                    boolean ambient = Boolean.TRUE.equals(map.get("ambient"));

                    // Accept both new keys ("particles"/"icon") and the old
                    // hyphenated ones for forward/backward compatibility.
                    boolean hasParticles = !Boolean.FALSE.equals(map.get("particles"))
                            && !Boolean.FALSE.equals(map.get("has-particles"));
                    boolean hasIcon = !Boolean.FALSE.equals(map.get("icon"))
                            && !Boolean.FALSE.equals(map.get("has-icon"));

                    effects.add(new PotionEffect(type, duration, amplifier, ambient, hasParticles, hasIcon));
                }
            }

            return new PlayerState(inv, health, food, saturation, totalExp, level, exp, effects);
        }

        // Registry lookup so we don't rely on a specific Attribute constant
        // name across Paper versions.
        private static double resolveMaxHealth(Player player) {
            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            return attr != null ? attr.getValue() : 20.0;
        }

        public void applyToPlayer(Player player) {
            ItemStack[] copy = Arrays.copyOf(inventory, player.getInventory().getSize());
            player.getInventory().setContents(copy);

            double maxHealth = resolveMaxHealth(player);
            double safeHealth = Math.max(1.0, Math.min(health, maxHealth));
            player.setHealth(safeHealth);

            player.setFoodLevel(food);
            player.setSaturation(saturation);
            player.setTotalExperience(totalExperience);
            player.setLevel(level);
            player.setExp(exp);
            player.clearActivePotionEffects();
            for (PotionEffect effect : effects) {
                player.addPotionEffect(effect);
            }
        }

        // Deprecated for the same reason as deserialize() above.
        @SuppressWarnings("deprecation")
        public void serialize(ConfigurationSection section) {
            List<Map<String, Object>> invList = new ArrayList<>(inventory.length);
            for (ItemStack item : inventory) {
                invList.add(item == null ? null : item.serialize());
            }

            section.set("inventory", invList);
            section.set("health", health);
            section.set("food", food);
            section.set("saturation", saturation);
            section.set("totalExperience", totalExperience);
            section.set("level", level);
            section.set("exp", exp);

            List<Map<String, Object>> effectList = new ArrayList<>(effects.size());
            for (PotionEffect effect : effects) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("type", effect.getType().getKey().toString());
                map.put("duration", effect.getDuration());
                map.put("amplifier", effect.getAmplifier());
                map.put("ambient", effect.isAmbient());
                map.put("particles", effect.hasParticles());
                map.put("icon", effect.hasIcon());
                effectList.add(map);
            }
            section.set("potionEffects", effectList);
        }

        // YAML numbers can round-trip as Long, Double, etc.
        private static int toInt(Object value, int fallback) {
            return value instanceof Number n ? n.intValue() : fallback;
        }

        private static int clampInt(int v, int min, int max) {
            return Math.max(min, Math.min(max, v));
        }

        private static float clampFloat(float v, float min, float max) {
            return Math.max(min, Math.min(max, v));
        }
    }
}