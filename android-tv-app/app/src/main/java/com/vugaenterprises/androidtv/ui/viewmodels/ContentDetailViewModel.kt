package com.vugaenterprises.androidtv.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.model.Content
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
class ContentDetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val userDataStore: UserDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentDetailUiState())
    val uiState: StateFlow<ContentDetailUiState> = _uiState.asStateFlow()

    fun loadContent(contentId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ContentDetailViewModel", "Loading content with ID: $contentId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Get user ID and profile ID from data store
                // Use userId = 0 for guest users to show all content
                val userId = userDataStore.getUserId().first() ?: 0
                val profileId = try {
                    userDataStore.getSelectedProfile().first()?.profileId
                } catch (e: Exception) {
                    null
                }
                
                val content = contentRepository.getContentById(contentId, userId, profileId)
                
                if (content != null) {
                    Log.d("ContentDetailViewModel", "Content loaded successfully: ${content.title}")
                    _uiState.value = _uiState.value.copy(
                        content = content,
                        isLoading = false,
                        error = null
                    )
                } else {
                    Log.w("ContentDetailViewModel", "Content not found for ID: $contentId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Content not found"
                    )
                }
            } catch (e: Exception) {
                Log.e("ContentDetailViewModel", "Error loading content with ID: $contentId", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun toggleWatchlist() {
        val content = _uiState.value.content ?: return
        
        viewModelScope.launch {
            try {
                Log.d("ContentDetailViewModel", "Toggling watchlist for content: ${content.title}")
                _uiState.value = _uiState.value.copy(isUpdatingWatchlist = true)
                
                // Get user ID and profile ID from data store
                val userId = userDataStore.getUserId().first() ?: return@launch
                val profileId = try {
                    userDataStore.getSelectedProfile().first()?.profileId
                } catch (e: Exception) {
                    null
                }
                
                val success = contentRepository.toggleWatchlist(content.contentId, userId, profileId)
                
                if (success) {
                    // Update the content's watchlist status locally
                    val updatedContent = content.copy(isWatchlist = !content.isWatchlist)
                    _uiState.value = _uiState.value.copy(
                        content = updatedContent,
                        isUpdatingWatchlist = false,
                        watchlistChanged = true
                    )
                    Log.d("ContentDetailViewModel", "Watchlist toggle successful: ${updatedContent.isWatchlist}")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingWatchlist = false,
                        error = "Failed to update watchlist"
                    )
                    Log.w("ContentDetailViewModel", "Watchlist toggle failed")
                }
            } catch (e: Exception) {
                Log.e("ContentDetailViewModel", "Error toggling watchlist", e)
                _uiState.value = _uiState.value.copy(
                    isUpdatingWatchlist = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun onWatchlistChangeHandled() {
        _uiState.value = _uiState.value.copy(watchlistChanged = false)
    }
}

data class ContentDetailUiState(
    val content: Content? = null,
    val isLoading: Boolean = false,
    val isUpdatingWatchlist: Boolean = false,
    val watchlistChanged: Boolean = false,
    val error: String? = null
) 