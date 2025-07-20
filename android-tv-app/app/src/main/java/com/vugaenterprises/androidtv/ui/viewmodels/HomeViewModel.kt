package com.vugaenterprises.androidtv.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.WatchHistory
import com.vugaenterprises.androidtv.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository
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
                
                // For now, use user ID 1 - in a real app, this would come from auth
                val userId = 1
                
                // Load home data which contains all sections
                Log.d("HomeViewModel", "Loading home data...")
                val homeData = contentRepository.getHomeData(userId)
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

                    _uiState.value = _uiState.value.copy(
                        featuredContent = homeData.featured,
                        trendingContent = homeData.topContents.map { it.content },
                        newContent = homeData.genreContents.flatMap { it.contents },
                        recommendations = homeData.watchlist,
                        continueWatching = homeData.watchlist.map { content ->
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
                        },
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

    fun testApiConnection() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Testing API connection...")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // First test basic connection
                val basicTest = contentRepository.testApiConnection()
                Log.d("HomeViewModel", "Basic connection test: $basicTest")
                
                // Then test the home data API
                val homeData = contentRepository.getHomeData(1)
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
    val isLoading: Boolean = false,
    val error: String? = null
) 