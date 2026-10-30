package com.cartoniq.pulse

import android.app.Application
import com.cartoniq.pulse.ads.AdManager
import com.cartoniq.pulse.data.PreferencesManager

class PulseApp : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)

        // Initialize Google Mobile Ads SDK once on application startup
        AdManager.initializeMobileAds(this)
    }
}
