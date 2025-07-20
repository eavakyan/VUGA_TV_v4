package com.vugaenterprises.androidtv.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import com.vugaenterprises.androidtv.data.model.Content

@Composable
fun ContentCard(
    content: Content,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
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
        modifier = modifier
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
                shape = RoundedCornerShape(12.dp)
            ),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 16.dp else 0.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            // Poster Image - use vertical poster for cards
            AsyncImage(
                model = content.verticalPoster.ifEmpty { content.horizontalPoster },
                contentDescription = content.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Focus overlay with glow effect
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00BFFF).copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
            
            // Progress indicator overlay
            if (content.watchProgress > 0f) {
                LinearProgressIndicator(
                    progress = content.watchProgress,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Black.copy(alpha = 0.3f)
                )
            }
            
            // Rating badge
            if (content.ratings > 0) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFD700)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = String.format("%.1f", content.ratings),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Focus indicator - bright border overlay
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 4.dp,
                            color = Color(0xFF00BFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
        }
    }
    
    // Auto-focus first item (optional)
    LaunchedEffect(Unit) {
        // Uncomment if you want the first item to be focused by default
        // focusRequester.requestFocus()
    }
} 