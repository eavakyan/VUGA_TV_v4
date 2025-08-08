-- ====================================
-- Migrate existing trailer data from content.trailer_url to content_trailer table
-- Purpose: Preserve existing trailer data during migration
-- Date: 2025-01-08
-- ====================================

-- Insert existing trailer data into new table
-- This handles various formats: YouTube URLs, YouTube IDs, etc.
INSERT INTO `content_trailer` (
    `content_id`, 
    `title`, 
    `youtube_id`, 
    `trailer_url`, 
    `is_primary`, 
    `sort_order`
)
SELECT 
    c.content_id,
    CONCAT('Official Trailer - ', c.title) as title,
    -- Extract YouTube ID from various URL formats
    CASE 
        -- Full YouTube URL: https://www.youtube.com/watch?v=VIDEO_ID
        WHEN c.trailer_url LIKE '%youtube.com/watch?v=%' THEN 
            SUBSTRING_INDEX(SUBSTRING_INDEX(c.trailer_url, 'v=', -1), '&', 1)
        -- YouTube short URL: https://youtu.be/VIDEO_ID
        WHEN c.trailer_url LIKE '%youtu.be/%' THEN 
            SUBSTRING_INDEX(SUBSTRING_INDEX(c.trailer_url, 'youtu.be/', -1), '?', 1)
        -- Embedded YouTube URL: https://www.youtube.com/embed/VIDEO_ID
        WHEN c.trailer_url LIKE '%youtube.com/embed/%' THEN 
            SUBSTRING_INDEX(SUBSTRING_INDEX(c.trailer_url, 'embed/', -1), '?', 1)
        -- Already just the YouTube ID (11 characters, alphanumeric with dash/underscore)
        WHEN LENGTH(c.trailer_url) = 11 AND c.trailer_url REGEXP '^[A-Za-z0-9_-]{11}$' THEN 
            c.trailer_url
        -- Default: assume it's a YouTube ID
        ELSE c.trailer_url
    END as youtube_id,
    -- Store the full URL or construct it if needed
    CASE 
        WHEN c.trailer_url LIKE 'http%' THEN c.trailer_url
        ELSE CONCAT('https://www.youtube.com/watch?v=', c.trailer_url)
    END as trailer_url,
    1 as is_primary, -- Mark as primary trailer
    0 as sort_order  -- First in order
FROM `content` c
WHERE c.trailer_url IS NOT NULL 
  AND c.trailer_url != ''
  AND c.trailer_url != '0'
  AND LENGTH(TRIM(c.trailer_url)) > 0;

-- Show migration results
SELECT 
    'Migration Summary' as info,
    COUNT(*) as total_trailers_migrated
FROM `content_trailer`;

-- Show any potential issues
SELECT 
    'Potential Issues' as info,
    COUNT(*) as contents_with_invalid_trailer_urls
FROM `content` c
WHERE c.trailer_url IS NOT NULL 
  AND c.trailer_url != ''
  AND c.trailer_url != '0'
  AND LENGTH(TRIM(c.trailer_url)) > 0
  AND c.content_id NOT IN (
      SELECT DISTINCT content_id FROM `content_trailer`
  );