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
use Illuminate\Support\Facades\Route;

Route::get('login', 'API\UserController@registration');
Route::prefix('User')->group(function () {
    Route::post('registration', 'API\UserController@registration');
    Route::post('Logout', 'API\UserController@Logout');
});

Route::prefix('User')->group(function () {
    Route::post('getProfile', 'API\UserController@getProfile');
    Route::post('deleteMyAccount', 'API\UserController@deleteMyAccount');
    Route::post('updateProfile', 'API\UserController@updateProfile');
    Route::post('makeUserSubscribe', 'API\UserController@makeUserSubscribe');
    Route::post('getSubscriptionList', 'API\UserController@getSubscriptionList');
});

Route::prefix('Ads')->group(function () {
    Route::post('fetchCustomAds', [CustomAdsController::class, 'fetchCustomAds']);
    Route::post('increaseAdView', [CustomAdsController::class, 'increaseAdView']);
    Route::post('increaseAdClick', [CustomAdsController::class, 'increaseAdClick']);
});


Route::prefix('Content')->group(function () {
    Route::post('GetHomeContentList', 'API\ContentController@GetHomeContentList');
    Route::post('getAllContentList', 'API\ContentController@getAllContentList');
    Route::post('searchContent', 'API\ContentController@searchContent');
    Route::post('getMovieList', 'API\ContentController@getMovieList');
    Route::post('getSeriesList', 'API\ContentController@getSeriesList');
    Route::post('getAllGenreList', 'API\ContentController@getAllGenreList');
    Route::get('getAllLanguageList', 'API\ContentController@getAllLanguageList');

    Route::post('getContentListByGenreID', 'API\ContentController@getContentListByGenreID');
    Route::post('getContentDetailsByID', 'API\ContentController@getContentDetailsByID');
    Route::post('increaseContentView', 'API\ContentController@increaseContentView');
    Route::post('increaseContentDownload', 'API\ContentController@increaseContentDownload');
    Route::post('increaseContentShare', 'API\ContentController@increaseContentShare');

    Route::post('addComment', 'API\ContentController@addComment');

    Route::post('getSourceByContentID', 'API\ContentController@getSourceByContentID');
    Route::post('getSubtitlesByContentID', 'API\ContentController@getSubtitlesByContentID');

    Route::post('getSeasonByContentID', 'API\ContentController@getSeasonByContentID');
    Route::post('getEpisodeBySeasonID', 'API\ContentController@getEpisodeBySeasonID');
    Route::post('getSourceByEpisodeID', 'API\ContentController@getSourceByEpisodeID');
    Route::post('getSubtitlesByEpisodeID', 'API\ContentController@getSubtitlesByEpisodeID');

    Route::post('addToWatchList', 'API\ContentController@addToWatchList');
    Route::post('removeFromWatchList', 'API\ContentController@removeFromWatchList');
    Route::post('getWatchlist', 'API\ContentController@getWatchlist');

    Route::post('increaseEpisodeView', 'API\ContentController@increaseEpisodeView');
    Route::post('increaseEpisodeDownload', 'API\ContentController@increaseEpisodeDownload');
});

Route::prefix('TV')->group(function () {
    Route::post('GetTvCategoryist', 'API\TVController@GetTvCategoryist');
    Route::post('getAllTvChannelList', 'API\TVController@getAllTvChannelList');
    Route::post('getTvChannelListByCategoryID', 'API\TVController@getTvChannelListByCategoryID');
    Route::post('increaseTVChannelView', 'API\TVController@increaseTVChannelView');
    Route::post('increaseTVChannelShare', 'API\TVController@increaseTVChannelShare');
});
Route::get('getSettings', 'API\SettingsController@getSettings');
Route::post('getAllNotification', 'API\SettingsController@getAllNotification');
Route::get('getSubscriptionPackage', 'API\SettingsController@getSubscriptionPackage');
// });
