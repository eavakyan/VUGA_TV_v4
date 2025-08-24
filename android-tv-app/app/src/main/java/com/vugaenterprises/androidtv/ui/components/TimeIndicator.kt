package com.vugaenterprises.androidtv.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Time indicator component showing current date and time
 * Updates automatically every second for smooth time display
 */
@Composable
fun TimeIndicator(
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
    showSeconds: Boolean = false,
    textColor: Color = Color.White,
    backgroundColor: Color = Color.Transparent,
    compact: Boolean = false
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Update time every second for smooth display
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(if (showSeconds) 1000L else 60000L) // 1 second if showing seconds, 1 minute otherwise
        }
    }
    
    val timeFormatter = remember {
        SimpleDateFormat(
            if (showSeconds) "h:mm:ss a" else "h:mm a",
            Locale.getDefault()
        )
    }
    
    val dateFormatter = remember {
        SimpleDateFormat(
            if (compact) "MMM dd" else "EEEE, MMMM dd",
            Locale.getDefault()
        )
    }
    
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = if (backgroundColor != Color.Transparent) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                if (backgroundColor != Color.Transparent) 12.dp else 0.dp
            ),
            horizontalAlignment = if (compact) Alignment.Start else Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)
        ) {
            // Current time
            Text(
                text = timeFormatter.format(Date(currentTime)),
                color = textColor,
                fontSize = if (compact) 16.sp else 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Current date
            if (showDate) {
                Text(
                    text = dateFormatter.format(Date(currentTime)),
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = if (compact) 12.sp else 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Header time indicator for TV screens
 * Shows in top-right corner with semi-transparent background
 */
@Composable
fun HeaderTimeIndicator(
    modifier: Modifier = Modifier
) {
    TimeIndicator(
        modifier = modifier,
        showDate = false,
        showSeconds = false,
        textColor = Color.White,
        backgroundColor = Color.Black.copy(alpha = 0.6f),
        compact = true
    )
}

/**
 * Program schedule time display
 * Shows time range for programs with progress indicator
 */
@Composable
fun ProgramTimeDisplay(
    startTime: String,
    endTime: String,
    currentTime: String = "",
    progressPercent: Float = 0f,
    modifier: Modifier = Modifier,
    showProgress: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = startTime,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (currentTime.isNotEmpty()) {
                Text(
                    text = currentTime,
                    color = Color(0xFFE50914),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = endTime,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Progress bar
        if (showProgress && progressPercent > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(
                            color = Color(0xFFE50914),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

/**
 * Countdown timer for program ending
 */
@Composable
fun ProgramCountdown(
    timeRemaining: String,
    isEndingSoon: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pulseAnimation = remember { Animatable(1f) }
    
    // Pulse animation when ending soon
    LaunchedEffect(isEndingSoon) {
        if (isEndingSoon) {
            pulseAnimation.animateTo(
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseAnimation.snapTo(1f)
        }
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isEndingSoon) Color(0xFFFF6B00).copy(alpha = pulseAnimation.value) else Color(0xFF2A2A2A)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Clock icon indicator
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = if (isEndingSoon) Color.White else Color(0xFFE50914),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
            
            Text(
                text = timeRemaining,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Full-screen time display for screensaver mode
 */
@Composable
fun FullScreenTimeDisplay(
    modifier: Modifier = Modifier,
    onTimeClick: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TimeIndicator(
            showDate = true,
            showSeconds = true,
            textColor = Color.White,
            backgroundColor = Color.Black.copy(alpha = 0.7f)
        )
    }
}