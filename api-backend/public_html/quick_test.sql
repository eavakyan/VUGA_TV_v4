-- Quick test to see what content you have
-- Run this first to see your data structure

SELECT 
    id,
    title,
    type,
    vertical_poster,
    horizontal_poster,
    ratings,
    release_year,
    genre_ids,
    language_id,
    total_view,
    is_featured,
    is_show,
    created_at
FROM contents 
WHERE is_show = 1 
ORDER BY total_view DESC 
LIMIT 3;

-- Show me what this returns and I'll build the MongoDB documents with your actual data! 