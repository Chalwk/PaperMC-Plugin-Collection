// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.util;

import java.util.List;

/**
 * An immutable chapter in the handbook. Content lines are stored raw, with
 * colour codes and page break markers still present. Page splitting happens
 * at book-build time, not here.
 */
public record Chapter(String id, String title, List<String> content) {
}