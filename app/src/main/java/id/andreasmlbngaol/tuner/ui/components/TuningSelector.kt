package id.andreasmlbngaol.tuner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.andreasmlbngaol.tuner.R
import id.andreasmlbngaol.tuner.data.TuningPreset

/**
 * A single-select menu rather than a scrolling chip row: with seven presets a
 * horizontal strip hides options and forces recall, while a menu keeps every
 * choice visible in one tap.
 */
@Composable
fun TuningSelector(
    tuning: TuningPreset,
    onSelect: (TuningPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var triggerWidth by remember { mutableIntStateOf(0) }
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val noteColor = if (colors.surface.luminance() < 0.5f) {
        Color(0xFF9BD3A8)
    } else {
        Color(0xFF3F6544)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            onClick = { expanded = true },
            shape = CircleShape,
            color = colors.surfaceContainerHigh,
            contentColor = colors.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .onSizeChanged { triggerWidth = it.width },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 18.dp, end = 14.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = colors.tertiary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tuning_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(tuning.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = stringResource(R.string.change_tuning),
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(density) { triggerWidth.toDp() }),
            shape = MaterialTheme.shapes.large,
            containerColor = colors.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
        ) {
            TuningPreset.entries.forEachIndexed { index, preset ->
                val isSelected = preset == tuning
                SelectableDropdownMenuItem(
                    selected = isSelected,
                    onClick = {
                        expanded = false
                        onSelect(preset)
                    },
                    text = {
                        Text(
                            text = stringResource(preset.titleRes),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        )
                    },
                    supportingText = {
                        Text(
                            text = preset.noteNames.joinToString(" "),
                            style = MaterialTheme.typography.labelMedium,
                            color = noteColor,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                        )
                    },
                    trailingContent = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = noteColor,
                            )
                        }
                    },
                    colors = MenuDefaults.selectableItemColors(
                        selectedContainerColor = colors.primaryContainer,
                        selectedTextColor = colors.onPrimaryContainer,
                        selectedTrailingContentColor = colors.onPrimaryContainer,
                    ),
                    shapes = MenuDefaults.itemShapes(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                )
                if (index < TuningPreset.entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = colors.outline.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}
