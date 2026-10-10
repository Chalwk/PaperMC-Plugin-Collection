// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.util;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

/**
 * Sends messages to any {@link CommandSender} through Adventure.
 *
 * <p>Wraps {@link BukkitAudiences} so the rest of the plugin never has to
 * think about which server implementation it's running on. The audiences
 * instance is owned by the main plugin class and passed in here.</p>
 *
 * <p>Input is legacy ampersand-formatted text. That's what config files use
 * and what server admins expect to be able to type.</p>
 */
public final class MessageHelper {

    private final BukkitAudiences audiences;

    public MessageHelper(BukkitAudiences audiences) {
        this.audiences = audiences;
    }

    public void sendMessage(CommandSender sender, String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        audiences.sender(sender).sendMessage(component);
    }
}