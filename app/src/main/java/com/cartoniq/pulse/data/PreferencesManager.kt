package com.cartoniq.pulse.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persisted user preferences using private SharedPreferences.
 * Guaranteed offline and private to the application.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, DEFAULT_HAPTICS_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

    var magneticThreshold: Float
        get() = prefs.getFloat(KEY_MAGNETIC_THRESHOLD, DEFAULT_MAGNETIC_THRESHOLD)
        set(value) {
            val clamped = value.coerceIn(MIN_MAGNETIC_THRESHOLD, MAX_MAGNETIC_THRESHOLD)
            prefs.edit().putFloat(KEY_MAGNETIC_THRESHOLD, clamped).apply()
        }

    var vibrationThreshold: Float
        get() = prefs.getFloat(KEY_VIBRATION_THRESHOLD, DEFAULT_VIBRATION_THRESHOLD)
        set(value) {
            val clamped = value.coerceIn(MIN_VIBRATION_THRESHOLD, MAX_VIBRATION_THRESHOLD)
            prefs.edit().putFloat(KEY_VIBRATION_THRESHOLD, clamped).apply()
        }

    var sampleRateMode: String
        get() = prefs.getString(KEY_SAMPLE_RATE_MODE, RATE_NORMAL) ?: RATE_NORMAL
        set(value) = prefs.edit().putString(KEY_SAMPLE_RATE_MODE, value).apply()

    var isOnboardingDismissed: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DISMISSED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DISMISSED, value).apply()

    fun resetToDefaults() {
        prefs.edit()
            .putBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND_ENABLED)
            .putBoolean(KEY_HAPTICS_ENABLED, DEFAULT_HAPTICS_ENABLED)
            .putFloat(KEY_MAGNETIC_THRESHOLD, DEFAULT_MAGNETIC_THRESHOLD)
            .putFloat(KEY_VIBRATION_THRESHOLD, DEFAULT_VIBRATION_THRESHOLD)
            .putString(KEY_SAMPLE_RATE_MODE, RATE_NORMAL)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "pulse_preferences"

        const val KEY_SOUND_ENABLED = "pref_sound_enabled"
        const val KEY_HAPTICS_ENABLED = "pref_haptics_enabled"
        const val KEY_MAGNETIC_THRESHOLD = "pref_magnetic_threshold"
        const val KEY_VIBRATION_THRESHOLD = "pref_vibration_threshold"
        const val KEY_SAMPLE_RATE_MODE = "pref_sample_rate_mode"
        const val KEY_ONBOARDING_DISMISSED = "pref_onboarding_dismissed"

        const val DEFAULT_SOUND_ENABLED = true
        const val DEFAULT_HAPTICS_ENABLED = true
        const val DEFAULT_MAGNETIC_THRESHOLD = 75.0f // µT
        const val DEFAULT_VIBRATION_THRESHOLD = 2.5f // m/s²

        const val MIN_MAGNETIC_THRESHOLD = 30.0f
        const val MAX_MAGNETIC_THRESHOLD = 300.0f

        const val MIN_VIBRATION_THRESHOLD = 0.5f
        const val MAX_VIBRATION_THRESHOLD = 25.0f

        const val RATE_NORMAL = "NORMAL"
        const val RATE_GAME = "GAME"
    }
}
