package com.cartoniq.pulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.pulse.R
import com.cartoniq.pulse.sensor.SensorAccuracyLevel
import com.cartoniq.pulse.sensor.SensorAvailability
import com.cartoniq.pulse.ui.theme.ElectricCyan
import com.cartoniq.pulse.ui.theme.StatusError
import com.cartoniq.pulse.ui.theme.StatusPaused
import com.cartoniq.pulse.ui.theme.StatusSuccess
import com.cartoniq.pulse.ui.theme.StatusWarning
import com.cartoniq.pulse.ui.theme.SurfaceBorder
import com.cartoniq.pulse.ui.theme.SurfaceCardElevated

@Composable
fun SensorStatusChip(
    availability: SensorAvailability,
    isPaused: Boolean = false,
    accuracy: SensorAccuracyLevel = SensorAccuracyLevel.HIGH,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusText) = when {
        isPaused -> Pair(StatusPaused, stringResource(R.string.status_paused))
        availability is SensorAvailability.Unavailable -> Pair(StatusError, stringResource(R.string.status_unavailable))
        availability is SensorAvailability.Unknown -> Pair(StatusWarning, stringResource(R.string.status_waiting))
        accuracy == SensorAccuracyLevel.UNCALIBRATED -> Pair(StatusWarning, stringResource(R.string.status_uncalibrated))
        accuracy == SensorAccuracyLevel.LOW -> Pair(StatusWarning, stringResource(R.string.status_low_accuracy))
        else -> Pair(StatusSuccess, stringResource(R.string.status_active))
    }

    Row(
        modifier = modifier
            .background(SurfaceCardElevated, RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing status dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(statusColor, CircleShape)
        )
        Text(
            text = statusText,
            color = statusColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
