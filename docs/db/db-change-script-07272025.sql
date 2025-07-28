
--As a DBA, here are my recommendations for improving this schema:

 -- 1. Add Missing Indexes
    -- ! add this after renaming tables and columns
  -- High-impact indexes for query performance
  CREATE INDEX idx_contents_genre_ids ON contents(genre_ids);
  CREATE INDEX idx_contents_language_id ON contents(language_id);
  CREATE INDEX idx_contents_type_featured_show ON contents(type, is_featured, is_show);
  CREATE INDEX idx_contents_release_year ON contents(release_year);

  CREATE INDEX idx_content_sources_content_id ON content_sources(content_id);
  CREATE INDEX idx_content_sources_access_type ON content_sources(access_type);

  CREATE INDEX idx_episodes_season_id ON episodes(season_id);
  CREATE INDEX idx_episode_sources_episode_id ON episode_sources(episode_id);

  CREATE INDEX idx_seasons_content_id ON seasons(content_id);
  CREATE INDEX idx_content_cast_content_actor ON content_cast(content_id, actor_id);

  CREATE INDEX idx_users_email ON users(email);
  CREATE INDEX idx_users_device_type ON users(device_type);

  CREATE INDEX idx_tv_channels_category_ids ON tv_channels(category_ids(255));

--  2. Add Missing Foreign Key Constraints
-- ! maybe....
  ALTER TABLE content_sources
  ADD CONSTRAINT fk_content_sources_content
  FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE CASCADE;

  ALTER TABLE content_cast
  ADD CONSTRAINT fk_content_cast_content
  FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE CASCADE;

  ALTER TABLE content_cast
  ADD CONSTRAINT fk_content_cast_actor
  FOREIGN KEY (actor_id) REFERENCES actors(id) ON DELETE CASCADE;

  ALTER TABLE contents
  ADD CONSTRAINT fk_contents_language
  FOREIGN KEY (language_id) REFERENCES languages(id);

  ALTER TABLE seasons
  ADD CONSTRAINT fk_seasons_content
  FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE CASCADE;

  ALTER TABLE episodes
  ADD CONSTRAINT fk_episodes_season
  FOREIGN KEY (season_id) REFERENCES seasons(id) ON DELETE CASCADE;

  ALTER TABLE episode_sources
  ADD CONSTRAINT fk_episode_sources_episode
  FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE;

  ALTER TABLE subtitles
  ADD CONSTRAINT fk_subtitles_language
  FOREIGN KEY (language_id) REFERENCES languages(id);

  ALTER TABLE episode_subtitles
  ADD CONSTRAINT fk_episode_subtitles_episode
  FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE;

  ALTER TABLE episode_subtitles
  ADD CONSTRAINT fk_episode_subtitles_language
  FOREIGN KEY (language_id) REFERENCES languages(id);

--  3. Data Type and Structure Improvements

  -- Fix inconsistent ID types
  ALTER TABLE content_cast MODIFY content_id INT(11) NOT NULL;
  ALTER TABLE seasons MODIFY content_id INT(11) NOT NULL;
  ALTER TABLE episodes MODIFY season_id INT(11) NOT NULL;

  -- Change genre_ids to proper many-to-many relationship
  CREATE TABLE content_genres (
      content_id INT(11) NOT NULL,
      genre_id INT(11) NOT NULL,
      PRIMARY KEY (content_id, genre_id),
      FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE CASCADE,
      FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
  );

  -- Convert watchlist to proper table
  CREATE TABLE user_watchlists (
      user_id INT(10) UNSIGNED NOT NULL,
      content_id INT(11) NOT NULL,
      added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (user_id, content_id),
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
      FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE CASCADE
  );

  -- Convert TV channel categories to proper many-to-many
  CREATE TABLE tv_channel_categories (
      channel_id INT(11) NOT NULL,
      category_id INT(11) NOT NULL,
      PRIMARY KEY (channel_id, category_id),
      FOREIGN KEY (channel_id) REFERENCES tv_channels(id) ON DELETE CASCADE,
      FOREIGN KEY (category_id) REFERENCES tv_categories(id) ON DELETE CASCADE
  );

--  4. Add User Activity Tracking Tables
-- ! implies code change to track user activity
  CREATE TABLE user_watch_history (
      id INT(11) NOT NULL AUTO_INCREMENT,
      user_id INT(10) UNSIGNED NOT NULL,
      content_id INT(11) NULL,
      episode_id INT(11) NULL,
      watch_duration INT(11) DEFAULT 0,
      total_duration INT(11) DEFAULT 0,
      last_position INT(11) DEFAULT 0,
      completed TINYINT(1) DEFAULT 0,
      device_type TINYINT(1) DEFAULT 0,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      INDEX idx_user_content (user_id, content_id),
      INDEX idx_user_episode (user_id, episode_id),
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
      FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE CASCADE,
      FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE
  );

  CREATE TABLE user_favorites (
      user_id INT(10) UNSIGNED NOT NULL,
      content_id INT(11) NOT NULL,
      added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (user_id, content_id),
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
      FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE CASCADE
  );

--  5. Security and Data Validation Improvements

  -- Add constraints for better data integrity
  --ALTER TABLE users ADD CONSTRAINT chk_login_type CHECK (login_type IN (0, 1, 2, 3, 4));
  --ALTER TABLE contents ADD CONSTRAINT chk_content_type CHECK (type IN (0, 1, 2));
  --ALTER TABLE contents ADD CONSTRAINT chk_ratings CHECK (ratings >= 0 AND ratings <= 10);
  --ALTER TABLE content_sources ADD CONSTRAINT chk_source_type CHECK (type IN (0, 1, 2, 3, 4, 5, 6, 7));

  -- Add unique constraints where needed
  ALTER TABLE users ADD UNIQUE KEY uk_email_login_type (email, login_type);
  ALTER TABLE languages ADD UNIQUE KEY uk_code (code);
  ALTER TABLE genres ADD UNIQUE KEY uk_title (title);

--   6. Performance Optimization Tables

  -- Create materialized view for content statistics
  -- ! implies code change to populate this table periodically
  CREATE TABLE content_statistics (
      content_id INT(11) PRIMARY KEY,
      total_views INT(11) DEFAULT 0,
      total_downloads INT(11) DEFAULT 0,
      total_shares INT(11) DEFAULT 0,
      avg_rating FLOAT DEFAULT 0,
      rating_count INT(11) DEFAULT 0,
      last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE CASCADE
  );

  -- Create search optimization table
  -- This table will store precomputed search text for full-text search
  -- ! implies code change to populate this table 
  CREATE TABLE content_search (
      content_id INT(11) PRIMARY KEY,
      search_text TEXT,
      FULLTEXT(search_text),
      FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE CASCADE
  );

--  7. Audit and Logging Tables
-- ! implies code change to log changes
  CREATE TABLE audit_log (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      table_name VARCHAR(64) NOT NULL,
      record_id INT(11) NOT NULL,
      action ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
      user_id INT(10) UNSIGNED,
      old_values JSON,
      new_values JSON,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      INDEX idx_table_record (table_name, record_id),
      INDEX idx_created_at (created_at)
  );

--  8. Schema Normalization Issues

  -- Normalize admin_user table
  -- ALTER TABLE admin_user ADD COLUMN email VARCHAR(255) UNIQUE AFTER user_name;
  ALTER TABLE admin_user ADD COLUMN last_login TIMESTAMP NULL;  -- ! implies code change to track logins
  ALTER TABLE admin_user ADD COLUMN is_active TINYINT(1) DEFAULT 1; -- ! implies code change to manage active status
  ALTER TABLE admin_user MODIFY user_password VARCHAR(255) NOT NULL COMMENT 'Should store hashed passwords'; -- ! implies code change to hash passwords

  -- Add missing timestamps
--  ALTER TABLE users MODIFY updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

--  9. Data Type Optimizations

  -- Optimize string lengths
  ALTER TABLE contents MODIFY duration INT(11) COMMENT 'Duration in seconds';
  ALTER TABLE episodes MODIFY duration INT(11) COMMENT 'Duration in seconds';
  ALTER TABLE actors MODIFY bio TEXT;
  ALTER TABLE contents MODIFY description TEXT;
  ALTER TABLE episodes MODIFY description TEXT;

--  10. Add Table Comments for Documentation

  ALTER TABLE contents COMMENT = 'Main content table for movies and series';
  ALTER TABLE users COMMENT = 'Registered user accounts';
  ALTER TABLE tv_auth_sessions COMMENT = 'TV device authentication sessions';
  ALTER TABLE content_sources COMMENT = 'Video sources for content items';
  ALTER TABLE episodes COMMENT = 'TV series episodes';
