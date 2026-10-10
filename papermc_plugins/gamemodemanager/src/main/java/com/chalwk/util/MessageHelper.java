// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.util;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

/**
 * Sends messages through Adventure, wrapped so the rest of the plugin
 * doesn't have to care which server implementation it's running on.
 *
 * <p>BukkitAudiences is the bridge that lets us push Components to a Spigot
 * CommandSender, which doesn't natively understand Adventure.</p>
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