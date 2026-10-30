package com.cartoniq.pulse.ui.screens

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.pulse.R
import com.cartoniq.pulse.data.PreferencesManager
import com.cartoniq.pulse.domain.SensorCalculations
import com.cartoniq.pulse.sensor.SensorAccuracyLevel
import com.cartoniq.pulse.sensor.SensorAvailability
import com.cartoniq.pulse.sensor.SensorManagerHelper
import com.cartoniq.pulse.sensor.SensorSubscription
import com.cartoniq.pulse.ui.components.ControlButton
import com.cartoniq.pulse.ui.components.EmptySensorState
import com.cartoniq.pulse.ui.components.MetricValue
import com.cartoniq.pulse.ui.components.PulseTopBar
import com.cartoniq.pulse.ui.components.SensorStatusChip
import com.cartoniq.pulse.ui.components.WaveformChart
import com.cartoniq.pulse.ui.theme.DeepSpace
import com.cartoniq.pulse.ui.theme.ElectricCyan
import com.cartoniq.pulse.ui.theme.NeonPink
import com.cartoniq.pulse.ui.theme.SurfaceBorder
import com.cartoniq.pulse.ui.theme.SurfaceCard
import com.cartoniq.pulse.ui.theme.TextMuted
import com.cartoniq.pulse.ui.theme.TextPrimary
import com.cartoniq.pulse.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun VibrationScreen(
    sensorHelper: SensorManagerHelper,
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sensorManager = remember { sensorHelper.getSensorManager() }
    val linearSensor = remember { sensorHelper.getSensor(Sensor.TYPE_LINEAR_ACCELERATION) }
    val rawAccSensor = remember { sensorHelper.getSensor(Sensor.TYPE_ACCELEROMETER) }

    // Prefer linear acceleration if present; fallback to raw accelerometer with gravity filter
    val activeSensor = linearSensor ?: rawAccSensor
    val isSensorAvailable = activeSensor != null
    val isHardwareLinear = linearSensor != null

    val gravityFilter = remember { SensorCalculations.GravityFilter() }

    var isPaused by remember { mutableStateOf(false) }
    var showDynamicOnly by remember { mutableStateOf(true) }

    var rawX by remember { mutableFloatStateOf(0f) }
    var rawY by remember { mutableFloatStateOf(0f) }
    var rawZ by remember { mutableFloatStateOf(0f) }

    var currentMag by remember { mutableFloatStateOf(0f) }
    var currentG by remember { mutableFloatStateOf(0f) }
    var peakG by remember { mutableFloatStateOf(0f) }
    var rollingSumG by remember { mutableFloatStateOf(0f) }
    var sampleCount by remember { mutableLongStateOf(0L) }
    var accuracy by remember { mutableStateOf(SensorAccuracyLevel.HIGH) }

    // Fixed bounded buffer of 120 samples for oscilloscope waveform
    val waveformSamples = remember { mutableStateListOf<Float>() }

    val userThreshold = preferencesManager.vibrationThreshold
    val isSoundEnabled = preferencesManager.isSoundEnabled
    val isHapticsEnabled = preferencesManager.isHapticsEnabled

    SensorSubscription(
        sensorManager = sensorManager,
        sensor = activeSensor,
        samplingPeriodUs = if (preferencesManager.sampleRateMode == PreferencesManager.RATE_GAME) {
            SensorManager.SENSOR_DELAY_GAME
        } else {
            SensorManager.SENSOR_DELAY_NORMAL
        },
        isActive = isSensorAvailable && !isPaused,
        onSensorChanged = { event ->
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            rawX = x
            rawY = y
            rawZ = z

            val displayMag: Float
            val displayG: Float

            if (isHardwareLinear) {
                val mag = SensorCalculations.vectorMagnitude(x, y, z)
                val g = SensorCalculations.toGForce(mag)
                displayMag = mag
                displayG = g
            } else {
                val filtered = gravityFilter.update(x, y, z)
                if (showDynamicOnly) {
                    displayMag = filtered.dynamicMagnitude
                    displayG = filtered.dynamicGForce
                } else {
                    displayMag = filtered.totalMagnitude
                    displayG = filtered.totalGForce
                }
            }

            currentMag = displayMag
            currentG = displayG

            if (displayG > peakG) {
                peakG = displayG
            }

            sampleCount++
            rollingSumG += displayG

            // Keep waveform samples buffer strictly bounded to 120 samples
            if (waveformSamples.size >= 120) {
                waveformSamples.removeAt(0)
            }
            waveformSamples.add(displayMag)

            // Alert threshold check
            if (SensorCalculations.isThresholdExceeded(displayMag, userThreshold)) {
                if (isSoundEnabled) sensorHelper.playAlertBeep()
                if (isHapticsEnabled) sensorHelper.triggerHapticFeedback()
            }
        },
        onAccuracyChanged = { _, acc ->
            accuracy = SensorManagerHelper.mapAccuracy(acc)
        }
    )

    val avgG = if (sampleCount > 0) rollingSumG / sampleCount else 0f

    Scaffold(
        topBar = {
            PulseTopBar(
                title = stringResource(R.string.vibration_screen_title),
                subtitle = stringResource(R.string.vibration_screen_subtitle),
                showBackButton = true,
                onBackClick = onNavigateBack,
                onDiagnosticsClick = onOpenDiagnostics
            )
        },
        containerColor = DeepSpace,
        modifier = modifier
    ) { innerPadding ->
        if (!isSensorAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptySensorState(
                    sensorName = "Accelerometer",
                    onOpenDiagnostics = onOpenDiagnostics
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Chip & Sensor Mode Indicator
            SensorStatusChip(
                availability = SensorAvailability.Available,
                isPaused = isPaused,
                accuracy = accuracy
            )

            // Sensor source badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHardwareLinear) {
                            stringResource(R.string.vibration_sensor_source_linear)
                        } else {
                            stringResource(R.string.vibration_sensor_source_filtered)
                        },
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (showDynamicOnly) "DYNAMIC" else "RAW TOTAL",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Real-Time Rolling Waveform Canvas Oscilloscope
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.vibration_waveform_title),
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                WaveformChart(
                    samples = waveformSamples,
                    maxScale = (userThreshold * 1.5f).coerceAtLeast(10f),
                    alertThreshold = userThreshold
                )
            }

            // Primary Dynamic Metric (m/s² and g)
            MetricValue(
                value = String.format(Locale.US, "%.2f", currentMag),
                unit = stringResource(R.string.unit_mps2),
                color = if (currentMag >= userThreshold) NeonPink else ElectricCyan,
                accessibilityDescription = "${String.format(Locale.US, "%.2f", currentMag)} meters per second squared"
            )

            // Dynamic G-Force sub-readout
            Text(
                text = String.format(Locale.US, "≈ %.3f g-force", currentG),
                color = if (currentMag >= userThreshold) NeonPink else TextSecondary,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )

            // Sub-metrics: Peak G, Average G
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceCard, RoundedCornerShape(12.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.vibration_peak_label),
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format(Locale.US, "%.3f g", peakG),
                            color = if (peakG * 9.80665f >= userThreshold) NeonPink else ElectricCyan,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceCard, RoundedCornerShape(12.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.vibration_avg_label),
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format(Locale.US, "%.3f g", avgG),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Raw Vector Components Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(16.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "X: ${String.format(Locale.US, "%+.2f", rawX)}", color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Text(text = "Y: ${String.format(Locale.US, "%+.2f", rawY)}", color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Text(text = "Z: ${String.format(Locale.US, "%+.2f", rawZ)}", color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                }
            }

            // Interactive Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ControlButton(
                    text = if (isPaused) stringResource(R.string.btn_resume) else stringResource(R.string.btn_pause),
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier.weight(1f)
                )
                if (!isHardwareLinear) {
                    ControlButton(
                        text = if (showDynamicOnly) stringResource(R.string.vibration_mode_raw) else stringResource(R.string.vibration_mode_dynamic),
                        onClick = { showDynamicOnly = !showDynamicOnly },
                        modifier = Modifier.weight(1f)
                    )
                }
                ControlButton(
                    text = stringResource(R.string.btn_reset_peak),
                    onClick = {
                        peakG = currentG
                        rollingSumG = currentG
                        sampleCount = 1L
                        waveformSamples.clear()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Educational Context & Disclaimer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.vibration_disclaimer),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
