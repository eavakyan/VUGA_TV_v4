-- User Notifications Table
CREATE TABLE IF NOT EXISTS `user_notification` (
  `notification_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `notification_type` enum('system', 'promotional', 'update', 'maintenance') DEFAULT 'system',
  `target_platforms` json DEFAULT NULL COMMENT 'Array of platforms: ["ios", "android", "android_tv", "all"]',
  `target_user_types` json DEFAULT NULL COMMENT 'Array of user types: ["all", "premium", "free"]',
  `priority` enum('low', 'medium', 'high', 'urgent') DEFAULT 'medium',
  `scheduled_at` timestamp NULL DEFAULT NULL,
  `expires_at` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_by` int(11) NOT NULL COMMENT 'admin_user_id',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`notification_id`),
  KEY `idx_active_scheduled` (`is_active`, `scheduled_at`),
  KEY `idx_expires_at` (`expires_at`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Profile Notification Status (tracks which profiles have seen which notifications)
CREATE TABLE IF NOT EXISTS `profile_notification_status` (
  `profile_id` int(11) NOT NULL,
  `notification_id` bigint(20) NOT NULL,
  `shown_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `dismissed_at` timestamp NULL DEFAULT NULL,
  `platform` varchar(20) NOT NULL COMMENT 'ios, android, android_tv',
  PRIMARY KEY (`profile_id`, `notification_id`),
  KEY `idx_notification_id` (`notification_id`),
  KEY `idx_shown_at` (`shown_at`),
  CONSTRAINT `fk_profile_notification_profile` FOREIGN KEY (`profile_id`) REFERENCES `app_user_profile` (`profile_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_profile_notification_notification` FOREIGN KEY (`notification_id`) REFERENCES `user_notification` (`notification_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Notification Analytics (for tracking performance)
CREATE TABLE IF NOT EXISTS `notification_analytics` (
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

-- Add indexes for performance with large datasets
CREATE INDEX idx_user_notification_active_scheduled ON user_notification(is_active, scheduled_at);
CREATE INDEX idx_user_notification_expires ON user_notification(expires_at);
CREATE INDEX idx_profile_notification_status_composite ON profile_notification_status(notification_id, profile_id, shown_at);

-- Add foreign key constraint to existing admin_user table
ALTER TABLE `user_notification` 
ADD CONSTRAINT `fk_notification_admin` FOREIGN KEY (`created_by`) REFERENCES `admin_user` (`admin_user_id`);