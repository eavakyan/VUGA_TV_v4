package com.vugaenterprises.androidtv.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

data class NavigationItem(
    val id: String,
    val title: String,
    val isSelected: Boolean = false,
    val subItems: List<NavigationItem>? = null
)

@Composable
fun NetflixNavigationBar(
    navigationItems: List<NavigationItem>,
    selectedItemId: String,
    onItemSelected: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var currentFocusedIndex by remember { mutableStateOf(0) }
    var showSubmenu by remember { mutableStateOf(false) }
    var submenuParentId by remember { mutableStateOf<String?>(null) }
    
    // Netflix-style gradient background
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.9f),
                        Color.Black.copy(alpha = 0.7f),
                        Color.Transparent
                    )
                )
            )
            .zIndex(10f)
    ) {
        // Main navigation bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .focusable()
        ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo placeholder - replace with actual logo when available
            Text(
                text = "VUGA",
                color = Color(0xFFE50914), // Netflix red
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 40.dp)
            )
            
            // Navigation Items Container
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                itemsIndexed(navigationItems) { index, item ->
                    NetflixNavigationItem(
                        item = item,
                        isSelected = item.id == selectedItemId,
                        onItemClick = {
                            onItemSelected(item)
                            // Show submenu if this item has subitems
                            if (item.subItems != null) {
                                showSubmenu = true
                                submenuParentId = item.id
                            } else {
                                showSubmenu = false
                            }
                        },
                        onItemFocused = {
                            // Show submenu when Watch is focused
                            if (item.subItems != null) {
                                showSubmenu = true
                                submenuParentId = item.id
                            }
                        },
                        modifier = Modifier
                            .focusRequester(if (index == 0) focusRequester else FocusRequester())
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    currentFocusedIndex = index
                                    // Show submenu when item with subitems gets focus
                                    if (item.subItems != null) {
                                        showSubmenu = true
                                        submenuParentId = item.id
                                    } else {
                                        // Hide submenu when focusing on items without subitems
                                        showSubmenu = false
                                        submenuParentId = null
                                    }
                                }
                            }
                    )
                }
            }
        }
        }
        
        // Submenu display
        AnimatedVisibility(
            visible = showSubmenu && submenuParentId != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val parentItem = navigationItems.find { it.id == submenuParentId }
            parentItem?.subItems?.let { subItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(horizontal = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Add some padding to align with parent item
                    Spacer(modifier = Modifier.width(140.dp)) // Space for logo and alignment
                    
                    subItems.forEach { subItem ->
                        NetflixSubmenuItem(
                            item = subItem,
                            onItemClick = {
                                onItemSelected(subItem)
                                // Navigate based on submenu selection
                                when (subItem.id) {
                                    "movies", "tv_shows", "cartoons", "anime", "hbo", "cinemax" -> {
                                        // These will navigate to filtered content
                                        onItemSelected(subItem)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Don't request initial focus - let the content have it first
    // The navigation bar will receive focus when user presses UP
}

@Composable
fun NetflixNavigationItem(
    item: NavigationItem,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onItemFocused: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    // Netflix-style animations
    val scale by animateFloatAsState(
        targetValue = when {
            isFocused -> 1.05f
            isSelected -> 1.02f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val textColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color.White.copy(alpha = 0.95f)
            else -> Color.White.copy(alpha = 0.6f)
        },
        animationSpec = tween(durationMillis = 150),
        label = "textColor"
    )
    
    val underlineAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "underlineAlpha"
    )
    
    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onItemClick
            )
            .focusable(interactionSource = interactionSource)
            .scale(scale)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = item.title,
            color = textColor,
            fontSize = 18.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Netflix-style underline
        Box(
            modifier = Modifier
                .width(if (isSelected) 40.dp else 0.dp)
                .height(3.dp)
                .graphicsLayer { alpha = underlineAlpha }
                .background(Color(0xFFE50914)) // Netflix red
        )
    }
}

@Composable
fun NetflixSubmenuItem(
    item: NavigationItem,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val textColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color.White.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 150),
        label = "submenuTextColor"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "submenuScale"
    )
    
    Text(
        text = item.title,
        color = textColor,
        fontSize = 16.sp,
        fontWeight = if (isFocused) FontWeight.Medium else FontWeight.Normal,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onItemClick
            )
            .focusable(interactionSource = interactionSource)
            .scale(scale)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

