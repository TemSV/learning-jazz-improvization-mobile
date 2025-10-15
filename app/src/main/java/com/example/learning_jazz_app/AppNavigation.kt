package com.example.learning_jazz_app

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.learning_jazz_app.ui.songdetail.SongDetailScreen
import com.example.learning_jazz_app.ui.songs.SongsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "songs") {
        composable("songs") {
            SongsScreen(
                onSongClick = { songId, songTitle, songComposer ->
                    navController.navigate("song_detail/$songId/$songTitle/$songComposer")
                }
            )
        }
        composable(
            route = "song_detail/{songId}/{songTitle}/{songComposer}",
            arguments = listOf(
                navArgument("songId") { type = NavType.IntType },
                navArgument("songTitle") { type = NavType.StringType },
                navArgument("songComposer") { type = NavType.StringType }
            )
        ) {
            SongDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
} 