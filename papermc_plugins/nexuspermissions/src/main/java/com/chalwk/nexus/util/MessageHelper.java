// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.util;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

/**
 * Sends messages through Adventure, wrapped so the rest of the plugin
 * doesn't have to care which server implementation it's running on.
 *
 * <p>BukkitAudiences is the bridge that lets us push Components to a Spigot
 * CommandSender, which doesn't natively understand Adventure. Adventure
 * itself is shaded and relocated into the JAR by the build.</p>
 *
 * <p>Unlike the static helpers elsewhere in the collection, this one is
 * instance-based so the audience can be shared and closed cleanly on
 * disable.</p>
 */
public final class MessageHelper {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private static final Component PREFIX = Component.text("[", NamedTextColor.DARK_GRAY)
            .append(Component.text("Nexus", NamedTextColor.AQUA))
            .append(Component.text("] ", NamedTextColor.DARK_GRAY));

    private final BukkitAudiences audiences;

    public MessageHelper(BukkitAudiences audiences) {
        this.audiences = audiences;
    }

    public void send(CommandSender sender, Component component) {
        audiences.sender(sender).sendMessage(component);
    }

    public void success(CommandSender sender, String message) {
        send(sender, PREFIX.append(Component.text(message, NamedTextColor.GREEN)));
    }

    public void error(CommandSender sender, String message) {
        send(sender, PREFIX.append(Component.text(message, NamedTextColor.RED)));
    }

    public void info(CommandSender sender, String message) {
        send(sender, PREFIX.append(Component.text(message, NamedTextColor.GRAY)));
    }

    public void noPermission(CommandSender sender) {
        error(sender, "You don't have permission to do that.");
    }

    /** Divider-style heading, used at the top of info blocks. */
    public void header(CommandSender sender, String title) {
        send(sender, Component.text("--- ", NamedTextColor.DARK_GRAY)
                .append(Component.text(title, NamedTextColor.AQUA))
                .append(Component.text(" ---", NamedTextColor.DARK_GRAY)));
    }

    /** One help line: command in yellow, description in grey. */
    public void line(CommandSender sender, String cmd, String description) {
        send(sender, Component.text(cmd, NamedTextColor.YELLOW)
                .append(Component.text(" - " + description, NamedTextColor.GRAY)));
    }

    public void kv(CommandSender sender, String key, Component value) {
        send(sender, Component.text("  " + key + ": ", NamedTextColor.GOLD).append(value));
    }

    public void kv(CommandSender sender, String key, String value) {
        kv(sender, key, Component.text(value, NamedTextColor.WHITE));
    }

    /** Converts &-style legacy colour codes to a Component. */
    public Component legacy(String text) {
        return LEGACY.deserialize(text);
    }
}