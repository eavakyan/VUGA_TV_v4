package com.vugaenterprises.androidtv.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.api.ApiService
import com.vugaenterprises.androidtv.data.model.User
import com.vugaenterprises.androidtv.data.model.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // For now, create a mock user profile
                val mockUser = User(
                    userId = 1,
                    username = "demo_user",
                    email = "demo@example.com",
                    profilePicture = "",
                    firstName = "Demo",
                    lastName = "User"
                )
                
                val mockUserStats = UserStats(
                    totalWatchTime = 120,
                    totalContentWatched = 15,
                    totalFavorites = 8,
                    favoriteGenres = listOf("Action", "Drama", "Comedy"),
                    averageRating = 4.2,
                    watchHistory = emptyList(),
                    favorites = emptyList()
                )
                
                _uiState.value = _uiState.value.copy(
                    userProfile = mockUser,
                    userStats = mockUserStats,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun refreshProfile() {
        loadProfile()
    }
}

data class ProfileUiState(
    val userProfile: User? = null,
    val userStats: UserStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) 