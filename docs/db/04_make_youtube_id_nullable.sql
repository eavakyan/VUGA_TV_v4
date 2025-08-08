-- Make youtube_id column nullable to support non-YouTube trailer URLs
-- This allows the system to handle MP4, HLS, and other video formats

-- STEP 1: Check current state
SELECT 
    COUNT(*) as total_trailers,
    COUNT(youtube_id) as trailers_with_youtube_id,
    COUNT(*) - COUNT(youtube_id) as trailers_without_youtube_id
FROM content_trailer;

-- STEP 2: Make youtube_id column nullable
ALTER TABLE `content_trailer` 
MODIFY COLUMN `youtube_id` varchar(20) NULL;

-- STEP 3: Verify the change
DESCRIBE content_trailer;

-- STEP 4: Show column definition
SHOW COLUMNS FROM content_trailer WHERE Field = 'youtube_id';

-- This migration allows trailers to be:
-- 1. YouTube videos (youtube_id will be extracted and stored)
-- 2. Direct MP4 files (youtube_id will be null)
-- 3. HLS/M3U8 streams (youtube_id will be null)
-- 4. Any other video URL format (youtube_id will be null)