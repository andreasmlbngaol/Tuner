package id.andreasmlbngaol.tuner.data

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Maps a detected frequency to a string in the current tuning.
 *
 * YIN can report a harmonic as the fundamental. A target whose octave-folded
 * frequency lands within 50 cents is treated as a strong match; among strong
 * matches the one requiring the fewest octave shifts wins. This keeps a 392 Hz
 * harmonic of G3 on the G string while a real 329.6 Hz E4 stays on the high E.
 * When no target is a strong match, scores and a hysteresis margin keep the
 * selection from flickering between neighbouring strings.
 */
object StringMatcher {

    fun nearestIndex(
        frequency: Double,
        tuning: TuningPreset,
        referencePitch: Double,
        previous: Int? = null,
        hysteresisCents: Double = 60.0,
    ): Int {
        val targets = tuning.midis.map { midi ->
            NoteMath.midiToFrequency(midi.toDouble(), referencePitch)
        }
        val folded = targets.map { NoteMath.foldToNearestOctave(frequency, it) }
        val foldedCents = targets.indices.map { index ->
            abs(NoteMath.centsBetween(folded[index], targets[index]))
        }
        val shifts = targets.indices.map { index ->
            abs(ln(folded[index] / frequency) / ln(2.0)).roundToInt()
        }

        val strong = targets.indices.filter { foldedCents[it] < STRONG_MATCH_CENTS }
        if (strong.isNotEmpty()) {
            if (previous != null && previous in strong) return previous
            return strong.minWith(compareBy<Int> { shifts[it] }.thenBy { it })
        }

        val scores = targets.indices.map { index ->
            foldedCents[index] + shifts[index] * OCTAVE_PENALTY_CENTS
        }
        val best = scores.indices.minWith(
            compareBy<Int> { scores[it] }
                .thenBy { foldedCents[it] }
                .thenBy { it },
        )

        if (
            previous != null &&
            previous in scores.indices &&
            previous != best &&
            abs(NoteMath.centsBetween(frequency, targets[previous])) < HYSTERESIS_WINDOW_CENTS &&
            scores[previous] - scores[best] < hysteresisCents
        ) {
            return previous
        }
        return best
    }

    private const val STRONG_MATCH_CENTS = 50.0
    private const val OCTAVE_PENALTY_CENTS = 300.0
    private const val HYSTERESIS_WINDOW_CENTS = 300.0
}
