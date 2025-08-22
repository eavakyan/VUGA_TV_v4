package com.vugaenterprises.androidtv.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vugaenterprises.androidtv.data.model.LiveChannel

/**
 * TV-optimized channel card component for Live TV browsing
 */
@Composable
fun ChannelCard(
    channel: LiveChannel,
    onChannelClick: (LiveChannel) -> Unit,
    modifier: Modifier = Modifier,
    showChannelNumber: Boolean = true,
    showProgramInfo: Boolean = true,
    cardSize: ChannelCardSize = ChannelCardSize.MEDIUM
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    // TV-friendly focus animations
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "channelCardScale"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFFE50914) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "channelCardBorder"
    )
    
    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = 200),
        label = "channelCardShadow"
    )

    Card(
        modifier = modifier
            .size(
                width = cardSize.width,
                height = cardSize.height
            )
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onChannelClick(channel) }
            )
            .focusable(interactionSource = interactionSource)
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = shadowElevation
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Channel thumbnail/logo
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(channel.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "${channel.name} logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )
            
            // Dark gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // Live indicator
            if (channel.isLive) {
                LiveIndicator(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
            
            // Channel number badge
            if (showChannelNumber && channel.channelNumber > 0) {
                ChannelNumberBadge(
                    channelNumber = channel.channelNumber,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }
            
            // Channel info at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Channel name
                Text(
                    text = channel.name,
                    color = Color.White,
                    fontSize = cardSize.titleFontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Program info
                if (showProgramInfo && channel.hasProgramInfo) {
                    Text(
                        text = channel.programInfo,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = cardSize.subtitleFontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Category tag
                if (channel.category.isNotEmpty()) {
                    CategoryTag(
                        category = channel.category,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            // Premium indicator
            if (channel.isPremium > 0) {
                PremiumIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }
    }
}

/**
 * Live indicator badge
 */
@Composable
private fun LiveIndicator(
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveIndicatorAlpha"
    )
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = Color.Red.copy(alpha = alpha)
    ) {
        Text(
            text = "LIVE",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Channel number badge
 */
@Composable
private fun ChannelNumberBadge(
    channelNumber: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Text(
            text = channelNumber.toString(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(6.dp)
                .widthIn(min = 20.dp)
        )
    }
}

/**
 * Category tag
 */
@Composable
private fun CategoryTag(
    category: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Text(
            text = category.uppercase(),
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Premium channel indicator
 */
@Composable
private fun PremiumIndicator(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFFFD700) // Gold color
    ) {
        Text(
            text = "PREMIUM",
            color = Color.Black,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

/**
 * Different card sizes for various layouts
 */
enum class ChannelCardSize(
    val width: Dp,
    val height: Dp,
    val titleFontSize: androidx.compose.ui.unit.TextUnit,
    val subtitleFontSize: androidx.compose.ui.unit.TextUnit
) {
    SMALL(width = 200.dp, height = 120.dp, titleFontSize = 12.sp, subtitleFontSize = 10.sp),
    MEDIUM(width = 280.dp, height = 160.dp, titleFontSize = 14.sp, subtitleFontSize = 12.sp),
    LARGE(width = 320.dp, height = 180.dp, titleFontSize = 16.sp, subtitleFontSize = 14.sp),
    EXTRA_LARGE(width = 400.dp, height = 220.dp, titleFontSize = 18.sp, subtitleFontSize = 16.sp)
}

/**
 * Grid layout for multiple channels
 */
@Composable
fun ChannelGrid(
    channels: List<LiveChannel>,
    onChannelClick: (LiveChannel) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4,
    cardSize: ChannelCardSize = ChannelCardSize.MEDIUM,
    showChannelNumbers: Boolean = true,
    showProgramInfo: Boolean = true
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(channels.size) { index ->
            ChannelCard(
                channel = channels[index],
                onChannelClick = onChannelClick,
                cardSize = cardSize,
                showChannelNumber = showChannelNumbers,
                showProgramInfo = showProgramInfo
            )
        }
    }
}