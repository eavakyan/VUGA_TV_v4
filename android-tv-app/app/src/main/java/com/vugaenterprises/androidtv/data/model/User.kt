package com.vugaenterprises.androidtv.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("profile_picture")
    val profilePicture: String? = null,
    
    @SerializedName("first_name")
    val firstName: String? = null,
    
    @SerializedName("last_name")
    val lastName: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class UserStats(
    @SerializedName("total_watch_time")
    val totalWatchTime: Int,

    @SerializedName("total_content_watched")
    val totalContentWatched: Int,

    @SerializedName("total_favorites")
    val totalFavorites: Int,

    @SerializedName("favorite_genres")
    val favoriteGenres: List<String> = emptyList(),

    @SerializedName("average_rating")
    val averageRating: Double = 0.0,

    @SerializedName("watch_history")
    val watchHistory: List<WatchHistory> = emptyList(),

    @SerializedName("favorites")
    val favorites: List<Content> = emptyList()
) 