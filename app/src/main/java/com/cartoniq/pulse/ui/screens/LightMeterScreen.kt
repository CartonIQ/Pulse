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
import com.cartoniq.pulse.ui.components.LuxSunVisual
import com.cartoniq.pulse.ui.components.MetricValue
import com.cartoniq.pulse.ui.components.PulseTopBar
import com.cartoniq.pulse.ui.components.SensorStatusChip
import com.cartoniq.pulse.ui.theme.DeepSpace
import com.cartoniq.pulse.ui.theme.ElectricCyan
import com.cartoniq.pulse.ui.theme.SurfaceBorder
import com.cartoniq.pulse.ui.theme.SurfaceCard
import com.cartoniq.pulse.ui.theme.TextMuted
import com.cartoniq.pulse.ui.theme.TextPrimary
import com.cartoniq.pulse.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun LightMeterScreen(
    sensorHelper: SensorManagerHelper,
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sensorManager = remember { sensorHelper.getSensorManager() }
    val lightSensor = remember { sensorHelper.getSensor(Sensor.TYPE_LIGHT) }
    val isSensorAvailable = lightSensor != null

    var isPaused by remember { mutableStateOf(false) }
    var currentLux by remember { mutableFloatStateOf(0f) }
    var minLux by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var maxLux by remember { mutableFloatStateOf(0f) }
    var accuracy by remember { mutableStateOf(SensorAccuracyLevel.HIGH) }

    SensorSubscription(
        sensorManager = sensorManager,
        sensor = lightSensor,
        samplingPeriodUs = if (preferencesManager.sampleRateMode == PreferencesManager.RATE_GAME) {
            SensorManager.SENSOR_DELAY_GAME
        } else {
            SensorManager.SENSOR_DELAY_NORMAL
        },
        isActive = isSensorAvailable && !isPaused,
        onSensorChanged = { event ->
            val lux = event.values[0]
            currentLux = lux
            if (lux < minLux) minLux = lux
            if (lux > maxLux) maxLux = lux
        },
        onAccuracyChanged = { _, acc ->
            accuracy = SensorManagerHelper.mapAccuracy(acc)
        }
    )

    val category = SensorCalculations.classifyLux(currentLux)
    val categoryString = when (category) {
        SensorCalculations.LuxCategory.EXTREME_DARKNESS -> stringResource(R.string.lux_extreme_darkness)
        SensorCalculations.LuxCategory.DIM_INTERIOR -> stringResource(R.string.lux_dim_interior)
        SensorCalculations.LuxCategory.NORMAL_INDOOR -> stringResource(R.string.lux_normal_indoor)
        SensorCalculations.LuxCategory.BRIGHT_OFFICE -> stringResource(R.string.lux_bright_office)
        SensorCalculations.LuxCategory.OVERCAST_DAYLIGHT -> stringResource(R.string.lux_overcast_day)
        SensorCalculations.LuxCategory.DIRECT_SUNLIGHT -> stringResource(R.string.lux_direct_sunlight)
    }

    val displayMin = if (minLux == Float.MAX_VALUE) 0f else minLux

    Scaffold(
        topBar = {
            PulseTopBar(
                title = stringResource(R.string.light_screen_title),
                subtitle = stringResource(R.string.light_screen_subtitle),
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
                    sensorName = "Photocell / Ambient Light Sensor",
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

            // Animated Sun Radial Visual
            LuxSunVisual(lux = currentLux)

            // Primary Numeric Metric (Lux)
            MetricValue(
                value = String.format(Locale.US, "%.0f", currentLux),
                unit = stringResource(R.string.unit_lux),
                color = ElectricCyan,
                accessibilityDescription = "${String.format(Locale.US, "%.0f", currentLux)} lux illuminance"
            )

            // Environmental Classification Bracket Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(16.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.light_category_label),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = categoryString,
                        color = ElectricCyan,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Sub-metrics: Min Lux & Max Lux
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
                            text = stringResource(R.string.light_min_label),
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format(Locale.US, "%.0f lx", displayMin),
                            color = TextPrimary,
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
                            text = stringResource(R.string.light_max_label),
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format(Locale.US, "%.0f lx", maxLux),
                            color = ElectricCyan,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
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
                    text = stringResource(R.string.btn_reset_peak),
                    onClick = {
                        minLux = currentLux
                        maxLux = currentLux
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Physical Sensor Placement Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.light_disclaimer),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
