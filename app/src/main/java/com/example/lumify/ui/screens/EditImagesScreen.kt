package com.example.lumify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lumify.data.model.MediaItem
import com.example.lumify.ui.components.MediaGridItem
import com.example.lumify.ui.components.SearchBar
import com.example.lumify.ui.components.StaggeredMediaGrid
import com.example.lumify.ui.theme.LumifyTheme

@Composable
fun EditedImagesScreen(
    onPhotoClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditedImagesViewModel = hiltViewModel()
) {
    // Collect state from ViewModel
    val editedImages by viewModel.editedImages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // Main content
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoading && editedImages.isEmpty()) {
            // Show loading indicator
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        } else if (error != null) {
            // Show error message
            Text(
                text = error ?: "Unknown error",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        } else if (editedImages.isEmpty()) {
            // Show empty state
            Text(
                text = "No edited photos found.\nFilter some images to see them here!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        } else {
            // LazyVerticalStaggeredGrid with a header for the search bar
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalItemSpacing = 4.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                // Header item that spans the full width (search bar)
                item(span = StaggeredGridItemSpan.FullLine) {
                    SearchBar(
                        backgroundColor = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Photos grid
                items(
                    items = editedImages,
                    key = { it.id }
                ) { photo ->
                    MediaGridItem(
                        photo = photo,
                        onClick = { onPhotoClick(photo) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditedImagesScreenPreview() {
    LumifyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Edited Photos Preview",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}