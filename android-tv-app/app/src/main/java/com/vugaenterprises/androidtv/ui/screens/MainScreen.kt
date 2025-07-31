package com.vugaenterprises.androidtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vugaenterprises.androidtv.data.UserDataStore
import com.vugaenterprises.androidtv.ui.components.NavigationItem
import com.vugaenterprises.androidtv.ui.components.NetflixNavigationBar
import com.vugaenterprises.androidtv.ui.navigation.Screen

@Composable
fun MainScreen(
    navController: NavHostController,
    userDataStore: UserDataStore,
    shouldFocusNavBar: Boolean = false,
    onNavBarFocusHandled: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var selectedNavItem by remember { mutableStateOf("watch") }
    val focusManager = LocalFocusManager.current
    val navBarFocusRequester = remember { FocusRequester() }
    
    // Observe login state and profile
    val isLoggedIn by userDataStore.isLoggedIn().collectAsState(initial = false)
    val userData by userDataStore.getUserData().collectAsState(initial = null)
    val selectedProfile by userDataStore.getSelectedProfile().collectAsState(initial = null)
    
    val navigationItems = remember(isLoggedIn, userData) {
        listOf(
            NavigationItem(
                id = "watch", 
                title = "Watch",
                subItems = listOf(
                    NavigationItem("movies", "Movies"),
                    NavigationItem("tv_shows", "TV Shows"),
                    NavigationItem("cartoons", "Cartoons"),
                    NavigationItem("anime", "Anime"),
                    NavigationItem("hbo", "HBO"),
                    NavigationItem("cinemax", "Cinemax")
                )
            ),
            NavigationItem("search", "Search"),
            NavigationItem("tv", "TV"),
            NavigationItem(
                id = if (isLoggedIn) "profile" else "login",
                title = if (isLoggedIn) userData?.fullname?.split(" ")?.firstOrNull() ?: "Profile" else "Log In"
            )
        )
    }
    
    // Handle navigation item selection
    LaunchedEffect(selectedNavItem) {
        when (selectedNavItem) {
            "watch" -> {
                if (navController.currentDestination?.route != Screen.Home.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
            "search" -> {
                navController.navigate(Screen.Search.route) {
                    popUpTo(Screen.Home.route)
                }
            }
            "tv" -> {
                // For now, navigate to home or create a TV-specific screen
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
            "login" -> {
                // Navigate to QR code authentication screen
                navController.navigate(Screen.QRCodeAuth.route) {
                    popUpTo(Screen.Home.route)
                }
            }
            "profile" -> {
                // Navigate to profile screen when logged in
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Home.route)
                }
            }
        }
    }
    
    // Handle focus request
    LaunchedEffect(shouldFocusNavBar) {
        if (shouldFocusNavBar) {
            android.util.Log.d("MainScreen", "Requesting focus on navigation bar")
            navBarFocusRequester.requestFocus()
            onNavBarFocusHandled()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionDown -> {
                        // Focus content when pressing down
                        focusManager.moveFocus(FocusDirection.Down)
                        true
                    }
                    else -> false
                }
            }
    ) {
        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp) // Space for navigation bar
        ) {
            content()
        }
        
        // Netflix-style navigation bar (overlaid on top)
        NetflixNavigationBar(
            navigationItems = navigationItems,
            selectedItemId = selectedNavItem,
            onItemSelected = { item ->
                selectedNavItem = item.id
            },
            currentProfile = selectedProfile,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .focusRequester(navBarFocusRequester)
                .onFocusChanged { focusState ->
                    android.util.Log.d("MainScreen", "Navigation bar focus changed: ${focusState.isFocused}")
                }
        )
    }
}

