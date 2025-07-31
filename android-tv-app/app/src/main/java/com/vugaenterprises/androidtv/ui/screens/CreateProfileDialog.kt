package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vugaenterprises.androidtv.data.model.Profile
import com.vugaenterprises.androidtv.data.model.ProfileColors
import com.vugaenterprises.androidtv.ui.components.TVButton

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateProfileDialog(
    profile: Profile? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, avatarId: Int, isKids: Boolean) -> Unit
) {
    var profileName by remember { mutableStateOf(profile?.name ?: "") }
    var selectedAvatarId by remember { mutableStateOf(profile?.avatarId ?: 1) }
    var isKidsProfile by remember { mutableStateOf(profile?.isKids ?: false) }
    
    val nameFocusRequester = remember { FocusRequester() }
    var isNameFieldFocused by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = if (profile != null) "Edit Profile" else "Create Profile",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 40.dp)
                )
                
                // Profile Name Input
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Profile Name",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    BasicTextField(
                        value = profileName,
                        onValueChange = { profileName = it.take(20) }, // Limit to 20 characters
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(60.dp)
                            .background(
                                color = Color(0xFF2A2A2A),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = if (isNameFieldFocused) Color.White else Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 20.dp)
                            .focusRequester(nameFocusRequester)
                            .onFocusChanged { isNameFieldFocused = it.isFocused }
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionDown) {
                                    false // Let focus move to avatar selection
                                } else false
                            },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Color.White)
                    )
                }
                
                // Avatar Selection
                Text(
                    text = "Choose Avatar Color",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    itemsIndexed(ProfileColors.colors) { index, color ->
                        AvatarColorOption(
                            avatarId = index + 1,
                            color = color,
                            initial = profileName.take(1).uppercase().ifEmpty { "?" },
                            isSelected = selectedAvatarId == index + 1,
                            onSelect = { selectedAvatarId = index + 1 }
                        )
                    }
                }
                
                // Kids Profile Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KidsProfileToggle(
                        isKids = isKidsProfile,
                        onToggle = { isKidsProfile = !isKidsProfile }
                    )
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
                ) {
                    TVButton(
                        text = "Cancel",
                        onClick = onDismiss
                    )
                    
                    TVButton(
                        text = if (profile != null) "Save" else "Create",
                        onClick = {
                            if (profileName.isNotBlank()) {
                                onConfirm(profileName, selectedAvatarId, isKidsProfile)
                            }
                        }
                    )
                }
            }
        }
    }
    
    // Request focus on name field when dialog opens
    LaunchedEffect(Unit) {
        nameFocusRequester.requestFocus()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AvatarColorOption(
    avatarId: Int,
    color: String,
    initial: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                Color(android.graphics.Color.parseColor(
                    color.let { hex ->
                        if (hex.startsWith("#")) hex else "#$hex"
                    }
                ))
            )
            .border(
                width = if (isSelected) 4.dp else if (isFocused) 2.dp else 0.dp,
                color = if (isSelected) Color.White else if (isFocused) Color.White.copy(alpha = 0.7f) else Color.Transparent,
                shape = CircleShape
            )
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onSelect() }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && 
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)) {
                    onSelect()
                    true
                } else false
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KidsProfileToggle(
    isKids: Boolean,
    onToggle: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onToggle() }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && 
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)) {
                    onToggle()
                    true
                } else false
            }
            .background(
                color = if (isFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Checkbox(
            checked = isKids,
            onCheckedChange = null, // Handled by row click
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFFFFB800),
                uncheckedColor = Color.White.copy(alpha = 0.5f),
                checkmarkColor = Color.Black
            )
        )
        
        Text(
            text = "Kids Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        
        if (isKids) {
            Box(
                modifier = Modifier
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
}