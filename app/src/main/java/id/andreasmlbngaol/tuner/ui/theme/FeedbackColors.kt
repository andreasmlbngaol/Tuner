package id.andreasmlbngaol.tuner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Functional colours for pitch direction. These stay constant across dynamic
 * palettes so "flat", "sharp" and "in tune" never look alike.
 */
@Immutable
data class TuningFeedbackColors(
    val upAccent: Color,
    val upContainer: Color,
    val onUpContainer: Color,
    val downAccent: Color,
    val downContainer: Color,
    val onDownContainer: Color,
    val inTuneAccent: Color,
    val inTuneContainer: Color,
    val onInTuneContainer: Color,
)

private val LightFeedbackColors = TuningFeedbackColors(
    upAccent = Color(0xFFB26A00),
    upContainer = Color(0xFFFFDDB0),
    onUpContainer = Color(0xFF2B1700),
    downAccent = Color(0xFF2F6DB5),
    downContainer = Color(0xFFD6E3FF),
    onDownContainer = Color(0xFF001B3E),
    inTuneAccent = Color(0xFF2E7D32),
    inTuneContainer = Color(0xFFB7F0BE),
    onInTuneContainer = Color(0xFF002106),
)

private val DarkFeedbackColors = TuningFeedbackColors(
    upAccent = Color(0xFFFFC94D),
    upContainer = Color(0xFF4A3200),
    onUpContainer = Color(0xFFFFE0A3),
    downAccent = Color(0xFFA8C7FA),
    downContainer = Color(0xFF123A66),
    onDownContainer = Color(0xFFD6E3FF),
    inTuneAccent = Color(0xFF8BD9A0),
    inTuneContainer = Color(0xFF0B4A22),
    onInTuneContainer = Color(0xFFA6F4BE),
)

@Composable
fun rememberTuningFeedbackColors(): TuningFeedbackColors =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
        DarkFeedbackColors
    } else {
        LightFeedbackColors
    }
