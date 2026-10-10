// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.nexus.model;

/**
 * A player. Identical to {@link Subject} in structure; the class exists so
 * that code can distinguish users from groups via instanceof and apply
 * user-specific behaviour (like the default group fallback) where needed.
 */
public class User extends Subject {

    public User(String name) {
        super(name);
    }
}