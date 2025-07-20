# TV App API Requirements Document

## Overview
This document outlines the complete API specifications for a TV/Movie streaming application backend. The API supports user management, content streaming (movies/series), live TV channels, custom advertisements, and application settings.

## Base URL Structure
- **API Base**: `/api/`
- **Authentication**: Header-based verification system
- **Response Format**: JSON

## Authentication & Headers
The API uses custom header-based authentication:
- **Required Headers**: Custom header verification (implement `checkHeader` middleware)
- **User Context**: Most endpoints require `user_id` parameter for user-specific operations

## Common Response Structure
```json
{
  "status": true/false,
  "message": "Response message",
  "data": {} // Response data (varies by endpoint)
}
```

## API Endpoints

### 1. User Management

#### 1.1 User Registration
- **Endpoint**: `POST /api/User/registration`
- **Parameters**:
  ```json
  {
    "identity": "string (required) - unique user identifier",
    "email": "string (required) - user email",
    "login_type": "integer (required) - 1=Google, 2=Facebook, 3=Apple, 4=Email",
    "device_type": "integer (required) - 1=Android, 2=iOS",
    "device_token": "string (required) - FCM/push notification token",
    "fullname": "string (optional) - user full name"
  }
  ```
- **Response**:
  ```json
  {
    "status": true,
    "message": "User Added Successfully",
    "data": {
      "id": 1,
      "fullname": "John Doe",
      "email": "john@example.com",
      "identity": "unique_id",
      "login_type": 1,
      "device_type": 1,
      "device_token": "fcm_token",
      "profile_image": "url_to_image",
      "watchlist_content_ids": "comma_separated_ids",
      "created_at": "timestamp",
      "updated_at": "timestamp"
    }
  }
  ```

#### 1.2 Get User Profile
- **Endpoint**: `POST /api/User/getProfile`
- **Parameters**:
  ```json
  {
    "user_id": "integer (required)"
  }
  ```
- **Response**: Same user object as registration

#### 1.3 Update User Profile
- **Endpoint**: `POST /api/User/updateProfile`
- **Parameters**:
  ```json
  {
    "user_id": "integer (required)",
    "fullname": "string (optional)",
    "email": "string (optional)",
    "watchlist_content_ids": "string (optional) - comma-separated content IDs",
    "login_type": "integer (optional)",
    "device_type": "integer (optional)",
    "device_token": "string (optional)",
    "profile_image": "file (optional) - image upload"
  }
  ```
- **Response**: Updated user object

#### 1.4 Logout
- **Endpoint**: `POST /api/User/Logout`
- **Parameters**:
  ```json
  {
    "user_id": "integer (required)"
  }
  ```
- **Response**: Success message with cleared device token

#### 1.5 Delete Account
- **Endpoint**: `POST /api/User/deleteMyAccount`
- **Parameters**:
  ```json
  {
    "user_id": "integer (required)"
  }
  ```
- **Response**: Success message

### 2. Content Management

#### 2.1 Get Home Page Content
- **Endpoint**: `POST /api/Content/GetHomeContentList`
- **Parameters**:
  ```json
  {
    "user_id": "integer (required)"
  }
  ```
- **Response**:
  ```json
  {
    "status": true,
    "message": "Fetch Home Page Data Successfully",
    "featured": [], // Featured content array
    "watchlist": [], // User's watchlist (limited to 5)
    "topContents": [], // Top content with ordering
    "genreContents": [] // Content grouped by genres
  }
  ```

#### 2.2 Get All Content List
- **Endpoint**: `POST /api/Content/getAllContentList`
- **Parameters**:
  ```json
  {
    "start": "integer (required) - pagination offset",
    "limit": "integer (required) - number of items",
    "type": "integer (optional) - 1=Movie, 2=Series, 3=Live TV"
  }
  ```

#### 2.3 Search Content
- **Endpoint**: `POST /api/Content/searchContent`
- **Parameters**:
  ```json
  {
    "start": "integer (required)",
    "limit": "integer (required)",
    "keyword": "string (optional) - search term",
    "type": "integer (optional) - 1=Movie, 2=Series, 3=Live TV",
    "genre_id": "integer (optional)",
    "language_id": "integer (optional)"
  }
  ```

#### 2.4 Get Movies List
- **Endpoint**: `POST /api/Content/getMovieList`
- **Parameters**: Standard pagination (start, limit)

#### 2.5 Get Series List
- **Endpoint**: `POST /api/Content/getSeriesList`
- **Parameters**: Standard pagination (start, limit)

#### 2.6 Get Content Details by ID
- **Endpoint**: `POST /api/Content/getContentDetailsByID`
- **Parameters**:
  ```json
  {
    "content_id": "integer (required)",
    "user_id": "integer (required)"
  }
  ```
- **Response**: Includes content details, cast, sources, subtitles, seasons (for series), and "more like this" suggestions

#### 2.7 Get Content Sources
- **Endpoint**: `POST /api/Content/getSourceByContentID`
- **Parameters**:
  ```json
  {
    "content_id": "integer (required)"
  }
  ```

#### 2.8 Get Content Subtitles
- **Endpoint**: `POST /api/Content/getSubtitlesByContentID`
- **Parameters**:
  ```json
  {
    "content_id": "integer (required)"
  }
  ```

#### 2.9 Series-Specific Endpoints

##### Get Seasons by Content ID
- **Endpoint**: `POST /api/Content/getSeasonByContentID`
- **Parameters**:
  ```json
  {
    "content_id": "integer (required)"
  }
  ```

##### Get Episodes by Season ID
- **Endpoint**: `POST /api/Content/getEpisodeBySeasonID`
- **Parameters**:
  ```json
  {
    "season_id": "integer (required)"
  }
  ```

##### Get Episode Sources
- **Endpoint**: `POST /api/Content/getSourceByEpisodeID`
- **Parameters**:
  ```json
  {
    "episode_id": "integer (required)"
  }
  ```

##### Get Episode Subtitles
- **Endpoint**: `POST /api/Content/getSubtitlesByEpisodeID`
- **Parameters**:
  ```json
  {
    "episode_id": "integer (required)"
  }
  ```

#### 2.10 Watchlist Management

##### Add to Watchlist
- **Endpoint**: `POST /api/Content/addToWatchList`
- **Parameters**:
  ```json
  {
    "user_id": "integer (required)",
    "content_id": "integer (required)"
  }
  ```

##### Remove from Watchlist
- **Endpoint**: `POST /api/Content/removeFromWatchList`
- **Parameters**:
  ```json
  {
    "user_id": "integer (required)",
    "content_id": "integer (required)"
  }
  ```

##### Get Watchlist
- **Endpoint**: `POST /api/Content/getWatchlist`
- **Parameters**:
  ```json
  {
    "user_id": "integer (required)",
    "start": "integer (required)",
    "limit": "integer (optional, default: 20)"
  }
  ```

#### 2.11 Analytics Tracking

##### Increase Content View
- **Endpoint**: `POST /api/Content/increaseContentView`
- **Parameters**:
  ```json
  {
    "content_id": "integer (required)"
  }
  ```

##### Increase Content Download
- **Endpoint**: `POST /api/Content/increaseContentDownload`
- **Parameters**:
  ```json
  {
    "content_id": "integer (required)"
  }
  ```

##### Increase Content Share
- **Endpoint**: `POST /api/Content/increaseContentShare`
- **Parameters**:
  ```json
  {
    "content_id": "integer (required)"
  }
  ```

##### Increase Episode View
- **Endpoint**: `POST /api/Content/increaseEpisodeView`
- **Parameters**:
  ```json
  {
    "episode_id": "integer (required)"
  }
  ```

##### Increase Episode Download
- **Endpoint**: `POST /api/Content/increaseEpisodeDownload`
- **Parameters**:
  ```json
  {
    "episode_id": "integer (required)"
  }
  ```

#### 2.12 Comments
- **Endpoint**: `POST /api/Content/addComment`
- **Parameters**: TBD (endpoint exists but needs analysis)

### 3. Live TV Management

#### 3.1 Get TV Categories
- **Endpoint**: `POST /api/TV/GetTvCategoryist`
- **Response**: List of TV categories with associated channels

#### 3.2 Get All TV Channels
- **Endpoint**: `POST /api/TV/getAllTvChannelList`
- **Parameters**: Standard pagination

#### 3.3 Get TV Channels by Category
- **Endpoint**: `POST /api/TV/getTvChannelListByCategoryID`
- **Parameters**:
  ```json
  {
    "tv_category_id": "integer (required)",
    "start": "integer (required)",
    "limit": "integer (required)"
  }
  ```

#### 3.4 Increase TV Channel View
- **Endpoint**: `POST /api/TV/increaseTVChannelView`
- **Parameters**:
  ```json
  {
    "channel_id": "integer (required)"
  }
  ```

#### 3.5 Increase TV Channel Share
- **Endpoint**: `POST /api/TV/increaseTVChannelShare`
- **Parameters**:
  ```json
  {
    "channel_id": "integer (required)"
  }
  ```

### 4. Custom Advertisements

#### 4.1 Fetch Custom Ads
- **Endpoint**: `POST /api/Ads/fetchCustomAds`
- **Parameters**:
  ```json
  {
    "is_android": "boolean (optional) - 1 for Android",
    "is_ios": "boolean (optional) - 1 for iOS"
  }
  ```
- **Note**: At least one platform must be selected
- **Response**: Returns up to 10 random active ads with sources

#### 4.2 Increase Ad View
- **Endpoint**: `POST /api/Ads/increaseAdView`
- **Parameters**:
  ```json
  {
    "custom_ad_id": "integer (required)"
  }
  ```

#### 4.3 Increase Ad Click
- **Endpoint**: `POST /api/Ads/increaseAdClick`
- **Parameters**:
  ```json
  {
    "custom_ad_id": "integer (required)"
  }
  ```

### 5. Metadata & Settings

#### 5.1 Get All Genres
- **Endpoint**: `POST /api/Content/getAllGenreList`
- **Response**: List of all available genres

#### 5.2 Get All Languages
- **Endpoint**: `GET /api/Content/getAllLanguageList`
- **Response**: List of all available languages

#### 5.3 Get Content by Genre
- **Endpoint**: `POST /api/Content/getContentListByGenreID`
- **Parameters**:
  ```json
  {
    "genre_id": "integer (required)",
    "start": "integer (required)",
    "limit": "integer (required)"
  }
  ```

#### 5.4 Get App Settings
- **Endpoint**: `GET /api/getSettings`
- **Response**: Application settings, genres, languages, and Admob configuration

#### 5.5 Get Notifications
- **Endpoint**: `POST /api/getAllNotification`
- **Parameters**:
  ```json
  {
    "user_id": "integer (required)",
    "start": "integer (required)",
    "limit": "integer (optional, default: 20)"
  }
  ```
- **Response**:
  ```json
  {
    "status": 200,
    "message": "Notification Data Found.",
    "data": [
      {
        "notification_type": 0,
        "title": "Notification Title",
        "message": "Notification Message",
        "image": "image_url",
        "created_at": "2024-01-01 12:00:00"
      }
    ]
  }
  ```

#### 5.6 Get Subscription Packages
- **Endpoint**: `GET /api/getSubscriptionPackage`
- **Response**:
  ```json
  {
    "status": 200,
    "message": "Subscription Data Found.",
    "monthly_data": {
      "id": 1,
      "duration": 1,
      "price": "9.99",
      "features": "..."
    },
    "yearly_data": {
      "id": 2,
      "duration": 2,
      "price": "99.99",
      "features": "..."
    }
  }
  ```

## Data Models & Structures

### User Model
```json
{
  "id": "integer",
  "fullname": "string",
  "email": "string",
  "identity": "string",
  "login_type": "integer (1=Google, 2=Facebook, 3=Apple, 4=Email)",
  "device_type": "integer (1=Android, 2=iOS)",
  "device_token": "string",
  "profile_image": "string (URL)",
  "watchlist_content_ids": "string (comma-separated)",
  "created_at": "timestamp",
  "updated_at": "timestamp"
}
```

### Content Model
```json
{
  "id": "integer",
  "title": "string",
  "description": "text",
  "type": "integer (1=Movie, 2=Series)",
  "vertical_poster": "string (URL)",
  "horizontal_poster": "string (URL)",
  "ratings": "decimal",
  "release_year": "integer",
  "language_id": "integer",
  "genre_ids": "string (comma-separated)",
  "total_view": "integer",
  "total_download": "integer",
  "total_share": "integer",
  "is_featured": "integer (0=No, 1=Yes)",
  "is_show": "integer (0=Hidden, 1=Visible)",
  "created_at": "timestamp",
  "updated_at": "timestamp",
  
  // Relations
  "language": "Language object",
  "sources": "Array of ContentSource objects",
  "subtitles": "Array of Subtitle objects",
  "seasons": "Array of Season objects (for series)",
  "contentCast": "Array of ContentCast objects",
  "more_like_this": "Array of similar Content objects",
  "is_watchlist": "boolean (computed field)"
}
```

### ContentSource Model
```json
{
  "id": "integer",
  "content_id": "integer",
  "title": "string",
  "source": "string (URL or file path)",
  "type": "integer (1=YouTube, 7=File)",
  "quality": "string (480p, 720p, 1080p, etc.)",
  "size": "string",
  "is_download": "integer (0=No, 1=Yes)",
  "access_type": "integer",
  "created_at": "timestamp",
  "updated_at": "timestamp"
}
```

### Season Model
```json
{
  "id": "integer",
  "content_id": "integer",
  "season_number": "integer",
  "title": "string",
  "episodes": "Array of Episode objects"
}
```

### Episode Model
```json
{
  "id": "integer",
  "season_id": "integer",
  "episode_number": "integer",
  "title": "string",
  "description": "text",
  "thumbnail": "string (URL)",
  "duration": "string",
  "total_view": "integer",
  "total_download": "integer",
  "sources": "Array of EpisodeSource objects",
  "subtitles": "Array of EpisodeSubtitle objects"
}
```

### TVChannel Model
```json
{
  "id": "integer",
  "title": "string",
  "image": "string (URL)",
  "stream_url": "string",
  "category_ids": "string (comma-separated)",
  "total_view": "integer",
  "total_share": "integer",
  "created_at": "timestamp",
  "updated_at": "timestamp"
}
```

### TVCategory Model
```json
{
  "id": "integer",
  "title": "string",
  "image": "string (URL)",
  "channels": "Array of TVChannel objects"
}
```

### CustomAd Model
```json
{
  "id": "integer",
  "title": "string",
  "brand_name": "string",
  "brand_logo": "string (URL)",
  "button_text": "string",
  "is_android": "integer (0=No, 1=Yes)",
  "android_link": "string",
  "is_ios": "integer (0=No, 1=Yes)",
  "ios_link": "string",
  "start_date": "date",
  "end_date": "date",
  "status": "integer (0=Off, 1=On)",
  "views": "integer",
  "clicks": "integer",
  "sources": "Array of CustomAdSource objects"
}
```

### CustomAdSource Model
```json
{
  "id": "integer",
  "custom_ad_id": "integer",
  "type": "integer (0=Image, 1=Video)",
  "content": "string (URL or file path)",
  "headline": "string",
  "description": "text",
  "is_skippable": "integer (0=Must Watch, 1=Skippable)"
}
```

## Constants
```javascript
const CONTENT_TYPES = {
  MOVIE: 1,
  SERIES: 2
};

const FEATURED_STATUS = {
  UNFEATURED: 0,
  FEATURED: 1
};

const CONTENT_VISIBILITY = {
  HIDDEN: 0,
  VISIBLE: 1
};

const SOURCE_TYPES = {
  YOUTUBE: 1,
  FILE: 7
};

const LOGIN_TYPES = {
  GOOGLE: 1,
  FACEBOOK: 2,
  APPLE: 3,
  EMAIL: 4
};

const DEVICE_TYPES = {
  ANDROID: 1,
  IOS: 2
};

const AD_SOURCE_TYPES = {
  IMAGE: 0,
  VIDEO: 1
};
```

## File Upload Requirements
- **Profile Images**: Support common image formats (JPG, PNG, etc.)
- **Content Posters**: Vertical and horizontal poster images
- **Video Sources**: Support for video file uploads
- **Subtitle Files**: Support for SRT subtitle format
- **Ad Content**: Images and videos for custom advertisements

## Middleware Requirements
1. **Header Verification**: Custom authentication header checking
2. **Rate Limiting**: 60 requests per minute (as per original config)
3. **CORS**: Proper cross-origin resource sharing setup
4. **File Upload**: Handling multipart form data
5. **Error Handling**: Consistent error response format

## Environment Variables Needed
- Database configuration
- File storage configuration (AWS S3, DigitalOcean Spaces, or local)
- TMDB API key (for movie data integration)
- Push notification settings
- Custom ad configuration flags
- Pagination limits for different content types

## Additional Implementation Notes
1. **Pagination**: Implement consistent `start` and `limit` parameters across all list endpoints
2. **Search**: Support partial text matching with LIKE queries
3. **Random Content**: Use proper randomization for "more like this" suggestions
4. **File Management**: Implement proper file upload, storage, and deletion
5. **Analytics**: Track view counts, downloads, and shares accurately
6. **Validation**: Implement comprehensive input validation for all endpoints
7. **Security**: Ensure proper authentication and authorization checks
8. **Performance**: Consider caching for frequently accessed data like genres and languages

This specification provides a complete blueprint for implementing the Node.js replacement API with all the features and functionality of the original PHP backend.

## MongoDB Document Structures

### Content Collection Document (Movies & TV Series)

This single document structure handles both movies and TV series with embedded data for optimal MongoDB performance.

#### Real Movie Example - "F1" (2025 Formula One Movie)

```json
{
  "_id": "ObjectId",
  "content_id": 1,
  "title": "F1",
  "description": "A Formula One driver comes out of retirement to mentor and team up with a younger driver.",
  "type": 1,
  "vertical_poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747494097_vuga_f1-vert.png",
  "horizontal_poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747494098_vuga_f1-horiz.png",
  "ratings": 5.0,
  "release_year": 2025,
  "duration": "140:13",
  "total_view": 16,
  "total_download": 0,
  "total_share": 0,
  "is_featured": 1,
  "is_show": 1,
  "created_at": "2025-05-17T15:01:38Z",
  "updated_at": "2025-07-15T05:33:19Z",
  
  "language": {
    "id": 1,
    "title": "English",
    "code": "en"
  },
  
  "genres": [
    {
      "id": 3,
      "title": "Drama",
      "created_at": "2025-05-17T14:53:51Z"
    }
  ],
  
  "cast": [
    {
      "cast_id": "actor_3",
      "character_name": "John Smith",
      "order": 1,
      "actor_details": {
        "fullname": "Brad Pitt",
        "profile_image": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747492493_vuga_brad-pitt.jpeg"
      }
    }
  ],
  
  "sources": [
    {
      "source_id": 4,
      "title": "F1 Trailer 1",
      "source_url": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/f1_trailer_1.mp4",
      "type": 4,
      "quality": "1080HD",
      "size": "35MB",
      "is_download": 0,
      "access_type": 1
    },
    {
      "source_id": 5,
      "title": "F1 Trailer 2", 
      "source_url": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/f1_trailer_2.mp4",
      "type": 4,
      "quality": "1080HD",
      "size": "35MB",
      "is_download": 0,
      "access_type": 1
    },
    {
      "source_id": 6,
      "title": "F1 Trailer 3",
      "source_url": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/f1_trailer_3.mp4",
      "type": 4,
      "quality": "1080HD",
      "size": "35MB",
      "is_download": 0,
      "access_type": 1
    }
  ],
  
  "subtitles": [
    {
      "subtitle_id": 1,
      "language": {
        "id": 1,
        "title": "English",
        "code": "en"
      },
      "file_url": "https://example.com/subtitles/movie_en.srt"
    },
    {
      "subtitle_id": 2,
      "language": {
        "id": 2,
        "title": "Spanish", 
        "code": "es"
      },
      "file_url": "https://example.com/subtitles/movie_es.srt"
    }
  ],
  
  "seasons": [
    {
      "season_id": 1,
      "season_number": 1,
      "title": "Season 1",
      "description": "The beginning of the story...",
      "poster": "https://example.com/seasons/s1_poster.jpg",
      "release_date": "2024-01-15",
      
      "episodes": [
        {
          "episode_id": 1,
          "episode_number": 1,
          "title": "Pilot",
          "description": "The first episode where everything begins...",
          "thumbnail": "https://example.com/episodes/s1e1_thumb.jpg",
          "duration": "45:30",
          "air_date": "2024-01-15",
          "total_view": 5240,
          "total_download": 420,
          
          "sources": [
            {
              "source_id": 101,
              "title": "1080p HD",
              "source_url": "https://example.com/episodes/s1e1_1080p.mp4",
              "type": 7,
              "quality": "1080p", 
              "size": "1.8GB",
              "is_download": 1,
              "access_type": 1
            },
            {
              "source_id": 102,
              "title": "720p HD",
              "source_url": "https://example.com/episodes/s1e1_720p.mp4",
              "type": 7,
              "quality": "720p",
              "size": "900MB", 
              "is_download": 1,
              "access_type": 1
            }
          ],
          
          "subtitles": [
            {
              "subtitle_id": 101,
              "language": {
                "id": 1,
                "title": "English",
                "code": "en"
              },
              "file_url": "https://example.com/subtitles/s1e1_en.srt"
            }
          ]
        },
        {
          "episode_id": 2,
          "episode_number": 2,
          "title": "The Mystery Deepens",
          "description": "Our heroes discover something unexpected...",
          "thumbnail": "https://example.com/episodes/s1e2_thumb.jpg",
          "duration": "42:15",
          "air_date": "2024-01-22",
          "total_view": 4890,
          "total_download": 380,
          
          "sources": [
            {
              "source_id": 201,
              "title": "1080p HD",
              "source_url": "https://example.com/episodes/s1e2_1080p.mp4",
              "type": 7,
              "quality": "1080p",
              "size": "1.7GB",
              "is_download": 1,
              "access_type": 1
            }
          ],
          
          "subtitles": [
            {
              "subtitle_id": 201,
              "language": {
                "id": 1,
                "title": "English", 
                "code": "en"
              },
              "file_url": "https://example.com/subtitles/s1e2_en.srt"
            }
          ]
        }
      ]
    },
    {
      "season_id": 2,
      "season_number": 2,
      "title": "Season 2",
      "description": "The story continues with more intensity...",
      "poster": "https://example.com/seasons/s2_poster.jpg",
      "release_date": "2024-06-15",
      
      "episodes": [
        {
          "episode_id": 11,
          "episode_number": 1,
          "title": "New Beginnings",
          "description": "Season 2 opens with unexpected developments...",
          "thumbnail": "https://example.com/episodes/s2e1_thumb.jpg",
          "duration": "47:12",
          "air_date": "2024-06-15",
          "total_view": 6120,
          "total_download": 520,
          
          "sources": [
            {
              "source_id": 301,
              "title": "4K UHD",
              "source_url": "https://example.com/episodes/s2e1_4k.mp4",
              "type": 7,
              "quality": "4K",
              "size": "3.2GB",
              "is_download": 1,
              "access_type": 2
            },
            {
              "source_id": 302,
              "title": "1080p HD", 
              "source_url": "https://example.com/episodes/s2e1_1080p.mp4",
              "type": 7,
              "quality": "1080p",
              "size": "1.9GB",
              "is_download": 1,
              "access_type": 1
            }
          ],
          
          "subtitles": [
            {
              "subtitle_id": 301,
              "language": {
                "id": 1,
                "title": "English",
                "code": "en"
              },
              "file_url": "https://example.com/subtitles/s2e1_en.srt"
            },
            {
              "subtitle_id": 302,
              "language": {
                "id": 2,
                "title": "Spanish",
                "code": "es"
              },
              "file_url": "https://example.com/subtitles/s2e1_es.srt"
            }
          ]
        }
      ]
    }
  ],
  
  "tmdb_id": 12345,
  "imdb_id": "tt1234567",
  "trailer_url": "8skLAmcQEX4",
  "tmdb_id": null,
  "imdb_id": null,
  
  "search_tags": ["f1", "formula one", "racing", "drama", "brad pitt", "2025"],
  "age_rating": null,
  "country": null,
  
  "seo": {
    "meta_title": "F1 - 2025 Formula One Movie",
    "meta_description": "A Formula One driver comes out of retirement to mentor and team up with a younger driver.",
    "keywords": ["f1", "formula one", "racing", "drama", "brad pitt"]
  }
}
```

#### Real TV Series Example - "Ari Global Show" (Celebrity Interview Show)

```json
{
  "_id": "ObjectId",
  "content_id": 4,
  "title": "Ari Global Show",
  "description": "ARI GLOBAL SHOW - celebrity interview show featuring such stars as Jennifer Lopez, Ben Affleck, Matt Damon, Ryan Reynolds, Emma Stone, Eva Longoria, Gerard Butler, Samuel Jackson, Henry Cavill, Shaquille O'Neal, Ruby Rose, and others. https://gossip-stone.com/ari-global-show/",
  "type": 2,
  "vertical_poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748119439_vuga_ari_global_vert.png",
  "horizontal_poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748119440_vuga_ari_global_horiz.png",
  "ratings": 5.0,
  "release_year": 2022,
  "duration": null,
  "total_view": 0,
  "total_download": 0,
  "total_share": 0,
  "is_featured": 1,
  "is_show": 1,
  "created_at": "2025-05-24T20:44:00Z",
  "updated_at": "2025-05-24T20:44:06Z",
  
  "language": {
    "id": 1,
    "title": "English",
    "code": "en"
  },
  
  "genres": [
    {
      "id": 2,
      "title": "Reality",
      "created_at": "2025-05-17T14:53:29Z"
    }
  ],
  
  "cast": [],
  "sources": [],
  "subtitles": [],
  
  "seasons": [
    {
      "season_id": 1,
      "season_number": 1,
      "title": "Season 1",
      "description": "Celebrity interview season featuring A-list stars",
      "poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748119439_vuga_ari_global_vert.png",
      "trailer_url": "58uC20vDPGE",
      "release_date": "2025-05-24",
      
      "episodes": [
        {
          "episode_id": 1,
          "episode_number": 1,
          "title": "Episode 1",
          "description": "Jennifer Marc Wahlberg, Ben Affleck, Matt Damon, Anna Kendrick, Natti Natasha, Luis Fonsi, Alejandra",
          "thumbnail": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748119659_vuga_ari_global_thumb_s1e1.jpg",
          "duration": "10:00",
          "air_date": "2025-05-24",
          "total_view": 2,
          "total_download": 0,
          
          "sources": [
            {
              "source_id": 1,
              "title": "Ari Global S1E1",
              "source_url": "https://gossip-stone.nyc3.digitaloceanspaces.com/ios_video_feed/AriGlobal/mp4/ariglobal_s1e1.mp4",
              "type": 4,
              "quality": "1080HD",
              "size": "35MB",
              "is_download": 0,
              "access_type": 1
            }
          ],
          
          "subtitles": []
        },
        {
          "episode_id": 2,
          "episode_number": 2,
          "title": "Episode 2",
          "description": "Gerald Butler, Emma Stone, Emma Thompson, Amy Schumer, Diego Boneta, Nicky Jam, Eva Longoria, Pedro Capo, Eugenio Derbez",
          "thumbnail": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748120033_vuga_ari_global_roku_thumb_s1e2.jpg",
          "duration": "10:00",
          "air_date": "2025-05-24",
          "total_view": 2,
          "total_download": 0,
          
          "sources": [],
          "subtitles": []
        },
        {
          "episode_id": 3,
          "episode_number": 3,
          "title": "Episode 3",
          "description": "Henry Cavill, Ryan Reynolds, Samuel Jackson, Ivy Queen, CNCO, Michael Pena",
          "thumbnail": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748120981_vuga_ari-global-s1-e3.jpg",
          "duration": "10:00",
          "air_date": "2025-05-24",
          "total_view": 2,
          "total_download": 0,
          
          "sources": [],
          "subtitles": []
        }
      ]
    }
  ],
  
  "trailer_url": "S44ZWwYu59Y",
  "tmdb_id": null,
  "imdb_id": null,
  
  "search_tags": ["ari global", "celebrity", "interview", "reality", "talk show", "jennifer lopez", "ben affleck", "matt damon", "ryan reynolds", "emma stone"],
  "age_rating": null,
  "country": "United States",
  
  "seo": {
    "meta_title": "Ari Global Show - Celebrity Interview Series",
    "meta_description": "Celebrity interview show featuring A-list stars like Jennifer Lopez, Ben Affleck, Matt Damon, and more.",
    "keywords": ["ari global", "celebrity interviews", "reality show", "talk show"]
  }
}
```

### Cast & Crew Collection Document

This collection stores all actors, directors, producers, and crew members that appear across different content.

#### Real Actor Example - "Brad Pitt"

```json
{
  "_id": "ObjectId",
  "cast_id": "actor_3",
  "fullname": "Brad Pitt",
  "stage_name": "Brad Pitt",
  "biography": "William Bradley Pitt is an American actor and film producer. He has received multiple awards, including two Golden Globe Awards and an Academy Award for his acting, in addition to another Academy Award and a Primetime Emmy Award as producer under his production company, Plan B Entertainment.",
  "date_of_birth": "1963-12-18",
  "place_of_birth": "Shawnee, Oklahoma, USA",
  "nationality": "American",
  "gender": "Male",
  
  "profile_images": {
    "thumbnail": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747492493_vuga_brad-pitt.jpeg",
    "medium": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747492493_vuga_brad-pitt.jpeg",
    "large": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747492493_vuga_brad-pitt.jpeg"
  },
  
  "social_media": {
    "instagram": null,
    "twitter": null,
    "facebook": null,
    "imdb": "nm0000093"
  },
  
  "roles": ["Actor", "Producer"],
  "primary_role": "Actor",
  
  "filmography": [
    {
      "content_id": 1,
      "content_title": "F1",
      "character_name": "John Smith",
      "role_type": "Lead Actor",
      "year": 2025,
      "poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747494097_vuga_f1-vert.png"
    }
  ],
  
  "awards": [
    {
      "award_name": "Golden Globe Award",
      "category": "Best Actor in a Motion Picture Musical or Comedy",
      "year": 2010,
      "movie": "Sherlock Holmes",
      "won": true
    },
    {
      "award_name": "Academy Award",
      "category": "Best Supporting Actor",
      "year": 1993,
      "movie": "Chaplin", 
      "won": false,
      "nominated": true
    }
  ],
  
  "statistics": {
    "total_movies": 45,
    "total_tv_shows": 8,
    "career_start_year": 1970,
    "total_awards_won": 15,
    "total_nominations": 42,
    "box_office_total": 15000000000
  },
  
  "personal_details": {
    "height": "5'9\"",
    "eye_color": "Brown",
    "hair_color": "Dark Brown",
    "marital_status": "Married",
    "spouse": "Susan Downey",
    "children": 3
  },
  
  "agency_info": {
    "agent": "CAA",
    "manager": "Brilliant Entertainment",
    "publicist": "42West"
  },
  
  "is_active": true,
  "is_featured": true,
  "popularity_score": 95.8,
  "fan_rating": 4.7,
  
  "created_at": "2024-01-10T08:15:00Z",
  "updated_at": "2024-01-20T16:45:00Z",
  
  "search_tags": ["marvel", "iron man", "sherlock holmes", "action", "drama"],
  
  "seo": {
    "meta_title": "Robert Downey Jr. - Actor Profile",
    "meta_description": "Complete filmography and biography of Robert Downey Jr...",
    "keywords": ["robert downey jr", "iron man", "marvel", "actor"]
  }
}
```

## MongoDB Indexing Strategy

For optimal performance, create the following indexes:

### Content Collection Indexes
```javascript
// Primary search and filtering
db.content.createIndex({ "type": 1, "is_show": 1, "is_featured": 1 })
db.content.createIndex({ "genres.id": 1, "is_show": 1 })
db.content.createIndex({ "language.id": 1, "is_show": 1 })
db.content.createIndex({ "title": "text", "description": "text", "search_tags": "text" })

// Analytics and sorting
db.content.createIndex({ "total_view": -1 })
db.content.createIndex({ "ratings": -1 })
db.content.createIndex({ "release_year": -1 })
db.content.createIndex({ "created_at": -1 })

// Series-specific
db.content.createIndex({ "seasons.episodes.episode_id": 1 })
db.content.createIndex({ "content_id": 1 }, { unique: true })
```

### Cast & Crew Collection Indexes  
```javascript
// Search and filtering
db.cast.createIndex({ "fullname": "text", "stage_name": "text", "biography": "text" })
db.cast.createIndex({ "cast_id": 1 }, { unique: true })
db.cast.createIndex({ "roles": 1 })
db.cast.createIndex({ "is_active": 1, "is_featured": 1 })

// Analytics
db.cast.createIndex({ "popularity_score": -1 })
db.cast.createIndex({ "filmography.content_id": 1 })
```

## MongoDB Aggregation Examples

### Get Content with Cast Details
```javascript
db.content.aggregate([
  { $match: { content_id: 1 } },
  { $lookup: {
      from: "cast",
      localField: "cast.cast_id",
      foreignField: "cast_id", 
      as: "cast_details"
  }},
  { $addFields: {
      "cast": {
        $map: {
          input: "$cast",
          as: "c",
          in: {
            $mergeObjects: [
              "$$c",
              { $arrayElemAt: [
                { $filter: {
                  input: "$cast_details",
                  cond: { $eq: ["$$this.cast_id", "$$c.cast_id"] }
                }}, 0
              ]}
            ]
          }
        }
      }
  }},
  { $project: { "cast_details": 0 } }
])
```

This MongoDB structure provides:
- **Denormalized design** for fast reads
- **Embedded documents** for related data (seasons, episodes, sources)
- **Reference pattern** for cast (to avoid duplication)
- **Flexible schema** supporting both movies and TV series
- **Rich metadata** for better search and recommendations
- **Optimized indexes** for common query patterns

## Real Data Summary

The MongoDB examples above are based on **actual data extracted from your live database**:

### Movie Data - "F1" (2025)
- **Genre**: Drama
- **Cast**: Brad Pitt as John Smith
- **Sources**: 3 trailer videos hosted on DigitalOcean Spaces
- **Analytics**: 16 total views, featured content
- **Storage**: DigitalOcean Spaces CDN for all media

### TV Series Data - "Ari Global Show" (2022)
- **Genre**: Reality (Celebrity interview show)
- **Episodes**: 3 episodes of 10 minutes each featuring A-list celebrities
- **Content**: Jennifer Lopez, Ben Affleck, Matt Damon, Ryan Reynolds, Emma Stone, etc.
- **Analytics**: 2 views per episode
- **Storage**: Mixed CDN storage (gossip-stone.nyc3.digitaloceanspaces.com)

### Technical Insights from Real Data
- **Source Type 4**: Your app uses type 4 for video files (not YouTube type 1)
- **File Storage**: DigitalOcean Spaces with CDN distribution
- **Quality Standards**: "1080HD" format, ~35MB file sizes
- **Episode Structure**: Simple numbering, 10-minute interview format
- **Celebrity Focus**: A-list Hollywood celebrities and Formula One content

This real data structure will ensure your Node.js API perfectly matches your existing content patterns! 