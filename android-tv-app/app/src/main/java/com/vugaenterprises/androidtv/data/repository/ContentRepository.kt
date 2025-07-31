package com.vugaenterprises.androidtv.data.repository

import android.util.Log
import com.vugaenterprises.androidtv.data.api.ApiService
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.WatchHistory
import com.vugaenterprises.androidtv.data.model.HomePageResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun testApiConnection(): String {
        return try {
            Log.d("ContentRepository", "Testing basic API connection...")
            val response = apiService.testConnection()
            Log.d("ContentRepository", "Basic connection test successful: status=${response.status}, message=${response.message}")
            "Connection successful: ${response.message}"
        } catch (e: Exception) {
            Log.e("ContentRepository", "Basic connection test failed", e)
            "Connection failed: ${e.message}"
        }
    }

    suspend fun getHomeData(userId: Int = 1): HomePageResponse {
        return try {
            Log.d("ContentRepository", "Calling getHomeData API for user: $userId")
            val response = apiService.getHomeData(userId)
            Log.d("ContentRepository", "Home data API response: status=${response.status}, message=${response.message}")
            Log.d("ContentRepository", "Featured content size: ${response.featured.size}")
            Log.d("ContentRepository", "Watchlist size: ${response.watchlist.size}")
            Log.d("ContentRepository", "Top contents size: ${response.topContents.size}")
            Log.d("ContentRepository", "Genre contents size: ${response.genreContents.size}")
            response
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error getting home data", e)
            Log.e("ContentRepository", "Error message: ${e.message}")
            Log.e("ContentRepository", "Error cause: ${e.cause}")
            Log.e("ContentRepository", "Full error: ${e}")
            
            // Try to get more details about the error
            if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                Log.e("ContentRepository", "This is an authentication error - API key may be wrong")
            }
            
            HomePageResponse()
        }
    }

    suspend fun getFeaturedContent(userId: Int = 1): List<Content> {
        return try {
            Log.d("ContentRepository", "Calling getHomeData API for featured content...")
            val response = apiService.getHomeData(userId)
            Log.d("ContentRepository", "Featured content API response: status=${response.status}")
            if (response.status) {
                val content = response.featured
                Log.d("ContentRepository", "Returning ${content.size} featured content items")
                content
            } else {
                Log.w("ContentRepository", "Featured content API returned status=false: ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error getting featured content", e)
            emptyList()
        }
    }

    suspend fun getTrendingContent(userId: Int = 1): List<Content> {
        return try {
            Log.d("ContentRepository", "Calling getHomeData API for trending content...")
            val response = apiService.getHomeData(userId)
            Log.d("ContentRepository", "Trending content API response: status=${response.status}")
            if (response.status) {
                // For now, use top contents as trending
                response.topContents.map { it.content }
            } else {
                Log.w("ContentRepository", "Trending content API returned status=false: ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error getting trending content", e)
            emptyList()
        }
    }

    suspend fun getNewContent(userId: Int = 1): List<Content> {
        return try {
            Log.d("ContentRepository", "Calling getHomeData API for new content...")
            val response = apiService.getHomeData(userId)
            Log.d("ContentRepository", "New content API response: status=${response.status}")
            if (response.status) {
                // For now, use genre contents as new content
                response.genreContents.flatMap { it.contents }
            } else {
                Log.w("ContentRepository", "New content API returned status=false: ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error getting new content", e)
            emptyList()
        }
    }

    suspend fun getRecommendations(userId: Int = 1): List<Content> {
        return try {
            Log.d("ContentRepository", "Calling getHomeData API for recommendations...")
            val response = apiService.getHomeData(userId)
            Log.d("ContentRepository", "Recommendations API response: status=${response.status}")
            if (response.status) {
                // For now, use watchlist as recommendations
                response.watchlist
            } else {
                Log.w("ContentRepository", "Recommendations API returned status=false: ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error getting recommendations", e)
            emptyList()
        }
    }

    suspend fun getContinueWatching(userId: Int = 1): List<WatchHistory> {
        return try {
            Log.d("ContentRepository", "Calling getHomeData API for continue watching...")
            val response = apiService.getHomeData(userId)
            Log.d("ContentRepository", "Continue watching API response: status=${response.status}")
            if (response.status) {
                // For now, convert watchlist to watch history format
                response.watchlist.map { content ->
                    WatchHistory(
                        id = content.contentId.toString(),
                        contentId = content.contentId.toString(),
                        userId = "1", // Default user ID
                        content = content,
                        progress = content.watchProgress,
                        position = 0L,
                        duration = 0L,
                        lastWatched = System.currentTimeMillis(),
                        completed = content.completed
                    )
                }
            } else {
                Log.w("ContentRepository", "Continue watching API returned status=false: ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error getting continue watching", e)
            emptyList()
        }
    }

    suspend fun getContentById(contentId: Int, userId: Int = 1): Content? {
        return try {
            Log.d("ContentRepository", "Calling getContentDetail API for id: $contentId, userId: $userId")
            val response = apiService.getContentDetail(userId, contentId)
            Log.d("ContentRepository", "Content by ID API response: status=${response.status}, message=${response.message}")
            if (response.status) {
                Log.d("ContentRepository", "Content data received: ${response.data.title}")
                response.data
            } else {
                Log.w("ContentRepository", "Content by ID API returned status=false: ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error getting content by ID: $contentId", e)
            null
        }
    }

    suspend fun searchContent(query: String): List<Content> {
        return try {
            Log.d("ContentRepository", "Calling searchContent API for query: $query")
            val response = apiService.searchContent(query)
            Log.d("ContentRepository", "Search API response: status=${response.status}, data size=${response.data.size}")
            if (response.status) {
                response.data
            } else {
                Log.w("ContentRepository", "Search API returned status=false: ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error searching content", e)
            emptyList()
        }
    }

    suspend fun getContentByGenre(genreId: Int, start: Int = 0, limit: Int = 20): List<Content> {
        return try {
            Log.d("ContentRepository", "Calling getContentByGenre API for genre: $genreId")
            val response = apiService.getContentByGenre(start, limit, genreId)
            Log.d("ContentRepository", "Content by genre API response: status=${response.status}, data size=${response.data.size}")
            if (response.status) {
                response.data
            } else {
                Log.w("ContentRepository", "Content by genre API returned status=false: ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error getting content by genre", e)
            emptyList()
        }
    }

    suspend fun increaseContentView(contentId: Int) {
        try {
            Log.d("ContentRepository", "Calling increaseContentView API for content: $contentId")
            val response = apiService.increaseContentView(contentId)
            Log.d("ContentRepository", "Increase view API response: status=${response.status}")
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error increasing content view", e)
        }
    }

    suspend fun increaseContentShare(contentId: Int) {
        try {
            Log.d("ContentRepository", "Calling increaseContentShare API for content: $contentId")
            val response = apiService.increaseContentShare(contentId)
            Log.d("ContentRepository", "Increase share API response: status=${response.status}")
        } catch (e: Exception) {
            Log.e("ContentRepository", "Error increasing content share", e)
        }
    }
} 