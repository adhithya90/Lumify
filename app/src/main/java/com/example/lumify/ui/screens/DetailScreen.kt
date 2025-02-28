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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.lumify.data.model.MediaItem
import com.example.lumify.data.model.PhotoFilter
import com.example.lumify.data.model.PhotoFilters
import com.example.lumify.data.repository.MediaRepository
import com.example.lumify.ui.components.PhotoFilterBottomSheet
import com.example.lumify.ui.theme.LumifyTheme
import kotlinx.coroutines.launch

/**
 * Enhanced detail screen that displays a photo full-screen with filtering capabilities
 * Filters are displayed at the bottom and applied directly to the main image
 */
@OptIn(ExperimentalMaterial3Api::class)
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
                errorMessage = (filterProcessState as DetailViewModel.FilterProcessState.Error).message
                showErrorDialog = true
                viewModel.resetFilterProcessState()
            }
            else -> { /* No action needed */ }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = mediaItem?.name ?: "Photo",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Share button (placeholder for future implementation)
                    IconButton(onClick = { /* Future implementation */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }

                    // Save button
                    IconButton(
                        onClick = {
                            if (selectedFilter.id != PhotoFilters.ORIGINAL.id) {
                                showSaveDialog = true
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No filter changes to save")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White
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
                    // Main image with filter applied
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentMediaItem.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = currentMediaItem.name,
                        contentScale = ContentScale.Fit,
                        loading = {
                            CircularProgressIndicator(color = Color.White)
                        },
                        colorFilter = selectedFilter.colorFilter,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )

                    // Filter strip at the bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(bottom = 16.dp, top = 12.dp)
                    ) {
                        // Filter name and description
                        Text(
                            text = selectedFilter.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Text(
                            text = selectedFilter.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filter thumbnails
                        FiltersRow(
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
}

@Composable
fun FiltersRow(
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
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(PhotoFilters.FILTERS) { filter ->
            FilterThumbnail(
                filter = filter,
                imageUri = imageUri,
                isSelected = filter.id == selectedFilter.id,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
fun FilterThumbnail(
    filter: PhotoFilter,
    imageUri: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(70.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = RoundedCornerShape(8.dp)
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
                    .clip(RoundedCornerShape(6.dp))
            )

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
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
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 10.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FiltersRowPreview() {
    LumifyTheme {
        Surface(color = Color.Black) {
            FiltersRow(
                imageUri = "",
                selectedFilter = PhotoFilters.ORIGINAL,
                onFilterSelected = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    LumifyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Photo Detail Preview",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}