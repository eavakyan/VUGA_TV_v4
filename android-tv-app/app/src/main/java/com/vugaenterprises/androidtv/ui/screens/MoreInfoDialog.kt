package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.ui.components.TVButton
import com.vugaenterprises.androidtv.utils.TimeUtils

@Composable
fun MoreInfoDialog(
    content: Content,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with poster and title
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Poster
                        AsyncImage(
                            model = content.verticalPoster.ifEmpty { content.horizontalPoster },
                            contentDescription = content.title,
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Title and basic info
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = content.title,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // Metadata
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "${content.releaseYear}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 16.sp
                                )
                                
                                Text(
                                    text = TimeUtils.formatRuntimeFromString(content.duration),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 16.sp
                                )
                                
                                if (content.isShow == 1) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF2196F3)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "TV SERIES",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Description
                item {
                    Column {
                        Text(
                            text = "Synopsis",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = content.description,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 24.sp
                        )
                    }
                }
                
                // Cast
                if (content.contentCast.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "Cast & Crew",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(content.contentCast) { castItem ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(100.dp)
                                    ) {
                                        if (castItem.actor.image.isNotEmpty()) {
                                            AsyncImage(
                                                model = castItem.actor.image,
                                                contentDescription = castItem.actor.name,
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Gray),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = castItem.actor.name.take(2).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 24.sp
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = castItem.actor.name,
                                            fontSize = 14.sp,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        if (castItem.characterName.isNotEmpty()) {
                                            Text(
                                                text = castItem.characterName,
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.6f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Technical Details
                item {
                    Column {
                        Text(
                            text = "Details",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailRow("Type", if (content.isShow == 1) "TV Series" else "Movie")
                            DetailRow("Release Year", content.releaseYear.toString())
                            DetailRow("Duration", TimeUtils.formatRuntimeFromString(content.duration))
                            if (content.seasons.isNotEmpty()) {
                                DetailRow("Seasons", content.seasons.size.toString())
                                DetailRow("Episodes", content.seasons.sumOf { it.episodes.size }.toString())
                            }
                            DetailRow("Views", content.totalView.toString())
                            if (content.totalDownload > 0) {
                                DetailRow("Downloads", content.totalDownload.toString())
                            }
                        }
                    }
                }
                
                // Close button
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TVButton(
                            text = "Close",
                            onClick = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}