package com.vugaenterprises.androidtv.data.model

import com.google.gson.annotations.SerializedName

data class WatchHistory(
    @SerializedName("_id")
    val id: String,
    @SerializedName("contentId")
    val contentId: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("progress")
    val progress: Float, // 0.0 to 1.0
    @SerializedName("position")
    val position: Long, // Position in seconds
    @SerializedName("duration")
    val duration: Long, // Total duration in seconds
    @SerializedName("lastWatched")
    val lastWatched: Long, // Timestamp
    @SerializedName("content")
    val content: Content? = null,
    @SerializedName("completed")
    val completed: Boolean = false
) 