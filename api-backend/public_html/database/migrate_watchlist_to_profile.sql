-- Migration script to change watchlist from user-based to profile-based
-- WARNING: Make a backup of your database before running this script!

-- Step 1: Create temporary table to store existing watchlist data
CREATE TEMPORARY TABLE temp_watchlist AS 
SELECT * FROM app_user_watchlist;

-- Step 2: Drop the existing foreign key constraint
ALTER TABLE app_user_watchlist 
DROP FOREIGN KEY app_user_watchlist_ibfk_1;

-- Step 3: Drop the app_user_id column
ALTER TABLE app_user_watchlist 
DROP COLUMN app_user_id;

-- Step 4: Add profile_id column
ALTER TABLE app_user_watchlist 
ADD COLUMN profile_id BIGINT UNSIGNED NOT NULL AFTER id;

-- Step 5: Add foreign key constraint to app_user_profile
ALTER TABLE app_user_watchlist 
ADD CONSTRAINT app_user_watchlist_profile_fk 
FOREIGN KEY (profile_id) REFERENCES app_user_profile(profile_id) ON DELETE CASCADE;

-- Step 6: Add unique constraint to prevent duplicate entries
ALTER TABLE app_user_watchlist 
ADD UNIQUE KEY unique_profile_content (profile_id, content_id);

-- Step 7: Migrate existing data to use the user's last active profile
INSERT INTO app_user_watchlist (profile_id, content_id, created_at, updated_at)
SELECT 
    u.last_active_profile_id,
    t.content_id,
    t.created_at,
    t.updated_at
FROM temp_watchlist t
INNER JOIN app_user u ON u.app_user_id = t.app_user_id
WHERE u.last_active_profile_id IS NOT NULL
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

-- Step 8: Drop the temporary table
DROP TEMPORARY TABLE temp_watchlist;

-- Verify the migration
SELECT 
    'Migration completed. Watchlist entries count:' as message,
    COUNT(*) as count 
FROM app_user_watchlist;