package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import com.vugaenterprises.androidtv.data.repository.ChannelSortBy
import com.vugaenterprises.androidtv.ui.components.ChannelCard
import com.vugaenterprises.androidtv.ui.components.ChannelCardSize
import com.vugaenterprises.androidtv.ui.viewmodels.LiveTVViewModel

/**
 * Main Live TV browsing screen with channel grid and filtering options
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
    val filterRowFocusRequester = remember { FocusRequester() }
    val channelGridFocusRequester = remember { FocusRequester() }
    
    // Handle D-pad navigation
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent(keyEventHandler)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header section
            LiveTVHeader(
                selectedCategory = uiState.selectedCategory,
                sortBy = uiState.sortBy,
                hasActiveFilters = viewModel.hasActiveFilters(),
                onRefresh = viewModel::refreshChannels,
                onClearFilters = viewModel::clearAllFilters,
                modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp)
            )
            
            // Filter section
            if (uiState.categories.isNotEmpty()) {
                CategoryFilterRow(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::filterByCategory,
                    modifier = Modifier
                        .focusRequester(filterRowFocusRequester)
                        .padding(horizontal = 48.dp, vertical = 8.dp)
                )
            }
            
            // Sort options
            SortOptionsRow(
                currentSortBy = uiState.sortBy,
                onSortByChanged = viewModel::sortChannels,
                modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
            )
            
            // Main content
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
                    // Channel grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(channelGridFocusRequester)
                            .padding(horizontal = 48.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(uiState.filteredChannels.size) { index ->
                            ChannelCard(
                                channel = uiState.filteredChannels[index],
                                onChannelClick = onChannelClick,
                                cardSize = ChannelCardSize.MEDIUM,
                                showChannelNumber = true,
                                showProgramInfo = true,
                                modifier = Modifier.onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        // Optional: Auto-scroll logic can be added here
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Refresh indicator
        if (uiState.isRefreshing) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFE50914),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Refreshing channels...",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
    
    // Initial focus management - safe focus request after composition
    LaunchedEffect(uiState.categories.isNotEmpty(), !uiState.isLoading) {
        if (!uiState.isLoading) {
            kotlinx.coroutines.delay(200) // Allow composition to complete
            try {
                if (uiState.categories.isNotEmpty()) {
                    filterRowFocusRequester.requestFocus()
                } else if (uiState.filteredChannels.isNotEmpty()) {
                    channelGridFocusRequester.requestFocus()
                }
            } catch (e: Exception) {
                android.util.Log.w("LiveTVScreen", "Failed to request initial focus", e)
            }
        }
    }
}

/**
 * Header section with title and actions
 */
@Composable
private fun LiveTVHeader(
    selectedCategory: String?,
    sortBy: ChannelSortBy,
    hasActiveFilters: Boolean,
    onRefresh: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live TV",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Clear filters button
                AnimatedVisibility(
                    visible = hasActiveFilters,
                    enter = slideInHorizontally() + fadeIn(),
                    exit = slideOutHorizontally() + fadeOut()
                ) {
                    OutlinedButton(
                        onClick = onClearFilters,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
                        )
                    ) {
                        Text("Clear Filters")
                    }
                }
                
                // Refresh button
                OutlinedButton(
                    onClick = onRefresh,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
                    )
                ) {
                    Text("Refresh")
                }
            }
        }
        
        // Active filters display
        if (selectedCategory != null) {
            Text(
                text = "Category: $selectedCategory",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Category filter row
 */
@Composable
private fun CategoryFilterRow(
    categories: List<ChannelCategory>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // All categories option
        item {
            CategoryChip(
                label = "All",
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }
        
        items(categories) { category ->
            CategoryChip(
                label = "${category.name} (${category.channelCount})",
                isSelected = selectedCategory == category.slug,
                onClick = { onCategorySelected(category.slug) }
            )
        }
    }
}

/**
 * Sort options row
 */
@Composable
private fun SortOptionsRow(
    currentSortBy: ChannelSortBy,
    onSortByChanged: (ChannelSortBy) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ChannelSortBy.values()) { sortBy ->
            CategoryChip(
                label = when (sortBy) {
                    ChannelSortBy.CHANNEL_NUMBER -> "Channel #"
                    ChannelSortBy.NAME -> "Name"
                    ChannelSortBy.CATEGORY -> "Category"
                    ChannelSortBy.CUSTOM_ORDER -> "Featured"
                },
                isSelected = currentSortBy == sortBy,
                onClick = { onSortByChanged(sortBy) }
            )
        }
    }
}

/**
 * Reusable category/filter chip
 */
@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFE50914) else Color.Transparent
    val textColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
    val borderColor = if (isSelected) Color(0xFFE50914) else Color.White.copy(alpha = 0.3f)
    
    Surface(
        modifier = modifier
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                    onClick()
                    true
                } else false
            },
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
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