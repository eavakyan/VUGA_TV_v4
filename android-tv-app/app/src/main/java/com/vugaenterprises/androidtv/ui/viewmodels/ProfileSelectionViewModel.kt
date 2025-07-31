package com.vugaenterprises.androidtv.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.model.Profile
import com.vugaenterprises.androidtv.data.repository.ProfileRepository
import com.vugaenterprises.androidtv.utils.ErrorLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileSelectionUiState(
    val isLoading: Boolean = true,
    val profiles: List<Profile> = emptyList(),
    val selectedProfile: Profile? = null,
    val isEditMode: Boolean = false,
    val error: String? = null,
    val isSelectionComplete: Boolean = false
)

@HiltViewModel
class ProfileSelectionViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val errorLogger: ErrorLogger
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileSelectionUiState())
    val uiState: StateFlow<ProfileSelectionUiState> = _uiState.asStateFlow()
    
    init {
        loadProfiles()
    }
    
    fun loadProfiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            profileRepository.getUserProfiles().collect { result ->
                result.fold(
                    onSuccess = { profiles ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                profiles = profiles,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        errorLogger.logError(
                            error = exception,
                            errorType = "PROFILE_LOAD",
                            customMessage = "Failed to load user profiles"
                        )
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load profiles"
                            )
                        }
                    }
                )
            }
        }
    }
    
    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            profileRepository.selectProfile(profile).collect { result ->
                result.fold(
                    onSuccess = { selectedProfile ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                selectedProfile = selectedProfile,
                                isSelectionComplete = true,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        errorLogger.logError(
                            error = exception,
                            errorType = "PROFILE_SELECT",
                            customMessage = "Failed to select profile"
                        )
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to select profile"
                            )
                        }
                    }
                )
            }
        }
    }
    
    fun createProfile(name: String, avatarId: Int, isKids: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            profileRepository.createProfile(name, avatarId, isKids).collect { result ->
                result.fold(
                    onSuccess = { newProfile ->
                        // Reload profiles after creation
                        loadProfiles()
                    },
                    onFailure = { exception ->
                        errorLogger.logError(
                            error = exception,
                            errorType = "PROFILE_CREATE",
                            customMessage = "Failed to create profile"
                        )
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to create profile"
                            )
                        }
                    }
                )
            }
        }
    }
    
    fun deleteProfile(profile: Profile) {
        // Don't allow deletion if it's the only profile
        if (_uiState.value.profiles.size <= 1) {
            _uiState.update { 
                it.copy(error = "Cannot delete the only profile")
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            profileRepository.deleteProfile(profile.profileId).collect { result ->
                result.fold(
                    onSuccess = {
                        // Reload profiles after deletion
                        loadProfiles()
                    },
                    onFailure = { exception ->
                        errorLogger.logError(
                            error = exception,
                            errorType = "PROFILE_DELETE",
                            customMessage = "Failed to delete profile"
                        )
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to delete profile"
                            )
                        }
                    }
                )
            }
        }
    }
    
    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}