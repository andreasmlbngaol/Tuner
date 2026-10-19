package id.andreasmlbngaol.tuner.data

import androidx.annotation.Keep

// Names are persisted in SharedPreferences, so they must survive R8.
@Keep
enum class TunerMode { STRINGS, CHROMATIC }

@Keep
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class TunerReading(
    val frequency: Double,
    val clarity: Float,
    val midi: Int,
    val cents: Double,
    val targetFrequency: Double,
    val stringIndex: Int?,
) {
    val noteName: String get() = NoteMath.noteName(midi)
    val octave: Int get() = NoteMath.octave(midi)
    val noteLabel: String get() = NoteMath.noteLabel(midi)
    val inTune: Boolean get() = NoteMath.isInTune(cents)
}
