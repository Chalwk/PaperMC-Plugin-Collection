// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class MessageParser {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final GsonComponentSerializer GSON_SERIALIZER = GsonComponentSerializer.gson();

    public static List<Component> parseLines(List<String> lines) {
        List<Component> components = new ArrayList<>();
        for (String line : lines) {
            if (line == null)
                continue;
            String trimmed = line.trim();
            Component comp;
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                try {
                    comp = GSON_SERIALIZER.deserialize(trimmed);
                } catch (Exception e) {
                    comp = LEGACY_SERIALIZER.deserialize(line);
                }
            } else {
                comp = LEGACY_SERIALIZER.deserialize(line);
            }
            components.add(comp);
        }
        return components;
    }
}