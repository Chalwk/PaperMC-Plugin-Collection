// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.util;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

/**
 * Sends messages through Adventure, wrapped so the rest of the plugin doesn't
 * have to care which server implementation it's running on.
 *
 * <p>
 * The audiences instance is owned by the main plugin class. It's the thing
 * that knows how to push a Component to a Spigot CommandSender, which doesn't
 * natively understand Adventure at all.
 * </p>
 */
public final class MessageHelper {

    private final BukkitAudiences audiences;

    public MessageHelper(BukkitAudiences audiences) {
        this.audiences = audiences;
    }

    /**
     * Sends a legacy ampersand-formatted string. Used for command responses
     * where the message comes straight from config.
     */
    public void sendMessage(CommandSender sender, String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        audiences.sender(sender).sendMessage(component);
    }

    /**
     * Sends an already-parsed Component. Used by the scheduler, which parses
     * its broadcast lines ahead of time and then hands the results to us.
     */
    public void sendComponent(CommandSender sender, Component component) {
        audiences.sender(sender).sendMessage(component);
    }
}