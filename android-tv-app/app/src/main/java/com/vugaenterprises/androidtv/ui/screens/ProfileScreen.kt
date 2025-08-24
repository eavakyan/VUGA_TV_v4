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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.ProfileColors
import com.vugaenterprises.androidtv.data.UserDataStore
import com.vugaenterprises.androidtv.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onContentClick: (Content) -> Unit,
    onNavigateBack: () -> Unit,
    onSwitchProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    userDataStore: UserDataStore,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userData by userDataStore.getUserData().collectAsState(initial = null)
    val selectedProfile by userDataStore.getSelectedProfile().collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(48.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            // Profile Icon
            val currentProfile = selectedProfile
            val hasCustomImage = currentProfile?.avatarType == "custom" && 
                                !currentProfile.avatarUrl.isNullOrBlank() && 
                                currentProfile.avatarUrl != "null" &&
                                (currentProfile.avatarUrl.startsWith("http://") || currentProfile.avatarUrl.startsWith("https://"))
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (hasCustomImage) {
                    // Show custom uploaded image
                    androidx.compose.foundation.Image(
                        painter = coil.compose.rememberAsyncImagePainter(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(currentProfile!!.avatarUrl)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Profile image",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Show color avatar with initials
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (currentProfile != null) {
                                    val colorHex = currentProfile.avatarColor ?: ProfileColors.getColorForId(currentProfile.avatarId ?: 1)
                                    val cleanHex = colorHex.removePrefix("#")
                                    Color(android.graphics.Color.parseColor("#$cleanHex"))
                                } else {
                                    Color(0xFF333333)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Generate initials (matching ProfileSelectionScreen logic)
                        val initials = if (currentProfile != null) {
                            generateInitials(currentProfile.name)
                        } else {
                            userData?.fullname?.firstOrNull()?.toString()?.uppercase() ?: "U"
                        }
                        Text(
                            text = initials,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Profile Name
            Text(
                text = selectedProfile?.name ?: userData?.fullname ?: "User",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // User Email
            Text(
                text = userData?.email ?: "",
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            // Premium Badge
            if (userData?.isPremium == true) {
                Card(
                    modifier = Modifier.padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "PREMIUM",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons - Horizontal Layout
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch Profile Button
                Button(
                    onClick = onSwitchProfile,
                    modifier = Modifier
                        .width(200.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF464646)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Switch Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Sign Out Button
                Button(
                    onClick = {
                        scope.launch {
                            userDataStore.clearUserData()
                            userDataStore.clearSelectedProfile()
                            onLogout()
                        }
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE50914)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Sign Out",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Back Button - Below the other buttons
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Back",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Helper function to generate initials (matching ProfileSelectionScreen logic)
private fun generateInitials(name: String): String {
    if (name.isBlank()) return "P"
    
    val words = name.trim().split("\\s+".toRegex())
    return when {
        words.isEmpty() -> "P"
        words.size == 1 -> words[0].take(1).uppercase()
        else -> {
            // Take first letter of first two words
            val first = words[0].take(1).uppercase()
            val second = words[1].take(1).uppercase()
            first + second
        }
    }
}