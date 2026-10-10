// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.BigBrother;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Handles loading and reloading of {@code config.yml}.
 *
 * <p>Defaults from the JAR-bundled config are applied in memory on every load,
 * so newly added keys work even if the user's on-disk file predates them.
 * The plugin never writes back to the user's file after the initial save.</p>
 */
public class ConfigManager {

    private final BigBrother plugin;
    private final PluginConfig pluginConfig;
    private File configFile;

    public ConfigManager(BigBrother plugin) {
        this.plugin = plugin;
        this.pluginConfig = new PluginConfig(plugin);
    }

    /**
     * Called once during onEnable. Creates the plugin data folder if needed,
     * writes a fresh config.yml if the user doesn't have one, then loads it.
     */
    public void loadConfig() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder: " + dataFolder.getAbsolutePath());
        }

        configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            // saveResource throws IllegalArgumentException if the resource isn't
            // in the JAR. Guard against that so we log something readable.
            if (plugin.getResource("config.yml") != null) {
                plugin.saveResource("config.yml", false);
            } else {
                plugin.getLogger().severe("Embedded config.yml is missing from the plugin JAR!");
            }
        }

        reloadConfig();
    }

    /**
     * Loads config.yml from disk and applies bundled defaults for any keys the
     * user's file doesn't define.
     *
     * <p>Deliberately does not save after loading. YamlConfiguration#save
     * doesn't preserve comments, so calling it here would strip every comment
     * from the user's file on the first reload. Defaults are still available
     * in memory thanks to copyDefaults(true).</p>
     */
    public void reloadConfig() {
        // Defensive: if something calls reloadConfig before loadConfig, we
        // still need a File to point at. Normally loadConfig sets this first.
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        InputStream resourceStream = plugin.getResource("config.yml");
        if (resourceStream != null) {
            try (InputStreamReader defaultConfigStream = new InputStreamReader(resourceStream,
                    StandardCharsets.UTF_8)) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
                config.setDefaults(defaultConfig);
                config.options().copyDefaults(true);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to load default config.yml: " + e.getMessage());
            }
        } else {
            plugin.getLogger().warning("Default config.yml not found inside plugin jar!");
        }

        pluginConfig.loadFromConfig(config);
    }

    public PluginConfig getConfig() {
        return pluginConfig;
    }
}