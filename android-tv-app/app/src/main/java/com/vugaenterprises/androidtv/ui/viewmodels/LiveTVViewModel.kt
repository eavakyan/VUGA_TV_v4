package com.vugaenterprises.androidtv.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.UserDataStore
import com.vugaenterprises.androidtv.data.model.LiveChannel
import com.vugaenterprises.androidtv.data.model.ChannelCategory
import com.vugaenterprises.androidtv.data.model.UserData
import com.vugaenterprises.androidtv.data.repository.LiveTVRepository
import com.vugaenterprises.androidtv.data.repository.ChannelSortBy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Live TV screen
 */
data class LiveTVUiState(
    val isLoading: Boolean = false,
    val channels: List<LiveChannel> = emptyList(),
    val filteredChannels: List<LiveChannel> = emptyList(),
    val categories: List<ChannelCategory> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val sortBy: ChannelSortBy = ChannelSortBy.CHANNEL_NUMBER,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * ViewModel for managing Live TV screen state and business logic
 */
@HiltViewModel
class LiveTVViewModel @Inject constructor(
    private val liveTVRepository: LiveTVRepository,
    private val userDataStore: UserDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveTVUiState())
    val uiState: StateFlow<LiveTVUiState> = _uiState.asStateFlow()

    // Current user and profile data
    private val userData = userDataStore.getUserData().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    private val selectedProfile = userDataStore.getSelectedProfile().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        // Load data when user data becomes available
        viewModelScope.launch {
            userData.filterNotNull().take(1).collect { user ->
                loadLiveChannels()
                loadCategories()
            }
        }
    }

    /**
     * Load live TV channels from API
     */
    fun loadLiveChannels() {
        val userId = userData.value?.id ?: return
        val profileId = selectedProfile.value?.profileId
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            liveTVRepository.getLiveChannels(
                userId = userId,
                profileId = profileId,
                category = _uiState.value.selectedCategory
            ).fold(
                onSuccess = { channels ->
                    val sortedChannels = liveTVRepository.sortChannels(channels, _uiState.value.sortBy)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            channels = sortedChannels,
                            filteredChannels = applyFilters(sortedChannels)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load channels"
                        )
                    }
                }
            )
        }
    }

    /**
     * Load channel categories for filtering
     */
    private fun loadCategories() {
        val userId = userData.value?.id ?: return
        val profileId = selectedProfile.value?.profileId
        
        viewModelScope.launch {
            liveTVRepository.getChannelCategories(
                userId = userId,
                profileId = profileId
            ).fold(
                onSuccess = { categories ->
                    _uiState.update { it.copy(categories = categories) }
                },
                onFailure = { error ->
                    // Categories are optional, don't show error to user
                    android.util.Log.e("LiveTVViewModel", "Failed to load categories", error)
                }
            )
        }
    }

    /**
     * Refresh channels data
     */
    fun refreshChannels() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadLiveChannels()
        loadCategories()
        _uiState.update { it.copy(isRefreshing = false) }
    }

    /**
     * Filter channels by category
     */
    fun filterByCategory(category: String?) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                filteredChannels = applyFilters(currentState.channels)
            )
        }
        
        // Optionally reload with server-side filtering
        loadLiveChannels()
    }

    /**
     * Search channels by query
     */
    fun searchChannels(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredChannels = applyFilters(currentState.channels)
            )
        }
    }

    /**
     * Sort channels by different criteria
     */
    fun sortChannels(sortBy: ChannelSortBy) {
        _uiState.update { currentState ->
            val sortedChannels = liveTVRepository.sortChannels(currentState.channels, sortBy)
            currentState.copy(
                sortBy = sortBy,
                channels = sortedChannels,
                filteredChannels = applyFilters(sortedChannels)
            )
        }
    }

    /**
     * Clear search query
     */
    fun clearSearch() {
        searchChannels("")
    }

    /**
     * Clear category filter
     */
    fun clearCategoryFilter() {
        filterByCategory(null)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Track channel view for analytics
     */
    fun trackChannelView(channelId: Int, watchDuration: Int) {
        val userId = userData.value?.id ?: return
        val profileId = selectedProfile.value?.profileId
        
        viewModelScope.launch {
            liveTVRepository.trackChannelView(
                userId = userId,
                channelId = channelId,
                watchDuration = watchDuration,
                profileId = profileId
            )
        }
    }

    /**
     * Get featured channels for highlights
     */
    fun getFeaturedChannels(): List<LiveChannel> {
        return liveTVRepository.getFeaturedChannels(_uiState.value.channels)
    }

    /**
     * Apply current filters and search to channel list
     */
    private fun applyFilters(channels: List<LiveChannel>): List<LiveChannel> {
        var filtered = channels
        
        // Apply category filter
        val currentState = _uiState.value
        if (!currentState.selectedCategory.isNullOrEmpty()) {
            filtered = liveTVRepository.filterChannelsByCategory(filtered, currentState.selectedCategory)
        }
        
        // Apply search filter
        if (currentState.searchQuery.isNotEmpty()) {
            filtered = liveTVRepository.searchChannels(filtered, currentState.searchQuery)
        }
        
        return filtered
    }

    /**
     * Get channel by ID
     */
    fun getChannelById(channelId: Int): LiveChannel? {
        return _uiState.value.channels.find { it.id == channelId }
    }

    /**
     * Check if there are any active filters
     */
    fun hasActiveFilters(): Boolean {
        val currentState = _uiState.value
        return !currentState.selectedCategory.isNullOrEmpty() || currentState.searchQuery.isNotEmpty()
    }

    /**
     * Clear all filters
     */
    fun clearAllFilters() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = null,
                searchQuery = "",
                filteredChannels = currentState.channels
            )
        }
    }
}