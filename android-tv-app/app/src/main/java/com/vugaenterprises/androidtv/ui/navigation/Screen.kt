package com.vugaenterprises.androidtv.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Profile : Screen("profile")
    object History : Screen("history")
    object Favorites : Screen("favorites")
    
    object ContentDetail : Screen("content_detail/{contentId}") {
        val arguments = listOf(
            navArgument("contentId") {
                type = NavType.IntType
            }
        )
        
        fun createRoute(contentId: Int) = "content_detail/$contentId"
    }
    
    object VideoPlayer : Screen("video_player") {
        fun createRoute() = "video_player"
    }
    
    object EpisodeSelection : Screen("episode_selection/{contentId}") {
        val arguments = listOf(
            navArgument("contentId") {
                type = NavType.IntType
            }
        )
        
        fun createRoute(contentId: Int) = "episode_selection/$contentId"
    }
    
    object ContentInfo : Screen("content_info/{contentId}") {
        val arguments = listOf(
            navArgument("contentId") {
                type = NavType.IntType
            }
        )
        
        fun createRoute(contentId: Int) = "content_info/$contentId"
    }
    
    object CastDetail : Screen("cast_detail/{actorId}/{characterName}") {
        val arguments = listOf(
            navArgument("actorId") {
                type = NavType.IntType
            },
            navArgument("characterName") {
                type = NavType.StringType
                nullable = true
            }
        )
        
        fun createRoute(actorId: Int, characterName: String = "") = "cast_detail/$actorId/$characterName"
    }
    
    object QRCodeAuth : Screen("qr_code_auth") {
        fun createRoute() = "qr_code_auth"
    }
    
    object ProfileSelection : Screen("profile_selection") {
        fun createRoute() = "profile_selection"
    }
} 