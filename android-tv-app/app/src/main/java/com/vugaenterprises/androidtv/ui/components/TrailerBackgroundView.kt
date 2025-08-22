package com.vugaenterprises.androidtv.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import coil.load
import com.vugaenterprises.androidtv.R
import kotlinx.coroutines.*

/**
 * Custom component for playing trailer videos as background with fallback to poster image.
 * Optimized for Android TV performance with automatic video lifecycle management.
 */
class TrailerBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val videoView: VideoView
    private val backgroundImage: ImageView
    private val gradientOverlay: View
    
    private var trailerUrl: String = ""
    private var posterUrl: String = ""
    private var isVideoPlaying = false
    private var isVideoLoaded = false
    private var shouldAutoPlay = false
    
    // Coroutine scope for managing video operations
    private val viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private const val TAG = "TrailerBackgroundView"
        private const val VIDEO_LOAD_TIMEOUT = 10000L // 10 seconds
    }

    init {
        // Inflate the layout
        inflate(context, R.layout.trailer_background_layout, this)
        
        videoView = findViewById(R.id.backgroundVideoView)
        backgroundImage = findViewById(R.id.backgroundImage)
        gradientOverlay = findViewById(R.id.gradientOverlay)
        
        setupVideoView()
    }
    
    private fun setupVideoView() {
        // Configure VideoView for background playback
        videoView.setOnPreparedListener { mediaPlayer ->
            Log.d(TAG, "Video prepared successfully for URL: '$trailerUrl'")
            isVideoLoaded = true
            
            try {
                // Configure for background playback - muted, looping
                mediaPlayer.isLooping = true
                mediaPlayer.setVolume(0f, 0f) // Muted by default
                
                // Set video scaling to fill the view
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                
                // Log video dimensions for debugging
                val videoWidth = mediaPlayer.videoWidth
                val videoHeight = mediaPlayer.videoHeight
                Log.d(TAG, "Video dimensions: ${videoWidth}x${videoHeight}")
                
                // Show video and hide fallback image
                showVideo()
                
                if (shouldAutoPlay) {
                    Log.d(TAG, "Auto-playing video")
                    startVideo()
                } else {
                    Log.d(TAG, "Video prepared but not auto-playing")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error configuring media player", e)
                showFallbackImage()
            }
        }
        
        videoView.setOnErrorListener { _, what, extra ->
            val errorMessage = when (what) {
                MediaPlayer.MEDIA_ERROR_UNKNOWN -> "Unknown media error"
                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "Media server died"
                else -> "Media error"
            }
            val extraMessage = when (extra) {
                MediaPlayer.MEDIA_ERROR_IO -> "IO error"
                MediaPlayer.MEDIA_ERROR_MALFORMED -> "Malformed media"
                MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "Unsupported media"
                MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "Media timeout"
                else -> "Unknown extra error"
            }
            Log.e(TAG, "Video playback error for URL '$trailerUrl': $errorMessage ($what), $extraMessage ($extra)")
            isVideoLoaded = false
            isVideoPlaying = false
            
            // Show fallback image on error
            showFallbackImage()
            true
        }
        
        videoView.setOnCompletionListener {
            Log.d(TAG, "Video playback completed - restarting for loop")
            if (isVideoLoaded && shouldAutoPlay) {
                startVideo()
            }
        }
        
        videoView.setOnInfoListener { _, what, extra ->
            when (what) {
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    Log.d(TAG, "Video rendering started")
                    isVideoPlaying = true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    Log.d(TAG, "Video buffering started")
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    Log.d(TAG, "Video buffering ended")
                }
            }
            false
        }
    }
    
    /**
     * Set the content for background display
     * @param trailerUrl URL of the trailer video (YouTube, direct video, etc.)
     * @param posterUrl URL of the poster image (fallback)
     * @param autoPlay Whether to start playing immediately when ready
     */
    fun setContent(trailerUrl: String, posterUrl: String, autoPlay: Boolean = true) {
        // Reset state first
        stopVideo()
        
        this.trailerUrl = trailerUrl.trim()
        this.posterUrl = posterUrl.trim()
        this.shouldAutoPlay = autoPlay
        
        Log.d(TAG, "Setting content - trailer: '$trailerUrl', poster: '$posterUrl', autoPlay: $autoPlay")
        
        // Always load the poster image first as fallback
        loadPosterImage()
        
        // Load video if URL is available and valid
        if (trailerUrl.isNotEmpty() && isValidVideoUrl(trailerUrl)) {
            Log.d(TAG, "Valid video URL detected, loading video")
            loadVideo()
        } else {
            if (trailerUrl.isEmpty()) {
                Log.d(TAG, "No trailer URL provided, showing fallback image")
            } else {
                Log.w(TAG, "Invalid video URL format: '$trailerUrl', showing fallback image")
            }
            showFallbackImage()
        }
    }
    
    private fun loadPosterImage() {
        backgroundImage.load(posterUrl) {
            crossfade(300)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
        }
    }
    
    private fun loadVideo() {
        viewScope.launch {
            try {
                Log.d(TAG, "Loading video: $trailerUrl")
                
                // Set timeout for video loading
                withTimeout(VIDEO_LOAD_TIMEOUT) {
                    withContext(Dispatchers.Main) {
                        if (isYouTubeUrl(trailerUrl)) {
                            // For YouTube URLs, we might need special handling
                            // For now, show fallback image as YouTube requires special integration
                            Log.w(TAG, "YouTube URL detected - showing fallback image: $trailerUrl")
                            showFallbackImage()
                        } else {
                            // Direct video URL
                            Log.d(TAG, "Setting direct video URI: $trailerUrl")
                            val uri = Uri.parse(trailerUrl)
                            videoView.setVideoURI(uri)
                            
                            // Set media controller for debugging (optional)
                            // videoView.setMediaController(MediaController(context))
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Video loading timeout for URL: $trailerUrl", e)
                showFallbackImage()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading video URL: $trailerUrl", e)
                showFallbackImage()
            }
        }
    }
    
    private fun isValidVideoUrl(url: String): Boolean {
        if (url.isEmpty()) return false
        
        val validFormats = listOf(
            ".mp4", ".mov", ".avi", ".mkv", ".webm", ".3gp",
            ".m3u8", ".ts", ".flv"
        )
        
        val validDomains = listOf(
            "youtube.com", "youtu.be", "vimeo.com", "dailymotion.com"
        )
        
        val hasValidExtension = validFormats.any { url.contains(it, ignoreCase = true) }
        val hasValidDomain = validDomains.any { url.contains(it, ignoreCase = true) }
        val isHttpUrl = url.startsWith("http", ignoreCase = true)
        
        val isValid = isHttpUrl && (hasValidExtension || hasValidDomain)
        
        Log.d(TAG, "URL validation - '$url': isHttpUrl=$isHttpUrl, hasValidExtension=$hasValidExtension, hasValidDomain=$hasValidDomain, isValid=$isValid")
        
        return isValid
    }
    
    private fun isYouTubeUrl(url: String): Boolean {
        return url.contains("youtube.com", ignoreCase = true) || 
               url.contains("youtu.be", ignoreCase = true)
    }
    
    private fun showVideo() {
        Log.d(TAG, "Showing video view")
        videoView.visibility = VISIBLE
        backgroundImage.visibility = GONE
    }
    
    private fun showFallbackImage() {
        Log.d(TAG, "Showing fallback image")
        videoView.visibility = GONE
        backgroundImage.visibility = VISIBLE
        isVideoPlaying = false
        isVideoLoaded = false
    }
    
    /**
     * Start video playback
     */
    fun startVideo() {
        if (isVideoLoaded && !isVideoPlaying) {
            try {
                Log.d(TAG, "Starting video playback for URL: '$trailerUrl'")
                if (videoView.canSeekBackward() || videoView.canSeekForward()) {
                    videoView.start()
                    isVideoPlaying = true
                    Log.d(TAG, "Video started successfully")
                } else {
                    Log.w(TAG, "Video cannot seek - may not be ready")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting video playback", e)
                showFallbackImage()
            }
        } else {
            Log.d(TAG, "Cannot start video - isVideoLoaded: $isVideoLoaded, isVideoPlaying: $isVideoPlaying")
        }
    }
    
    /**
     * Pause video playback
     */
    fun pauseVideo() {
        if (isVideoPlaying) {
            Log.d(TAG, "Pausing video playback")
            videoView.pause()
            isVideoPlaying = false
        }
    }
    
    /**
     * Stop video playback and show fallback
     */
    fun stopVideo() {
        Log.d(TAG, "Stopping video playback")
        if (videoView.isPlaying) {
            videoView.stopPlayback()
        }
        isVideoPlaying = false
        isVideoLoaded = false
        showFallbackImage()
    }
    
    /**
     * Check if video is currently playing
     */
    fun isPlaying(): Boolean = isVideoPlaying
    
    /**
     * Check if video is loaded and ready
     */
    fun isVideoReady(): Boolean = isVideoLoaded
    
    /**
     * Handle focus changes for Android TV - start/pause video based on focus
     */
    fun onFocusChanged(hasFocus: Boolean) {
        Log.d(TAG, "Focus changed: hasFocus=$hasFocus, shouldAutoPlay=$shouldAutoPlay, isVideoLoaded=$isVideoLoaded")
        if (hasFocus && shouldAutoPlay && isVideoLoaded) {
            // Add slight delay to ensure smooth focus transition
            postDelayed({
                startVideo()
            }, 300)
        } else {
            pauseVideo()
        }
    }
    
    /**
     * Handle visibility changes for performance optimization
     */
    fun onVisibilityChanged(isVisible: Boolean) {
        Log.d(TAG, "Visibility changed: isVisible=$isVisible")
        if (!isVisible) {
            pauseVideo()
        } else if (shouldAutoPlay && isVideoLoaded) {
            startVideo()
        }
    }
    
    /**
     * Preload video without starting playback
     */
    fun preloadVideo() {
        if (trailerUrl.isNotEmpty() && isValidVideoUrl(trailerUrl) && !isVideoLoaded) {
            shouldAutoPlay = false // Don't auto-play when preloading
            loadVideo()
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopVideo()
        viewScope.cancel()
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Resume video if it was playing
        if (shouldAutoPlay && isVideoLoaded) {
            startVideo()
        }
    }
}