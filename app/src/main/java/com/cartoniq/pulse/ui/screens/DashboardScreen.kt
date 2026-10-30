package com.cartoniq.pulse.ui.screens

import android.hardware.Sensor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.pulse.R
import com.cartoniq.pulse.sensor.SensorAvailability
import com.cartoniq.pulse.sensor.SensorManagerHelper
import com.cartoniq.pulse.ui.components.BannerAdContainer
import com.cartoniq.pulse.ui.components.PulseTopBar
import com.cartoniq.pulse.ui.components.ToolCard
import com.cartoniq.pulse.ui.theme.DeepSpace
import com.cartoniq.pulse.ui.theme.ElectricCyan
import com.cartoniq.pulse.ui.theme.NeonPink
import com.cartoniq.pulse.ui.theme.StatusSuccess
import com.cartoniq.pulse.ui.theme.StatusWarning
import com.cartoniq.pulse.ui.theme.SurfaceBorder
import com.cartoniq.pulse.ui.theme.SurfaceCard
import com.cartoniq.pulse.ui.theme.TextMuted
import com.cartoniq.pulse.ui.theme.TextPrimary
import com.cartoniq.pulse.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    sensorHelper: SensorManagerHelper,
    onOpenMetal: () -> Unit,
    onOpenVibration: () -> Unit,
    onOpenLight: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasMagnetometer = remember { sensorHelper.hasSensor(Sensor.TYPE_MAGNETIC_FIELD) }
    val hasAccelerometer = remember { sensorHelper.hasSensor(Sensor.TYPE_ACCELEROMETER) }
    val hasLightSensor = remember { sensorHelper.hasSensor(Sensor.TYPE_LIGHT) }

    val magAvailability = if (hasMagnetometer) SensorAvailability.Available else SensorAvailability.Unavailable("Hardware missing")
    val vibAvailability = if (hasAccelerometer) SensorAvailability.Available else SensorAvailability.Unavailable("Hardware missing")
    val lightAvailability = if (hasLightSensor) SensorAvailability.Available else SensorAvailability.Unavailable("Hardware missing")

    val supportedSensorCount = listOf(hasMagnetometer, hasAccelerometer, hasLightSensor).count { it }

    Scaffold(
        topBar = {
            PulseTopBar(
                title = stringResource(R.string.app_full_name),
                subtitle = stringResource(R.string.app_tagline),
                showBackButton = false,
                onSettingsClick = onOpenSettings,
                onDiagnosticsClick = onOpenDiagnostics
            )
        },
        bottomBar = {
            // Anchored AdMob test banner container (never covers controls)
            BannerAdContainer()
        },
        containerColor = DeepSpace,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hardware Status Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(16.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.dashboard_header_tag),
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$supportedSensorCount / 3 SENSORS READY",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (supportedSensorCount == 3) StatusSuccess else StatusWarning,
                                        CircleShape
                                    )
                            )
                            Text(
                                text = if (supportedSensorCount == 3) "OPTIMAL" else "DEGRADED",
                                color = if (supportedSensorCount == 3) StatusSuccess else StatusWarning,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }

            // Tool 1: Magnetic Field / Metal Detector
            item {
                ToolCard(
                    title = stringResource(R.string.card_metal_title),
                    description = stringResource(R.string.card_metal_desc),
                    icon = Icons.Default.ElectricBolt,
                    availability = magAvailability,
                    accentColor = ElectricCyan,
                    onClick = onOpenMetal
                )
            }

            // Tool 2: Vibration Meter
            item {
                ToolCard(
                    title = stringResource(R.string.card_vibration_title),
                    description = stringResource(R.string.card_vibration_desc),
                    icon = Icons.Default.Vibration,
                    availability = vibAvailability,
                    accentColor = NeonPink,
                    onClick = onOpenVibration
                )
            }

            // Tool 3: Ambient Light Meter
            item {
                ToolCard(
                    title = stringResource(R.string.card_light_title),
                    description = stringResource(R.string.card_light_desc),
                    icon = Icons.Default.LightMode,
                    availability = lightAvailability,
                    accentColor = ElectricCyan,
                    onClick = onOpenLight
                )
            }

            // Bottom Brand Footnote
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.brand_credit),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
