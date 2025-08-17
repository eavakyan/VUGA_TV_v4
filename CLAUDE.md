# VUGA TV Multi-Platform System Documentation

## System Overview
VUGA TV is a streaming platform with multiple client applications sharing a common Laravel backend API.

## Platform Components

### Backend API
- **Location**: `/api-backend/public_html/`
- **Framework**: Laravel (PHP 8.2)
- **Database**: MySQL (database name: vuga_tv_app)
- **API Version**: v2 (primary), v1 (legacy)
- **Base URL Production**: https://iosdev.gossip-stone.com/api/v2/
- **Base URL Local**: http://localhost:8001/api/v2/
- **Storage**: Digital Ocean Spaces (NYC3 region)
  - Profile avatars: `profile-avatars/`
  - Content images: `vuga/uploads/`
- **API Key**: jpwc3pny (required in header)

### Client Applications

#### 1. iOS Mobile App
- **Location**: `/ios-mobile-app/`
- **Framework**: SwiftUI
- **Target iOS**: 16.1+
- **Status**: In Production
- **Bundle ID**: com.vugaenterprises.vuga
- **Team ID**: 8TQG9PZ4H5
- **Build Tool**: Xcode 16.0+
- **Dependencies**: CocoaPods
- **Key Libraries**: Kingfisher (image loading), GoogleCast, Firebase
- **Deployment**: TestFlight / App Store

#### 2. Android Mobile App
- **Location**: `/android-mobile-app/`
- **Framework**: Native Android (Java)
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Status**: In Production
- **Package**: com.retry.vuga
- **Build Tool**: Gradle
- **Key Libraries**: Retrofit, Gson, Glide, ExoPlayer, GoogleCast
- **Deployment**: ADB WiFi (192.168.1.230:5555 for Samsung test device)

#### 3. Android TV App
- **Location**: `/android-tv-app/`
- **Framework**: Android TV Leanback
- **Min SDK**: 21 (Android TV 5.0)
- **Status**: In Production
- **Package**: com.vuga.tv
- **Special Considerations**: Remote control navigation, 10-foot UI, D-pad focus handling
- **Key Libraries**: Leanback, ExoPlayer

#### 4. Admin Panel
- **Location**: `/admin-app/`
- **Framework**: React
- **Status**: In Development
- **Node Version**: 18+
- **Package Manager**: npm

## Critical API Endpoints & Consumers

### Profile Management
| Endpoint | iOS | Android Mobile | Android TV | Notes |
|----------|-----|----------------|------------|-------|
| `/getUserProfiles` | ✅ | ✅ | ✅ | Returns all user profiles |
| `/profile/update` | ✅ | ❌ | ❌ | New endpoint for profile updates |
| `/updateProfile` | ❌ | ✅ | ✅ | Legacy - updates user, not profile |
| `/createProfile` | ✅ | ✅ | ✅ | Creates new profile |
| `/deleteProfile` | ✅ | ✅ | ✅ | Soft deletes profile |
| `/selectProfile` | ✅ | ✅ | ✅ | Switches active profile |

### Content & Playback
| Endpoint | iOS | Android Mobile | Android TV | Notes |
|----------|-----|----------------|------------|-------|
| `/fetchHomePageData` | ✅ | ✅ | ✅ | Main content feed |
| `/fetchContentDetails` | ✅ | ✅ | ✅ | Movie/show details |
| `/user/toggle-watchlist` | ✅ | ✅ | ✅ | Add/remove from My List |
| `/watch/update-progress` | ✅ | ✅ | ✅ | Track playback position |
| `/watch/continue-watching` | ✅ | ✅ | ✅ | Resume playback list |

## Field Compatibility Requirements

### Profile Object
```json
{
  "profile_id": "integer - REQUIRED all platforms",
  "name": "string - REQUIRED all platforms",
  "avatar_type": "string - REQUIRED all platforms - values: 'custom', 'color', 'default'",
  "avatar_url": "string|null - OPTIONAL all platforms",
  "avatar_color": "string - REQUIRED Android, OPTIONAL iOS - always return from API",
  "avatar_id": "integer|null - OPTIONAL all platforms",
  "is_kids": "boolean - REQUIRED all platforms",
  "is_kids_profile": "boolean|null - OPTIONAL all platforms",
  "age": "integer|null - OPTIONAL all platforms"
}
```

### Critical Compatibility Rules
1. **NEVER remove fields** from API responses - apps may crash
2. **NEVER change field types** (e.g., string to integer)
3. **Making fields nullable is OK** if clients handle it
4. **Adding new fields is SAFE** - older apps will ignore them
5. **Profile avatar_color must ALWAYS be returned** (Android apps expect it)

## Recent Changes & Issues

### August 2024
- **iOS Profile Edit Fix**: Changed iOS to use `/profile/update` instead of `/updateProfile`
- **Avatar Color Handling**: Made avatar_color optional in iOS to handle null values
- **Profile Image Upload**: Moved from base64 to multipart/form-data for Digital Ocean Spaces

## Development Guidelines

### Before Making API Changes
1. Check all platforms that use the endpoint (search in all app directories)
2. Verify field usage in model classes:
   - iOS: `/ios-mobile-app/Vuga/Models/`
   - Android: `/android-mobile-app/app/src/main/java/com/retry/vuga/model/`
   - Android TV: `/android-tv-app/app/src/main/java/com/vuga/tv/models/`
3. If breaking change is needed, create new versioned endpoint
4. Test on all platforms or document required client updates

### Safe Change Patterns
- ✅ Add optional fields to responses
- ✅ Add new endpoints
- ✅ Make required fields optional in clients
- ✅ Add default values in clients
- ❌ Remove fields from responses
- ❌ Change field types
- ❌ Rename fields without alias
- ❌ Change endpoint URLs without redirect

## Platform-Specific Notes

### iOS
- Uses SwiftUI and Codable for JSON parsing
- Handles optional fields well with `decodeIfPresent`
- Profile images upload directly to Digital Ocean Spaces

### Android Mobile
- Uses Gson for JSON parsing
- Expects all declared fields to be present
- Uses Retrofit for API calls

### Android TV
- Similar to Android Mobile but with TV-specific UI
- Focus on D-pad navigation
- Larger touch targets for remote control

## Testing Checklist
When making changes that affect the API:
- [ ] Test iOS app login and profile selection
- [ ] Test Android app login and profile selection
- [ ] Test profile editing on all platforms
- [ ] Verify watchlist functionality
- [ ] Check content playback
- [ ] Verify "Continue Watching" updates

## Known Issues
1. Some API responses return `"<null>"` as string instead of proper null
2. iOS uses `/profile/update` while Android uses `/updateProfile` for profile updates
3. Avatar color is required by Android but can be null from API for custom avatars
4. Duplicate build files in iOS project (TrailerPlayerView.swift, TrailerInlinePlayer.swift)
5. MongoDB PHP extension warnings (can be ignored)

## System Configuration

### Development Environment
- **macOS**: Darwin 24.6.0
- **PHP**: 8.2 (via Homebrew)
- **MySQL**: via MAMP or local
- **Node.js**: 18+ 
- **Xcode**: 16.0+
- **Android Studio**: Latest stable

### Backend Configuration
- **Laravel .env location**: `/api-backend/public_html/.env`
- **Storage disk**: Digital Ocean Spaces (S3 compatible)
- **Queue driver**: database
- **Cache driver**: file
- **Session driver**: file
- **Broadcast driver**: log

### iOS Build Commands
```bash
# Build for simulator
xcodebuild -workspace Vuga.xcworkspace -scheme Vuga -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 15 Pro'

# Archive for release
xcodebuild -workspace Vuga.xcworkspace -scheme Vuga -configuration Release -archivePath ~/Desktop/Vuga.xcarchive archive

# Export IPA (requires proper certificates)
xcodebuild -exportArchive -archivePath ~/Desktop/Vuga.xcarchive -exportPath ~/Desktop -exportOptionsPlist ExportOptions.plist
```

### Android Build Commands
```bash
# Android Mobile
cd android-mobile-app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Android TV
cd android-tv-app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Connect to Samsung phone via WiFi
adb connect 192.168.1.230:5555
```

### PHP/Laravel Commands
```bash
# Start local server
cd api-backend/public_html
php artisan serve --port=8001

# Run migrations
php artisan migrate

# Clear caches
php artisan cache:clear
php artisan config:clear
php artisan route:clear
```

## File Structure Patterns

### iOS Models
- Location: `/ios-mobile-app/Vuga/Models/`
- Pattern: Codable structs with CodingKeys
- Handle optional fields with `decodeIfPresent`

### Android Models  
- Location: `/android-mobile-app/app/src/main/java/com/retry/vuga/model/`
- Pattern: POJO classes with @SerializedName annotations
- Gson for JSON parsing

### API Controllers
- Location: `/api-backend/public_html/app/Http/Controllers/Api/V2/`
- Pattern: RESTful controllers with validation
- Return consistent JSON responses

## Testing Accounts
- Test User ID: 1
- Test Profile IDs: 1, 17, 37, 38
- API Key for testing: jpwc3pny

## Contact & Documentation
- API Documentation: [Internal docs needed]
- iOS Developer: Gene Avakyan
- Platform: VUGA TV Streaming Service
- Storage: Digital Ocean Spaces (NYC3)

---
*Last Updated: August 2024*
*This document should be updated whenever breaking changes or new patterns are introduced.*