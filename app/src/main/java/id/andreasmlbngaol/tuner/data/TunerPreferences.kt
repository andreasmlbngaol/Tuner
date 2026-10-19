package id.andreasmlbngaol.tuner.data

import android.content.Context
import androidx.core.content.edit

/** Small, synchronous preference store: the tuner has no database needs. */
class TunerPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("tuner_preferences", Context.MODE_PRIVATE)

    var referencePitch: Int
        get() = prefs.getInt(KEY_REFERENCE_PITCH, DEFAULT_REFERENCE_PITCH)
            .coerceIn(MIN_REFERENCE_PITCH, MAX_REFERENCE_PITCH)
        set(value) = prefs.edit {
            putInt(KEY_REFERENCE_PITCH, value.coerceIn(MIN_REFERENCE_PITCH, MAX_REFERENCE_PITCH))
        }

    var mode: TunerMode
        get() = runCatching { TunerMode.valueOf(prefs.getString(KEY_MODE, null) ?: "") }
            .getOrDefault(TunerMode.STRINGS)
        set(value) = prefs.edit { putString(KEY_MODE, value.name) }

    var tuning: TuningPreset
        get() = TuningPreset.fromId(prefs.getString(KEY_TUNING, null))
        set(value) = prefs.edit { putString(KEY_TUNING, value.name) }

    var selectedString: Int?
        get() = prefs.getInt(KEY_SELECTED_STRING, AUTO).takeIf { it in 0..5 }
        set(value) = prefs.edit { putInt(KEY_SELECTED_STRING, value ?: AUTO) }

    var themeMode: ThemeMode
        get() = runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, null) ?: "") }
            .getOrDefault(ThemeMode.SYSTEM)
        set(value) = prefs.edit { putString(KEY_THEME, value.name) }

    var dynamicColor: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        set(value) = prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, value) }

    var haptics: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit { putBoolean(KEY_HAPTICS, value) }

    private companion object {
        const val AUTO = -1
        const val KEY_REFERENCE_PITCH = "reference_pitch"
        const val KEY_MODE = "mode"
        const val KEY_TUNING = "tuning"
        const val KEY_SELECTED_STRING = "selected_string"
        const val KEY_THEME = "theme"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
        const val KEY_HAPTICS = "haptics"
    }
}
