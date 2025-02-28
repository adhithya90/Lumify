package com.example.lumify.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import com.example.lumify.data.model.MediaItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple repository for accessing media items on the device
 */
/**
 * Simple repository for accessing media items on the device
 */
@Singleton
class MediaRepository @Inject constructor(
    private val context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver


    /**
     * Query media store for all photos
     */
    private fun queryMediaItems(): List<MediaItem> {
        val mediaItems = mutableListOf<MediaItem>()

        // Define the collection URI
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // Define projection (columns to fetch)
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        // Define sort order (newest first)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        // Query the media store
        val cursor = contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn).toString()
                val name = it.getString(nameColumn) ?: "Untitled"

                // Create Uri
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toLong()
                )

                val mediaItem = MediaItem(
                    id = id,
                    uri = uri,
                    name = name
                )

                mediaItems.add(mediaItem)
            }
        }

        return mediaItems
    }

    /**
     * Get a specific media item by ID
     */
    fun getMediaItemById(id: String): MediaItem? {
        val uri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            id.toLong()
        )

        return MediaItem(
            id = id,
            uri = uri,
            name = "Photo $id"
        )
    }

    /**
     * Manually refresh the media items - useful after capturing a new photo
     */
    fun refreshMediaItems() {
        viewModelScope.launch(Dispatchers.IO) {
            // Simply emit a new list of items from the query
            val updatedItems = queryMediaItems()
            mediaItemsFlow.value = updatedItems
        }
    }

    // Add this property to the class to manage the flow
    private val mediaItemsFlow = MutableStateFlow<List<MediaItem>>(emptyList())

    // Update the getMediaItems() method to use our flow
    fun getMediaItems(): Flow<List<MediaItem>> {
        // If the flow is empty, load items
        if (mediaItemsFlow.value.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val items = queryMediaItems()
                mediaItemsFlow.value = items
            }
        }
        return mediaItemsFlow.asStateFlow()
    }

    // Add ViewModel scope to the class
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Override close method to clean up resources
    fun close() {
        viewModelScope.cancel()
    }
}