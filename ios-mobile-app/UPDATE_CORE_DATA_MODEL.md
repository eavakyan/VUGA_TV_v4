# Update Core Data Model Instructions

## RecentlyWatched Entity Changes

The RecentlyWatched entity should be simplified to only store:

### Keep These Attributes:
- `id` (UUID) - Unique identifier
- `contentID` (Integer 16) - The content ID
- `episodeId` (Integer 16) - The episode ID (for series)
- `contentType` (Integer 16) - Type of content (movie/series)
- `date` (Date) - When it was watched
- `progress` (Double) - Playback progress
- `totalDuration` (Double) - Total duration
- `contentSourceId` (Integer 16) - Source ID
- `contentSourceType` (Integer 16) - Source type
- `sourceUrl` (String) - Source URL for playback
- `isForDownload` (Boolean) - Whether it's a download

### Remove These Attributes (will fetch from API):
- `name` - Will fetch from API
- `episodeName` - Will fetch from API
- `thumbnail` - Will fetch from API
- `episodeHorizontalPoster` - Will fetch from API
- `downloadId` - Keep only if needed for downloads

## Steps to Update:

1. Open Xcode
2. Navigate to Vuga.xcdatamodeld
3. Select the RecentlyWatched entity
4. Delete the attributes listed above
5. Save the model
6. Clean Build Folder (Product > Clean Build Folder)
7. Delete the app from simulator/device to reset Core Data
8. Rebuild the app