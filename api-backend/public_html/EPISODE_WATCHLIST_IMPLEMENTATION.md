# Episode Ratings and Watchlist Implementation

## Overview

This implementation extends the existing VUGA TV backend to support individual episode ratings and watchlist functionality. The design maintains compatibility with existing systems while adding new capabilities for episodes to be rated and added to user watchlists separately from their parent TV series.

## Architecture

### Database Schema

#### New Tables

1. **app_profile_episode_watchlist** - Stores episode watchlist entries per profile
   - `profile_id` (FK to app_user_profile)
   - `episode_id` (FK to episode)
   - `created_at`, `updated_at`
   - Composite primary key: (profile_id, episode_id)

2. **app_profile_episode_rating** (Already exists)
   - `profile_id` (FK to app_user_profile)
   - `episode_id` (FK to episode)
   - `rating` (float, 0-10 scale)
   - `created_at`, `updated_at`

#### Unified Watchlist View

The `app_profile_unified_watchlist` view combines content and episode watchlists:
- Movies and TV series from `app_user_watchlist`
- Individual episodes from `app_profile_episode_watchlist`
- Includes metadata for unified display

### API Endpoints

#### Episode Rating (Already implemented)
```
POST /api/v2/user/rate-episode
```

#### Episode Watchlist Management
```
POST /api/v2/user/toggle-episode-watchlist
POST /api/v2/user/check-episode-watchlist
POST /api/v2/user/fetch-unified-watchlist
```

## Implementation Details

### 1. Database Migration

Run the migration script:
```bash
cd /path/to/api-backend/public_html/database
./run_episode_watchlist_migration.sh
```

### 2. New Models

#### AppProfileEpisodeWatchlist
- Handles composite primary key (profile_id, episode_id)
- Relationships to Profile and Episode models
- Timestamps for tracking when episodes were added

### 3. Controller Methods

#### toggleEpisodeWatchlist()
- Validates user authentication and profile ownership
- Adds or removes episodes from watchlist
- Returns current status after operation

#### checkEpisodeWatchlist()
- Checks if specific episode is in user's watchlist
- Returns boolean status

#### fetchUnifiedWatchlist()
- Combines movies, TV shows, and episodes in single response
- Supports filtering by type (1=movies, 2=series, 3=episodes)
- Includes pagination
- Sorts by date added (newest first)

### 4. Enhanced Profile Model

Updated `AppUserProfile` model includes:
- `episodeWatchlist()` relationship method
- Many-to-many relationship with Episode model

## API Usage Examples

### Add Episode to Watchlist
```json
POST /api/v2/user/toggle-episode-watchlist
{
  "app_user_id": 123,
  "episode_id": 456,
  "profile_id": 789
}

Response:
{
  "status": true,
  "message": "Added to watchlist",
  "is_in_watchlist": true
}
```

### Check Episode Watchlist Status
```json
POST /api/v2/user/check-episode-watchlist
{
  "app_user_id": 123,
  "episode_id": 456,
  "profile_id": 789
}

Response:
{
  "status": true,
  "is_in_watchlist": true,
  "message": "Episode watchlist status retrieved"
}
```

### Fetch Unified Watchlist
```json
POST /api/v2/user/fetch-unified-watchlist
{
  "user_id": 123,
  "start": 0,
  "limit": 20,
  "type": null,
  "profile_id": 789
}

Response:
{
  "status": true,
  "message": "Unified watchlist fetched successfully",
  "data": [
    {
      "item_type": "content",
      "content_id": 100,
      "title": "Movie Title",
      "type": 1,
      "poster": "poster.jpg",
      "ratings": 8.5,
      "added_at": "2024-01-01T00:00:00Z"
    },
    {
      "item_type": "episode",
      "episode_id": 200,
      "content_id": 101,
      "title": "Episode Title",
      "series_title": "Series Title",
      "season_number": 1,
      "episode_number": 5,
      "poster": "episode_thumb.jpg",
      "ratings": 9.0,
      "added_at": "2024-01-02T00:00:00Z"
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_items": 50,
    "items_per_page": 20,
    "total_pages": 3
  }
}
```

## iOS App Integration

### WatchlistViewModel Updates

The iOS app's `WatchlistViewModel` should be updated to:

1. **Support Episode Operations**
   ```swift
   func toggleEpisodeWatchlist(episodeId: Int, profileId: Int) async
   func checkEpisodeWatchlistStatus(episodeId: Int, profileId: Int) async -> Bool
   ```

2. **Use Unified Watchlist Endpoint**
   ```swift
   func fetchUnifiedWatchlist(type: Int? = nil) async -> [WatchlistItem]
   ```

3. **Handle Mixed Content Types**
   ```swift
   enum WatchlistItemType {
       case content(ContentModel)
       case episode(EpisodeModel, seriesTitle: String)
   }
   ```

### UI Considerations

1. **Episode Detail View**
   - Add watchlist toggle button
   - Show watchlist status indicator
   - Display episode-specific rating

2. **Unified Watchlist View**
   - Filter tabs: All, Movies, Series, Episodes
   - Different cell layouts for content vs episodes
   - Show series context for episodes

## Performance Optimizations

### Database Indexes
- `idx_episode_watchlist` on episode_id
- `idx_profile_episode_watchlist` on profile_id
- `idx_episode_watchlist_created` on created_at for sorting

### Caching Strategy
- Cache unified watchlist results per profile
- Invalidate cache on add/remove operations
- Use Redis for session-based caching

### Query Optimization
- Eager load relationships in unified watchlist
- Use database views for complex joins
- Pagination to limit memory usage

## Security Considerations

### Authorization
- Profile ownership validation on all operations
- Episode existence validation
- Rate limiting on watchlist operations

### Data Integrity
- Foreign key constraints prevent orphaned records
- Composite primary keys prevent duplicates
- Cascading deletes maintain consistency

## Monitoring and Analytics

### Metrics to Track
- Episode watchlist addition/removal rates
- Most watched-listed episodes
- Unified watchlist usage patterns
- API response times

### Logging
- All watchlist operations logged with profile context
- Error tracking for failed operations
- Performance monitoring for complex queries

## Migration Notes

1. **Backward Compatibility**
   - Existing watchlist functionality unchanged
   - V1 API endpoints still supported
   - Gradual migration path for clients

2. **Data Migration**
   - No existing data migration required
   - New tables start empty
   - Users can populate through normal usage

3. **Rollback Plan**
   - Drop new table: `app_profile_episode_watchlist`
   - Drop view: `app_profile_unified_watchlist`
   - Remove new API routes
   - Revert model changes

## Files Modified/Created

### Database
- `/database/migrations/2025_08_21_create_episode_watchlist_table.sql`
- `/database/run_episode_watchlist_migration.sh`

### Models
- `/app/Models/V2/AppProfileEpisodeWatchlist.php` (new)
- `/app/Models/V2/AppUserProfile.php` (modified)

### Controllers
- `/app/Http/Controllers/Api/V2/UserController.php` (modified)

### Routes
- `/routes/api_v2.php` (modified)

### Documentation
- `/EPISODE_WATCHLIST_IMPLEMENTATION.md` (this file)

## Next Steps

1. Run database migration
2. Test new API endpoints
3. Update iOS app to use new functionality
4. Deploy and monitor performance
5. Gather user feedback for improvements

## Support

For technical questions or issues, refer to:
- Laravel documentation for Eloquent relationships
- MySQL documentation for views and indexing
- API testing with Postman or similar tools