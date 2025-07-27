<?php

use App\Http\Controllers\ActorController;
use App\Http\Controllers\AdmobController;
use App\Http\Controllers\ContentController;
use App\Http\Controllers\CustomAdsController;
use App\Http\Controllers\GenreController;
use App\Http\Controllers\LanguageController;
use App\Http\Controllers\SettingController;
use App\Http\Controllers\LoginController;
use App\Http\Controllers\MediaGalleryController;
use App\Http\Controllers\NotificationController;
use App\Http\Controllers\TVController;  
use App\Http\Controllers\UserController;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Route;

Route::get('/linkstorage', function () {
    Artisan::call('storage:link');
});

Route::get('/', [LoginController::class, 'login'])->name('/');
Route::post('login', [LoginController::class, 'checklogin'])->middleware(['checkLogin'])->name('login');
Route::get('index', [SettingController::class, 'index'])->middleware(['checkLogin'])->name('index');
Route::get('logout', [LoginController::class, 'logout'])->middleware(['checkLogin'])->name('logout');


Route::get('users', [UserController::class, 'viewUserList'])->middleware(['checkLogin'])->name('users');
Route::post('usersList', [UserController::class, 'usersList'])->middleware(['checkLogin'])->name('usersList');

Route::get('contentList', [ContentController::class, 'contentList'])->middleware(['checkLogin'])->name('contentList');
Route::post('fetchMoviesList', [ContentController::class, 'fetchMoviesList'])->middleware(['checkLogin'])->name('fetchMoviesList');
Route::post('unfeatured', [ContentController::class, 'unfeatured'])->middleware(['checkLogin'])->name('unfeatured');
Route::post('featured', [ContentController::class, 'featured'])->middleware(['checkLogin'])->name('featured');
Route::post('hideContent', [ContentController::class, 'hideContent'])->middleware(['checkLogin'])->name('hideContent');
Route::post('showContent', [ContentController::class, 'showContent'])->middleware(['checkLogin'])->name('showContent');
Route::post('addNewContent', [ContentController::class, 'addNewContent'])->middleware(['checkLogin'])->name('addNewContent');
Route::post('updateContent', [ContentController::class, 'updateContent'])->middleware(['checkLogin'])->name('updateContent');
Route::post('deleteContent', [ContentController::class, 'deleteContent'])->middleware(['checkLogin'])->name('deleteContent');
Route::post('notifyContent', [ContentController::class, 'notifyContent'])->middleware(['checkLogin'])->name('notifyContent');

// Series
Route::post('fetchSeriesList', [ContentController::class, 'fetchSeriesList'])->middleware(['checkLogin'])->name('fetchSeriesList');
Route::get('series/{id}', [ContentController::class, 'seriesDetailView'])->middleware(['checkLogin'])->name('seriesDetailView');
Route::post('addSeason', [ContentController::class, 'addSeason'])->middleware(['checkLogin'])->name('addSeason');
Route::post('updateSeason', [ContentController::class, 'updateSeason'])->middleware(['checkLogin'])->name('updateSeason');
Route::post('deleteSeason', [ContentController::class, 'deleteSeason'])->middleware(['checkLogin'])->name('deleteSeason');
Route::post('fetchEpisodeList', [ContentController::class, 'fetchEpisodeList'])->middleware(['checkLogin'])->name('fetchEpisodeList');

// Episode
Route::get('series/episodeDetail/{id}', [ContentController::class, 'episodeDetailView'])->middleware(['checkLogin'])->name('episodeDetailView');

Route::post('addEpisode', [ContentController::class, 'addEpisode'])->middleware(['checkLogin'])->name('addEpisode');
Route::post('updateEpisode', [ContentController::class, 'updateEpisode'])->middleware(['checkLogin'])->name('updateEpisode');
Route::post('deleteEpisode', [ContentController::class, 'deleteEpisode'])->middleware(['checkLogin'])->name('deleteEpisode');

Route::post('fetchEpisodeSourceList', [ContentController::class, 'fetchEpisodeSourceList'])->middleware(['checkLogin'])->name('fetchEpisodeSourceList');
Route::post('addEpisodeSource', [ContentController::class, 'addEpisodeSource'])->middleware(['checkLogin'])->name('addEpisodeSource');
Route::post('updateEpisodeSource', [ContentController::class, 'updateEpisodeSource'])->middleware(['checkLogin'])->name('updateEpisodeSource');
Route::post('deleteEpisodeSource', [ContentController::class, 'deleteEpisodeSource'])->middleware(['checkLogin'])->name('deleteEpisodeSource');

Route::post('fetchEpisodeSubtitleList', [ContentController::class, 'fetchEpisodeSubtitleList'])->middleware(['checkLogin'])->name('fetchEpisodeSubtitleList');
Route::post('addEpisodeSubtitle', [ContentController::class, 'addEpisodeSubtitle'])->middleware(['checkLogin'])->name('addEpisodeSubtitle');
Route::post('deleteEpisodeSubtitle', [ContentController::class, 'deleteEpisodeSubtitle'])->middleware(['checkLogin'])->name('deleteEpisodeSubtitle');

Route::get('contentList/{id}', [ContentController::class, 'contentDetailView'])->middleware(['checkLogin'])->name('contentDetailView');
Route::post('fetchSourceList', [ContentController::class, 'fetchSourceList'])->middleware(['checkLogin'])->name('fetchSourceList');
Route::post('addSource', [ContentController::class, 'addSource'])->middleware(['checkLogin'])->name('addSource');
Route::post('updateContentSource', [ContentController::class, 'updateContentSource'])->middleware(['checkLogin'])->name('updateContentSource');
Route::post('deleteSource', [ContentController::class, 'deleteSource'])->middleware(['checkLogin'])->name('deleteSource');

Route::post('fetchCastList', [ContentController::class, 'fetchCastList'])->middleware(['checkLogin'])->name('fetchCastList');
Route::post('addCast', [ContentController::class, 'addCast'])->middleware(['checkLogin'])->name('addCast');
Route::post('updateCast', [ContentController::class, 'updateCast'])->middleware(['checkLogin'])->name('updateCast');
Route::post('deleteCast', [ContentController::class, 'deleteCast'])->middleware(['checkLogin'])->name('deleteCast');

Route::get('actors', [ActorController::class, 'actors'])->middleware(['checkLogin'])->name('actors');
Route::post('actorsList', [ActorController::class, 'actorsList'])->middleware(['checkLogin'])->name('actorsList');
Route::post('addNewActor', [ActorController::class, 'addNewActor'])->middleware(['checkLogin'])->name('addNewActor');
Route::post('updateActor', [ActorController::class, 'updateActor'])->middleware(['checkLogin'])->name('updateActor');
Route::post('deleteActor', [ActorController::class, 'deleteActor'])->middleware(['checkLogin'])->name('deleteActor');

Route::get('fetchActorFromTMDB', [ActorController::class, 'fetchActorFromTMDB'])->middleware(['checkLogin'])->name('fetchActorFromTMDB');

Route::post('fetchSubtitleList', [ContentController::class, 'fetchSubtitleList'])->middleware(['checkLogin'])->name('fetchSubtitleList');
Route::post('addSubtitle', [ContentController::class, 'addSubtitle'])->middleware(['checkLogin'])->name('addSubtitle');
Route::post('deleteSubtitle', [ContentController::class, 'deleteSubtitle'])->middleware(['checkLogin'])->name('deleteSubtitle');

Route::get('genres', [GenreController::class, 'genres'])->middleware(['checkLogin'])->name('genres');
Route::post('genresList', [GenreController::class, 'genresList'])->middleware(['checkLogin'])->name('genresList');
Route::post('addGenre', [GenreController::class, 'addGenre'])->middleware(['checkLogin'])->name('addGenre');
Route::post('updateGenre', [GenreController::class, 'updateGenre'])->middleware(['checkLogin'])->name('updateGenre');
Route::post('deleteGenre', [GenreController::class, 'deleteGenre'])->middleware(['checkLogin'])->name('deleteGenre');

Route::get('languages', [LanguageController::class, 'languages'])->middleware(['checkLogin'])->name('languages');
Route::post('languagesList', [LanguageController::class, 'languagesList'])->middleware(['checkLogin'])->name('languagesList');
Route::post('addLanguage', [LanguageController::class, 'addLanguage'])->middleware(['checkLogin'])->name('addLanguage');
Route::post('updateLanguage', [LanguageController::class, 'updateLanguage'])->middleware(['checkLogin'])->name('updateLanguage');
Route::post('deleteLanguage', [LanguageController::class, 'deleteLanguage'])->middleware(['checkLogin'])->name('deleteLanguage');
// For Select Languages In Fetch FromTMDB
Route::get('getAllLanguages', [LanguageController::class, 'getAllLanguages'])->name('getAllLanguages');

// Live Tv categories
Route::get('liveTvCategories', [TVController::class, 'liveTvCategories'])->middleware(['checkLogin'])->name('liveTvCategories');
Route::post('fetchTvCategoryList', [TVController::class, 'fetchTvCategoryList'])->middleware(['checkLogin'])->name('fetchTvCategoryList');
Route::post('addTvCategory', [TVController::class, 'addTvCategory'])->middleware(['checkLogin'])->name('addTvCategory');
Route::post('updateTvCategory', [TVController::class, 'updateTvCategory'])->middleware(['checkLogin'])->name('updateTvCategory');
Route::post('deleteTvCategory', [TVController::class, 'deleteTvCategory'])->middleware(['checkLogin'])->name('deleteTvCategory');

// Live Tv Channel
Route::get('liveTvChannels', [TVController::class, 'liveTvChannels'])->middleware(['checkLogin'])->name('liveTvChannels');
Route::post('fetchTvChannelList', [TVController::class, 'fetchTvChannelList'])->middleware(['checkLogin'])->name('fetchTvChannelList');
Route::post('addTvChannel', [TVController::class, 'addTvChannel'])->middleware(['checkLogin'])->name('addTvChannel');
Route::post('updateTvChannel', [TVController::class, 'updateTvChannel'])->middleware(['checkLogin'])->name('updateTvChannel');
Route::post('deleteTvChannel', [TVController::class, 'deleteTvChannel'])->middleware(['checkLogin'])->name('deleteTvChannel');

// Notification
Route::get('notification', [NotificationController::class, 'notification'])->middleware(['checkLogin'])->name('notification');
Route::post('notificationList', [NotificationController::class, 'notificationList'])->middleware(['checkLogin'])->name('notificationList');
Route::post('addNotification', [NotificationController::class, 'addNotification'])->middleware(['checkLogin'])->name('addNotification');
Route::post('updateNotification', [NotificationController::class, 'updateNotification'])->middleware(['checkLogin'])->name('updateNotification');
Route::post('repeatNotification', [NotificationController::class, 'repeatNotification'])->middleware(['checkLogin'])->name('repeatNotification');
Route::post('deleteNotification', [NotificationController::class, 'deleteNotification'])->middleware(['checkLogin'])->name('deleteNotification');

// Admob
Route::get('admob', [AdmobController::class, 'admob'])->middleware(['checkLogin'])->name('admob');
Route::post('admobAndroid', [AdmobController::class, 'admobAndroid'])->middleware(['checkLogin'])->name('admobAndroid');
Route::post('admobiOS', [AdmobController::class, 'admobiOS'])->middleware(['checkLogin'])->name('admobiOS');

// Custom Ads
Route::get('customAds', [CustomAdsController::class, 'customAds'])->middleware(['checkLogin'])->name('customAds');
Route::post('fetchCustomAdList', [CustomAdsController::class, 'fetchCustomAdList'])->middleware(['checkLogin'])->name('fetchCustomAdList');
Route::post('customAdOn', [CustomAdsController::class, 'customAdOn'])->middleware(['checkLogin'])->name('customAdOn');
Route::post('customAdOff', [CustomAdsController::class, 'customAdOff'])->middleware(['checkLogin'])->name('customAdOff');
Route::post('addCustomAd', [CustomAdsController::class, 'addCustomAd'])->middleware(['checkLogin'])->name('addCustomAd');
Route::post('updateCustomAd', [CustomAdsController::class, 'updateCustomAd'])->middleware(['checkLogin'])->name('updateCustomAd');
Route::post('deleteCustomAd', [CustomAdsController::class, 'deleteCustomAd'])->middleware(['checkLogin'])->name('deleteCustomAd');

Route::get('customAdDetailView/{id}', [CustomAdsController::class, 'customAdDetailView'])->middleware(['checkLogin'])->name('customAdDetailView');
Route::post('fetchCustomAdImageSourceList', [CustomAdsController::class, 'fetchCustomAdImageSourceList'])->middleware(['checkLogin'])->name('fetchCustomAdImageSourceList');
Route::post('addCustomAdSourceImage', [CustomAdsController::class, 'addCustomAdSourceImage'])->middleware(['checkLogin'])->name('addCustomAdSourceImage');
Route::post('updateCustomAdSource', [CustomAdsController::class, 'updateCustomAdSource'])->middleware(['checkLogin'])->name('updateCustomAdSource');
Route::post('deleteCustomAdSource', [CustomAdsController::class, 'deleteCustomAdSource'])->middleware(['checkLogin'])->name('deleteCustomAdSource');

Route::post('fetchCustomAdVideoSourceList', [CustomAdsController::class, 'fetchCustomAdVideoSourceList'])->middleware(['checkLogin'])->name('fetchCustomAdVideoSourceList');
Route::post('addCustomAdSourceVideo', [CustomAdsController::class, 'addCustomAdSourceVideo'])->middleware(['checkLogin'])->name('addCustomAdSourceVideo');


// Setting
Route::get('setting', [SettingController::class, 'setting'])->middleware(['checkLogin'])->name('setting');
Route::post('saveSettings', [SettingController::class, 'saveSettings'])->middleware(['checkLogin'])->name('saveSettings');
Route::post('changePassword', [SettingController::class, 'changePassword'])->middleware(['checkLogin'])->name('changePassword');

Route::get('viewPrivacy', [SettingController::class, 'viewPrivacy'])->middleware(['checkLogin'])->name('viewPrivacy');
Route::post('updatePrivacy', [SettingController::class, 'updatePrivacy'])->middleware(['checkLogin'])->name('updatePrivacy');
Route::post('addContentForm', [SettingController::class, 'addContentForm'])->middleware(['checkLogin'])->name('addContentForm');
Route::post('addTermsForm', [SettingController::class, 'addTermsForm'])->middleware(['checkLogin'])->name('addTermsForm');
Route::post('updateTerms', [SettingController::class, 'updateTerms'])->middleware(['checkLogin'])->name('updateTerms');
Route::get('viewTerms', [SettingController::class, 'viewTerms'])->middleware(['checkLogin'])->name('viewTerms');
Route::get('privacyPolicy', [SettingController::class, 'privacyPolicy'])->name('privacyPolicy');
Route::get('termsOfUse', [SettingController::class, 'termsOfUse'])->name('termsOfUse');

Route::get('fetchContentFromTMDB', [SettingController::class, 'fetchContentFromTMDB'])->middleware(['checkLogin'])->name('fetchContentFromTMDB');
Route::post('searchQueryTMDB', [SettingController::class, 'searchQueryTMDB'])->middleware(['checkLogin'])->name('searchQueryTMDB');

Route::get('topContents', [ContentController::class, 'topContents'])->middleware(['checkLogin'])->name('topContents');
Route::post('topContentsList', [ContentController::class, 'topContentsList'])->middleware(['checkLogin'])->name('topContentsList');
Route::post('saveOrder', [ContentController::class, 'saveOrder'])->middleware(['checkLogin'])->name('saveOrder');
Route::post('removeFromTopContent', [ContentController::class, 'removeFromTopContent'])->middleware(['checkLogin'])->name('removeFromTopContent');
Route::post('addToTopContent', [ContentController::class, 'addToTopContent'])->middleware(['checkLogin'])->name('addToTopContent');

Route::get('mediaGallery', [MediaGalleryController::class, 'mediaGallery'])->middleware(['checkLogin'])->name('mediaGallery');
Route::post('mediaGalleryList', [MediaGalleryController::class, 'mediaGalleryList'])->middleware(['checkLogin'])->name('mediaGalleryList');
Route::post('addMedia', [MediaGalleryController::class, 'addMedia'])->middleware(['checkLogin'])->name('addMedia');
Route::post('updateMedia', [MediaGalleryController::class, 'updateMedia'])->middleware(['checkLogin'])->name('updateMedia');
Route::post('deleteMedia', [MediaGalleryController::class, 'deleteMedia'])->middleware(['checkLogin'])->name('deleteMedia');