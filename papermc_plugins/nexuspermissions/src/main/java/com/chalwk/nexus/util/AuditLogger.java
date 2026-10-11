// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.util;

import com.chalwk.nexus.NexusPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Appends a line to {@code plugins/NexusPermissions/audit.log} for every
 * mutation a command performs.
 *
 * <p>Format is one entry per line, timestamp first, actor in square brackets,
 * then the action in a command-like syntax. Good enough to grep, good enough
 * to read. Not intended as a structured log.</p>
 *
 * <p>Controlled by {@code audit-log.enabled} in config.yml. When disabled,
 * {@link #log} is a no-op and nothing is written to disk.</p>
 */
public final class AuditLogger {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NexusPermissions plugin;
    private final File file;
    private volatile boolean enabled;

    public AuditLogger(NexusPermissions plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "audit.log");
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Writes one audit line. Silent if the logger is disabled. Failures are
     * logged once as a warning and then the logger disables itself, so a
     * broken disk doesn't spam the console on every command.
     */
    public void log(CommandSender actor, String action) {
        if (!enabled) return;
        String timestamp = LocalDateTime.now().format(FORMAT);
        String who = actor instanceof Player p ? p.getName() : "CONSOLE";
        String line = "[" + timestamp + "] [" + who + "] " + action;
        try (FileWriter w = new FileWriter(file, true)) {
            w.write(line);
            w.write(System.lineSeparator());
        } catch (IOException e) {
            plugin.getLogger().warning("Audit log write failed, disabling: " + e.getMessage());
            enabled = false;
        }
    }
}