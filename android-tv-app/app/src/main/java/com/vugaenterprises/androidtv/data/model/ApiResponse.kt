package com.vugaenterprises.androidtv.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("status")
    val status: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("data")
    val data: T? = null
)

data class PaginatedResponse<T>(
    @SerializedName("content")
    val content: List<T>,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalItems")
    val totalItems: Int,
    @SerializedName("hasNext")
    val hasNext: Boolean,
    @SerializedName("hasPrevious")
    val hasPrevious: Boolean
)

data class SearchResponse(
    @SerializedName("content")
    val content: List<Content>,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalItems")
    val totalItems: Int,
    @SerializedName("hasNext")
    val hasNext: Boolean,
    @SerializedName("hasPrevious")
    val hasPrevious: Boolean
)

data class StreamResponse(
    @SerializedName("streamUrl")
    val streamUrl: String,
    @SerializedName("quality")
    val quality: String,
    @SerializedName("duration")
    val duration: Long
)

data class AuthResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: UserProfile
)

data class UserProfile(
    @SerializedName("_id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("preferences")
    val preferences: UserPreferences? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class UserPreferences(
    @SerializedName("language")
    val language: String = "en",
    @SerializedName("subtitles")
    val subtitles: Boolean = false,
    @SerializedName("autoplay")
    val autoplay: Boolean = true,
    @SerializedName("quality")
    val quality: String = "1080p"
)



data class GenreCount(
    @SerializedName("genre")
    val genre: String,
    @SerializedName("count")
    val count: Int
)

data class FeaturedContentResponse(
    @SerializedName("content")
    val content: List<Content>
)

data class RecommendationsResponse(
    @SerializedName("recommendations")
    val recommendations: List<Content>
)

data class ContinueWatchingResponse(
    @SerializedName("continueWatching")
    val continueWatching: List<WatchHistory>
)

data class FavoritesResponse(
    @SerializedName("favorites")
    val favorites: List<Content>
)

data class WatchHistoryResponse(
    @SerializedName("history")
    val history: List<WatchHistory>
)

// Response models matching mobile apps structure
data class HomePageResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("featured")
    val featured: List<Content> = emptyList(),

    @SerializedName("watchlist")
    val watchlist: List<Content> = emptyList(),

    @SerializedName("topContents")
    val topContents: List<TopContentItem> = emptyList(),

    @SerializedName("genreContents")
    val genreContents: List<GenreContents> = emptyList()
)

data class TopContentItem(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("content_index")
    val contentIndex: Int = 0,
    
    @SerializedName("content_id")
    val contentId: Int = 0,
    
    @SerializedName("content")
    val content: Content = Content()
)

data class GenreContents(
    @SerializedName("title")
    val title: String = "",
    
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("contents")
    val contents: List<Content> = emptyList()
)

data class ContentDetailResponse(
    @SerializedName("status")
    val status: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("data")
    val data: Content = Content()
)

data class AllContentResponse(
    @SerializedName("status")
    val status: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("data")
    val data: List<Content> = emptyList()
)

data class ContentByGenreResponse(
    @SerializedName("status")
    val status: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("data")
    val data: List<Content> = emptyList()
)

data class UserRegistrationResponse(
    @SerializedName("status")
    val status: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("data")
    val data: UserData = UserData()
)

data class UserData(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("fullname")
    val fullname: String = "",
    
    @SerializedName("email")
    val email: String = "",
    
    @SerializedName("profile_image")
    val profileImage: String = "",
    
    @SerializedName("identity")
    val identity: String = "",
    
    @SerializedName("login_type")
    val loginType: String = ""
)

data class AppSettingResponse(
    @SerializedName("status")
    val status: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("data")
    val data: AppSetting = AppSetting()
)

data class AppSetting(
    @SerializedName("app_name")
    val appName: String = "",
    
    @SerializedName("app_version")
    val appVersion: String = "",
    
    @SerializedName("app_logo")
    val appLogo: String = "",
    
    @SerializedName("app_banner")
    val appBanner: String = "",
    
    @SerializedName("app_description")
    val appDescription: String = "",
    
    @SerializedName("app_website")
    val appWebsite: String = "",
    
    @SerializedName("app_email")
    val appEmail: String = "",
    
    @SerializedName("app_phone")
    val appPhone: String = "",
    
    @SerializedName("app_address")
    val appAddress: String = "",
    
    @SerializedName("app_privacy_policy")
    val appPrivacyPolicy: String = "",
    
    @SerializedName("app_terms_of_service")
    val appTermsOfService: String = "",
    
    @SerializedName("app_about")
    val appAbout: String = ""
)

data class RestResponse(
    @SerializedName("status")
    val status: Boolean = false,
    
    @SerializedName("message")
    val message: String = ""
)

data class ActorDataResponse(
    @SerializedName("status")
    val status: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("data")
    val data: ActorData = ActorData()
)

data class ActorData(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("fullname")
    val fullname: String = "",
    
    @SerializedName("profile_image")
    val profileImage: String = "",
    
    @SerializedName("biography")
    val biography: String = "",
    
    @SerializedName("birth_date")
    val birthDate: String = "",
    
    @SerializedName("birth_place")
    val birthPlace: String = "",
    
    @SerializedName("height")
    val height: String = "",
    
    @SerializedName("nationality")
    val nationality: String = "",
    
    @SerializedName("contents")
    val contents: List<Content> = emptyList()
)

data class CustomAdsResponse(
    @SerializedName("status")
    val status: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("data")
    val data: CustomAds = CustomAds()
)

data class CustomAds(
    @SerializedName("id")
    val id: Long = 0,
    
    @SerializedName("title")
    val title: String = "",
    
    @SerializedName("description")
    val description: String = "",
    
    @SerializedName("image")
    val image: String = "",
    
    @SerializedName("url")
    val url: String = "",
    
    @SerializedName("type")
    val type: String = "",
    
    @SerializedName("position")
    val position: String = "",
    
    @SerializedName("is_active")
    val isActive: Boolean = false
) 