package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.ui.viewmodels.ContentDetailViewModel
import com.vugaenterprises.androidtv.utils.TimeUtils

@Composable
fun ContentDetailScreen(
    contentId: Int,
    onContentClick: (Content) -> Unit,
    onNavigateBack: () -> Unit,
    onPlayVideo: (Content) -> Unit,
    onWatchlistChanged: (() -> Unit)? = null,
    viewModel: ContentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(contentId) {
        viewModel.loadContent(contentId)
    }
    
    // Handle watchlist changes
    LaunchedEffect(uiState.watchlistChanged) {
        if (uiState.watchlistChanged) {
            onWatchlistChanged?.invoke()
            viewModel.onWatchlistChangeHandled()
        }
    }

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
        
        uiState.content != null -> {
            val content = uiState.content!!
            
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Back) {
                            onNavigateBack()
                            true
                        } else {
                            false
                        }
                    }
            ) {
                // Back Button Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "← Back to Home",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                // Modern Hero Section
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                    ) {
                        // Background with vertical poster for TV
                        AsyncImage(
                            model = content.verticalPoster.ifEmpty { content.horizontalPoster },
                            contentDescription = content.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        ),
                                        startY = 200f
                                    )
                                )
                        )
                        
                        // Content overlay
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(48.dp),
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Vertical poster
                            Card(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(270.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(16.dp)
                            ) {
                                AsyncImage(
                                    model = content.verticalPoster.ifEmpty { content.horizontalPoster },
                                    contentDescription = content.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            // Content info
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = content.title,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Metadata row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rating badge
                                    if (content.ratings > 0) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFFFFD700)
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(
                                                text = "★ ${String.format("%.1f", content.ratings)}",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.Black
                                            )
                                        }
                                    }
                                    
                                    Text(
                                        text = content.releaseYear.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    
                                    // Duration with proper formatting
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.2f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = TimeUtils.formatRuntimeFromString(content.duration),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Action buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = { onPlayVideo(content) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(32.dp),
                                        elevation = ButtonDefaults.buttonElevation(8.dp)
                                    ) {
                                        Text(
                                            text = if (content.isShow == 1 && content.seasons.isNotEmpty()) "▶ Select Episode" else "▶ Play",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                        )
                                    }
                                    
                                    // Watchlist Button
                                    Button(
                                        onClick = { viewModel.toggleWatchlist() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (content.isWatchlist) 
                                                Color(0xFF4CAF50) else Color.White.copy(alpha = 0.4f)
                                        ),
                                        shape = RoundedCornerShape(32.dp),
                                        enabled = !uiState.isUpdatingWatchlist
                                    ) {
                                        if (uiState.isUpdatingWatchlist) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else {
                                            Text(
                                                text = if (content.isWatchlist) "✓ In List" else "+ My List",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    
                                    Button(
                                        onClick = { /* More info action */ },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        shape = RoundedCornerShape(32.dp)
                                    ) {
                                        Text(
                                            text = "ⓘ More Info",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Description Section
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = content.description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 24.sp
                            ),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                // Genres
                if (content.genreList.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Genres",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(content.genreList) { genre ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.15f)
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = genre,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Cast
                if (content.contentCast.isNotEmpty()) {
                    item {
                        Text(
                            text = "Cast: ${content.contentCast.joinToString(", ") { it.actor.name }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Related Content
                if (content.moreLikeThis.isNotEmpty()) {
                    item {
                        RelatedContentSection(
                            relatedContent = content.moreLikeThis,
                            onContentClick = onContentClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RelatedContentCard(
    content: Content,
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    // Animation for scaling and glow effect
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 200),
        label = "glow"
    )
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .scale(scale)
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                brush = if (isFocused) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00BFFF), // Bright blue
                            Color(0xFF0066CC), // Medium blue
                            Color.Transparent
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                },
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 16.dp else 0.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Content Poster
            Box {
                AsyncImage(
                    model = content.verticalPoster.ifEmpty { content.horizontalPoster },
                    contentDescription = content.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Focus overlay with glow effect
                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .border(
                                width = 2.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00BFFF).copy(alpha = glowAlpha),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            )
                    )
                }
                
                // Focus indicator - bright border overlay
                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .border(
                                width = 4.dp,
                                color = Color(0xFF00BFFF),
                                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            )
                    )
                }
            }
            
            // Content Info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${content.releaseYear} • ${TimeUtils.formatRuntimeFromString(content.duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (content.ratings > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format("%.1f", content.ratings)}★",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun RelatedContentSection(
    relatedContent: List<Content>,
    onContentClick: (Content) -> Unit
) {
    Column {
        Text(
            text = "Related",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            ),
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(relatedContent) { content ->
                RelatedContentCard(
                    content = content,
                    onClick = { onContentClick(content) }
                )
            }
        }
    }
} 