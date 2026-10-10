// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import com.chalwk.util.Chapter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parsed content from handbook.yml. Holds the book metadata and the ordered
 * list of chapters. Reloading replaces the list wholesale; there is no
 * merge or migration step.
 */
public class HandbookConfig {

    private String title = "Compendium of the Realm";
    private String author = "JeriCraft";
    private final List<Chapter> chapters = new ArrayList<>();

    public void loadFromConfig(ConfigurationSection config) {
        ConfigurationSection book = config.getConfigurationSection("book");
        if (book != null) {
            title = book.getString("title", title);
            author = book.getString("author", author);
        }

        chapters.clear();
        List<Map<?, ?>> rawChapters = config.getMapList("chapters");
        for (Map<?, ?> raw : rawChapters) {
            Object rawId = raw.get("id");
            Object rawTitle = raw.get("title");
            Object rawContent = raw.get("content");

            if (rawTitle == null || rawContent == null) {
                continue;
            }

            List<String> lines = new ArrayList<>();
            if (rawContent instanceof List<?> list) {
                for (Object o : list) {
                    lines.add(o == null ? "" : o.toString());
                }
            }

            String id = rawId == null ? "chapter_" + chapters.size() : rawId.toString();
            chapters.add(new Chapter(id, rawTitle.toString(), List.copyOf(lines)));
        }
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public List<Chapter> getChapters() {
        return List.copyOf(chapters);
    }
}