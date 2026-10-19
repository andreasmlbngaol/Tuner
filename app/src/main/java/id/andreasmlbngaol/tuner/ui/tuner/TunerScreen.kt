package id.andreasmlbngaol.tuner.ui.tuner

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.andreasmlbngaol.tuner.R
import id.andreasmlbngaol.tuner.data.ThemeMode
import id.andreasmlbngaol.tuner.data.TunerMode
import id.andreasmlbngaol.tuner.data.TunerReading
import id.andreasmlbngaol.tuner.data.TuningPreset
import id.andreasmlbngaol.tuner.ui.components.SignalWaveform
import id.andreasmlbngaol.tuner.ui.components.StringSelector
import id.andreasmlbngaol.tuner.ui.components.TunerGauge
import id.andreasmlbngaol.tuner.ui.components.TuningSelector
import id.andreasmlbngaol.tuner.ui.settings.SettingsSheet
import id.andreasmlbngaol.tuner.ui.theme.rememberTuningFeedbackColors

private enum class Feedback { LISTENING, IN_TUNE, TUNE_UP, TUNE_DOWN }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TunerScreen(
    state: TunerUiState,
    onModeChange: (TunerMode) -> Unit,
    onTuningChange: (TuningPreset) -> Unit,
    onAutoDetectChange: (Boolean) -> Unit,
    onStringTap: (Int) -> Unit,
    onReferencePitchChange: (Int) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onRetryAudio: () -> Unit,
    onDismissAudioError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val reading = state.reading
    val inTune = reading?.inTune == true
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(inTune, state.haptics) {
        if (inTune && state.haptics) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.tertiaryContainer.copy(alpha = 0.30f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width / 2f, 0f),
                        radius = size.width * 1.05f,
                    ),
                )
            },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                Header(
                    referencePitch = state.referencePitch,
                    onOpenSettings = { showSettings = true },
                )

                Spacer(Modifier.height(18.dp))

                ModeToggle(mode = state.mode, onModeChange = onModeChange)

                AnimatedVisibility(visible = state.mode == TunerMode.STRINGS) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        TuningSelector(tuning = state.tuning, onSelect = onTuningChange)
                    }
                }

                if (state.audioError) {
                    AudioErrorCard(onRetry = onRetryAudio, onDismiss = onDismissAudioError)
                }

                Spacer(Modifier.height(14.dp))

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    GaugeCard(
                        reading = reading,
                        isListening = state.isListening,
                        signalLevel = state.signalLevel,
                        modifier = Modifier
                            .widthIn(max = 440.dp)
                            .fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(visible = state.mode == TunerMode.STRINGS) {
                    StringSelector(
                        tuning = state.tuning,
                        selectedString = state.selectedString,
                        detectedString = reading?.stringIndex,
                        playingString = state.playingString,
                        onStringTap = onStringTap,
                        onAutoDetectChange = onAutoDetectChange,
                    )
                }

                if (state.mode == TunerMode.CHROMATIC) {
                    ChromaticStats(reading = reading, referencePitch = state.referencePitch)
                }

                Spacer(Modifier.height(22.dp))
                Footer(referencePitch = state.referencePitch)
                Spacer(Modifier.height(22.dp))
                }
            }
        }
    }
    }

    if (showSettings) {
        SettingsSheet(
            referencePitch = state.referencePitch,
            themeMode = state.themeMode,
            dynamicColor = state.dynamicColor,
            haptics = state.haptics,
            supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            onReferencePitchChange = onReferencePitchChange,
            onThemeModeChange = onThemeModeChange,
            onDynamicColorChange = onDynamicColorChange,
            onHapticsChange = onHapticsChange,
            onDismiss = { showSettings = false },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Header(referencePitch: Int, onOpenSettings: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.header_eyebrow),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.tertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayMediumEmphasized,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 52.sp,
                )
            }
            Surface(
                onClick = onOpenSettings,
                shape = CircleShape,
                color = colors.surfaceContainerHigh,
                contentColor = colors.onSurface,
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = colors.tertiary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = stringResource(R.string.reference_pill, referencePitch),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ModeToggle(mode: TunerMode, onModeChange: (TunerMode) -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(5.dp)
                .fillMaxHeight(),
        ) {
            ModePill(
                selected = mode == TunerMode.STRINGS,
                icon = Icons.Rounded.MusicNote,
                label = stringResource(R.string.mode_strings),
                onClick = { onModeChange(TunerMode.STRINGS) },
                modifier = Modifier.weight(1f),
            )
            ModePill(
                selected = mode == TunerMode.CHROMATIC,
                icon = Icons.Rounded.GraphicEq,
                label = stringResource(R.string.mode_chromatic),
                onClick = { onModeChange(TunerMode.CHROMATIC) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ModePill(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val container by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = motion.defaultEffectsSpec(),
        label = "mode-container",
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        animationSpec = motion.defaultEffectsSpec(),
        label = "mode-content",
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = container,
        contentColor = content,
        modifier = modifier.fillMaxHeight(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GaugeCard(
    reading: TunerReading?,
    isListening: Boolean,
    signalLevel: Float,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(44.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colors.tertiaryContainer.copy(alpha = 0.34f),
                        colors.surfaceContainerLow,
                    ),
                ),
            ),
    ) {
        TunerGauge(
            reading = reading,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
        )
        FeedbackBlock(
            reading = reading,
            isListening = isListening,
            signalLevel = signalLevel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
        )
    }
}

private data class FeedbackUi(
    val accent: Color,
    val container: Color,
    val onContainer: Color,
    val icon: ImageVector,
    val title: String,
    val detail: String? = null,
)

@Composable
private fun feedbackUi(
    target: Feedback,
    isListening: Boolean,
): FeedbackUi {
    val colors = MaterialTheme.colorScheme
    val feedbackColors = rememberTuningFeedbackColors()

    return when (target) {
        Feedback.TUNE_UP -> FeedbackUi(
            accent = feedbackColors.upAccent,
            container = feedbackColors.upContainer,
            onContainer = feedbackColors.onUpContainer,
            icon = Icons.Rounded.ArrowUpward,
            title = stringResource(R.string.tune_up),
        )
        Feedback.TUNE_DOWN -> FeedbackUi(
            accent = feedbackColors.downAccent,
            container = feedbackColors.downContainer,
            onContainer = feedbackColors.onDownContainer,
            icon = Icons.Rounded.ArrowDownward,
            title = stringResource(R.string.tune_down),
        )
        Feedback.IN_TUNE -> FeedbackUi(
            accent = feedbackColors.inTuneAccent,
            container = feedbackColors.inTuneContainer,
            onContainer = feedbackColors.onInTuneContainer,
            icon = Icons.Rounded.Check,
            title = stringResource(R.string.in_tune),
        )
        Feedback.LISTENING -> FeedbackUi(
            accent = colors.onSurfaceVariant,
            container = colors.surfaceContainerHighest,
            onContainer = colors.onSurface,
            icon = Icons.Rounded.GraphicEq,
            title = stringResource(R.string.listening_short),
            detail = stringResource(
                if (isListening) R.string.listening_hint else R.string.microphone_paused,
            ),
        )
    }
}

@Composable
private fun FeedbackBlock(
    reading: TunerReading?,
    isListening: Boolean,
    signalLevel: Float,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val feedback = when {
        reading == null -> Feedback.LISTENING
        reading.inTune -> Feedback.IN_TUNE
        reading.cents < 0 -> Feedback.TUNE_UP
        else -> Feedback.TUNE_DOWN
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        AnimatedContent(
            targetState = feedback,
            transitionSpec = {
                fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
            },
            label = "feedback",
        ) { target ->
            val ui = feedbackUi(target, isListening)
            val onAccent = if (ui.accent.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ui.container)
                    .padding(start = 6.dp, top = 6.dp, bottom = 6.dp, end = 18.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = ui.accent,
                    contentColor = onAccent,
                    modifier = Modifier.size(54.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = ui.icon,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
                Column {
                    Text(
                        text = ui.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = ui.onContainer,
                    )
                    ui.detail?.let { detail ->
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.labelMedium,
                            color = ui.onContainer.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }

        if (reading == null) {
            SignalWaveform(
                level = signalLevel,
                active = isListening,
                color = colors.primary,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .width(84.dp)
                    .height(22.dp),
            )
        } else {
            Text(
                text = stringResource(R.string.target_frequency, reading.targetFrequency),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ChromaticStats(reading: TunerReading?, referencePitch: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Stat(
            label = stringResource(R.string.stat_frequency),
            value = reading?.let { stringResource(R.string.frequency_format, it.frequency) } ?: "—",
            modifier = Modifier.weight(1f),
        )
        Stat(
            label = stringResource(R.string.stat_target),
            value = reading?.let { stringResource(R.string.frequency_format, it.targetFrequency) } ?: "—",
            modifier = Modifier.weight(1f),
        )
        Stat(
            label = stringResource(R.string.stat_reference),
            value = stringResource(R.string.hertz_value, referencePitch),
            modifier = Modifier.weight(0.75f),
        )
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun AudioErrorCard(onRetry: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.audio_error_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.audio_error_body),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dismiss))
            }
        }
    }
}

@Composable
private fun Footer(referencePitch: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = stringResource(R.string.footer_note, referencePitch),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
