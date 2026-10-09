// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.AutoMessages;
import com.chalwk.util.MessageParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class MessageScheduler {

    private final AutoMessages plugin;
    private BukkitTask task;
    private int currentIndex = 0;

    public MessageScheduler(AutoMessages plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null)
            return;

        int intervalTicks = plugin.getConfigManager().getConfig().getInterval() * 20;
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

    private void broadcastNext() {
        List<List<String>> messages = plugin.getConfigManager().getConfig().getMessages();
        if (messages.isEmpty()) {
            return;
        }

        if (currentIndex >= messages.size()) {
            currentIndex = 0;
        }

        List<String> lines = messages.get(currentIndex);
        List<Component> components = MessageParser.parseLines(lines);

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Component comp : components) {
                player.sendMessage(comp);
            }
        }
        for (Component comp : components) {
            Bukkit.getConsoleSender().sendMessage(comp);
        }

        currentIndex++;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }
}