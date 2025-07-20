package com.vugaenterprises.androidtv.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.api.ApiService
import com.vugaenterprises.androidtv.data.model.WatchHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Use watchlist as history for now
                val response = apiService.getHomeData(1)
                
                if (response.status) {
                    val watchHistory = response.watchlist.map { content ->
                        WatchHistory(
                            id = content.contentId.toString(),
                            contentId = content.contentId.toString(),
                            userId = "1",
                            content = content,
                            progress = content.watchProgress,
                            position = 0L,
                            duration = 0L,
                            lastWatched = System.currentTimeMillis(),
                            completed = content.completed
                        )
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        watchHistory = watchHistory,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.message ?: "Failed to load history"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
}

data class HistoryUiState(
    val watchHistory: List<WatchHistory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 