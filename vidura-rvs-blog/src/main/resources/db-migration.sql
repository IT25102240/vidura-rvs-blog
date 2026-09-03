-- ============================================================
-- ViduraRvs Blog Upgrade Migration
-- Run this ONCE before restarting the Spring Boot app.
-- ============================================================

-- 1. Add profile fields to users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS profile_picture_path VARCHAR(300) NULL,
    ADD COLUMN IF NOT EXISTS bio TEXT NULL;

-- 2. Multiple images per post
CREATE TABLE IF NOT EXISTS post_images (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT NOT NULL,
    image_path  VARCHAR(300) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0,
    caption     VARCHAR(200) NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_images_post (post_id)
);

-- 3. Performance indexes (safe to run even if index already exists with IF NOT EXISTS syntax)
CREATE INDEX IF NOT EXISTS idx_posts_pub_created ON posts(published, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_pub_cat     ON posts(published, category_id);

-- Done. Restart the Spring Boot app now.
