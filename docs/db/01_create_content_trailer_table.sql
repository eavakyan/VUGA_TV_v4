-- ====================================
-- Create content_trailer table
-- Purpose: Convert single trailer_url field to one-to-many relationship
-- Date: 2025-01-08
-- ====================================

-- Create the new content_trailer table
CREATE TABLE `content_trailer` (
  `content_trailer_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` int(11) NOT NULL,
  `title` varchar(255) DEFAULT NULL COMMENT 'Trailer title/description',
  `youtube_id` varchar(20) NOT NULL COMMENT 'YouTube video ID only',
  `trailer_url` varchar(500) NOT NULL COMMENT 'Full YouTube URL for backward compatibility',
  `is_primary` tinyint(1) DEFAULT 0 COMMENT '1 = Primary trailer, 0 = Additional trailer',
  `sort_order` int(11) DEFAULT 0 COMMENT 'Display order',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`content_trailer_id`),
  KEY `idx_content_trailer_content` (`content_id`),
  KEY `idx_content_trailer_primary` (`content_id`, `is_primary`),
  KEY `idx_content_trailer_sort` (`content_id`, `sort_order`),
  CONSTRAINT `fk_content_trailer_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Add index for faster queries
CREATE INDEX `idx_content_trailer_youtube_id` ON `content_trailer` (`youtube_id`);

-- Add unique constraint to prevent duplicate trailers per content
CREATE UNIQUE INDEX `idx_content_trailer_unique` ON `content_trailer` (`content_id`, `youtube_id`);