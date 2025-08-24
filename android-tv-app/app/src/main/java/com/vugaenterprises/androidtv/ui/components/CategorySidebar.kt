package com.vugaenterprises.androidtv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vugaenterprises.androidtv.data.model.LiveTvCategory

/**
 * TUBI-style sidebar for category navigation in Live TV
 */
@Composable
fun CategorySidebar(
    categories: List<LiveTvCategory>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onNavigateToGrid: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val lazyListState = rememberLazyListState()
    
    // Create predefined categories
    val predefinedCategories = remember {
        listOf(
            LiveTvCategory(0, "Recommended", "recommended", "", 0),
            LiveTvCategory(-1, "Featured", "featured", "", 0),
            LiveTvCategory(-2, "Recently Added", "recently-added", "", 0),
            LiveTvCategory(-3, "National News", "news", "", 0),
            LiveTvCategory(-4, "Sports On Now", "sports-live", "", 0),
            LiveTvCategory(-5, "Movies", "movies", "", 0),
            LiveTvCategory(-6, "Entertainment", "entertainment", "", 0),
            LiveTvCategory(-7, "Kids", "kids", "", 0)
        )
    }
    
    val allCategories = predefinedCategories + categories.filter { category ->
        predefinedCategories.none { it.slug == category.slug }
    }

    Surface(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.type == KeyEventType.KeyDown -> {
                        when (keyEvent.key) {
                            Key.DirectionRight -> {
                                onNavigateToGrid()
                                true
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            },
        color = Color(0xFF0A0A0A),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp)
        ) {
            // Sidebar header
            Text(
                text = "Live TV",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            
            Divider(
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Categories list
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(allCategories) { index, category ->
                    CategorySidebarItem(
                        category = category,
                        isSelected = selectedCategory == category.slug || (selectedCategory == null && category.slug == "recommended"),
                        onSelected = { 
                            if (category.slug == "recommended") {
                                onCategorySelected(null)
                            } else {
                                onCategorySelected(category.slug)
                            }
                        },
                        modifier = if (index == 0) {
                            Modifier.focusRequester(focusRequester)
                        } else {
                            Modifier
                        }
                    )
                }
            }
            
            // Footer with current time
            Divider(
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            
            TimeIndicator(
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

/**
 * Individual category item in sidebar
 */
@Composable
private fun CategorySidebarItem(
    category: LiveTvCategory,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    // Animation values
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color(0xFFE50914)
            isFocused -> Color(0xFF2A2A2A)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "sidebarItemBackground"
    )
    
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White
            isFocused -> Color.White
            else -> Color.White.copy(alpha = 0.8f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "sidebarItemText"
    )
    
    val paddingStart by animateDpAsState(
        targetValue = if (isFocused) 20.dp else 16.dp,
        animationSpec = spring(),
        label = "sidebarItemPadding"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelected
            )
            .focusable(interactionSource = interactionSource)
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = paddingStart,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category icon (if available)
            if (category.iconUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(category.iconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "${category.name} icon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                // Default icon placeholder
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = textColor.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = category.name,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (category.channelCount > 0) {
                    Text(
                        text = "${category.channelCount} channels",
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
            
            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(24.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

/**
 * Time indicator component for sidebar footer
 */
@Composable
private fun TimeIndicator(
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(getCurrentTime()) }
    
    // Update time every minute
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000L) // Update every minute
            currentTime = getCurrentTime()
        }
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Current Time",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Get current time formatted for display
 */
private fun getCurrentTime(): String {
    return try {
        val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        formatter.format(java.util.Date())
    } catch (e: Exception) {
        "12:00 PM"
    }
}