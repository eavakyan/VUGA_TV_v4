package com.vugaenterprises.androidtv.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val TAG = "SplashScreen"
private const val SPLASH_DURATION = 3000L // 3 seconds total

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Set landscape orientation for splash
    LaunchedEffect(Unit) {
        Log.d(TAG, "SplashScreen started")
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // Auto-advance after splash duration
        delay(SPLASH_DURATION)
        Log.d(TAG, "Splash completed, advancing to main app")
        onSplashComplete()
    }
    
    // Clean up orientation when leaving
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }
    
    // Trigger animations
    LaunchedEffect(Unit) {
        delay(200) // Small delay before starting animations
        showContent = true
        delay(500)
        showSubtitle = true
        delay(300)
        showProgress = true
    }
    
    // Fade-in animation for main content
    val fadeInAnimation by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fadeIn"
    )
    
    // Scale animation for main title
    val scaleAnimation by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.8f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutBack),
        label = "scale"
    )
    
    // Fade-in animation for subtitle
    val subtitleAnimation by animateFloatAsState(
        targetValue = if (showSubtitle) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "subtitle"
    )
    
    // Progress animation
    val progressAnimation by animateFloatAsState(
        targetValue = if (showProgress) 1f else 0f,
        animationSpec = tween(durationMillis = 2000),
        label = "progress"
    )
    
    // Subtle background animation
    val backgroundAnimation by rememberInfiniteTransition(label = "background").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(
                    red = 0.05f + backgroundAnimation * 0.02f,
                    green = 0.05f + backgroundAnimation * 0.02f,
                    blue = 0.05f + backgroundAnimation * 0.02f
                )
            )
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(fadeInAnimation),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main title with scale animation
            Text(
                text = "VUGA TV",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.scale(scaleAnimation)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Subtitle with fade animation
            Text(
                text = "Entertainment Redefined",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light
                ),
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.alpha(subtitleAnimation)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Progress indicator
            LinearProgressIndicator(
                progress = { progressAnimation },
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .alpha(if (showProgress) 1f else 0f)
            )
        }
        
        // Skip button
        Button(
            onClick = {
                Log.d(TAG, "Skip button pressed")
                onSplashComplete()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Text("Skip", color = Color.White)
        }
    }
} 