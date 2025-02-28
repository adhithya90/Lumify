package com.example.lumify.data.model

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.example.lumify.utils.ColorMatrixUtils



/**
 * Represents a photo filter that can be applied to images
 */
data class PhotoFilter(
    val id: String,
    val name: String,
    val description: String,
    val colorMatrix: ColorMatrix
) {
    // Convert ColorMatrix to a ColorFilter that can be applied to images
    val colorFilter: ColorFilter
        get() = ColorFilter.colorMatrix(colorMatrix)
}

/**
 * Collection of premade photo filters with various styles inspired by famous camera brands
 */
object PhotoFilters {
    private fun createFilter(
        id: String,
        name: String,
        description: String,
        matrix: ColorMatrix
    ): PhotoFilter = PhotoFilter(id, name, description, matrix)

    // Original/No filter
    val ORIGINAL = createFilter(
        id = "original",
        name = "Original",
        description = "No filter applied",
        matrix = ColorMatrixUtils.createIdentityMatrix()
    )

    // Classic black and white inspired by classic rangefinder cameras
    val MONOCHROME = createFilter(
        id = "monochrome",
        name = "Silver",
        description = "Classic high-contrast black & white",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createSaturationMatrix(0f),
            ColorMatrixUtils.createContrastMatrix(1.2f)
        ))
    )

    // Vintage film look with warm tones
    val VINTAGE = createFilter(
        id = "vintage",
        name = "Nostalgia",
        description = "Warm tones with vintage film character",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createTemperatureMatrix(0.3f),
            ColorMatrixUtils.createSaturationMatrix(0.85f),
            ColorMatrixUtils.createSepiaMatrix(0.2f),
            ColorMatrixUtils.createContrastMatrix(1.1f)
        ))
    )

    // Cool, crisp look inspired by medium format digital
    val COOL = createFilter(
        id = "cool",
        name = "Nordic",
        description = "Clean, cool tones with crisp detail",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createTemperatureMatrix(-0.3f),
            ColorMatrixUtils.createContrastMatrix(1.15f),
            ColorMatrixUtils.createSaturationMatrix(0.9f)
        ))
    )

    // Rich, warm look inspired by premium mirrorless cameras
    val WARM = createFilter(
        id = "warm",
        name = "Golden",
        description = "Rich, warm tones with enhanced depth",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createTemperatureMatrix(0.4f),
            ColorMatrixUtils.createSaturationMatrix(1.05f),
            ColorMatrixUtils.createTintMatrix(1.1f, 0.95f, 0.8f, 0.3f)
        ))
    )

    // High contrast, punchy look inspired by professional DSLRs
    val HIGH_CONTRAST = createFilter(
        id = "high_contrast",
        name = "Impact",
        description = "Bold contrast with vibrant colors",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createContrastMatrix(1.4f),
            ColorMatrixUtils.createSaturationMatrix(1.15f),
            ColorMatrixUtils.createBrightnessMatrix(-10f)
        ))
    )

    // Muted, filmic look inspired by cinema cameras
    val MUTED = createFilter(
        id = "muted",
        name = "Cinema",
        description = "Subtle, filmic tones with reduced contrast",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createSaturationMatrix(0.8f),
            ColorMatrixUtils.createContrastMatrix(0.9f),
            ColorMatrixUtils.createTintMatrix(0.95f, 0.95f, 1.05f, 0.2f)
        ))
    )

    // Dramatic black and white inspired by street photography
    val DRAMATIC = createFilter(
        id = "dramatic",
        name = "Street",
        description = "Dramatic black & white with deep shadows",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createSaturationMatrix(0f),
            ColorMatrixUtils.createContrastMatrix(1.5f),
            ColorMatrixUtils.createBrightnessMatrix(-20f)
        ))
    )

    // Vibrant, colorful look inspired by popular consumer cameras
    val VIBRANT = createFilter(
        id = "vibrant",
        name = "Pop",
        description = "Bright, vibrant colors with extra punch",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createSaturationMatrix(1.3f),
            ColorMatrixUtils.createContrastMatrix(1.2f),
            ColorMatrixUtils.createBrightnessMatrix(10f)
        ))
    )

    // Soft pastel look inspired by medium format film
    val PASTEL = createFilter(
        id = "pastel",
        name = "Dreamy",
        description = "Soft pastel colors with lifted shadows",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createSaturationMatrix(0.9f),
            ColorMatrixUtils.createContrastMatrix(0.85f),
            ColorMatrixUtils.createBrightnessMatrix(25f),
            ColorMatrixUtils.createTintMatrix(1.05f, 1.05f, 1.1f, 0.2f)
        ))
    )

    // Cross-processed look inspired by experimental film techniques
    val CROSS_PROCESS = createFilter(
        id = "cross_process",
        name = "Analog",
        description = "Creative color shift with cross-processed look",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createTintMatrix(1.0f, 1.2f, 0.8f, 0.3f),
            ColorMatrixUtils.createContrastMatrix(1.2f),
            ColorMatrixUtils.createSaturationMatrix(1.1f)
        ))
    )

    // Fuji-inspired green and teal tones
    val VERDE = createFilter(
        id = "verde",
        name = "Verde",
        description = "Lush greens with teal shadows",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createTintMatrix(0.9f, 1.1f, 1.0f, 0.2f),
            ColorMatrixUtils.createSaturationMatrix(1.1f),
            ColorMatrixUtils.createContrastMatrix(1.1f)
        ))
    )

    // Portrait-oriented filter with pleasing skin tones
    val PORTRAIT = createFilter(
        id = "portrait",
        name = "Portrait",
        description = "Flattering skin tones with soft detail",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createSaturationMatrix(0.95f),
            ColorMatrixUtils.createContrastMatrix(0.95f),
            ColorMatrixUtils.createTemperatureMatrix(0.15f),
            ColorMatrixUtils.createTintMatrix(1.05f, 0.98f, 0.95f, 0.15f)
        ))
    )

    // High-key bright and airy look
    val AIRY = createFilter(
        id = "airy",
        name = "Airy",
        description = "Bright, airy look with minimal contrast",
        matrix = ColorMatrixUtils.combineMatrices(listOf(
            ColorMatrixUtils.createBrightnessMatrix(30f),
            ColorMatrixUtils.createContrastMatrix(0.8f),
            ColorMatrixUtils.createSaturationMatrix(0.85f)
        ))
    )

    // The complete list of all available filters
    val FILTERS = listOf(
        ORIGINAL,
        MONOCHROME,
        VINTAGE,
        COOL,
        WARM,
        HIGH_CONTRAST,
        MUTED,
        DRAMATIC,
        VIBRANT,
        PASTEL,
        CROSS_PROCESS,
        VERDE,
        PORTRAIT,
        AIRY
    )
}