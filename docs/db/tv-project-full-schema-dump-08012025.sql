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

 Date: 01/08/2025 15:53:57
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
  PRIMARY KEY (`app_user_id`),
  KEY `idx_app_user_email` (`email`),
  KEY `idx_app_user_device_type` (`device_type`),
  KEY `fk_user_last_profile` (`last_active_profile_id`),
  CONSTRAINT `fk_user_last_profile` FOREIGN KEY (`last_active_profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
-- Table structure for app_user_profile
-- ----------------------------
DROP TABLE IF EXISTS `app_user_profile`;
CREATE TABLE `app_user_profile` (
  `profile_id` int(11) NOT NULL AUTO_INCREMENT,
  `app_user_id` int(10) unsigned NOT NULL,
  `name` varchar(100) NOT NULL,
  `avatar_type` enum('default','custom') DEFAULT 'default',
  `avatar_id` int(11) DEFAULT NULL COMMENT 'References default_avatar table or custom upload',
  `custom_avatar_url` varchar(500) DEFAULT NULL,
  `is_kids` tinyint(1) DEFAULT 0,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`profile_id`),
  KEY `idx_app_user_id` (`app_user_id`),
  CONSTRAINT `fk_profile_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
-- Table structure for app_user_watch_history
-- ----------------------------
DROP TABLE IF EXISTS `app_user_watch_history`;
CREATE TABLE `app_user_watch_history` (
  `watch_history_id` int(11) NOT NULL AUTO_INCREMENT,
  `app_user_id` int(10) unsigned NOT NULL,
  `content_id` int(11) DEFAULT NULL,
  `episode_id` int(11) DEFAULT NULL,
  `last_watched_position` int(11) DEFAULT 0 COMMENT 'Position in seconds',
  `total_duration` int(11) DEFAULT 0 COMMENT 'Total duration in seconds',
  `completed` tinyint(1) DEFAULT 0,
  `device_type` tinyint(1) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`watch_history_id`),
  UNIQUE KEY `uk_user_content_episode` (`app_user_id`,`content_id`,`episode_id`),
  KEY `idx_user_content` (`app_user_id`,`content_id`),
  KEY `idx_user_episode` (`app_user_id`,`episode_id`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `content_id` (`content_id`),
  KEY `episode_id` (`episode_id`),
  CONSTRAINT `app_user_watch_history_ibfk_1` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE,
  CONSTRAINT `app_user_watch_history_ibfk_2` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `app_user_watch_history_ibfk_3` FOREIGN KEY (`episode_id`) REFERENCES `episode` (`episode_id`) ON DELETE CASCADE
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
  `release_year` int(11) NOT NULL,
  `ratings` float NOT NULL DEFAULT 0,
  `language_id` int(11) NOT NULL,
  `trailer_url` varchar(255) DEFAULT NULL,
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
  PRIMARY KEY (`content_id`),
  KEY `idx_content_type_featured_show` (`type`,`is_featured`,`is_show`),
  KEY `idx_content_language` (`language_id`),
  KEY `idx_content_release_year` (`release_year`),
  KEY `idx_content_genre_ids` (`genre_ids`),
  CONSTRAINT `fk_content_language` FOREIGN KEY (`language_id`) REFERENCES `app_language` (`app_language_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
  KEY `fk_content_cast_actor` (`actor_id`),
  KEY `fk_content_cast_content` (`content_id`),
  CONSTRAINT `fk_content_cast_actor` FOREIGN KEY (`actor_id`) REFERENCES `actor` (`actor_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_content_cast_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for content_genre
-- ----------------------------
DROP TABLE IF EXISTS `content_genre`;
CREATE TABLE `content_genre` (
  `content_id` int(11) NOT NULL,
  `genre_id` int(11) NOT NULL,
  PRIMARY KEY (`content_id`,`genre_id`),
  KEY `idx_genre_content` (`genre_id`,`content_id`),
  CONSTRAINT `content_genre_ibfk_1` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `content_genre_ibfk_2` FOREIGN KEY (`genre_id`) REFERENCES `genre` (`genre_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  CONSTRAINT `fk_content_source_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
  `total_view` int(11) DEFAULT 0,
  `total_download` int(11) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`episode_id`),
  KEY `idx_episode_season` (`season_id`),
  CONSTRAINT `fk_episode_season` FOREIGN KEY (`season_id`) REFERENCES `season` (`season_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
-- Table structure for genre
-- ----------------------------
DROP TABLE IF EXISTS `genre`;
CREATE TABLE `genre` (
  `genre_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`genre_id`),
  UNIQUE KEY `uk_title` (`title`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
  KEY `idx_profile_history_recent` (`profile_id`,`updated_at` DESC),
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
  CONSTRAINT `fk_season_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
-- View structure for contents
-- ----------------------------
DROP VIEW IF EXISTS `contents`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `contents` AS select `content`.`content_id` AS `id`,`content`.`title` AS `title`,`content`.`description` AS `description`,`content`.`type` AS `type`,case when `content`.`duration` is null then NULL else cast(`content`.`duration` as char charset utf8mb4) end AS `duration`,`content`.`release_year` AS `release_year`,`content`.`ratings` AS `ratings`,`content`.`language_id` AS `language_id`,`content`.`trailer_url` AS `trailer_url`,`content`.`vertical_poster` AS `vertical_poster`,`content`.`horizontal_poster` AS `horizontal_poster`,`content`.`genre_ids` AS `genre_ids`,`content`.`is_featured` AS `is_featured`,`content`.`is_show` AS `is_show`,`content`.`total_view` AS `total_view`,`content`.`total_download` AS `total_download`,`content`.`total_share` AS `total_share`,`content`.`created_at` AS `created_at`,`content`.`updated_at` AS `updated_at` from `content`;

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
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `genres` AS select `genre`.`genre_id` AS `id`,`genre`.`title` AS `title`,`genre`.`created_at` AS `created_at`,`genre`.`updated_at` AS `updated_at` from `genre`;

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

SET FOREIGN_KEY_CHECKS = 1;
