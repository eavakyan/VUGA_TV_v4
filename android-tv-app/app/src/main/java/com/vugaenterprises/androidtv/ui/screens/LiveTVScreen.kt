package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vugaenterprises.androidtv.data.model.LiveChannel
import com.vugaenterprises.androidtv.data.model.ChannelCategory
import com.vugaenterprises.androidtv.data.model.LiveTvCategory
import com.vugaenterprises.androidtv.data.repository.ChannelSortBy
import com.vugaenterprises.androidtv.ui.components.ChannelCardSize
import com.vugaenterprises.androidtv.ui.components.ChannelGrid
import com.vugaenterprises.androidtv.ui.components.CategorySidebar
import com.vugaenterprises.androidtv.ui.components.HeaderTimeIndicator
import com.vugaenterprises.androidtv.ui.viewmodels.LiveTVViewModel

/**
 * TUBI-style Live TV browsing screen with left sidebar and main channel grid
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTVScreen(
    onChannelClick: (LiveChannel) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LiveTVViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val sidebarFocusRequester = remember { FocusRequester() }
    val channelGridFocusRequester = remember { FocusRequester() }
    
    // Handle D-pad navigation for TUBI-style layout
    val keyEventHandler = { keyEvent: KeyEvent ->
        when {
            keyEvent.type == KeyEventType.KeyDown -> {
                when (keyEvent.key) {
                    Key.DirectionUp -> {
                        focusManager.moveFocus(FocusDirection.Up)
                        true
                    }
                    Key.DirectionDown -> {
                        focusManager.moveFocus(FocusDirection.Down)
                        true
                    }
                    Key.DirectionLeft -> {
                        focusManager.moveFocus(FocusDirection.Left)
                        true
                    }
                    Key.DirectionRight -> {
                        focusManager.moveFocus(FocusDirection.Right)
                        true
                    }
                    Key.Back -> {
                        onNavigateBack()
                        true
                    }
                    else -> false
                }
            }
            else -> false
        }
    }

    // TUBI-style layout with sidebar and main content
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent(keyEventHandler)
    ) {
        // Left sidebar for categories
        CategorySidebar(
            categories = convertToLiveTvCategories(uiState.categories),
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = viewModel::filterByCategory,
            onNavigateToGrid = { 
                // Move focus to channel grid
                try {
                    channelGridFocusRequester.requestFocus()
                } catch (e: Exception) {
                    android.util.Log.w("LiveTVScreen", "Failed to focus grid", e)
                }
            },
            focusRequester = sidebarFocusRequester
        )
        
        // Main content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header with time indicator
                TubiStyleHeader(
                    selectedCategory = uiState.selectedCategory,
                    channelCount = uiState.filteredChannels.size,
                    onRefresh = viewModel::refreshChannels,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                )
                
                // Main content based on state
                when {
                    uiState.isLoading -> {
                        LoadingScreen(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.errorMessage != null -> {
                        ErrorScreen(
                            message = uiState.errorMessage ?: "Unknown error",
                            onRetry = viewModel::loadLiveChannels,
                            onDismiss = viewModel::clearError,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.filteredChannels.isEmpty() -> {
                        EmptyChannelsScreen(
                            hasActiveFilters = viewModel.hasActiveFilters(),
                            onClearFilters = viewModel::clearAllFilters,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        // TUBI-style channel grid
                        ChannelGrid(
                            channels = uiState.filteredChannels,
                            onChannelClick = onChannelClick,
                            columns = 3, // Reduced columns for better TUBI-style layout
                            cardSize = ChannelCardSize.LARGE,
                            showChannelNumbers = true,
                            showProgramInfo = true,
                            useTubiStyle = true,
                            selectedChannelId = viewModel.getSelectedChannelId(),
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(channelGridFocusRequester)
                                .padding(horizontal = 32.dp)
                        )
                    }
                }
            }
            
            // Refresh indicator
            if (uiState.isRefreshing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Black.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFE50914),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Refreshing channels...",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Initial focus management for TUBI-style layout
    LaunchedEffect(uiState.categories.isNotEmpty(), !uiState.isLoading) {
        if (!uiState.isLoading) {
            kotlinx.coroutines.delay(300) // Allow composition to complete
            try {
                // Always start focus on sidebar for TUBI-style navigation
                sidebarFocusRequester.requestFocus()
            } catch (e: Exception) {
                android.util.Log.w("LiveTVScreen", "Failed to request initial focus", e)
            }
        }
    }
}

/**
 * TUBI-style header with category info and time
 */
@Composable
private fun TubiStyleHeader(
    selectedCategory: String?,
    channelCount: Int,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category and channel count info
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = getCategoryDisplayName(selectedCategory),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$channelCount channels available",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Header actions and time
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Refresh button with TUBI styling
            Surface(
                onClick = onRefresh,
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2A2A2A),
                modifier = Modifier
                    .focusable()
                    .padding(2.dp)
            ) {
                Text(
                    text = "Refresh",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Current time indicator
            HeaderTimeIndicator()
        }
    }
}

/**
 * Convert ChannelCategory to LiveTvCategory
 */
private fun convertToLiveTvCategories(categories: List<ChannelCategory>): List<LiveTvCategory> {
    return categories.map { category ->
        LiveTvCategory(
            id = category.id,
            name = category.name,
            slug = category.slug,
            iconUrl = category.imageUrl,
            channelCount = category.channelCount,
            isActive = category.isActive
        )
    }
}

/**
 * Get display name for category
 */
private fun getCategoryDisplayName(selectedCategory: String?): String {
    return when (selectedCategory) {
        null -> "Recommended Channels"
        "featured" -> "Featured Channels"
        "recently-added" -> "Recently Added"
        "news" -> "National News"
        "sports-live" -> "Sports On Now"
        "movies" -> "Movies"
        "entertainment" -> "Entertainment"
        "kids" -> "Kids Programming"
        else -> selectedCategory.replaceFirstChar { it.uppercase() }.replace("-", " ")
    }
}

/**
 * Loading screen
 */
@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFFE50914),
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading live channels...",
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}

/**
 * Error screen
 */
@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Error Loading Channels",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Dismiss")
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
}

/**
 * Empty channels screen
 */
@Composable
private fun EmptyChannelsScreen(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (hasActiveFilters) "No channels match your filters" else "No live channels available",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (hasActiveFilters) "Try adjusting your filter criteria" else "Please check back later",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
            if (hasActiveFilters) {
                Button(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE50914)
                    )
                ) {
                    Text("Clear Filters")
                }
            }
        }
    }
}