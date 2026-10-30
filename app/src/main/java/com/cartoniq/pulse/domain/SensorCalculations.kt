package com.cartoniq.pulse.domain

import kotlin.math.sqrt

/**
 * Pure calculation utilities for Pulse sensor processing.
 * Strictly decoupled from Android framework dependencies for testability.
 */
object SensorCalculations {

    /** Standard gravitational acceleration constant in m/s² (ISO 80000-3) */
    const val GRAVITY_STANDARD: Float = 9.80665f

    /** Default exponential moving average alpha for low-pass gravity filter */
    const val DEFAULT_GRAVITY_FILTER_ALPHA: Float = 0.8f

    /**
     * Calculates the Euclidean vector magnitude sqrt(x² + y² + z²).
     */
    fun vectorMagnitude(x: Float, y: Float, z: Float): Float {
        return sqrt(x * x + y * y + z * z)
    }

    /**
     * Converts acceleration in m/s² to standard G-force (g).
     */
    fun toGForce(accelerationMps2: Float): Float {
        return accelerationMps2 / GRAVITY_STANDARD
    }

    /**
     * Computes magnetic baseline deviation:
     * deviation = currentMagnitude - baselineMagnitude
     */
    fun magneticDeviation(currentMagnitude: Float, baselineMagnitude: Float): Float {
        return currentMagnitude - baselineMagnitude
    }

    /**
     * Checks if a measured value meets or exceeds the alert threshold.
     */
    fun isThresholdExceeded(value: Float, threshold: Float): Boolean {
        return value >= threshold && threshold > 0f
    }

    /**
     * Environment lux classification brackets according to architectural blueprint.
     */
    enum class LuxCategory {
        EXTREME_DARKNESS,
        DIM_INTERIOR,
        NORMAL_INDOOR,
        BRIGHT_OFFICE,
        OVERCAST_DAYLIGHT,
        DIRECT_SUNLIGHT
    }

    /**
     * Classifies an ambient illuminance reading into its descriptive environment bracket.
     */
    fun classifyLux(lux: Float): LuxCategory {
        return when {
            lux < 1.0f -> LuxCategory.EXTREME_DARKNESS
            lux < 50.0f -> LuxCategory.DIM_INTERIOR
            lux < 500.0f -> LuxCategory.NORMAL_INDOOR
            lux < 1000.0f -> LuxCategory.BRIGHT_OFFICE
            lux < 10000.0f -> LuxCategory.OVERCAST_DAYLIGHT
            else -> LuxCategory.DIRECT_SUNLIGHT
        }
    }

    /**
     * Stateful low-pass gravity filter for dynamic vibration extraction.
     * Separates the constant static gravity vector (~9.81 m/s²) from dynamic vibration oscillations.
     */
    class GravityFilter(private val alpha: Float = DEFAULT_GRAVITY_FILTER_ALPHA) {
        var gravityX: Float = 0f
            private set
        var gravityY: Float = 0f
            private set
        var gravityZ: Float = 0f
            private set

        private var isInitialized = false

        fun reset() {
            gravityX = 0f
            gravityY = 0f
            gravityZ = 0f
            isInitialized = false
        }

        fun update(rawX: Float, rawY: Float, rawZ: Float): FilteredAcceleration {
            if (!isInitialized) {
                gravityX = rawX
                gravityY = rawY
                gravityZ = rawZ
                isInitialized = true
            } else {
                gravityX = alpha * gravityX + (1f - alpha) * rawX
                gravityY = alpha * gravityY + (1f - alpha) * rawY
                gravityZ = alpha * gravityZ + (1f - alpha) * rawZ
            }

            val dynamicX = rawX - gravityX
            val dynamicY = rawY - gravityY
            val dynamicZ = rawZ - gravityZ
            val dynamicMag = vectorMagnitude(dynamicX, dynamicY, dynamicZ)
            val totalMag = vectorMagnitude(rawX, rawY, rawZ)

            return FilteredAcceleration(
                rawX = rawX,
                rawY = rawY,
                rawZ = rawZ,
                gravityX = gravityX,
                gravityY = gravityY,
                gravityZ = gravityZ,
                dynamicX = dynamicX,
                dynamicY = dynamicY,
                dynamicZ = dynamicZ,
                dynamicMagnitude = dynamicMag,
                dynamicGForce = toGForce(dynamicMag),
                totalMagnitude = totalMag,
                totalGForce = toGForce(totalMag)
            )
        }
    }

    data class FilteredAcceleration(
        val rawX: Float,
        val rawY: Float,
        val rawZ: Float,
        val gravityX: Float,
        val gravityY: Float,
        val gravityZ: Float,
        val dynamicX: Float,
        val dynamicY: Float,
        val dynamicZ: Float,
        val dynamicMagnitude: Float,
        val dynamicGForce: Float,
        val totalMagnitude: Float,
        val totalGForce: Float
    )
}
