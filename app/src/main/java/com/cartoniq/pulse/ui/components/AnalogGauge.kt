package com.cartoniq.pulse.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.cartoniq.pulse.ui.theme.ElectricCyan
import com.cartoniq.pulse.ui.theme.NeonPink
import com.cartoniq.pulse.ui.theme.SurfaceBorder
import com.cartoniq.pulse.ui.theme.SurfaceCard
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Canvas analog gauge instrument with animated needle and sci-fi aesthetic.
 */
@Composable
fun AnalogGauge(
    value: Float,
    minValue: Float = 0f,
    maxValue: Float = 200f,
    threshold: Float = 75f,
    modifier: Modifier = Modifier
        .width(260.dp)
        .height(180.dp)
) {
    val clampedValue = value.coerceIn(minValue, maxValue)
    val fraction = (clampedValue - minValue) / (maxValue - minValue).coerceAtLeast(1f)

    // Smoothly damped needle angle (-135 degrees to +135 degrees)
    val targetAngle = -135f + fraction * 270f
    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "NeedleAngle"
    )

    // Dynamic color shifting from Electric Cyan to Neon Pink
    val needleColor = if (value >= threshold) NeonPink else ElectricCyan

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.82f)
            val radius = size.width * 0.40f

            // 1. Background Arc (270 degrees total, start at 135 deg)
            drawArc(
                color = SurfaceBorder,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. Active Gradient Arc
            val activeSweep = (fraction * 270f).coerceIn(0f, 270f)
            if (activeSweep > 0.5f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(ElectricCyan, NeonPink),
                        center = center
                    ),
                    startAngle = 135f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 3. Dial Tick Marks
            val totalTicks = 20
            for (i in 0..totalTicks) {
                val tickFraction = i.toFloat() / totalTicks.toFloat()
                val tickAngle = 135f + tickFraction * 270f
                val isMajor = i % 5 == 0
                val tickLength = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                val tickWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx()
                val tickColor = if (tickFraction * (maxValue - minValue) >= threshold) {
                    NeonPink.copy(alpha = 0.8f)
                } else {
                    ElectricCyan.copy(alpha = 0.6f)
                }

                val angleRad = (tickAngle * PI / 180f).toFloat()
                val outerPoint = Offset(
                    center.x + (radius - 12.dp.toPx()) * cos(angleRad),
                    center.y + (radius - 12.dp.toPx()) * sin(angleRad)
                )
                val innerPoint = Offset(
                    center.x + (radius - 12.dp.toPx() - tickLength) * cos(angleRad),
                    center.y + (radius - 12.dp.toPx() - tickLength) * sin(angleRad)
                )

                drawLine(
                    color = tickColor,
                    start = innerPoint,
                    end = outerPoint,
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // 4. Center Hub
            drawCircle(
                color = SurfaceCard,
                radius = 16.dp.toPx(),
                center = center
            )
            drawCircle(
                color = SurfaceBorder,
                radius = 16.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = needleColor,
                radius = 6.dp.toPx(),
                center = center
            )

            // 5. Analog Needle
            rotate(degrees = animatedAngle, pivot = center) {
                val needleLength = radius - 8.dp.toPx()
                val needlePath = Path().apply {
                    moveTo(center.x - 3.dp.toPx(), center.y)
                    lineTo(center.x, center.y - needleLength)
                    lineTo(center.x + 3.dp.toPx(), center.y)
                    close()
                }
                drawPath(path = needlePath, color = needleColor)

                // Needle tip glow line
                drawLine(
                    color = Color.White,
                    start = Offset(center.x, center.y - needleLength + 16.dp.toPx()),
                    end = Offset(center.x, center.y - needleLength),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
