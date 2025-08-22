package com.vugaenterprises.androidtv.data.repository

import com.vugaenterprises.androidtv.data.api.ApiService
import com.vugaenterprises.androidtv.data.model.LiveChannel
import com.vugaenterprises.androidtv.data.model.ChannelCategory
import com.vugaenterprises.androidtv.data.model.LiveChannelsResponse
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
     * Get featured/recommended channels
     */
    fun getFeaturedChannels(channels: List<LiveChannel>): List<LiveChannel> {
        return channels.filter { it.isLive && it.isActive }
            .sortedByDescending { it.sortOrder }
            .take(10)
    }
}

/**
 * Enum for different channel sorting options
 */
enum class ChannelSortBy {
    CHANNEL_NUMBER,
    NAME,
    CATEGORY,
    CUSTOM_ORDER
}