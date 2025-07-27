
-- !!! FIRST DB CHANGE OF THE DAY: 07272025-1

ALTER TABLE tv_auth_sessions DROP FOREIGN KEY IF EXISTS tv_auth_sessions_ibfk_1;

RENAME TABLE actors TO actor;
RENAME TABLE admob TO admob_config;
RENAME TABLE contents TO content;
RENAME TABLE content_sources TO content_source;
RENAME TABLE custom_ads TO custom_ad;
RENAME TABLE custom_ad_sources TO custom_ad_source;
RENAME TABLE episodes TO episode;
RENAME TABLE episode_sources TO episode_source;
RENAME TABLE episode_subtitles TO episode_subtitle;
RENAME TABLE failed_jobs TO failed_job;
RENAME TABLE genres TO genre;
RENAME TABLE global_settings TO global_setting;
RENAME TABLE languages TO app_language;  -- avoid reserved keyword
RENAME TABLE migrations TO schema_migration;  -- avoid reserved keyword
RENAME TABLE notifications TO notification;
RENAME TABLE seasons TO season;
RENAME TABLE subtitles TO subtitle;
RENAME TABLE tbl_pages TO cms_page;  -- cleaner name
RENAME TABLE top_contents TO top_content;
RENAME TABLE tv_auth_sessions TO tv_auth_session;
RENAME TABLE tv_categories TO tv_category;
RENAME TABLE tv_channels TO tv_channel;
RENAME TABLE users TO app_user;  -- avoid reserved keyword


ALTER TABLE actor CHANGE COLUMN id actor_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE admin_user CHANGE COLUMN id admin_user_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE admob_config CHANGE COLUMN id admob_config_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE content CHANGE COLUMN id content_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE content_cast CHANGE COLUMN id content_cast_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE content_source CHANGE COLUMN id content_source_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE custom_ad CHANGE COLUMN id custom_ad_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE custom_ad_source CHANGE COLUMN id custom_ad_source_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE episode CHANGE COLUMN id episode_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE episode_source CHANGE COLUMN id episode_source_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE episode_subtitle CHANGE COLUMN id episode_subtitle_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE failed_job CHANGE COLUMN id failed_job_id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT;
ALTER TABLE genre CHANGE COLUMN id genre_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE global_setting CHANGE COLUMN id global_setting_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE app_language CHANGE COLUMN id app_language_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE media_gallery CHANGE COLUMN id media_gallery_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE schema_migration CHANGE COLUMN id schema_migration_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT;
ALTER TABLE notification CHANGE COLUMN id notification_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE cms_page CHANGE COLUMN id cms_page_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE season CHANGE COLUMN id season_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE subtitle CHANGE COLUMN id subtitle_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE top_content CHANGE COLUMN id top_content_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE tv_auth_session CHANGE COLUMN id tv_auth_session_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT;
ALTER TABLE tv_category CHANGE COLUMN id tv_category_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE tv_channel CHANGE COLUMN id tv_channel_id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE app_user CHANGE COLUMN id app_user_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT;


ALTER TABLE tv_auth_session CHANGE COLUMN user_id app_user_id INT(10) UNSIGNED DEFAULT NULL;

-- Step 5: Recreate foreign key constraints
ALTER TABLE tv_auth_session
ADD CONSTRAINT fk_tv_auth_session_app_user
FOREIGN KEY (app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE;

-- Add other missing FK constraints as identified earlier
ALTER TABLE content_source
ADD CONSTRAINT fk_content_source_content
FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE;

ALTER TABLE content_cast
ADD CONSTRAINT fk_content_cast_actor
FOREIGN KEY (actor_id) REFERENCES actor(actor_id) ON DELETE CASCADE;


-- Fix the data type mismatch first
ALTER TABLE content_cast MODIFY COLUMN content_id INT(11) NOT NULL;

-- Then add the foreign key constraint
ALTER TABLE content_cast
ADD CONSTRAINT fk_content_cast_content
FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE;



-- !!! ***** NEXT DB CHANGE OF THE DAY: 07272025-2 ***** !!!

--  1. Data Type Inconsistencies (Critical)

  -- CRITICAL: Fix mismatched foreign key data types
  ALTER TABLE episode MODIFY COLUMN season_id INT(11) NOT NULL;  -- Currently BIGINT(20)
  ALTER TABLE season MODIFY COLUMN content_id INT(11) NOT NULL;   -- Currently BIGINT(20)
  ALTER TABLE subtitle MODIFY COLUMN content_id INT(11) NOT NULL; -- Currently VARCHAR(8)!
  ALTER TABLE episode_subtitle MODIFY COLUMN language_id INT(11) NOT NULL; 

--  2. Missing Foreign Key Constraints

  -- Add missing critical FK constraints
  ALTER TABLE content
  ADD CONSTRAINT fk_content_language
  FOREIGN KEY (language_id) REFERENCES app_language(app_language_id);

  ALTER TABLE season
  ADD CONSTRAINT fk_season_content
  FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE;

  ALTER TABLE episode
  ADD CONSTRAINT fk_episode_season
  FOREIGN KEY (season_id) REFERENCES season(season_id) ON DELETE CASCADE;

  ALTER TABLE episode_source
  ADD CONSTRAINT fk_episode_source_episode
  FOREIGN KEY (episode_id) REFERENCES episode(episode_id) ON DELETE CASCADE;

  ALTER TABLE episode_subtitle
  ADD CONSTRAINT fk_episode_subtitle_episode
  FOREIGN KEY (episode_id) REFERENCES episode(episode_id) ON DELETE CASCADE;

  ALTER TABLE episode_subtitle
  ADD CONSTRAINT fk_episode_subtitle_language
  FOREIGN KEY (language_id) REFERENCES app_language(app_language_id);

  ALTER TABLE subtitle
  ADD CONSTRAINT fk_subtitle_content
  FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE;

  ALTER TABLE subtitle
  ADD CONSTRAINT fk_subtitle_language
  FOREIGN KEY (language_id) REFERENCES app_language(app_language_id);

  ALTER TABLE top_content
  ADD CONSTRAINT fk_top_content_content
  FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE;

  ALTER TABLE custom_ad_source
  ADD CONSTRAINT fk_custom_ad_source_custom_ad
  FOREIGN KEY (custom_ad_id) REFERENCES custom_ad(custom_ad_id) ON DELETE CASCADE;

  3. Missing Indexes for Performance

  -- Critical performance indexes
  CREATE INDEX idx_content_type_featured_show ON content(type, is_featured, is_show);
  CREATE INDEX idx_content_language ON content(language_id);
  CREATE INDEX idx_content_release_year ON content(release_year);
  CREATE INDEX idx_content_genre_ids ON content(genre_ids);  -- Consider normalizing this

  CREATE INDEX idx_content_source_content ON content_source(content_id);
  CREATE INDEX idx_content_source_access_type ON content_source(access_type);

  CREATE INDEX idx_episode_season ON episode(season_id);
  CREATE INDEX idx_episode_source_episode ON episode_source(episode_id);

  CREATE INDEX idx_season_content ON season(content_id);

  CREATE INDEX idx_app_user_email ON app_user(email);
  CREATE INDEX idx_app_user_device_type ON app_user(device_type);

  CREATE INDEX idx_custom_ad_dates ON custom_ad(start_date, end_date, status);

  4. Schema Design Issues

  -- Normalize genre_ids (currently comma-separated string)
  CREATE TABLE content_genre (
      content_id INT(11) NOT NULL,
      genre_id INT(11) NOT NULL,
      PRIMARY KEY (content_id, genre_id),
      FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE,
      FOREIGN KEY (genre_id) REFERENCES genre(genre_id) ON DELETE CASCADE,
      INDEX idx_genre_content (genre_id, content_id)
  );

  -- Normalize watchlist_content_ids
  CREATE TABLE app_user_watchlist (
      app_user_id INT(10) UNSIGNED NOT NULL,
      content_id INT(11) NOT NULL,
      added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (app_user_id, content_id),
      FOREIGN KEY (app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE,
      FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE,
      INDEX idx_content_watchlist (content_id)
  );

  -- Normalize tv_channel category_ids
  CREATE TABLE tv_channel_category (
      tv_channel_id INT(11) NOT NULL,
      tv_category_id INT(11) NOT NULL,
      PRIMARY KEY (tv_channel_id, tv_category_id),
      FOREIGN KEY (tv_channel_id) REFERENCES tv_channel(tv_channel_id) ON DELETE CASCADE,
      FOREIGN KEY (tv_category_id) REFERENCES tv_category(tv_category_id) ON DELETE CASCADE
  );

  5. Data Type Optimizations

  -- Change duration from VARCHAR to INT (seconds)
  ALTER TABLE content MODIFY COLUMN duration INT(11) COMMENT 'Duration in seconds';
  ALTER TABLE episode MODIFY COLUMN duration INT(11) COMMENT 'Duration in seconds';

  -- Fix typo in column name
  ALTER TABLE admob_config CHANGE COLUMN intersial_id interstitial_id VARCHAR(255) NOT NULL;

  -- Add constraints for better data integrity
  ALTER TABLE content ADD CONSTRAINT chk_content_type CHECK (type IN (0, 1, 2));
  ALTER TABLE content ADD CONSTRAINT chk_ratings CHECK (ratings >= 0 AND ratings <= 10);
  ALTER TABLE content_source ADD CONSTRAINT chk_source_type CHECK (type IN (0, 1, 2, 3, 4, 5, 6, 7));
  ALTER TABLE app_user ADD CONSTRAINT chk_login_type CHECK (login_type IN (0, 1, 2, 3, 4));

  6. Add Missing Business Logic Tables

  -- User viewing history/continue watching
  CREATE TABLE app_user_watch_history (
      watch_history_id INT(11) NOT NULL AUTO_INCREMENT,
      app_user_id INT(10) UNSIGNED NOT NULL,
      content_id INT(11) NULL,
      episode_id INT(11) NULL,
      last_watched_position INT(11) DEFAULT 0 COMMENT 'Position in seconds',
      total_duration INT(11) DEFAULT 0 COMMENT 'Total duration in seconds',
      completed TINYINT(1) DEFAULT 0,
      device_type TINYINT(1) DEFAULT 0,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (watch_history_id),
      UNIQUE KEY uk_user_content_episode (app_user_id, content_id, episode_id),
      INDEX idx_user_content (app_user_id, content_id),
      INDEX idx_user_episode (app_user_id, episode_id),
      INDEX idx_updated_at (updated_at),
      FOREIGN KEY (app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE,
      FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE,
      FOREIGN KEY (episode_id) REFERENCES episode(episode_id) ON DELETE CASCADE
  );

  -- User favorites
  CREATE TABLE app_user_favorite (
      app_user_id INT(10) UNSIGNED NOT NULL,
      content_id INT(11) NOT NULL,
      added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (app_user_id, content_id),
      INDEX idx_content_favorite (content_id),
      FOREIGN KEY (app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE,
      FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE
  );

  -- Content ratings by users
  CREATE TABLE app_user_rating (
      app_user_id INT(10) UNSIGNED NOT NULL,
      content_id INT(11) NOT NULL,
      rating FLOAT NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (app_user_id, content_id),
      INDEX idx_content_rating (content_id),
      CONSTRAINT chk_user_rating CHECK (rating >= 0 AND rating <= 10),
      FOREIGN KEY (app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE,
      FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE
  );

  7. Security & Audit Improvements

  -- Add missing fields for security
  ALTER TABLE admin_user ADD COLUMN email VARCHAR(255) UNIQUE AFTER user_name;
  ALTER TABLE admin_user ADD COLUMN last_login_at TIMESTAMP NULL;
  ALTER TABLE admin_user ADD COLUMN is_active TINYINT(1) DEFAULT 1;
  ALTER TABLE admin_user ADD COLUMN failed_login_attempts INT DEFAULT 0;

  -- Add audit fields
  ALTER TABLE app_user MODIFY updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
  ALTER TABLE admob_config MODIFY updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
  ALTER TABLE custom_ad MODIFY updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

  8. Consistency Issues

  -- Fix inconsistent timestamp columns
  ALTER TABLE episode_subtitle MODIFY updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
  ALTER TABLE subtitle MODIFY updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
  ALTER TABLE global_setting MODIFY updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

  -- Add unique constraints where needed
  ALTER TABLE app_language ADD UNIQUE KEY uk_code (code);
  ALTER TABLE genre ADD UNIQUE KEY uk_title (title);
  ALTER TABLE tv_category ADD UNIQUE KEY uk_title (title);








