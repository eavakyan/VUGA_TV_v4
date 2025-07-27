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

 Date: 27/07/2025 11:45:12
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
  PRIMARY KEY (`app_user_id`),
  KEY `idx_app_user_email` (`email`),
  KEY `idx_app_user_device_type` (`device_type`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
  `app_user_id` int(10) unsigned NOT NULL,
  `content_id` int(11) NOT NULL,
  `added_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`app_user_id`,`content_id`),
  KEY `idx_content_watchlist` (`content_id`),
  CONSTRAINT `app_user_watchlist_ibfk_1` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE,
  CONSTRAINT `app_user_watchlist_ibfk_2` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

SET FOREIGN_KEY_CHECKS = 1;
