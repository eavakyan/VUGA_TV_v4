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

 Date: 25/07/2025 22:10:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
