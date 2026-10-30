package com.cartoniq.pulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cartoniq.pulse.ads.AdManager
import com.cartoniq.pulse.navigation.PulseNavGraph
import com.cartoniq.pulse.sensor.SensorManagerHelper
import com.cartoniq.pulse.ui.theme.DeepSpace
import com.cartoniq.pulse.ui.theme.PulseTheme

class MainActivity : ComponentActivity() {

    private lateinit var sensorHelper: SensorManagerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as PulseApp
        sensorHelper = SensorManagerHelper(this)

        // Request UMP consent update safely on startup
        AdManager.requestConsent(this) {
            // Consent ready or failed gracefully
        }

        setContent {
            PulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepSpace
                ) {
                    PulseNavGraph(
                        sensorHelper = sensorHelper,
                        preferencesManager = app.preferencesManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorHelper.release()
    }
}
