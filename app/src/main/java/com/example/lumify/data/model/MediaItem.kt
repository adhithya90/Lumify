package com.example.lumify.data.model

import android.location.Location
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.content.MediaType
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Simple model class for media items (photos)
 */
data class MediaItem(
    val id: String,
    val uri: Uri,
    val name: String
)


