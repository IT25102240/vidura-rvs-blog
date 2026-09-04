-- ============================================================
-- ViduraRvs Blog — Database Migration Script
-- Compatible with: MySQL 8.0 (standard)
-- Run this ONCE on each environment (local + Oracle VM)
-- before starting the Spring Boot app for the first time.
--
-- HOW TO RUN IN MYSQL WORKBENCH:
--   1. Open MySQL Workbench
--   2. Connect to your local instance
--   3. Click File > Open SQL Script and select this file
--      OR paste the contents into a new SQL tab
--   4. Select the vidurarvs_blog schema from the left panel
--      (double-click it so it is bold/active)
--   5. Press Ctrl+Shift+Enter to run all statements
--
-- HOW TO RUN ON ORACLE VM (SSH terminal):
--   mysql -u vidura_blog -p vidurarvs_blog < db-migration.sql
-- ============================================================

-- Make sure you are using the right database
USE vidurarvs_blog;

-- ============================================================
-- 1. Add profile_picture_path column to users (if not already there)
-- ============================================================
DROP PROCEDURE IF EXISTS add_col_profile_picture;
DELIMITER ;;
CREATE PROCEDURE add_col_profile_picture()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'users'
          AND COLUMN_NAME  = 'profile_picture_path'
    ) THEN
        ALTER TABLE users ADD COLUMN profile_picture_path VARCHAR(300) NULL;
    END IF;
END;;
DELIMITER ;
CALL add_col_profile_picture();
DROP PROCEDURE IF EXISTS add_col_profile_picture;

-- ============================================================
-- 2. Add bio column to users (if not already there)
-- ============================================================
DROP PROCEDURE IF EXISTS add_col_bio;
DELIMITER ;;
CREATE PROCEDURE add_col_bio()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'users'
          AND COLUMN_NAME  = 'bio'
    ) THEN
        ALTER TABLE users ADD COLUMN bio TEXT NULL;
    END IF;
END;;
DELIMITER ;
CALL add_col_bio();
DROP PROCEDURE IF EXISTS add_col_bio;

-- ============================================================
-- 3. Create post_images table (multiple images per post)
-- ============================================================
CREATE TABLE IF NOT EXISTS post_images (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    post_id     BIGINT       NOT NULL,
    image_path  VARCHAR(300) NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    caption     VARCHAR(200) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_images_post
        FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. Performance index: posts by published + date
-- ============================================================
DROP PROCEDURE IF EXISTS add_idx_pub_created;
DELIMITER ;;
CREATE PROCEDURE add_idx_pub_created()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'posts'
          AND INDEX_NAME   = 'idx_posts_pub_created'
    ) THEN
        ALTER TABLE posts ADD INDEX idx_posts_pub_created (published, created_at DESC);
    END IF;
END;;
DELIMITER ;
CALL add_idx_pub_created();
DROP PROCEDURE IF EXISTS add_idx_pub_created;

-- ============================================================
-- 5. Performance index: posts by author
-- ============================================================
DROP PROCEDURE IF EXISTS add_idx_author;
DELIMITER ;;
CREATE PROCEDURE add_idx_author()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'posts'
          AND INDEX_NAME   = 'idx_posts_author'
    ) THEN
        ALTER TABLE posts ADD INDEX idx_posts_author (author_id);
    END IF;
END;;
DELIMITER ;
CALL add_idx_author();
DROP PROCEDURE IF EXISTS add_idx_author;

-- ============================================================
-- Done! You can now restart the Spring Boot application.
-- ============================================================
SELECT 'Migration completed successfully.' AS status;
