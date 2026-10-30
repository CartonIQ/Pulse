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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.cartoniq.pulse.ui.components.AnalogGauge
import com.cartoniq.pulse.ui.components.ControlButton
import com.cartoniq.pulse.ui.components.EmptySensorState
import com.cartoniq.pulse.ui.components.MetricValue
import com.cartoniq.pulse.ui.components.PulseTopBar
import com.cartoniq.pulse.ui.components.SensorStatusChip
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
fun MetalDetectorScreen(
    sensorHelper: SensorManagerHelper,
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sensorManager = remember { sensorHelper.getSensorManager() }
    val magSensor = remember { sensorHelper.getSensor(Sensor.TYPE_MAGNETIC_FIELD) }
    val isSensorAvailable = magSensor != null

    var isPaused by remember { mutableStateOf(false) }
    var rawX by remember { mutableFloatStateOf(0f) }
    var rawY by remember { mutableFloatStateOf(0f) }
    var rawZ by remember { mutableFloatStateOf(0f) }
    var magnitude by remember { mutableFloatStateOf(0f) }
    var baseline by remember { mutableFloatStateOf(0f) }
    var peakMagnitude by remember { mutableFloatStateOf(0f) }
    var accuracy by remember { mutableStateOf(SensorAccuracyLevel.HIGH) }
    var lastUpdateTimestamp by remember { mutableLongStateOf(0L) }

    val userThreshold = preferencesManager.magneticThreshold
    val isSoundEnabled = preferencesManager.isSoundEnabled
    val isHapticsEnabled = preferencesManager.isHapticsEnabled

    // Lifecycle-aware sensor registration: unregisters on pause / dispose
    SensorSubscription(
        sensorManager = sensorManager,
        sensor = magSensor,
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
            val mag = SensorCalculations.vectorMagnitude(x, y, z)

            rawX = x
            rawY = y
            rawZ = z
            magnitude = mag

            if (mag > peakMagnitude) {
                peakMagnitude = mag
            }
            lastUpdateTimestamp = System.currentTimeMillis()

            // Threshold alert check
            if (SensorCalculations.isThresholdExceeded(mag, userThreshold)) {
                if (isSoundEnabled) sensorHelper.playAlertBeep()
                if (isHapticsEnabled) sensorHelper.triggerHapticFeedback()
            }
        },
        onAccuracyChanged = { _, acc ->
            accuracy = SensorManagerHelper.mapAccuracy(acc)
        }
    )

    val deviation = SensorCalculations.magneticDeviation(magnitude, baseline)

    Scaffold(
        topBar = {
            PulseTopBar(
                title = stringResource(R.string.metal_screen_title),
                subtitle = stringResource(R.string.metal_screen_subtitle),
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
                    sensorName = "Magnetometer (Magnetic Field)",
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
            // Status Chip & Accuracy
            SensorStatusChip(
                availability = SensorAvailability.Available,
                isPaused = isPaused,
                accuracy = accuracy
            )

            // Canvas Analog Gauge Instrument
            AnalogGauge(
                value = magnitude,
                minValue = 0f,
                maxValue = 150f,
                threshold = userThreshold
            )

            // Primary Numeric Metric (Total Field Magnitude)
            MetricValue(
                value = String.format(Locale.US, "%.1f", magnitude),
                unit = stringResource(R.string.unit_microtesla),
                color = if (magnitude >= userThreshold) NeonPink else ElectricCyan,
                accessibilityDescription = "${String.format(Locale.US, "%.1f", magnitude)} microtesla total magnetic flux"
            )

            // Metrics Grid: Baseline, Deviation, Peak Hold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSubCard(
                    title = stringResource(R.string.metal_deviation_label),
                    value = String.format(Locale.US, "%+.1f", deviation),
                    unit = stringResource(R.string.unit_microtesla),
                    modifier = Modifier.weight(1f),
                    highlightColor = if (deviation > 15f) NeonPink else ElectricCyan
                )
                MetricSubCard(
                    title = stringResource(R.string.metal_peak_label),
                    value = String.format(Locale.US, "%.1f", peakMagnitude),
                    unit = stringResource(R.string.unit_microtesla),
                    modifier = Modifier.weight(1f)
                )
                MetricSubCard(
                    title = stringResource(R.string.metal_baseline_label),
                    value = String.format(Locale.US, "%.1f", baseline),
                    unit = stringResource(R.string.unit_microtesla),
                    modifier = Modifier.weight(1f)
                )
            }

            // 3-Axis Vector Breakdown Card (X, Y, Z)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(16.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.metal_axis_breakdown),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AxisItem(label = "X", value = rawX)
                        AxisItem(label = "Y", value = rawY)
                        AxisItem(label = "Z", value = rawZ)
                    }
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
                ControlButton(
                    text = stringResource(R.string.btn_zero_baseline),
                    onClick = { baseline = magnitude },
                    modifier = Modifier.weight(1f)
                )
                ControlButton(
                    text = stringResource(R.string.btn_reset_peak),
                    onClick = { peakMagnitude = magnitude },
                    modifier = Modifier.weight(1f)
                )
            }

            // Technical Disclaimers and Calibration Guide
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.metal_disclaimer),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Text(
                        text = stringResource(R.string.metal_calibration_guide),
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricSubCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    highlightColor: androidx.compose.ui.graphics.Color = ElectricCyan
) {
    Box(
        modifier = modifier
            .background(SurfaceCard, RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = value,
                    color = highlightColor,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit,
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AxisItem(
    label: String,
    value: Float
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            color = TextMuted,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = String.format(Locale.US, "%+.1f", value),
            color = TextPrimary,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
