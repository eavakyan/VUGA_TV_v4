/*
 Navicat MySQL Data Transfer

 Source Server         : DEV-iosdev.gossip-stone.com
 Source Server Type    : MySQL
 Source Server Version : 101110 (10.11.10-MariaDB-log)
 Source Host           : 195.35.15.43:3306
 Source Schema         : u853155779_iosdev

 Target Server Type    : MySQL
 Target Server Version : 101110 (10.11.10-MariaDB-log)
 File Encoding         : 65001

 Date: 23/08/2025 15:30:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for actor
-- ----------------------------
DROP TABLE IF EXISTS `actor`;
CREATE TABLE `actor` (
  `actor_id` int(11) NOT NULL AUTO_INCREMENT,
  `fullname` varchar(100) DEFAULT NULL,
  `dob` varchar(11) NOT NULL,
  `bio` varchar(900) NOT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`actor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user` (
  `admin_user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `user_password` varchar(255) NOT NULL,
  `user_type` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `last_login_at` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `failed_login_attempts` int(11) DEFAULT 0,
  PRIMARY KEY (`admin_user_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for admob_config
-- ----------------------------
DROP TABLE IF EXISTS `admob_config`;
CREATE TABLE `admob_config` (
  `admob_config_id` int(11) NOT NULL AUTO_INCREMENT,
  `banner_id` varchar(255) NOT NULL,
  `interstitial_id` varchar(255) NOT NULL,
  `rewarded_id` varchar(255) NOT NULL,
  `type` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`admob_config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for age_limit
-- ----------------------------
DROP TABLE IF EXISTS `age_limit`;
CREATE TABLE `age_limit` (
  `age_limit_id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `min_age` int(11) NOT NULL DEFAULT 0,
  `max_age` int(11) DEFAULT NULL,
  `code` varchar(10) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`age_limit_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for age_limit_backup
-- ----------------------------
DROP TABLE IF EXISTS `age_limit_backup`;
CREATE TABLE `age_limit_backup` (
  `age_limit_id` int(11) NOT NULL DEFAULT 0,
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_language
-- ----------------------------
DROP TABLE IF EXISTS `app_language`;
CREATE TABLE `app_language` (
  `app_language_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `code` varchar(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`app_language_id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for app_profile_episode_rating
-- ----------------------------
DROP TABLE IF EXISTS `app_profile_episode_rating`;
CREATE TABLE `app_profile_episode_rating` (
  `profile_id` int(11) NOT NULL,
  `episode_id` int(11) NOT NULL,
  `rating` float NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`profile_id`,`episode_id`),
  KEY `idx_episode_rating` (`episode_id`),
  KEY `idx_profile_episode_rating` (`profile_id`),
  CONSTRAINT `app_profile_episode_rating_episode_fk` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE,
  CONSTRAINT `app_profile_episode_rating_profile_fk` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_episode_rating` CHECK (`rating` >= 0 and `rating` <= 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_profile_episode_watchlist
-- ----------------------------
DROP TABLE IF EXISTS `app_profile_episode_watchlist`;
CREATE TABLE `app_profile_episode_watchlist` (
  `profile_id` int(11) NOT NULL,
  `episode_id` int(11) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`profile_id`,`episode_id`),
  KEY `idx_profile_id` (`profile_id`),
  KEY `idx_episode_id` (`episode_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_epw_episode` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_epw_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_profile_favorite
-- ----------------------------
DROP TABLE IF EXISTS `app_profile_favorite`;
CREATE TABLE `app_profile_favorite` (
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `added_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`profile_id`,`content_id`),
  KEY `idx_content_favorite` (`content_id`),
  KEY `idx_profile_favorite` (`profile_id`),
  KEY `idx_profile_favorite_recent` (`profile_id`,`added_at` DESC),
  CONSTRAINT `app_profile_favorite_content_fk` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `app_profile_favorite_profile_fk` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_profile_rating
-- ----------------------------
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
  CONSTRAINT `app_profile_rating_content_fk` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `app_profile_rating_profile_fk` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_profile_rating` CHECK (`rating` >= 0 and `rating` <= 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_profile_watch_history
-- ----------------------------
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
  KEY `idx_watch_history_profile_recent` (`profile_id`,`updated_at` DESC,`completed`),
  CONSTRAINT `app_profile_watch_history_content_fk` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `app_profile_watch_history_episode_fk` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE,
  CONSTRAINT `app_profile_watch_history_profile_fk` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1740 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_user
-- ----------------------------
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user` (
  `app_user_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fullname` varchar(50) DEFAULT NULL,
  `email` varchar(55) DEFAULT NULL,
  `login_type` tinyint(1) DEFAULT 0 COMMENT '1 = Google / 2 = Apple / 3 = Email',
  `identity` text DEFAULT NULL,
  `profile_image` text DEFAULT NULL,
  `watchlist_content_ids` varchar(255) DEFAULT NULL,
  `device_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 = Andriod, 1 = iOS',
  `device_token` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `last_active_profile_id` int(11) DEFAULT NULL,
  `sms_consent` tinyint(1) DEFAULT 0 COMMENT 'User consent for SMS marketing messages',
  `email_consent` tinyint(1) DEFAULT 0 COMMENT 'User consent for email marketing messages',
  `sms_consent_date` timestamp NULL DEFAULT NULL COMMENT 'Date when SMS consent was given/revoked',
  `email_consent_date` timestamp NULL DEFAULT NULL COMMENT 'Date when email consent was given/revoked',
  `consent_ip_address` varchar(45) DEFAULT NULL COMMENT 'IP address when consent was last updated',
  PRIMARY KEY (`app_user_id`),
  KEY `idx_app_user_email` (`email`),
  KEY `idx_app_user_device_type` (`device_type`),
  KEY `fk_user_last_profile` (`last_active_profile_id`),
  KEY `idx_app_user_login` (`email`,`login_type`),
  KEY `idx_user_activity` (`device_type`,`created_at`),
  CONSTRAINT `fk_user_last_profile` FOREIGN KEY (`last_active_profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for app_user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `app_user_favorite`;
CREATE TABLE `app_user_favorite` (
  `app_user_id` int(10) unsigned NOT NULL,
  `content_id` int(11) NOT NULL,
  `added_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`app_user_id`,`content_id`),
  KEY `idx_content_favorite` (`content_id`),
  CONSTRAINT `app_user_favorite_ibfk_1` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE,
  CONSTRAINT `app_user_favorite_ibfk_2` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_user_favorite_backup
-- ----------------------------
DROP TABLE IF EXISTS `app_user_favorite_backup`;
CREATE TABLE `app_user_favorite_backup` (
  `app_user_id` int(10) unsigned NOT NULL,
  `content_id` int(11) NOT NULL,
  `added_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_user_profile
-- ----------------------------
DROP TABLE IF EXISTS `app_user_profile`;
CREATE TABLE `app_user_profile` (
  `profile_id` int(11) NOT NULL AUTO_INCREMENT,
  `app_user_id` int(10) unsigned NOT NULL,
  `name` varchar(100) NOT NULL,
  `avatar_type` enum('default','custom') DEFAULT 'default',
  `avatar_id` int(11) DEFAULT NULL COMMENT 'References default_avatar table or custom upload',
  `avatar_url` varchar(255) DEFAULT NULL,
  `avatar_color` varchar(7) DEFAULT NULL,
  `custom_avatar_url` varchar(500) DEFAULT NULL,
  `custom_avatar_uploaded_at` timestamp NULL DEFAULT NULL,
  `is_kids` tinyint(1) DEFAULT 0,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `age` int(11) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `is_kids_profile` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`profile_id`),
  KEY `idx_app_user_id` (`app_user_id`),
  KEY `idx_user_profile_active` (`app_user_id`,`is_active`),
  KEY `idx_profile_age` (`age`,`is_kids_profile`),
  KEY `idx_profile_kids` (`is_kids_profile`),
  CONSTRAINT `fk_profile_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for app_user_rating
-- ----------------------------
DROP TABLE IF EXISTS `app_user_rating`;
CREATE TABLE `app_user_rating` (
  `app_user_id` int(10) unsigned NOT NULL,
  `content_id` int(11) NOT NULL,
  `rating` float NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`app_user_id`,`content_id`),
  KEY `idx_content_rating` (`content_id`),
  CONSTRAINT `app_user_rating_ibfk_1` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE,
  CONSTRAINT `app_user_rating_ibfk_2` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_user_rating` CHECK (`rating` >= 0 and `rating` <= 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_user_rating_backup
-- ----------------------------
DROP TABLE IF EXISTS `app_user_rating_backup`;
CREATE TABLE `app_user_rating_backup` (
  `app_user_id` int(10) unsigned NOT NULL,
  `content_id` int(11) NOT NULL,
  `rating` float NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_user_watch_history_backup
-- ----------------------------
DROP TABLE IF EXISTS `app_user_watch_history_backup`;
CREATE TABLE `app_user_watch_history_backup` (
  `watch_history_id` int(11) NOT NULL DEFAULT 0,
  `app_user_id` int(10) unsigned NOT NULL,
  `content_id` int(11) DEFAULT NULL,
  `episode_id` int(11) DEFAULT NULL,
  `last_watched_position` int(11) DEFAULT 0 COMMENT 'Position in seconds',
  `total_duration` int(11) DEFAULT 0 COMMENT 'Total duration in seconds',
  `completed` tinyint(1) DEFAULT 0,
  `device_type` tinyint(1) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for app_user_watchlist
-- ----------------------------
DROP TABLE IF EXISTS `app_user_watchlist`;
CREATE TABLE `app_user_watchlist` (
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  UNIQUE KEY `unique_profile_content` (`profile_id`,`content_id`),
  KEY `profile_id` (`profile_id`),
  KEY `content_id` (`content_id`),
  CONSTRAINT `app_user_watchlist_content_fk` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `app_user_watchlist_profile_fk` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `category_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_title` (`title`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for cms_page
-- ----------------------------
DROP TABLE IF EXISTS `cms_page`;
CREATE TABLE `cms_page` (
  `cms_page_id` int(11) NOT NULL AUTO_INCREMENT,
  `privacy` mediumtext DEFAULT NULL,
  `termsofuse` mediumtext DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`cms_page_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for content
-- ----------------------------
DROP TABLE IF EXISTS `content`;
CREATE TABLE `content` (
  `content_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '1 = movie / 2 = series',
  `duration` int(11) DEFAULT NULL COMMENT 'Duration in seconds',
  `duration_backup` int(11) DEFAULT NULL,
  `release_year` int(11) NOT NULL,
  `ratings` float NOT NULL DEFAULT 0,
  `language_id` int(11) NOT NULL,
  `vertical_poster` varchar(255) DEFAULT NULL,
  `horizontal_poster` varchar(255) DEFAULT NULL,
  `genre_ids` varchar(255) NOT NULL,
  `is_featured` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 = unfeatured / 1 = featured',
  `is_show` tinyint(1) NOT NULL DEFAULT 1 COMMENT '0 = Hide content / 1 = Show Content',
  `total_view` int(11) NOT NULL DEFAULT 0,
  `total_download` int(11) NOT NULL DEFAULT 0,
  `total_share` int(11) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  `content_distributor_id` int(11) DEFAULT NULL COMMENT 'NULL = legacy content, otherwise links to content_distributor',
  PRIMARY KEY (`content_id`),
  KEY `idx_content_type_featured_show` (`type`,`is_featured`,`is_show`),
  KEY `idx_content_language` (`language_id`),
  KEY `idx_content_release_year` (`release_year`),
  KEY `idx_content_genre_ids` (`genre_ids`),
  KEY `idx_content_featured_type_show` (`is_featured`,`type`,`is_show`,`updated_at` DESC),
  KEY `idx_content_language_show` (`language_id`,`is_show`,`updated_at` DESC),
  KEY `idx_content_genre_show` (`genre_ids`,`is_show`),
  KEY `idx_content_popular` (`total_view` DESC,`is_show`,`type`),
  KEY `idx_content_recent` (`release_year` DESC,`is_show`,`created_at` DESC),
  KEY `idx_content_stats` (`type`,`created_at`,`total_view`,`total_download`),
  KEY `idx_content_distributor` (`content_distributor_id`),
  KEY `idx_content_search2` (`is_show`,`type`,`release_year` DESC,`total_view` DESC),
  FULLTEXT KEY `idx_content_search` (`title`,`description`),
  CONSTRAINT `fk_content_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_content_language` FOREIGN KEY (`language_id`) REFERENCES `app_language` (`app_language_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for content_age_limit
-- ----------------------------
DROP TABLE IF EXISTS `content_age_limit`;
CREATE TABLE `content_age_limit` (
  `content_age_limit_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` int(11) NOT NULL,
  `age_limit_id` int(11) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`content_age_limit_id`),
  UNIQUE KEY `uk_content_age` (`content_id`,`age_limit_id`),
  KEY `idx_age_limit` (`age_limit_id`),
  KEY `idx_content` (`content_id`),
  CONSTRAINT `content_age_limit_ibfk_1` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `content_age_limit_ibfk_2` FOREIGN KEY (`age_limit_id`) REFERENCES `age_limit` (`age_limit_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for content_age_limit_backup
-- ----------------------------
DROP TABLE IF EXISTS `content_age_limit_backup`;
CREATE TABLE `content_age_limit_backup` (
  `content_age_limit_id` int(11) NOT NULL DEFAULT 0,
  `content_id` int(11) NOT NULL,
  `age_limit_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for content_audio_tracks
-- ----------------------------
DROP TABLE IF EXISTS `content_audio_tracks`;
CREATE TABLE `content_audio_tracks` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `content_id` int(11) NOT NULL,
  `content_source_id` int(11) DEFAULT NULL COMMENT 'Optional: link to specific source if audio is source-specific',
  `language_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL COMMENT 'Display name like "English", "Spanish", "French"',
  `language_code` varchar(10) NOT NULL COMMENT 'ISO language code like en, es, fr',
  `audio_url` text NOT NULL COMMENT 'URL to the audio track file',
  `audio_format` varchar(50) DEFAULT 'AAC' COMMENT 'Audio format: AAC, MP3, AC3, DTS, etc',
  `audio_channels` varchar(20) DEFAULT 'Stereo' COMMENT 'Stereo, 5.1, 7.1, etc',
  `bitrate` varchar(20) DEFAULT NULL COMMENT 'Audio bitrate like 128kbps, 320kbps',
  `is_default` tinyint(1) DEFAULT 0 COMMENT 'Is this the default audio track?',
  `is_original` tinyint(1) DEFAULT 0 COMMENT 'Is this the original language track?',
  `sort_order` int(11) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_content_audio_content` (`content_id`),
  KEY `idx_content_audio_source` (`content_source_id`),
  KEY `idx_content_audio_language` (`language_id`),
  KEY `idx_audio_default` (`content_id`,`is_default`),
  CONSTRAINT `fk_audio_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_audio_language` FOREIGN KEY (`language_id`) REFERENCES `app_language` (`app_language_id`),
  CONSTRAINT `fk_audio_source` FOREIGN KEY (`content_source_id`) REFERENCES `content_source` (`content_source_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for content_cast
-- ----------------------------
DROP TABLE IF EXISTS `content_cast`;
CREATE TABLE `content_cast` (
  `content_cast_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` int(11) NOT NULL,
  `actor_id` int(11) NOT NULL,
  `character_name` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`content_cast_id`),
  KEY `fk_content_cast_content` (`content_id`),
  KEY `idx_content_cast_actor` (`actor_id`,`content_id`),
  CONSTRAINT `fk_content_cast_actor` FOREIGN KEY (`actor_id`) REFERENCES `actor` (`actor_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_content_cast_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for content_category
-- ----------------------------
DROP TABLE IF EXISTS `content_category`;
CREATE TABLE `content_category` (
  `content_id` int(11) NOT NULL,
  `category_id` int(11) NOT NULL,
  PRIMARY KEY (`content_id`,`category_id`),
  KEY `idx_genre_content` (`category_id`,`content_id`),
  CONSTRAINT `content_category_ibfk_1` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `content_category_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for content_distributor
-- ----------------------------
DROP TABLE IF EXISTS `content_distributor`;
CREATE TABLE `content_distributor` (
  `content_distributor_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `code` varchar(50) NOT NULL,
  `description` text DEFAULT NULL,
  `logo_url` varchar(500) DEFAULT NULL,
  `is_base_included` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1 = Included in base subscription',
  `is_premium` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 = Premium content, 0 = Base content',
  `display_order` int(11) DEFAULT 0,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`content_distributor_id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_code` (`code`),
  KEY `idx_active_premium` (`is_active`,`is_premium`),
  KEY `idx_content_distributor_active` (`is_active`,`is_premium`,`is_base_included`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for content_source
-- ----------------------------
DROP TABLE IF EXISTS `content_source`;
CREATE TABLE `content_source` (
  `content_source_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `quality` varchar(255) NOT NULL,
  `size` varchar(255) DEFAULT NULL,
  `is_download` int(11) NOT NULL DEFAULT 0 COMMENT '0 = no/ 1 = yes',
  `access_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1 = Free / 2 = Paid/ 3 = Unlock With Video Ads',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '1 for Youtube URL, 2 for M3u8 Url, 3 for Mov Url, 4 for Mp4 Url, 5 for Mkv Url, 6 for Webm Url, 7 for File Upload (Mp4, Mov, Mp4, Mkv, Webm)',
  `source` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`content_source_id`),
  KEY `idx_content_source_content` (`content_id`),
  KEY `idx_content_source_access_type` (`access_type`),
  KEY `idx_content_source_download` (`content_id`,`is_download`,`quality`),
  KEY `idx_content_source_access` (`access_type`,`content_id`),
  CONSTRAINT `fk_content_source_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for content_subtitle_tracks
-- ----------------------------
DROP TABLE IF EXISTS `content_subtitle_tracks`;
CREATE TABLE `content_subtitle_tracks` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `content_id` int(11) NOT NULL,
  `content_source_id` int(11) DEFAULT NULL COMMENT 'Optional: link to specific source if subtitle is source-specific',
  `language_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL COMMENT 'Display name like "English", "Spanish (Latin America)", "French"',
  `language_code` varchar(10) NOT NULL COMMENT 'ISO language code like en, es-MX, fr',
  `subtitle_url` text NOT NULL COMMENT 'URL to the SRT/VTT subtitle file',
  `subtitle_format` varchar(20) DEFAULT 'SRT' COMMENT 'Format: SRT, VTT, ASS, etc',
  `subtitle_type` varchar(50) DEFAULT 'dialogue' COMMENT 'Type: dialogue, commentary, forced, SDH',
  `is_default` tinyint(1) DEFAULT 0 COMMENT 'Is this the default subtitle track?',
  `is_forced` tinyint(1) DEFAULT 0 COMMENT 'Forced subtitles for foreign language parts',
  `is_sdh` tinyint(1) DEFAULT 0 COMMENT 'Subtitles for Deaf and Hard of Hearing',
  `sort_order` int(11) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_content_subtitle_content` (`content_id`),
  KEY `idx_content_subtitle_source` (`content_source_id`),
  KEY `idx_content_subtitle_language` (`language_id`),
  KEY `idx_subtitle_default` (`content_id`,`is_default`),
  CONSTRAINT `fk_content_subtitle_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_content_subtitle_language` FOREIGN KEY (`language_id`) REFERENCES `app_language` (`app_language_id`),
  CONSTRAINT `fk_content_subtitle_source` FOREIGN KEY (`content_source_id`) REFERENCES `content_source` (`content_source_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for content_trailer
-- ----------------------------
DROP TABLE IF EXISTS `content_trailer`;
CREATE TABLE `content_trailer` (
  `content_trailer_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` int(11) NOT NULL,
  `title` varchar(255) DEFAULT NULL COMMENT 'Trailer title/description',
  `youtube_id` varchar(20) DEFAULT NULL,
  `trailer_url` varchar(500) NOT NULL COMMENT 'Full YouTube URL for backward compatibility',
  `is_primary` tinyint(1) DEFAULT 0 COMMENT '1 = Primary trailer, 0 = Additional trailer',
  `sort_order` int(11) DEFAULT 0 COMMENT 'Display order',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`content_trailer_id`),
  UNIQUE KEY `idx_content_trailer_unique` (`content_id`,`youtube_id`),
  KEY `idx_content_trailer_content` (`content_id`),
  KEY `idx_content_trailer_primary` (`content_id`,`is_primary`),
  KEY `idx_content_trailer_sort` (`content_id`,`sort_order`),
  KEY `idx_content_trailer_youtube_id` (`youtube_id`),
  CONSTRAINT `fk_content_trailer_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for content_trailer_url_backup
-- ----------------------------
DROP TABLE IF EXISTS `content_trailer_url_backup`;
CREATE TABLE `content_trailer_url_backup` (
  `content_id` int(11) NOT NULL DEFAULT 0,
  `trailer_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for custom_ad
-- ----------------------------
DROP TABLE IF EXISTS `custom_ad`;
CREATE TABLE `custom_ad` (
  `custom_ad_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `brand_name` varchar(255) DEFAULT NULL,
  `brand_logo` varchar(555) DEFAULT NULL,
  `button_text` varchar(255) DEFAULT NULL,
  `is_android` int(11) DEFAULT NULL COMMENT '1=yes 0=no',
  `android_link` varchar(555) DEFAULT NULL,
  `is_ios` tinyint(1) DEFAULT NULL COMMENT '1=yes 0=no',
  `ios_link` varchar(555) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0=off 1=on',
  `views` int(11) NOT NULL DEFAULT 0,
  `clicks` int(11) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`custom_ad_id`),
  KEY `idx_custom_ad_dates` (`start_date`,`end_date`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for custom_ad_source
-- ----------------------------
DROP TABLE IF EXISTS `custom_ad_source`;
CREATE TABLE `custom_ad_source` (
  `custom_ad_source_id` int(11) NOT NULL AUTO_INCREMENT,
  `custom_ad_id` int(11) NOT NULL,
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '0 = Image / 1 = Video',
  `content` varchar(255) NOT NULL,
  `headline` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `show_time` int(11) DEFAULT NULL,
  `is_skippable` int(11) DEFAULT NULL COMMENT '0 = Must Watch / 1 = Skippable',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`custom_ad_source_id`),
  KEY `fk_custom_ad_source_custom_ad` (`custom_ad_id`),
  CONSTRAINT `fk_custom_ad_source_custom_ad` FOREIGN KEY (`custom_ad_id`) REFERENCES `custom_ad` (`custom_ad_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for default_avatar
-- ----------------------------
DROP TABLE IF EXISTS `default_avatar`;
CREATE TABLE `default_avatar` (
  `avatar_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `color` varchar(7) DEFAULT NULL COMMENT 'Hex color code',
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`avatar_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for episode
-- ----------------------------
DROP TABLE IF EXISTS `episode`;
CREATE TABLE `episode` (
  `episode_id` int(11) NOT NULL AUTO_INCREMENT,
  `season_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `thumbnail` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `description` varchar(900) NOT NULL,
  `duration` int(11) DEFAULT NULL COMMENT 'Duration in seconds',
  `ratings` float NOT NULL DEFAULT 0,
  `total_view` int(11) DEFAULT 0,
  `total_download` int(11) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`episode_id`),
  KEY `idx_episode_season` (`season_id`),
  KEY `idx_episode_season_number` (`season_id`,`number`),
  KEY `idx_episode_ratings` (`ratings` DESC),
  CONSTRAINT `fk_episode_season` FOREIGN KEY (`season_id`) REFERENCES `season` (`season_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for episode_audio_tracks
-- ----------------------------
DROP TABLE IF EXISTS `episode_audio_tracks`;
CREATE TABLE `episode_audio_tracks` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `episode_id` int(11) NOT NULL,
  `episode_source_id` int(11) DEFAULT NULL COMMENT 'Optional: link to specific source',
  `language_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `language_code` varchar(10) NOT NULL,
  `audio_url` text NOT NULL,
  `audio_format` varchar(50) DEFAULT 'AAC',
  `audio_channels` varchar(20) DEFAULT 'Stereo',
  `bitrate` varchar(20) DEFAULT NULL,
  `is_default` tinyint(1) DEFAULT 0,
  `is_original` tinyint(1) DEFAULT 0,
  `sort_order` int(11) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_episode_audio_episode` (`episode_id`),
  KEY `idx_episode_audio_source` (`episode_source_id`),
  KEY `idx_episode_audio_language` (`language_id`),
  KEY `idx_ep_audio_default` (`episode_id`,`is_default`),
  CONSTRAINT `fk_episode_audio_episode` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_episode_audio_language` FOREIGN KEY (`language_id`) REFERENCES `app_language` (`app_language_id`),
  CONSTRAINT `fk_episode_audio_source` FOREIGN KEY (`episode_source_id`) REFERENCES `episode_source` (`episode_source_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for episode_source
-- ----------------------------
DROP TABLE IF EXISTS `episode_source`;
CREATE TABLE `episode_source` (
  `episode_source_id` int(11) NOT NULL AUTO_INCREMENT,
  `episode_id` int(11) NOT NULL,
  `title` varchar(50) NOT NULL,
  `quality` varchar(50) NOT NULL,
  `size` varchar(30) DEFAULT NULL,
  `is_download` int(11) NOT NULL DEFAULT 0 COMMENT '0 = no / 1 = yes',
  `access_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1 = Free/ 2 = Paid / 3 = Unlock With Video Ads',
  `type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 for Youtube URL, 2 for M3u8 Url, 3 for Mov Url, 4 for Mp4 Url, 5 for Mkv Url, 6 for Webm Url, 7 for File Upload (Mp4, Mov, Mp4, Mkv, Webm)',
  `source` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`episode_source_id`),
  KEY `idx_episode_source_episode` (`episode_id`),
  KEY `idx_episode_source_download` (`episode_id`,`is_download`,`quality`),
  KEY `idx_episode_source_access` (`access_type`,`episode_id`),
  CONSTRAINT `fk_episode_source_episode` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for episode_subtitle
-- ----------------------------
DROP TABLE IF EXISTS `episode_subtitle`;
CREATE TABLE `episode_subtitle` (
  `episode_subtitle_id` int(11) NOT NULL AUTO_INCREMENT,
  `episode_id` int(11) NOT NULL,
  `language_id` int(11) NOT NULL,
  `file` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`episode_subtitle_id`),
  KEY `fk_episode_subtitle_episode` (`episode_id`),
  KEY `fk_episode_subtitle_language` (`language_id`),
  CONSTRAINT `fk_episode_subtitle_episode` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_episode_subtitle_language` FOREIGN KEY (`language_id`) REFERENCES `app_language` (`app_language_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for episode_subtitle_tracks
-- ----------------------------
DROP TABLE IF EXISTS `episode_subtitle_tracks`;
CREATE TABLE `episode_subtitle_tracks` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `episode_id` int(11) NOT NULL,
  `episode_source_id` int(11) DEFAULT NULL COMMENT 'Optional: link to specific source',
  `language_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `language_code` varchar(10) NOT NULL,
  `subtitle_url` text NOT NULL,
  `subtitle_format` varchar(20) DEFAULT 'SRT',
  `subtitle_type` varchar(50) DEFAULT 'dialogue',
  `is_default` tinyint(1) DEFAULT 0,
  `is_forced` tinyint(1) DEFAULT 0,
  `is_sdh` tinyint(1) DEFAULT 0,
  `sort_order` int(11) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_episode_subtitle_episode` (`episode_id`),
  KEY `idx_episode_subtitle_source` (`episode_source_id`),
  KEY `idx_episode_subtitle_language` (`language_id`),
  KEY `idx_ep_subtitle_default` (`episode_id`,`is_default`),
  CONSTRAINT `fk_ep_subtitle_episode` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ep_subtitle_language` FOREIGN KEY (`language_id`) REFERENCES `app_language` (`app_language_id`),
  CONSTRAINT `fk_ep_subtitle_source` FOREIGN KEY (`episode_source_id`) REFERENCES `episode_source` (`episode_source_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for failed_job
-- ----------------------------
DROP TABLE IF EXISTS `failed_job`;
CREATE TABLE `failed_job` (
  `failed_job_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `connection` text NOT NULL,
  `queue` text NOT NULL,
  `payload` longtext NOT NULL,
  `exception` longtext NOT NULL,
  `failed_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`failed_job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for global_setting
-- ----------------------------
DROP TABLE IF EXISTS `global_setting`;
CREATE TABLE `global_setting` (
  `global_setting_id` int(11) NOT NULL AUTO_INCREMENT,
  `app_name` varchar(55) DEFAULT NULL,
  `is_live_tv_enable` tinyint(1) NOT NULL,
  `is_admob_android` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=on 0=off',
  `is_admob_ios` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=on 0=off',
  `is_custom_android` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=on 0=off',
  `is_custom_ios` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=on 0=off',
  `videoad_skip_time` int(11) NOT NULL DEFAULT 8,
  `storage_type` int(11) NOT NULL DEFAULT 0 COMMENT '0 = Local / 1 = AWS S3 / 2 = DigitaoOcean Space',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`global_setting_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for media_gallery
-- ----------------------------
DROP TABLE IF EXISTS `media_gallery`;
CREATE TABLE `media_gallery` (
  `media_gallery_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(55) NOT NULL,
  `file` varchar(555) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`media_gallery_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `notification_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` varchar(900) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`notification_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for notification_analytics
-- ----------------------------
DROP TABLE IF EXISTS `notification_analytics`;
CREATE TABLE `notification_analytics` (
  `notification_id` bigint(20) NOT NULL,
  `total_eligible_profiles` int(11) DEFAULT 0,
  `total_shown` int(11) DEFAULT 0,
  `total_dismissed` int(11) DEFAULT 0,
  `ios_shown` int(11) DEFAULT 0,
  `android_shown` int(11) DEFAULT 0,
  `android_tv_shown` int(11) DEFAULT 0,
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`notification_id`),
  CONSTRAINT `fk_analytics_notification` FOREIGN KEY (`notification_id`) REFERENCES `user_notification` (`notification_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for payment_transaction
-- ----------------------------
DROP TABLE IF EXISTS `payment_transaction`;
CREATE TABLE `payment_transaction` (
  `transaction_id` int(11) NOT NULL AUTO_INCREMENT,
  `app_user_id` int(10) unsigned NOT NULL,
  `transaction_type` enum('subscription','renewal','upgrade','refund') NOT NULL,
  `subscription_type` enum('base','distributor') NOT NULL,
  `content_distributor_id` int(11) DEFAULT NULL,
  `pricing_id` int(11) DEFAULT NULL,
  `promo_code_id` int(11) DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT NULL COMMENT 'stripe, paypal, apple_pay, google_pay, etc',
  `payment_status` enum('pending','completed','failed','refunded','cancelled') NOT NULL DEFAULT 'pending',
  `currency` varchar(3) NOT NULL DEFAULT 'USD',
  `subtotal` decimal(10,2) NOT NULL,
  `discount_amount` decimal(10,2) DEFAULT 0.00,
  `tax_amount` decimal(10,2) DEFAULT 0.00,
  `total_amount` decimal(10,2) NOT NULL,
  `payment_gateway_id` varchar(255) DEFAULT NULL COMMENT 'External payment provider transaction ID',
  `payment_gateway_response` text DEFAULT NULL COMMENT 'JSON response from payment provider',
  `billing_period` enum('daily','weekly','monthly','quarterly','yearly','lifetime') DEFAULT NULL,
  `subscription_start_date` timestamp NULL DEFAULT NULL,
  `subscription_end_date` timestamp NULL DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`transaction_id`),
  KEY `idx_user_transactions` (`app_user_id`,`created_at` DESC),
  KEY `idx_payment_status` (`payment_status`,`created_at` DESC),
  KEY `idx_distributor_trans` (`content_distributor_id`),
  KEY `idx_promo_usage` (`promo_code_id`),
  KEY `idx_pricing_trans` (`pricing_id`),
  CONSTRAINT `fk_trans_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_trans_pricing` FOREIGN KEY (`pricing_id`) REFERENCES `subscription_pricing` (`pricing_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_trans_promo` FOREIGN KEY (`promo_code_id`) REFERENCES `promo_code` (`promo_code_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_trans_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for profile_download
-- ----------------------------
DROP TABLE IF EXISTS `profile_download`;
CREATE TABLE `profile_download` (
  `download_id` int(11) NOT NULL AUTO_INCREMENT,
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `episode_id` int(11) DEFAULT NULL,
  `source_id` int(11) NOT NULL,
  `download_path` varchar(500) DEFAULT NULL,
  `file_size` bigint(20) DEFAULT NULL COMMENT 'Size in bytes',
  `status` enum('pending','downloading','completed','failed','deleted') DEFAULT 'pending',
  `progress` int(3) DEFAULT 0 COMMENT 'Download progress percentage',
  `started_at` timestamp NULL DEFAULT NULL,
  `completed_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`download_id`),
  UNIQUE KEY `unique_profile_content_episode` (`profile_id`,`content_id`,`episode_id`),
  KEY `idx_profile_id` (`profile_id`),
  KEY `idx_status` (`status`),
  KEY `fk_download_content` (`content_id`),
  KEY `fk_download_episode` (`episode_id`),
  KEY `idx_profile_downloads_status` (`profile_id`,`status`),
  CONSTRAINT `fk_download_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_download_episode` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_download_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for profile_favorite
-- ----------------------------
DROP TABLE IF EXISTS `profile_favorite`;
CREATE TABLE `profile_favorite` (
  `favorite_id` int(11) NOT NULL AUTO_INCREMENT,
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`favorite_id`),
  UNIQUE KEY `unique_profile_content` (`profile_id`,`content_id`),
  KEY `idx_profile_id` (`profile_id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_profile_favorites_recent` (`profile_id`,`added_at` DESC),
  CONSTRAINT `fk_favorite_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorite_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for profile_notification_status
-- ----------------------------
DROP TABLE IF EXISTS `profile_notification_status`;
CREATE TABLE `profile_notification_status` (
  `profile_id` int(11) NOT NULL,
  `notification_id` bigint(20) NOT NULL,
  `shown_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `dismissed_at` timestamp NULL DEFAULT NULL,
  `platform` varchar(20) NOT NULL COMMENT 'ios, android, android_tv',
  PRIMARY KEY (`profile_id`,`notification_id`),
  KEY `idx_notification_id` (`notification_id`),
  KEY `idx_shown_at` (`shown_at`),
  KEY `idx_profile_notification_status_composite` (`notification_id`,`profile_id`,`shown_at`),
  CONSTRAINT `fk_profile_notification_notification` FOREIGN KEY (`notification_id`) REFERENCES `user_notification` (`notification_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_profile_notification_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for profile_watch_history
-- ----------------------------
DROP TABLE IF EXISTS `profile_watch_history`;
CREATE TABLE `profile_watch_history` (
  `history_id` int(11) NOT NULL AUTO_INCREMENT,
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `episode_id` int(11) DEFAULT NULL,
  `last_position` int(11) DEFAULT 0 COMMENT 'Position in seconds',
  `duration` int(11) DEFAULT 0 COMMENT 'Total duration in seconds',
  `completed` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`history_id`),
  UNIQUE KEY `unique_profile_content_episode` (`profile_id`,`content_id`,`episode_id`),
  KEY `idx_profile_id` (`profile_id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_last_watched` (`updated_at`),
  KEY `fk_history_episode` (`episode_id`),
  KEY `idx_profile_history_recent` (`profile_id`,`updated_at` DESC,`completed` DESC) USING BTREE,
  CONSTRAINT `fk_history_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_history_episode` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_history_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for profile_watchlist
-- ----------------------------
DROP TABLE IF EXISTS `profile_watchlist`;
CREATE TABLE `profile_watchlist` (
  `watchlist_id` int(11) NOT NULL AUTO_INCREMENT,
  `profile_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`watchlist_id`),
  UNIQUE KEY `unique_profile_content` (`profile_id`,`content_id`),
  KEY `idx_profile_id` (`profile_id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_profile_watchlist_recent` (`profile_id`,`added_at` DESC),
  CONSTRAINT `fk_watchlist_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_watchlist_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for promo_code
-- ----------------------------
DROP TABLE IF EXISTS `promo_code`;
CREATE TABLE `promo_code` (
  `promo_code_id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount_type` enum('percentage','fixed_amount','free_period') NOT NULL DEFAULT 'percentage',
  `discount_value` decimal(10,2) NOT NULL COMMENT 'Percentage (0-100), fixed amount, or days for free period',
  `applicable_to` enum('base','distributor','all') NOT NULL DEFAULT 'all',
  `content_distributor_id` int(11) DEFAULT NULL COMMENT 'Specific distributor if applicable',
  `minimum_purchase` decimal(10,2) DEFAULT 0.00,
  `usage_limit` int(11) DEFAULT NULL COMMENT 'NULL = unlimited',
  `usage_count` int(11) DEFAULT 0,
  `user_limit` int(11) DEFAULT 1 COMMENT 'Times a single user can use this code',
  `valid_from` timestamp NOT NULL DEFAULT current_timestamp(),
  `valid_until` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_by` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`promo_code_id`),
  UNIQUE KEY `code` (`code`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_code_active` (`code`,`is_active`),
  KEY `idx_valid_dates` (`valid_from`,`valid_until`),
  KEY `idx_distributor_promo` (`content_distributor_id`),
  KEY `fk_promo_admin` (`created_by`),
  CONSTRAINT `fk_promo_admin` FOREIGN KEY (`created_by`) REFERENCES `admin_user` (`admin_user_id`),
  CONSTRAINT `fk_promo_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for promo_code_usage
-- ----------------------------
DROP TABLE IF EXISTS `promo_code_usage`;
CREATE TABLE `promo_code_usage` (
  `usage_id` int(11) NOT NULL AUTO_INCREMENT,
  `promo_code_id` int(11) NOT NULL,
  `app_user_id` int(10) unsigned NOT NULL,
  `transaction_id` int(11) DEFAULT NULL,
  `used_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`usage_id`),
  UNIQUE KEY `uk_user_promo_trans` (`app_user_id`,`promo_code_id`,`transaction_id`),
  KEY `idx_promo_usage` (`promo_code_id`),
  KEY `idx_user_promos` (`app_user_id`),
  KEY `fk_usage_trans` (`transaction_id`),
  CONSTRAINT `fk_usage_promo` FOREIGN KEY (`promo_code_id`) REFERENCES `promo_code` (`promo_code_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usage_trans` FOREIGN KEY (`transaction_id`) REFERENCES `payment_transaction` (`transaction_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_usage_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for revenue_share_config
-- ----------------------------
DROP TABLE IF EXISTS `revenue_share_config`;
CREATE TABLE `revenue_share_config` (
  `revenue_share_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_distributor_id` int(11) NOT NULL,
  `revenue_share_percentage` decimal(5,2) NOT NULL COMMENT 'Percentage to distributor (0-100)',
  `minimum_payout` decimal(10,2) DEFAULT 0.00 COMMENT 'Minimum amount before payout',
  `payment_terms_days` int(11) DEFAULT 30 COMMENT 'Days after period end for payment',
  `is_active` tinyint(1) DEFAULT 1,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`revenue_share_id`),
  UNIQUE KEY `uk_distributor_revshare` (`content_distributor_id`),
  CONSTRAINT `fk_revshare_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for revenue_share_payout
-- ----------------------------
DROP TABLE IF EXISTS `revenue_share_payout`;
CREATE TABLE `revenue_share_payout` (
  `payout_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_distributor_id` int(11) NOT NULL,
  `period_start` date NOT NULL,
  `period_end` date NOT NULL,
  `total_revenue` decimal(10,2) NOT NULL,
  `share_percentage` decimal(5,2) NOT NULL,
  `payout_amount` decimal(10,2) NOT NULL,
  `payout_status` enum('pending','processing','completed','failed') DEFAULT 'pending',
  `payout_date` timestamp NULL DEFAULT NULL,
  `payment_reference` varchar(255) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`payout_id`),
  KEY `idx_distributor_payouts` (`content_distributor_id`,`period_start`),
  KEY `idx_payout_status` (`payout_status`),
  CONSTRAINT `fk_payout_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for schema_migration
-- ----------------------------
DROP TABLE IF EXISTS `schema_migration`;
CREATE TABLE `schema_migration` (
  `schema_migration_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `migration` varchar(191) NOT NULL,
  `batch` int(11) NOT NULL,
  PRIMARY KEY (`schema_migration_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for season
-- ----------------------------
DROP TABLE IF EXISTS `season`;
CREATE TABLE `season` (
  `season_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` int(11) NOT NULL,
  `title` varchar(100) NOT NULL,
  `trailer_url` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`season_id`),
  KEY `idx_season_content` (`content_id`),
  KEY `idx_season_content_number` (`content_id`,`title`),
  CONSTRAINT `fk_season_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for subscription_pricing
-- ----------------------------
DROP TABLE IF EXISTS `subscription_pricing`;
CREATE TABLE `subscription_pricing` (
  `pricing_id` int(11) NOT NULL AUTO_INCREMENT,
  `pricing_type` enum('base','distributor') NOT NULL DEFAULT 'base',
  `content_distributor_id` int(11) DEFAULT NULL COMMENT 'NULL for base subscription, otherwise specific distributor',
  `billing_period` enum('daily','weekly','monthly','quarterly','yearly','lifetime') NOT NULL DEFAULT 'monthly',
  `price` decimal(10,2) NOT NULL,
  `currency` varchar(3) NOT NULL DEFAULT 'USD',
  `display_name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `sort_order` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`pricing_id`),
  KEY `idx_pricing_type_active` (`pricing_type`,`is_active`),
  KEY `idx_distributor_pricing` (`content_distributor_id`),
  CONSTRAINT `fk_pricing_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for subtitle
-- ----------------------------
DROP TABLE IF EXISTS `subtitle`;
CREATE TABLE `subtitle` (
  `subtitle_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` int(11) NOT NULL,
  `language_id` int(11) NOT NULL,
  `file` varchar(555) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`subtitle_id`),
  KEY `fk_subtitle_content` (`content_id`),
  KEY `fk_subtitle_language` (`language_id`),
  CONSTRAINT `fk_subtitle_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_subtitle_language` FOREIGN KEY (`language_id`) REFERENCES `app_language` (`app_language_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for top_content
-- ----------------------------
DROP TABLE IF EXISTS `top_content`;
CREATE TABLE `top_content` (
  `top_content_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_index` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`top_content_id`),
  KEY `fk_top_content_content` (`content_id`),
  CONSTRAINT `fk_top_content_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for tv_auth_session
-- ----------------------------
DROP TABLE IF EXISTS `tv_auth_session`;
CREATE TABLE `tv_auth_session` (
  `tv_auth_session_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `session_token` varchar(64) NOT NULL,
  `qr_code` varchar(255) NOT NULL,
  `app_user_id` int(10) unsigned DEFAULT NULL,
  `tv_device_id` varchar(255) NOT NULL,
  `status` enum('pending','authenticated','expired') DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `expires_at` timestamp NULL DEFAULT (current_timestamp() + interval 5 minute),
  `authenticated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`tv_auth_session_id`),
  UNIQUE KEY `session_token` (`session_token`),
  KEY `idx_session_token` (`session_token`),
  KEY `idx_status_expires` (`status`,`expires_at`),
  KEY `fk_tv_auth_session_app_user` (`app_user_id`),
  CONSTRAINT `fk_tv_auth_session_app_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for tv_category
-- ----------------------------
DROP TABLE IF EXISTS `tv_category`;
CREATE TABLE `tv_category` (
  `tv_category_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`tv_category_id`),
  UNIQUE KEY `uk_title` (`title`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for tv_channel
-- ----------------------------
DROP TABLE IF EXISTS `tv_channel`;
CREATE TABLE `tv_channel` (
  `tv_channel_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `thumbnail` varchar(255) DEFAULT NULL,
  `access_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1 = Free / 2 = Paid / 3 = Unlock With Video Ads',
  `category_ids` text DEFAULT NULL,
  `type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 = Youtube URL, 2 = M3u8 Url',
  `source` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`tv_channel_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for tv_channel_category
-- ----------------------------
DROP TABLE IF EXISTS `tv_channel_category`;
CREATE TABLE `tv_channel_category` (
  `tv_channel_id` int(11) NOT NULL,
  `tv_category_id` int(11) NOT NULL,
  PRIMARY KEY (`tv_channel_id`,`tv_category_id`),
  KEY `tv_category_id` (`tv_category_id`),
  CONSTRAINT `tv_channel_category_ibfk_1` FOREIGN KEY (`tv_channel_id`) REFERENCES `tv_channel` (`tv_channel_id`) ON DELETE CASCADE,
  CONSTRAINT `tv_channel_category_ibfk_2` FOREIGN KEY (`tv_category_id`) REFERENCES `tv_category` (`tv_category_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for user_base_subscription
-- ----------------------------
DROP TABLE IF EXISTS `user_base_subscription`;
CREATE TABLE `user_base_subscription` (
  `subscription_id` int(11) NOT NULL AUTO_INCREMENT,
  `app_user_id` int(10) unsigned NOT NULL,
  `start_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `end_date` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `subscription_type` varchar(50) DEFAULT 'monthly' COMMENT 'monthly, yearly, etc',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`subscription_id`),
  KEY `idx_user_active` (`app_user_id`,`is_active`),
  KEY `idx_end_date` (`end_date`),
  CONSTRAINT `fk_base_sub_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user_distributor_access
-- ----------------------------
DROP TABLE IF EXISTS `user_distributor_access`;
CREATE TABLE `user_distributor_access` (
  `access_id` int(11) NOT NULL AUTO_INCREMENT,
  `app_user_id` int(10) unsigned NOT NULL,
  `content_distributor_id` int(11) NOT NULL,
  `start_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `end_date` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `subscription_type` varchar(50) DEFAULT 'monthly',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`access_id`),
  UNIQUE KEY `uk_user_distributor` (`app_user_id`,`content_distributor_id`),
  KEY `idx_user_active_dist` (`app_user_id`,`is_active`),
  KEY `idx_distributor` (`content_distributor_id`),
  KEY `idx_end_date_dist` (`end_date`),
  CONSTRAINT `fk_dist_access_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_dist_access_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user_notification
-- ----------------------------
DROP TABLE IF EXISTS `user_notification`;
CREATE TABLE `user_notification` (
  `notification_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `notification_type` enum('system','promotional','update','maintenance') DEFAULT 'system',
  `target_platforms` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Array of platforms: ["ios", "android", "android_tv", "all"]' CHECK (json_valid(`target_platforms`)),
  `target_user_types` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Array of user types: ["all", "premium", "free"]' CHECK (json_valid(`target_user_types`)),
  `priority` enum('low','medium','high','urgent') DEFAULT 'medium',
  `scheduled_at` timestamp NULL DEFAULT NULL,
  `expires_at` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_by` int(11) NOT NULL COMMENT 'admin_user_id',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`notification_id`),
  KEY `idx_active_scheduled` (`is_active`,`scheduled_at`),
  KEY `idx_expires_at` (`expires_at`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_user_notification_active_scheduled` (`is_active`,`scheduled_at`),
  KEY `idx_user_notification_expires` (`expires_at`),
  CONSTRAINT `fk_notification_admin` FOREIGN KEY (`created_by`) REFERENCES `admin_user` (`admin_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- View structure for actors
-- ----------------------------
DROP VIEW IF EXISTS `actors`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `actors` AS select `actor`.`actor_id` AS `id`,`actor`.`fullname` AS `fullname`,`actor`.`dob` AS `dob`,`actor`.`bio` AS `bio`,`actor`.`profile_image` AS `profile_image`,`actor`.`created_at` AS `created_at`,`actor`.`updated_at` AS `updated_at` from `actor`;

-- ----------------------------
-- View structure for admob
-- ----------------------------
DROP VIEW IF EXISTS `admob`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `admob` AS select `admob_config`.`admob_config_id` AS `id`,`admob_config`.`banner_id` AS `banner_id`,`admob_config`.`interstitial_id` AS `intersial_id`,`admob_config`.`rewarded_id` AS `rewarded_id`,`admob_config`.`type` AS `type`,`admob_config`.`created_at` AS `created_at`,`admob_config`.`updated_at` AS `updated_at` from `admob_config`;

-- ----------------------------
-- View structure for app_profile_unified_watchlist
-- ----------------------------
DROP VIEW IF EXISTS `app_profile_unified_watchlist`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `app_profile_unified_watchlist` AS select `w`.`profile_id` AS `profile_id`,`w`.`content_id` AS `item_id`,`c`.`title` AS `title`,`c`.`vertical_poster` AS `poster`,`c`.`horizontal_poster` AS `thumbnail`,`c`.`type` AS `item_type`,'content' AS `source_type`,`w`.`created_at` AS `created_at` from (`app_user_watchlist` `w` join `content` `c` on(`w`.`content_id` = `c`.`content_id`)) union all select `ew`.`profile_id` AS `profile_id`,`ew`.`episode_id` AS `item_id`,concat(`s`.`title`,' - E',`e`.`number`,': ',`e`.`title`) AS `title`,`c`.`vertical_poster` AS `poster`,`e`.`thumbnail` AS `thumbnail`,3 AS `item_type`,'episode' AS `source_type`,`ew`.`created_at` AS `created_at` from (((`app_profile_episode_watchlist` `ew` join `episode` `e` on(`ew`.`episode_id` = `e`.`episode_id`)) join `season` `s` on(`e`.`season_id` = `s`.`season_id`)) join `content` `c` on(`s`.`content_id` = `c`.`content_id`)) order by `created_at` desc;

-- ----------------------------
-- View structure for contents
-- ----------------------------
DROP VIEW IF EXISTS `contents`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `contents` AS select `content`.`content_id` AS `id`,`content`.`title` AS `title`,`content`.`description` AS `description`,`content`.`type` AS `type`,case when `content`.`duration` is null then NULL else cast(`content`.`duration` as char charset utf8mb4) end AS `duration`,`content`.`release_year` AS `release_year`,`content`.`ratings` AS `ratings`,`content`.`language_id` AS `language_id`,`content`.`vertical_poster` AS `vertical_poster`,`content`.`horizontal_poster` AS `horizontal_poster`,`content`.`genre_ids` AS `genre_ids`,`content`.`is_featured` AS `is_featured`,`content`.`is_show` AS `is_show`,`content`.`total_view` AS `total_view`,`content`.`total_download` AS `total_download`,`content`.`total_share` AS `total_share`,`content`.`created_at` AS `created_at`,`content`.`updated_at` AS `updated_at` from `content`;

-- ----------------------------
-- View structure for content_sources
-- ----------------------------
DROP VIEW IF EXISTS `content_sources`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `content_sources` AS select `content_source`.`content_source_id` AS `id`,`content_source`.`content_id` AS `content_id`,`content_source`.`title` AS `title`,`content_source`.`quality` AS `quality`,`content_source`.`size` AS `size`,`content_source`.`is_download` AS `is_download`,`content_source`.`access_type` AS `access_type`,`content_source`.`type` AS `type`,`content_source`.`source` AS `source`,`content_source`.`created_at` AS `created_at`,`content_source`.`updated_at` AS `updated_at` from `content_source`;

-- ----------------------------
-- View structure for custom_ads
-- ----------------------------
DROP VIEW IF EXISTS `custom_ads`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `custom_ads` AS select `custom_ad`.`custom_ad_id` AS `id`,`custom_ad`.`title` AS `title`,`custom_ad`.`brand_name` AS `brand_name`,`custom_ad`.`brand_logo` AS `brand_logo`,`custom_ad`.`button_text` AS `button_text`,`custom_ad`.`is_android` AS `is_android`,`custom_ad`.`android_link` AS `android_link`,`custom_ad`.`is_ios` AS `is_ios`,`custom_ad`.`ios_link` AS `ios_link`,`custom_ad`.`start_date` AS `start_date`,`custom_ad`.`end_date` AS `end_date`,`custom_ad`.`status` AS `status`,`custom_ad`.`views` AS `views`,`custom_ad`.`clicks` AS `clicks`,`custom_ad`.`created_at` AS `created_at`,`custom_ad`.`updated_at` AS `updated_at` from `custom_ad`;

-- ----------------------------
-- View structure for custom_ad_sources
-- ----------------------------
DROP VIEW IF EXISTS `custom_ad_sources`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `custom_ad_sources` AS select `custom_ad_source`.`custom_ad_source_id` AS `id`,`custom_ad_source`.`custom_ad_id` AS `custom_ad_id`,`custom_ad_source`.`type` AS `type`,`custom_ad_source`.`content` AS `content`,`custom_ad_source`.`headline` AS `headline`,`custom_ad_source`.`description` AS `description`,`custom_ad_source`.`show_time` AS `show_time`,`custom_ad_source`.`is_skippable` AS `is_skippable`,`custom_ad_source`.`created_at` AS `created_at`,`custom_ad_source`.`updated_at` AS `updated_at` from `custom_ad_source`;

-- ----------------------------
-- View structure for episodes
-- ----------------------------
DROP VIEW IF EXISTS `episodes`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `episodes` AS select `episode`.`episode_id` AS `id`,`episode`.`season_id` AS `season_id`,`episode`.`number` AS `number`,`episode`.`thumbnail` AS `thumbnail`,`episode`.`title` AS `title`,`episode`.`description` AS `description`,case when `episode`.`duration` is null then NULL else cast(`episode`.`duration` as char charset utf8mb4) end AS `duration`,`episode`.`total_view` AS `total_view`,`episode`.`total_download` AS `total_download`,`episode`.`created_at` AS `created_at`,`episode`.`updated_at` AS `updated_at` from `episode`;

-- ----------------------------
-- View structure for episode_sources
-- ----------------------------
DROP VIEW IF EXISTS `episode_sources`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `episode_sources` AS select `episode_source`.`episode_source_id` AS `id`,`episode_source`.`episode_id` AS `episode_id`,`episode_source`.`title` AS `title`,`episode_source`.`quality` AS `quality`,`episode_source`.`size` AS `size`,`episode_source`.`is_download` AS `is_download`,`episode_source`.`access_type` AS `access_type`,`episode_source`.`type` AS `type`,`episode_source`.`source` AS `source`,`episode_source`.`created_at` AS `created_at`,`episode_source`.`updated_at` AS `updated_at` from `episode_source`;

-- ----------------------------
-- View structure for episode_subtitles
-- ----------------------------
DROP VIEW IF EXISTS `episode_subtitles`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `episode_subtitles` AS select `episode_subtitle`.`episode_subtitle_id` AS `id`,`episode_subtitle`.`episode_id` AS `episode_id`,`episode_subtitle`.`language_id` AS `language_id`,`episode_subtitle`.`file` AS `file`,`episode_subtitle`.`created_at` AS `created_at`,`episode_subtitle`.`updated_at` AS `updated_at` from `episode_subtitle`;

-- ----------------------------
-- View structure for failed_jobs
-- ----------------------------
DROP VIEW IF EXISTS `failed_jobs`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `failed_jobs` AS select `failed_job`.`failed_job_id` AS `id`,`failed_job`.`connection` AS `connection`,`failed_job`.`queue` AS `queue`,`failed_job`.`payload` AS `payload`,`failed_job`.`exception` AS `exception`,`failed_job`.`failed_at` AS `failed_at` from `failed_job`;

-- ----------------------------
-- View structure for genres
-- ----------------------------
DROP VIEW IF EXISTS `genres`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `genres` AS select `u853155779_iosdev`.`genre`.`genre_id` AS `id`,`u853155779_iosdev`.`genre`.`title` AS `title`,`u853155779_iosdev`.`genre`.`created_at` AS `created_at`,`u853155779_iosdev`.`genre`.`updated_at` AS `updated_at` from `genre`;

-- ----------------------------
-- View structure for global_settings
-- ----------------------------
DROP VIEW IF EXISTS `global_settings`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `global_settings` AS select `global_setting`.`global_setting_id` AS `id`,`global_setting`.`app_name` AS `app_name`,`global_setting`.`is_live_tv_enable` AS `is_live_tv_enable`,`global_setting`.`is_admob_android` AS `is_admob_android`,`global_setting`.`is_admob_ios` AS `is_admob_ios`,`global_setting`.`is_custom_android` AS `is_custom_android`,`global_setting`.`is_custom_ios` AS `is_custom_ios`,`global_setting`.`videoad_skip_time` AS `videoad_skip_time`,`global_setting`.`storage_type` AS `storage_type`,`global_setting`.`created_at` AS `created_at`,`global_setting`.`updated_at` AS `updated_at` from `global_setting`;

-- ----------------------------
-- View structure for languages
-- ----------------------------
DROP VIEW IF EXISTS `languages`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `languages` AS select `app_language`.`app_language_id` AS `id`,`app_language`.`title` AS `title`,`app_language`.`code` AS `code`,`app_language`.`created_at` AS `created_at`,`app_language`.`updated_at` AS `updated_at` from `app_language`;

-- ----------------------------
-- View structure for migrations
-- ----------------------------
DROP VIEW IF EXISTS `migrations`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `migrations` AS select `schema_migration`.`schema_migration_id` AS `id`,`schema_migration`.`migration` AS `migration`,`schema_migration`.`batch` AS `batch` from `schema_migration`;

-- ----------------------------
-- View structure for notifications
-- ----------------------------
DROP VIEW IF EXISTS `notifications`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `notifications` AS select `notification`.`notification_id` AS `id`,`notification`.`title` AS `title`,`notification`.`description` AS `description`,`notification`.`created_at` AS `created_at`,`notification`.`updated_at` AS `updated_at` from `notification`;

-- ----------------------------
-- View structure for seasons
-- ----------------------------
DROP VIEW IF EXISTS `seasons`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `seasons` AS select `season`.`season_id` AS `id`,`season`.`content_id` AS `content_id`,`season`.`title` AS `title`,`season`.`trailer_url` AS `trailer_url`,`season`.`created_at` AS `created_at`,`season`.`updated_at` AS `updated_at` from `season`;

-- ----------------------------
-- View structure for subtitles
-- ----------------------------
DROP VIEW IF EXISTS `subtitles`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `subtitles` AS select `subtitle`.`subtitle_id` AS `id`,`subtitle`.`content_id` AS `content_id`,`subtitle`.`language_id` AS `language_id`,`subtitle`.`file` AS `file`,`subtitle`.`created_at` AS `created_at`,`subtitle`.`updated_at` AS `updated_at` from `subtitle`;

-- ----------------------------
-- View structure for tbl_pages
-- ----------------------------
DROP VIEW IF EXISTS `tbl_pages`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `tbl_pages` AS select `cms_page`.`cms_page_id` AS `id`,`cms_page`.`privacy` AS `privacy`,`cms_page`.`termsofuse` AS `termsofuse`,`cms_page`.`created_at` AS `created_at`,`cms_page`.`updated_at` AS `updated_at` from `cms_page`;

-- ----------------------------
-- View structure for top_contents
-- ----------------------------
DROP VIEW IF EXISTS `top_contents`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `top_contents` AS select `top_content`.`top_content_id` AS `id`,`top_content`.`content_index` AS `content_index`,`top_content`.`content_id` AS `content_id`,`top_content`.`created_at` AS `created_at`,`top_content`.`updated_at` AS `updated_at` from `top_content`;

-- ----------------------------
-- View structure for tv_auth_sessions
-- ----------------------------
DROP VIEW IF EXISTS `tv_auth_sessions`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `tv_auth_sessions` AS select `tv_auth_session`.`tv_auth_session_id` AS `id`,`tv_auth_session`.`session_token` AS `session_token`,`tv_auth_session`.`qr_code` AS `qr_code`,`tv_auth_session`.`app_user_id` AS `user_id`,`tv_auth_session`.`tv_device_id` AS `tv_device_id`,`tv_auth_session`.`status` AS `status`,`tv_auth_session`.`created_at` AS `created_at`,`tv_auth_session`.`expires_at` AS `expires_at`,`tv_auth_session`.`authenticated_at` AS `authenticated_at` from `tv_auth_session`;

-- ----------------------------
-- View structure for tv_categories
-- ----------------------------
DROP VIEW IF EXISTS `tv_categories`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `tv_categories` AS select `tv_category`.`tv_category_id` AS `id`,`tv_category`.`title` AS `title`,`tv_category`.`image` AS `image`,`tv_category`.`created_at` AS `created_at`,`tv_category`.`updated_at` AS `updated_at` from `tv_category`;

-- ----------------------------
-- View structure for tv_channels
-- ----------------------------
DROP VIEW IF EXISTS `tv_channels`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `tv_channels` AS select `tv_channel`.`tv_channel_id` AS `id`,`tv_channel`.`title` AS `title`,`tv_channel`.`thumbnail` AS `thumbnail`,`tv_channel`.`access_type` AS `access_type`,`tv_channel`.`category_ids` AS `category_ids`,`tv_channel`.`type` AS `type`,`tv_channel`.`source` AS `source`,`tv_channel`.`created_at` AS `created_at`,`tv_channel`.`updated_at` AS `updated_at` from `tv_channel`;

-- ----------------------------
-- View structure for users
-- ----------------------------
DROP VIEW IF EXISTS `users`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `users` AS select `app_user`.`app_user_id` AS `id`,`app_user`.`fullname` AS `fullname`,`app_user`.`email` AS `email`,`app_user`.`login_type` AS `login_type`,`app_user`.`identity` AS `identity`,`app_user`.`profile_image` AS `profile_image`,`app_user`.`watchlist_content_ids` AS `watchlist_content_ids`,`app_user`.`device_type` AS `device_type`,`app_user`.`device_token` AS `device_token`,`app_user`.`created_at` AS `created_at`,`app_user`.`updated_at` AS `updated_at` from `app_user`;

-- ----------------------------
-- View structure for user_accessible_content
-- ----------------------------
DROP VIEW IF EXISTS `user_accessible_content`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `user_accessible_content` AS select distinct `c`.`content_id` AS `content_id`,`c`.`title` AS `title`,`c`.`content_distributor_id` AS `content_distributor_id`,`cd`.`code` AS `distributor_code`,`cd`.`name` AS `distributor_name`,`cd`.`is_base_included` AS `is_base_included`,`cd`.`is_premium` AS `is_premium`,`ubs`.`app_user_id` AS `base_user_id`,`uda`.`app_user_id` AS `premium_user_id` from (((`content` `c` left join `content_distributor` `cd` on(`c`.`content_distributor_id` = `cd`.`content_distributor_id`)) left join `user_base_subscription` `ubs` on(`ubs`.`is_active` = 1 and (`ubs`.`end_date` is null or `ubs`.`end_date` > current_timestamp()))) left join `user_distributor_access` `uda` on(`uda`.`content_distributor_id` = `c`.`content_distributor_id` and `uda`.`is_active` = 1 and (`uda`.`end_date` is null or `uda`.`end_date` > current_timestamp()))) where `cd`.`is_active` = 1;

-- ----------------------------
-- View structure for v_active_subscriptions
-- ----------------------------
DROP VIEW IF EXISTS `v_active_subscriptions`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_active_subscriptions` AS select `ubs`.`app_user_id` AS `app_user_id`,'base' AS `subscription_type`,NULL AS `content_distributor_id`,`ubs`.`start_date` AS `start_date`,`ubs`.`end_date` AS `end_date`,`ubs`.`is_active` AS `is_active`,`pt`.`total_amount` AS `last_payment_amount`,`pt`.`payment_status` AS `last_payment_status`,`pt`.`created_at` AS `last_payment_date` from (`user_base_subscription` `ubs` left join (select `payment_transaction`.`app_user_id` AS `app_user_id`,`payment_transaction`.`total_amount` AS `total_amount`,`payment_transaction`.`payment_status` AS `payment_status`,`payment_transaction`.`created_at` AS `created_at`,row_number() over ( partition by `payment_transaction`.`app_user_id` order by `payment_transaction`.`created_at` desc) AS `rn` from `payment_transaction` where `payment_transaction`.`subscription_type` = 'base' and `payment_transaction`.`payment_status` = 'completed') `pt` on(`ubs`.`app_user_id` = `pt`.`app_user_id` and `pt`.`rn` = 1)) where `ubs`.`is_active` = 1 union all select `uda`.`app_user_id` AS `app_user_id`,'distributor' AS `subscription_type`,`uda`.`content_distributor_id` AS `content_distributor_id`,`uda`.`start_date` AS `start_date`,`uda`.`end_date` AS `end_date`,`uda`.`is_active` AS `is_active`,`pt`.`total_amount` AS `last_payment_amount`,`pt`.`payment_status` AS `last_payment_status`,`pt`.`created_at` AS `last_payment_date` from (`user_distributor_access` `uda` left join (select `payment_transaction`.`app_user_id` AS `app_user_id`,`payment_transaction`.`content_distributor_id` AS `content_distributor_id`,`payment_transaction`.`total_amount` AS `total_amount`,`payment_transaction`.`payment_status` AS `payment_status`,`payment_transaction`.`created_at` AS `created_at`,row_number() over ( partition by `payment_transaction`.`app_user_id`,`payment_transaction`.`content_distributor_id` order by `payment_transaction`.`created_at` desc) AS `rn` from `payment_transaction` where `payment_transaction`.`subscription_type` = 'distributor' and `payment_transaction`.`payment_status` = 'completed') `pt` on(`uda`.`app_user_id` = `pt`.`app_user_id` and `uda`.`content_distributor_id` = `pt`.`content_distributor_id` and `pt`.`rn` = 1)) where `uda`.`is_active` = 1;

-- ----------------------------
-- View structure for v_content_age_restrictions
-- ----------------------------
DROP VIEW IF EXISTS `v_content_age_restrictions`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_content_age_restrictions` AS select `c`.`content_id` AS `content_id`,`c`.`title` AS `title`,`al`.`age_limit_id` AS `age_limit_id`,`al`.`name` AS `age_group`,`al`.`min_age` AS `min_age`,`al`.`max_age` AS `max_age`,`al`.`code` AS `code`,`al`.`description` AS `description` from ((`content` `c` left join `content_age_limit` `cal` on(`c`.`content_id` = `cal`.`content_id`)) left join `age_limit` `al` on(`cal`.`age_limit_id` = `al`.`age_limit_id`)) where `c`.`is_show` = 1;

-- ----------------------------
-- View structure for v_content_with_tracks
-- ----------------------------
DROP VIEW IF EXISTS `v_content_with_tracks`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_content_with_tracks` AS select `c`.`content_id` AS `content_id`,`c`.`title` AS `title`,count(distinct `cat`.`id`) AS `audio_track_count`,count(distinct `cst`.`id`) AS `subtitle_track_count`,group_concat(distinct `cat`.`language_code` separator ',') AS `audio_languages`,group_concat(distinct `cst`.`language_code` separator ',') AS `subtitle_languages` from ((`content` `c` left join `content_audio_tracks` `cat` on(`c`.`content_id` = `cat`.`content_id`)) left join `content_subtitle_tracks` `cst` on(`c`.`content_id` = `cst`.`content_id`)) group by `c`.`content_id`,`c`.`title`;

-- ----------------------------
-- Function structure for can_profile_access_content
-- ----------------------------
DROP FUNCTION IF EXISTS `can_profile_access_content`;
delimiter ;;
CREATE FUNCTION `can_profile_access_content`(p_profile_id INT,                                                                                                                         
    p_content_id INT)
 RETURNS tinyint(1)
  READS SQL DATA 
  DETERMINISTIC
BEGIN                                                                                                                                         
    DECLARE profile_age INT;                                                                                                                  
    DECLARE is_kids_prof BOOLEAN;                                                                                                             
    DECLARE content_min_age INT;                                                                                                              
                                                                                                                                              
    -- Get profile details                                                                                                                    
    SELECT age, is_kids_profile                                                                                                               
    INTO profile_age, is_kids_prof                                                                                                            
    FROM app_user_profile                                                                                                                     
    WHERE profile_id = p_profile_id;                                                                                                          
                                                                                                                                              
    -- Kids profiles can only access 0-6 and 7-12 content                                                                                     
    IF is_kids_prof = TRUE THEN                                                                                                               
        RETURN EXISTS (                                                                                                                       
            SELECT 1                                                                                                                          
            FROM content_age_limit cal                                                                                                        
            JOIN age_limit al ON cal.age_limit_id = al.age_limit_id                                                                           
            WHERE cal.content_id = p_content_id                                                                                               
            AND al.max_age <= 12                                                                                                              
        ) OR NOT EXISTS (                                                                                                                     
            SELECT 1 FROM content_age_limit WHERE content_id = p_content_id                                                                   
        );                                                                                                                                    
    END IF;                                                                                                                                   
                                                                                                                                              
    -- If profile has no age set and not kids profile, allow all content                                                                      
    IF profile_age IS NULL THEN                                                                                                               
        RETURN TRUE;                                                                                                                          
    END IF;                                                                                                                                   
                                                                                                                                              
    -- Get content's minimum age requirement                                                                                                  
    SELECT COALESCE(MAX(al.min_age), 0)                                                                                                       
    INTO content_min_age                                                                                                                      
    FROM content_age_limit cal                                                                                                                
    JOIN age_limit al ON cal.age_limit_id = al.age_limit_id                                                                                   
    WHERE cal.content_id = p_content_id;                                                                                                      
                                                                                                                                              
    -- Profile age must be >= content minimum age                                                                                             
    RETURN profile_age >= content_min_age;                                                                                                    
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
