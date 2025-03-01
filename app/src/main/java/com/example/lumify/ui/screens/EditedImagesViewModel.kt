package com.example.lumify.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumify.data.model.MediaItem
import com.example.lumify.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
* ViewModel for the EditedImagesScreen
* Manages loading and displaying edited (filtered) images
*/
@HiltViewModel
class EditedImagesViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Edited images state
    private val _editedImages = MutableStateFlow<List<MediaItem>>(emptyList())
    val editedImages: StateFlow<List<MediaItem>> = _editedImages.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Init block to load data when view model is created
    init {
        loadEditedImages()
    }

    /**
     * Load edited images from the repository
     */
    fun loadEditedImages() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Use the repository's dedicated method for edited images
                mediaRepository.getEditedMediaItems().collect { items ->
                    _editedImages.value = items
                }
            } catch (e: Exception) {
                _error.value = "Failed to load edited photos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh the edited images list
     * Useful after editing a new image
     */
    fun refreshEditedImages() {
        loadEditedImages()
    }
}