// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.NoctiView;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final NoctiView plugin;
    private final PluginConfig pluginConfig;
    private File configFile;

    public ConfigManager(NoctiView plugin) {
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
            plugin.saveResource("config.yml", false);
        }

        reloadConfig();
    }

    public void reloadConfig() {
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

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config: " + e.getMessage());
        }

        pluginConfig.loadFromConfig(config);
    }

    public void setWorldEnabled(String worldName, boolean enabled) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        config.set("worlds." + worldName, enabled);
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config: " + e.getMessage());
        }
        reloadConfig();
    }

    public PluginConfig getConfig() {
        return pluginConfig;
    }
}