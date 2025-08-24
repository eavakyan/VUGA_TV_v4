package com.vugaenterprises.androidtv.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data model for Live TV channels
 */
data class LiveChannel(
    @SerializedName("tv_channel_id")
    val id: Int = 0,

    @SerializedName("title")
    val name: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("thumbnail")
    val thumbnailUrl: String = "",

    @SerializedName("logo_url")
    val logoUrl: String = "",

    @SerializedName("source")
    val streamUrl: String = "",

    @SerializedName("is_live")
    val isLive: Boolean = true,

    @SerializedName("category")
    val category: String = "",

    @SerializedName("current_program")
    val currentProgram: String? = null,

    @SerializedName("next_program")
    val nextProgram: String? = null,

    @SerializedName("program_time")
    val programTime: String? = null,

    @SerializedName("channel_number")
    val channelNumber: Int = 0,

    @SerializedName("language")
    val language: String = "",

    @SerializedName("quality")
    val quality: String = "HD",

    @SerializedName("type")
    val streamType: Int = 2, // Based on API response: type=2

    @SerializedName("access_type")
    val isPremium: Int = 1, // Based on API response: access_type=1

    @SerializedName("is_adult")
    val isAdult: Boolean = false,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("sort_order")
    val sortOrder: Int = 0,

    @SerializedName("created_at")
    val createdAt: String = "",

    @SerializedName("updated_at")
    val updatedAt: String = "",

    @SerializedName("categories")
    val categories: List<ChannelCategory> = emptyList(),

    @SerializedName("current_schedule")
    val currentSchedule: LiveTvSchedule? = null,

    @SerializedName("next_schedule")
    val nextSchedule: LiveTvSchedule? = null,

    @SerializedName("schedule")
    val todaySchedule: List<LiveTvSchedule> = emptyList()
) {
    /**
     * Get formatted channel display name with number
     */
    val displayName: String
        get() = if (channelNumber > 0) "$channelNumber. $name" else name

    /**
     * Get the appropriate logo/thumbnail URL
     */
    val imageUrl: String
        get() = if (logoUrl.isNotEmpty()) logoUrl else thumbnailUrl

    /**
     * Check if channel has current program information
     */
    val hasProgramInfo: Boolean
        get() = currentSchedule != null || !currentProgram.isNullOrEmpty()

    /**
     * Check if channel has schedule data
     */
    val hasScheduleData: Boolean
        get() = currentSchedule != null || todaySchedule.isNotEmpty()

    /**
     * Check if current program is ending soon (less than 15 minutes)
     */
    val isEndingSoon: Boolean
        get() = currentSchedule?.timeRemainingMinutes ?: 0 <= 15

    /**
     * Get formatted program information
     */
    val programInfo: String
        get() = when {
            currentSchedule != null -> currentSchedule.title
            !currentProgram.isNullOrEmpty() && !programTime.isNullOrEmpty() -> 
                "$currentProgram • $programTime"
            !currentProgram.isNullOrEmpty() -> currentProgram
            else -> "Live Programming"
        }

    /**
     * Get current program time information
     */
    val currentProgramTime: String
        get() = currentSchedule?.timeRange ?: programTime ?: ""

    /**
     * Get time remaining for current program
     */
    val timeRemaining: String
        get() = currentSchedule?.timeRemainingDisplay ?: ""

    /**
     * Get next program information
     */
    val nextProgramInfo: String
        get() = when {
            nextSchedule != null -> "Next: ${nextSchedule.title} at ${nextSchedule.startTime}"
            !nextProgram.isNullOrEmpty() -> "Next: $nextProgram"
            else -> ""
        }
}

/**
 * Response wrapper for Live TV channels API
 */
data class LiveChannelsResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: List<LiveChannel> = emptyList(),

    @SerializedName("pagination")
    val pagination: Pagination? = null
)

/**
 * Response wrapper for Live TV categories API
 */
data class LiveCategoriesResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: List<ChannelCategory> = emptyList()
)

/**
 * Pagination info for Live TV responses
 */
data class Pagination(
    @SerializedName("current_page")
    val currentPage: Int = 1,

    @SerializedName("last_page")
    val lastPage: Int = 1,

    @SerializedName("per_page")
    val perPage: Int = 20,

    @SerializedName("total")
    val total: Int = 0
)

/**
 * Channel category for filtering
 */
data class ChannelCategory(
    @SerializedName("tv_category_id")
    val id: Int = 0,

    @SerializedName("title")
    val name: String = "",

    @SerializedName("slug")
    val slug: String = "",

    @SerializedName("channels_count")
    val channelCount: Int = 0,

    @SerializedName("image")
    val imageUrl: String = "",

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("created_at")
    val createdAt: String = "",

    @SerializedName("updated_at")
    val updatedAt: String = ""
)

/**
 * Live TV program schedule entry
 */
data class LiveTvSchedule(
    @SerializedName("program_id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("start_time")
    val startTime: String = "",

    @SerializedName("end_time")
    val endTime: String = "",

    @SerializedName("duration_minutes")
    val durationMinutes: Int = 0,

    @SerializedName("is_current")
    val isCurrent: Boolean = false,

    @SerializedName("genre")
    val genre: String = "",

    @SerializedName("rating")
    val rating: String = "",

    @SerializedName("thumbnail")
    val thumbnailUrl: String = ""
) {
    /**
     * Get formatted time range for display
     */
    val timeRange: String
        get() = "$startTime - $endTime"

    /**
     * Get time remaining in minutes (if current program)
     */
    val timeRemainingMinutes: Int
        get() {
            if (!isCurrent) return 0
            // TODO: Calculate based on current time and end_time
            return durationMinutes / 2 // Placeholder
        }

    /**
     * Get formatted time remaining display
     */
    val timeRemainingDisplay: String
        get() {
            val remaining = timeRemainingMinutes
            return when {
                remaining <= 0 -> "Ending soon"
                remaining < 60 -> "${remaining}m left"
                else -> "${remaining / 60}h ${remaining % 60}m left"
            }
        }
}

/**
 * Live TV category with enhanced metadata
 */
data class LiveTvCategory(
    @SerializedName("tv_category_id")
    val id: Int = 0,

    @SerializedName("title")
    val name: String = "",

    @SerializedName("slug")
    val slug: String = "",

    @SerializedName("icon")
    val iconUrl: String = "",

    @SerializedName("channels_count")
    val channelCount: Int = 0,

    @SerializedName("featured_channel_id")
    val featuredChannelId: Int? = null,

    @SerializedName("sort_order")
    val sortOrder: Int = 0,

    @SerializedName("is_active")
    val isActive: Boolean = true
) {
    /**
     * Get display name with channel count
     */
    val displayName: String
        get() = if (channelCount > 0) "$name ($channelCount)" else name
}

/**
 * Response wrapper for Live TV schedule API
 */
data class LiveTvScheduleResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: List<LiveTvSchedule> = emptyList()
)

/**
 * Response wrapper for Live TV categories API (enhanced)
 */
data class LiveTvCategoriesResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: List<LiveTvCategory> = emptyList()
)

/**
 * Live TV streaming quality options
 */
enum class StreamQuality(val displayName: String, val value: String) {
    AUTO("Auto", "auto"),
    HD("HD", "720p"),
    FULL_HD("Full HD", "1080p"),
    SD("SD", "480p"),
    LOW("Low", "360p")
}

/**
 * Live TV stream types
 */
enum class StreamType(val displayName: String, val value: String) {
    HLS("HLS", "m3u8"),
    RTMP("RTMP", "rtmp"),
    YOUTUBE("YouTube", "youtube"),
    DIRECT("Direct", "direct")
}