package com.example.lumify.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack


import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material.icons.filled.MoreVert

import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

import androidx.compose.material3.Text

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

import com.example.lumify.data.model.PhotoFilter
import com.example.lumify.data.model.PhotoFilters

import com.example.lumify.ui.theme.LumifyTheme
import kotlinx.coroutines.launch

/**
 * Immersive detail screen that displays a photo edge-to-edge with subtle overlay controls
 * and a transparent filter strip at the bottom
 */
@Composable
fun DetailScreen(
    mediaId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val mediaItem by viewModel.mediaItem.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val filterProcessState by viewModel.filterProcessState.collectAsStateWithLifecycle()

    // UI state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSaveDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(PhotoFilters.ORIGINAL) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Load the media item
    LaunchedEffect(mediaId) {
        viewModel.loadMediaItem(mediaId)
    }

    // Handle filter process state changes
    LaunchedEffect(filterProcessState) {
        when (filterProcessState) {
            is DetailViewModel.FilterProcessState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Photo saved successfully")
                }
                viewModel.resetFilterProcessState()
                showSaveDialog = false
            }

            is DetailViewModel.FilterProcessState.Error -> {
                errorMessage =
                    (filterProcessState as DetailViewModel.FilterProcessState.Error).message
                showErrorDialog = true
                viewModel.resetFilterProcessState()
            }

            else -> { /* No action needed */
            }
        }
    }

    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Save options dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Photo") },
            text = { Text("How would you like to save your filtered photo?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyFilter(selectedFilter, false)
                    }
                ) {
                    Text("Replace Original")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.applyFilter(selectedFilter, true)
                    }
                ) {
                    Text("Save as Copy")
                }
            }
        )
    }

    // Fully immersive edge-to-edge layout
    Box(
        modifier = modifier
            .padding(WindowInsets.statusBars.asPaddingValues())
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(42.dp)
            )
        } else if (error != null) {
            Text(
                text = error ?: "Unknown error",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            val currentMediaItem = mediaItem
            if (currentMediaItem != null) {
                // Full screen image with filter applied
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentMediaItem.uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    loading = {
                        CircularProgressIndicator(color = Color.White)
                    },
                    colorFilter = selectedFilter.colorFilter,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay controls - minimal UI
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top action bar - minimal with just icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Back button - no background
                        IconButton(
                            onClick = onBackClick,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Heart icon
                        IconButton(onClick = { /* Future implementation */ }) {
                            Icon(
                                imageVector = Icons.Outlined.Favorite,
                                tint = Color.White,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Share button
                        IconButton(onClick = { /* Future implementation */ }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Three dots menu
                        IconButton(onClick = { /* Future implementation */ }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Bottom section with filter thumbnails
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(vertical = 12.dp)
                    ) {
                        ImmersiveFiltersRow(
                            imageUri = currentMediaItem.uri.toString(),
                            selectedFilter = selectedFilter,
                            onFilterSelected = { filter ->
                                selectedFilter = filter
                            }
                        )
                    }

                    // Processing overlay
                    AnimatedVisibility(
                        visible = filterProcessState is DetailViewModel.FilterProcessState.Processing,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = Color.White)
                                Text(
                                    text = "Applying filter...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                        }
                    }
                }

                // Snackbar host
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp) // Position above the filter strip
                )
            } else {
                Text(
                    text = "Photo not found",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ImmersiveFiltersRow(
    imageUri: String,
    selectedFilter: PhotoFilter,
    onFilterSelected: (PhotoFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Find the selected filter index for initial scroll
    val selectedFilterIndex = PhotoFilters.FILTERS.indexOf(selectedFilter).coerceAtLeast(0)

    // Scroll to selected filter when it changes
    LaunchedEffect(selectedFilterIndex) {
        if (selectedFilterIndex > 0) {
            listState.animateScrollToItem(selectedFilterIndex)
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(8.dp, 24.dp, 8.dp, 24.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(PhotoFilters.FILTERS) { filter ->
            ImmersiveFilterThumbnail(
                filter = filter,
                imageUri = imageUri,
                isSelected = filter.id == selectedFilter.id,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
fun ImmersiveFilterThumbnail(
    filter: PhotoFilter,
    imageUri: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(92.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(84.dp)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(2.dp)
        ) {
            // Thumbnail image with filter applied
            val imageRequest = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .build()

            AsyncImage(
                model = imageRequest,
                contentDescription = filter.name,
                contentScale = ContentScale.Crop,
                colorFilter = filter.colorFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(2.dp))
            )

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Filter name
        Text(
            text = filter.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 10.sp
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ImmersiveDetailScreenPreview() {
    LumifyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Simulate the image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            )

            // Top controls - more minimal like in reference image
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Bottom filter strip
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(vertical = 12.dp)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(5) { index ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(92.dp)
                                .padding(horizontal = 4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(84.dp)
                                    .border(
                                        width = if (index == 0) 2.dp else 0.dp,
                                        color = if (index == 0) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(Color.Gray.copy(alpha = 0.3f))
                            ) {
                                if (index == 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(24.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.5f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (index == 0) "Original" else "Filter ${index + 1}",
                                color = Color.White,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}