-- Complete Migration Script to Profile-Based System
-- This script migrates watchlist, favorites, ratings, and watch history from user-based to profile-based

-- 1. Create new profile-based tables if they don't exist

-- Watchlist table (profile-based)
CREATE TABLE IF NOT EXISTS app_user_watchlist (
    id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    profile_id INT(11) UNSIGNED NOT NULL,
    content_id INT(11) UNSIGNED NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY unique_profile_content (profile_id, content_id),
    KEY idx_profile_id (profile_id),
    KEY idx_content_id (content_id),
    CONSTRAINT app_user_watchlist_profile_fk FOREIGN KEY (profile_id) REFERENCES app_user_profile(profile_id) ON DELETE CASCADE,
    CONSTRAINT app_user_watchlist_content_fk FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Favorites table (profile-based)
CREATE TABLE IF NOT EXISTS app_user_favorite (
    id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    profile_id INT(11) UNSIGNED NOT NULL,
    content_id INT(11) UNSIGNED NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY unique_profile_content (profile_id, content_id),
    KEY idx_profile_id (profile_id),
    KEY idx_content_id (content_id),
    CONSTRAINT app_user_favorite_profile_fk FOREIGN KEY (profile_id) REFERENCES app_user_profile(profile_id) ON DELETE CASCADE,
    CONSTRAINT app_user_favorite_content_fk FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ratings table (profile-based)
CREATE TABLE IF NOT EXISTS app_user_rating (
    id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    profile_id INT(11) UNSIGNED NOT NULL,
    content_id INT(11) UNSIGNED NOT NULL,
    rating DECIMAL(3,1) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY unique_profile_content (profile_id, content_id),
    KEY idx_profile_id (profile_id),
    KEY idx_content_id (content_id),
    CONSTRAINT app_user_rating_profile_fk FOREIGN KEY (profile_id) REFERENCES app_user_profile(profile_id) ON DELETE CASCADE,
    CONSTRAINT app_user_rating_content_fk FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Modify watch history table to use profile_id
ALTER TABLE app_user_watch_history 
    ADD COLUMN profile_id INT(11) UNSIGNED NULL AFTER watch_history_id,
    ADD KEY idx_profile_id (profile_id);

-- 3. Migrate existing data to profile-based tables

-- Migrate watchlist data (from user's watchlist_content_ids to profile-based)
INSERT INTO app_user_watchlist (profile_id, content_id, created_at, updated_at)
SELECT 
    p.profile_id,
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(u.watchlist_content_ids, ',', n.n), ',', -1) AS UNSIGNED) as content_id,
    NOW(),
    NOW()
FROM app_user u
INNER JOIN app_user_profile p ON p.app_user_id = u.app_user_id AND p.is_active = 1
INNER JOIN (
    SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL 
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
    SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL
    SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL
    SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) n ON CHAR_LENGTH(u.watchlist_content_ids) - CHAR_LENGTH(REPLACE(u.watchlist_content_ids, ',', '')) >= n.n - 1
WHERE u.watchlist_content_ids IS NOT NULL AND u.watchlist_content_ids != ''
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Migrate favorites (if user_favorite table exists)
INSERT INTO app_user_favorite (profile_id, content_id, created_at, updated_at)
SELECT 
    p.profile_id,
    uf.content_id,
    uf.created_at,
    uf.updated_at
FROM user_favorite uf
INNER JOIN app_user_profile p ON p.app_user_id = uf.user_id AND p.is_active = 1
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Migrate ratings (if user_rating table exists)
INSERT INTO app_user_rating (profile_id, content_id, rating, created_at, updated_at)
SELECT 
    p.profile_id,
    ur.content_id,
    ur.rating,
    ur.created_at,
    ur.updated_at
FROM user_rating ur
INNER JOIN app_user_profile p ON p.app_user_id = ur.user_id AND p.is_active = 1
ON DUPLICATE KEY UPDATE rating = ur.rating, updated_at = NOW();

-- Migrate watch history
UPDATE app_user_watch_history wh
INNER JOIN app_user_profile p ON p.app_user_id = wh.app_user_id AND p.is_active = 1
SET wh.profile_id = p.profile_id
WHERE wh.profile_id IS NULL;

-- 4. Add foreign key constraint for watch history
ALTER TABLE app_user_watch_history
    ADD CONSTRAINT app_user_watch_history_profile_fk 
    FOREIGN KEY (profile_id) REFERENCES app_user_profile(profile_id) ON DELETE CASCADE;

-- 5. Create or update views for backward compatibility and reporting

-- User watchlist view (for admin panel)
CREATE OR REPLACE VIEW user_watchlist_view AS
SELECT 
    u.app_user_id,
    u.email,
    u.fullname,
    p.profile_id,
    p.name as profile_name,
    GROUP_CONCAT(w.content_id) as watchlist_content_ids,
    COUNT(w.content_id) as watchlist_count
FROM app_user u
LEFT JOIN app_user_profile p ON u.app_user_id = p.app_user_id
LEFT JOIN app_user_watchlist w ON p.profile_id = w.profile_id
GROUP BY u.app_user_id, p.profile_id;

-- User favorites view
CREATE OR REPLACE VIEW user_favorites_view AS
SELECT 
    u.app_user_id,
    u.email,
    u.fullname,
    p.profile_id,
    p.name as profile_name,
    GROUP_CONCAT(f.content_id) as favorite_content_ids,
    COUNT(f.content_id) as favorites_count
FROM app_user u
LEFT JOIN app_user_profile p ON u.app_user_id = p.app_user_id
LEFT JOIN app_user_favorite f ON p.profile_id = f.profile_id
GROUP BY u.app_user_id, p.profile_id;

-- User ratings view
CREATE OR REPLACE VIEW user_ratings_view AS
SELECT 
    u.app_user_id,
    u.email,
    u.fullname,
    p.profile_id,
    p.name as profile_name,
    r.content_id,
    r.rating,
    c.title as content_title
FROM app_user u
INNER JOIN app_user_profile p ON u.app_user_id = p.app_user_id
INNER JOIN app_user_rating r ON p.profile_id = r.profile_id
INNER JOIN content c ON r.content_id = c.content_id;

-- Content statistics view (including profile-based data)
CREATE OR REPLACE VIEW content_statistics_view AS
SELECT 
    c.content_id,
    c.title,
    c.type,
    c.total_view,
    c.total_download,
    c.total_share,
    COUNT(DISTINCT w.profile_id) as watchlist_count,
    COUNT(DISTINCT f.profile_id) as favorite_count,
    AVG(r.rating) as average_rating,
    COUNT(DISTINCT r.profile_id) as rating_count
FROM content c
LEFT JOIN app_user_watchlist w ON c.content_id = w.content_id
LEFT JOIN app_user_favorite f ON c.content_id = f.content_id
LEFT JOIN app_user_rating r ON c.content_id = r.content_id
GROUP BY c.content_id;

-- Profile activity view
CREATE OR REPLACE VIEW profile_activity_view AS
SELECT 
    p.profile_id,
    p.app_user_id,
    p.name as profile_name,
    p.is_kids,
    COUNT(DISTINCT w.content_id) as watchlist_count,
    COUNT(DISTINCT f.content_id) as favorite_count,
    COUNT(DISTINCT r.content_id) as rated_count,
    COUNT(DISTINCT wh.content_id) as watch_history_count,
    MAX(wh.updated_at) as last_watched_at
FROM app_user_profile p
LEFT JOIN app_user_watchlist w ON p.profile_id = w.profile_id
LEFT JOIN app_user_favorite f ON p.profile_id = f.profile_id
LEFT JOIN app_user_rating r ON p.profile_id = r.profile_id
LEFT JOIN app_user_watch_history wh ON p.profile_id = wh.profile_id
WHERE p.is_active = 1
GROUP BY p.profile_id;

-- 6. Update the admin compatibility view to include profile data
CREATE OR REPLACE VIEW user AS 
SELECT 
    u.app_user_id AS user_id,
    u.fullname,
    u.email,
    u.login_type,
    u.identity,
    u.profile_image,
    GROUP_CONCAT(DISTINCT w.content_id) AS watchlist_content_ids,
    u.device_type,
    u.device_token,
    u.created_at,
    u.updated_at,
    u.last_active_profile_id,
    COUNT(DISTINCT p.profile_id) as profile_count
FROM app_user u
LEFT JOIN app_user_profile p ON u.app_user_id = p.app_user_id AND p.is_active = 1
LEFT JOIN app_user_watchlist w ON p.profile_id = w.profile_id
GROUP BY u.app_user_id;

-- 7. Clean up old columns (commented out for safety - run manually after verification)
-- ALTER TABLE app_user DROP COLUMN watchlist_content_ids;
-- ALTER TABLE app_user_watch_history DROP COLUMN app_user_id;
-- DROP TABLE IF EXISTS user_favorite;
-- DROP TABLE IF EXISTS user_rating;

-- 8. Update content ratings from profile ratings
UPDATE content c
SET c.ratings = (
    SELECT AVG(r.rating)
    FROM app_user_rating r
    WHERE r.content_id = c.content_id
)
WHERE EXISTS (
    SELECT 1 
    FROM app_user_rating r 
    WHERE r.content_id = c.content_id
);