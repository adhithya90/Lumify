package com.example.lumify.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lumify.data.model.MediaItem

@Composable
fun StaggeredMediaGrid(
    photos: List<MediaItem>,
    onPhotoClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalItemSpacing = 4.dp,
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = photos,
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