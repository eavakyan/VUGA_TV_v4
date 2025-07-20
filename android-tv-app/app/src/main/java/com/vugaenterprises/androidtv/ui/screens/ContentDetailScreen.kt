package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.animation.core.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.ui.viewmodels.ContentDetailViewModel

@Composable
fun ContentDetailScreen(
    contentId: Int,
    onContentClick: (Content) -> Unit,
    onNavigateBack: () -> Unit,
    onPlayVideo: (Content) -> Unit,
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Section
                item {
                    Box {
                        AsyncImage(
                            model = content.horizontalPoster.ifEmpty { content.verticalPoster },
                            contentDescription = content.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Play Button
                        Button(
                            onClick = { onPlayVideo(content) },
                            modifier = Modifier.align(Alignment.Center),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                if (content.isShow == 1 && content.seasons.isNotEmpty()) "Select Episode" else "Play",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                
                // Content Info
                item {
                    Column {
                        Text(
                            text = content.title,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "${content.releaseYear} • ${content.duration} • ${String.format("%.1f", content.ratings)}★",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = content.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Genres
                if (content.genreList.isNotEmpty()) {
                    item {
                        Text(
                            text = "Genres: ${content.genreString}",
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                    text = "${content.releaseYear} • ${content.duration}",
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