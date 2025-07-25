package com.vugaenterprises.androidtv

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.vugaenterprises.androidtv.data.VideoPlayerDataStore
import com.vugaenterprises.androidtv.data.EpisodeDataStore
import com.vugaenterprises.androidtv.data.CastDetailDataStore
import com.vugaenterprises.androidtv.ui.theme.AndroidTVStreamingTheme
import com.vugaenterprises.androidtv.ui.navigation.AppNavigation
import com.vugaenterprises.androidtv.ui.screens.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var videoPlayerDataStore: VideoPlayerDataStore
    
    @Inject
    lateinit var episodeDataStore: EpisodeDataStore
    
    @Inject
    lateinit var castDetailDataStore: CastDetailDataStore
    
    // Callback for video player controls
    var onEnterKeyPressed: (() -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTVStreamingTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    AndroidTVStreamingApp(videoPlayerDataStore, episodeDataStore, castDetailDataStore)
                }
            }
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        android.util.Log.d("MainActivity", "KEY DOWN: $keyCode")
        
        // Handle Enter/OK key (KEYCODE_DPAD_CENTER = 23, KEYCODE_ENTER = 66)
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            android.util.Log.d("MainActivity", "ENTER KEY INTERCEPTED - NOT EXITING")
            onEnterKeyPressed?.let { callback ->
                callback()
                return true
            }
        }
        
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun AndroidTVStreamingApp(
    videoPlayerDataStore: VideoPlayerDataStore,
    episodeDataStore: EpisodeDataStore,
    castDetailDataStore: CastDetailDataStore
) {
    var showSplash by remember { mutableStateOf(true) }
    
    AnimatedVisibility(
        visible = showSplash,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        SplashScreen(
            onSplashComplete = {
                showSplash = false
            }
        )
    }
    
    AnimatedVisibility(
        visible = !showSplash,
        enter = fadeIn(animationSpec = tween(1000, delayMillis = 500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        val navController = rememberNavController()
        
        AppNavigation(
            navController = navController,
            videoPlayerDataStore = videoPlayerDataStore,
            episodeDataStore = episodeDataStore,
            castDetailDataStore = castDetailDataStore
        )
    }
} 