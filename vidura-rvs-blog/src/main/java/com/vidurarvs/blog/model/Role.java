package com.vidurarvs.blog.model;

/**
 * The two publishing roles on ViduraRvs.
 *
 * SUPER_ADMIN  - the blog owner (you). Can publish content AND grant/revoke
 *                admin access to other verified contributors.
 * ADMIN        - a verified contributor invited by the SUPER_ADMIN. Can
 *                publish and manage their own posts, but cannot manage
 *                other admin accounts.
 */
public enum Role {
    SUPER_ADMIN,
    ADMIN
}
