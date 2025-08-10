-- Content Distributor Schema - Fixed Foreign Key References
-- This script adds content distributor functionality with base + premium access model

-- Drop tables in reverse dependency order if they exist
DROP TABLE IF EXISTS `user_distributor_access`;
DROP TABLE IF EXISTS `user_base_subscription`;
DROP TABLE IF EXISTS `content_distributor`;

-- Create content_distributor table
CREATE TABLE IF NOT EXISTS `content_distributor` (
  `content_distributor_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `code` varchar(50) NOT NULL UNIQUE,
  `description` text DEFAULT NULL,
  `logo_url` varchar(500) DEFAULT NULL,
  `is_base_included` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1 = Included in base subscription',
  `is_premium` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 = Premium content, 0 = Base content',
  `display_order` int(11) DEFAULT 0,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`content_distributor_id`),
  KEY `idx_code` (`code`),
  KEY `idx_active_premium` (`is_active`, `is_premium`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Insert default distributor for self-hosted content
INSERT INTO `content_distributor` (`name`, `code`, `description`, `is_base_included`, `is_premium`, `display_order`, `is_active`)
VALUES ('Self-Hosted', 'SELF', 'Content directly hosted by the platform', 1, 0, 0, 1);

-- Add content_distributor_id to content table
ALTER TABLE `content` 
ADD COLUMN `content_distributor_id` int(11) DEFAULT NULL 
COMMENT 'NULL = legacy content, otherwise links to content_distributor';

-- Create index for distributor lookup
ALTER TABLE `content` 
ADD INDEX `idx_content_distributor` (`content_distributor_id`);

-- Add foreign key constraint
ALTER TABLE `content` 
ADD CONSTRAINT `fk_content_distributor` 
FOREIGN KEY (`content_distributor_id`) 
REFERENCES `content_distributor` (`content_distributor_id`) 
ON DELETE SET NULL;

-- Update existing content to use SELF distributor
UPDATE `content` 
SET `content_distributor_id` = (SELECT `content_distributor_id` FROM `content_distributor` WHERE `code` = 'SELF' LIMIT 1)
WHERE `content_distributor_id` IS NULL;

-- Create user_base_subscription table (tracks base subscription status)
-- Fixed: Using app_user_id to reference app_user(app_user_id)
CREATE TABLE IF NOT EXISTS `user_base_subscription` (
  `subscription_id` int(11) NOT NULL AUTO_INCREMENT,
  `app_user_id` int(10) unsigned NOT NULL,
  `start_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `end_date` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `subscription_type` varchar(50) DEFAULT 'monthly' COMMENT 'monthly, yearly, etc',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`subscription_id`),
  KEY `idx_user_active` (`app_user_id`, `is_active`),
  KEY `idx_end_date` (`end_date`),
  CONSTRAINT `fk_base_sub_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Create user_distributor_access table (tracks premium distributor access)
-- Fixed: Using app_user_id to reference app_user(app_user_id)
CREATE TABLE IF NOT EXISTS `user_distributor_access` (
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
  UNIQUE KEY `uk_user_distributor` (`app_user_id`, `content_distributor_id`),
  KEY `idx_user_active_dist` (`app_user_id`, `is_active`),
  KEY `idx_distributor` (`content_distributor_id`),
  KEY `idx_end_date_dist` (`end_date`),
  CONSTRAINT `fk_dist_access_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_dist_access_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Sample premium distributors (for testing)
INSERT INTO `content_distributor` (`name`, `code`, `description`, `is_base_included`, `is_premium`, `display_order`) VALUES
('Premium Studios', 'PREMIUM_STUDIOS', 'Premium studio content collection', 0, 1, 1),
('Indie Films', 'INDIE_FILMS', 'Independent film collection', 0, 1, 2),
('Classic Cinema', 'CLASSIC_CINEMA', 'Classic movies collection', 0, 1, 3);

-- Create views for easier access control queries
CREATE OR REPLACE VIEW `user_accessible_content` AS
SELECT DISTINCT
    c.content_id,
    c.title,
    c.content_distributor_id,
    cd.code as distributor_code,
    cd.name as distributor_name,
    cd.is_base_included,
    cd.is_premium,
    ubs.app_user_id as base_user_id,
    uda.app_user_id as premium_user_id
FROM 
    content c
    LEFT JOIN content_distributor cd ON c.content_distributor_id = cd.content_distributor_id
    LEFT JOIN user_base_subscription ubs ON ubs.is_active = 1 AND (ubs.end_date IS NULL OR ubs.end_date > NOW())
    LEFT JOIN user_distributor_access uda ON uda.content_distributor_id = c.content_distributor_id 
        AND uda.is_active = 1 
        AND (uda.end_date IS NULL OR uda.end_date > NOW())
WHERE 
    cd.is_active = 1;

-- Create index for common queries
CREATE INDEX idx_content_distributor_active ON content_distributor(is_active, is_premium, is_base_included);