package com.vugaenterprises.androidtv.data.repository

import android.content.Context
import android.provider.Settings
import com.vugaenterprises.androidtv.data.api.ApiService
import com.vugaenterprises.androidtv.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TVAuthRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    
    // Get unique device ID for this TV
    fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_tv_device"
    }
    
    // Generate a new QR code session
    suspend fun generateAuthSession(): Flow<Result<TVAuthSessionData>> = flow {
        try {
            val request = TVAuthSessionRequest(tvDeviceId = getDeviceId())
            val response = apiService.generateAuthSession(request)
            
            if (response.status && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    // Check authentication status
    suspend fun checkAuthStatus(sessionToken: String): Flow<Result<TVAuthStatusData>> = flow {
        try {
            val request = TVAuthStatusRequest(sessionToken = sessionToken)
            val response = apiService.checkAuthStatus(request)
            
            if (response.status && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    // Complete authentication and get user data
    suspend fun completeAuth(sessionToken: String): Flow<Result<UserData>> = flow {
        try {
            val request = TVAuthCompleteRequest(
                sessionToken = sessionToken,
                tvDeviceId = getDeviceId()
            )
            val response = apiService.completeAuth(request)
            
            if (response.status && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}