package com.vugaenterprises.androidtv.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.WatchHistory
import com.vugaenterprises.androidtv.data.model.GenreContents
import com.vugaenterprises.androidtv.data.repository.ContentRepository
import com.vugaenterprises.androidtv.data.UserDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val userDataStore: UserDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Starting to load content...")
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Check if user is logged in
                val isLoggedIn = userDataStore.isLoggedIn().first()
                Log.d("HomeViewModel", "User login status: $isLoggedIn")
                
                // Get user ID and profile ID from data store
                // Use userId = 0 for guest users to show all content
                val userId = userDataStore.getUserId().first() ?: 0
                val profileId = try {
                    userDataStore.getSelectedProfile().first()?.profileId
                } catch (e: Exception) {
                    null
                }
                
                Log.d("HomeViewModel", "Loading home data for userId: $userId, profileId: $profileId")
                
                // Load home data which contains all sections
                val homeData = contentRepository.getHomeData(userId, profileId)
                Log.d("HomeViewModel", "Home data loaded: status=${homeData.status}")
                
                if (homeData.status) {
                    Log.d("HomeViewModel", "Featured content loaded: ${homeData.featured.size} items")
                    homeData.featured.forEach { content ->
                        Log.d("HomeViewModel", "Featured content: ID=${content.contentId}, Title=${content.title}")
                    }
                    Log.d("HomeViewModel", "Trending content loaded: ${homeData.topContents.size} items")
                    homeData.topContents.forEach { topContent ->
                        Log.d("HomeViewModel", "Top content: ID=${topContent.content.contentId}, Title=${topContent.content.title}")
                    }
                    Log.d("HomeViewModel", "New content loaded: ${homeData.genreContents.size} genre sections")
                    homeData.genreContents.forEach { genreContent ->
                        Log.d("HomeViewModel", "Genre '${genreContent.title}': ${genreContent.contents.size} items")
                        genreContent.contents.forEach { content ->
                            Log.d("HomeViewModel", "  - ID=${content.contentId}, Title=${content.title}")
                        }
                    }
                    Log.d("HomeViewModel", "Watchlist loaded: ${homeData.watchlist.size} items")

                    // Filter out personalized content if user is not logged in
                    val watchlistContent = if (isLoggedIn) homeData.watchlist else emptyList()
                    val continueWatchingContent = if (isLoggedIn) {
                        homeData.watchlist.map { content ->
                            WatchHistory(
                                id = content.contentId.toString(),
                                contentId = content.contentId.toString(),
                                userId = userId.toString(),
                                content = content,
                                progress = content.watchProgress,
                                position = 0L,
                                duration = 0L,
                                lastWatched = System.currentTimeMillis(),
                                completed = content.completed
                            )
                        }
                    } else {
                        emptyList()
                    }
                    
                    Log.d("HomeViewModel", "Filtering content based on auth status - Watchlist: ${watchlistContent.size}, Continue Watching: ${continueWatchingContent.size}")

                    // Filter categories to only include those with content
                    val filteredCategoryContent = homeData.genreContents.filter { it.contents.isNotEmpty() }
                    
                    _uiState.value = _uiState.value.copy(
                        featuredContent = homeData.featured,
                        trendingContent = homeData.topContents.map { it.content },
                        newContent = homeData.genreContents.flatMap { it.contents },
                        recommendations = watchlistContent,
                        continueWatching = continueWatchingContent,
                        categoryContent = filteredCategoryContent,
                        isLoading = false,
                        error = null
                    )
                    Log.d("HomeViewModel", "Content loading completed successfully")
                } else {
                    Log.w("HomeViewModel", "Home data API returned status=false: ${homeData.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = homeData.message
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading content", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun refreshContent() {
        loadContent()
    }

    fun refreshWatchlist() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Refreshing watchlist data...")
                
                // Get user ID and profile ID from data store
                // Use userId = 0 for guest users to show all content
                val userId = userDataStore.getUserId().first() ?: 0
                val profileId = try {
                    userDataStore.getSelectedProfile().first()?.profileId
                } catch (e: Exception) {
                    null
                }
                
                // Get fresh watchlist data
                val updatedWatchlist = contentRepository.getWatchlist(userId, profileId)
                Log.d("HomeViewModel", "Updated watchlist: ${updatedWatchlist.size} items")
                
                // Update only the watchlist/recommendations in the current state
                _uiState.value = _uiState.value.copy(
                    recommendations = updatedWatchlist
                )
                
                Log.d("HomeViewModel", "Watchlist refresh completed")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error refreshing watchlist", e)
            }
        }
    }

    fun testApiConnection() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Testing API connection...")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // First test basic connection
                val basicTest = contentRepository.testApiConnection()
                Log.d("HomeViewModel", "Basic connection test: $basicTest")
                
                // Then test the home data API
                // Use userId = 0 for guest users to show all content
                val userId = userDataStore.getUserId().first() ?: 0
                val profileId = try {
                    userDataStore.getSelectedProfile().first()?.profileId
                } catch (e: Exception) {
                    null
                }
                val homeData = contentRepository.getHomeData(userId, profileId)
                Log.d("HomeViewModel", "API test successful: got ${homeData.featured.size} featured items")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "API connection successful! Found ${homeData.featured.size} featured items."
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "API test failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "API connection failed: ${e.message}"
                )
            }
        }
    }
}

data class HomeUiState(
    val featuredContent: List<Content> = emptyList(),
    val trendingContent: List<Content> = emptyList(),
    val newContent: List<Content> = emptyList(),
    val recommendations: List<Content> = emptyList(),
    val continueWatching: List<WatchHistory> = emptyList(),
    val categoryContent: List<GenreContents> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 