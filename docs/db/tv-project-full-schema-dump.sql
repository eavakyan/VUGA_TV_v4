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

 Date: 27/07/2025 10:09:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for actors
-- ----------------------------
DROP TABLE IF EXISTS `actors`;
CREATE TABLE `actors` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fullname` varchar(100) DEFAULT NULL,
  `dob` varchar(11) NOT NULL,
  `bio` varchar(900) NOT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) NOT NULL,
  `user_password` varchar(255) NOT NULL,
  `user_type` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for admob
-- ----------------------------
DROP TABLE IF EXISTS `admob`;
CREATE TABLE `admob` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `banner_id` varchar(255) NOT NULL,
  `intersial_id` varchar(255) NOT NULL,
  `rewarded_id` varchar(255) NOT NULL,
  `type` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for content_cast
-- ----------------------------
DROP TABLE IF EXISTS `content_cast`;
CREATE TABLE `content_cast` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` bigint(20) NOT NULL,
  `actor_id` int(11) NOT NULL,
  `character_name` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for content_sources
-- ----------------------------
DROP TABLE IF EXISTS `content_sources`;
CREATE TABLE `content_sources` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for contents
-- ----------------------------
DROP TABLE IF EXISTS `contents`;
CREATE TABLE `contents` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '1 = movie / 2 = series',
  `duration` varchar(50) DEFAULT NULL,
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for custom_ad_sources
-- ----------------------------
DROP TABLE IF EXISTS `custom_ad_sources`;
CREATE TABLE `custom_ad_sources` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `custom_ad_id` int(11) NOT NULL,
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '0 = Image / 1 = Video',
  `content` varchar(255) NOT NULL,
  `headline` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `show_time` int(11) DEFAULT NULL,
  `is_skippable` int(11) DEFAULT NULL COMMENT '0 = Must Watch / 1 = Skippable',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for custom_ads
-- ----------------------------
DROP TABLE IF EXISTS `custom_ads`;
CREATE TABLE `custom_ads` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
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
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for episode_sources
-- ----------------------------
DROP TABLE IF EXISTS `episode_sources`;
CREATE TABLE `episode_sources` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for episode_subtitles
-- ----------------------------
DROP TABLE IF EXISTS `episode_subtitles`;
CREATE TABLE `episode_subtitles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `episode_id` int(11) NOT NULL,
  `language_id` int(11) NOT NULL,
  `file` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for episodes
-- ----------------------------
DROP TABLE IF EXISTS `episodes`;
CREATE TABLE `episodes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `season_id` bigint(20) NOT NULL,
  `number` int(11) NOT NULL,
  `thumbnail` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `description` varchar(900) NOT NULL,
  `duration` varchar(50) NOT NULL,
  `total_view` int(11) DEFAULT 0,
  `total_download` int(11) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for failed_jobs
-- ----------------------------
DROP TABLE IF EXISTS `failed_jobs`;
CREATE TABLE `failed_jobs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `connection` text NOT NULL,
  `queue` text NOT NULL,
  `payload` longtext NOT NULL,
  `exception` longtext NOT NULL,
  `failed_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for genres
-- ----------------------------
DROP TABLE IF EXISTS `genres`;
CREATE TABLE `genres` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for global_settings
-- ----------------------------
DROP TABLE IF EXISTS `global_settings`;
CREATE TABLE `global_settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app_name` varchar(55) DEFAULT NULL,
  `is_live_tv_enable` tinyint(1) NOT NULL,
  `is_admob_android` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=on 0=off',
  `is_admob_ios` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=on 0=off',
  `is_custom_android` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=on 0=off',
  `is_custom_ios` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=on 0=off',
  `videoad_skip_time` int(11) NOT NULL DEFAULT 8,
  `storage_type` int(11) NOT NULL DEFAULT 0 COMMENT '0 = Local / 1 = AWS S3 / 2 = DigitaoOcean Space',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for languages
-- ----------------------------
DROP TABLE IF EXISTS `languages`;
CREATE TABLE `languages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `code` varchar(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for media_gallery
-- ----------------------------
DROP TABLE IF EXISTS `media_gallery`;
CREATE TABLE `media_gallery` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(55) NOT NULL,
  `file` varchar(555) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for migrations
-- ----------------------------
DROP TABLE IF EXISTS `migrations`;
CREATE TABLE `migrations` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `migration` varchar(191) NOT NULL,
  `batch` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` varchar(900) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for seasons
-- ----------------------------
DROP TABLE IF EXISTS `seasons`;
CREATE TABLE `seasons` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` bigint(20) NOT NULL,
  `title` varchar(100) NOT NULL,
  `trailer_url` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for subtitles
-- ----------------------------
DROP TABLE IF EXISTS `subtitles`;
CREATE TABLE `subtitles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` varchar(8) NOT NULL,
  `language_id` int(11) NOT NULL,
  `file` varchar(555) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for tbl_pages
-- ----------------------------
DROP TABLE IF EXISTS `tbl_pages`;
CREATE TABLE `tbl_pages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `privacy` mediumtext DEFAULT NULL,
  `termsofuse` mediumtext DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for top_contents
-- ----------------------------
DROP TABLE IF EXISTS `top_contents`;
CREATE TABLE `top_contents` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content_index` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for tv_auth_sessions
-- ----------------------------
DROP TABLE IF EXISTS `tv_auth_sessions`;
CREATE TABLE `tv_auth_sessions` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `session_token` varchar(64) NOT NULL,
  `qr_code` varchar(255) NOT NULL,
  `user_id` int(10) unsigned DEFAULT NULL,
  `tv_device_id` varchar(255) NOT NULL,
  `status` enum('pending','authenticated','expired') DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `expires_at` timestamp NULL DEFAULT (current_timestamp() + interval 5 minute),
  `authenticated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `session_token` (`session_token`),
  KEY `idx_session_token` (`session_token`),
  KEY `idx_status_expires` (`status`,`expires_at`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `tv_auth_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for tv_categories
-- ----------------------------
DROP TABLE IF EXISTS `tv_categories`;
CREATE TABLE `tv_categories` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for tv_channels
-- ----------------------------
DROP TABLE IF EXISTS `tv_channels`;
CREATE TABLE `tv_channels` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `thumbnail` varchar(255) DEFAULT NULL,
  `access_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1 = Free / 2 = Paid / 3 = Unlock With Video Ads',
  `category_ids` text DEFAULT NULL,
  `type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 = Youtube URL, 2 = M3u8 Url',
  `source` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fullname` varchar(50) DEFAULT NULL,
  `email` varchar(55) DEFAULT NULL,
  `login_type` tinyint(1) DEFAULT 0 COMMENT '1 = Google / 2 = Apple / 3 = Email',
  `identity` text DEFAULT NULL,
  `profile_image` text DEFAULT NULL,
  `watchlist_content_ids` varchar(255) DEFAULT NULL,
  `device_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 = Andriod, 1 = iOS',
  `device_token` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
