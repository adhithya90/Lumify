package com.example.lumify.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
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
 * Repository for accessing media items on the device and tracking edited images
 */
@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver
    private val prefs: SharedPreferences = context.getSharedPreferences("lumify_prefs", Context.MODE_PRIVATE)

    // Media items flow
    private val mediaItemsFlow = MutableStateFlow<List<MediaItem>>(emptyList())

    // Edited media items flow
    private val editedMediaItemsFlow = MutableStateFlow<List<MediaItem>>(emptyList())

    // Key prefix for tracking edited images in SharedPreferences
    private val EDITED_IMAGE_KEY_PREFIX = "edited_image_"

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
     * Mark an image as edited
     */
    fun markImageAsEdited(mediaId: String) {
        prefs.edit().putBoolean("$EDITED_IMAGE_KEY_PREFIX$mediaId", true).apply()

        // Refresh edited items list
        refreshEditedMediaItems()
    }

    /**
     * Check if an image has been edited
     */
    fun isImageEdited(mediaId: String): Boolean {
        return prefs.getBoolean("$EDITED_IMAGE_KEY_PREFIX$mediaId", false)
    }

    /**
     * Refresh edited media items
     */
    private fun refreshEditedMediaItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val allItems = queryMediaItems()
            val editedItems = allItems.filter { mediaItem ->
                isImageEdited(mediaItem.id)
            }
            editedMediaItemsFlow.value = editedItems
        }
    }

    /**
     * Manually refresh all media items - useful after capturing a new photo
     */
    fun refreshMediaItems() {
        viewModelScope.launch(Dispatchers.IO) {
            // Update all media items
            val updatedItems = queryMediaItems()
            mediaItemsFlow.value = updatedItems

            // Also refresh edited items
            refreshEditedMediaItems()
        }
    }

    // Get all media items
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

    // Get only edited media items
    fun getEditedMediaItems(): Flow<List<MediaItem>> {
        // If the flow is empty, load items
        if (editedMediaItemsFlow.value.isEmpty()) {
            refreshEditedMediaItems()
        }
        return editedMediaItemsFlow.asStateFlow()
    }

    // Add ViewModel scope to the class
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Clean up resources
    fun close() {
        viewModelScope.cancel()
    }

    // Method needed to fix compiler error
    fun cancel() {
        viewModelScope.cancel()
    }
}