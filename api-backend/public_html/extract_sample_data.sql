-- Extract Sample Movie Data
-- Run this query to get a complete movie record with all related data

SELECT 
    c.*,
    l.title as language_title,
    l.code as language_code
FROM contents c
LEFT JOIN languages l ON c.language_id = l.id
WHERE c.type = 1 AND c.is_show = 1
LIMIT 1;

-- Get genres for the movie
SELECT g.* 
FROM genres g
WHERE FIND_IN_SET(g.id, (
    SELECT genre_ids FROM contents WHERE type = 1 AND is_show = 1 LIMIT 1
));

-- Get sources for the movie
SELECT cs.*
FROM content_sources cs
JOIN contents c ON cs.content_id = c.id
WHERE c.type = 1 AND c.is_show = 1
LIMIT 1;

-- Get subtitles for the movie
SELECT s.*, l.title as language_title, l.code as language_code
FROM subtitles s
JOIN languages l ON s.language_id = l.id
JOIN contents c ON s.content_id = c.id
WHERE c.type = 1 AND c.is_show = 1
LIMIT 1;

-- Get cast for the movie
SELECT cc.*, a.fullname, a.profile_image
FROM content_casts cc
JOIN actors a ON cc.actor_id = a.id
JOIN contents c ON cc.content_id = c.id
WHERE c.type = 1 AND c.is_show = 1
LIMIT 1;

-- Extract Sample TV Series Data
SELECT 
    c.*,
    l.title as language_title,
    l.code as language_code
FROM contents c
LEFT JOIN languages l ON c.language_id = l.id
WHERE c.type = 2 AND c.is_show = 1
LIMIT 1;

-- Get seasons for the series
SELECT s.*
FROM seasons s
JOIN contents c ON s.content_id = c.id
WHERE c.type = 2 AND c.is_show = 1
LIMIT 1;

-- Get episodes for the first season
SELECT e.*
FROM episodes e
JOIN seasons s ON e.season_id = s.id
JOIN contents c ON s.content_id = c.id
WHERE c.type = 2 AND c.is_show = 1
ORDER BY e.episode_number
LIMIT 3;

-- Get episode sources
SELECT es.*
FROM episode_sources es
JOIN episodes e ON es.episode_id = e.id
JOIN seasons s ON e.season_id = s.id
JOIN contents c ON s.content_id = c.id
WHERE c.type = 2 AND c.is_show = 1
LIMIT 5;

-- Get episode subtitles
SELECT est.*, l.title as language_title, l.code as language_code
FROM episode_subtitles est
JOIN languages l ON est.language_id = l.id
JOIN episodes e ON est.episode_id = e.id
JOIN seasons s ON e.season_id = s.id
JOIN contents c ON s.content_id = c.id
WHERE c.type = 2 AND c.is_show = 1
LIMIT 5;

-- Extract Actor/Cast Data
SELECT a.*
FROM actors a
WHERE a.id IN (
    SELECT DISTINCT actor_id 
    FROM content_casts 
    WHERE content_id IN (
        SELECT id FROM contents WHERE is_show = 1 LIMIT 5
    )
)
LIMIT 3; 