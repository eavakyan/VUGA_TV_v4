package com.vugaenterprises.androidtv.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.model.AgeRating
import com.vugaenterprises.androidtv.data.model.Profile
import com.vugaenterprises.androidtv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgeSettingsUiState(
    val profile: Profile? = null,
    val ageRatings: List<AgeRating> = emptyList(),
    val isKidsProfile: Boolean = false,
    val selectedAge: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AgeSettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AgeSettingsUiState())
    val uiState: StateFlow<AgeSettingsUiState> = _uiState.asStateFlow()
    
    fun loadProfile(profile: Profile) {
        _uiState.value = _uiState.value.copy(
            profile = profile,
            isKidsProfile = profile.effectiveKidsProfile,
            selectedAge = profile.age
        )
    }
    
    fun loadAgeRatings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val response = profileRepository.getAgeRatings()
                if (response.status && response.ageRatings != null) {
                    _uiState.value = _uiState.value.copy(
                        ageRatings = response.ageRatings,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = response.message,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load age ratings",
                    isLoading = false
                )
            }
        }
    }
    
    fun setKidsProfile(isKids: Boolean) {
        _uiState.value = _uiState.value.copy(isKidsProfile = isKids)
    }
    
    fun setAge(age: Int?) {
        _uiState.value = _uiState.value.copy(selectedAge = age)
    }
    
    fun canAccessRating(rating: AgeRating): Boolean {
        val state = _uiState.value
        
        // Kids profiles can only access content for ages 12 and under
        if (state.isKidsProfile) {
            return rating.isKidsFriendly
        }
        
        // If age is set, check if it meets the minimum requirement
        state.selectedAge?.let { age ->
            return age >= rating.minAge
        }
        
        // No age restriction if age not set
        return true
    }
    
    suspend fun saveAgeSettings() {
        val state = _uiState.value
        val profile = state.profile ?: return
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        try {
            val ageToSave = if (state.isKidsProfile) null else state.selectedAge
            val response = profileRepository.updateAgeSettings(
                profileId = profile.profileId,
                age = ageToSave,
                isKidsProfile = state.isKidsProfile
            )
            
            if (!response.status) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = response.message,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Failed to save age settings",
                isLoading = false
            )
        }
    }
}