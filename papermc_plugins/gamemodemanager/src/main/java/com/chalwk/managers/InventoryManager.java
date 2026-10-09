// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.GameModeManager;
import org.bukkit.GameMode;
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
                if (!isTracked(gm)) continue;
                String path = gm.name().toLowerCase();
                ConfigurationSection section = config.getConfigurationSection(path);
                if (section != null) {
                    PlayerState state = PlayerState.deserialize(section);
                    modeMap.put(gm, state);
                }
            }
        }

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
        if (modeMap == null) return;

        captureCurrentState(player);

        File file = getPlayerFile(uuid);
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<GameMode, PlayerState> entry : modeMap.entrySet()) {
            GameMode gm = entry.getKey();
            if (!isTracked(gm)) continue;
            String path = gm.name().toLowerCase();
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
        if (!isTracked(gm)) return;
        PlayerState state = PlayerState.fromPlayer(player);
        setState(player, gm, state);
    }

    public void setState(Player player, GameMode gm, PlayerState state) {
        UUID uuid = player.getUniqueId();
        Map<GameMode, PlayerState> modeMap = playerData.computeIfAbsent(uuid, k -> new HashMap<>());
        modeMap.put(gm, state);
    }

    public PlayerState getState(Player player, GameMode gm) {
        UUID uuid = player.getUniqueId();
        Map<GameMode, PlayerState> modeMap = playerData.get(uuid);
        if (modeMap == null) return PlayerState.createDefault();
        return modeMap.getOrDefault(gm, PlayerState.createDefault());
    }

    public void applyState(Player player, GameMode gm) {
        PlayerState state = getState(player, gm);
        state.applyToPlayer(player);
    }

    public void switchGamemode(Player player, GameMode newGm) {
        captureCurrentState(player);
        if (isTracked(newGm)) {
            applyState(player, newGm);
        }
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    public static class PlayerState {
        private final ItemStack[] inventory;      // size 41
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
            ItemStack[] contents = inv.getContents();
            return new PlayerState(
                    contents,
                    player.getHealth(),
                    player.getFoodLevel(),
                    player.getSaturation(),
                    player.getTotalExperience(),
                    player.getLevel(),
                    player.getExp(),
                    new ArrayList<>(player.getActivePotionEffects())
            );
        }

        public static PlayerState createDefault() {
            ItemStack[] empty = new ItemStack[41];
            return new PlayerState(
                    empty,
                    20.0,
                    20,
                    5.0f,
                    0,
                    0,
                    0.0f,
                    Collections.emptyList()
            );
        }

        public static PlayerState deserialize(ConfigurationSection section) {
            List<?> rawInv = section.getList("inventory");
            ItemStack[] inv = new ItemStack[41];
            if (rawInv != null) {
                int size = Math.min(rawInv.size(), 41);
                for (int i = 0; i < size; i++) {
                    Object obj = rawInv.get(i);
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) obj;
                        inv[i] = ItemStack.deserialize(map);
                    } else {
                        inv[i] = null;
                    }
                }
            }

            double health = section.getDouble("health", 20.0);
            int food = section.getInt("food", 20);
            float saturation = (float) section.getDouble("saturation", 5.0);
            int totalExp = section.getInt("totalExperience", 0);
            int level = section.getInt("level", 0);
            float exp = (float) section.getDouble("exp", 0.0);

            List<PotionEffect> effects = new ArrayList<>();
            List<?> rawEffects = section.getList("potionEffects");
            if (rawEffects != null) {
                for (Object obj : rawEffects) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) obj;

                        PotionEffectType type = PotionEffectType.getByName((String) map.get("effect"));
                        if (type == null) continue;

                        int duration = (int) map.getOrDefault("duration", 0);
                        int amplifier = (int) map.getOrDefault("amplifier", 0);
                        boolean ambient = (boolean) map.getOrDefault("ambient", false);
                        boolean hasParticles = (boolean) map.getOrDefault("has-particles", true);
                        boolean hasIcon = (boolean) map.getOrDefault("has-icon", true);

                        PotionEffect effect = new PotionEffect(type, duration, amplifier, ambient, hasParticles, hasIcon);
                        effects.add(effect);
                    }
                }
            }

            return new PlayerState(inv, health, food, saturation, totalExp, level, exp, effects);
        }

        public void applyToPlayer(Player player) {
            player.getInventory().setContents(inventory);
            player.setHealth(Math.min(health, player.getMaxHealth()));
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

        public void serialize(ConfigurationSection section) {
            List<Map<String, Object>> invList = new ArrayList<>();
            for (ItemStack item : inventory) {
                if (item != null) {
                    invList.add(item.serialize());
                } else {
                    invList.add(null);
                }
            }
            section.set("inventory", invList);

            section.set("health", health);
            section.set("food", food);
            section.set("saturation", saturation);
            section.set("totalExperience", totalExperience);
            section.set("level", level);
            section.set("exp", exp);

            List<Map<String, Object>> effectList = new ArrayList<>();
            for (PotionEffect effect : effects) {
                effectList.add(effect.serialize());
            }
            section.set("potionEffects", effectList);
        }
    }
}