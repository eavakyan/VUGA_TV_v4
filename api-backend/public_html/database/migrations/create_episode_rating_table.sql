-- Create table for episode ratings
CREATE TABLE IF NOT EXISTS `app_profile_episode_rating` (
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

-- Add average rating column to episode table
ALTER TABLE `episode` 
ADD COLUMN `ratings` float NOT NULL DEFAULT 0 AFTER `duration`;

-- Create index for better performance
CREATE INDEX idx_episode_ratings ON `episode` (`ratings` DESC);