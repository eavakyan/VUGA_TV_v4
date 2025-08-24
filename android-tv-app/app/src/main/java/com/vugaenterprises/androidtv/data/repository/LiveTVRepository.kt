package com.vugaenterprises.androidtv.data.repository

import com.vugaenterprises.androidtv.data.api.ApiService
import com.vugaenterprises.androidtv.data.model.LiveChannel
import com.vugaenterprises.androidtv.data.model.ChannelCategory
import com.vugaenterprises.androidtv.data.model.LiveChannelsResponse
import com.vugaenterprises.androidtv.data.model.LiveTvSchedule
import com.vugaenterprises.androidtv.data.model.LiveTvScheduleResponse
import com.vugaenterprises.androidtv.data.model.LiveTvCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Live TV channels and related data
 */
@Singleton
class LiveTVRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * Get all live TV channels with optional filtering
     */
    suspend fun getLiveChannels(
        userId: Int,
        profileId: Int? = null,
        category: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<LiveChannel>> {
        return try {
            val response = apiService.getLiveChannels(
                userId = userId,
                profileId = profileId,
                category = category,
                limit = limit,
                offset = offset
            )
            
            if (response.status) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get channel categories for filtering
     */
    suspend fun getChannelCategories(
        userId: Int,
        profileId: Int? = null
    ): Result<List<ChannelCategory>> {
        return try {
            val response = apiService.getLiveChannelCategories(
                userId = userId,
                profileId = profileId
            )
            
            if (response.status) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get detailed information for a specific channel
     */
    suspend fun getChannelDetails(
        userId: Int,
        channelId: Int,
        profileId: Int? = null
    ): Result<LiveChannel> {
        return try {
            val response = apiService.getLiveChannelDetails(
                userId = userId,
                channelId = channelId,
                profileId = profileId
            )
            
            if (response.status && response.data.isNotEmpty()) {
                Result.success(response.data.first())
            } else {
                Result.failure(Exception(response.message.ifEmpty { "Channel not found" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Track channel view for analytics
     */
    suspend fun trackChannelView(
        userId: Int,
        channelId: Int,
        watchDuration: Int,
        profileId: Int? = null
    ): Result<Unit> {
        return try {
            val response = apiService.trackLiveChannelView(
                userId = userId,
                channelId = channelId,
                watchDuration = watchDuration,
                profileId = profileId
            )
            
            if (response.status) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get live channels as a Flow for reactive UI updates
     */
    fun getLiveChannelsFlow(
        userId: Int,
        profileId: Int? = null,
        category: String? = null
    ): Flow<Result<List<LiveChannel>>> = flow {
        emit(getLiveChannels(userId, profileId, category))
    }
    
    /**
     * Filter channels by category locally
     */
    fun filterChannelsByCategory(
        channels: List<LiveChannel>,
        category: String?
    ): List<LiveChannel> {
        return if (category.isNullOrEmpty()) {
            channels
        } else {
            channels.filter { it.category.equals(category, ignoreCase = true) }
        }
    }
    
    /**
     * Sort channels by different criteria
     */
    fun sortChannels(
        channels: List<LiveChannel>,
        sortBy: ChannelSortBy = ChannelSortBy.CHANNEL_NUMBER
    ): List<LiveChannel> {
        return when (sortBy) {
            ChannelSortBy.CHANNEL_NUMBER -> channels.sortedBy { it.channelNumber }
            ChannelSortBy.NAME -> channels.sortedBy { it.name }
            ChannelSortBy.CATEGORY -> channels.sortedBy { it.category }
            ChannelSortBy.CUSTOM_ORDER -> channels.sortedBy { it.sortOrder }
            ChannelSortBy.CURRENT_PROGRAM -> channels.sortedWith(
                compareByDescending<LiveChannel> { it.currentSchedule != null }
                    .thenBy { it.currentSchedule?.title ?: "" }
            )
            ChannelSortBy.ENDING_SOON -> channels.sortedWith(
                compareByDescending<LiveChannel> { it.isEndingSoon }
                    .thenBy { it.currentSchedule?.timeRemainingMinutes ?: Int.MAX_VALUE }
            )
        }
    }
    
    /**
     * Search channels by name or description
     */
    fun searchChannels(
        channels: List<LiveChannel>,
        query: String
    ): List<LiveChannel> {
        if (query.isBlank()) return channels
        
        val lowerQuery = query.lowercase()
        return channels.filter { channel ->
            channel.name.lowercase().contains(lowerQuery) ||
            channel.description.lowercase().contains(lowerQuery) ||
            channel.category.lowercase().contains(lowerQuery) ||
            channel.channelNumber.toString().contains(query)
        }
    }
    
    /**
     * Get channel schedule for a specific date
     */
    suspend fun getChannelSchedule(
        userId: Int,
        channelId: Int,
        date: String,
        profileId: Int? = null
    ): Result<List<LiveTvSchedule>> {
        return try {
            val response = apiService.getLiveChannelSchedule(
                userId = userId,
                channelId = channelId,
                date = date,
                profileId = profileId
            )
            
            if (response.status) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get EPG data for multiple channels
     */
    suspend fun getEPGData(
        userId: Int,
        channelIds: List<Int>,
        date: String,
        profileId: Int? = null
    ): Result<Map<Int, List<LiveTvSchedule>>> {
        return try {
            val scheduleMap = mutableMapOf<Int, List<LiveTvSchedule>>()
            
            // Fetch schedule for each channel
            channelIds.forEach { channelId ->
                getChannelSchedule(userId, channelId, date, profileId).fold(
                    onSuccess = { schedule -> scheduleMap[channelId] = schedule },
                    onFailure = { /* Log error but continue with other channels */ }
                )
            }
            
            Result.success(scheduleMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current program for a channel
     */
    suspend fun getCurrentProgram(
        userId: Int,
        channelId: Int,
        profileId: Int? = null
    ): Result<LiveTvSchedule?> {
        return try {
            val currentDate = getCurrentDateString()
            getChannelSchedule(userId, channelId, currentDate, profileId).fold(
                onSuccess = { schedule ->
                    val currentProgram = schedule.find { it.isCurrent }
                    Result.success(currentProgram)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Calculate program progress percentage
     */
    fun calculateProgramProgress(schedule: LiveTvSchedule): Float {
        if (!schedule.isCurrent || schedule.durationMinutes <= 0) return 0f
        
        try {
            // This is a simplified calculation
            // In a real implementation, you'd parse the actual start/end times
            val elapsed = schedule.durationMinutes - schedule.timeRemainingMinutes
            return (elapsed.toFloat() / schedule.durationMinutes.toFloat()).coerceIn(0f, 1f)
        } catch (e: Exception) {
            return 0f
        }
    }
    
    /**
     * Get channels with enhanced program information
     */
    suspend fun getChannelsWithPrograms(
        userId: Int,
        profileId: Int? = null,
        category: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<LiveChannel>> {
        return try {
            // First get the channels
            val channelsResult = getLiveChannels(userId, profileId, category, limit, offset)
            
            channelsResult.fold(
                onSuccess = { channels ->
                    // Then get schedule data for each channel
                    val currentDate = getCurrentDateString()
                    val enhancedChannels = channels.map { channel ->
                        getChannelSchedule(userId, channel.id, currentDate, profileId).fold(
                            onSuccess = { schedule ->
                                val currentProgram = schedule.find { it.isCurrent }
                                val nextProgram = schedule.find { !it.isCurrent && (currentProgram == null || it.startTime > currentProgram.endTime) }
                                
                                channel.copy(
                                    currentSchedule = currentProgram,
                                    nextSchedule = nextProgram,
                                    todaySchedule = schedule
                                )
                            },
                            onFailure = { channel } // Return channel without schedule data on error
                        )
                    }
                    Result.success(enhancedChannels)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current date string
     */
    private fun getCurrentDateString(): String {
        val calendar = java.util.Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
    
    /**
     * Get featured/recommended channels with program data
     */
    fun getFeaturedChannels(channels: List<LiveChannel>): List<LiveChannel> {
        val currentTime = System.currentTimeMillis()
        
        return channels.filter { it.isLive && it.isActive }
            .filter { channel ->
                // Prioritize channels with current programs
                channel.currentSchedule != null || channel.hasProgramInfo
            }
            .sortedWith(compareByDescending<LiveChannel> { it.sortOrder }
                .thenByDescending { it.currentSchedule?.isCurrent == true }
                .thenByDescending { it.hasScheduleData })
            .take(12)
    }
    
    /**
     * Filter channels by program content
     */
    fun filterChannelsByProgram(
        channels: List<LiveChannel>,
        programQuery: String
    ): List<LiveChannel> {
        if (programQuery.isBlank()) return channels
        
        val lowerQuery = programQuery.lowercase()
        return channels.filter { channel ->
            channel.currentSchedule?.title?.lowercase()?.contains(lowerQuery) == true ||
            channel.nextSchedule?.title?.lowercase()?.contains(lowerQuery) == true ||
            channel.todaySchedule.any { it.title.lowercase().contains(lowerQuery) }
        }
    }
    
    /**
     * Get channels by time slot
     */
    fun getChannelsByTimeSlot(
        channels: List<LiveChannel>,
        startHour: Int,
        endHour: Int
    ): List<LiveChannel> {
        return channels.filter { channel ->
            channel.todaySchedule.any { schedule ->
                // This would need proper time parsing in a real implementation
                schedule.isCurrent || schedule.startTime.isNotEmpty()
            }
        }
    }
}

/**
 * Enum for different channel sorting options
 */
enum class ChannelSortBy {
    CHANNEL_NUMBER,
    NAME,
    CATEGORY,
    CUSTOM_ORDER,
    CURRENT_PROGRAM,
    ENDING_SOON
}