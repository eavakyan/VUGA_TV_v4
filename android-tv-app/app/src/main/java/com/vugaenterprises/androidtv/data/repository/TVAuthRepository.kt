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
            android.util.Log.d("TVAuthRepository", "=== TV AUTH SESSION GENERATION ===")
            android.util.Log.d("TVAuthRepository", "Device ID: $deviceId")
            
            // First, test basic connectivity
            try {
                android.util.Log.d("TVAuthRepository", "Testing basic API connectivity...")
                val testResponse = apiService.testConnection()
                android.util.Log.d("TVAuthRepository", "Test connection response: status=${testResponse.status}, message=${testResponse.message}")
            } catch (testError: Exception) {
                android.util.Log.e("TVAuthRepository", "Basic connectivity test failed: ${testError.message}")
                if (testError is retrofit2.HttpException) {
                    android.util.Log.e("TVAuthRepository", "Test HTTP Status: ${testError.code()}")
                }
            }
            
            val request = TVAuthSessionRequest(tvDeviceId = deviceId)
            android.util.Log.d("TVAuthRepository", "Auth session request: $request")
            android.util.Log.d("TVAuthRepository", "Calling endpoint: tv-auth/generate-session")
            
            val response = apiService.generateAuthSession(request)
            android.util.Log.d("TVAuthRepository", "Auth response received: status=${response.status}, message=${response.message}")
            android.util.Log.d("TVAuthRepository", "Response data: ${response.data}")
            
            if (response.status && response.data != null) {
                android.util.Log.d("TVAuthRepository", "✅ Auth session generated successfully")
                android.util.Log.d("TVAuthRepository", "Session token: ${response.data.sessionToken}")
                android.util.Log.d("TVAuthRepository", "QR code: ${response.data.qrCode}")
                android.util.Log.d("TVAuthRepository", "Expires in: ${response.data.expiresInSeconds} seconds")
                emit(Result.success(response.data))
            } else {
                val errorMsg = response.message ?: "Unknown API error"
                android.util.Log.e("TVAuthRepository", "❌ API returned error: $errorMsg")
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            android.util.Log.e("TVAuthRepository", "❌ Exception in generateAuthSession: ${e.message}", e)
            android.util.Log.e("TVAuthRepository", "Exception type: ${e.javaClass.simpleName}")
            
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("TVAuthRepository", "HTTP Error body: $errorBody")
                android.util.Log.e("TVAuthRepository", "HTTP Status code: ${e.code()}")
                android.util.Log.e("TVAuthRepository", "HTTP Status message: ${e.message()}")
                
                // Create more specific error message based on HTTP status
                val specificError = when (e.code()) {
                    404 -> "TV authentication endpoint not found (404). Check if the API supports TV auth."
                    500 -> "Server error (500). The authentication service may be down."
                    403 -> "Forbidden (403). Check API key or authentication permissions."
                    400 -> "Bad request (400). Check request format."
                    else -> "HTTP Error ${e.code()}: ${e.message()}"
                }
                emit(Result.failure(Exception(specificError)))
            } else if (e is java.net.UnknownHostException) {
                android.util.Log.e("TVAuthRepository", "Network error: Cannot resolve host")
                emit(Result.failure(Exception("Cannot connect to VUGA servers. Check internet connection.")))
            } else if (e is java.net.SocketTimeoutException) {
                android.util.Log.e("TVAuthRepository", "Network timeout")
                emit(Result.failure(Exception("Connection timeout. Please try again.")))
            } else {
                emit(Result.failure(e))
            }
        }
    }
    
    // Check authentication status
    suspend fun checkAuthStatus(sessionToken: String): Flow<Result<TVAuthStatusData>> = flow {
        try {
            android.util.Log.d("TVAuthRepository", "Checking auth status for session: ${sessionToken.take(10)}...")
            
            val request = TVAuthStatusRequest(sessionToken = sessionToken)
            val response = apiService.checkAuthStatus(request)
            
            android.util.Log.d("TVAuthRepository", "Status check response: status=${response.status}, message=${response.message}")
            
            if (response.status && response.data != null) {
                android.util.Log.d("TVAuthRepository", "Status data: authenticated=${response.data.authenticated}")
                if (response.data.authenticated) {
                    android.util.Log.d("TVAuthRepository", "✅ Authentication successful! User: ${response.data.user?.fullname}")
                }
                emit(Result.success(response.data))
            } else {
                android.util.Log.w("TVAuthRepository", "Status check failed: ${response.message}")
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            android.util.Log.e("TVAuthRepository", "❌ Exception in checkAuthStatus: ${e.message}", e)
            
            if (e is retrofit2.HttpException) {
                android.util.Log.e("TVAuthRepository", "Status check HTTP error: ${e.code()}")
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("TVAuthRepository", "Status check error body: $errorBody")
            }
            
            emit(Result.failure(e))
        }
    }
    
    // Note: TV app doesn't need to call completeAuth
    // The mobile app calls authenticate endpoint
    // TV app just polls checkAuthStatus until authenticated
}