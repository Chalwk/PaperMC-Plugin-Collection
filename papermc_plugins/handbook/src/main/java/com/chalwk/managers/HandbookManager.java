// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.managers;

import com.chalwk.Handbook;
import com.chalwk.config.HandbookConfig;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.Chapter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the written book from the loaded chapter list and hands it out.
 *
 * <p>
 * Also tracks which players have already received a copy, via a
 * PersistentDataContainer flag on the player. That lets the plugin skip
 * re-giving on subsequent joins, and lets staff use the same give path
 * without duplicating logic.
 * </p>
 */
public class HandbookManager {

    // Manual page break marker in the config. A line equal to this starts
    // a fresh page instead of contributing any content. Not tunable; it's
    // a syntax marker, not a setting.
    private static final String PAGE_BREAK = "---";

    // Minecraft's hard limit on rendered lines per page. Used for
    // validation and warnings only; the actual value is configurable.
    private static final int MINECRAFT_MAX_RENDERED_LINES = 14;

    private final Handbook plugin;
    private final NamespacedKey receivedKey;

    public HandbookManager(Handbook plugin) {
        this.plugin = plugin;
        this.receivedKey = new NamespacedKey(plugin, "handbook_received");
    }

    /**
     * Builds a fresh written book from the current handbook config. Called
     * on every give, so changes from /handbook reload apply immediately.
     */
    public ItemStack buildBook() {
        PluginConfig settings = plugin.getConfigManager().getConfig();
        HandbookConfig content = plugin.getConfigManager().getHandbook();

        int linesPerPage = Math.max(1, settings.getLinesPerPage());
        if (linesPerPage > MINECRAFT_MAX_RENDERED_LINES) {
            plugin.getLogger().warning("lines_per_page is set to " + linesPerPage
                    + " but Minecraft only renders " + MINECRAFT_MAX_RENDERED_LINES
                    + " lines per page. Content will spill.");
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) {
            return book;
        }

        meta.setTitle(truncate(content.getTitle(), 32));
        meta.setAuthor(truncate(content.getAuthor(), 32));

        List<String> pages = new ArrayList<>();
        for (Chapter chapter : content.getChapters()) {
            pages.addAll(buildPages(chapter, linesPerPage));
        }

        if (pages.isEmpty()) {
            pages.add(ChatColor.translateAlternateColorCodes('&', "&7This handbook is empty."));
        }

        int maxPages = Math.max(1, settings.getMaxPages());
        if (pages.size() > maxPages) {
            pages = pages.subList(0, maxPages);
        }

        meta.setPages(pages);
        book.setItemMeta(meta);
        return book;
    }

    /**
     * Splits a chapter into pages. The chapter title sits at the top of its
     * first page. Content lines wrap automatically when the page is full,
     * or when a manual break marker is hit.
     */
    private List<String> buildPages(Chapter chapter, int linesPerPage) {
        List<String> pages = new ArrayList<>();
        List<String> current = new ArrayList<>();

        // Chapter heading at the top of the first page.
        current.add("");
        current.add(translate("&6&l" + chapter.title()));
        current.add(translate("&8──────────"));
        current.add("");

        for (String raw : chapter.content()) {
            if (PAGE_BREAK.equals(raw.trim())) {
                flushPage(pages, current);
                continue;
            }

            if (current.size() >= linesPerPage) {
                flushPage(pages, current);
            }

            current.add(translate(raw));
        }

        if (!current.isEmpty()) {
            flushPage(pages, current);
        }

        return pages;
    }

    private void flushPage(List<String> pages, List<String> current) {
        if (current.isEmpty())
            return;
        pages.add(String.join("\n", current));
        current.clear();
    }

    /**
     * Translates '&' colour codes to the section-sign form the client
     * expects inside book pages.
     */
    @SuppressWarnings("deprecation")
    private String translate(String line) {
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    private String truncate(String input, int max) {
        return input.length() <= max ? input : input.substring(0, max);
    }

    /**
     * Gives the player a fresh handbook. Drops it at their feet if their
     * inventory is full and the config allows dropping.
     *
     * @return true if the book was given, false if it was skipped
     */
    public boolean giveToPlayer(Player player) {
        ItemStack book = buildBook();

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(book);
            return true;
        }

        if (plugin.getConfigManager().getConfig().isDropWhenInventoryFull()) {
            player.getWorld().dropItemNaturally(player.getLocation(), book);
            return true;
        }

        return false;
    }

    public boolean hasReceived(Player player) {
        return player.getPersistentDataContainer().has(receivedKey, PersistentDataType.BYTE);
    }

    public void markReceived(Player player) {
        player.getPersistentDataContainer().set(receivedKey, PersistentDataType.BYTE, (byte) 1);
    }
}