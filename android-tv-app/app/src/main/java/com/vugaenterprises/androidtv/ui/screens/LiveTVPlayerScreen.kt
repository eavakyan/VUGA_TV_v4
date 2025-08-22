package com.vugaenterprises.androidtv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import com.vugaenterprises.androidtv.data.model.LiveChannel
import com.vugaenterprises.androidtv.ui.viewmodels.LiveTVViewModel
import kotlinx.coroutines.delay

/**
 * Full-screen Live TV player with TV-optimized controls
 */
@Composable
fun LiveTVPlayerScreen(
    channelId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LiveTVViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Player state
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isControlsVisible by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var watchStartTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Channel data
    val channel = viewModel.getChannelById(channelId)
    
    // Auto-hide controls
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(5000) // Hide after 5 seconds
            isControlsVisible = false
        }
    }
    
    // Initialize player
    LaunchedEffect(channel) {
        if (channel != null && exoPlayer == null) {
            try {
                // Create data source factory for HTTP streams
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                
                exoPlayer = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                    .build().apply {
                    
                    val mediaItem = createMediaItemForStream(channel.streamUrl, channel.streamType)
                    
                    val mediaSource = when {
                        // Detect DASH streams by URL
                        channel.streamUrl.contains(".mpd") -> {
                            DashMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(mediaItem)
                        }
                        // Detect HLS streams by URL
                        channel.streamUrl.contains(".m3u8") -> {
                            HlsMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(mediaItem)
                        }
                        // For other streams, let ExoPlayer auto-detect
                        else -> {
                            DefaultMediaSourceFactory(dataSourceFactory)
                                .createMediaSource(mediaItem)
                        }
                    }
                    
                    setMediaSource(mediaSource)
                    playWhenReady = true
                    prepare()
                    
                    addListener(object : androidx.media3.common.Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                androidx.media3.common.Player.STATE_BUFFERING -> {
                                    isLoading = true
                                    errorMessage = null
                                }
                                androidx.media3.common.Player.STATE_READY -> {
                                    isLoading = false
                                    isPlaying = true
                                    errorMessage = null
                                }
                                androidx.media3.common.Player.STATE_ENDED -> {
                                    isPlaying = false
                                    isLoading = false
                                }
                                androidx.media3.common.Player.STATE_IDLE -> {
                                    isLoading = false
                                }
                            }
                        }
                        
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            isLoading = false
                            isPlaying = false
                            errorMessage = "Failed to play live stream: ${error.message}"
                        }
                    })
                }
            } catch (e: Exception) {
                errorMessage = "Failed to initialize player: ${e.message}"
                isLoading = false
            }
        }
    }
    
    // Lifecycle management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer?.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer?.play()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    // Track watch time before releasing player
                    if (channel != null) {
                        val watchDuration = ((System.currentTimeMillis() - watchStartTime) / 1000).toInt()
                        viewModel.trackChannelView(channelId, watchDuration)
                    }
                    exoPlayer?.release()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer?.release()
        }
    }
    
    // Handle back navigation
    BackHandler {
        onNavigateBack()
    }
    
    // Key event handling for TV remote
    val keyEventHandler = { keyEvent: KeyEvent ->
        when {
            keyEvent.type == KeyEventType.KeyDown -> {
                when (keyEvent.key) {
                    Key.Back -> {
                        onNavigateBack()
                        true
                    }
                    Key.Enter, Key.DirectionCenter -> {
                        isControlsVisible = !isControlsVisible
                        true
                    }
                    Key.MediaPlay -> {
                        exoPlayer?.play()
                        isControlsVisible = true
                        true
                    }
                    Key.MediaPause -> {
                        exoPlayer?.pause()
                        isControlsVisible = true
                        true
                    }
                    Key.MediaPlayPause -> {
                        if (exoPlayer?.isPlaying == true) {
                            exoPlayer?.pause()
                        } else {
                            exoPlayer?.play()
                        }
                        isControlsVisible = true
                        true
                    }
                    Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight -> {
                        isControlsVisible = true
                        false // Let the system handle navigation
                    }
                    else -> false
                }
            }
            else -> false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onKeyEvent(keyEventHandler)
    ) {
        // Video player
        if (exoPlayer != null && channel != null && errorMessage == null) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        useController = false // We'll handle controls ourselves
                        setKeepContentOnPlayerReset(true)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Loading indicator
        if (isLoading) {
            LoadingOverlay(
                channel = channel,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Error screen
        if (errorMessage != null) {
            ErrorOverlay(
                errorMessage = errorMessage!!,
                channel = channel,
                onRetry = {
                    errorMessage = null
                    isLoading = true
                    exoPlayer?.seekToDefaultPosition()
                    exoPlayer?.prepare()
                },
                onBack = onNavigateBack,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Custom controls overlay
        AnimatedVisibility(
            visible = isControlsVisible && errorMessage == null,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            LiveTVControls(
                channel = channel,
                isPlaying = isPlaying,
                onPlayPause = {
                    if (exoPlayer?.isPlaying == true) {
                        exoPlayer?.pause()
                    } else {
                        exoPlayer?.play()
                    }
                },
                onBack = onNavigateBack,
                modifier = Modifier.padding(32.dp)
            )
        }
        
        // Channel info overlay (top)
        AnimatedVisibility(
            visible = isControlsVisible && errorMessage == null && channel != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            ChannelInfoOverlay(
                channel = channel!!,
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}

/**
 * Loading overlay with channel info
 */
@Composable
private fun LoadingOverlay(
    channel: LiveChannel?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFFE50914),
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
            if (channel != null) {
                Text(
                    text = "Loading ${channel.name}...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                if (channel.hasProgramInfo) {
                    Text(
                        text = channel.programInfo,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "Loading channel...",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}

/**
 * Error overlay with retry option
 */
@Composable
private fun ErrorOverlay(
    errorMessage: String,
    channel: LiveChannel?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Playback Error",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (channel != null) {
                Text(
                    text = channel.name,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp
                )
            }
            
            Text(
                text = errorMessage,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Back to Channels")
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE50914)
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

/**
 * Custom TV controls overlay
 */
@Composable
private fun LiveTVControls(
    channel: LiveChannel?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel info
            if (channel != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = channel.displayName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (channel.hasProgramInfo) {
                        Text(
                            text = channel.programInfo,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause button
                Button(
                    onClick = onPlayPause,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE50914)
                    ),
                    modifier = Modifier.focusable()
                ) {
                    Text(if (isPlaying) "Pause" else "Play")
                }
                
                // Back button
                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    modifier = Modifier.focusable()
                ) {
                    Text("Back")
                }
            }
        }
    }
}

/**
 * Channel info overlay at top
 */
@Composable
private fun ChannelInfoOverlay(
    channel: LiveChannel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(bottomEnd = 16.dp),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live indicator
            if (channel.isLive) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Red
                ) {
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            // Channel info
            Column {
                Text(
                    text = channel.displayName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (channel.hasProgramInfo) {
                    Text(
                        text = channel.programInfo,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Create a MediaItem for the given stream URL and type
 */
private fun createMediaItemForStream(streamUrl: String, streamType: Int): MediaItem {
    return when {
        // DASH streams
        streamUrl.contains(".mpd") -> {
            MediaItem.Builder()
                .setUri(streamUrl)
                .setMimeType(MimeTypes.APPLICATION_MPD)
                .build()
        }
        // HLS streams  
        streamUrl.contains(".m3u8") -> {
            MediaItem.Builder()
                .setUri(streamUrl)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()
        }
        // Based on stream type from API
        streamType == 1 -> {
            // RTMP or other type 1
            MediaItem.Builder()
                .setUri(streamUrl)
                .setMimeType(MimeTypes.VIDEO_UNKNOWN)
                .build()
        }
        else -> {
            // Let ExoPlayer auto-detect
            MediaItem.fromUri(streamUrl)
        }
    }
}