package com.cartoniq.pulse.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class SensorManagerHelper(private val context: Context) {

    private val sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private var toneGenerator: ToneGenerator? = null
    private var lastToneTime = 0L
    private var lastHapticTime = 0L

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (e: Exception) {
            Log.w("SensorManagerHelper", "ToneGenerator initialization failed", e)
        }
    }

    fun hasSensor(sensorType: Int): Boolean {
        return sensorManager?.getDefaultSensor(sensorType) != null
    }

    fun getSensor(sensorType: Int): Sensor? {
        return sensorManager?.getDefaultSensor(sensorType)
    }

    fun getSensorManager(): SensorManager? = sensorManager

    fun getAllSensors(): List<SensorHardwareInfo> {
        val list = sensorManager?.getSensorList(Sensor.TYPE_ALL) ?: emptyList()
        return list.map { sensor ->
            SensorHardwareInfo(
                type = sensor.type,
                typeName = getSensorTypeName(sensor.type),
                name = sensor.name ?: "Unknown Sensor",
                vendor = sensor.vendor ?: "Unknown Vendor",
                version = sensor.version,
                powerMa = sensor.power,
                maxRange = sensor.maximumRange,
                resolution = sensor.resolution
            )
        }.sortedBy { it.typeName }
    }

    /**
     * Plays a short, throttled sci-fi alert beep (minimum 250ms cooldown).
     */
    fun playAlertBeep() {
        val now = System.currentTimeMillis()
        if (now - lastToneTime > 250L) {
            lastToneTime = now
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            } catch (e: Exception) {
                Log.w("SensorManagerHelper", "Failed playing tone", e)
            }
        }
    }

    /**
     * Triggers a short, tactile haptic buzz (minimum 250ms cooldown).
     */
    fun triggerHapticFeedback() {
        val now = System.currentTimeMillis()
        if (now - lastHapticTime > 250L) {
            lastHapticTime = now
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    val vibrator = vibratorManager?.defaultVibrator
                    if (vibrator?.hasVibrator() == true) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                        )
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (vibrator?.hasVibrator() == true) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(
                                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                            )
                        } else {
                            vibrator.vibrate(50)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("SensorManagerHelper", "Haptic trigger failed", e)
            }
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.w("SensorManagerHelper", "Failed releasing ToneGenerator", e)
        }
    }

    companion object {
        fun mapAccuracy(accuracy: Int): SensorAccuracyLevel {
            return when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> SensorAccuracyLevel.UNCALIBRATED
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracyLevel.LOW
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracyLevel.MEDIUM
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracyLevel.HIGH
                else -> SensorAccuracyLevel.MEDIUM
            }
        }

        fun getSensorTypeName(type: Int): String {
            return when (type) {
                Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer (Magnetic Field)"
                Sensor.TYPE_ACCELEROMETER -> "Accelerometer (Linear + Gravity)"
                Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration (Dynamic Only)"
                Sensor.TYPE_LIGHT -> "Photocell (Ambient Light)"
                Sensor.TYPE_GYROSCOPE -> "Gyroscope"
                Sensor.TYPE_PRESSURE -> "Barometer (Pressure)"
                Sensor.TYPE_PROXIMITY -> "Proximity Sensor"
                Sensor.TYPE_GRAVITY -> "Gravity Sensor"
                Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
                Sensor.TYPE_STEP_COUNTER -> "Step Counter"
                Sensor.TYPE_STEP_DETECTOR -> "Step Detector"
                Sensor.TYPE_RELATIVE_HUMIDITY -> "Humidity Sensor"
                Sensor.TYPE_AMBIENT_TEMPERATURE -> "Temperature Sensor"
                else -> "Sensor Type $type"
            }
        }
    }
}
