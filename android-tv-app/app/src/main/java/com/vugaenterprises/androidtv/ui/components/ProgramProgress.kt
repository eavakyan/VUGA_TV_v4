package com.vugaenterprises.androidtv.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Program progress component showing time remaining and progress
 */
@Composable
fun ProgramProgress(
    programTitle: String,
    startTime: String,
    endTime: String,
    progressPercent: Float,
    timeRemaining: String,
    isEndingSoon: Boolean = false,
    modifier: Modifier = Modifier,
    showProgressBar: Boolean = true,
    showCircularProgress: Boolean = false
) {
    val pulseAnimation = remember { Animatable(1f) }
    
    // Pulse animation when ending soon
    LaunchedEffect(isEndingSoon) {
        if (isEndingSoon) {
            while (true) {
                pulseAnimation.animateTo(0.7f, animationSpec = tween(800))
                pulseAnimation.animateTo(1f, animationSpec = tween(800))
            }
        } else {
            pulseAnimation.snapTo(1f)
        }
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Program title
            Text(
                text = programTitle,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            
            // Time information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$startTime - $endTime",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isEndingSoon) {
                        Color(0xFFFF6B00).copy(alpha = pulseAnimation.value)
                    } else {
                        Color(0xFFE50914)
                    }
                ) {
                    Text(
                        text = timeRemaining,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            
            // Progress visualization
            if (showCircularProgress) {
                CircularProgramProgress(
                    progressPercent = progressPercent,
                    isEndingSoon = isEndingSoon,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else if (showProgressBar) {
                LinearProgramProgress(
                    progressPercent = progressPercent,
                    isEndingSoon = isEndingSoon,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Linear progress bar for program progress
 */
@Composable
fun LinearProgramProgress(
    progressPercent: Float,
    isEndingSoon: Boolean = false,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "programProgress"
    )
    
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(Color.White.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(
                    if (isEndingSoon) Color(0xFFFF6B00) else Color(0xFFE50914)
                )
        )
    }
}

/**
 * Circular progress indicator for program progress
 */
@Composable
fun CircularProgramProgress(
    progressPercent: Float,
    isEndingSoon: Boolean = false,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "circularProgress"
    )
    
    val progressColor = if (isEndingSoon) Color(0xFFFF6B00) else Color(0xFFE50914)
    val backgroundColor = Color.White.copy(alpha = 0.3f)
    
    Canvas(
        modifier = modifier
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val canvasSize = minOf(canvasWidth, canvasHeight)
        val radius = (canvasSize / 2f) - strokeWidth.toPx()
        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
        
        // Background circle
        drawCircle(
            color = backgroundColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        
        // Progress arc
        if (animatedProgress > 0f) {
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Compact program progress indicator for channel cards
 */
@Composable
fun CompactProgramProgress(
    timeRemaining: String,
    progressPercent: Float,
    isEndingSoon: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pulseAnimation = remember { Animatable(1f) }
    
    LaunchedEffect(isEndingSoon) {
        if (isEndingSoon) {
            while (true) {
                pulseAnimation.animateTo(0.6f, animationSpec = tween(600))
                pulseAnimation.animateTo(1f, animationSpec = tween(600))
            }
        } else {
            pulseAnimation.snapTo(1f)
        }
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Time remaining
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isEndingSoon) {
                Color(0xFFFF6B00).copy(alpha = pulseAnimation.value)
            } else {
                Color.Black.copy(alpha = 0.7f)
            }
        ) {
            Text(
                text = timeRemaining,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        
        // Mini progress bar
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color.White.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressPercent.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (isEndingSoon) Color(0xFFFF6B00) else Color(0xFFE50914)
                    )
            )
        }
    }
}

/**
 * Live indicator with pulse animation
 */
@Composable
fun LiveIndicatorWithProgress(
    isLive: Boolean = true,
    isEndingSoon: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pulseAnimation = remember { Animatable(1f) }
    
    LaunchedEffect(isLive, isEndingSoon) {
        if (isLive) {
            while (true) {
                pulseAnimation.animateTo(0.5f, animationSpec = tween(1000))
                pulseAnimation.animateTo(1f, animationSpec = tween(1000))
            }
        } else {
            pulseAnimation.snapTo(0.7f)
        }
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = if (isEndingSoon) {
            Color(0xFFFF6B00).copy(alpha = pulseAnimation.value)
        } else if (isLive) {
            Color.Red.copy(alpha = pulseAnimation.value)
        } else {
            Color.Gray.copy(alpha = 0.7f)
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Live dot
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
            )
            
            Text(
                text = if (isLive) "LIVE" else "OFF AIR",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Program grid progress overlay
 */
@Composable
fun ProgramGridProgress(
    programs: List<String>,
    currentProgramIndex: Int,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(programs.size) { index ->
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index <= currentProgramIndex) {
                            Color(0xFFE50914)
                        } else {
                            Color.White.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
}