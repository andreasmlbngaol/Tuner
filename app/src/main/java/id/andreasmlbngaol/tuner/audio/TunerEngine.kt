package id.andreasmlbngaol.tuner.audio

/**
 * Turns raw microphone blocks into pitch estimates.
 *
 * Audio is captured at 44.1 kHz, then decimated to 22.05 kHz. That is still
 * far above the highest note we care about and roughly halves the work done by
 * the O(N·tau) YIN difference function.
 */
class TunerEngine(inputSampleRate: Int = 44_100) {

    private val detector = YinPitchDetector(
        sampleRate = inputSampleRate / DECIMATION,
        minFrequency = 60.0,
        maxFrequency = 1_200.0,
    )

    private val buffer = FloatArray(WINDOW * 2)
    private var size = 0
    private var hopCounter = 0

    fun process(input: ShortArray): PitchResult? {
        var i = 0
        while (i + 1 < input.size) {
            append((input[i] + input[i + 1]) / (2f * 32_768f))
            i += DECIMATION
            hopCounter++
            if (hopCounter >= HOP && size >= WINDOW) {
                hopCounter = 0
                return detector.detect(buffer, size - WINDOW, WINDOW)
            }
        }
        return null
    }

    private fun append(value: Float) {
        if (size == buffer.size) {
            buffer.copyInto(buffer, destinationOffset = 0, startIndex = size - WINDOW, endIndex = size)
            size = WINDOW
        }
        buffer[size++] = value
    }

    private companion object {
        const val DECIMATION = 2
        const val WINDOW = 2048
        const val HOP = 1024
    }
}
