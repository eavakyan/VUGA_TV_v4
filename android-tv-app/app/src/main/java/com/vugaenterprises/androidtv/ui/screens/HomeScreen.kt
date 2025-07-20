package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.ui.components.AutoScrollingBanner
import com.vugaenterprises.androidtv.ui.components.ContentRow
import com.vugaenterprises.androidtv.ui.components.FeaturedContentSection
import com.vugaenterprises.androidtv.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    onContentClick: (Content) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
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
            // Use Android View for better focus handling on Android TV
            AndroidView(
                factory = { context ->
                    HomeScreenView(context).apply {
                        setOnContentClick { content ->
                            onContentClickRemembered(content)
                        }
                        setOnNavigateToSearch {
                            onNavigateToSearchRemembered()
                        }
                        setOnNavigateToProfile {
                            // Could add profile navigation here if needed
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
                                recommendations = uiState.recommendations
                            )
                        }
                    }
                }
            )
        }
    }
} 