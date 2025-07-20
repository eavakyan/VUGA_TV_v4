<?php
/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| is assigned the "api" middleware group. Enjoy building your API!
|
*/

use App\Http\Controllers\CustomAdsController;
use App\Http\Controllers\Api\UserController;
use App\Http\Controllers\Api\ContentController;
use App\Http\Controllers\Api\ContentDetailsController;
use App\Http\Controllers\Api\AnalyticsController;
use App\Http\Controllers\Api\WatchlistController;
use App\Http\Controllers\Api\TVController;
use App\Http\Controllers\Api\SettingsController;
use Illuminate\Support\Facades\Route;

// User Authentication & Profile Routes
Route::get('login', [UserController::class, 'registration']);
Route::prefix('User')->group(function () {
    Route::post('registration', [UserController::class, 'registration']);
    Route::post('Logout', [UserController::class, 'logout']);
    Route::post('getProfile', [UserController::class, 'getProfile']);
    Route::post('deleteMyAccount', [UserController::class, 'deleteMyAccount']);
    Route::post('updateProfile', [UserController::class, 'updateProfile']);
    Route::post('makeUserSubscribe', [UserController::class, 'makeUserSubscribe']);
    Route::post('getSubscriptionList', [UserController::class, 'getSubscriptionList']);
});

// Custom Ads Routes
Route::prefix('Ads')->group(function () {
    Route::post('fetchCustomAds', [CustomAdsController::class, 'fetchCustomAds']);
    Route::post('increaseAdView', [CustomAdsController::class, 'increaseAdView']);
    Route::post('increaseAdClick', [CustomAdsController::class, 'increaseAdClick']);
});

// Content Routes (List & Discovery)
Route::prefix('Content')->group(function () {
    // Content Lists
    Route::post('GetHomeContentList', [ContentController::class, 'getHomeContentList']);
    Route::post('getAllContentList', [ContentController::class, 'getAllContentList']);
    Route::post('getMovieList', [ContentController::class, 'getMovieList']);
    Route::post('getSeriesList', [ContentController::class, 'getSeriesList']);
    Route::post('getAllGenreList', [ContentController::class, 'getAllGenreList']);
    Route::get('getAllLanguageList', [ContentDetailsController::class, 'getAllLanguageList']);
    Route::post('getContentListByGenreID', [ContentController::class, 'getContentListByGenreID']);
    
    // Content Details & Search
    Route::post('searchContent', [ContentDetailsController::class, 'searchContent']);
    Route::post('getContentDetailsByID', [ContentDetailsController::class, 'getContentDetailsByID']);
    
    // Content Media (Sources & Subtitles)
    Route::post('getSourceByContentID', [ContentDetailsController::class, 'getSourceByContentID']);
    Route::post('getSubtitlesByContentID', [ContentDetailsController::class, 'getSubtitlesByContentID']);
    
    // Series Content (Seasons & Episodes)
    Route::post('getSeasonByContentID', [ContentDetailsController::class, 'getSeasonByContentID']);
    Route::post('getEpisodeBySeasonID', [ContentDetailsController::class, 'getEpisodeBySeasonID']);
    Route::post('getSourceByEpisodeID', [ContentDetailsController::class, 'getSourceByEpisodeID']);
    Route::post('getSubtitlesByEpisodeID', [ContentDetailsController::class, 'getSubtitlesByEpisodeID']);
    
    // Analytics & Tracking
    Route::post('increaseContentView', [AnalyticsController::class, 'increaseContentView']);
    Route::post('increaseContentDownload', [AnalyticsController::class, 'increaseContentDownload']);
    Route::post('increaseContentShare', [AnalyticsController::class, 'increaseContentShare']);
    Route::post('increaseEpisodeView', [AnalyticsController::class, 'increaseEpisodeView']);
    Route::post('increaseEpisodeDownload', [AnalyticsController::class, 'increaseEpisodeDownload']);
    
    // Watchlist Management
    Route::post('addToWatchList', [WatchlistController::class, 'addToWatchList']);
    Route::post('removeFromWatchList', [WatchlistController::class, 'removeFromWatchList']);
    Route::post('getWatchlist', [WatchlistController::class, 'getWatchlist']);
    
    // Comments (TODO: Move to separate CommentController)
    Route::post('addComment', 'API\ContentController@addComment');
});

// TV Routes
Route::prefix('TV')->group(function () {
    Route::post('GetTvCategoryist', [TVController::class, 'GetTvCategoryist']);
    Route::post('getAllTvChannelList', [TVController::class, 'getAllTvChannelList']);
    Route::post('getTvChannelListByCategoryID', [TVController::class, 'getTvChannelListByCategoryID']);
    Route::post('increaseTVChannelView', [TVController::class, 'increaseTVChannelView']);
    Route::post('increaseTVChannelShare', [TVController::class, 'increaseTVChannelShare']);
});

// Settings Routes
Route::get('getSettings', [SettingsController::class, 'getSettings']);
Route::post('getAllNotification', [SettingsController::class, 'getAllNotification']);
Route::get('getSubscriptionPackage', [SettingsController::class, 'getSubscriptionPackage']);
