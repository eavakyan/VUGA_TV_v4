package com.vugaenterprises.androidtv.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.vugaenterprises.androidtv.data.VideoPlayerDataStore
import com.vugaenterprises.androidtv.data.EpisodeDataStore
import com.vugaenterprises.androidtv.data.CastDetailDataStore
import com.vugaenterprises.androidtv.data.UserDataStore
import com.vugaenterprises.androidtv.ui.screens.*
import com.vugaenterprises.androidtv.ui.viewmodels.ContentDetailViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    videoPlayerDataStore: VideoPlayerDataStore,
    episodeDataStore: EpisodeDataStore,
    castDetailDataStore: CastDetailDataStore,
    userDataStore: UserDataStore,
    startDestination: String = Screen.Home.route
) {
    var shouldFocusNavBar by remember { mutableStateOf(false) }
    
    // Wrap the entire navigation in MainScreen for Netflix-style navigation
    MainScreen(
        navController = navController,
        userDataStore = userDataStore,
        shouldFocusNavBar = shouldFocusNavBar,
        onNavBarFocusHandled = { shouldFocusNavBar = false }
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.Home.route) {
                val homeViewModel = hiltViewModel<com.vugaenterprises.androidtv.ui.viewmodels.HomeViewModel>()
                
                HomeScreen(
                    onContentClick = { content ->
                        navController.navigate(Screen.ContentDetail.createRoute(content.contentId))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onRequestNavBarFocus = {
                        android.util.Log.d("AppNavigation", "HomeScreen requested nav bar focus")
                        shouldFocusNavBar = true
                    },
                    viewModel = homeViewModel
                )
            }
            
            composable(Screen.Search.route) {
                // Use the new Netflix-style search screen
                NetflixSearchScreen(
                    onContentClick = { content ->
                        navController.navigate(Screen.ContentDetail.createRoute(content.contentId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onContentClick = { content ->
                        navController.navigate(Screen.ContentDetail.createRoute(content.contentId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSwitchProfile = {
                        // Navigate to profile selection
                        navController.navigate(Screen.ProfileSelection.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onLogout = {
                        // Navigate to login screen and clear back stack
                        navController.navigate(Screen.QRCodeAuth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    userDataStore = userDataStore
                )
            }
            
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onContentClick = { content ->
                        navController.navigate(Screen.ContentDetail.createRoute(content.contentId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.History.route) {
                HistoryScreen(
                    onContentClick = { content ->
                        navController.navigate(Screen.ContentDetail.createRoute(content.contentId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Screen.ContentDetail.route,
                arguments = Screen.ContentDetail.arguments
            ) { backStackEntry ->
                val contentId = backStackEntry.arguments?.getInt("contentId") ?: 0
                val contentDetailViewModel: ContentDetailViewModel = hiltViewModel()
                val contentDetailState by contentDetailViewModel.uiState.collectAsState()
                
                // Get home view model to refresh watchlist
                val homeViewModel = hiltViewModel<com.vugaenterprises.androidtv.ui.viewmodels.HomeViewModel>(
                    remember { navController.getBackStackEntry(Screen.Home.route) }
                )
                
                LaunchedEffect(contentId) {
                    contentDetailViewModel.loadContent(contentId)
                }
                
                // Use Compose Content Detail Screen
                ContentDetailScreen(
                    contentId = contentId,
                    onContentClick = { content ->
                        navController.navigate(Screen.ContentDetail.createRoute(content.contentId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onPlayVideo = { content ->
                        // Direct play for movies
                        videoPlayerDataStore.setCurrentContent(content)
                        navController.navigate(Screen.VideoPlayer.createRoute())
                    },
                    onWatchlistChanged = {
                        // Refresh the home screen watchlist when watchlist changes
                        homeViewModel.refreshWatchlist()
                    }
                )
            }
            
            composable(
                route = Screen.EpisodeSelection.route,
                arguments = Screen.EpisodeSelection.arguments
            ) { backStackEntry ->
                val contentId = backStackEntry.arguments?.getInt("contentId") ?: 0
                EpisodeSelectionScreen(
                    contentId = contentId,
                    onEpisodeClick = { episode ->
                        // Store the selected episode and navigate to video player
                        episodeDataStore.setSelectedEpisode(episode)
                        navController.navigate(Screen.VideoPlayer.createRoute())
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.VideoPlayer.route) {
                val content = videoPlayerDataStore.getCurrentContent()
                val episode = episodeDataStore.selectedEpisode.value
                
                if (content != null || episode != null) {
                    VideoPlayerScreen(
                        content = content,
                        episode = episode,
                        onNavigateBack = {
                            videoPlayerDataStore.clearCurrentContent()
                            episodeDataStore.clearSelectedEpisode()
                            navController.popBackStack()
                        }
                    )
                } else {
                    // Fallback for no content
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No content to play")
                    }
                }
            }
            
            composable(
                route = Screen.ContentInfo.route,
                arguments = Screen.ContentInfo.arguments
            ) { backStackEntry ->
                val contentId = backStackEntry.arguments?.getInt("contentId") ?: 0
                val contentDetailViewModel: ContentDetailViewModel = hiltViewModel()
                val contentDetailState by contentDetailViewModel.uiState.collectAsState()
                
                LaunchedEffect(contentId) {
                    contentDetailViewModel.loadContent(contentId)
                }
                
                // Use Android View for the info page
                AndroidView(
                    factory = { context ->
                        ContentInfoView(context).apply {
                            setOnBackClick {
                                navController.popBackStack()
                            }
                            setOnCastMemberClick { castMember ->
                                // Store the cast member data
                                castDetailDataStore.setCurrentCastMember(castMember, contentDetailState.content?.moreLikeThis ?: emptyList())
                                navController.navigate(Screen.CastDetail.createRoute(castMember.actor.id, castMember.characterName))
                            }
                        }
                    },
                    update = { contentInfoView ->
                        contentDetailState.content?.let { content ->
                            contentInfoView.setContent(content)
                        }
                    }
                )
            }
            
            composable(
                route = Screen.CastDetail.route,
                arguments = Screen.CastDetail.arguments
            ) { backStackEntry ->
                val castMember by castDetailDataStore.currentCastMember.collectAsState()
                val relatedContent by castDetailDataStore.relatedContent.collectAsState()
                
                AndroidView(
                    factory = { context ->
                        CastDetailView(context).apply {
                            setOnBackClick {
                                castDetailDataStore.clearCurrentCastMember()
                                navController.popBackStack()
                            }
                            setOnContentClick { content ->
                                navController.navigate(Screen.ContentDetail.createRoute(content.contentId))
                            }
                        }
                    },
                    update = { castDetailView ->
                        castMember?.let { member ->
                            castDetailView.setCastMember(member, relatedContent)
                        }
                    }
                )
            }
            
            composable(Screen.QRCodeAuth.route) {
                QRCodeAuthScreen(
                    onAuthenticationSuccess = {
                        // Navigate to profile selection after successful authentication
                        navController.navigate(Screen.ProfileSelection.route) {
                            popUpTo(Screen.QRCodeAuth.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.ProfileSelection.route) {
                ProfileSelectionScreen(
                    onProfileSelected = {
                        // Navigate to home after profile selection
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.ProfileSelection.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.LiveTV.route) {
                LiveTVScreen(
                    onChannelClick = { channel ->
                        navController.navigate(Screen.LiveTVPlayer.createRoute(channel.id))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Screen.LiveTVPlayer.route,
                arguments = Screen.LiveTVPlayer.arguments
            ) { backStackEntry ->
                val channelId = backStackEntry.arguments?.getInt("channelId") ?: 0
                LiveTVPlayerScreen(
                    channelId = channelId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

