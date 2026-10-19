package id.andreasmlbngaol.tuner.ui.permission

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.andreasmlbngaol.tuner.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PermissionScreen(
    permanentlyDenied: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "mic-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.surface, colors.surfaceContainerLow),
                ),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.permission_eyebrow),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.tertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(220.dp),
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        repeat(2) { index ->
                            val progress = (pulse + index * 0.5f) % 1f
                            drawCircle(
                                color = colors.primary.copy(alpha = (1f - progress) * 0.22f),
                                radius = size.minDimension * (0.30f + 0.20f * progress),
                                center = center,
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = colors.primaryContainer,
                        contentColor = colors.onPrimaryContainer,
                        modifier = Modifier.size(124.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.permission_title),
                    style = MaterialTheme.typography.displaySmallEmphasized,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 44.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.permission_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )

                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = colors.surfaceContainerHigh,
                    contentColor = colors.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
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
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.permission_privacy),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                if (permanentlyDenied) {
                    Text(
                        text = stringResource(R.string.permission_denied_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                }
            }

            Button(
                onClick = if (permanentlyDenied) onOpenSettings else onRequest,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
            ) {
                Icon(
                    imageVector = if (permanentlyDenied) Icons.Rounded.Settings else Icons.Rounded.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(
                        if (permanentlyDenied) R.string.permission_open_settings else R.string.permission_grant,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
