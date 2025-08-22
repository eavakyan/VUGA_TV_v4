package com.vugaenterprises.androidtv.data.api

import com.vugaenterprises.androidtv.data.model.ActorDataResponse
import com.vugaenterprises.androidtv.data.model.AgeRating
import com.vugaenterprises.androidtv.data.model.AgeRatingResponse
import com.vugaenterprises.androidtv.data.model.AllContentResponse
import com.vugaenterprises.androidtv.data.model.AppSettingResponse
import com.vugaenterprises.androidtv.data.model.ContentByGenreResponse
import com.vugaenterprises.androidtv.data.model.ContentDetailResponse
import com.vugaenterprises.androidtv.data.model.CustomAdsResponse
import com.vugaenterprises.androidtv.data.model.HomePageResponse
import com.vugaenterprises.androidtv.data.model.RestResponse
import com.vugaenterprises.androidtv.data.model.UserRegistrationResponse
import com.vugaenterprises.androidtv.data.model.TVAuthSessionRequest
import com.vugaenterprises.androidtv.data.model.TVAuthSessionResponse
import com.vugaenterprises.androidtv.data.model.TVAuthStatusRequest
import com.vugaenterprises.androidtv.data.model.TVAuthStatusResponse
import com.vugaenterprises.androidtv.data.model.TVAuthCompleteRequest
import com.vugaenterprises.androidtv.data.model.ProfilesResponse
import com.vugaenterprises.androidtv.data.model.CreateProfileRequest
import com.vugaenterprises.androidtv.data.model.SelectProfileRequest
import com.vugaenterprises.androidtv.data.model.LiveChannelsResponse
import com.vugaenterprises.androidtv.data.model.LiveCategoriesResponse
import retrofit2.http.*

interface ApiService {
    
    // Simple test endpoint
    @GET("test")
    suspend fun testConnection(): RestResponse
    
    // App settings
    @POST("fetchSettings")
    suspend fun getAppSettings(): AppSettingResponse
    
    // User registration and authentication
    @FormUrlEncoded
    @POST("userRegistration")
    suspend fun registerUser(
        @Field("fullname") fullname: String,
        @Field("email") email: String,
        @Field("identity") identity: String,
        @Field("login_type") loginType: String,
        @Field("device_token") deviceToken: String = ""
    ): UserRegistrationResponse
    
    @FormUrlEncoded
    @POST("updateProfile")
    suspend fun updateProfile(
        @Field("user_id") userId: Int,
        @Field("fullname") fullname: String,
        @Field("email") email: String
    ): UserRegistrationResponse
    
    @FormUrlEncoded
    @POST("logOut")
    suspend fun logOutUser(@Field("user_id") userId: Int): UserRegistrationResponse
    
    @FormUrlEncoded
    @POST("deleteMyAccount")
    suspend fun deleteAccount(@Field("user_id") userId: Int): RestResponse
    
    // Home page data
    @FormUrlEncoded
    @POST("fetchHomePageData")
    suspend fun getHomeData(
        @Field("user_id") userId: Int,
        @Field("profile_id") profileId: Int? = null
    ): HomePageResponse
    
    // Content endpoints
    @FormUrlEncoded
    @POST("fetchContentDetails")
    suspend fun getContentDetail(
        @Field("user_id") userId: Int,
        @Field("content_id") contentId: Int,
        @Field("profile_id") profileId: Int? = null
    ): ContentDetailResponse
    
    @FormUrlEncoded
    @POST("fetchContentsByGenre")
    suspend fun getContentByGenre(
        @Field("start") start: Int,
        @Field("limit") limit: Int,
        @Field("genre_id") genreId: Int
    ): ContentByGenreResponse
    
    @FormUrlEncoded
    @POST("fetchWatchList")
    suspend fun getWatchList(
        @Field("type") type: Int,
        @Field("user_id") userId: Int,
        @Field("start") start: Int,
        @Field("limit") limit: Int,
        @Field("profile_id") profileId: Int? = null
    ): AllContentResponse
    
    // Search endpoints
    @FormUrlEncoded
    @POST("searchContent")
    suspend fun searchContent(
        @Field("keyword") keyword: String,
        @Field("start") start: Int = 0,
        @Field("limit") limit: Int = 20
    ): AllContentResponse
    
    // Analytics and metrics
    @FormUrlEncoded
    @POST("increaseContentView")
    suspend fun increaseContentView(@Field("content_id") contentId: Int): RestResponse
    
    @FormUrlEncoded
    @POST("increaseContentShare")
    suspend fun increaseContentShare(@Field("content_id") contentId: Int): RestResponse
    
    @FormUrlEncoded
    @POST("increaseContentDownload")
    suspend fun increaseContentDownload(@Field("content_id") contentId: Int): RestResponse
    
    @FormUrlEncoded
    @POST("increaseEpisodeView")
    suspend fun increaseEpisodeView(@Field("episode_id") episodeId: Int): RestResponse
    
    @FormUrlEncoded
    @POST("increaseEpisodeDownload")
    suspend fun increaseEpisodeDownload(@Field("episode_id") episodeId: Int): RestResponse
    
    // Actor details
    @FormUrlEncoded
    @POST("fetchActorDetails")
    suspend fun fetchActorDetails(@Field("actor_id") actorId: Int): ActorDataResponse
    
    // Ads
    @FormUrlEncoded
    @POST("fetchCustomAds")
    suspend fun fetchCustomAds(@Field("is_android") deviceType: Int): CustomAdsResponse
    
    @FormUrlEncoded
    @POST("increaseAdMetric")
    suspend fun increaseAdMetric(
        @Field("custom_ad_id") customAdId: Long,
        @Field("metric") metric: String
    ): RestResponse
    
    // TV Authentication - V2 endpoints
    @POST("tv-auth/generate-session")
    suspend fun generateAuthSession(@Body request: TVAuthSessionRequest): TVAuthSessionResponse
    
    @POST("tv-auth/check-status")
    suspend fun checkAuthStatus(@Body request: TVAuthStatusRequest): TVAuthStatusResponse
    
    // Note: tv-auth/authenticate is called by mobile app, not TV app
    
    // Profile Management
    @FormUrlEncoded
    @POST("getUserProfiles")
    suspend fun getUserProfiles(
        @Field("user_id") userId: Int
    ): ProfilesResponse
    
    @POST("createProfile")
    suspend fun createProfile(@Body request: CreateProfileRequest): ProfilesResponse
    
    @FormUrlEncoded
    @POST("deleteProfile")
    suspend fun deleteProfile(
        @Field("user_id") userId: Int,
        @Field("profile_id") profileId: Int
    ): ProfilesResponse
    
    @POST("selectProfile")
    suspend fun selectProfile(@Body request: SelectProfileRequest): ProfilesResponse
    
    // Profile-based features
    @FormUrlEncoded
    @POST("user/toggle-watchlist")
    suspend fun toggleWatchlist(
        @Field("app_user_id") userId: Int,
        @Field("content_id") contentId: Int,
        @Field("profile_id") profileId: Int? = null
    ): UserRegistrationResponse
    
    @FormUrlEncoded
    @POST("user/toggle-favorite")
    suspend fun toggleFavorite(
        @Field("app_user_id") userId: Int,
        @Field("content_id") contentId: Int,
        @Field("profile_id") profileId: Int? = null
    ): UserRegistrationResponse
    
    @FormUrlEncoded
    @POST("user/rate-content")
    suspend fun rateContent(
        @Field("app_user_id") userId: Int,
        @Field("content_id") contentId: Int,
        @Field("rating") rating: Float,
        @Field("profile_id") profileId: Int? = null
    ): RestResponse
    
    @FormUrlEncoded
    @POST("watch/update-progress")
    suspend fun updateWatchProgress(
        @Field("app_user_id") userId: Int,
        @Field("content_id") contentId: Int? = null,
        @Field("episode_id") episodeId: Int? = null,
        @Field("last_watched_position") position: Int,
        @Field("total_duration") duration: Int,
        @Field("device_type") deviceType: Int = 2, // Android TV
        @Field("profile_id") profileId: Int? = null
    ): RestResponse
    
    @FormUrlEncoded
    @POST("watch/continue-watching")
    suspend fun getContinueWatching(
        @Field("app_user_id") userId: Int,
        @Field("limit") limit: Int = 20,
        @Field("profile_id") profileId: Int? = null
    ): RestResponse
    
    @FormUrlEncoded
    @POST("watch/mark-completed")
    suspend fun markAsCompleted(
        @Field("app_user_id") userId: Int,
        @Field("content_id") contentId: Int? = null,
        @Field("episode_id") episodeId: Int? = null,
        @Field("profile_id") profileId: Int? = null
    ): RestResponse
    
    @FormUrlEncoded
    @POST("user/watch-history")
    suspend fun getWatchHistory(
        @Field("app_user_id") userId: Int,
        @Field("profile_id") profileId: Int? = null
    ): RestResponse
    
    // Age Settings
    @POST("profile/age-ratings")
    suspend fun getAgeRatings(): AgeRatingResponse
    
    @FormUrlEncoded
    @POST("profile/update-age-settings")
    suspend fun updateAgeSettings(
        @Field("profile_id") profileId: Int,
        @Field("user_id") userId: Int,
        @Field("age") age: Int? = null,
        @Field("is_kids_profile") isKidsProfile: Boolean
    ): RestResponse
    
    // Live TV Endpoints
    @FormUrlEncoded
    @POST("live-tv/channels")
    suspend fun getLiveChannels(
        @Field("user_id") userId: Int,
        @Field("profile_id") profileId: Int? = null,
        @Field("category") category: String? = null,
        @Field("limit") limit: Int = 50,
        @Field("offset") offset: Int = 0
    ): LiveChannelsResponse
    
    @FormUrlEncoded
    @POST("live-tv/channel-details")
    suspend fun getLiveChannelDetails(
        @Field("user_id") userId: Int,
        @Field("channel_id") channelId: Int,
        @Field("profile_id") profileId: Int? = null
    ): LiveChannelsResponse
    
    @FormUrlEncoded
    @POST("live-tv/categories")
    suspend fun getLiveChannelCategories(
        @Field("user_id") userId: Int,
        @Field("profile_id") profileId: Int? = null
    ): LiveCategoriesResponse
    
    @FormUrlEncoded
    @POST("live-tv/track-view")
    suspend fun trackLiveChannelView(
        @Field("user_id") userId: Int,
        @Field("channel_id") channelId: Int,
        @Field("watch_duration") watchDuration: Int,
        @Field("profile_id") profileId: Int? = null,
        @Field("device_type") deviceType: Int = 2 // Android TV
    ): RestResponse
}

// Constants for API field names
object ApiFields {
    const val USER_ID = "user_id"
    const val CONTENT_ID = "content_id"
    const val EPISODE_ID = "episode_id"
    const val START = "start"
    const val LIMIT = "limit"
    const val GENRE_ID = "genre_id"
    const val TYPE = "type"
    const val KEYWORD = "keyword"
    const val FULLNAME = "fullname"
    const val EMAIL = "email"
    const val IDENTITY = "identity"
    const val LOGIN_TYPE = "login_type"
    const val DEVICE_TOKEN = "device_token"
    const val ACTOR_ID = "actor_id"
    const val CUSTOM_AD_ID = "custom_ad_id"
    const val METRIC = "metric"
    const val IS_ANDROID = "is_android"
    const val PROFILE_ID = "profile_id"
    const val APP_USER_ID = "app_user_id"
    const val RATING = "rating"
    const val LAST_WATCHED_POSITION = "last_watched_position"
    const val TOTAL_DURATION = "total_duration"
    const val DEVICE_TYPE = "device_type"
} 