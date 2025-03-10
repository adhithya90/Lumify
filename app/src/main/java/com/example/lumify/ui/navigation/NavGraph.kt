package com.example.lumify.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lumify.data.repository.MediaRepository
import com.example.lumify.ui.camera.CameraScreen
import com.example.lumify.ui.screens.DetailScreen
import com.example.lumify.ui.screens.EditedImagesScreen
import com.example.lumify.ui.screens.GalleryScreen

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun LumifyNavGraph(
    navController: NavHostController,
    mediaRepository: MediaRepository,
    modifier: Modifier = Modifier,
    startDestination: String = "gallery"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home (Gallery) screen
        composable("gallery") {
            GalleryScreen(
                onPhotoClick = { mediaItem ->
                    navController.navigate("detail/${mediaItem.id}")
                }
            )
        }

        // Edited Images screen
        composable("edited") {
            EditedImagesScreen(
                onPhotoClick = { mediaItem ->
                    navController.navigate("detail/${mediaItem.id}")
                }
            )
        }

        // Detail screen
        composable(
            route = "detail/{mediaId}",
            arguments = listOf(
                navArgument("mediaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId") ?: ""

            DetailScreen(
                mediaId = mediaId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Camera screen
        composable("camera") {
            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onImageCaptured = { uri ->
                    // Navigate back to gallery after capture
                    navController.popBackStack()
                    // Refresh media items to show newly captured photo
                    mediaRepository.refreshMediaItems()
                }
            )
        }
    }
}