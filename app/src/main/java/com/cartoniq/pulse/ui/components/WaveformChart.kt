package com.cartoniq.pulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cartoniq.pulse.ui.theme.DeepSpace
import com.cartoniq.pulse.ui.theme.ElectricCyan
import com.cartoniq.pulse.ui.theme.NeonPink
import com.cartoniq.pulse.ui.theme.SurfaceBorder

@Composable
fun WaveformChart(
    samples: List<Float>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(140.dp),
    maxScale: Float = 10f,
    alertThreshold: Float = 2.5f
) {
    Canvas(
        modifier = modifier
            .background(DeepSpace, RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // 1. Draw horizontal reference grid
        val gridLines = 4
        for (i in 1..gridLines) {
            val y = (height / (gridLines + 1)) * i
            drawLine(
                color = SurfaceBorder.copy(alpha = 0.4f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Center zero-reference line
        drawLine(
            color = SurfaceBorder.copy(alpha = 0.8f),
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = 1.5.dp.toPx()
        )

        if (samples.isEmpty()) return@Canvas

        // 2. Draw rolling trace
        val stepX = width / (samples.size - 1).coerceAtLeast(1)
        val strokePath = Path()
        val fillPath = Path()

        var started = false
        var hasAlertCrossing = false

        samples.forEachIndexed { index, sampleVal ->
            val x = index * stepX
            // Normalize sample value around center line
            val normalized = (sampleVal / maxScale.coerceAtLeast(1f)).coerceIn(-1f, 1f)
            val y = midY - (normalized * (midY - 10.dp.toPx()))

            if (sampleVal >= alertThreshold) {
                hasAlertCrossing = true
            }

            if (!started) {
                strokePath.moveTo(x, y)
                fillPath.moveTo(x, midY)
                fillPath.lineTo(x, y)
                started = true
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Close fill area towards center baseline
        val lastX = (samples.size - 1) * stepX
        fillPath.lineTo(lastX, midY)
        fillPath.close()

        val traceColor = if (hasAlertCrossing) NeonPink else ElectricCyan

        // Gradient fill under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    traceColor.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )
        )

        // Draw crisp stroke line
        drawPath(
            path = strokePath,
            color = traceColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}
