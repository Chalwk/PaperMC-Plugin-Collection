// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.model;

/**
 * A named collection of permissions that other groups and users can
 * inherit from. Groups are sorted by weight during inheritance resolution.
 */
public class Group extends Subject {

    /**
     * Fallback weight for a group whose weight option is missing or
     * unparseable. 1000 is intentionally high so an unconfigured group
     * loses to anything that's been given an explicit weight.
     */
    public static final int DEFAULT_WEIGHT = 1000;

    public Group(String name) {
        super(name);
    }

    /**
     * Lower weight = higher priority.
     *
     * <p>Weight is stored as a string option so it round-trips through
     * YAML alongside everything else, but it's parsed here so callers can
     * treat it as an int. A value that doesn't parse falls back to
     * {@link #DEFAULT_WEIGHT} rather than throwing.</p>
     */
    public int getWeight() {
        String raw = getOwnOption("weight");
        if (raw == null) return DEFAULT_WEIGHT;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_WEIGHT;
        }
    }

    public void setWeight(int weight) {
        setOption("weight", String.valueOf(weight));
    }
}