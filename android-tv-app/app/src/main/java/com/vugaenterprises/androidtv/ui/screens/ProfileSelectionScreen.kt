package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vugaenterprises.androidtv.data.model.Profile
import com.vugaenterprises.androidtv.data.model.ProfileColors
import com.vugaenterprises.androidtv.ui.components.TVOutlinedButton
import com.vugaenterprises.androidtv.ui.viewmodels.ProfileSelectionViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileSelectionScreen(
    onProfileSelected: () -> Unit,
    viewModel: ProfileSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var profileToEdit by remember { mutableStateOf<Profile?>(null) }
    
    // Navigate when profile is selected
    LaunchedEffect(uiState.isSelectionComplete) {
        if (uiState.isSelectionComplete) {
            delay(300) // Brief delay for visual feedback
            onProfileSelected()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 80.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Who's watching?",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 60.dp)
            )
            
            // Profiles Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(40.dp),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    items(uiState.profiles) { profile ->
                        ProfileItem(
                            profile = profile,
                            isEditMode = uiState.isEditMode,
                            onSelect = {
                                if (uiState.isEditMode) {
                                    profileToEdit = profile
                                    showCreateDialog = true
                                } else {
                                    viewModel.selectProfile(profile)
                                }
                            },
                            onDelete = {
                                viewModel.deleteProfile(profile)
                            }
                        )
                    }
                    
                    // Add Profile button
                    if (uiState.profiles.size < 4 && !uiState.isEditMode) {
                        item {
                            AddProfileButton(
                                onClick = {
                                    profileToEdit = null
                                    showCreateDialog = true
                                }
                            )
                        }
                    }
                }
            }
            
            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TVOutlinedButton(
                    text = if (uiState.isEditMode) "Done" else "Manage Profiles",
                    onClick = { viewModel.toggleEditMode() }
                )
            }
        }
        
        // Loading overlay
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        
        // Error dialog
        uiState.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = {
                    Text(
                        text = "Error",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = error,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 18.sp
                    )
                },
                confirmButton = {
                    TVOutlinedButton(
                        text = "OK",
                        onClick = { viewModel.clearError() }
                    )
                },
                containerColor = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
    
    // Create/Edit Profile Dialog
    if (showCreateDialog) {
        CreateProfileDialog(
            profile = profileToEdit,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, avatarId, isKids ->
                if (profileToEdit != null) {
                    // Edit functionality would go here
                } else {
                    viewModel.createProfile(name, avatarId, isKids)
                }
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileItem(
    profile: Profile,
    isEditMode: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onSelect() }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionCenter, Key.Enter -> {
                            onSelect()
                            true
                        }
                        Key.Delete -> {
                            if (isEditMode) {
                                onDelete()
                                true
                            } else false
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Profile Avatar
            val colorHex = ProfileColors.getColorForId(profile.avatarId ?: 1)
            val cleanHex = colorHex.removePrefix("#")
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor("#$cleanHex")))
                    .then(
                        if (isFocused) {
                            Modifier.border(
                                width = 4.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.initial,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Edit mode overlay
                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )
                    
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // Kids badge
            if (profile.isKidsProfile) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 10.dp)
                        .background(
                            color = Color(0xFFFFB800),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "KIDS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
        
        // Profile Name
        Text(
            text = profile.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddProfileButton(
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && 
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)) {
                    onClick()
                    true
                } else false
            }
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1A1A))
                .border(
                    width = if (isFocused) 4.dp else 2.dp,
                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Add,
                contentDescription = "Add Profile",
                tint = if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(72.dp)
            )
        }
        
        Text(
            text = "Add Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

