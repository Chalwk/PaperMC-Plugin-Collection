// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.AutoMessages;
import com.chalwk.util.MessageHelper;
import com.chalwk.util.MessageParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * Fires broadcasts on a fixed interval and rotates through the configured
 * entries in order.
 *
 * <p>
 * Holds the current position in the cycle as an index. Restarting the
 * scheduler resets it back to the top, which matches the plugin's documented
 * "reload resets the broadcast index" behaviour.
 * </p>
 */
public class MessageScheduler {

    private final AutoMessages plugin;
    private final MessageHelper messageHelper;
    private BukkitTask task;
    private int currentIndex = 0;

    public MessageScheduler(AutoMessages plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    public void start() {
        // Guard against double-start. Nothing should be calling start twice,
        // but it's cheap insurance.
        if (task != null)
            return;

        int intervalTicks = plugin.getConfigManager().getConfig().getInterval() * 20;
        // Anything under a second would just spam chat. Clamp it.
        if (intervalTicks < 20)
            intervalTicks = 20;

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastNext, intervalTicks, intervalTicks);
        plugin.getLogger().info("Message scheduler started (interval: " + (intervalTicks / 20) + "s)");
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
            plugin.getLogger().info("Message scheduler stopped");
        }
    }

    public void restart() {
        stop();
        currentIndex = 0;
        start();
    }

    /**
     * Called on every tick interval. Pulls the next broadcast, parses it,
     * and sends it to everyone online plus the console.
     */
    private void broadcastNext() {
        List<List<String>> messages = plugin.getConfigManager().getConfig().getBroadcasts();
        if (messages.isEmpty()) {
            return;
        }

        // Wrap around if we've hit the end of the list. The config might have
        // shrunk since the last tick, so this can't just be an equals check.
        if (currentIndex >= messages.size()) {
            currentIndex = 0;
        }

        List<String> lines = messages.get(currentIndex);
        List<Component> components = MessageParser.parseLines(lines);

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Component comp : components) {
                messageHelper.sendComponent(player, comp);
            }
        }
        for (Component comp : components) {
            messageHelper.sendComponent(Bukkit.getConsoleSender(), comp);
        }

        currentIndex++;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }
}