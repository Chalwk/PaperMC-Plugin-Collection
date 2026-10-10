// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.util;

import java.util.List;

/**
 * An immutable perk definition. Ownership is determined externally by
 * checking the player against {@link #permissions()}.
 */
public record Perk(
        String id,
        String name,
        String category,
        double price,
        String packageId,
        List<String> description,
        List<String> permissions) {
}