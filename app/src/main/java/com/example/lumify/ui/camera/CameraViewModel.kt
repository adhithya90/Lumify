package com.example.lumify.ui.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    // Constants
    companion object {
        private const val TAG = "CameraViewModel"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    // Camera lens selector (back by default)
    private val _cameraSelector = MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector: StateFlow<CameraSelector> = _cameraSelector

    // Used to set up a link between the Camera and UI
    private val _surfaceRequest = MutableStateFlow<androidx.camera.core.SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<androidx.camera.core.SurfaceRequest?> = _surfaceRequest

    // Image capture use case
    private val _imageCapture = MutableStateFlow<ImageCapture?>(null)

    // Camera state
    private val _captureState = MutableStateFlow<CaptureState>(CaptureState.Idle)
    val captureState: StateFlow<CaptureState> = _captureState

    // Flash mode
    private val _flashMode = MutableStateFlow(ImageCapture.FLASH_MODE_OFF)
    val flashMode: StateFlow<Int> = _flashMode

    // Camera control for tap-to-focus
    private var cameraControl: androidx.camera.core.CameraControl? = null
    private var surfaceMeteringPointFactory: SurfaceOrientedMeteringPointFactory? = null

    // Preview use case
    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.value = newSurfaceRequest
            surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                newSurfaceRequest.resolution.width.toFloat(),
                newSurfaceRequest.resolution.height.toFloat()
            )
        }
    }

    // Image capture use case
    private val imageCaptureUseCase = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .setFlashMode(_flashMode.value)
        .build()
        .also { _imageCapture.value = it }

    // Switch camera lens
    fun toggleCamera() {
        _cameraSelector.value = if (_cameraSelector.value == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    // Toggle flash mode
    fun toggleFlash() {
        val newFlashMode = when (_flashMode.value) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        _flashMode.value = newFlashMode
        _imageCapture.value?.flashMode = newFlashMode
    }

    // Tap to focus
    fun tapToFocus(tapCoords: Offset) {
        val point = surfaceMeteringPointFactory?.createPoint(tapCoords.x, tapCoords.y)
        if (point != null) {
            val meteringAction = FocusMeteringAction.Builder(point).build()
            cameraControl?.startFocusAndMetering(meteringAction)
        }
    }

    // Capture image and save it
    fun captureImage(context: Context, executor: Executor) {
        val imageCapture = _imageCapture.value ?: return

        // Create output options
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Lumify")
            }
        }

        // Create output options object
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Update UI state
        _captureState.value = CaptureState.Capturing

        // Take the picture
        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    Log.d(TAG, "Photo capture succeeded: $savedUri")
                    viewModelScope.launch {
                        if (savedUri != null) {
                            _captureState.value = CaptureState.Success(savedUri)
                        } else {
                            _captureState.value = CaptureState.Error("Saved URI is null")
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    _captureState.value = CaptureState.Error(exception.message ?: "Unknown error")
                }
            }
        )
    }

    // Variable to track if we're already bound to the camera
    private var isBoundToCamera = false

    suspend fun bindToCamera(context: Context, lifecycleOwner: androidx.lifecycle.LifecycleOwner) = withContext(Dispatchers.Main) {
        // Don't rebind if already bound
        if (isBoundToCamera) return@withContext

        try {
            val cameraProvider = getCameraProvider(context)

            // Unbind previous use cases
            cameraProvider.unbindAll()

            // Bind use cases to camera
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                _cameraSelector.value,
                cameraPreviewUseCase,
                imageCaptureUseCase
            )

            // Get camera control for tap-to-focus
            cameraControl = camera.cameraControl
            isBoundToCamera = true

            Log.d(TAG, "Successfully bound to camera")

        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
            isBoundToCamera = false
        }
    }

    // Helper function to get the camera provider
    private suspend fun getCameraProvider(context: Context): ProcessCameraProvider = suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(context).also { future ->
            future.addListener({
                try {
                    continuation.resume(future.get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, context.mainExecutor)
        }
    }

    // Reset the capture state to idle
    fun resetCaptureState() {
        _captureState.value = CaptureState.Idle
    }

    // Camera capture state
    sealed class CaptureState {
        object Idle : CaptureState()
        object Capturing : CaptureState()
        data class Success(val uri: Uri) : CaptureState()
        data class Error(val message: String) : CaptureState()
    }
}