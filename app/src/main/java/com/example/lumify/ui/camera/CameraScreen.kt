package com.example.lumify.ui.camera

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lumify.ui.theme.LumifyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onImageCaptured: (String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // State to track if permission has just been granted
    var permissionJustGranted by remember { mutableStateOf(false) }

    // Permission handling using Accompanist
    CameraPermissionScreen(
        onPermissionGranted = {
            // Set permissionJustGranted to true when permission is granted
            permissionJustGranted = true
        }
    )

    // Collect states from ViewModel
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val captureState by viewModel.captureState.collectAsStateWithLifecycle()
    val flashMode by viewModel.flashMode.collectAsStateWithLifecycle()

    // For tap-to-focus visual indicator
    var autofocusRequest by remember { mutableStateOf(UUID.randomUUID() to Offset.Unspecified) }
    val autofocusRequestId = autofocusRequest.first
    val showAutofocusIndicator = autofocusRequest.second != Offset.Unspecified
    val autofocusCoords = remember(autofocusRequestId) { autofocusRequest.second }

    // Bind to camera when composable enters the composition tree or permissions are granted
    LaunchedEffect(permissionJustGranted) {
        if (permissionJustGranted && hasCameraPermissions(context)) {
            viewModel.bindToCamera(context, lifecycleOwner)
        }
    }

    // Also try to bind on initial composition if permissions are already granted
    LaunchedEffect(Unit) {
        if (hasCameraPermissions(context)) {
            viewModel.bindToCamera(context, lifecycleOwner)
        }
    }

    // Hide focus indicator after delay
    if (showAutofocusIndicator) {
        LaunchedEffect(autofocusRequestId) {
            delay(1000)
            autofocusRequest = autofocusRequestId to Offset.Unspecified
        }
    }

    // Handle capture state changes
    LaunchedEffect(captureState) {
        when (captureState) {
            is CameraViewModel.CaptureState.Success -> {
                val uri = (captureState as CameraViewModel.CaptureState.Success).uri
                onImageCaptured(uri.toString())
                viewModel.resetCaptureState()
            }
            is CameraViewModel.CaptureState.Error -> {
                val errorMessage = (captureState as CameraViewModel.CaptureState.Error).message
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Failed to save photo: $errorMessage")
                }
                viewModel.resetCaptureState()
            }
            else -> { /* No action needed */ }
        }
    }

    // Only show camera UI if permissions are granted
    if (!hasCameraPermissions(context)) {
        return
    }

    // Use Box directly for edge-to-edge experience
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Camera Preview
        surfaceRequest?.let { request ->
            val coordinateTransformer = remember { androidx.camera.viewfinder.compose.MutableCoordinateTransformer() }

            androidx.camera.compose.CameraXViewfinder(
                surfaceRequest = request,
                coordinateTransformer = coordinateTransformer,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tapCoords ->
                            with(coordinateTransformer) {
                                viewModel.tapToFocus(tapCoords.transform())
                            }
                            autofocusRequest = UUID.randomUUID() to tapCoords
                        }
                    }
            )

            // Tap to focus indicator
            AnimatedVisibility(
                visible = showAutofocusIndicator,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.offset {
                    IntOffset(
                        x = (autofocusCoords.x - 24.dp.toPx()).roundToInt(),
                        y = (autofocusCoords.y - 24.dp.toPx()).roundToInt()
                    )
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        } ?: run {
            // Show loading while camera initializes
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // Camera Controls with insets padding for status bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Top bar with close and flash buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleFlash() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    val flashIcon = when (flashMode) {
                        androidx.camera.core.ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                        androidx.camera.core.ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                        else -> Icons.Default.FlashAuto
                    }

                    Icon(
                        imageVector = flashIcon,
                        contentDescription = "Toggle Flash",
                        tint = Color.White
                    )
                }
            }

            // Bottom bar with capture and switch camera buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Spacer on the left for alignment
                Spacer(modifier = Modifier.weight(1f))

                // Capture button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (captureState is CameraViewModel.CaptureState.Capturing)
                                Color.LightGray
                            else
                                Color.White
                        )
                        .clickable(
                            enabled = captureState !is CameraViewModel.CaptureState.Capturing
                        ) {
                            viewModel.captureImage(context, context.mainExecutor)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (captureState is CameraViewModel.CaptureState.Capturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color.DarkGray,
                            strokeWidth = 3.dp
                        )
                    }
                }

                // Switch camera button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.toggleCamera() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Snackbar host for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    LumifyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            // Top controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = Color.White
                    )
                }
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Capture button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )

                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}