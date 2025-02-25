package com.example.lumify

import com.example.lumify.ui.screens.GalleryScreen
import com.example.lumify.ui.screens.GalleryViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme


import androidx.compose.material3.Surface
import androidx.compose.material3.Text



import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lumify.data.repository.MediaRepository
import com.example.lumify.ui.screens.DetailScreen
import com.example.lumify.ui.theme.LumifyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mediaRepository: MediaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LumifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LumifyApp(mediaRepository)
                }
            }
        }
    }
}

@Composable
fun LumifyApp(mediaRepository: MediaRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Track if we need to refresh gallery after permission change
    var permissionJustGranted by remember { mutableStateOf(false) }

    // Storage permission handling
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Required permissions based on Android version
    val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        showPermissionDialog = !allGranted

        // If permissions were just granted, set flag to refresh gallery
        if (allGranted) {
            permissionJustGranted = true
        }
    }

    // Check permission on start
    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions)
    }

    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Storage Permission Required") },
            text = {
                Text("Lumify needs access to your photos to display them in the gallery.")
            },
            confirmButton = {
                Button(onClick = {
                    permissionLauncher.launch(permissions)
                }) {
                    Text("Grant Permission")
                }
            }
        )
    }

    // Navigation
    NavHost(
        navController = navController,
        startDestination = "gallery"
    ) {
        composable("gallery") {
            val galleryViewModel = hiltViewModel<GalleryViewModel>()

            // If permissions were just granted, trigger a reload
            LaunchedEffect(permissionJustGranted) {
                if (permissionJustGranted) {
                    galleryViewModel.loadMediaItems()
                    permissionJustGranted = false
                }
            }

            GalleryScreen(
                viewModel = galleryViewModel,
                onPhotoClick = { mediaItem ->
                    navController.navigate("detail/${mediaItem.id}")
                }
            )
        }

        composable(
            route = "detail/{mediaId}",
            arguments = listOf(
                navArgument("mediaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId") ?: ""

            DetailScreen(
                mediaId = mediaId,
                mediaRepository = mediaRepository,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}