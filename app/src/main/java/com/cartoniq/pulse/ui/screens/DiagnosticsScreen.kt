package com.cartoniq.pulse.ui.screens

import android.hardware.Sensor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.cartoniq.pulse.sensor.SensorHardwareInfo
import com.cartoniq.pulse.sensor.SensorManagerHelper
import com.cartoniq.pulse.ui.components.PulseTopBar
import com.cartoniq.pulse.ui.theme.DeepSpace
import com.cartoniq.pulse.ui.theme.ElectricCyan
import com.cartoniq.pulse.ui.theme.StatusError
import com.cartoniq.pulse.ui.theme.StatusSuccess
import com.cartoniq.pulse.ui.theme.SurfaceBorder
import com.cartoniq.pulse.ui.theme.SurfaceCard
import com.cartoniq.pulse.ui.theme.TextMuted
import com.cartoniq.pulse.ui.theme.TextPrimary
import com.cartoniq.pulse.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun DiagnosticsScreen(
    sensorHelper: SensorManagerHelper,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSensors = remember { sensorHelper.getAllSensors() }

    val hasMag = remember { sensorHelper.hasSensor(Sensor.TYPE_MAGNETIC_FIELD) }
    val hasAcc = remember { sensorHelper.hasSensor(Sensor.TYPE_ACCELEROMETER) }
    val hasLinear = remember { sensorHelper.hasSensor(Sensor.TYPE_LINEAR_ACCELERATION) }
    val hasLight = remember { sensorHelper.hasSensor(Sensor.TYPE_LIGHT) }

    Scaffold(
        topBar = {
            PulseTopBar(
                title = stringResource(R.string.diagnostics_title),
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = DeepSpace,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Core Hardware Health Check Summary
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(16.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.diagnostics_core_header),
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        CoreSensorStatusRow(label = "Magnetometer (Magnetic Field)", isAvailable = hasMag)
                        CoreSensorStatusRow(label = "Accelerometer (Linear + Gravity)", isAvailable = hasAcc)
                        CoreSensorStatusRow(label = "Linear Acceleration (Hardware Filtered)", isAvailable = hasLinear)
                        CoreSensorStatusRow(label = "Photocell (Ambient Light)", isAvailable = hasLight)
                    }
                }
            }

            // Total Count Header
            item {
                Text(
                    text = stringResource(R.string.diagnostics_count_format, allSensors.size),
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // List of all installed hardware sensors
            items(allSensors) { sensor ->
                SensorDetailCard(sensor = sensor)
            }
        }
    }
}

@Composable
private fun CoreSensorStatusRow(
    label: String,
    isAvailable: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isAvailable) StatusSuccess else StatusError, CircleShape)
            )
            Text(
                text = if (isAvailable) "ONLINE" else "ABSENT",
                color = if (isAvailable) StatusSuccess else StatusError,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun SensorDetailCard(sensor: SensorHardwareInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = sensor.typeName,
                color = ElectricCyan,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = sensor.name,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.sensor_vendor_format, sensor.vendor, sensor.version),
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = stringResource(
                    R.string.sensor_power_format,
                    sensor.powerMa,
                    sensor.maxRange,
                    sensor.resolution
                ),
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
