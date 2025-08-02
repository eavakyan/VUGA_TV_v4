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
                val userId = userDataStore.getUserId().first() ?: 1
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
}

data class ContentDetailUiState(
    val content: Content? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) 