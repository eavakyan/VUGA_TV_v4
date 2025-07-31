package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject

@HiltViewModel
class TVAuthDebugViewModel @Inject constructor() : ViewModel() {
    
    private val _debugInfo = MutableStateFlow<String>("Starting debug...")
    val debugInfo: StateFlow<String> = _debugInfo
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    fun testTVAuth() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val result = withContext(Dispatchers.IO) {
                    val logs = mutableListOf<String>()
                    
                    val client = OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor { message ->
                            logs.add(message)
                        }.apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                        .build()
                    
                    // Test direct HTTP request
                    val json = """{"tv_device_id":"test_device_123"}"""
                    val body = json.toRequestBody("application/json".toMediaType())
                    
                    val debugInfo = StringBuilder()
                    
                    // First test the test endpoint
                    val testRequest = Request.Builder()
                        .url("https://iosdev.gossip-stone.com/api/v2/tv-auth/test")
                        .addHeader("apikey", "jpwc3pny")
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build()
                    
                    debugInfo.append("=== TEST ENDPOINT ===\n")
                    try {
                        val testResponse = client.newCall(testRequest).execute()
                        debugInfo.append("Test Response Code: ${testResponse.code}\n")
                        debugInfo.append("Test Response Body: ${testResponse.body?.string()}\n\n")
                    } catch (e: Exception) {
                        debugInfo.append("Test endpoint error: ${e.message}\n\n")
                    }
                    
                    // Now test the real endpoint
                    val request = Request.Builder()
                        .url("https://iosdev.gossip-stone.com/api/v2/tv-auth/generate-session")
                        .addHeader("apikey", "jpwc3pny")
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build()
                    debugInfo.append("=== SENDING REQUEST ===\n")
                    debugInfo.append("URL: ${request.url}\n")
                    debugInfo.append("Headers: ${request.headers}\n")
                    debugInfo.append("Body: $json\n")
                    
                    try {
                        val response = client.newCall(request).execute()
                        
                        debugInfo.append("\n=== RESPONSE ===\n")
                        debugInfo.append("Code: ${response.code}\n")
                        debugInfo.append("Message: ${response.message}\n")
                        debugInfo.append("Headers: ${response.headers}\n")
                        
                        val responseBody = response.body?.string() ?: "No body"
                        debugInfo.append("Body: $responseBody\n")
                        
                        // Try to parse error message if it's a 500 error
                        if (response.code == 500) {
                            debugInfo.append("\n=== SERVER ERROR DETAILS ===\n")
                            debugInfo.append("This is a server-side error. The body should contain error details.\n")
                        }
                        
                        debugInfo.append("\n=== HTTP LOGS ===\n")
                        logs.forEach { log ->
                            debugInfo.append("$log\n")
                        }
                        
                    } catch (e: Exception) {
                        debugInfo.append("\n=== ERROR DURING REQUEST ===\n")
                        debugInfo.append("Error: ${e.message}\n")
                        debugInfo.append("Type: ${e::class.java.simpleName}\n")
                        debugInfo.append("Stack trace:\n${e.stackTraceToString()}\n")
                    }
                    
                    debugInfo.toString()
                }
                
                _debugInfo.value = result
                
            } catch (e: Exception) {
                _debugInfo.value = "Fatal error: ${e.message}\n\nStack trace:\n${e.stackTraceToString()}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@Composable
fun TVAuthDebugScreen(
    onBack: () -> Unit = {},
    viewModel: TVAuthDebugViewModel = hiltViewModel()
) {
    val debugInfo by viewModel.debugInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.testTVAuth()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(40.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TV Auth Debug",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Text(
                        text = debugInfo,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Button(
                    onClick = { viewModel.testTVAuth() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green,
                        contentColor = Color.White
                    )
                ) {
                    Text("Retry")
                }
                
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("Back")
                }
            }
        }
    }
}