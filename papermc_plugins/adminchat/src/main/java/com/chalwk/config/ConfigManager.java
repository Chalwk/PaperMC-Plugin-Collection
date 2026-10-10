// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.AdminChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final AdminChat plugin;
    private final PluginConfig pluginConfig;
    private File configFile;

    public ConfigManager(AdminChat plugin) {
        this.plugin = plugin;
        this.pluginConfig = new PluginConfig(plugin);
    }

    public void loadConfig() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder: " + dataFolder.getAbsolutePath());
        }

        configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            if (plugin.getResource("config.yml") != null) {
                plugin.saveResource("config.yml", false);
            } else {
                plugin.getLogger().severe("Embedded config.yml is missing from the plugin JAR!");
            }
        }

        reloadConfig();
    }

    public void reloadConfig() {
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

        // Intentionally not saving. YamlConfiguration#save strips comments.
        pluginConfig.loadFromConfig(config);
    }

    public PluginConfig getConfig() {
        return pluginConfig;
    }
}