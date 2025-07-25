package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.ui.viewmodels.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NetflixSearchScreen(
    onContentClick: (Content) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchState by viewModel.searchState.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    
    // Debounced search
    LaunchedEffect(searchQuery.text) {
        if (searchQuery.text.isNotEmpty()) {
            delay(300) // Netflix-style debounce
            viewModel.search(searchQuery.text)
        } else {
            viewModel.clearSearch()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 100.dp) // Space for navigation bar
    ) {
        // Netflix-style search bar
        NetflixSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            focusRequester = focusRequester,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Search results or suggestions
        when {
            searchState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE50914),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            searchQuery.text.isEmpty() -> {
                // Show popular searches or categories
                PopularSearches(
                    onSearchTermClick = { term ->
                        searchQuery = TextFieldValue(term)
                    }
                )
            }
            
            searchState.results.isEmpty() -> {
                // No results found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found for \"${searchQuery.text}\"",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 18.sp
                    )
                }
            }
            
            else -> {
                // Show search results grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchState.results) { content ->
                        NetflixSearchResultCard(
                            content = content,
                            onClick = { onContentClick(content) }
                        )
                    }
                }
            }
        }
    }
    
    // Request focus on search bar when screen opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun NetflixSearchBar(
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color.White.copy(alpha = 0.3f),
        animationSpec = tween(150)
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(Color(0xFFE50914)),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .focusable(interactionSource = interactionSource),
            decorationBox = { innerTextField ->
                Box {
                    if (searchQuery.text.isEmpty()) {
                        Text(
                            text = "Search for shows, movies, genres...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun NetflixSearchResultCard(
    content: Content,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Box(
        modifier = Modifier
            .aspectRatio(0.7f)
            .scale(scale)
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
    ) {
        AsyncImage(
            model = content.verticalPoster,
            contentDescription = content.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Gradient overlay when focused
        AnimatedVisibility(
            visible = isFocused,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0.6f
                        )
                    )
            ) {
                Text(
                    text = content.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun PopularSearches(
    onSearchTermClick: (String) -> Unit
) {
    val popularTerms = listOf(
        "Action Movies",
        "Comedy Shows",
        "New Releases",
        "Trending Now",
        "Award Winners",
        "Documentaries",
        "Kids & Family",
        "Anime"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
    ) {
        Text(
            text = "Popular Searches",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(popularTerms.size) { index ->
                PopularSearchItem(
                    term = popularTerms[index],
                    onClick = { onSearchTermClick(popularTerms[index]) }
                )
            }
        }
    }
}

@Composable
fun PopularSearchItem(
    term: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
        animationSpec = tween(150)
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = term,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = if (isFocused) FontWeight.Medium else FontWeight.Normal
        )
    }
}

