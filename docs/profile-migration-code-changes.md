# Profile-Level Migration: Code Changes Required

## Overview
This document outlines all code changes required to migrate favorites, ratings, and watch history from user-level to profile-level across all applications.

## 1. API Backend (Laravel/PHP)

### 1.1 Database Migration
- **File**: `/api-backend/public_html/database/migrate_favorites_ratings_history_to_profile.sql`
- **Status**: ✅ Created
- **Action**: Run after all code updates are complete

### 1.2 Model Updates

#### AppUser.php
**File**: `/api-backend/public_html/app/Models/V2/AppUser.php`

Remove or update these relationships:
```php
// OLD - Remove these
public function favorites()
{
    return $this->belongsToMany(Content::class, 'app_user_favorite', 'app_user_id', 'content_id')
        ->withTimestamps();
}

public function ratings()
{
    return $this->belongsToMany(Content::class, 'app_user_rating', 'app_user_id', 'content_id')
        ->withPivot('rating')
        ->withTimestamps();
}

public function watchHistory()
{
    return $this->hasMany(AppUserWatchHistory::class, 'app_user_id');
}
```

#### AppUserProfile.php
**File**: `/api-backend/public_html/app/Models/V2/AppUserProfile.php`

Add new relationships:
```php
public function favorites()
{
    return $this->belongsToMany(Content::class, 'app_user_favorite', 'profile_id', 'content_id')
        ->withTimestamps();
}

public function ratings()
{
    return $this->belongsToMany(Content::class, 'app_user_rating', 'profile_id', 'content_id')
        ->withPivot('rating')
        ->withTimestamps();
}

public function watchHistory()
{
    return $this->hasMany(AppUserWatchHistory::class, 'profile_id');
}
```

#### Content.php
**File**: `/api-backend/public_html/app/Models/V2/Content.php`

Update relationships:
```php
// Change from app_user_id to profile_id
public function favoritedBy()
{
    return $this->belongsToMany(AppUserProfile::class, 'app_user_favorite', 'content_id', 'profile_id')
        ->withTimestamps();
}

public function profileRatings()
{
    return $this->belongsToMany(AppUserProfile::class, 'app_user_rating', 'content_id', 'profile_id')
        ->withPivot('rating')
        ->withTimestamps();
}
```

#### AppUserWatchHistory.php
**File**: `/api-backend/public_html/app/Models/V2/AppUserWatchHistory.php`

Update model:
```php
protected $fillable = [
    'profile_id',  // Change from app_user_id
    'content_id',
    'episode_id',
    'last_watched_position',
    'total_duration',
    'completed',
    'device_type'
];

public function profile()
{
    return $this->belongsTo(AppUserProfile::class, 'profile_id');
}
```

### 1.3 Controller Updates

#### UserController.php
**File**: `/api-backend/public_html/app/Http/Controllers/Api/V2/UserController.php`

1. Update `toggleFavorite` method (lines 390-407):
```php
if ($profile->favorites()->where('app_user_favorite.content_id', $request->content_id)->exists()) {
    // Remove from favorites
    $profile->favorites()->detach($request->content_id);
    $message = 'Removed from favorites';
} else {
    // Add to favorites
    $profile->favorites()->attach($request->content_id);
    $message = 'Added to favorites';
}
```

2. Remove user-level fallback code (lines 405-417)

3. Update `rateContent` method:
```php
public function rateContent(Request $request)
{
    $validator = Validator::make($request->all(), [
        'app_user_id' => 'required|integer|exists:app_user,app_user_id',
        'content_id' => 'required|integer|exists:content,content_id',
        'rating' => 'required|numeric|min:0|max:10',
        'profile_id' => 'required|integer|exists:app_user_profile,profile_id'
    ]);

    // ... validation code ...

    $profile = AppUserProfile::find($request->profile_id);
    
    // Verify profile belongs to user
    if ($profile->app_user_id != $request->app_user_id) {
        return response()->json(['status' => false, 'message' => 'Unauthorized'], 403);
    }
    
    // Update or create rating
    $profile->ratings()->syncWithoutDetaching([
        $request->content_id => ['rating' => $request->rating]
    ]);

    // Update content average rating
    $this->updateContentAverageRating($request->content_id);

    return response()->json([
        'status' => true,
        'message' => 'Rating submitted successfully'
    ]);
}
```

4. Update `updateContentAverageRating` helper method:
```php
private function updateContentAverageRating($contentId)
{
    $avgRating = DB::table('app_user_rating')
                    ->where('content_id', $contentId)
                    ->avg('rating');
    
    Content::where('content_id', $contentId)
           ->update(['ratings' => $avgRating ?: 0]);
}
```

#### WatchHistoryController.php
Create new controller or update existing:
```php
public function updateWatchProgress(Request $request)
{
    $validator = Validator::make($request->all(), [
        'app_user_id' => 'required|integer|exists:app_user,app_user_id',
        'profile_id' => 'required|integer|exists:app_user_profile,profile_id',
        'content_id' => 'nullable|integer|exists:content,content_id',
        'episode_id' => 'nullable|integer|exists:episode,episode_id',
        'last_watched_position' => 'required|integer|min:0',
        'total_duration' => 'required|integer|min:0',
        'completed' => 'nullable|boolean',
        'device_type' => 'nullable|integer'
    ]);

    // Verify profile belongs to user
    $profile = AppUserProfile::find($request->profile_id);
    if ($profile->app_user_id != $request->app_user_id) {
        return response()->json(['status' => false, 'message' => 'Unauthorized'], 403);
    }

    // Update or create watch history
    $watchHistory = AppUserWatchHistory::updateOrCreate(
        [
            'profile_id' => $request->profile_id,
            'content_id' => $request->content_id,
            'episode_id' => $request->episode_id
        ],
        [
            'last_watched_position' => $request->last_watched_position,
            'total_duration' => $request->total_duration,
            'completed' => $request->completed ?? false,
            'device_type' => $request->device_type ?? 0
        ]
    );

    return response()->json([
        'status' => true,
        'message' => 'Watch progress updated',
        'data' => $watchHistory
    ]);
}
```

### 1.4 API Routes
**File**: `/api-backend/public_html/routes/api_v2.php`

Add new endpoints if missing:
```php
Route::post('/user/toggle-favorite', [V2\UserController::class, 'toggleFavorite']);
Route::post('/user/rate-content', [V2\UserController::class, 'rateContent']);
Route::post('/watch/update-progress', [V2\WatchHistoryController::class, 'updateWatchProgress']);
```

## 2. Android Mobile App

### 2.1 Update API Calls

#### BaseActivity.java
Ensure all API calls include profile_id:

```java
// Add helper method to get current profile ID
private static Integer getCurrentProfileId() {
    return sessionManager.getUser().getLastActiveProfileId();
}

// Update any favorites toggle
public static void toggleFavorite(Context context, int content_id, OnFavoriteListener listener) {
    Integer profileId = getCurrentProfileId();
    if (profileId == null || profileId == 0) {
        Log.e("Favorite", "No valid profile ID");
        return;
    }
    
    HashMap<String, Object> params = new HashMap<>();
    params.put("app_user_id", sessionManager.getUser().getId());
    params.put("content_id", content_id);
    params.put("profile_id", profileId);
    
    // Make API call...
}
```

#### MovieDetailActivity.java
Update rating submission:
```java
private void submitRating(float rating) {
    Integer profileId = sessionManager.getUser().getLastActiveProfileId();
    if (profileId == null || profileId == 0) {
        showError("Please select a profile");
        return;
    }
    
    HashMap<String, Object> params = new HashMap<>();
    params.put("app_user_id", sessionManager.getUser().getId());
    params.put("content_id", contentId);
    params.put("profile_id", profileId);
    params.put("rating", rating);
    
    // Make API call...
}
```

### 2.2 Update RetrofitService
Add profile_id to relevant API methods:
```java
@FormUrlEncoded
@POST("user/toggle-favorite")
Single<UserRegistration> toggleFavorite(@FieldMap HashMap<String, Object> params);

@FormUrlEncoded
@POST("user/rate-content")
Single<RestResponse> rateContent(@FieldMap HashMap<String, Object> params);

@FormUrlEncoded
@POST("watch/update-progress")
Single<RestResponse> updateWatchProgress(@FieldMap HashMap<String, Object> params);
```

## 3. iOS Mobile App

### 3.1 Update API Endpoints

#### APIs.swift
Add new endpoints:
```swift
case toggleFavorite = "user/toggle-favorite"
case rateContent = "user/rate-content"
case updateWatchProgress = "watch/update-progress"
```

### 3.2 Update ViewModels

#### ContentDetailViewModel.swift
Update favorites toggle:
```swift
func toggleFavorite() {
    guard let profileId = myUser?.lastActiveProfileId, profileId > 0 else {
        print("No valid profile ID")
        return
    }
    
    var params: [Params: Any] = [
        .appUserId: myUser?.id ?? 0,
        .contentId: content?.id ?? 0,
        .profileId: profileId
    ]
    
    NetworkManager.callWebService(url: .toggleFavorite, params: params) { [weak self] (obj: UserModel) in
        // Handle response
    }
}

func submitRating(_ rating: Float) {
    guard let profileId = myUser?.lastActiveProfileId, profileId > 0 else {
        print("No valid profile ID")
        return
    }
    
    var params: [Params: Any] = [
        .appUserId: myUser?.id ?? 0,
        .contentId: content?.id ?? 0,
        .profileId: profileId,
        .rating: rating
    ]
    
    NetworkManager.callWebService(url: .rateContent, params: params) { [weak self] (obj: StatusModel) in
        // Handle response
    }
}
```

#### VideoPlayerModel.swift or similar
Update watch progress tracking:
```swift
func updateWatchProgress(position: Int, duration: Int, completed: Bool = false) {
    guard let profileId = myUser?.lastActiveProfileId, profileId > 0 else {
        return
    }
    
    var params: [Params: Any] = [
        .appUserId: myUser?.id ?? 0,
        .profileId: profileId,
        .lastWatchedPosition: position,
        .totalDuration: duration,
        .completed: completed,
        .deviceType: 1 // iOS
    ]
    
    if let contentId = currentContent?.id {
        params[.contentId] = contentId
    }
    
    if let episodeId = currentEpisode?.id {
        params[.episodeId] = episodeId
    }
    
    NetworkManager.callWebService(url: .updateWatchProgress, params: params) { (obj: StatusModel) in
        // Handle response
    }
}
```

## 4. Android TV App

### 4.1 Update Data Models and API Calls

Similar changes to Android Mobile app but in Kotlin:

```kotlin
// Ensure profile_id is included in all relevant API calls
fun toggleFavorite(contentId: Int) {
    val profileId = sessionManager.user?.lastActiveProfileId ?: return
    
    val params = hashMapOf(
        "app_user_id" to (sessionManager.user?.id ?: 0),
        "content_id" to contentId,
        "profile_id" to profileId
    )
    
    // Make API call
}

fun updateWatchProgress(contentId: Int?, episodeId: Int?, position: Long, duration: Long) {
    val profileId = sessionManager.user?.lastActiveProfileId ?: return
    
    val params = hashMapOf(
        "app_user_id" to (sessionManager.user?.id ?: 0),
        "profile_id" to profileId,
        "last_watched_position" to (position / 1000).toInt(), // Convert to seconds
        "total_duration" to (duration / 1000).toInt(),
        "device_type" to 2 // Android TV
    )
    
    contentId?.let { params["content_id"] = it }
    episodeId?.let { params["episode_id"] = it }
    
    // Make API call
}
```

## 5. Admin Panel Updates

### 5.1 Update Queries
Any admin panel queries that show user favorites, ratings, or watch history need to be updated to join through profiles:

```sql
-- Old query
SELECT u.*, COUNT(f.content_id) as favorite_count 
FROM app_user u 
LEFT JOIN app_user_favorite f ON u.app_user_id = f.app_user_id 
GROUP BY u.app_user_id;

-- New query
SELECT u.*, COUNT(f.content_id) as favorite_count 
FROM app_user u 
LEFT JOIN app_user_profile p ON u.app_user_id = p.app_user_id
LEFT JOIN app_user_favorite f ON p.profile_id = f.profile_id 
GROUP BY u.app_user_id;
```

## Migration Steps

1. **Phase 1**: Update all backend models and controllers
2. **Phase 2**: Deploy backend changes with backward compatibility
3. **Phase 3**: Update and release all mobile apps
4. **Phase 4**: Run database migration script
5. **Phase 5**: Remove backward compatibility code
6. **Phase 6**: Drop old user-level tables

## Testing Checklist

- [ ] Profile can add/remove favorites
- [ ] Profile can rate content
- [ ] Profile watch history is tracked correctly
- [ ] Different profiles have separate favorites/ratings/history
- [ ] Average ratings are calculated correctly
- [ ] Continue watching shows profile-specific content
- [ ] Admin panel shows correct data