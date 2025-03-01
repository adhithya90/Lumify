package com.example.lumify

import android.os.Build
import com.example.lumify.ui.screens.GalleryScreen
import com.example.lumify.ui.screens.GalleryViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold


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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lumify.data.repository.MediaRepository
import com.example.lumify.ui.components.BottomNavItem
import com.example.lumify.ui.components.LumifyBottomNavigation
import com.example.lumify.ui.navigation.LumifyNavGraph
import com.example.lumify.ui.screens.DetailScreen
import com.example.lumify.ui.theme.LumifyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mediaRepository: MediaRepository

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge with transparent status bar
        enableEdgeToEdge()

        setContent {
            LumifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black // Dark theme base color
                ) {
                    LumifyApp(mediaRepository)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun LumifyApp(mediaRepository: MediaRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Track if we need to refresh gallery after permission change
    var permissionJustGranted by remember { mutableStateOf(false) }

    // Storage permission handling
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Required permissions based on Android version
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    // Get current route for bottom navigation selection
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavItem.HOME.route

    // Check if we're on camera screen
    val isOnCameraScreen = currentRoute == "camera"

    // Only show bottom navigation when not on the camera screen
    val showBottomBar = !isOnCameraScreen

    Scaffold(
        containerColor = Color.Black, // Black background for the entire app
        bottomBar = {
            if (showBottomBar) {
                LumifyBottomNavigation(
                    currentRoute = currentRoute,
                    onNavItemClick = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // Apply padding only if we're not on the camera screen
        val contentModifier = if (showBottomBar) {
            Modifier.padding(innerPadding)
        } else {
            Modifier
        }

        // Use the LumifyNavGraph with appropriate modifier
        LumifyNavGraph(
            navController = navController,
            mediaRepository = mediaRepository,
            modifier = contentModifier
        )
    }
}