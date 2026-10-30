package com.cartoniq.pulse.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cartoniq.pulse.ui.theme.ElectricCyan
import com.cartoniq.pulse.ui.theme.StatusWarning
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin

@Composable
fun LuxSunVisual(
    lux: Float,
    modifier: Modifier = Modifier
        .width(220.dp)
        .height(180.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SunRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    // Logarithmic normalization for dynamic optical response across 0.1 to 50,000 lx
    val logLux = log10(lux.coerceAtLeast(0.1f)).coerceIn(0f, 4.7f) // 10^4.7 ~ 50,000 lx
    val intensityFraction = (logLux / 4.7f).coerceIn(0.15f, 1.0f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = 28.dp.toPx()
            val coreRadius = baseRadius * (0.8f + intensityFraction * 0.4f)

            val sunColor = when {
                lux < 50f -> ElectricCyan
                lux < 1000f -> StatusWarning
                else -> Color(0xFFFFE066)
            }

            // 1. Outer Corona Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        sunColor.copy(alpha = 0.35f * intensityFraction),
                        sunColor.copy(alpha = 0.08f * intensityFraction),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 2.2f
                ),
                radius = coreRadius * 2.2f,
                center = center
            )

            // 2. Solar Rays
            val rayCount = 12
            val maxRayLen = 32.dp.toPx() * intensityFraction

            for (i in 0 until rayCount) {
                val angleDeg = (i * (360f / rayCount)) + rotation
                val angleRad = (angleDeg * PI / 180f).toFloat()

                val innerPoint = Offset(
                    center.x + (coreRadius + 6.dp.toPx()) * cos(angleRad),
                    center.y + (coreRadius + 6.dp.toPx()) * sin(angleRad)
                )
                val outerPoint = Offset(
                    center.x + (coreRadius + 6.dp.toPx() + maxRayLen) * cos(angleRad),
                    center.y + (coreRadius + 6.dp.toPx() + maxRayLen) * sin(angleRad)
                )

                drawLine(
                    color = sunColor.copy(alpha = 0.8f),
                    start = innerPoint,
                    end = outerPoint,
                    strokeWidth = (2.2.dp * intensityFraction).toPx().coerceAtLeast(1.5f),
                    cap = StrokeCap.Round
                )
            }

            // 3. Central Sun Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        sunColor
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // Inner orbital ring
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = coreRadius * 0.7f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
