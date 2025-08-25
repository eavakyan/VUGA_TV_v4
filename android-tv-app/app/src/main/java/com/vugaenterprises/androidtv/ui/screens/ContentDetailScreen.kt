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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import com.vugaenterprises.androidtv.data.model.EpisodeItem
import com.vugaenterprises.androidtv.data.model.SeasonItem
import com.vugaenterprises.androidtv.ui.components.TrailerPlayer
import com.vugaenterprises.androidtv.ui.viewmodels.ContentDetailViewModel
import com.vugaenterprises.androidtv.utils.CategoryUtils
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
    var showMoreInfo by remember { mutableStateOf(false) }

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
            val content = uiState.content
            if (content == null) {
                // Safety check - should not happen but prevents crash
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Content unavailable",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                return@ContentDetailScreen
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Back) {
                            onNavigateBack()
                            true
                        } else {
                            false
                        }
                    }
            ) {
                // Back Button
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "← Back",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // Trailer Player Section
                if (content.trailerUrl.isNotEmpty() && 
                    (content.trailerUrl.startsWith("http://") || content.trailerUrl.startsWith("https://"))) {
                    item {
                        TrailerPlayer(
                            trailerUrl = content.trailerUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            autoPlay = true,
                            showControls = true
                        )
                    }
                } else {
                    // Fallback to poster image if no trailer
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                        ) {
                            AsyncImage(
                                model = content.horizontalPoster.ifEmpty { content.verticalPoster },
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
                                                Color.Black.copy(alpha = 0.8f)
                                            ),
                                            startY = 100f
                                        )
                                    )
                            )
                        }
                    }
                }
                
                // Content Info Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        // Title
                        Text(
                            text = content.title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Metadata Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Release Year
                            Text(
                                text = content.releaseYear.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            
                            // Duration
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = TimeUtils.formatRuntimeFromString(content.duration),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                            
                            // Rating
                            if (content.ratings > 0) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFD700)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "★ ${String.format("%.1f", content.ratings)}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.Black
                                    )
                                }
                            }
                            
                            // TV Series Badge
                            if (content.isShow == 1) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF2196F3)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "SERIES",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Play Button
                            Button(
                                onClick = { onPlayVideo(content) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                elevation = ButtonDefaults.buttonElevation(6.dp)
                            ) {
                                Text(
                                    text = if (content.isShow == 1 && content.seasons.isNotEmpty()) 
                                        "▶ Episodes" else "▶ Play",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            
                            // My List Button
                            Button(
                                onClick = { viewModel.toggleWatchlist() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (content.isWatchlist) 
                                        Color(0xFF4CAF50) else Color.White.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(24.dp),
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
                                        text = if (content.isWatchlist) "✓ My List" else "+ My List",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            
                            // More Info Button
                            Button(
                                onClick = { showMoreInfo = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(
                                    text = "ⓘ More Info",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                // Categories Section
                if (content.genreIds.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Categories",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val categories = CategoryUtils.getCategoryNames(content.genreIds)
                                items(categories) { category ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.15f)
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = category,
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
                
                // Description Section
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
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
                
                // Cast Preview
                if (content.contentCast.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Cast",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = content.contentCast.take(5).joinToString(", ") { it.actor.name },
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                // Episodes Section for TV Shows
                if (content.isShow == 1 && content.seasons.isNotEmpty()) {
                    item {
                        EpisodesSection(
                            seasons = content.seasons,
                            onEpisodeClick = { episode ->
                                // Navigate to episode playback
                                onPlayVideo(content)
                            },
                            onViewAllClick = {
                                // Navigate to full episode selection screen
                                onPlayVideo(content)
                            }
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
            
            // More Info Dialog
            if (showMoreInfo) {
                MoreInfoDialog(
                    content = content,
                    onDismiss = { showMoreInfo = false }
                )
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
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
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
                color = if (isFocused) Color(0xFF00BFFF) else Color.Transparent,
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
            AsyncImage(
                model = content.verticalPoster.ifEmpty { content.horizontalPoster },
                contentDescription = content.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            
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
                    Text(
                        text = "★ ${String.format("%.1f", content.ratings)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Helper function to format episode release date
fun formatEpisodeReleaseDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        if (parts.size >= 3) {
            val year = parts[0]
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1
            
            val monthNames = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            
            "${monthNames.getOrNull(month - 1) ?: "Jan"} $day, $year"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun EpisodesSection(
    seasons: List<SeasonItem>,
    onEpisodeClick: (EpisodeItem) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (seasons.size == 1) "Episodes" else "Season ${seasons.firstOrNull()?.id ?: 1} Episodes",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            
            TextButton(
                onClick = onViewAllClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF00BFFF)
                )
            ) {
                Text(
                    text = "View All Episodes →",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Show first season's episodes (or first 6 episodes)
        val firstSeason = seasons.firstOrNull()
        if (firstSeason != null) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val episodesToShow = firstSeason.episodes.take(6)
                items(episodesToShow) { episode ->
                    EpisodeCard(
                        episode = episode,
                        seasonNumber = firstSeason.id,
                        onClick = { onEpisodeClick(episode) }
                    )
                }
                
                // Add "View More" card if there are more episodes
                if (firstSeason.episodes.size > 6 || seasons.size > 1) {
                    item {
                        ViewMoreEpisodesCard(onClick = onViewAllClick)
                    }
                }
            }
        }
    }
}

@Composable
fun EpisodeCard(
    episode: EpisodeItem,
    seasonNumber: Int,
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .scale(scale)
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = if (isFocused) Color(0xFF00BFFF) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Episode Thumbnail
            Box {
                AsyncImage(
                    model = episode.thumbnail,
                    contentDescription = episode.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(157.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Episode number badge
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "S${seasonNumber}E${episode.number}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Episode Info
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = episode.title.ifEmpty { "Episode ${episode.number}" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Episode metadata row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duration
                    Text(
                        text = if (!episode.duration.isNullOrEmpty()) {
                            TimeUtils.formatRuntimeFromString(episode.duration)
                        } else {
                            "N/A"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    // Separator
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    
                    // Rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "★",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = if (episode.rating > 0) {
                                String.format("%.1f", episode.rating)
                            } else {
                                "N/A"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Release date - always show
                Text(
                    text = if (!episode.releaseDate.isNullOrEmpty()) {
                        "Released: ${formatEpisodeReleaseDate(episode.releaseDate)}"
                    } else {
                        "Release Date: N/A"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ViewMoreEpisodesCard(
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(220.dp)
            .scale(scale)
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = if (isFocused) Color(0xFF00BFFF) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "View More",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
                Text(
                    text = "View All Episodes",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RelatedContentSection(
    relatedContent: List<Content>,
    onContentClick: (Content) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "More Like This",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
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