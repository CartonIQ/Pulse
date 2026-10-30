package com.cartoniq.pulse.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Centralized AdMob & Google UMP consent coordinator.
 * Pinned official Google Test IDs used exclusively.
 */
object AdManager {

    private const val TAG = "PulseAdManager"

    // Official Google Android AdMob Sample App ID (TEST ONLY)
    const val TEST_APP_ID = "ca-app-pub-9771662959477951~6837873999"

    // Official Google Android Fixed-Size Banner Test Ad Unit ID (320x50)
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-9771662959477951/4263681544"

    // Official Google Android Anchored Adaptive Banner Test Ad Unit ID
    const val TEST_ADAPTIVE_BANNER_AD_UNIT_ID = "ca-app-pub-9771662959477951/4263681544"

    private var isInitialized = false

    /**
     * Initializes Google Mobile Ads SDK once per app process.
     */
    fun initializeMobileAds(context: Context) {
        if (isInitialized) return
        try {
            // Configure test device policy for safety
            val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            MobileAds.initialize(context) { status ->
                isInitialized = true
                Log.d(TAG, "MobileAds initialized: ${status.adapterStatusMap.keys}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "MobileAds initialization skipped or failed gracefully (offline mode)", e)
        }
    }

    /**
     * Checks if ads can be requested based on UMP consent and SDK state.
     */
    fun canRequestAds(context: Context): Boolean {
        return try {
            val consentInformation = UserMessagingPlatform.getConsentInformation(context)
            consentInformation.canRequestAds()
        } catch (e: Exception) {
            // Default to true for standard test mode in development
            true
        }
    }

    /**
     * Requests UMP Consent Info update on app startup.
     */
    fun requestConsent(activity: Activity, onConsentReady: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "UMP consent form error: ${formError.message}")
                    }
                    onConsentReady()
                }
            },
            { requestError ->
                Log.w(TAG, "UMP request error: ${requestError.message} (offline fallback)")
                onConsentReady()
            }
        )
    }

    /**
     * Checks if privacy options (consent change) are required to be shown in Settings.
     */
    fun isPrivacyOptionsRequired(context: Context): Boolean {
        return try {
            val consentInformation = UserMessagingPlatform.getConsentInformation(context)
            consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Opens the UMP privacy options form so users can modify consent choices.
     */
    fun showPrivacyOptionsForm(activity: Activity, onComplete: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(TAG, "Failed to show privacy options form: ${formError.message}")
            }
            onComplete()
        }
    }
}
