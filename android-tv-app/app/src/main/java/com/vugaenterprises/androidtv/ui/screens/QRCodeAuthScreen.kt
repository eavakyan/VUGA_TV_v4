package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.vugaenterprises.androidtv.ui.viewmodels.QRCodeAuthViewModel
import com.vugaenterprises.androidtv.utils.QRCodeGenerator
import com.vugaenterprises.androidtv.utils.ErrorReporter
import kotlinx.coroutines.launch

@Composable
fun QRCodeAuthScreen(
    onAuthenticationSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: QRCodeAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navigate on successful authentication
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthenticationSuccess()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFE50914) // Netflix Red
                )
            }
            
            uiState.error != null -> {
                val context = LocalContext.current
                val errorReporter = remember { ErrorReporter(context, viewModel.errorLogger) }
                val scope = rememberCoroutineScope()
                
                // Show debug screen on error for APIBKEND07
                if (getErrorCode(uiState.error ?: "") == "APIBKEND07") {
                    TVAuthDebugScreen(
                        onBack = { viewModel.generateNewSession() }
                    )
                } else {
                    ErrorContent(
                        error = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.generateNewSession() },
                        onBack = onNavigateBack,
                        onSendReport = {
                            scope.launch {
                                errorReporter.sendErrorReport(getErrorCode(uiState.error ?: ""))
                            }
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            
            uiState.sessionData != null -> {
                QRCodeContent(
                    qrCodeData = uiState.sessionData!!.qrCode,
                    timeRemaining = uiState.timeRemaining,
                    onRefresh = { viewModel.generateNewSession() },
                    onBack = onNavigateBack,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun QRCodeContent(
    qrCodeData: String,
    timeRemaining: Int,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.6f)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Scan to Sign In",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        // QR Code Container
        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            val qrBitmap = remember(qrCodeData) {
                QRCodeGenerator.generateQRCode(qrCodeData, 250)
            }
            
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        // Instructions
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "1. Open the VUGA app on your phone",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "2. Tap the TV icon or scan button",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "3. Point your camera at this QR code",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        
        // Timer
        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        Text(
            text = "Code expires in: ${String.format("%d:%02d", minutes, seconds)}",
            fontSize = 16.sp,
            color = if (timeRemaining < 60) Color.Red else Color.White.copy(alpha = 0.6f)
        )
        
        // Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
                )
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE50914) // Netflix Red
                )
            ) {
                Text("Generate New Code")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onSendReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .background(
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Error Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Color(0xFFE50914).copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE50914)
            )
        }
        
        Text(
            text = "Service Temporarily Unavailable",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "We're unable to connect to VUGA services right now. Our team has been notified and is working to resolve this issue.",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        // Error code for support
        Text(
            text = "Error Code: ${getErrorCode(error)}",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
                )
            ) {
                Text("Back", fontSize = 16.sp)
            }
            
            Button(
                onClick = onRetry,
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE50914) // Netflix Red
                )
            ) {
                Text("Try Again", fontSize = 16.sp)
            }
            
            TextButton(
                onClick = onSendReport,
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text("Report Issue", fontSize = 14.sp)
            }
        }
    }
}

private fun getErrorCode(error: String): String {
    return when {
        error.contains("Unable to resolve host") -> "APIBKEND03"
        error.contains("timeout", ignoreCase = true) -> "APIBKEND04"
        error.contains("connection", ignoreCase = true) -> "APIBKEND05"
        error.contains("404") -> "APIBKEND06"
        error.contains("500") -> "APIBKEND07"
        else -> "APIBKEND99"
    }
}