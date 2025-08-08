# Content Trailer Migration Summary

## Overview

This migration converts the single `trailer_url` field in the `content` table to a one-to-many relationship using a new `content_trailer` table. This allows multiple trailers per content item and provides better management capabilities.

## Database Changes

### New Table: `content_trailer`

```sql
CREATE TABLE `content_trailer` (
  `content_trailer_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` int(11) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `youtube_id` varchar(20) NOT NULL,
  `trailer_url` varchar(500) NOT NULL,
  `is_primary` tinyint(1) DEFAULT 0,
  `sort_order` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`content_trailer_id`),
  KEY `idx_content_trailer_content` (`content_id`),
  KEY `idx_content_trailer_primary` (`content_id`, `is_primary`),
  KEY `idx_content_trailer_sort` (`content_id`, `sort_order`),
  CONSTRAINT `fk_content_trailer_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### Fields Explanation

- **content_trailer_id**: Auto-increment primary key
- **content_id**: Foreign key to content table
- **title**: Trailer title/description (e.g., "Official Trailer", "Teaser")
- **youtube_id**: YouTube video ID extracted from URL
- **trailer_url**: Full YouTube URL (for backward compatibility)
- **is_primary**: Boolean flag indicating the primary trailer (only one per content)
- **sort_order**: Display order for multiple trailers
- **created_at/updated_at**: Standard timestamps

## Migration Process

### Step 1: Run SQL Scripts

1. **01_create_content_trailer_table.sql** - Creates the new table with indexes
2. **02_migrate_existing_trailer_data.sql** - Migrates existing trailer data
3. **03_remove_old_trailer_url_column.sql** - Removes old column (optional)

### Step 2: Execute Migration Script

```bash
chmod +x /docs/db/run_trailer_migration.sh
./docs/db/run_trailer_migration.sh
```

The script will:
- Prompt for database credentials
- Execute SQL files in order
- Show migration summary
- Optionally remove the old column

### Step 3: Data Migration Logic

The migration script handles various YouTube URL formats:
- Full URLs: `https://www.youtube.com/watch?v=VIDEO_ID`
- Short URLs: `https://youtu.be/VIDEO_ID`
- Embed URLs: `https://www.youtube.com/embed/VIDEO_ID`
- Plain IDs: `VIDEO_ID` (11 characters)

## Backend Changes

### New Model: ContentTrailer

Location: `app/Models/V2/ContentTrailer.php`

**Key Methods:**
- `getPrimaryTrailer($contentId)` - Get primary trailer for content
- `getContentTrailers($contentId)` - Get all trailers ordered
- `createFromUrl($contentId, $url, $title, $isPrimary, $sortOrder)` - Create from URL
- `setPrimary()` - Set as primary trailer
- `extractYouTubeId($url)` - Extract YouTube ID from various formats

**Accessor Methods:**
- `getEmbedUrlAttribute()` - YouTube embed URL
- `getWatchUrlAttribute()` - YouTube watch URL  
- `getThumbnailUrlAttribute()` - YouTube thumbnail URL

### Updated Content Model

Location: `app/Models/V2/Content.php`

**New Relationships:**
- `trailers()` - HasMany relationship to ContentTrailer
- `primaryTrailer()` - HasOne relationship to primary trailer

**Backward Compatibility:**
- `getTrailerUrlAttribute()` - Returns primary trailer URL
- `getTrailerYoutubeIdAttribute()` - Returns primary trailer YouTube ID

### New Controller: ContentTrailerController

Location: `app/Http/Controllers/Api/V2/ContentTrailerController.php`

**Available Methods:**
- `getContentTrailers()` - Get all trailers for content
- `getPrimaryTrailer()` - Get primary trailer only
- `addTrailer()` - Add new trailer
- `updateTrailer()` - Update existing trailer
- `deleteTrailer()` - Delete trailer
- `setPrimaryTrailer()` - Set trailer as primary
- `reorderTrailers()` - Reorder trailers by sort_order

### Updated ContentController

Location: `app/Http/Controllers/Api/V2/ContentController.php`

**Changes:**
- Added `'trailers'` to relationship loading in content queries
- Updated `formatContent()` method to include trailer data
- Maintains backward compatibility by including `trailer_url` and `trailer_youtube_id` fields

## API Endpoints

### New Trailer Endpoints

```
POST /api/v2/content/trailers
- Get all trailers for a content
- Parameters: content_id

POST /api/v2/content/trailer/primary  
- Get primary trailer for a content
- Parameters: content_id

POST /api/v2/content/trailer/add
- Add new trailer to content
- Parameters: content_id, trailer_url, title (optional), is_primary (optional), sort_order (optional)

POST /api/v2/content/trailer/update
- Update existing trailer
- Parameters: content_trailer_id, title (optional), trailer_url (optional), is_primary (optional), sort_order (optional)

POST /api/v2/content/trailer/delete
- Delete a trailer
- Parameters: content_trailer_id

POST /api/v2/content/trailer/set-primary
- Set a trailer as primary
- Parameters: content_trailer_id

POST /api/v2/content/trailer/reorder
- Reorder trailers for content
- Parameters: content_id, trailer_orders (array of {content_trailer_id, sort_order})
```

### Response Format

**Single Trailer:**
```json
{
  "status": true,
  "message": "Trailer fetched successfully",
  "data": {
    "content_trailer_id": 1,
    "content_id": 123,
    "title": "Official Trailer",
    "youtube_id": "dQw4w9WgXcQ",
    "trailer_url": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "embed_url": "https://www.youtube.com/embed/dQw4w9WgXcQ",
    "watch_url": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "thumbnail_url": "https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
    "is_primary": true,
    "sort_order": 0,
    "created_at": "2025-01-08T12:00:00.000000Z",
    "updated_at": "2025-01-08T12:00:00.000000Z"
  }
}
```

**Multiple Trailers:**
```json
{
  "status": true,
  "message": "Trailers fetched successfully", 
  "data": [
    {
      "content_trailer_id": 1,
      "title": "Official Trailer",
      "is_primary": true,
      "sort_order": 0,
      // ... other fields
    },
    {
      "content_trailer_id": 2,
      "title": "Teaser Trailer",
      "is_primary": false,
      "sort_order": 1,
      // ... other fields
    }
  ]
}
```

## Backward Compatibility

### Existing API Responses

All existing content API endpoints continue to work and include:
- `trailer_url` - URL of the primary trailer (or first trailer if no primary)
- `trailer_youtube_id` - YouTube ID of the primary trailer
- `trailers` - Array of all trailers with detailed information

### Content Detail Response

```json
{
  "content_id": 123,
  "title": "Movie Title",
  "trailer_url": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
  "trailer_youtube_id": "dQw4w9WgXcQ", 
  "trailers": [
    {
      "content_trailer_id": 1,
      "title": "Official Trailer",
      "youtube_id": "dQw4w9WgXcQ",
      "trailer_url": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "embed_url": "https://www.youtube.com/embed/dQw4w9WgXcQ",
      "thumbnail_url": "https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
      "is_primary": true,
      "sort_order": 0
    }
  ],
  // ... other content fields
}
```

## Migration Verification

After migration, verify:

1. **Data Count Match:**
   ```sql
   -- Original count
   SELECT COUNT(*) FROM content WHERE trailer_url IS NOT NULL AND trailer_url != '';
   
   -- Migrated count  
   SELECT COUNT(*) FROM content_trailer;
   ```

2. **Primary Trailer Assignment:**
   ```sql
   -- Each content should have at most one primary trailer
   SELECT content_id, COUNT(*) as primary_count 
   FROM content_trailer 
   WHERE is_primary = 1 
   GROUP BY content_id 
   HAVING primary_count > 1;
   ```

3. **URL Format Validation:**
   ```sql
   -- Check YouTube ID format
   SELECT * FROM content_trailer 
   WHERE LENGTH(youtube_id) != 11 OR youtube_id NOT REGEXP '^[A-Za-z0-9_-]{11}$';
   ```

## Post-Migration Steps

1. **Test API Endpoints**: Verify all new trailer endpoints work correctly
2. **Update Mobile Apps**: Update mobile applications to use new trailer data structure
3. **Admin Interface**: Update admin interface to manage multiple trailers
4. **Documentation**: Update API documentation with new endpoints
5. **Monitoring**: Monitor for any issues with trailer functionality

## Rollback Plan

If rollback is needed:

1. **Preserve Data:**
   ```sql
   -- Update content table with primary trailer URLs
   UPDATE content c
   JOIN content_trailer ct ON c.content_id = ct.content_id
   SET c.trailer_url = ct.trailer_url
   WHERE ct.is_primary = 1;
   ```

2. **Drop New Table:**
   ```sql
   DROP TABLE content_trailer;
   ```

3. **Restore Routes**: Remove trailer routes from `api_v2.php`

4. **Revert Code**: Restore original Content model and controller code

## Benefits

1. **Multiple Trailers**: Support for multiple trailers per content
2. **Better Organization**: Primary trailer designation and custom ordering
3. **Rich Metadata**: Trailer titles and additional information
4. **Future-Proof**: Extensible structure for additional trailer features
5. **Performance**: Optimized indexes for fast queries
6. **Backward Compatible**: Existing APIs continue to work

## Files Created/Modified

### New Files:
- `docs/db/01_create_content_trailer_table.sql`
- `docs/db/02_migrate_existing_trailer_data.sql` 
- `docs/db/03_remove_old_trailer_url_column.sql`
- `docs/db/run_trailer_migration.sh`
- `app/Models/V2/ContentTrailer.php`
- `app/Http/Controllers/Api/V2/ContentTrailerController.php`

### Modified Files:
- `app/Models/V2/Content.php`
- `app/Http/Controllers/Api/V2/ContentController.php`
- `routes/api_v2.php`

## Conclusion

This migration successfully converts the single trailer URL field to a flexible one-to-many relationship while maintaining full backward compatibility. The new structure allows for better content management and provides a foundation for enhanced trailer features in the future.