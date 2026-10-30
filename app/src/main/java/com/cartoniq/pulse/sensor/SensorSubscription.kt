package com.cartoniq.pulse.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Lifecycle-aware sensor listener subscription for Jetpack Compose.
 *
 * Guarantees:
 * 1. Listener is registered ONLY while the screen composable is active AND Lifecycle is RESUMED.
 * 2. Listener is immediately unregistered when the app is backgrounded (ON_PAUSE/ON_STOP).
 * 3. Listener is immediately cleaned up when navigating away from the screen (onDispose).
 * 4. Never executes background sensor polling or drains device battery.
 */
@Composable
fun SensorSubscription(
    sensorManager: SensorManager?,
    sensor: Sensor?,
    samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_NORMAL,
    isActive: Boolean = true,
    onSensorChanged: (SensorEvent) -> Unit,
    onAccuracyChanged: ((Sensor?, Int) -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnSensorChanged = rememberUpdatedState(onSensorChanged)
    val currentOnAccuracyChanged = rememberUpdatedState(onAccuracyChanged)

    DisposableEffect(lifecycleOwner, sensorManager, sensor, samplingPeriodUs, isActive) {
        if (sensorManager == null || sensor == null || !isActive) {
            return@DisposableEffect onDispose { }
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                currentOnSensorChanged.value(event)
            }

            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {
                currentOnAccuracyChanged.value?.invoke(s, accuracy)
            }
        }

        var isRegistered = false

        fun register() {
            if (!isRegistered) {
                isRegistered = sensorManager.registerListener(listener, sensor, samplingPeriodUs)
            }
        }

        fun unregister() {
            if (isRegistered) {
                sensorManager.unregisterListener(listener)
                isRegistered = false
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> register()
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> unregister()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // If lifecycle is already resumed when composing
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            register()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            unregister()
        }
    }
}
