package id.andreasmlbngaol.tuner.audio

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class PitchResult(
    val frequency: Double,
    val clarity: Float,
    val rms: Float,
)

/**
 * YIN pitch detector (de Cheveigné & Kawahara, 2002).
 *
 * YIN is used instead of a plain FFT peak because a plucked string is rich in
 * harmonics: the strongest partial is often not the fundamental. YIN tracks
 * the period directly, which keeps the low E string from being reported an
 * octave too high.
 */
class YinPitchDetector(
    private val sampleRate: Int,
    private val minFrequency: Double = 60.0,
    private val maxFrequency: Double = 1200.0,
) {

    fun detect(samples: FloatArray, offset: Int = 0, length: Int = samples.size): PitchResult? {
        if (length < 256) return null

        var energy = 0.0
        for (i in offset until offset + length) {
            energy += samples[i] * samples[i]
        }
        val rms = sqrt(energy / length)
        if (rms < 1e-4) return null

        val tauMin = max(2, (sampleRate / maxFrequency).toInt())
        val tauMax = min(length / 2 - 1, (sampleRate / minFrequency).toInt())
        if (tauMax <= tauMin) return null

        val difference = DoubleArray(tauMax + 1)
        for (tau in 1..tauMax) {
            var sum = 0.0
            val limit = length - tau
            var i = 0
            while (i < limit) {
                val delta = samples[offset + i] - samples[offset + i + tau]
                sum += delta * delta
                i++
            }
            difference[tau] = sum
        }

        // Cumulative mean normalized difference.
        val normalized = DoubleArray(tauMax + 1)
        normalized[0] = 1.0
        var runningSum = 0.0
        for (tau in 1..tauMax) {
            runningSum += difference[tau]
            normalized[tau] = if (runningSum == 0.0) 1.0 else difference[tau] * tau / runningSum
        }

        // First dip below the threshold; fall back to the global minimum.
        var tau = tauMin
        while (tau <= tauMax && normalized[tau] >= THRESHOLD) tau++
        if (tau > tauMax) {
            tau = tauMin
            for (candidate in tauMin + 1..tauMax) {
                if (normalized[candidate] < normalized[tau]) tau = candidate
            }
        } else {
            // The threshold is crossed on the falling slope; walk to the dip.
            while (tau + 1 <= tauMax && normalized[tau + 1] < normalized[tau]) tau++
        }
        if (tau <= 1 || tau >= tauMax) return null

        val clarity = (1.0 - normalized[tau]).toFloat()
        if (clarity < MIN_CLARITY) return null

        // Parabolic interpolation around the minimum for sub-sample precision.
        val previous = normalized[tau - 1]
        val current = normalized[tau]
        val next = normalized[tau + 1]
        val denominator = previous - 2.0 * current + next
        val shift = if (denominator != 0.0) 0.5 * (previous - next) / denominator else 0.0
        val period = tau + shift
        if (period <= 0.0) return null

        val frequency = sampleRate / period
        if (frequency < minFrequency || frequency > maxFrequency) return null

        return PitchResult(frequency = frequency, clarity = clarity, rms = rms.toFloat())
    }

    private companion object {
        const val THRESHOLD = 0.14
        const val MIN_CLARITY = 0.62f
    }
}
