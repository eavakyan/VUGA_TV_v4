package com.vugaenterprises.androidtv.data.api

import com.vugaenterprises.androidtv.data.model.ActorDataResponse
import com.vugaenterprises.androidtv.data.model.AllContentResponse
import com.vugaenterprises.androidtv.data.model.AppSettingResponse
import com.vugaenterprises.androidtv.data.model.ContentByGenreResponse
import com.vugaenterprises.androidtv.data.model.ContentDetailResponse
import com.vugaenterprises.androidtv.data.model.CustomAdsResponse
import com.vugaenterprises.androidtv.data.model.HomePageResponse
import com.vugaenterprises.androidtv.data.model.RestResponse
import com.vugaenterprises.androidtv.data.model.UserRegistrationResponse
import retrofit2.http.*

interface ApiService {
    
    // Simple test endpoint
    @GET(".")
    suspend fun testConnection(): String
    
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
        @Field("user_id") userId: Int
    ): HomePageResponse
    
    // Content endpoints
    @FormUrlEncoded
    @POST("fetchContentDetails")
    suspend fun getContentDetail(
        @Field("user_id") userId: Int,
        @Field("content_id") contentId: Int
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
        @Field("limit") limit: Int
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
} 