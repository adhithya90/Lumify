package com.example.lumify.utils

import androidx.compose.ui.graphics.ColorMatrix

/**
 * Utility functions for creating and manipulating ColorMatrix objects for image filters
 */
object ColorMatrixUtils {

    /**
     * Create an identity matrix (no transformation)
     *
     * @return A ColorMatrix that doesn't modify the image
     */
    fun createIdentityMatrix(): ColorMatrix {
        return ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, 0f,  // Red channel
            0f, 1f, 0f, 0f, 0f,  // Green channel
            0f, 0f, 1f, 0f, 0f,  // Blue channel
            0f, 0f, 0f, 1f, 0f   // Alpha channel
        ))
    }

    /**
     * Create a new ColorMatrix with contrast adjustment
     *
     * @param contrast Contrast value (1.0 = normal, 0.0 to 10.0 reasonable range)
     * @return A ColorMatrix that applies contrast adjustment
     */
    fun createContrastMatrix(contrast: Float): ColorMatrix {
        val scale = contrast
        val translate = (-.5f * scale + .5f) * 255f
        val array = floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
        return ColorMatrix(array)
    }

    /**
     * Create a new ColorMatrix with brightness adjustment
     *
     * @param brightness Brightness value (-255 to 255)
     * @return A ColorMatrix that applies brightness adjustment
     */
    fun createBrightnessMatrix(brightness: Float): ColorMatrix {
        val array = floatArrayOf(
            1f, 0f, 0f, 0f, brightness,
            0f, 1f, 0f, 0f, brightness,
            0f, 0f, 1f, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        )
        return ColorMatrix(array)
    }

    /**
     * Create a new ColorMatrix with temperature adjustment (warm/cool)
     *
     * @param temperature Temperature value (-1.0 to 1.0, negative = cooler, positive = warmer)
     * @return A ColorMatrix that applies temperature adjustment
     */
    fun createTemperatureMatrix(temperature: Float): ColorMatrix {
        // Adjust red and blue channels in opposite directions
        val redShift = if (temperature > 0) temperature * 30f else 0f
        val blueShift = if (temperature < 0) -temperature * 30f else 0f
        val greenShift = (Math.abs(temperature) * 10f)

        val array = floatArrayOf(
            1f, 0f, 0f, 0f, redShift,
            0f, 1f, 0f, 0f, greenShift,
            0f, 0f, 1f, 0f, blueShift,
            0f, 0f, 0f, 1f, 0f
        )
        return ColorMatrix(array)
    }

    /**
     * Create a new ColorMatrix with saturation adjustment
     *
     * @param saturation Saturation value (0.0 = grayscale, 1.0 = normal, 2.0 = super saturated)
     * @return A ColorMatrix that applies saturation adjustment
     */
    fun createSaturationMatrix(saturation: Float): ColorMatrix {
        // Constants for luminance (grayscale)
        val lumR = 0.3086f
        val lumG = 0.6094f
        val lumB = 0.0820f

        val sr = (1 - saturation) * lumR
        val sg = (1 - saturation) * lumG
        val sb = (1 - saturation) * lumB

        val matrix = floatArrayOf(
            sr + saturation, sg,             sb,             0f, 0f,
            sr,             sg + saturation, sb,             0f, 0f,
            sr,             sg,             sb + saturation, 0f, 0f,
            0f,             0f,             0f,              1f, 0f
        )

        return ColorMatrix(matrix)
    }

    /**
     * Create a new ColorMatrix with sepia tone effect
     *
     * @param intensity The intensity of the sepia effect (0.0 to 1.0)
     * @return A ColorMatrix that applies a sepia effect
     */
    fun createSepiaMatrix(intensity: Float): ColorMatrix {
        val adjustedIntensity = intensity.coerceIn(0f, 1f)

        // Base sepia filter
        val sr = 0.393f
        val sg = 0.769f
        val sb = 0.189f

        val mr = 0.349f
        val mg = 0.686f
        val mb = 0.168f

        val br = 0.272f
        val bg = 0.534f
        val bb = 0.131f

        // Calculate interpolated values between identity and sepia
        val finalSr = 1f - adjustedIntensity + (sr * adjustedIntensity)
        val finalSg = 0f + (sg * adjustedIntensity)
        val finalSb = 0f + (sb * adjustedIntensity)

        val finalMr = 0f + (mr * adjustedIntensity)
        val finalMg = 1f - adjustedIntensity + (mg * adjustedIntensity)
        val finalMb = 0f + (mb * adjustedIntensity)

        val finalBr = 0f + (br * adjustedIntensity)
        val finalBg = 0f + (bg * adjustedIntensity)
        val finalBb = 1f - adjustedIntensity + (bb * adjustedIntensity)

        val array = floatArrayOf(
            finalSr, finalSg, finalSb, 0f, 0f,
            finalMr, finalMg, finalMb, 0f, 0f,
            finalBr, finalBg, finalBb, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        return ColorMatrix(array)
    }

    /**
     * Create a new ColorMatrix with a tint effect
     *
     * @param r Red tint value (0.0 to 1.0)
     * @param g Green tint value (0.0 to 1.0)
     * @param b Blue tint value (0.0 to 1.0)
     * @param intensity The intensity of the tint (0.0 to 1.0)
     * @return A ColorMatrix that applies a tint effect
     */
    fun createTintMatrix(r: Float, g: Float, b: Float, intensity: Float): ColorMatrix {
        val adjustedIntensity = intensity.coerceIn(0f, 1f)

        // Balance factor maintains overall brightness
        val balanceFactor = 1f - adjustedIntensity

        val array = floatArrayOf(
            balanceFactor + (r * adjustedIntensity), r * adjustedIntensity, r * adjustedIntensity, 0f, 0f,
            g * adjustedIntensity, balanceFactor + (g * adjustedIntensity), g * adjustedIntensity, 0f, 0f,
            b * adjustedIntensity, b * adjustedIntensity, balanceFactor + (b * adjustedIntensity), 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        return ColorMatrix(array)
    }

    /**
     * Combine multiple ColorMatrix objects into a single effect
     *
     * @param matrices List of ColorMatrix objects to combine
     * @return A ColorMatrix that applies all effects in sequence
     */
    fun combineMatrices(matrices: List<ColorMatrix>): ColorMatrix {
        if (matrices.isEmpty()) {
            return ColorMatrix()
        }

        val result = ColorMatrix()
        result.set(matrices.first())

        for (i in 1 until matrices.size) {
            result.timesAssign(matrices[i])
        }

        return result
    }
}