package com.example.lumify

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LumifyApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide services here
    }

    /**
     * Custom image loader configuration for Coil
     * - Handles thumbnail loading
     * - Configures memory and disk caching
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // Memory cache configuration
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of app memory for image cache
                    .build()
            }
            // Disk cache configuration
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(1024L * 1024L * 100L) // 100 MB
                    .build()
            }
            // Cross-fade by default
            .crossfade(true)
            .build()
    }
}
