-- ====================================
-- Remove old trailer_url column from content table
-- Purpose: Clean up after migration (RUN THIS ONLY AFTER CONFIRMING MIGRATION SUCCESS)
-- Date: 2025-01-08
-- WARNING: This will permanently remove the trailer_url column!
-- ====================================

-- STEP 1: Verify migration was successful
-- Run this query first to ensure all trailers were migrated
SELECT 
    'Pre-removal verification' as step,
    (SELECT COUNT(*) FROM content WHERE trailer_url IS NOT NULL AND trailer_url != '' AND trailer_url != '0') as original_trailers,
    (SELECT COUNT(*) FROM content_trailer) as migrated_trailers;

-- STEP 2: Create backup of trailer_url data (RECOMMENDED)
CREATE TABLE `content_trailer_url_backup` AS 
SELECT content_id, trailer_url, created_at, updated_at 
FROM content 
WHERE trailer_url IS NOT NULL AND trailer_url != '';

-- STEP 3: Update any views that reference trailer_url
-- Update the contents view to remove trailer_url reference
DROP VIEW IF EXISTS `contents`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `contents` AS 
SELECT 
    `content`.`content_id` AS `id`,
    `content`.`title` AS `title`,
    `content`.`description` AS `description`,
    `content`.`type` AS `type`,
    CASE WHEN `content`.`duration` IS NULL THEN NULL ELSE CAST(`content`.`duration` AS CHAR CHARSET utf8mb4) END AS `duration`,
    `content`.`release_year` AS `release_year`,
    `content`.`ratings` AS `ratings`,
    `content`.`language_id` AS `language_id`,
    -- trailer_url field removed - now handled by content_trailer table
    `content`.`vertical_poster` AS `vertical_poster`,
    `content`.`horizontal_poster` AS `horizontal_poster`,
    `content`.`genre_ids` AS `genre_ids`,
    `content`.`is_featured` AS `is_featured`,
    `content`.`is_show` AS `is_show`,
    `content`.`total_view` AS `total_view`,
    `content`.`total_download` AS `total_download`,
    `content`.`total_share` AS `total_share`,
    `content`.`created_at` AS `created_at`,
    `content`.`updated_at` AS `updated_at` 
FROM `content`;

-- STEP 4: Remove the trailer_url column (DESTRUCTIVE - DO AFTER VERIFICATION)
-- ALTER TABLE `content` DROP COLUMN `trailer_url`;

-- STEP 5: Verification after removal
-- SELECT 'Post-removal verification' as step, COUNT(*) as total_content_records FROM content;
-- SELECT 'Trailer records in new table' as step, COUNT(*) as total_trailer_records FROM content_trailer;

-- Note: Uncomment the ALTER TABLE and final SELECT statements above after verifying the migration was successful