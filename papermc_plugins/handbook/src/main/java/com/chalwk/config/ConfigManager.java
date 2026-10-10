// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.Handbook;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Handles loading and reloading of config.yml and handbook.yml. Neither file
 * is ever written back to, so comments and formatting survive reloads.
 */
public class ConfigManager {

    private final Handbook plugin;
    private final PluginConfig pluginConfig;
    private final HandbookConfig handbookConfig;
    private File configFile;
    private File handbookFile;

    public ConfigManager(Handbook plugin) {
        this.plugin = plugin;
        this.pluginConfig = new PluginConfig();
        this.handbookConfig = new HandbookConfig();
    }

    public void loadConfig() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder: " + dataFolder.getAbsolutePath());
        }

        configFile = new File(dataFolder, "config.yml");
        handbookFile = new File(dataFolder, "handbook.yml");

        saveIfMissing(configFile, "config.yml");
        saveIfMissing(handbookFile, "handbook.yml");

        reloadConfig();
    }

    private void saveIfMissing(File target, String resourceName) {
        if (target.exists()) return;
        if (plugin.getResource(resourceName) != null) {
            plugin.saveResource(resourceName, false);
        } else {
            plugin.getLogger().severe("Embedded " + resourceName + " is missing from the plugin JAR!");
        }
    }

    public void reloadConfig() {
        FileConfiguration config = loadWithDefaults(configFile, "config.yml");
        if (config != null) {
            pluginConfig.loadFromConfig(config);
        }

        FileConfiguration handbook = loadWithDefaults(handbookFile, "handbook.yml");
        if (handbook != null) {
            handbookConfig.loadFromConfig(handbook);
        }

        plugin.getLogger().info("Loaded " + handbookConfig.getChapters().size() + " handbook chapters.");
    }

    private FileConfiguration loadWithDefaults(File file, String resourceName) {
        if (file == null) {
            file = new File(plugin.getDataFolder(), resourceName);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        InputStream resourceStream = plugin.getResource(resourceName);
        if (resourceStream != null) {
            try (InputStreamReader defaultStream = new InputStreamReader(resourceStream, StandardCharsets.UTF_8)) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultStream);
                config.setDefaults(defaultConfig);
                config.options().copyDefaults(true);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to load default " + resourceName + ": " + e.getMessage());
            }
        }
        return config;
    }

    public PluginConfig getConfig() {
        return pluginConfig;
    }

    public HandbookConfig getHandbook() {
        return handbookConfig;
    }
}