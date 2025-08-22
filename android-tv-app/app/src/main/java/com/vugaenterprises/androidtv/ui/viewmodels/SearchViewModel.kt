package com.vugaenterprises.androidtv.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.GenreContents
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
    val categories: List<CategoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CategoryItem(
    val id: Int,
    val title: String,
    val contentCount: Int = 0
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {
    
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()
    
    private var searchJob: Job? = null
    
    init {
        loadCategories()
    }
    
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
        _searchState.value = _searchState.value.copy(
            query = "",
            results = emptyList(),
            error = null
        )
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                // Get categories from home data
                val homeData = contentRepository.getHomeData()
                if (homeData.status) {
                    val categories = homeData.genreContents.map { genreContent ->
                        CategoryItem(
                            id = genreContent.id,
                            title = genreContent.title,
                            contentCount = genreContent.contents.size
                        )
                    }
                    _searchState.value = _searchState.value.copy(categories = categories)
                }
            } catch (e: Exception) {
                // If fetching categories fails, use default categories
                val defaultCategories = listOf(
                    CategoryItem(0, "Action", 0),
                    CategoryItem(0, "Comedy", 0),
                    CategoryItem(0, "Drama", 0),
                    CategoryItem(0, "Horror", 0),
                    CategoryItem(0, "Sci-Fi", 0),
                    CategoryItem(0, "Documentary", 0),
                    CategoryItem(0, "Animation", 0),
                    CategoryItem(0, "Romance", 0)
                )
                _searchState.value = _searchState.value.copy(categories = defaultCategories)
            }
        }
    }
    
    fun searchByCategory(categoryId: Int) {
        if (categoryId <= 0) return
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.value = _searchState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val results = contentRepository.getContentByGenre(categoryId)
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
}

