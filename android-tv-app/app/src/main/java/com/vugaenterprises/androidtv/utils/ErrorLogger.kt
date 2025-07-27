package com.vugaenterprises.androidtv.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.vugaenterprises.androidtv.data.api.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ErrorLog(
    val timestamp: String,
    val errorCode: String,
    val errorMessage: String,
    val errorType: String,
    val deviceInfo: String,
    val appVersion: String,
    val stackTrace: String? = null
)

private val Context.errorDataStore: DataStore<Preferences> by preferencesDataStore(name = "error_logs")

@Singleton
class ErrorLogger @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService? = null
) {
    private val ERROR_LOGS_KEY = stringPreferencesKey("error_logs")
    private val LAST_SYNC_TIME_KEY = longPreferencesKey("last_error_sync_time")
    private val MAX_STORED_ERRORS = 100
    
    companion object {
        private const val TAG = "ErrorLogger"
        
        // Error codes
        const val ERROR_API_UNREACHABLE = "APIBKEND03"
        const val ERROR_API_TIMEOUT = "APIBKEND04"
        const val ERROR_API_CONNECTION = "APIBKEND05"
        const val ERROR_API_NOT_FOUND = "APIBKEND06"
        const val ERROR_API_SERVER = "APIBKEND07"
        const val ERROR_UNKNOWN = "APIBKEND99"
    }
    
    fun logError(
        error: Throwable,
        errorType: String = "API_ERROR",
        customMessage: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val errorCode = getErrorCode(error)
                val errorLog = ErrorLog(
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                    errorCode = errorCode,
                    errorMessage = customMessage ?: error.message ?: "Unknown error",
                    errorType = errorType,
                    deviceInfo = getDeviceInfo(),
                    appVersion = getAppVersion(),
                    stackTrace = error.stackTraceToString().take(1000) // Limit stack trace size
                )
                
                // Log to Android logcat
                Log.e(TAG, "Error logged: $errorCode - ${errorLog.errorMessage}", error)
                
                // Store locally
                storeErrorLog(errorLog)
                
                // Try to sync if we have connectivity
                trySyncErrors()
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log error", e)
            }
        }
    }
    
    private suspend fun storeErrorLog(errorLog: ErrorLog) {
        try {
            val currentLogs = getStoredErrors()
            val updatedLogs = (listOf(errorLog) + currentLogs).take(MAX_STORED_ERRORS)
            
            context.errorDataStore.edit { preferences ->
                preferences[ERROR_LOGS_KEY] = Json.encodeToString(updatedLogs)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store error log", e)
        }
    }
    
    private suspend fun getStoredErrors(): List<ErrorLog> {
        return try {
            val logsJson = context.errorDataStore.data
                .map { preferences -> preferences[ERROR_LOGS_KEY] ?: "[]" }
                .first()
            Json.decodeFromString(logsJson)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve stored errors", e)
            emptyList()
        }
    }
    
    suspend fun trySyncErrors() {
        try {
            // Check if we should sync (not more than once per hour)
            val lastSyncTime = context.errorDataStore.data
                .map { preferences -> preferences[LAST_SYNC_TIME_KEY] ?: 0 }
                .first()
            
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSyncTime < 3600000) { // 1 hour
                return
            }
            
            val errors = getStoredErrors()
            if (errors.isEmpty()) return
            
            // Try to send errors to backend
            apiService?.let { api ->
                try {
                    // You would create an endpoint for this
                    // api.reportErrors(errors)
                    
                    // For now, just log that we would sync
                    Log.d(TAG, "Would sync ${errors.size} errors to backend")
                    
                    // Clear synced errors
                    context.errorDataStore.edit { preferences ->
                        preferences[ERROR_LOGS_KEY] = "[]"
                        preferences[LAST_SYNC_TIME_KEY] = currentTime
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync errors", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in sync process", e)
        }
    }
    
    fun getErrorCode(error: Throwable): String {
        val message = error.message ?: ""
        return when {
            message.contains("Unable to resolve host") -> ERROR_API_UNREACHABLE
            message.contains("timeout", ignoreCase = true) -> ERROR_API_TIMEOUT
            message.contains("connection", ignoreCase = true) -> ERROR_API_CONNECTION
            message.contains("404") -> ERROR_API_NOT_FOUND
            message.contains("500") || message.contains("502") || message.contains("503") -> ERROR_API_SERVER
            else -> ERROR_UNKNOWN
        }
    }
    
    private fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    // Get errors for display in settings/debug screen
    suspend fun getErrorLogs(): List<ErrorLog> {
        return getStoredErrors()
    }
    
    // Clear all error logs
    suspend fun clearErrorLogs() {
        context.errorDataStore.edit { preferences ->
            preferences[ERROR_LOGS_KEY] = "[]"
        }
    }
}