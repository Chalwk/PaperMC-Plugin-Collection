// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.NoctiView;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.MessageHelper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NightVisionManager {

    private final NoctiView plugin;
    private final Set<UUID> activeNightVision;

    public NightVisionManager(NoctiView plugin) {
        this.plugin = plugin;
        this.activeNightVision = new HashSet<>();
    }

    public void toggleNightVision(Player player) {
        UUID playerId = player.getUniqueId();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!config.isWorldEnabled(player.getWorld().getName())) {
            MessageHelper.sendMessage(player, config.getWorldNotEnabledSelfMsg());
            return;
        }

        if (activeNightVision.contains(playerId)) {
            removeNightVision(player);
            MessageHelper.sendMessage(player, config.getNightVisionDisabledMsg());
        } else {
            addNightVision(player);
            MessageHelper.sendMessage(player, config.getNightVisionEnabledMsg());
        }
    }

    public void toggleNightVisionForPlayer(Player target, CommandSender executor) {
        UUID targetId = target.getUniqueId();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!config.isWorldEnabled(target.getWorld().getName())) {
            MessageHelper.sendMessage(executor,
                    config.getWorldNotEnabledOtherMsg()
                            .replace("{world}", target.getWorld().getName()));
            return;
        }

        if (activeNightVision.contains(targetId)) {
            removeNightVision(target);
            MessageHelper.sendMessage(executor,
                    config.getNightVisionDisabledForMsg()
                            .replace("{player}", target.getName()));
            MessageHelper.sendMessage(target,
                    config.getNightVisionDisabledByMsg()
                            .replace("{sender}", executor.getName()));
        } else {
            addNightVision(target);
            MessageHelper.sendMessage(executor,
                    config.getNightVisionEnabledForMsg()
                            .replace("{player}", target.getName()));
            MessageHelper.sendMessage(target,
                    config.getNightVisionEnabledByMsg()
                            .replace("{sender}", executor.getName()));
        }
    }

    public void removeAllNightVision() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        activeNightVision.clear();
    }

    private void addNightVision(Player player) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        PotionEffect nightVision = new PotionEffect(
                PotionEffectType.NIGHT_VISION,
                config.getEffectDuration(),
                config.getEffectAmplifier(),
                true,
                false);
        player.addPotionEffect(nightVision);
        activeNightVision.add(player.getUniqueId());
        playEffects(player);
    }

    private void removeNightVision(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        activeNightVision.remove(player.getUniqueId());
        playEffects(player);
    }

    private void playEffects(Player player) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        Location location = player.getLocation().add(0, 1, 0);

        if (config.isParticleEnabled()) {
            try {
                Particle particle = Particle.valueOf(config.getParticleType());
                player.spawnParticle(
                        particle,
                        location,
                        config.getParticleCount(),
                        config.getParticleOffsetX(),
                        config.getParticleOffsetY(),
                        config.getParticleOffsetZ(),
                        config.getParticleSpeed());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid particle type: " + config.getParticleType());
            }
        }

        if (config.isSoundEnabled()) {
            try {
                Sound sound = Sound.valueOf(config.getSoundType());
                player.playSound(
                        location,
                        sound,
                        (float) config.getSoundVolume(),
                        (float) config.getSoundPitch());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound type: " + config.getSoundType());
            }
        }
    }
}