// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.model;

import java.util.UUID;

/**
 * A player.
 *
 * <p>Distinct from {@link Group} mainly because of the UUID. Users get one
 * assigned the first time they join, and it's used as the storage key in
 * users.yml going forward. The name is still kept because that's what
 * admins type at the command line, but it's just a label.</p>
 */
public class User extends Subject {

    private UUID uuid;

    public User(String name) {
        super(name);
    }

    public UUID getUuid() {
        return uuid;
    }

    /** Set once when the player first joins. Never cleared afterwards. */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}