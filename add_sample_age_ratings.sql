-- Sample Age Ratings for Testing
-- This script assigns age ratings to existing content for testing purposes

-- First, let's see what content exists
SELECT content_id, title, type FROM content WHERE is_show = 1 LIMIT 10;

-- Assign age ratings to content (adjust content_ids based on what exists in your system)
-- You can run the SELECT above first to see what content_ids you have

-- Example assignments (adjust content_ids as needed):

-- Assign some content as suitable for all ages (0-6)
INSERT IGNORE INTO content_age_limit (content_id, age_limit_id) VALUES
(1, 1),  -- Content ID 1 -> 0-6 years
(2, 1);  -- Content ID 2 -> 0-6 years

-- Assign some content for children (7-12)
INSERT IGNORE INTO content_age_limit (content_id, age_limit_id) VALUES
(3, 2),  -- Content ID 3 -> 7-12 years
(4, 2);  -- Content ID 4 -> 7-12 years

-- Assign some content for young teens (13-16)
INSERT IGNORE INTO content_age_limit (content_id, age_limit_id) VALUES
(5, 3),  -- Content ID 5 -> 13-16 years
(6, 3);  -- Content ID 6 -> 13-16 years

-- Assign some content for older teens (17-18)
INSERT IGNORE INTO content_age_limit (content_id, age_limit_id) VALUES
(7, 4),  -- Content ID 7 -> 17-18 years
(8, 4);  -- Content ID 8 -> 17-18 years

-- Assign some content for adults only (18+)
INSERT IGNORE INTO content_age_limit (content_id, age_limit_id) VALUES
(9, 5),   -- Content ID 9 -> 18+ years
(10, 5),  -- Content ID 10 -> 18+ years
(11, 5),  -- Content ID 11 -> 18+ years
(12, 5),  -- Content ID 12 -> 18+ years
(13, 5);  -- Content ID 13 -> 18+ years

-- Verify the assignments
SELECT 
    c.content_id,
    c.title,
    al.name as age_group,
    al.code,
    al.min_age,
    al.max_age
FROM content c
JOIN content_age_limit cal ON c.content_id = cal.content_id
JOIN age_limit al ON cal.age_limit_id = al.age_limit_id
ORDER BY c.content_id;

-- Count content by age group
SELECT 
    al.name as age_group,
    al.code,
    COUNT(cal.content_id) as content_count
FROM age_limit al
LEFT JOIN content_age_limit cal ON al.age_limit_id = cal.age_limit_id
GROUP BY al.age_limit_id
ORDER BY al.min_age;