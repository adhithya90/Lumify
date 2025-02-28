package com.example.lumify.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumify.data.model.MediaItem
import com.example.lumify.data.model.PhotoFilter
import com.example.lumify.data.repository.MediaRepository
import com.example.lumify.utils.ImageFilterUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Media item state
    private val _mediaItem = MutableStateFlow<MediaItem?>(null)
    val mediaItem: StateFlow<MediaItem?> = _mediaItem.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Filter process state
    private val _filterProcessState = MutableStateFlow<FilterProcessState>(FilterProcessState.Idle)
    val filterProcessState: StateFlow<FilterProcessState> = _filterProcessState.asStateFlow()

    // Currently applied filter (for saving/export)
    private val _currentFilter = MutableStateFlow<PhotoFilter?>(null)
    val currentFilter: StateFlow<PhotoFilter?> = _currentFilter.asStateFlow()

    /**
     * Set a filter for preview without saving
     */
    fun setPreviewFilter(filter: PhotoFilter) {
        _currentFilter.value = filter
    }

    /**
     * Load a media item by its ID
     */
    fun loadMediaItem(mediaId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val item = mediaRepository.getMediaItemById(mediaId)
                _mediaItem.value = item

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to load photo: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Apply a filter to the current image
     */
    fun applyFilter(filter: PhotoFilter, createNewCopy: Boolean) {
        val currentMediaItem = _mediaItem.value ?: return
        val imageUri = currentMediaItem.uri

        viewModelScope.launch {
            try {
                _filterProcessState.value = FilterProcessState.Processing

                // Apply the filter and save the image
                val result = ImageFilterUtils.applyFilterAndSave(
                    context,
                    imageUri,
                    filter.colorFilter,
                    createNewCopy
                )

                result.fold(
                    onSuccess = { savedUri ->
                        // If we created a new copy, we need to refresh the media repository
                        // to ensure it includes the new file
                        if (createNewCopy) {
                            mediaRepository.refreshMediaItems()

                            // Create a new MediaItem for the filtered image
                            val filteredName = "${currentMediaItem.name}_${filter.name}"
                            val newMediaItem = MediaItem(
                                id = savedUri.toString(),
                                uri = savedUri,
                                name = filteredName
                            )

                            _mediaItem.value = newMediaItem
                        }

                        _currentFilter.value = filter
                        _filterProcessState.value = FilterProcessState.Success(savedUri)
                    },
                    onFailure = { exception ->
                        _filterProcessState.value = FilterProcessState.Error(exception.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _filterProcessState.value = FilterProcessState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Reset the filter process state to idle
     */
    fun resetFilterProcessState() {
        _filterProcessState.value = FilterProcessState.Idle
    }

    /**
     * States for the filter application process
     */
    sealed class FilterProcessState {
        object Idle : FilterProcessState()
        object Processing : FilterProcessState()
        data class Success(val uri: Uri) : FilterProcessState()
        data class Error(val message: String) : FilterProcessState()
    }
}