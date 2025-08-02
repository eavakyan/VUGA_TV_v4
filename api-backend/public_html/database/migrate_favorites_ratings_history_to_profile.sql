-- Migration Script: Move favorites, ratings, and watch history from user level to profile level
-- Date: 2025-08-01
-- This script migrates app_user_favorite, app_user_rating, and app_user_watch_history to be profile-based

SET FOREIGN_KEY_CHECKS = 0;

-- Step 1: Create new profile-based tables

-- 1.1 Create app_profile_favorite table
DROP TABLE IF EXISTS `app_profile_favorite`;
CREATE TABLE `app_profile_favorite` (
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `added_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`profile_id`,`content_id`),
  KEY `idx_content_favorite` (`content_id`),
  KEY `idx_profile_favorite` (`profile_id`),
  CONSTRAINT `app_profile_favorite_profile_fk` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE,
  CONSTRAINT `app_profile_favorite_content_fk` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.2 Create app_profile_rating table  
DROP TABLE IF EXISTS `app_profile_rating`;
CREATE TABLE `app_profile_rating` (
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `rating` float NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`profile_id`,`content_id`),
  KEY `idx_content_rating` (`content_id`),
  KEY `idx_profile_rating` (`profile_id`),
  CONSTRAINT `app_profile_rating_profile_fk` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE,
  CONSTRAINT `app_profile_rating_content_fk` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_profile_rating` CHECK (`rating` >= 0 and `rating` <= 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.3 Create app_profile_watch_history table
DROP TABLE IF EXISTS `app_profile_watch_history`;
CREATE TABLE `app_profile_watch_history` (
  `watch_history_id` int(11) NOT NULL AUTO_INCREMENT,
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) DEFAULT NULL,
  `episode_id` int(11) DEFAULT NULL,
  `last_watched_position` int(11) DEFAULT 0 COMMENT 'Position in seconds',
  `total_duration` int(11) DEFAULT 0 COMMENT 'Total duration in seconds',
  `completed` tinyint(1) DEFAULT 0,
  `device_type` tinyint(1) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`watch_history_id`),
  UNIQUE KEY `uk_profile_content_episode` (`profile_id`,`content_id`,`episode_id`),
  KEY `idx_profile_content` (`profile_id`,`content_id`),
  KEY `idx_profile_episode` (`profile_id`,`episode_id`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `content_id` (`content_id`),
  KEY `episode_id` (`episode_id`),
  CONSTRAINT `app_profile_watch_history_profile_fk` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE,
  CONSTRAINT `app_profile_watch_history_content_fk` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `app_profile_watch_history_episode_fk` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 2: Migrate existing data

-- 2.1 Migrate favorites to the active profile
INSERT INTO app_profile_favorite (profile_id, content_id, added_at)
SELECT 
    COALESCE(u.last_active_profile_id, 
             (SELECT profile_id FROM app_user_profile WHERE app_user_id = f.app_user_id AND is_active = 1 LIMIT 1)) as profile_id,
    f.content_id,
    f.added_at
FROM app_user_favorite f
INNER JOIN app_user u ON f.app_user_id = u.app_user_id
WHERE COALESCE(u.last_active_profile_id, 
               (SELECT profile_id FROM app_user_profile WHERE app_user_id = f.app_user_id AND is_active = 1 LIMIT 1)) IS NOT NULL
ON DUPLICATE KEY UPDATE added_at = f.added_at;

-- 2.2 Migrate ratings to the active profile
INSERT INTO app_profile_rating (profile_id, content_id, rating, created_at, updated_at)
SELECT 
    COALESCE(u.last_active_profile_id, 
             (SELECT profile_id FROM app_user_profile WHERE app_user_id = r.app_user_id AND is_active = 1 LIMIT 1)) as profile_id,
    r.content_id,
    r.rating,
    r.created_at,
    r.updated_at
FROM app_user_rating r
INNER JOIN app_user u ON r.app_user_id = u.app_user_id
WHERE COALESCE(u.last_active_profile_id, 
               (SELECT profile_id FROM app_user_profile WHERE app_user_id = r.app_user_id AND is_active = 1 LIMIT 1)) IS NOT NULL
ON DUPLICATE KEY UPDATE rating = r.rating, updated_at = r.updated_at;

-- 2.3 Migrate watch history to the active profile
INSERT INTO app_profile_watch_history (profile_id, content_id, episode_id, last_watched_position, total_duration, completed, device_type, created_at, updated_at)
SELECT 
    COALESCE(u.last_active_profile_id, 
             (SELECT profile_id FROM app_user_profile WHERE app_user_id = h.app_user_id AND is_active = 1 LIMIT 1)) as profile_id,
    h.content_id,
    h.episode_id,
    h.last_watched_position,
    h.total_duration,
    h.completed,
    h.device_type,
    h.created_at,
    h.updated_at
FROM app_user_watch_history h
INNER JOIN app_user u ON h.app_user_id = u.app_user_id
WHERE COALESCE(u.last_active_profile_id, 
               (SELECT profile_id FROM app_user_profile WHERE app_user_id = h.app_user_id AND is_active = 1 LIMIT 1)) IS NOT NULL;

-- Step 3: Create backup tables (for rollback if needed)
CREATE TABLE IF NOT EXISTS `app_user_favorite_backup` AS SELECT * FROM `app_user_favorite`;
CREATE TABLE IF NOT EXISTS `app_user_rating_backup` AS SELECT * FROM `app_user_rating`;
CREATE TABLE IF NOT EXISTS `app_user_watch_history_backup` AS SELECT * FROM `app_user_watch_history`;

-- Step 4: Drop old tables (commented out for safety - run manually after verification)
-- DROP TABLE IF EXISTS `app_user_favorite`;
-- DROP TABLE IF EXISTS `app_user_rating`;
-- DROP TABLE IF EXISTS `app_user_watch_history`;

-- Step 5: Rename new tables to replace old ones (commented out for safety - run manually after verification)
-- RENAME TABLE `app_profile_favorite` TO `app_user_favorite`;
-- RENAME TABLE `app_profile_rating` TO `app_user_rating`;
-- RENAME TABLE `app_profile_watch_history` TO `app_user_watch_history`;

SET FOREIGN_KEY_CHECKS = 1;

-- Verification queries
SELECT 'Original favorites count:' as description, COUNT(*) as count FROM app_user_favorite
UNION ALL
SELECT 'Migrated favorites count:', COUNT(*) FROM app_profile_favorite
UNION ALL
SELECT 'Original ratings count:', COUNT(*) FROM app_user_rating
UNION ALL
SELECT 'Migrated ratings count:', COUNT(*) FROM app_profile_rating
UNION ALL
SELECT 'Original watch history count:', COUNT(*) FROM app_user_watch_history
UNION ALL
SELECT 'Migrated watch history count:', COUNT(*) FROM app_profile_watch_history;