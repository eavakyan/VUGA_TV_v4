package com.vugaenterprises.androidtv.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vugaenterprises.androidtv.data.model.Content
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AutoScrollingBanner(
    content: List<Content>,
    onContentClick: (Content) -> Unit,
    modifier: Modifier = Modifier
) {
    if (content.isEmpty()) return
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var currentFocusedIndex by remember { mutableStateOf(0) }
    var isUserInteracting by remember { mutableStateOf(false) }
    
    // Auto-scroll animation (paused when user is interacting)
    val autoScrollAnimation by rememberInfiniteTransition(label = "autoScroll").animateFloat(
        initialValue = 0f,
        targetValue = content.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isUserInteracting) 1000000 else content.size * 3000, // Pause when interacting
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "autoScroll"
    )
    
    // Auto-scroll effect
    LaunchedEffect(autoScrollAnimation) {
        if (!isUserInteracting) {
            val targetIndex = (autoScrollAnimation % content.size).toInt()
            listState.animateScrollToItem(targetIndex)
            currentFocusedIndex = targetIndex
        }
    }
    
    // Focus management
    LaunchedEffect(Unit) {
        delay(1000) // Wait for initial load
        focusRequester.requestFocus()
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black)
    ) {
        // Banner title
        Text(
            text = "Featured",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        
        // Auto-scrolling row
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .focusGroup()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.DirectionRight -> {
                                focusManager.moveFocus(FocusDirection.Right)
                                true
                            }
                            Key.DirectionLeft -> {
                                focusManager.moveFocus(FocusDirection.Left)
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
        ) {
            itemsIndexed(content) { index, item ->
                BannerItem(
                    content = item,
                    isFocused = currentFocusedIndex == index,
                    onClick = { onContentClick(item) },
                    onFocusChanged = { focused ->
                        if (focused) {
                            currentFocusedIndex = index
                            isUserInteracting = true
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                            // Resume auto-scroll after user interaction
                            coroutineScope.launch {
                                delay(5000) // Wait 5 seconds after user interaction
                                isUserInteracting = false
                            }
                        }
                    },
                    focusRequester = if (index == 0) focusRequester else null
                )
            }
        }
    }
}

@Composable
fun BannerItem(
    content: Content,
    isFocused: Boolean,
    onClick: () -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    focusRequester: FocusRequester? = null
) {
    // LocalContext.current is available if needed for future features
    
    // Focus animation
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            .scale(scale)
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .focusable()
            .focusRequester(focusRequester ?: remember { FocusRequester() })
            .onFocusChanged { focusState ->
                onFocusChanged(focusState.isFocused)
            },
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 8.dp else 4.dp
        )
    ) {
        Box {
            // Background image
            AsyncImage(
                model = content.horizontalPoster.ifEmpty { content.verticalPoster },
                contentDescription = content.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            // Content info overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rating
                    if (content.ratings > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "★",
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp
                            )
                            Text(
                                text = String.format("%.1f", content.ratings),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    
                    // Year
                    if (content.releaseYear > 0) {
                        Text(
                            text = content.releaseYear.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Duration
                    if (content.duration.isNotEmpty()) {
                        Text(
                            text = content.duration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            // Play button overlay when focused
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} 