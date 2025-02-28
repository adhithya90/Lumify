package com.example.lumify.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidColorFilter
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilities for applying filters to images and saving them
 */
object ImageFilterUtils {
    private const val TAG = "ImageFilterUtils"
    private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    private const val IMAGE_QUALITY = 90

    /**
     * Apply a filter to an image and save it (either as a new file or overwriting the original)
     *
     * @param context The application context
     * @param imageUri The URI of the original image
     * @param colorFilter The color filter to apply
     * @param createNewCopy Whether to create a new file or overwrite the original
     * @return The URI of the saved image (either new or original)
     */
    suspend fun applyFilterAndSave(
        context: Context,
        imageUri: Uri,
        colorFilter: ColorFilter,
        createNewCopy: Boolean
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            // Load the bitmap using Coil
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUri)
                .allowHardware(false) // Disable hardware bitmaps for filtering
                .size(coil.size.Size.ORIGINAL) // Use fully qualified name
                .build()

            val result = loader.execute(request)
            val drawable = result.drawable

            if (drawable == null) {
                return@withContext Result.failure(Exception("Failed to load image - drawable is null"))
            }

            if (drawable !is BitmapDrawable) {
                return@withContext Result.failure(Exception("Failed to load image - drawable is not a BitmapDrawable"))
            }

            val bitmap = drawable.bitmap ?: return@withContext Result.failure(Exception("Failed to load image - bitmap is null"))

            if (bitmap.isRecycled) {
                return@withContext Result.failure(Exception("Failed to load image - bitmap is recycled"))
            }

            Log.d(TAG, "Loaded bitmap: ${bitmap.width}x${bitmap.height}, config: ${bitmap.config}")

            try {
                // Apply the filter to the bitmap
                val filteredBitmap = applyFilterToBitmap(bitmap, colorFilter)

                // Save the filtered bitmap
                val savedUri = if (createNewCopy) {
                    saveAsNewImage(context, filteredBitmap)
                } else {
                    overwriteOriginalImage(context, imageUri, filteredBitmap)
                }

                Result.success(savedUri)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing or saving image", e)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying filter and saving image", e)
            Result.failure(e)
        }
    }

    /**
     * Apply a filter to a Bitmap
     */
    private fun applyFilterToBitmap(bitmap: Bitmap, colorFilter: ColorFilter): Bitmap {
        // Create a mutable copy that can be safely modified
        val mutableBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            // Hardware bitmaps can't be directly modified, so convert it first
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        val canvas = android.graphics.Canvas(mutableBitmap)

        // Convert Compose ColorFilter to Android ColorFilter
        val paint = android.graphics.Paint().apply {
            this.colorFilter = colorFilter.asAndroidColorFilter()
        }

        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return mutableBitmap
    }

    /**
     * Save a bitmap as a new image file
     */
    private suspend fun saveAsNewImage(context: Context, bitmap: Bitmap): Uri = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())
        val fileName = "LUMIFY_EDIT_$timestamp.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Lumify")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Failed to create new file for image")

        contentResolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
        } ?: throw Exception("Failed to open output stream")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
        }

        return@withContext uri
    }

    /**
     * Overwrite the original image file with a filtered bitmap
     */
    private suspend fun overwriteOriginalImage(
        context: Context,
        imageUri: Uri,
        bitmap: Bitmap
    ): Uri = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        try {
            // First try direct overwrite
            contentResolver.openOutputStream(imageUri, "wt")?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
            } ?: throw Exception("Failed to open output stream for original image")

            return@withContext imageUri
        } catch (e: Exception) {
            Log.w(TAG, "Direct overwrite failed, trying to create a new file and delete the old one", e)

            // If direct overwrite fails, create a new file with same name and delete the old one
            val cursor = contentResolver.query(
                imageUri,
                arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.RELATIVE_PATH
                ),
                null,
                null,
                null
            )

            // Get filename and path
            var fileName = "filtered_image.jpg"
            var relativePath = "Pictures/Lumify"

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val pathIndex = it.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)

                    if (nameIndex >= 0) {
                        fileName = it.getString(nameIndex)
                    }

                    if (pathIndex >= 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        relativePath = it.getString(pathIndex)
                    }
                }
            }

            // Create a new file
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val newUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Failed to create new file for image")

            // Write to the new file
            contentResolver.openOutputStream(newUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
            } ?: throw Exception("Failed to open output stream")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(newUri, contentValues, null, null)
            }

            // Try to delete the old file (may fail if not permitted)
            try {
                contentResolver.delete(imageUri, null, null)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete original image", e)
                // Not critical, so we can continue
            }

            return@withContext newUri
        }
    }
}