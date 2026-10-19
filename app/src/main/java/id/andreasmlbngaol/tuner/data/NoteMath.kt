package id.andreasmlbngaol.tuner.data

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

private val NOTE_NAMES = arrayOf(
    "C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B",
)

const val DEFAULT_REFERENCE_PITCH = 440
const val MIN_REFERENCE_PITCH = 415
const val MAX_REFERENCE_PITCH = 466

/** Pure math helpers for equal-temperament pitch conversion. */
object NoteMath {

    fun midiToFrequency(midi: Double, referencePitch: Double = DEFAULT_REFERENCE_PITCH.toDouble()): Double =
        referencePitch * 2.0.pow((midi - 69.0) / 12.0)

    fun frequencyToMidi(frequency: Double, referencePitch: Double = DEFAULT_REFERENCE_PITCH.toDouble()): Double =
        69.0 + 12.0 * (ln(frequency / referencePitch) / ln(2.0))

    fun centsBetween(frequency: Double, targetFrequency: Double): Double =
        1200.0 * (ln(frequency / targetFrequency) / ln(2.0))

    fun noteName(midi: Int): String = NOTE_NAMES[Math.floorMod(midi, 12)]

    fun octave(midi: Int): Int = Math.floorDiv(midi, 12) - 1

    fun noteLabel(midi: Int): String = noteName(midi) + octave(midi)

    fun nearestNote(
        frequency: Double,
        referencePitch: Double = DEFAULT_REFERENCE_PITCH.toDouble(),
    ): NoteInfo {
        val midi = frequencyToMidi(frequency, referencePitch).roundToInt()
        val target = midiToFrequency(midi.toDouble(), referencePitch)
        return NoteInfo(
            midi = midi,
            name = noteName(midi),
            octave = octave(midi),
            frequency = target,
            cents = centsBetween(frequency, target),
        )
    }

    fun isInTune(cents: Double, tolerance: Double = 5.0): Boolean = abs(cents) <= tolerance

    /**
     * Moves [frequency] by whole octaves until it is within ±600 cents of
     * [target]. A plucked string can make YIN report a harmonic as the
     * fundamental; folding keeps the reading on the intended string.
     */
    fun foldToNearestOctave(frequency: Double, target: Double): Double {
        if (frequency <= 0.0 || target <= 0.0) return frequency
        var folded = frequency
        while (folded > target * OCTAVE_FOLD_RATIO) folded /= 2.0
        while (folded < target / OCTAVE_FOLD_RATIO) folded *= 2.0
        return folded
    }

    private const val OCTAVE_FOLD_RATIO = 1.4142135623730951 // sqrt(2)
}

data class NoteInfo(
    val midi: Int,
    val name: String,
    val octave: Int,
    val frequency: Double,
    val cents: Double,
)
