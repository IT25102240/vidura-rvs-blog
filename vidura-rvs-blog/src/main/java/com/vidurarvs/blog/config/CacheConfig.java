package com.vidurarvs.blog.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Activates Spring's caching abstraction. Uses the built-in
 * ConcurrentMapCache (no extra dependency needed), which is
 * sufficient for a single-VM Oracle Free Tier deployment.
 *
 * Cached items:
 *   - "navCategories" — the nav-bar category list (evicted only when
 *     a category is added/changed, which is very rare).
 *
 * This eliminates the DB query that previously fired on every single
 * page load just to populate the navigation category strip.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    // Spring Boot auto-configures a ConcurrentMapCacheManager when
    // @EnableCaching is present and no CacheManager bean is defined.
    // Nothing else is required for single-node deployments.
}
