package com.cartoniq.pulse.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.pulse.ui.theme.ElectricCyan
import com.cartoniq.pulse.ui.theme.TextMuted

@Composable
fun MetricValue(
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    color: Color = ElectricCyan,
    accessibilityDescription: String = "$value $unit"
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = accessibilityDescription
        },
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = value,
            color = color,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 42.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = unit,
            color = TextMuted,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )
    }
}
