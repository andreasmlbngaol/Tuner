package id.andreasmlbngaol.tuner.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin

/**
 * A small, living equalizer used while the tuner is waiting for a note.
 */
@Composable
fun SignalWaveform(
    level: Float,
    active: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Canvas(modifier = modifier) {
        val bars = 9
        val gap = 4.dp.toPx()
        val barWidth = (size.width - gap * (bars - 1)) / bars
        val maxHeight = size.height
        val base = 0.18f + level.coerceIn(0f, 1f) * 0.5f

        repeat(bars) { index ->
            val wave = if (active) {
                abs(sin(phase + index * 0.75f)) * 0.5f
            } else {
                0f
            }
            val envelope = 1f - abs(index - (bars - 1) / 2f) / (bars + 2)
            val height = (maxHeight * (base * envelope + wave * envelope)).coerceAtLeast(2.5.dp.toPx())
            val left = index * (barWidth + gap)
            drawRoundRect(
                color = color.copy(alpha = 0.45f + 0.55f * envelope),
                topLeft = Offset(left, (maxHeight - height) / 2f),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}
