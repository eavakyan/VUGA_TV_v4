package com.vugaenterprises.androidtv.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vugaenterprises.androidtv.data.model.Content

@Composable
fun FeaturedContentSection(
    content: List<Content>,
    onContentClick: (Content) -> Unit
) {
    if (content.isEmpty()) return
    
    val featuredContent = content.firstOrNull() ?: return
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        // Background Image - use vertical poster for TV interface
        AsyncImage(
            model = featuredContent.verticalPoster.ifEmpty { featuredContent.horizontalPoster },
            contentDescription = featuredContent.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.3f)
                )
        )
        
        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Title
            Text(
                text = featuredContent.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Genre and metadata
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "★",
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp
                    )
                    Text(
                        text = String.format("%.1f", featuredContent.ratings),
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp
                    )
                }
                
                // Year
                Text(
                    text = featuredContent.releaseYear.toString(),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                
                // Duration
                if (!featuredContent.duration.isNullOrEmpty()) {
                    Text(
                        text = featuredContent.duration,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Genre chips
            if (featuredContent.genreList.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(featuredContent.genreList.take(3)) { genre ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = genre,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Play button
            Button(
                onClick = { onContentClick(featuredContent) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Play Now",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    
    // Horizontal scrollable featured content
    if (content.size > 1) {
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(content.drop(1)) { item ->
                FeaturedContentCard(
                    content = item,
                    onContentClick = onContentClick
                )
            }
        }
    }
}

@Composable
private fun FeaturedContentCard(
    content: Content,
    onContentClick: (Content) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    // Animation for scaling and glow effect
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 200),
        label = "glow"
    )
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(240.dp)
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
                shape = RoundedCornerShape(8.dp)
            ),
        onClick = { onContentClick(content) },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 16.dp else 0.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            AsyncImage(
                model = content.verticalPoster.ifEmpty { content.horizontalPoster },
                contentDescription = content.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Focus overlay with glow effect
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00BFFF).copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.3f)
                    )
            )
            
            // Title overlay
            Text(
                text = content.title,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                maxLines = 2
            )
            
            // Focus indicator - bright border overlay
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 4.dp,
                            color = Color(0xFF00BFFF),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }
} 