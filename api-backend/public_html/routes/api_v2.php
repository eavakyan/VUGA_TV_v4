<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\V2;

/*
|--------------------------------------------------------------------------
| API V2 Routes
|--------------------------------------------------------------------------
|
| These routes use the new database schema with singular table names
| and renamed primary keys (e.g., app_user_id instead of id)
|
*/

Route::prefix('v2')->group(function () {
    
    // V1 Compatible Routes (for mobile apps)
    Route::post('/fetchSettings', [V2\SettingController::class, 'fetchSettings']);
    Route::post('/userRegistration', [V2\UserController::class, 'userRegistration']);
    Route::post('/fetchProfile', [V2\UserController::class, 'fetchProfile']);
    Route::post('/fetchHomePageData', [V2\ContentController::class, 'fetchHomePageData']);
    Route::post('/fetchContentDetails', [V2\ContentController::class, 'fetchContentDetail']);
    Route::post('/fetchContentsByGenre', [V2\ContentController::class, 'fetchContentsByGenre']);
    Route::post('/fetchWatchList', [V2\UserController::class, 'fetchWatchList']);
    Route::post('/searchContent', [V2\ContentController::class, 'searchContent']);
    Route::post('/updateProfile', [V2\UserController::class, 'updateProfile']);
    Route::post('/logOut', [V2\UserController::class, 'logOut']);
    Route::post('/deleteMyAccount', [V2\UserController::class, 'deleteMyAccount']);
    Route::post('/increaseContentView', [V2\ContentController::class, 'increaseContentView']);
    Route::post('/increaseContentShare', [V2\ContentController::class, 'increaseContentShare']);
    Route::post('/increaseContentDownload', [V2\ContentController::class, 'increaseContentDownload']);
    Route::post('/increaseEpisodeView', [V2\ContentController::class, 'increaseEpisodeView']);
    Route::post('/increaseEpisodeDownload', [V2\ContentController::class, 'increaseEpisodeDownload']);
    Route::post('/fetchActorDetails', [V2\ActorController::class, 'fetchActorDetails']);
    Route::post('/fetchCustomAds', [V2\CustomAdController::class, 'fetchCustomAds']);
    Route::post('/increaseAdMetric', [V2\CustomAdController::class, 'increaseAdMetric']);
    Route::post('/increaseAdView', [V2\CustomAdController::class, 'increaseAdView']);
    Route::post('/increaseAdClick', [V2\CustomAdController::class, 'increaseAdClick']);
    Route::post('/getUserSubscription', [V2\UserController::class, 'getUserSubscription']);
    Route::post('/getAllNotification', [V2\NotificationController::class, 'getAllNotification']);
    Route::post('/fetchLiveTVPageData', [V2\LiveTvController::class, 'fetchLiveTVPageData']);
    Route::post('/fetchTVChannelByCategory', [V2\LiveTvController::class, 'fetchTVChannelByCategory']);
    Route::post('/searchTVChannel', [V2\LiveTvController::class, 'searchTVChannel']);
    Route::post('/increaseTVChannelView', [V2\LiveTvController::class, 'increaseTVChannelView']);
    Route::post('/increaseTVChannelShare', [V2\LiveTvController::class, 'increaseTVChannelShare']);
    
    // User Management
    Route::prefix('user')->group(function () {
        Route::post('/registration', [V2\UserController::class, 'userRegistration']);
        Route::post('/update-profile', [V2\UserController::class, 'updateProfile']);
        Route::post('/fetch-profile', [V2\UserController::class, 'fetchProfile']);
        Route::post('/logout', [V2\UserController::class, 'logOut']);
        Route::post('/delete-account', [V2\UserController::class, 'deleteMyAccount']);
        Route::post('/toggle-watchlist', [V2\UserController::class, 'toggleWatchlist']);
        Route::post('/toggle-favorite', [V2\UserController::class, 'toggleFavorite']);
        Route::post('/rate-content', [V2\UserController::class, 'rateContent']);
        Route::post('/rate-episode', [V2\UserController::class, 'rateEpisode']);
        Route::post('/watch-history', [V2\UserController::class, 'getWatchHistory']);
    });
    
    // Content Management
    Route::prefix('content')->group(function () {
        Route::post('/home-page', [V2\ContentController::class, 'getHomePageData']);
        Route::post('/all', [V2\ContentController::class, 'getAllContent']);
        Route::post('/detail', [V2\ContentController::class, 'getContentById']);
        Route::post('/by-ids', [V2\ContentController::class, 'getContentsByIds']);
        Route::post('/search', [V2\ContentController::class, 'searchContent']);
        Route::post('/featured', [V2\ContentController::class, 'getFeaturedContent']);
        Route::post('/trending', [V2\ContentController::class, 'getTrendingContent']);
        Route::post('/new', [V2\ContentController::class, 'getNewContent']);
        Route::post('/by-genre', [V2\ContentController::class, 'getContentByGenre']);
        Route::post('/increase-view', [V2\ContentController::class, 'increaseContentView']);
        Route::post('/increase-share', [V2\ContentController::class, 'increaseContentShare']);
        Route::post('/increase-download', [V2\ContentController::class, 'increaseContentDownload']);
        Route::post('/episode/increase-view', [V2\ContentController::class, 'increaseEpisodeView']);
        Route::post('/episode/increase-download', [V2\ContentController::class, 'increaseEpisodeDownload']);
        
        // Trailer Management
        Route::post('/trailers', [V2\ContentTrailerController::class, 'getContentTrailers']);
        Route::post('/trailer/primary', [V2\ContentTrailerController::class, 'getPrimaryTrailer']);
        Route::post('/trailer/add', [V2\ContentTrailerController::class, 'addTrailer']);
        Route::post('/trailer/update', [V2\ContentTrailerController::class, 'updateTrailer']);
        Route::post('/trailer/delete', [V2\ContentTrailerController::class, 'deleteTrailer']);
        Route::post('/trailer/set-primary', [V2\ContentTrailerController::class, 'setPrimaryTrailer']);
        Route::post('/trailer/reorder', [V2\ContentTrailerController::class, 'reorderTrailers']);
    });
    
    // Watch History & Progress
    Route::prefix('watch')->group(function () {
        Route::post('/update-progress', [V2\WatchHistoryController::class, 'updateWatchProgress']);
        Route::post('/continue-watching', [V2\WatchHistoryController::class, 'getContinueWatching']);
        Route::post('/mark-completed', [V2\WatchHistoryController::class, 'markAsCompleted']);
    });
    
    // Genres
    Route::prefix('genre')->group(function () {
        Route::post('/all', [V2\GenreController::class, 'getAllGenres']);
        Route::post('/with-content-count', [V2\GenreController::class, 'getGenresWithContentCount']);
    });
    
    // Languages
    Route::prefix('language')->group(function () {
        Route::post('/all', [V2\LanguageController::class, 'getAllLanguages']);
    });
    
    // Actors
    Route::prefix('actor')->group(function () {
        Route::post('/detail', [V2\ActorController::class, 'getActorDetail']);
        Route::post('/all', [V2\ActorController::class, 'getAllActors']);
    });
    
    // Settings
    Route::post('/fetchSettings', [V2\SettingsController::class, 'getAppSettings']);
    Route::prefix('settings')->group(function () {
        Route::post('/app', [V2\SettingsController::class, 'getAppSettings']);
        Route::post('/cms-pages', [V2\SettingsController::class, 'getCmsPages']);
    });
    
    // TV Authentication
    Route::prefix('tv-auth')->group(function () {
        Route::post('/generate-session', [V2\TvAuthController::class, 'generateSession']);
        Route::post('/authenticate', [V2\TvAuthController::class, 'authenticate']);
        Route::post('/check-status', [V2\TvAuthController::class, 'checkStatus']);
        Route::post('/logout', [V2\TvAuthController::class, 'logout']);
    });
    
    // Notifications
    Route::prefix('notification')->group(function () {
        Route::post('/all', [V2\NotificationController::class, 'getAllNotifications']);
        Route::post('/send', [V2\NotificationController::class, 'sendNotification']);
    });
    
    // Custom Ads
    Route::prefix('ads')->group(function () {
        Route::post('/all', [V2\CustomAdController::class, 'getCustomAds']);
        Route::post('/active', [V2\CustomAdController::class, 'getActiveAds']);
        Route::post('/detail', [V2\CustomAdController::class, 'getCustomAd']);
        Route::post('/create', [V2\CustomAdController::class, 'createCustomAd']);
        Route::post('/update', [V2\CustomAdController::class, 'updateCustomAd']);
        Route::post('/delete', [V2\CustomAdController::class, 'deleteCustomAd']);
    });
    
    // Live TV
    Route::prefix('live-tv')->group(function () {
        Route::post('/page-data', [V2\LiveTvController::class, 'getLiveTvPageData']);
        Route::post('/channels', [V2\LiveTvController::class, 'getAllChannels']);
        Route::post('/categories', [V2\LiveTvController::class, 'getCategories']);
        Route::post('/channels-by-category', [V2\LiveTvController::class, 'getChannelsByCategory']);
        Route::post('/channel-detail', [V2\LiveTvController::class, 'getChannelDetails']);
        Route::post('/search', [V2\LiveTvController::class, 'searchChannels']);
        Route::post('/increase-view', [V2\LiveTvController::class, 'increaseChannelView']);
        Route::post('/increase-share', [V2\LiveTvController::class, 'increaseChannelShare']);
    });
    
    // Profile Management (V1 Compatible routes)
    Route::post('/getUserProfiles', [V2\ProfileController::class, 'getUserProfiles']);
    Route::post('/createProfile', [V2\ProfileController::class, 'createProfile']);
    // Route::post('/updateProfile', [V2\ProfileController::class, 'updateProfile']); // Commented out to avoid conflict with UserController updateProfile
    Route::post('/deleteProfile', [V2\ProfileController::class, 'deleteProfile']);
    Route::post('/selectProfile', [V2\ProfileController::class, 'selectProfile']);
    Route::post('/getDefaultAvatars', [V2\ProfileController::class, 'getDefaultAvatars']);
    
    // Profile Management
    Route::prefix('profile')->group(function () {
        Route::post('/list', [V2\ProfileController::class, 'getUserProfiles']);
        Route::post('/create', [V2\ProfileController::class, 'createProfile']);
        Route::post('/update', [V2\ProfileController::class, 'updateProfile']);
        Route::post('/delete', [V2\ProfileController::class, 'deleteProfile']);
        Route::post('/select', [V2\ProfileController::class, 'selectProfile']);
        Route::post('/avatars', [V2\ProfileController::class, 'getDefaultAvatars']);
        Route::post('/update-age-settings', [V2\ProfileController::class, 'updateAgeSettings']);
        Route::post('/age-ratings', [V2\ProfileController::class, 'getAgeRatings']);
    });
    
    // User Notifications (One-time messages for profiles)
    Route::prefix('user-notification')->group(function () {
        // Client endpoints
        Route::post('/pending', [V2\UserNotificationController::class, 'getPendingNotifications']);
        Route::post('/mark-shown', [V2\UserNotificationController::class, 'markNotificationShown']);
        Route::post('/dismiss', [V2\UserNotificationController::class, 'dismissNotification']);
        
        // Admin endpoints
        Route::prefix('admin')->group(function () {
            Route::post('/create', [V2\UserNotificationController::class, 'createNotification']);
            Route::post('/list', [V2\UserNotificationController::class, 'getNotificationsList']);
            Route::post('/update/{notificationId}', [V2\UserNotificationController::class, 'updateNotification']);
            Route::delete('/delete/{notificationId}', [V2\UserNotificationController::class, 'deleteNotification']);
            Route::get('/analytics/{notificationId}', [V2\UserNotificationController::class, 'getNotificationAnalytics']);
        });
    });
    
    // Subscription Management
    Route::prefix('subscription')->group(function () {
        Route::post('/plans', [V2\SubscriptionController::class, 'getPlans']);
        Route::post('/my-subscriptions', [V2\SubscriptionController::class, 'getMySubscriptions']);
        Route::post('/validate-promo', [V2\SubscriptionController::class, 'validatePromoCode']);
        Route::post('/create', [V2\SubscriptionController::class, 'createSubscription']);
        Route::post('/cancel', [V2\SubscriptionController::class, 'cancelSubscription']);
        Route::post('/payment-history', [V2\SubscriptionController::class, 'getPaymentHistory']);
    });
    
    // Test endpoint to verify V2 API is working
    Route::get('/test', function () {
        return response()->json([
            'status' => true,
            'message' => 'V2 API is working',
            'version' => '2.0.0',
            'schema' => 'Updated with singular table names and new primary keys'
        ]);
    });
});