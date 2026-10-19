package id.andreasmlbngaol.tuner.data

import androidx.annotation.Keep
import androidx.annotation.StringRes
import id.andreasmlbngaol.tuner.R

/**
 * Presets are ordered from the lowest (6th) string to the highest (1st) string.
 */
@Keep
enum class TuningPreset(
    @param:StringRes val titleRes: Int,
    val midis: List<Int>,
) {
    STANDARD(R.string.tuning_standard, listOf(40, 45, 50, 55, 59, 64)),
    DROP_D(R.string.tuning_drop_d, listOf(38, 45, 50, 55, 59, 64)),
    HALF_STEP_DOWN(R.string.tuning_half_step_down, listOf(39, 44, 49, 54, 58, 63)),
    FULL_STEP_DOWN(R.string.tuning_full_step_down, listOf(38, 43, 48, 53, 57, 62)),
    DADGAD(R.string.tuning_dadgad, listOf(38, 45, 50, 55, 57, 62)),
    OPEN_G(R.string.tuning_open_g, listOf(38, 43, 50, 55, 59, 62)),
    OPEN_D(R.string.tuning_open_d, listOf(38, 45, 50, 54, 57, 62));

    val notes: List<String> get() = midis.map(NoteMath::noteLabel)

    /** Note letters without octave numbers, used in the tuning menu. */
    val noteNames: List<String> get() = midis.map(NoteMath::noteName)

    /** Human string number for an index in [midis] (index 0 -> 6th string). */
    fun stringNumber(index: Int): Int = midis.size - index

    companion object {
        fun fromId(id: String?): TuningPreset = entries.firstOrNull { it.name == id } ?: STANDARD
    }
}
