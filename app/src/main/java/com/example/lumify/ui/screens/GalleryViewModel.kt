package com.example.lumify.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumify.data.model.MediaItem
import com.example.lumify.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Simple ViewModel for the gallery screen
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    // Media items state
    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Init block to load data when view model is created
    init {
        loadMediaItems()
    }

    /**
     * Load media items from the repository
     */
    fun loadMediaItems() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                mediaRepository.getMediaItems().collect { items ->
                    _mediaItems.value = items
                }
            } catch (e: Exception) {
                _error.value = "Failed to load photos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}