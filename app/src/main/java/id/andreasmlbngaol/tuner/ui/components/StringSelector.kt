package id.andreasmlbngaol.tuner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.andreasmlbngaol.tuner.R
import id.andreasmlbngaol.tuner.data.NoteMath
import id.andreasmlbngaol.tuner.data.TuningPreset

@Composable
fun StringSelector(
    tuning: TuningPreset,
    selectedString: Int?,
    detectedString: Int?,
    playingString: Int?,
    onStringTap: (Int) -> Unit,
    onAutoDetectChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val auto = selectedString == null
    val autoDescription = stringResource(R.string.strings_auto_description)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(
                text = stringResource(R.string.strings_label),
                style = MaterialTheme.typography.labelLarge,
                color = colors.tertiary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.strings_auto),
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = auto,
                onCheckedChange = onAutoDetectChange,
                modifier = Modifier.semantics { contentDescription = autoDescription },
            )
        }

        Surface(
            shape = CircleShape,
            color = colors.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(6.dp),
            ) {
                tuning.midis.forEachIndexed { index, midi ->
                    val isSelected = selectedString == index
                    val isDetected = auto && detectedString == index
                    val isPlaying = playingString == index
                    val note = NoteMath.noteLabel(midi)
                    val description = if (isPlaying) {
                        stringResource(
                            R.string.string_playing,
                            tuning.stringNumber(index),
                            note,
                        )
                    } else {
                        stringResource(
                            R.string.string_item_description,
                            tuning.stringNumber(index),
                            note,
                        )
                    }
                    val container by animateColorAsState(
                        targetValue = when {
                            isPlaying -> colors.primaryContainer
                            isSelected -> colors.tertiary
                            isDetected -> colors.surfaceContainerHighest
                            else -> Color.Transparent
                        },
                        animationSpec = motion.defaultEffectsSpec(),
                        label = "string-container",
                    )
                    val content by animateColorAsState(
                        targetValue = when {
                            isPlaying -> colors.onPrimaryContainer
                            isSelected -> colors.onTertiary
                            isDetected -> colors.tertiary
                            else -> colors.onSurfaceVariant
                        },
                        animationSpec = motion.defaultEffectsSpec(),
                        label = "string-content",
                    )
                    Surface(
                        onClick = { onStringTap(index) },
                        shape = CircleShape,
                        color = container,
                        contentColor = content,
                        border = when {
                            isPlaying -> BorderStroke(2.dp, colors.primary)
                            isDetected -> BorderStroke(2.dp, colors.tertiary)
                            else -> null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(62.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = description
                                selected = isSelected
                            },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = tuning.stringNumber(index).toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = content.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                )
                                if (isPlaying) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                    )
                                }
                            }
                            Text(
                                text = note,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
