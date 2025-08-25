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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.compose.AsyncImage
import com.vugaenterprises.androidtv.R
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.EpisodeItem
import com.vugaenterprises.androidtv.data.model.SeasonItem
import com.vugaenterprises.androidtv.ui.components.EpisodeAdapter
import com.vugaenterprises.androidtv.ui.viewmodels.ContentDetailViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EpisodeSelectionScreen(
    contentId: Int,
    onEpisodeClick: (EpisodeItem) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ContentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(contentId) {
        viewModel.loadContent(contentId)
    }
    
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
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
            EpisodeSelectionContent(
                content = content,
                onEpisodeClick = onEpisodeClick,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun EpisodeSelectionContent(
    content: Content,
    onEpisodeClick: (EpisodeItem) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedSeasonIndex by remember { mutableStateOf(0) }
    var expandedSeasonDropdown by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = "Episodes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Season Selector Dropdown (if multiple seasons)
            if (content.seasons.size > 1) {
                Box {
                    Button(
                        onClick = { expandedSeasonDropdown = !expandedSeasonDropdown },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A2A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = content.seasons.getOrNull(selectedSeasonIndex)?.title 
                                ?: "Season ${selectedSeasonIndex + 1}",
                            color = Color.White
                        )
                        Icon(
                            imageVector = if (expandedSeasonDropdown) 
                                Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expandedSeasonDropdown,
                        onDismissRequest = { expandedSeasonDropdown = false },
                        modifier = Modifier.background(Color(0xFF2A2A2A))
                    ) {
                        content.seasons.forEachIndexed { index, season ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = season.title.ifEmpty { "Season ${index + 1}" },
                                        color = Color.White
                                    )
                                },
                                onClick = {
                                    selectedSeasonIndex = index
                                    expandedSeasonDropdown = false
                                },
                                modifier = Modifier.background(
                                    if (index == selectedSeasonIndex) 
                                        Color(0xFF3A3A3A) else Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
            
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
            ) {
                Text("Back", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Episodes for selected season
        if (content.seasons.isNotEmpty()) {
            val currentSeason = content.seasons.getOrNull(selectedSeasonIndex) ?: content.seasons.first()
            
            // Season title
            Text(
                text = currentSeason.title.ifEmpty { "Season ${selectedSeasonIndex + 1}" },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Episodes in a scrollable grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(currentSeason.episodes) { episode ->
                    EpisodeListItem(
                        episode = episode,
                        seasonNumber = currentSeason.id,
                        onClick = { onEpisodeClick(episode) }
                    )
                }
            }
        } else {
            // Fallback for content with episodes but no seasons
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No Episodes Available",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "This show doesn't have any episodes yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Helper function to format release date
fun formatReleaseDate(dateString: String): String {
    // Expected format: "2024-01-15" or similar
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
fun EpisodeListItem(
    episode: EpisodeItem,
    seasonNumber: Int,
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Episode Thumbnail
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = episode.thumbnail,
                    contentDescription = episode.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Play icon overlay
                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Episode Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Episode number and title
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF00BFFF)
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
                        
                        Text(
                            text = episode.title.ifEmpty { "Episode ${episode.number}" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Episode description
                    Text(
                        text = episode.description.ifEmpty { "No description available" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Episode metadata
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duration
                    if (!episode.duration.isNullOrEmpty()) {
                        Text(
                            text = com.vugaenterprises.androidtv.utils.TimeUtils.formatRuntimeFromString(episode.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    
                    // Rating
                    if (episode.rating > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "★",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = String.format("%.1f", episode.rating),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // Release Date
                    if (!episode.releaseDate.isNullOrEmpty()) {
                        Text(
                            text = formatReleaseDate(episode.releaseDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

 