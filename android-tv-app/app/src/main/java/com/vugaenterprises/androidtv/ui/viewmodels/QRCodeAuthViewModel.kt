package com.vugaenterprises.androidtv.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vugaenterprises.androidtv.data.model.TVAuthSessionData
import com.vugaenterprises.androidtv.data.model.TVAuthStatusData
import com.vugaenterprises.androidtv.data.model.UserData as ApiUserData
import com.vugaenterprises.androidtv.data.repository.TVAuthRepository
import com.vugaenterprises.androidtv.data.UserData
import com.vugaenterprises.androidtv.data.UserDataStore
import com.vugaenterprises.androidtv.utils.ErrorLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QRCodeAuthUiState(
    val isLoading: Boolean = true,
    val sessionData: TVAuthSessionData? = null,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val timeRemaining: Int = 300 // seconds
)

private val Context.dataStore by preferencesDataStore(name = "user_preferences")
private val USER_KEY = stringPreferencesKey("user_data")

@HiltViewModel
class QRCodeAuthViewModel @Inject constructor(
    private val tvAuthRepository: TVAuthRepository,
    val errorLogger: ErrorLogger,
    private val userDataStore: UserDataStore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QRCodeAuthUiState())
    val uiState: StateFlow<QRCodeAuthUiState> = _uiState.asStateFlow()
    
    private var currentSessionToken: String? = null
    private var pollingJob: kotlinx.coroutines.Job? = null
    private var countdownJob: kotlinx.coroutines.Job? = null
    
    init {
        generateNewSession()
    }
    
    fun generateNewSession() {
        // Cancel any existing polling
        pollingJob?.cancel()
        countdownJob?.cancel()
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            tvAuthRepository.generateAuthSession().collect { result ->
                result.fold(
                    onSuccess = { sessionData ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                sessionData = sessionData,
                                timeRemaining = sessionData.expiresInSeconds,
                                isAuthenticated = false
                            )
                        }
                        currentSessionToken = sessionData.sessionToken
                        startPolling()
                        startCountdown()
                    },
                    onFailure = { exception ->
                        errorLogger.logError(
                            error = exception,
                            errorType = "QR_AUTH_GENERATION",
                            customMessage = "Failed to generate QR authentication session"
                        )
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to generate QR code"
                            )
                        }
                    }
                )
            }
        }
    }
    
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (currentSessionToken != null) {
                delay(2000) // Poll every 2 seconds
                
                currentSessionToken?.let { token ->
                    tvAuthRepository.checkAuthStatus(token).collect { result ->
                        result.fold(
                            onSuccess = { statusData ->
                                if (statusData.authenticated) {
                                    // Authentication successful, save user data from status
                                    pollingJob?.cancel()
                                    countdownJob?.cancel()
                                    
                                    // The user data should be in the status response
                                    // For now, we need to fetch user profile separately
                                    // since the status endpoint might not return full user data
                                    completeAuthenticationFromStatus(statusData)
                                }
                            },
                            onFailure = { 
                                // Continue polling even on failure
                            }
                        )
                    }
                }
            }
        }
    }
    
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var timeLeft = _uiState.value.timeRemaining
            while (timeLeft > 0) {
                delay(1000) // Update every second
                timeLeft--
                _uiState.update { it.copy(timeRemaining = timeLeft) }
                
                if (timeLeft <= 0) {
                    // Session expired, generate new one
                    generateNewSession()
                }
            }
        }
    }
    
    // Removed - TV app doesn't call completeAuth, it gets user data from checkStatus
    
    private fun completeAuthenticationFromStatus(statusData: TVAuthStatusData) {
        viewModelScope.launch {
            try {
                // Get user data from status response
                statusData.user?.let { userData ->
                    // Save user data to UserDataStore
                    val appUserData = UserData(
                        id = userData.id,
                        fullname = userData.fullname,
                        email = userData.email,
                        token = null, // TV authentication doesn't return a token
                        profileImage = userData.profileImage,
                        isPremium = false // Default to false for TV auth
                    )
                    userDataStore.saveUserData(appUserData)
                    
                    _uiState.update { 
                        it.copy(
                            isAuthenticated = true,
                            isLoading = false
                        )
                    }
                } ?: run {
                    // No user data in response
                    throw Exception("No user data received from authentication")
                }
            } catch (e: Exception) {
                errorLogger.logError(
                    error = e,
                    errorType = "QR_AUTH_COMPLETE",
                    customMessage = "Failed to complete TV authentication: ${e.message}"
                )
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to complete authentication"
                    )
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        countdownJob?.cancel()
    }
}