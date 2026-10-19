package id.andreasmlbngaol.tuner.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.andreasmlbngaol.tuner.R
import id.andreasmlbngaol.tuner.data.MAX_REFERENCE_PITCH
import id.andreasmlbngaol.tuner.data.MIN_REFERENCE_PITCH
import id.andreasmlbngaol.tuner.data.ThemeMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsSheet(
    referencePitch: Int,
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    haptics: Boolean,
    supportsDynamicColor: Boolean,
    onReferencePitchChange: (Int) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.displaySmallEmphasized,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 18.dp),
            )

            ReferencePitchCard(
                referencePitch = referencePitch,
                onReferencePitchChange = onReferencePitchChange,
            )

            SectionTitle(stringResource(R.string.section_appearance))

            Surface(
                shape = CircleShape,
                color = colors.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxHeight(),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        ChoicePill(
                            label = stringResource(mode.labelRes),
                            selected = themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            SwitchRow(
                icon = Icons.Rounded.Palette,
                title = stringResource(R.string.dynamic_color_title),
                subtitle = if (supportsDynamicColor) {
                    stringResource(R.string.dynamic_color_body)
                } else {
                    stringResource(R.string.dynamic_color_unsupported)
                },
                checked = dynamicColor && supportsDynamicColor,
                enabled = supportsDynamicColor,
                onCheckedChange = onDynamicColorChange,
            )

            HorizontalDivider(
                color = colors.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 10.dp),
            )

            SectionTitle(stringResource(R.string.section_feedback), topPadding = 0.dp)

            SwitchRow(
                icon = Icons.Rounded.Vibration,
                title = stringResource(R.string.haptics_title),
                subtitle = stringResource(R.string.haptics_body),
                checked = haptics,
                enabled = true,
                onCheckedChange = onHapticsChange,
            )

            Spacer(Modifier.height(18.dp))

            Surface(
                shape = MaterialTheme.shapes.large,
                color = colors.surfaceContainerHigh,
                contentColor = colors.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = colors.tertiary,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.privacy_note),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferencePitchCard(
    referencePitch: Int,
    onReferencePitchChange: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reference_pitch_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.reference_pitch_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = referencePitch.toString(),
                        style = MaterialTheme.typography.displaySmallEmphasized,
                        color = colors.tertiary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = stringResource(R.string.hertz_unit),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = 3.dp, bottom = 8.dp),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp),
            ) {
                FilledTonalIconButton(
                    onClick = { onReferencePitchChange(referencePitch - 1) },
                    enabled = referencePitch > MIN_REFERENCE_PITCH,
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = stringResource(R.string.decrease),
                        modifier = Modifier.size(20.dp),
                    )
                }
                Slider(
                    value = referencePitch.toFloat(),
                    onValueChange = { onReferencePitchChange(it.roundToInt()) },
                    valueRange = MIN_REFERENCE_PITCH.toFloat()..MAX_REFERENCE_PITCH.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.tertiary,
                        activeTrackColor = colors.tertiary,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                )
                FilledTonalIconButton(
                    onClick = { onReferencePitchChange(referencePitch + 1) },
                    enabled = referencePitch < MAX_REFERENCE_PITCH,
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.increase),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.hertz_value, MIN_REFERENCE_PITCH),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.hertz_value, MAX_REFERENCE_PITCH),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun ChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val container by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = motion.defaultEffectsSpec(),
        label = "choice-container",
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        animationSpec = motion.defaultEffectsSpec(),
        label = "choice-content",
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = container,
        contentColor = content,
        modifier = modifier.fillMaxHeight(),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, topPadding: androidx.compose.ui.unit.Dp = 20.dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.tertiary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = topPadding, bottom = 10.dp),
    )
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = colors.surfaceContainerHighest,
            contentColor = colors.tertiary,
            modifier = Modifier.size(42.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.5f),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }
