package com.vugaenterprises.androidtv.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.EpisodeItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoPlayerScreen(
    content: Content?,
    episode: EpisodeItem? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Set fullscreen and landscape orientation
    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    
    // Clean up orientation when leaving
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }
    
    // Determine video source and title based on content or episode
    val videoSource = when {
        episode != null -> {
            // Use episode sources
            episode.sources.firstOrNull { it.source.isNotEmpty() }
        }
        content != null -> {
            // Use content sources
            content.contentSources.firstOrNull { it.source.isNotEmpty() }
        }
        else -> null
    }
    
    val contentTitle = when {
        episode != null -> "${content?.title ?: "Show"} - Episode ${episode.number}: ${episode.title}"
        content != null -> content.title
        else -> "Unknown Content"
    }
    
    if (videoSource != null) {
        VideoPlayer(
            videoUrl = videoSource.source,
            contentTitle = contentTitle,
            onNavigateBack = onNavigateBack
        )
    } else {
        // No video source available
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "No video available",
                    style = MaterialTheme.typography.headlineMedium
                )
                Button(onClick = onNavigateBack) {
                    Text("Go Back")
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(
    videoUrl: String,
    contentTitle: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    
    // State for timeline and seeking
    var showTimeline by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekDirection by remember { mutableStateOf(0) } // -1 for rewind, 1 for fast forward, 0 for none
    var speedLevel by remember { mutableStateOf(0) } // 0 = no speed, 1 = 1x, 2 = 2x, 3 = 3x
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            
            // Add listener to track position and duration
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        duration = duration
                    }
                }
            })
        }
    }
    
    // Update position and duration
    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration
            delay(100) // Update every 100ms
        }
    }
    
    // Continuous seeking effect
    LaunchedEffect(seekDirection) {
        if (seekDirection != 0) {
            isSeeking = true
            showTimeline = true
            
            while (seekDirection != 0) {
                val seekAmount = when (seekDirection) {
                    -1 -> -5000L * speedLevel // 5 seconds * speed level rewind
                    1 -> 5000L * speedLevel   // 5 seconds * speed level fast forward
                    else -> 0L
                }
                
                val newPosition = (exoPlayer.currentPosition + seekAmount).coerceIn(0, duration)
                exoPlayer.seekTo(newPosition)
                delay(200) // Seek every 200ms while key is held
            }
            
            // Hide timeline after a delay
            delay(2000)
            showTimeline = false
            isSeeking = false
        }
    }
    
    // Clean up player when leaving
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .onKeyEvent { keyEvent ->
                when (keyEvent.key) {
                                         Key.DirectionLeft -> {
                         when (keyEvent.type) {
                             KeyEventType.KeyDown -> {
                                 if (seekDirection == 0) {
                                     // Initial press - increase speed level and seek once
                                     speedLevel = when (speedLevel) {
                                         0 -> 1
                                         1 -> 2
                                         2 -> 3
                                         3 -> 1
                                         else -> 1
                                     }
                                     val seekAmount = 10000L * speedLevel
                                     val newPosition = (exoPlayer.currentPosition - seekAmount).coerceAtLeast(0)
                                     exoPlayer.seekTo(newPosition)
                                     showTimeline = true
                                     coroutineScope.launch {
                                         delay(2000)
                                         if (seekDirection == 0) {
                                             showTimeline = false
                                         }
                                     }
                                 }
                                 seekDirection = -1
                             }
                             KeyEventType.KeyUp -> {
                                 seekDirection = 0
                             }
                         }
                         true
                     }
                                         Key.DirectionRight -> {
                         when (keyEvent.type) {
                             KeyEventType.KeyDown -> {
                                 if (seekDirection == 0) {
                                     // Initial press - increase speed level and seek once
                                     speedLevel = when (speedLevel) {
                                         0 -> 1
                                         1 -> 2
                                         2 -> 3
                                         3 -> 1
                                         else -> 1
                                     }
                                     val seekAmount = 10000L * speedLevel
                                     val newPosition = exoPlayer.currentPosition + seekAmount
                                     exoPlayer.seekTo(newPosition)
                                     showTimeline = true
                                     coroutineScope.launch {
                                         delay(2000)
                                         if (seekDirection == 0) {
                                             showTimeline = false
                                         }
                                     }
                                 }
                                 seekDirection = 1
                             }
                             KeyEventType.KeyUp -> {
                                 seekDirection = 0
                             }
                         }
                         true
                     }
                    Key.Back -> {
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            onNavigateBack()
                        }
                        true
                    }
                    else -> false
                }
            }
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Back button overlay
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Text("Back")
        }
        
        // Title overlay
        Text(
            text = contentTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
        
        // Timeline overlay
        AnimatedVisibility(
            visible = showTimeline,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        ) {
                         TimelineOverlay(
                 currentPosition = currentPosition,
                 duration = duration,
                 isSeeking = isSeeking,
                 seekDirection = seekDirection,
                 speedLevel = speedLevel
             )
        }
    }
    
    // Request focus when the component is first displayed
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun TimelineOverlay(
    currentPosition: Long,
    duration: Long,
    isSeeking: Boolean,
    seekDirection: Int,
    speedLevel: Int
) {
    Card(
        modifier = Modifier
            .width(600.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                         // Seek direction indicator
             if (isSeeking) {
                 Text(
                     text = when (seekDirection) {
                         -1 -> "⏪ REWINDING ${speedLevel}x"
                         1 -> "⏩ FAST FORWARDING ${speedLevel}x"
                         else -> ""
                     },
                     style = MaterialTheme.typography.bodyMedium,
                     color = Color.White,
                     fontWeight = FontWeight.Bold
                 )
             }
            
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            ) {
                if (duration > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(currentPosition.toFloat() / duration.toFloat())
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                    )
                }
            }
            
            // Time display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    if (timeMs <= 0) return "0:00"
    
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        String.format("%d:%02d:%02d", hours, remainingMinutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
} 