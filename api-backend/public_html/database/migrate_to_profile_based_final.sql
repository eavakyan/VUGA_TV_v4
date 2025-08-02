-- Complete Migration Script to Profile-Based System
-- This script migrates favorites, ratings, and watch history from user-based to profile-based

-- 1. Migrate favorites from app_user_favorite to app_profile_favorite
INSERT INTO app_profile_favorite (profile_id, content_id, added_at)
SELECT 
    p.profile_id,
    uf.content_id,
    uf.added_at
FROM app_user_favorite uf
INNER JOIN app_user_profile p ON p.app_user_id = uf.app_user_id
WHERE p.is_active = 1
ON DUPLICATE KEY UPDATE added_at = uf.added_at;

-- 2. Migrate ratings from app_user_rating to app_profile_rating  
INSERT INTO app_profile_rating (profile_id, content_id, rating, created_at, updated_at)
SELECT 
    p.profile_id,
    ur.content_id,
    ur.rating,
    ur.created_at,
    ur.updated_at
FROM app_user_rating ur
INNER JOIN app_user_profile p ON p.app_user_id = ur.app_user_id
WHERE p.is_active = 1
ON DUPLICATE KEY UPDATE 
    rating = ur.rating,
    updated_at = ur.updated_at;

-- 3. Add profile_id column to app_user_watch_history
ALTER TABLE app_user_watch_history 
    ADD COLUMN profile_id INT(11) NULL AFTER watch_history_id,
    ADD KEY idx_profile_id (profile_id);

-- 4. Migrate watch history to use profile_id
UPDATE app_user_watch_history wh
INNER JOIN app_user_profile p ON p.app_user_id = wh.app_user_id
SET wh.profile_id = p.profile_id
WHERE wh.profile_id IS NULL 
  AND p.is_active = 1;

-- 5. Add foreign key constraint for profile_id
ALTER TABLE app_user_watch_history
    ADD CONSTRAINT app_user_watch_history_profile_fk 
    FOREIGN KEY (profile_id) REFERENCES app_user_profile(profile_id) ON DELETE CASCADE;

-- 6. Create views for backward compatibility and reporting

-- User activity summary view
CREATE OR REPLACE VIEW user_activity_summary AS
SELECT 
    u.app_user_id,
    u.email,
    u.fullname,
    p.profile_id,
    p.name as profile_name,
    p.is_kids,
    (SELECT COUNT(*) FROM app_user_watchlist w WHERE w.profile_id = p.profile_id) as watchlist_count,
    (SELECT COUNT(*) FROM app_profile_favorite f WHERE f.profile_id = p.profile_id) as favorite_count,
    (SELECT COUNT(*) FROM app_profile_rating r WHERE r.profile_id = p.profile_id) as rated_count,
    (SELECT COUNT(*) FROM app_user_watch_history wh WHERE wh.profile_id = p.profile_id) as watch_history_count,
    (SELECT MAX(wh.updated_at) FROM app_user_watch_history wh WHERE wh.profile_id = p.profile_id) as last_watched_at
FROM app_user u
INNER JOIN app_user_profile p ON u.app_user_id = p.app_user_id
WHERE p.is_active = 1;

-- Content statistics view
CREATE OR REPLACE VIEW content_statistics AS
SELECT 
    c.content_id,
    c.title,
    c.type,
    c.total_view,
    c.total_download,
    c.total_share,
    (SELECT COUNT(DISTINCT profile_id) FROM app_user_watchlist WHERE content_id = c.content_id) as watchlist_count,
    (SELECT COUNT(DISTINCT profile_id) FROM app_profile_favorite WHERE content_id = c.content_id) as favorite_count,
    (SELECT AVG(rating) FROM app_profile_rating WHERE content_id = c.content_id) as average_rating,
    (SELECT COUNT(DISTINCT profile_id) FROM app_profile_rating WHERE content_id = c.content_id) as rating_count,
    (SELECT COUNT(DISTINCT profile_id) FROM app_user_watch_history WHERE content_id = c.content_id) as unique_viewers
FROM content c;

-- Profile watch progress view
CREATE OR REPLACE VIEW profile_watch_progress AS
SELECT 
    wh.profile_id,
    p.name as profile_name,
    wh.content_id,
    c.title as content_title,
    wh.episode_id,
    e.title as episode_title,
    wh.last_watched_position,
    wh.total_duration,
    CASE 
        WHEN wh.completed = 1 THEN 100
        WHEN wh.total_duration > 0 THEN ROUND((wh.last_watched_position / wh.total_duration) * 100, 2)
        ELSE 0
    END as progress_percentage,
    wh.completed,
    wh.updated_at as last_watched_at
FROM app_user_watch_history wh
INNER JOIN app_user_profile p ON wh.profile_id = p.profile_id
LEFT JOIN content c ON wh.content_id = c.content_id
LEFT JOIN episode e ON wh.episode_id = e.episode_id
WHERE p.is_active = 1;

-- 7. Update content ratings from profile ratings
UPDATE content c
SET c.ratings = (
    SELECT AVG(r.rating)
    FROM app_profile_rating r
    WHERE r.content_id = c.content_id
)
WHERE EXISTS (
    SELECT 1 
    FROM app_profile_rating r 
    WHERE r.content_id = c.content_id
);

-- 8. Data cleanup queries (run these manually after verifying the migration)
-- DROP TABLE app_user_favorite_backup;
-- DROP TABLE app_user_rating_backup;
-- DROP TABLE app_user_watch_history_backup;
-- ALTER TABLE app_user_watch_history DROP COLUMN app_user_id;