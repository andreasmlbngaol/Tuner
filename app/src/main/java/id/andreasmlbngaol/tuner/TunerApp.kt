package id.andreasmlbngaol.tuner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.andreasmlbngaol.tuner.ui.permission.PermissionScreen
import id.andreasmlbngaol.tuner.ui.theme.TunerTheme
import id.andreasmlbngaol.tuner.ui.tuner.TunerScreen
import id.andreasmlbngaol.tuner.ui.tuner.TunerViewModel

@Composable
fun TunerApp(viewModel: TunerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(context.hasMicrophonePermission()) }
    var hasRequested by rememberSaveable { mutableStateOf(false) }
    var listenAttempt by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasRequested = true
        hasPermission = granted
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasPermission = context.hasMicrophonePermission()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.stopTone()
    }

    LaunchedEffect(hasPermission, listenAttempt) {
        if (hasPermission) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.listen()
            }
        }
    }

    val activity = context.findActivity()
    val permanentlyDenied = !hasPermission &&
        hasRequested &&
        activity != null &&
        !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)

    TunerTheme(
        themeMode = state.themeMode,
        dynamicColor = state.dynamicColor,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (hasPermission) {
                TunerScreen(
                    state = state,
                    onModeChange = viewModel::setMode,
                    onTuningChange = viewModel::setTuning,
                    onAutoDetectChange = viewModel::setAutoDetect,
                    onStringTap = viewModel::onStringTap,
                    onReferencePitchChange = viewModel::setReferencePitch,
                    onThemeModeChange = viewModel::setThemeMode,
                    onDynamicColorChange = viewModel::setDynamicColor,
                    onHapticsChange = viewModel::setHaptics,
                    onRetryAudio = {
                        viewModel.clearAudioError()
                        listenAttempt++
                    },
                    onDismissAudioError = viewModel::clearAudioError,
                )
            } else {
                PermissionScreen(
                    permanentlyDenied = permanentlyDenied,
                    onRequest = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onOpenSettings = { context.openAppSettings() },
                )
            }
        }
    }
}

private fun Context.hasMicrophonePermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
