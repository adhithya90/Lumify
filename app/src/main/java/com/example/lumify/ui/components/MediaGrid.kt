package com.example.lumify.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.lumify.data.model.MediaItem
import com.example.lumify.ui.theme.LumifyTheme

/**
 * Grid item that displays a photo with its natural aspect ratio
 */
@Composable
fun MediaGridItem(
    photo: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Default aspect ratio until image is loaded
    var aspectRatio by remember { mutableStateOf(1f) }

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.uri)
                .crossfade(true)
                .build(),
            contentDescription = photo.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
            contentScale = ContentScale.Crop
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // Use 1:1 while loading
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (state is AsyncImagePainter.State.Success) {
                // Calculate and store the aspect ratio once the image is loaded
                state.result.drawable.let { drawable ->
                    if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                        val imageAspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                        // Only update if significantly different to avoid recomposition loops
                        if (kotlin.math.abs(imageAspectRatio - aspectRatio) > 0.01f) {
                            aspectRatio = imageAspectRatio
                        }
                    }
                }
                SubcomposeAsyncImageContent()
            }
        }
    }
}

@Preview
@Composable
fun MediaGridItemPreview() {
    LumifyTheme {
        // Create a sample media item for preview
        val samplePhoto = MediaItem(
            id = "1",
            uri = Uri.parse("https://example.com/sample.jpg"),
            name = "Sample Photo"
        )

        MediaGridItem(
            photo = samplePhoto,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}