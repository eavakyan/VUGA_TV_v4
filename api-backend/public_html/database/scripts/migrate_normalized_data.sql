-- Migration script to populate normalized tables from comma-separated data

-- Migrate genre data from comma-separated to content_genre table
-- First, clear any existing data
TRUNCATE TABLE content_genre;

-- Populate content_genre from genre_ids field
INSERT INTO content_genre (content_id, genre_id)
SELECT DISTINCT 
    c.content_id,
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(c.genre_ids, ',', numbers.n), ',', -1) AS UNSIGNED) AS genre_id
FROM content c
CROSS JOIN (
    SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
    UNION ALL SELECT 9 UNION ALL SELECT 10
) numbers
WHERE 
    CHAR_LENGTH(c.genre_ids) - CHAR_LENGTH(REPLACE(c.genre_ids, ',', '')) >= numbers.n - 1
    AND CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(c.genre_ids, ',', numbers.n), ',', -1) AS UNSIGNED) > 0
    AND EXISTS (
        SELECT 1 FROM genre g 
        WHERE g.genre_id = CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(c.genre_ids, ',', numbers.n), ',', -1) AS UNSIGNED)
    );

-- Migrate watchlist data from comma-separated to app_user_watchlist table
-- First, clear any existing data
TRUNCATE TABLE app_user_watchlist;

-- Populate app_user_watchlist from watchlist_content_ids field
INSERT INTO app_user_watchlist (app_user_id, content_id, added_at)
SELECT DISTINCT 
    u.app_user_id,
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(u.watchlist_content_ids, ',', numbers.n), ',', -1) AS UNSIGNED) AS content_id,
    NOW() AS added_at
FROM app_user u
CROSS JOIN (
    SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
    UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
    UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16
    UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) numbers
WHERE 
    u.watchlist_content_ids IS NOT NULL
    AND u.watchlist_content_ids != ''
    AND CHAR_LENGTH(u.watchlist_content_ids) - CHAR_LENGTH(REPLACE(u.watchlist_content_ids, ',', '')) >= numbers.n - 1
    AND CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(u.watchlist_content_ids, ',', numbers.n), ',', -1) AS UNSIGNED) > 0
    AND EXISTS (
        SELECT 1 FROM content c 
        WHERE c.content_id = CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(u.watchlist_content_ids, ',', numbers.n), ',', -1) AS UNSIGNED)
    );

-- Migrate TV channel categories from comma-separated to tv_channel_category table
-- First, clear any existing data
TRUNCATE TABLE tv_channel_category;

-- Populate tv_channel_category from category_ids field
INSERT INTO tv_channel_category (tv_channel_id, tv_category_id)
SELECT DISTINCT 
    tc.tv_channel_id,
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(tc.category_ids, ',', numbers.n), ',', -1) AS UNSIGNED) AS tv_category_id
FROM tv_channel tc
CROSS JOIN (
    SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
    UNION ALL SELECT 9 UNION ALL SELECT 10
) numbers
WHERE 
    tc.category_ids IS NOT NULL
    AND tc.category_ids != ''
    AND CHAR_LENGTH(tc.category_ids) - CHAR_LENGTH(REPLACE(tc.category_ids, ',', '')) >= numbers.n - 1
    AND CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(tc.category_ids, ',', numbers.n), ',', -1) AS UNSIGNED) > 0
    AND EXISTS (
        SELECT 1 FROM tv_category cat 
        WHERE cat.tv_category_id = CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(tc.category_ids, ',', numbers.n), ',', -1) AS UNSIGNED)
    );

-- Verify the migration
SELECT 'Content Genres:' AS Migration, COUNT(*) AS Count FROM content_genre
UNION ALL
SELECT 'User Watchlists:', COUNT(*) FROM app_user_watchlist
UNION ALL
SELECT 'TV Channel Categories:', COUNT(*) FROM tv_channel_category;