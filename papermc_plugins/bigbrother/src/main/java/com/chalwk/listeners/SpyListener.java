// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.listeners;

import com.chalwk.BigBrother;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.SpyType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class SpyListener implements Listener {

    private final BigBrother plugin;

    public SpyListener(BigBrother plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!plugin.getSpyManager().isSpyEnabled(player, SpyType.COMMAND))
            return;
        if (config.isPlayerExcluded(player.getName()) || config.isWorldExcluded(player.getWorld().getName()))
            return;

        String fullCommand = event.getMessage();
        String[] parts = fullCommand.split(" ", 2);
        String baseCommand = parts[0].substring(1);

        if (config.isCommandExcluded(baseCommand))
            return;

        String message = config.getSpyMessage(SpyType.COMMAND)
                .replace("{player}", player.getName())
                .replace("{command}", fullCommand);

        notifyStaff(message, SpyType.COMMAND, player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!plugin.getSpyManager().isSpyEnabled(player, SpyType.SIGN))
            return;
        if (config.isPlayerExcluded(player.getName()) || config.isWorldExcluded(player.getWorld().getName()))
            return;

        StringBuilder signContent = new StringBuilder();
        for (Component component : event.lines()) {
            String line = PlainTextComponentSerializer.plainText().serialize(component);
            if (!line.trim().isEmpty()) {
                if (signContent.length() > 0)
                    signContent.append("\n");
                signContent.append(line);
            }
        }

        String message = config.getSpyMessage(SpyType.SIGN)
                .replace("{player}", player.getName())
                .replace("{sign_lines}", signContent.toString());

        notifyStaff(message, SpyType.SIGN, player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnvilUse(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        if (event.getInventory().getType() != InventoryType.ANVIL)
            return;

        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!plugin.getSpyManager().isSpyEnabled(player, SpyType.ANVIL))
            return;
        if (config.isPlayerExcluded(player.getName()) || config.isWorldExcluded(player.getWorld().getName()))
            return;

        if (event.getRawSlot() == 2) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.hasItemMeta()) {
                ItemMeta meta = result.getItemMeta();
                String oldName = "Unnamed";
                String newName;
                Component comp = meta.displayName();
                if (comp != null) {
                    newName = PlainTextComponentSerializer.plainText().serialize(comp);
                } else {
                    newName = "Unnamed";
                }

                String message = config.getSpyMessage(SpyType.ANVIL)
                        .replace("{player}", player.getName())
                        .replace("{old_name}", oldName)
                        .replace("{new_name}", newName);

                notifyStaff(message, SpyType.ANVIL, player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBookEdit(PlayerEditBookEvent event) {
        Player player = event.getPlayer();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!plugin.getSpyManager().isSpyEnabled(player, SpyType.BOOK))
            return;
        if (config.isPlayerExcluded(player.getName()) || config.isWorldExcluded(player.getWorld().getName()))
            return;

        BookMeta bookMeta = event.getNewBookMeta();
        String title = bookMeta.hasTitle() ? bookMeta.getTitle() : "Untitled";

        StringBuilder preview = new StringBuilder();
        if (bookMeta.hasPages()) {
            for (String page : bookMeta.getPages()) {
                String cleanPage = page.replace("\n", " ");
                if (cleanPage.length() > 50) {
                    preview.append(cleanPage, 0, 50).append("...");
                } else {
                    preview.append(cleanPage);
                }
                break;
            }
        }

        String message = config.getSpyMessage(SpyType.BOOK)
                .replace("{player}", player.getName())
                .replace("{title}", title)
                .replace("{preview}", preview.toString());

        notifyStaff(message, SpyType.BOOK, player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPortalTravel(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!plugin.getSpyManager().isSpyEnabled(player, SpyType.PORTAL))
            return;
        if (config.isPlayerExcluded(player.getName()) || config.isWorldExcluded(player.getWorld().getName()))
            return;

        String fromWorld = event.getFrom().getWorld().getName();
        String toWorld = event.getTo().getWorld().getName();

        String message = config.getSpyMessage(SpyType.PORTAL)
                .replace("{player}", player.getName())
                .replace("{from_world}", fromWorld)
                .replace("{to_world}", toWorld);

        notifyStaff(message, SpyType.PORTAL, player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        BlockState state = block.getState();
        if (!(state instanceof Sign sign))
            return;

        Player player = event.getPlayer();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!plugin.getSpyManager().isSpyEnabled(player, SpyType.SIGN))
            return;
        if (config.isPlayerExcluded(player.getName()) || config.isWorldExcluded(player.getWorld().getName()))
            return;

        StringBuilder signText = new StringBuilder();
        for (Component component : sign.lines()) {
            String line = PlainTextComponentSerializer.plainText().serialize(component);
            if (!line.trim().isEmpty()) {
                if (signText.length() > 0)
                    signText.append("\n");
                signText.append(line);
            }
        }

        String message = config.getSpyMessage(SpyType.SIGN)
                .replace("{player}", player.getName())
                .replace("{sign_lines}", signText.toString());

        notifyStaff(message, SpyType.SIGN, player);
    }

    private void notifyStaff(String message, SpyType spyType, Player triggerPlayer) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!config.isSpyEnabled(spyType))
            return;

        Component formattedMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.equals(triggerPlayer))
                continue;
            if (staff.hasPermission(spyType.getPermission())) {
                staff.sendMessage(formattedMessage);
            }
        }

        Bukkit.getConsoleSender().sendMessage(formattedMessage);
    }
}