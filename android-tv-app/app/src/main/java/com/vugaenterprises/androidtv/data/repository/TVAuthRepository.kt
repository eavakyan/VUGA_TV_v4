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
            val deviceId = getDeviceId()
            android.util.Log.d("TVAuthRepository", "Generating auth session with device ID: $deviceId")
            
            val request = TVAuthSessionRequest(tvDeviceId = deviceId)
            android.util.Log.d("TVAuthRepository", "Request: $request")
            
            val response = apiService.generateAuthSession(request)
            android.util.Log.d("TVAuthRepository", "Response received: status=${response.status}, message=${response.message}")
            
            if (response.status && response.data != null) {
                android.util.Log.d("TVAuthRepository", "Session data: ${response.data}")
                emit(Result.success(response.data))
            } else {
                android.util.Log.e("TVAuthRepository", "API returned error: ${response.message}")
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            android.util.Log.e("TVAuthRepository", "Exception in generateAuthSession: ${e.message}", e)
            android.util.Log.e("TVAuthRepository", "Full exception: $e")
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("TVAuthRepository", "HTTP Error body: $errorBody")
                android.util.Log.e("TVAuthRepository", "HTTP Status code: ${e.code()}")
            }
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
    
    // Note: TV app doesn't need to call completeAuth
    // The mobile app calls authenticate endpoint
    // TV app just polls checkAuthStatus until authenticated
}