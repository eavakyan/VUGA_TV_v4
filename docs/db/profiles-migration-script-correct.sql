-- Profiles Feature Migration Script (Corrected for unsigned int)
-- This script adds profile support to the VUGA TV application
-- Run this script in order on your MySQL database

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Create the app_user_profile table with correct data types
DROP TABLE IF EXISTS `app_user_profile`;
CREATE TABLE `app_user_profile` (
  `profile_id` int(11) NOT NULL AUTO_INCREMENT,
  `app_user_id` int(10) unsigned NOT NULL,  -- Match app_user table's unsigned int
  `name` varchar(100) NOT NULL,
  `avatar_type` enum('default','custom') DEFAULT 'default',
  `avatar_id` int(11) DEFAULT NULL COMMENT 'References default_avatar table or custom upload',
  `custom_avatar_url` varchar(500) DEFAULT NULL,
  `is_kids` tinyint(1) DEFAULT 0,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`profile_id`),
  KEY `idx_app_user_id` (`app_user_id`),
  CONSTRAINT `fk_profile_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Create default avatars table
DROP TABLE IF EXISTS `default_avatar`;
CREATE TABLE `default_avatar` (
  `avatar_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `color` varchar(7) DEFAULT NULL COMMENT 'Hex color code',
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`avatar_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Insert default avatars
INSERT INTO `default_avatar` (`name`, `image_url`, `color`) VALUES
('Blue User', 'avatars/avatar_blue.png', '#3B82F6'),
('Red User', 'avatars/avatar_red.png', '#EF4444'),
('Green User', 'avatars/avatar_green.png', '#10B981'),
('Purple User', 'avatars/avatar_purple.png', '#8B5CF6'),
('Orange User', 'avatars/avatar_orange.png', '#F97316'),
('Pink User', 'avatars/avatar_pink.png', '#EC4899'),
('Teal User', 'avatars/avatar_teal.png', '#14B8A6'),
('Yellow User', 'avatars/avatar_yellow.png', '#F59E0B');

-- 4. Create profile-specific watchlist table
DROP TABLE IF EXISTS `profile_watchlist`;
CREATE TABLE `profile_watchlist` (
  `watchlist_id` int(11) NOT NULL AUTO_INCREMENT,
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`watchlist_id`),
  UNIQUE KEY `unique_profile_content` (`profile_id`, `content_id`),
  KEY `idx_profile_id` (`profile_id`),
  KEY `idx_content_id` (`content_id`),
  CONSTRAINT `fk_watchlist_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_watchlist_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Create profile-specific favorites table
DROP TABLE IF EXISTS `profile_favorite`;
CREATE TABLE `profile_favorite` (
  `favorite_id` int(11) NOT NULL AUTO_INCREMENT,
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`favorite_id`),
  UNIQUE KEY `unique_profile_content` (`profile_id`, `content_id`),
  KEY `idx_profile_id` (`profile_id`),
  KEY `idx_content_id` (`content_id`),
  CONSTRAINT `fk_favorite_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorite_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Create profile-specific watch history table
DROP TABLE IF EXISTS `profile_watch_history`;
CREATE TABLE `profile_watch_history` (
  `history_id` int(11) NOT NULL AUTO_INCREMENT,
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `episode_id` int(11) DEFAULT NULL,
  `last_position` int(11) DEFAULT 0 COMMENT 'Position in seconds',
  `duration` int(11) DEFAULT 0 COMMENT 'Total duration in seconds',
  `completed` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`history_id`),
  UNIQUE KEY `unique_profile_content_episode` (`profile_id`, `content_id`, `episode_id`),
  KEY `idx_profile_id` (`profile_id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_last_watched` (`updated_at`),
  CONSTRAINT `fk_history_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_history_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_history_episode` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Create profile-specific downloads table
DROP TABLE IF EXISTS `profile_download`;
CREATE TABLE `profile_download` (
  `download_id` int(11) NOT NULL AUTO_INCREMENT,
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `episode_id` int(11) DEFAULT NULL,
  `source_id` int(11) NOT NULL,
  `download_path` varchar(500) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL COMMENT 'Size in bytes',
  `status` enum('pending','downloading','completed','failed','deleted') DEFAULT 'pending',
  `progress` int(3) DEFAULT 0 COMMENT 'Download progress percentage',
  `started_at` timestamp NULL DEFAULT NULL,
  `completed_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`download_id`),
  UNIQUE KEY `unique_profile_content_episode` (`profile_id`, `content_id`, `episode_id`),
  KEY `idx_profile_id` (`profile_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_download_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_download_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_download_episode` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Add last_active_profile_id to app_user table
ALTER TABLE `app_user` 
ADD COLUMN `last_active_profile_id` int(11) DEFAULT NULL,
ADD CONSTRAINT `fk_user_last_profile` FOREIGN KEY (`last_active_profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE SET NULL;

-- 9. Create default profile for existing users
INSERT INTO `app_user_profile` (`app_user_id`, `name`, `avatar_type`, `avatar_id`)
SELECT 
    u.app_user_id,
    COALESCE(u.fullname, 'Profile 1') as name,
    'default' as avatar_type,
    1 as avatar_id
FROM `app_user` u
WHERE NOT EXISTS (
    SELECT 1 FROM `app_user_profile` p WHERE p.app_user_id = u.app_user_id
);

-- 10. Migrate existing watchlist data to profile watchlist
INSERT IGNORE INTO `profile_watchlist` (`profile_id`, `content_id`)
SELECT 
    p.profile_id,
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(u.watchlist_content_ids, ',', numbers.n), ',', -1) AS UNSIGNED) as content_id
FROM `app_user` u
INNER JOIN `app_user_profile` p ON u.app_user_id = p.app_user_id
CROSS JOIN (
    SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL 
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL 
    SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL
    SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL
    SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) numbers
WHERE u.watchlist_content_ids IS NOT NULL 
AND u.watchlist_content_ids != ''
AND CHAR_LENGTH(u.watchlist_content_ids) - CHAR_LENGTH(REPLACE(u.watchlist_content_ids, ',', '')) >= numbers.n - 1
AND CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(u.watchlist_content_ids, ',', numbers.n), ',', -1) AS UNSIGNED) > 0;

-- 11. Check if app_user_watch_history table exists and migrate data
SET @table_exists = 0;
SELECT COUNT(*) INTO @table_exists 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'app_user_watch_history';

SET @sql = IF(@table_exists > 0,
    'INSERT IGNORE INTO `profile_watch_history` (`profile_id`, `content_id`, `episode_id`, `last_position`, `duration`, `completed`, `updated_at`)
    SELECT 
        p.profile_id,
        h.content_id,
        h.episode_id,
        h.last_watched_position,
        h.total_duration,
        h.completed,
        h.updated_at
    FROM `app_user_watch_history` h
    INNER JOIN `app_user_profile` p ON h.app_user_id = p.app_user_id',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 12. Update last_active_profile_id for users
UPDATE `app_user` u
SET u.last_active_profile_id = (
    SELECT p.profile_id 
    FROM `app_user_profile` p 
    WHERE p.app_user_id = u.app_user_id 
    ORDER BY p.created_at ASC 
    LIMIT 1
)
WHERE u.last_active_profile_id IS NULL;

-- 13. Create indexes for better performance
CREATE INDEX idx_profile_downloads_status ON profile_download(profile_id, status);
CREATE INDEX idx_profile_history_recent ON profile_watch_history(profile_id, updated_at DESC);
CREATE INDEX idx_profile_watchlist_recent ON profile_watchlist(profile_id, added_at DESC);
CREATE INDEX idx_profile_favorites_recent ON profile_favorite(profile_id, added_at DESC);

SET FOREIGN_KEY_CHECKS = 1;

-- End of migration script