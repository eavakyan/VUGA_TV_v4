<?php

use App\Http\Controllers\ActorController;
use App\Http\Controllers\ContentController;
use App\Http\Controllers\CustomAdsController;
use App\Http\Controllers\SettingController;
use App\Http\Controllers\TVAuthController;
use App\Http\Controllers\TVController;
use App\Http\Controllers\UserController;
use Illuminate\Support\Facades\Route;

Route::post('userRegistration', [UserController::class, 'userRegistration'])->middleware('checkHeader');
Route::post('updateProfile', [UserController::class, 'updateProfile'])->middleware('checkHeader');
Route::post('fetchProfile', [UserController::class, 'fetchProfile'])->middleware('checkHeader');
Route::post('logOut', [UserController::class, 'logOut'])->middleware('checkHeader');

Route::post('fetchHomePageData', [ContentController::class, 'fetchHomePageData'])->middleware('checkHeader');
Route::post('fetchWatchList', [ContentController::class, 'fetchWatchList'])->middleware('checkHeader');
Route::post('fetchContentsByGenre', [ContentController::class, 'fetchContentsByGenre'])->middleware('checkHeader');
Route::post('fetchContentDetails', [ContentController::class, 'fetchContentDetails'])->middleware('checkHeader');
Route::post('searchContent', [ContentController::class, 'searchContent'])->middleware('checkHeader');
Route::post('increaseContentView', [ContentController::class, 'increaseContentView'])->middleware('checkHeader');
Route::post('increaseContentDownload', [ContentController::class, 'increaseContentDownload'])->middleware('checkHeader');
Route::post('increaseContentShare', [ContentController::class, 'increaseContentShare'])->middleware('checkHeader');

Route::post('increaseEpisodeView', [ContentController::class, 'increaseEpisodeView'])->middleware('checkHeader');
Route::post('increaseEpisodeDownload', [ContentController::class, 'increaseEpisodeDownload'])->middleware('checkHeader');

Route::post('fetchLiveTVPageData', [TVController::class, 'fetchLiveTVPageData'])->middleware('checkHeader');
Route::post('fetchTVChannelByCategory', [TVController::class, 'fetchTVChannelByCategory'])->middleware('checkHeader');
Route::post('searchTVChannel', [TVController::class, 'searchTVChannel'])->middleware('checkHeader');
Route::post('increaseTVChannelView', [TVController::class, 'increaseTVChannelView'])->middleware('checkHeader');
Route::post('increaseTVChannelShare', [TVController::class, 'increaseTVChannelShare'])->middleware('checkHeader');

Route::post('fetchSettings', [SettingController::class, 'fetchSettings'])->middleware('checkHeader');

Route::post('deleteMyAccount', [UserController::class, 'deleteMyAccount'])->middleware('checkHeader');

Route::post('fetchCustomAds', [CustomAdsController::class, 'fetchCustomAds'])->middleware('checkHeader');
Route::post('increaseAdMetric', [CustomAdsController::class, 'increaseAdMetric'])->middleware('checkHeader');

Route::post('fetchActorDetails', [ActorController::class, 'fetchActorDetails'])->middleware('checkHeader');
Route::post('deleteFile', [ActorController::class, 'deleteFile'])->middleware('checkHeader');

// TV Authentication Routes
Route::prefix('TV')->group(function () {
    Route::post('generateAuthSession', [TVAuthController::class, 'generateAuthSession'])->middleware('checkHeader');
    Route::post('checkAuthStatus', [TVAuthController::class, 'checkAuthStatus'])->middleware('checkHeader');
    Route::post('authenticateSession', [TVAuthController::class, 'authenticateSession'])->middleware('checkHeader');
    Route::post('completeAuth', [TVAuthController::class, 'completeAuth'])->middleware('checkHeader');
});
