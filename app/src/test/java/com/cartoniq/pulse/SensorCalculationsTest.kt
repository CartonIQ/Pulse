package com.cartoniq.pulse

import com.cartoniq.pulse.domain.SensorCalculations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating pure sensor domain calculations as specified in Chapter 40.
 */
class SensorCalculationsTest {

    @Test
    fun testMT01_magneticMagnitudeKnownVector() {
        // Pythagorean 3-4-0 vector -> 5.0
        val mag1 = SensorCalculations.vectorMagnitude(3.0f, 4.0f, 0.0f)
        assertEquals(5.0f, mag1, 0.001f)

        // 3D vector (10, 20, 20) -> sqrt(100 + 400 + 400) = sqrt(900) = 30.0
        val mag2 = SensorCalculations.vectorMagnitude(10.0f, 20.0f, 20.0f)
        assertEquals(30.0f, mag2, 0.001f)
    }

    @Test
    fun testVT01_accelerationMagnitudeAndGConversion() {
        val standardGravity = 9.80665f
        val gForce = SensorCalculations.toGForce(standardGravity)
        assertEquals(1.0f, gForce, 0.0001f)

        val doubleGravity = standardGravity * 2f
        val gForceDouble = SensorCalculations.toGForce(doubleGravity)
        assertEquals(2.0f, gForceDouble, 0.0001f)
    }

    @Test
    fun testVT02_gravityRemovalAtRestApproximatesZeroDynamicAcceleration() {
        val filter = SensorCalculations.GravityFilter(alpha = 0.8f)

        // Device resting flat on a table: gravity along Z axis ~ 9.81 m/s²
        var lastFiltered = filter.update(0f, 0f, 9.81f)

        // Simulate 20 constant stationary cycles
        repeat(20) {
            lastFiltered = filter.update(0f, 0f, 9.81f)
        }

        // At rest, dynamic acceleration should be virtually zero (< 0.01 m/s²)
        assertEquals(0.0f, lastFiltered.dynamicMagnitude, 0.01f)
        assertEquals(0.0f, lastFiltered.dynamicGForce, 0.001f)
        // Total magnitude should remain 9.81 m/s²
        assertEquals(9.81f, lastFiltered.totalMagnitude, 0.01f)
    }

    @Test
    fun testLT01_luxClassificationBands() {
        assertEquals(
            SensorCalculations.LuxCategory.EXTREME_DARKNESS,
            SensorCalculations.classifyLux(0.4f)
        )
        assertEquals(
            SensorCalculations.LuxCategory.DIM_INTERIOR,
            SensorCalculations.classifyLux(25.0f)
        )
        assertEquals(
            SensorCalculations.LuxCategory.NORMAL_INDOOR,
            SensorCalculations.classifyLux(250.0f)
        )
        assertEquals(
            SensorCalculations.LuxCategory.BRIGHT_OFFICE,
            SensorCalculations.classifyLux(750.0f)
        )
        assertEquals(
            SensorCalculations.LuxCategory.OVERCAST_DAYLIGHT,
            SensorCalculations.classifyLux(4000.0f)
        )
        assertEquals(
            SensorCalculations.LuxCategory.DIRECT_SUNLIGHT,
            SensorCalculations.classifyLux(25000.0f)
        )
    }

    @Test
    fun testThresholdCrossing() {
        assertTrue(SensorCalculations.isThresholdExceeded(76.0f, 75.0f))
        assertTrue(SensorCalculations.isThresholdExceeded(75.0f, 75.0f))
        assertFalse(SensorCalculations.isThresholdExceeded(74.9f, 75.0f))
        assertFalse(SensorCalculations.isThresholdExceeded(10.0f, 0.0f))
    }

    @Test
    fun testBaselineDeviation() {
        val baseline = 45.0f
        val current = 62.5f
        val deviation = SensorCalculations.magneticDeviation(current, baseline)
        assertEquals(17.5f, deviation, 0.001f)
    }
}
