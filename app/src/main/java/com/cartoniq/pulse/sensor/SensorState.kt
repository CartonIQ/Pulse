package com.cartoniq.pulse.sensor

import com.cartoniq.pulse.domain.SensorCalculations

sealed interface SensorAvailability {
    data object Unknown : SensorAvailability
    data object Available : SensorAvailability
    data class Unavailable(val reason: String) : SensorAvailability
}

enum class SensorAccuracyLevel {
    UNCALIBRATED,
    LOW,
    MEDIUM,
    HIGH
}

data class MagneticReading(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 0f,
    val baseline: Float = 0f,
    val deviation: Float = 0f,
    val peak: Float = 0f,
    val accuracy: SensorAccuracyLevel = SensorAccuracyLevel.HIGH,
    val timestampMs: Long = 0L,
    val isPaused: Boolean = false
)

data class VibrationReading(
    val rawX: Float = 0f,
    val rawY: Float = 0f,
    val rawZ: Float = 0f,
    val totalMagnitude: Float = 0f,
    val totalGForce: Float = 0f,
    val dynamicX: Float = 0f,
    val dynamicY: Float = 0f,
    val dynamicZ: Float = 0f,
    val dynamicMagnitude: Float = 0f,
    val dynamicGForce: Float = 0f,
    val peakGForce: Float = 0f,
    val isLinearSensor: Boolean = false,
    val accuracy: SensorAccuracyLevel = SensorAccuracyLevel.HIGH,
    val timestampMs: Long = 0L,
    val isPaused: Boolean = false
)

data class LightReading(
    val lux: Float = 0f,
    val category: SensorCalculations.LuxCategory = SensorCalculations.LuxCategory.NORMAL_INDOOR,
    val minLux: Float = 0f,
    val maxLux: Float = 0f,
    val accuracy: SensorAccuracyLevel = SensorAccuracyLevel.HIGH,
    val timestampMs: Long = 0L,
    val isPaused: Boolean = false
)

data class SensorHardwareInfo(
    val type: Int,
    val typeName: String,
    val name: String,
    val vendor: String,
    val version: Int,
    val powerMa: Float,
    val maxRange: Float,
    val resolution: Float
)
