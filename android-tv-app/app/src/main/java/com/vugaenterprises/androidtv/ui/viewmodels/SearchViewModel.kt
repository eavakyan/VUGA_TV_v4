package com.vugaenterprises.androidtv.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val results: List<Content> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {
    
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()
    
    private var searchJob: Job? = null
    
    fun search(query: String) {
        // Cancel previous search
        searchJob?.cancel()
        
        if (query.isBlank()) {
            clearSearch()
            return
        }
        
        searchJob = viewModelScope.launch {
            _searchState.value = _searchState.value.copy(
                query = query,
                isLoading = true,
                error = null
            )
            
            try {
                // Add debounce delay
                delay(300)
                
                // Perform search
                val results = contentRepository.searchContent(query)
                
                _searchState.value = _searchState.value.copy(
                    results = results,
                    isLoading = false
                )
            } catch (e: Exception) {
                _searchState.value = _searchState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    fun clearSearch() {
        searchJob?.cancel()
        _searchState.value = SearchState()
    }
}

