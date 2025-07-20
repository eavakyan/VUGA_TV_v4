package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.ui.components.ContentCard
import com.vugaenterprises.androidtv.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    onContentClick: (Content) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Profile Header
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Profile info
                        uiState.userProfile?.let { profile ->
                            Text(
                                text = profile.username,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            if (profile.firstName != null && profile.lastName != null) {
                                Text(
                                    text = "${profile.firstName} ${profile.lastName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        // Stats
                        uiState.userStats?.let { stats ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Total Watch Time: ${stats.totalWatchTime} minutes",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Watched Content: ${stats.totalContentWatched}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Favorites: ${stats.totalFavorites}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Watch History
                uiState.userStats?.watchHistory?.let { watchHistory ->
                    if (watchHistory.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recently Watched",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        
                        items(watchHistory.take(10)) { historyItem ->
                            historyItem.content?.let { content ->
                                ContentCard(
                                    content = content,
                                    onClick = { onContentClick(content) }
                                )
                            }
                        }
                    }
                }
                
                // Favorites
                uiState.userStats?.favorites?.let { favorites ->
                    if (favorites.isNotEmpty()) {
                        item {
                            Text(
                                text = "My Favorites",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        
                        items(favorites.take(10)) { content ->
                            ContentCard(
                                content = content,
                                onClick = { onContentClick(content) }
                            )
                        }
                    }
                }
                
                // Settings
                item {
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { /* Handle logout */ }
                        ) {
                            Text("Logout")
                        }
                    }
                }
            }
        }
    }
} 