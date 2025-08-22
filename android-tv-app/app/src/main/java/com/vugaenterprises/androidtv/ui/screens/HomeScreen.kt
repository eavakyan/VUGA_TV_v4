package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.GenreContents
import com.vugaenterprises.androidtv.ui.components.AutoScrollingBanner
import com.vugaenterprises.androidtv.ui.components.ContentRow
import com.vugaenterprises.androidtv.ui.components.FeaturedContentSection
import com.vugaenterprises.androidtv.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    onContentClick: (Content) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onRequestNavBarFocus: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Performance optimization: Use derivedStateOf for expensive computations
    val hasContent by remember {
        derivedStateOf {
            uiState.featuredContent.isNotEmpty() || 
            uiState.trendingContent.isNotEmpty() || 
            uiState.newContent.isNotEmpty() || 
            uiState.continueWatching.isNotEmpty() || 
            uiState.recommendations.isNotEmpty()
        }
    }
    
    // Remember callbacks to prevent unnecessary recompositions
    val onContentClickRemembered = remember(onContentClick) { onContentClick }
    val onNavigateToSearchRemembered = remember(onNavigateToSearch) { onNavigateToSearch }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading content...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error loading content",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { viewModel.refreshContent() }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
        
        else -> {
            val focusManager = LocalFocusManager.current
            
            // Use Android View for better focus handling on Android TV
            AndroidView(
                factory = { context ->
                    object : HomeScreenView(context) {
                        override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
                            android.util.Log.d("HomeScreen", "AndroidView dispatchKeyEvent: keyCode=${event.keyCode}, action=${event.action}")
                            
                            if (event.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP && 
                                event.action == android.view.KeyEvent.ACTION_DOWN) {
                                // Check if we're at the top of content
                                val focusedChild = findFocus()
                                if (focusedChild != null && contentContainer.childCount > 0) {
                                    val firstChild = contentContainer.getChildAt(0)
                                    // Check if focused element is within the first content (featured slider)
                                    var parent = focusedChild.parent
                                    while (parent != null && parent != this) {
                                        if ((parent is FeaturedSliderView && firstChild is FeaturedSliderView) ||
                                            (parent is ContentRowView && firstChild == parent)) {
                                            android.util.Log.d("HomeScreen", "UP pressed on first content - calling onNavigateUp")
                                            // Call the navigate up callback
                                            onNavigateUpCallback?.invoke()
                                            return true
                                        }
                                        parent = parent.parent
                                    }
                                }
                            }
                            
                            return super.dispatchKeyEvent(event)
                        }
                    }.apply {
                        setOnContentClick { content ->
                            onContentClickRemembered(content)
                        }
                        setOnNavigateToSearch {
                            onNavigateToSearchRemembered()
                        }
                        setOnNavigateToProfile {
                            // Could add profile navigation here if needed
                        }
                        setOnNavigateUp {
                            android.util.Log.d("HomeScreen", "onNavigateUp callback triggered")
                            // Call the callback to request nav bar focus
                            onRequestNavBarFocus()
                        }
                    }
                },
                update = { homeScreenView ->
                    when {
                        uiState.isLoading -> {
                            homeScreenView.showLoading()
                        }
                        uiState.error != null -> {
                            homeScreenView.showError(uiState.error ?: "Unknown error")
                        }
                        else -> {
                            homeScreenView.updateContent(
                                featuredContent = uiState.featuredContent,
                                trendingContent = uiState.trendingContent,
                                newContent = uiState.newContent,
                                continueWatching = uiState.continueWatching.map { it.content }.filterNotNull(),
                                recommendations = uiState.recommendations,
                                categoryContent = uiState.categoryContent
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .focusable()
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionUp) {
                            // When UP is pressed, move focus to the navigation bar
                            focusManager.moveFocus(FocusDirection.Up)
                            true
                        } else {
                            false
                        }
                    }
            )
        }
    }
} 