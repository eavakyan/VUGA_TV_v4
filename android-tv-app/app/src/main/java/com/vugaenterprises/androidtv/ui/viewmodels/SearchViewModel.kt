package com.vugaenterprises.androidtv.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Popular search suggestions
    private val popularSearches = listOf("F1", "Action", "Drama", "Horror", "Comedy", "Sci-Fi", "Thriller", "Romance")
    
    // Recent searches (in a real app, this would be persisted)
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    init {
        // Debounce search queries to avoid too many API calls
        searchQuery
            .debounce(500)
            .filter { it.length >= 2 }
            .distinctUntilChanged()
            .onEach { query ->
                searchContent(query)
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }

    fun searchContent(query: String) {
        if (query.length < 2) return
        
        viewModelScope.launch {
            try {
                Log.d("SearchViewModel", "Searching for: $query")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val results = contentRepository.searchContent(query)
                
                Log.d("SearchViewModel", "Search results: ${results.size} items")
                
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isLoading = false,
                    error = null
                )
                
                // Add to recent searches (in a real app, this would be persisted)
                addToRecentSearches(query)
                
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }
    
    private fun addToRecentSearches(query: String) {
        val current = _recentSearches.value.toMutableList()
        if (!current.contains(query)) {
            current.add(0, query)
            if (current.size > 10) {
                current.removeAt(current.size - 1)
            }
            _recentSearches.value = current
        }
    }
    
    fun getPopularSearches(): List<String> = popularSearches
    
    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }
}

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<Content> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 