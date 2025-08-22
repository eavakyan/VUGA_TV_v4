-- Migration: Create Episode Watchlist Table
-- Date: 2025-08-21
-- Purpose: Allow episodes to be added to user profile watchlists

-- Create table for episode watchlist
CREATE TABLE IF NOT EXISTS `app_profile_episode_watchlist` (
  `profile_id` int(11) NOT NULL,
  `episode_id` int(11) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`profile_id`,`episode_id`),
  KEY `idx_episode_watchlist` (`episode_id`),
  KEY `idx_profile_episode_watchlist` (`profile_id`),
  KEY `idx_episode_watchlist_created` (`created_at` DESC),
  CONSTRAINT `app_profile_episode_watchlist_episode_fk` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE,
  CONSTRAINT `app_profile_episode_watchlist_profile_fk` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create view for unified watchlist queries
CREATE OR REPLACE VIEW `app_profile_unified_watchlist` AS
SELECT 
    awl.profile_id,
    awl.content_id,
    NULL as episode_id,
    'content' as item_type,
    c.title,
    c.poster,
    c.type as content_type,
    c.ratings,
    NULL as series_title,
    NULL as season_number,
    NULL as episode_number,
    NULL as episode_thumbnail,
    awl.created_at,
    awl.updated_at
FROM app_user_watchlist awl
JOIN content c ON awl.content_id = c.content_id
WHERE c.is_show = 1

UNION ALL

SELECT 
    aew.profile_id,
    s.content_id,
    aew.episode_id,
    'episode' as item_type,
    e.title,
    e.thumbnail as poster,
    2 as content_type, -- TV Series type
    e.ratings,
    c.title as series_title,
    s.season_number,
    e.number as episode_number,
    e.thumbnail as episode_thumbnail,
    aew.created_at,
    aew.updated_at
FROM app_profile_episode_watchlist aew
JOIN episode e ON aew.episode_id = e.episode_id
JOIN season s ON e.season_id = s.season_id
JOIN content c ON s.content_id = c.content_id
WHERE c.is_show = 1;