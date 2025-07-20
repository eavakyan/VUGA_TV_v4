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
            Column {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = "Select Episode",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
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
        
        // Seasons and Episodes using Android Views for reliable focus
        if (content.seasons.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(content.seasons) { season ->
                    AndroidViewSeasonSection(
                        season = season,
                        onEpisodeClick = onEpisodeClick
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

@Composable
fun AndroidViewSeasonSection(
    season: SeasonItem,
    onEpisodeClick: (EpisodeItem) -> Unit
) {
    val context = LocalContext.current
    
    Column {
        // Season Header
        Text(
            text = season.title.ifEmpty { "Season ${season.id}" },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Episodes using Android RecyclerView for reliable focus
                    AndroidView(
                factory = { context ->
                    RecyclerView(context).apply {
                        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        adapter = EpisodeAdapter(season.episodes, season.id, onEpisodeClick)
                    // Enable focus for Android TV
                    isFocusable = true
                    isFocusableInTouchMode = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
    }
}

 