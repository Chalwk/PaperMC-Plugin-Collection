// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.model;

/**
 * A single permission grant or denial on a subject, with an optional expiry.
 *
 * <p>Expiry is stored as epoch milliseconds. A value of zero means the entry
 * is permanent. Anything else is checked against the current time when the
 * subject is queried, so an expired entry stops applying the moment it
 * expires, even before the scheduled sweep removes it from disk.</p>
 */
public record PermissionEntry(boolean value, long expiresAt) {

    public static PermissionEntry permanent(boolean value) {
        return new PermissionEntry(value, 0L);
    }

    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }
}