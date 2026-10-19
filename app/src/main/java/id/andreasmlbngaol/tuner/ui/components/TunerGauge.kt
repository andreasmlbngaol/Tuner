package id.andreasmlbngaol.tuner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.andreasmlbngaol.tuner.R
import id.andreasmlbngaol.tuner.data.TunerReading
import id.andreasmlbngaol.tuner.ui.theme.rememberTuningFeedbackColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val START_ANGLE = 150f
private const val SWEEP_ANGLE = 240f
private const val CENTS_RANGE = 50f
private const val DEGREES_PER_CENT = 2.4f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TunerGauge(
    reading: TunerReading?,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val feedbackColors = rememberTuningFeedbackColors()
    val inTune = reading?.inTune == true
    val targetCents = reading?.cents
        ?.coerceIn(-CENTS_RANGE.toDouble(), CENTS_RANGE.toDouble())
        ?.toFloat()
        ?: 0f
    val cents by animateFloatAsState(
        targetValue = targetCents,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "cents",
    )

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = colors.onSurfaceVariant.copy(alpha = 0.65f),
    )
    val labels = remember(labelStyle) {
        listOf("-50" to 150f, "+50" to 390f).map { (text, angle) ->
            Triple(text, angle, textMeasurer.measure(AnnotatedString(text), labelStyle))
        }
    }

    val description = if (reading != null) {
        val centsOff = abs(reading.cents.roundToInt())
        pluralStringResource(R.plurals.gauge_description, centsOff, reading.noteLabel, centsOff)
    } else {
        stringResource(R.string.listening_hint)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics(mergeDescendants = true) { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 44.dp.toPx()
            val stroke = 13.dp.toPx()
            val arcTopLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)

            if (inTune) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(feedbackColors.inTuneAccent.copy(alpha = 0.18f), Color.Transparent),
                        center = center,
                        radius = radius * 1.15f,
                    ),
                    radius = radius * 1.15f,
                    center = center,
                )
            }

            drawArc(
                color = colors.onSurfaceVariant.copy(alpha = 0.22f),
                startAngle = START_ANGLE,
                sweepAngle = SWEEP_ANGLE,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            // Target window: the middle ±5 cents.
            drawArc(
                color = feedbackColors.inTuneAccent.copy(alpha = if (inTune) 1f else 0.35f),
                startAngle = 270f - 5f * DEGREES_PER_CENT,
                sweepAngle = 10f * DEGREES_PER_CENT,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            val accent = when {
                reading == null -> colors.onSurfaceVariant.copy(alpha = 0.45f)
                inTune -> feedbackColors.inTuneAccent
                reading.cents < 0 -> feedbackColors.upAccent
                else -> feedbackColors.downAccent
            }

            if (reading != null && abs(cents) > 0.15f) {
                val sweep = cents * DEGREES_PER_CENT
                drawArc(
                    color = accent,
                    startAngle = if (sweep >= 0f) 270f else 270f + sweep,
                    sweepAngle = abs(sweep),
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }

            // Scale labels at the two ends of the arc.
            labels.forEach { (_, angleDeg, layout) ->
                val angle = Math.toRadians(angleDeg.toDouble())
                val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                drawLine(
                    color = colors.onSurfaceVariant.copy(alpha = 0.45f),
                    start = center + direction * (radius + 9.dp.toPx()),
                    end = center + direction * (radius + 15.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                val labelCenter = center + direction * (radius + 26.dp.toPx())
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        labelCenter.x - layout.size.width / 2f,
                        labelCenter.y - layout.size.height / 2f,
                    ),
                )
            }

            // Center reference tick.
            drawLine(
                color = colors.onSurfaceVariant.copy(alpha = 0.45f),
                start = Offset(center.x, center.y - radius - 9.dp.toPx()),
                end = Offset(center.x, center.y - radius - 15.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )

            // Knob riding on the arc.
            val knobAngle = Math.toRadians((270f + cents * DEGREES_PER_CENT).toDouble())
            val knobDirection = Offset(cos(knobAngle).toFloat(), sin(knobAngle).toFloat())
            val knobCenter = center + knobDirection * radius
            drawCircle(color = colors.surface, radius = 13.dp.toPx(), center = knobCenter)
            drawCircle(color = accent, radius = 8.dp.toPx(), center = knobCenter)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 26.dp),
        ) {
            if (reading == null) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant.copy(alpha = 0.28f),
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .size(76.dp),
                )
            } else {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = reading.noteName,
                        style = MaterialTheme.typography.displayLargeEmphasized.copy(
                            fontSize = 104.sp,
                            lineHeight = 104.sp,
                            fontWeight = FontWeight.ExtraBold,
                        ),
                        color = if (inTune) feedbackColors.inTuneAccent else colors.onSurface,
                    )
                    Text(
                        text = reading.octave.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 18.dp, start = 4.dp),
                    )
                }
            }

            Text(
                text = if (reading != null) {
                    stringResource(R.string.frequency_format, reading.frequency)
                } else {
                    stringResource(R.string.waiting_for_note)
                },
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            if (reading != null) {
                val pillContainer = when {
                    inTune -> feedbackColors.inTuneContainer
                    reading.cents < 0 -> feedbackColors.upContainer
                    else -> feedbackColors.downContainer
                }
                val pillContent = when {
                    inTune -> feedbackColors.onInTuneContainer
                    reading.cents < 0 -> feedbackColors.onUpContainer
                    else -> feedbackColors.onDownContainer
                }
                Surface(
                    shape = CircleShape,
                    color = pillContainer,
                    contentColor = pillContent,
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            imageVector = when {
                                inTune -> Icons.Rounded.Check
                                reading.cents < 0 -> Icons.Rounded.ArrowUpward
                                else -> Icons.Rounded.ArrowDownward
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = if (inTune) {
                                stringResource(R.string.in_tune_short)
                            } else {
                                stringResource(R.string.cents_value, abs(reading.cents).roundToInt())
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
