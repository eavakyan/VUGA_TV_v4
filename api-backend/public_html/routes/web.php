<?php

use App\Http\Controllers\CustomAdsController;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| contains the "web" middleware group. Now create something great!
|
*/

Route::get('/', 'Admin\AdminController@showLogin')->name('login');
Route::post('dologin', 'Admin\AdminController@doLogin')->name('login.submit');
Route::get('logout/{flag}', 'Admin\AdminController@logout')->name('logout');
Route::get('privacy-policy', 'Admin\PrivacyPolicyController@viewPrivacyPolicy')->name('privacy-policy');
Route::get('terms-condition', 'Admin\TermsConditionController@viewTermsCondition')->name('terms-condition');

Route::group(array('middleware' => 'checkRole'), function () {
    // Route::group(['middleware' => ['auth', 'admin']], function () {

    Route::get('dashboard', 'Admin\AdminController@showDashboard')->name('dashboard');
    Route::get('my-profile', 'Admin\AdminController@MyProfile')->name('my-profile');
    Route::post('updateAdminProfile', 'Admin\AdminController@updateAdminProfile')->name('updateAdminProfile');

    Route::prefix('user')->group(function () {
        Route::get('list', 'Admin\UserController@viewListUser')->name('user/list');
        Route::post('showUserList', 'Admin\UserController@showUserList')->name('showUserList');
        Route::get('view/{id}', 'Admin\UserController@viewUser')->name('user/view');
        Route::post('deleteUser', 'Admin\UserController@deleteUser')->name('deleteUser');
        Route::post('updateUserProfile', 'Admin\UserController@updateUserProfile')->name('updateUserProfile');
    });

    Route::prefix('content')->group(function () {
        Route::get('list', 'Admin\ContentController@viewListContent')->name('content/list');
        Route::get('add', 'Admin\ContentController@viewAddContent')->name('content/add');
        Route::get('edit/{flag?}/{id}', 'Admin\ContentController@viewUpdateContent')->name('content/edit');
        Route::get('view/{id}', 'Admin\ContentController@viewContent')->name('content/view');
        Route::post('addUpdateContent', 'Admin\ContentController@addUpdateContent')->name('addUpdateContent');
        Route::post('showContentList', 'Admin\ContentController@showContentList')->name('showContentList');
        Route::post('deleteContent', 'Admin\ContentController@deleteContent')->name('deleteContent');
        Route::post('changeFeatureStatus', 'Admin\ContentController@changeFeatureStatus')->name('changeFeatureStatus');
        Route::post('UpdateContentMedia', 'Admin\ContentController@UpdateContentMedia')->name('UpdateContentMedia');

        Route::post('UploadContentSourceMedia', 'Admin\ContentController@UploadContentSourceMedia')->name('UploadContentSourceMedia');

        Route::post('showContentSourceList', 'Admin\ContentController@showContentSourceList')->name('showContentSourceList');
        Route::post('addUpdateContentSource', 'Admin\ContentController@addUpdateContentSource')->name('addUpdateContentSource');
        Route::post('deleteContentSource', 'Admin\ContentController@deleteContentSource')->name('deleteContentSource');

        Route::post('addUpdateContentSubtitles', 'Admin\ContentController@addUpdateContentSubtitles')->name('addUpdateContentSubtitles');
        Route::post('deleteContentSubtitles', 'Admin\ContentController@deleteContentSubtitles')->name('deleteContentSubtitles');
        Route::post('showContentSubtitlesList', 'Admin\ContentController@showContentSubtitlesList')->name('showContentSubtitleList');

        Route::post('showSeasonEpisodeList', 'Admin\ContentController@showSeasonEpisodeList')->name('showSeasonEpisodeList');
        Route::post('addUpdateSeriesSeason', 'Admin\ContentController@addUpdateSeriesSeason')->name('addUpdateSeriesSeason');
        Route::post('deleteSeriesSeason', 'Admin\ContentController@deleteSeriesSeason')->name('deleteSeriesSeason');

        Route::post('addUpdateSeasonEpisode', 'Admin\ContentController@addUpdateSeasonEpisode')->name('addUpdateSeasonEpisode');
        Route::post('deleteSeasonEpisode', 'Admin\ContentController@deleteSeasonEpisode')->name('deleteSeasonEpisode');

        Route::post('showEpisodeSourceList', 'Admin\ContentController@showEpisodeSourceList')->name('showEpisodeSourceList');
        Route::post('addUpdateEpisodeSource', 'Admin\ContentController@addUpdateEpisodeSource')->name('addUpdateEpisodeSource');
        Route::post('deleteEpisodeSource', 'Admin\ContentController@deleteEpisodeSource')->name('deleteEpisodeSource');

        Route::post('showEpisodeSubtitlesList', 'Admin\ContentController@showEpisodeSubtitlesList')->name('showEpisodeSubtitlesList');
        Route::post('addUpdateEpisodeSubtitles', 'Admin\ContentController@addUpdateEpisodeSubtitles')->name('addUpdateEpisodeSubtitles');
        Route::post('deleteEpisodeSubtitles', 'Admin\ContentController@deleteEpisodeSubtitles')->name('deleteEpisodeSubtitles');

        Route::post('showContentCommentList', 'Admin\ContentController@showContentCommentList')->name('showContentCommentList');
        Route::post('deleteComment', 'Admin\ContentController@deleteComment')->name('deleteComment');
        Route::post('changeCommentStatus', 'Admin\ContentController@changeCommentStatus')->name('changeCommentStatus');
    });

    Route::prefix('movie/cast')->group(function () {
        Route::get('list/{flag?}/{id}', 'Admin\ContentController@viewAddMovieCast')->name('movie/cast/list');
        Route::post('addUpdateMovieCast', 'Admin\ContentController@addUpdateMovieCast')->name('addUpdateMovieCast');
        Route::post('deleteMovieCast', 'Admin\ContentController@deleteMovieCast')->name('deleteMovieCast');
        Route::post('showMovieCastList', 'Admin\ContentController@showMovieCastList')->name('showMovieCastList');
        Route::post('CheckExistMCastActor', 'Admin\ContentController@CheckExistMCastActor')->name('CheckExistMCastActor');
    });

    Route::prefix('movie/source')->group(function () {
        Route::get('list/{flag?}/{id}', 'Admin\ContentController@viewContentSource')->name('movie/source/list');
    });

    Route::prefix('series/source')->group(function () {
        Route::get('list/{flag?}/{id}', 'Admin\ContentController@viewContentSource')->name('series/source/list');
    });

    Route::prefix('movie/subtitle')->group(function () {
        Route::get('list/{flag?}/{id}', 'Admin\ContentController@viewAddContentSubtitles')->name('movie/subtitle/list');
    });
    Route::prefix('movie/comment')->group(function () {
        Route::get('list/{flag?}/{id}', 'Admin\ContentController@viewContentComment')->name('movie/comment/list');
    });
    Route::prefix('series/subtitle')->group(function () {
        Route::get('list/{flag?}/{id}', 'Admin\ContentController@viewAddContentSubtitles')->name('series/subtitle/list');
    });

    Route::prefix('series/season')->group(function () {
        Route::get('list/{flag?}/{id}', 'Admin\ContentController@viewAddSeriesSeason')->name('series/season/list');
        Route::get('edit/{flag?}/{id}', 'Admin\ContentController@viewUpdateContentSeriesSeason')->name('series/season/edit');
        Route::post('CheckExistSeason', 'Admin\ContentController@CheckExistSeason')->name('CheckExistSeason');
        Route::get('episode/add/{season_id}', 'Admin\ContentController@viewAddEpisode')->name('series/season/episode/add');
        Route::get('episode/edit/{season_id}/{id}', 'Admin\ContentController@viewUpdateEpisode')->name('series/season/episode/edit');
    });
    Route::prefix('series/season/episode/source')->group(function () {
        Route::get('list/{season_id}/{id}', 'Admin\ContentController@viewEpisodeSource')->name('series/season/episode/source/list');
    });
    Route::prefix('series/season/episode/subtitle')->group(function () {
        Route::get('list/{season_id}/{id}', 'Admin\ContentController@viewEpisodeSubtitles')->name('series/season/episode/subtitle/list');
    });
    Route::prefix('series/comment')->group(function () {
        Route::get('list/{flag?}/{id}', 'Admin\ContentController@viewContentComment')->name('series/comment/list');
    });
    Route::prefix('genre')->group(function () {
        Route::get('list', 'Admin\GenreController@viewListGenre')->name('genre/list');
        Route::post('showGenreList', 'Admin\GenreController@showGenreList')->name('showGenreList');
        Route::post('addUpdateGenre', 'Admin\GenreController@addUpdateGenre')->name('addUpdateGenre');
        Route::post('deleteGenre', 'Admin\GenreController@deleteGenre')->name('deleteGenre');
        Route::post('CheckExistGenre', 'Admin\GenreController@CheckExistGenre')->name('CheckExistGenre');
    });

    Route::prefix('actor')->group(function () {
        Route::get('list', 'Admin\ActorController@viewListActor')->name('actor/list');
        Route::post('showActorList', 'Admin\ActorController@showActorList')->name('showActorList');
        Route::post('addUpdateActor', 'Admin\ActorController@addUpdateActor')->name('addUpdateActor');
        Route::post('deleteActor', 'Admin\ActorController@deleteActor')->name('deleteActor');
        Route::post('CheckExistActor', 'Admin\ActorController@CheckExistActor')->name('CheckExistActor');
    });

    Route::prefix('language')->group(function () {
        Route::get('list', 'Admin\LanguageController@viewListLanguage')->name('language/list');
        Route::post('showLanguageList', 'Admin\LanguageController@showLanguageList')->name('showLanguageList');
        Route::post('addUpdateLanguage', 'Admin\LanguageController@addUpdateLanguage')->name('addUpdateLanguage');
        Route::post('deleteLanguage', 'Admin\LanguageController@deleteLanguage')->name('deleteLanguage');
        Route::post('CheckExistLanguage', 'Admin\LanguageController@CheckExistLanguage')->name('CheckExistLanguage');
    });

    Route::prefix('tv')->group(function () {
        Route::get('category/list', 'Admin\TVController@viewListTVCategory')->name('category/list');
        Route::post('showTVCategoryList', 'Admin\TVController@showTVCategoryList')->name('showTVCategoryList');
        Route::post('addUpdateTVCategory', 'Admin\TVController@addUpdateTVCategory')->name('addUpdateTVCategory');
        Route::post('deleteTVCategory', 'Admin\TVController@deleteTVCategory')->name('deleteTVCategory');
        Route::post('CheckExistTVCategory', 'Admin\TVController@CheckExistTVCategory')->name('CheckExistTVCategory');

        Route::get('channel/list', 'Admin\TVController@viewListTVChannel')->name('channel/list');
        Route::get('channel/add', 'Admin\TVController@viewAddTVChannel')->name('channel/add');
        Route::get('channel/edit/{id}', 'Admin\TVController@viewUpdateTVChannel')->name('channel/edit');
        Route::get('channel/view/{id}', 'Admin\TVController@viewTVChannel')->name('channel/view');
        Route::post('showTVChannelList', 'Admin\TVController@showTVChannelList')->name('showTVChannelList');
        Route::post('addUpdateTVChannel', 'Admin\TVController@addUpdateTVChannel')->name('addUpdateTVChannel');
        Route::post('deleteTVChannel', 'Admin\TVController@deleteTVChannel')->name('deleteTVChannel');
        Route::post('CheckExistTVChannel', 'Admin\TVController@CheckExistTVChannel')->name('CheckExistTVChannel');
        Route::post('deleteChannelSource', 'Admin\TVController@deleteChannelSource')->name('deleteChannelSource');
        Route::post('UploadSourceMedia', 'Admin\TVController@UploadSourceMedia')->name('UploadSourceMedia');

        Route::post('showTVChannelSourceList', 'Admin\TVController@showTVChannelSourceList')->name('showTVChannelSourceList');
        Route::post('addUpdateTVChannelSource', 'Admin\TVController@addUpdateTVChannelSource')->name('addUpdateTVChannelSource');
        Route::post('deleteTVChannelSource', 'Admin\TVController@deleteTVChannelSource')->name('deleteTVChannelSource');
    });

    Route::prefix('tv/channel/source')->group(function () {
        Route::get('list/{channel_id}', 'Admin\TVController@viewTvChannelSource')->name('tv/channel/source/list');
    });

    Route::prefix('settings')->group(function () {

        Route::get('/', 'Admin\SettingsController@viewSettings');
        Route::post('addUpdateSetting', 'Admin\SettingsController@addUpdateSetting')->name('addUpdateSetting');
    });

    Route::prefix('ads')->group(function () {

        Route::get('/', 'Admin\SettingsController@viewAds');
        Route::post('addUpdateAndriodAds', 'Admin\SettingsController@addUpdateAndriodAds')->name('addUpdateAndriodAds');
        Route::post('addUpdateIosAds', 'Admin\SettingsController@addUpdateIosAds')->name('addUpdateIosAds');

        Route::get('customAdsList', 'Admin\SettingsController@viewCustomAds');
        Route::post('showCustomAdsList', [CustomAdsController::class, 'showCustomAdsList'])->name('showCustomAdsList');
        Route::get('changeAdStatus/{id}/{status}', [CustomAdsController::class, 'changeAdStatus'])->name('changeAdStatus');
        Route::get('ads/edit/{id}', [CustomAdsController::class, 'editCustomAd'])->name('ads/edit/');
        Route::get('ads/view/{id}', [CustomAdsController::class, 'viewCustomAd'])->name('ads/view/');
        Route::get('createCustomAd', [CustomAdsController::class, 'createCustomAd']);
        Route::post('createNewAd', [CustomAdsController::class, 'createNewAd']);
        Route::post('editCustomAd', [CustomAdsController::class, 'editAd']);
        Route::post('deleteCustomAd', [CustomAdsController::class, 'deleteCustomAd'])->name('deleteCustomAd');
        
        Route::post('showAdImagesList', [CustomAdsController::class, 'showAdImagesList'])->name('showAdImagesList');
        Route::post('addImageToAd', [CustomAdsController::class, 'addImageToAd'])->name('addImageToAd');
        Route::post('deleteAdImage', [CustomAdsController::class, 'deleteAdImage'])->name('deleteAdImage');
        Route::post('editAdImage', [CustomAdsController::class, 'editAdImage'])->name('editAdImage');
        
        Route::post('showAdVideoList', [CustomAdsController::class, 'showAdVideoList'])->name('showAdVideoList');
        Route::post('addVideoToAd', [CustomAdsController::class, 'addVideoToAd'])->name('addVideoToAd');
        Route::post('deleteAdVideo', [CustomAdsController::class, 'deleteAdVideo'])->name('deleteAdVideo');
        Route::post('editAdVideo', [CustomAdsController::class, 'editAdVideo'])->name('editAdVideo');
    });

    Route::prefix('notification')->group(function () {

        Route::post('sendNotification', 'Admin\SettingsController@sendNotification')->name('sendNotification');
        Route::get('list', 'Admin\SettingsController@viewListNotification')->name('notification/list');
        Route::post('showNotificationList', 'Admin\SettingsController@showNotificationList')->name('showNotificationList');
        Route::post('UpdateNotification', 'Admin\SettingsController@UpdateNotification')->name('UpdateNotification');
        Route::post('deleteNotification', 'Admin\SettingsController@deleteNotification')->name('deleteNotification');
    });
    Route::prefix('subscription')->group(function () {
        Route::get('list', 'Admin\SubscriptionController@viewListSubscription')->name('subscription/list');
        Route::post('showSubscriptionList', 'Admin\SubscriptionController@showSubscriptionList')->name('showSubscriptionList');
        Route::get('view/{id}', 'Admin\SubscriptionController@viewSubscription')->name('viewSubscription');
        Route::post('deleteSubscription', 'Admin\SubscriptionController@deleteSubscription')->name('deleteSubscription');
    });

    Route::prefix('subscription/package')->group(function () {
        Route::get('/', 'Admin\SettingsController@viewSubscriptionPackage');
        Route::post('addUpdateSubscriptionPackage', 'Admin\SettingsController@addUpdateSubscriptionPackage')->name('addUpdateSubscriptionPackage');
    });


    Route::get('privacypolicy', 'Admin\PrivacyPolicyController@index')->name('privacypolicy.index');
    Route::post('UpdatePrivacypolicy', 'Admin\PrivacyPolicyController@UpdatePrivacypolicy')->name('UpdatePrivacypolicy');

    Route::get('termscondition', 'Admin\TermsConditionController@index')->name('termscondition.index');
    Route::post('UpdateTermscondition', 'Admin\TermsConditionController@UpdateTermscondition')->name('UpdateTermscondition');
});
