package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import com.vugaenterprises.androidtv.data.UserDataStore
import com.vugaenterprises.androidtv.data.model.AgeRating
import com.vugaenterprises.androidtv.data.model.Profile
import androidx.compose.material3.CircularProgressIndicator
import com.vugaenterprises.androidtv.ui.viewmodels.AgeSettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun AgeSettingsScreen(
    profile: Profile,
    onNavigateBack: () -> Unit,
    userDataStore: UserDataStore,
    viewModel: AgeSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
        viewModel.loadAgeRatings()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(48.dp)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Age Settings",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Card(
                            onClick = {
                                scope.launch {
                                    viewModel.saveAgeSettings()
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.focusRequester(focusRequester),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Save",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Profile Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Color(android.graphics.Color.parseColor(profile.avatarColor))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.initial,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(24.dp))
                        
                        Text(
                            text = profile.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // Kids Profile Toggle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Kids Profile",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Kids profiles can only access content for ages 12 and under",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            
                            Switch(
                                checked = uiState.isKidsProfile,
                                onCheckedChange = { viewModel.setKidsProfile(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50)
                                )
                            )
                        }
                    }
                    
                    // Age Selection (if not kids profile)
                    if (!uiState.isKidsProfile) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = "Age",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Set the age to filter content appropriately",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Simple age display - in a real app, this would be a focusable selector
                                Text(
                                    text = "Age: ${uiState.selectedAge ?: "Not Set"}",
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // Age Ratings List
                    if (uiState.ageRatings.isNotEmpty()) {
                        Text(
                            text = "Content Ratings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.ageRatings) { rating ->
                                AgeRatingItem(
                                    rating = rating,
                                    canAccess = viewModel.canAccessRating(rating)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun AgeRatingItem(
    rating: AgeRating,
    canAccess: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (canAccess) 
                Color.White.copy(alpha = 0.1f) 
            else 
                Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rating Badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(android.graphics.Color.parseColor(rating.displayColor))
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = rating.code.replace("AG_", ""),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Rating Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rating.name,
                    fontSize = 16.sp,
                    color = if (canAccess) Color.White else Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
                rating.description?.let { desc ->
                    Text(
                        text = desc,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = if (canAccess) 0.7f else 0.3f)
                    )
                }
            }
            
            // Access indicator
            Text(
                text = if (canAccess) "✓" else "🔒",
                fontSize = 20.sp,
                color = if (canAccess) Color.Green else Color.Red
            )
        }
    }
}