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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
     * Gets all photos from the device
     */
    fun getMediaItems(): Flow<List<MediaItem>> = flow {
        val mediaItems = queryMediaItems()
        emit(mediaItems)
    }.flowOn(Dispatchers.IO)

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
}