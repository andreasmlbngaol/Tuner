package id.andreasmlbngaol.tuner

import id.andreasmlbngaol.tuner.audio.ToneSynth
import id.andreasmlbngaol.tuner.audio.YinPitchDetector
import id.andreasmlbngaol.tuner.data.NoteMath
import id.andreasmlbngaol.tuner.data.StringMatcher
import id.andreasmlbngaol.tuner.data.TuningPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class TunerMathTest {

    @Test
    fun concertA_is440() {
        assertEquals(440.0, NoteMath.midiToFrequency(69.0), 0.0001)
    }

    @Test
    fun nearestNote_forA440_isA4WithNoDeviation() {
        val note = NoteMath.nearestNote(440.0)
        assertEquals("A", note.name)
        assertEquals(4, note.octave)
        assertEquals(0.0, note.cents, 0.01)
    }

    @Test
    fun nearestNote_forLowE_isE2() {
        val note = NoteMath.nearestNote(82.4069)
        assertEquals("E", note.name)
        assertEquals(2, note.octave)
        assertEquals(40, note.midi)
        assertEquals(0.0, note.cents, 0.5)
    }

    @Test
    fun cents_isSymmetricAroundTarget() {
        assertEquals(100.0, NoteMath.centsBetween(NoteMath.midiToFrequency(70.0), NoteMath.midiToFrequency(69.0)), 0.001)
        assertEquals(-100.0, NoteMath.centsBetween(NoteMath.midiToFrequency(68.0), NoteMath.midiToFrequency(69.0)), 0.001)
    }

    @Test
    fun referencePitch_changesTargets() {
        val baroqueA = NoteMath.nearestNote(415.0, referencePitch = 415.0)
        assertEquals("A", baroqueA.name)
        assertEquals(0.0, baroqueA.cents, 0.01)

        val shifted = NoteMath.midiToFrequency(69.0, referencePitch = 442.0)
        assertEquals(442.0, shifted, 0.0001)
    }

    @Test
    fun standardTuning_matchesGuitarStrings() {
        assertEquals(
            listOf("E2", "A2", "D3", "G3", "B3", "E4"),
            TuningPreset.STANDARD.notes,
        )
    }

    @Test
    fun dropD_lowersOnlyTheSixthString() {
        assertEquals(
            listOf("D2", "A2", "D3", "G3", "B3", "E4"),
            TuningPreset.DROP_D.notes,
        )
    }

    @Test
    fun yin_detectsLowE() {
        val detector = YinPitchDetector(sampleRate = 22_050)
        val samples = sineWave(frequency = 82.4069, sampleRate = 22_050, size = 2048)
        val result = detector.detect(samples)
        assertNotNull(result)
        assertEquals(82.4069, result!!.frequency, 0.5)
        assertTrue(result.clarity > 0.8f)
    }

    @Test
    fun yin_detectsHighEWithHarmonics() {
        val detector = YinPitchDetector(sampleRate = 22_050)
        val sampleRate = 22_050
        val samples = FloatArray(2048) { index ->
            val time = index.toDouble() / sampleRate
            val fundamental = sin(2 * PI * 329.6276 * time)
            val second = 0.6 * sin(2 * PI * 329.6276 * 2 * time)
            val third = 0.3 * sin(2 * PI * 329.6276 * 3 * time)
            (fundamental + second + third).toFloat() / 1.9f
        }
        val result = detector.detect(samples)
        assertNotNull(result)
        assertEquals(329.6276, result!!.frequency, 1.0)
    }

    @Test
    fun yin_ignoresSilence() {
        val detector = YinPitchDetector(sampleRate = 22_050)
        assertNull(detector.detect(FloatArray(2048)))
    }

    @Test
    fun octaveFolding_keepsGHarmonicOnTheGString() {
        assertEquals(196.0, NoteMath.foldToNearestOctave(392.0, 196.0), 0.001)
        assertEquals(196.0, NoteMath.foldToNearestOctave(98.0, 196.0), 0.001)
        assertEquals(329.6276, NoteMath.foldToNearestOctave(329.6276, 329.6276), 0.001)
    }

    @Test
    fun stringMatcher_mapsOctaveErrorToGString() {
        // G3 is 196 Hz. A detector that reports its second harmonic must still
        // resolve to the G string, not the high E string.
        assertEquals(3, StringMatcher.nearestIndex(392.0, TuningPreset.STANDARD, 440.0))
        assertEquals(3, StringMatcher.nearestIndex(392.0, TuningPreset.STANDARD, 440.0, previous = 5))
        assertEquals(3, StringMatcher.nearestIndex(196.0, TuningPreset.STANDARD, 440.0))
        assertEquals(5, StringMatcher.nearestIndex(329.6276, TuningPreset.STANDARD, 440.0))
    }

    @Test
    fun stringMatcher_holdsPreviousStringNearTheMidpoint() {
        // 171.4 Hz is about halfway between D3 and G3 in cents.
        assertEquals(
            2,
            StringMatcher.nearestIndex(171.4, TuningPreset.STANDARD, 440.0, previous = 2),
        )
    }

    @Test
    fun stringMatcher_handlesGStringWithStrongSecondHarmonic() {
        val detector = YinPitchDetector(sampleRate = 22_050)
        val samples = FloatArray(2048) { index ->
            val time = index / 22_050.0
            (
                0.2 * sin(2 * PI * 196.0 * time) +
                    sin(2 * PI * 392.0 * time) +
                    0.3 * sin(2 * PI * 588.0 * time)
                ).toFloat() / 1.5f
        }
        val result = detector.detect(samples)
        assertNotNull(result)
        assertEquals(
            3,
            StringMatcher.nearestIndex(result!!.frequency, TuningPreset.STANDARD, 440.0),
        )
    }

    @Test
    fun stringMatcher_keepsRealHighEOnTheHighEString() {
        val detector = YinPitchDetector(sampleRate = 22_050)
        val samples = FloatArray(2048) { index ->
            val time = index / 22_050.0
            (
                0.2 * sin(2 * PI * 329.6276 * time) +
                    sin(2 * PI * 659.2551 * time) +
                    0.3 * sin(2 * PI * 988.8827 * time)
                ).toFloat() / 1.5f
        }
        val result = detector.detect(samples)
        assertNotNull(result)
        assertEquals(
            5,
            StringMatcher.nearestIndex(result!!.frequency, TuningPreset.STANDARD, 440.0),
        )
    }

    @Test
    fun referenceTone_hasExpectedLengthAndHeadroom() {
        val samples = ToneSynth.render(82.4069, durationMs = 500)
        assertEquals(22_050, samples.size)
        val peak = samples.maxOf { abs(it.toInt()) }
        assertTrue(peak in 1 until Short.MAX_VALUE.toInt())
    }

    @Test
    fun referenceTone_fadesOutBeforeEnding() {
        val samples = ToneSynth.render(440.0, durationMs = 500)
        val tailPeak = samples.takeLast(100).maxOf { abs(it.toInt()) }
        assertTrue(tailPeak < 2_000)
    }

    private fun sineWave(frequency: Double, sampleRate: Int, size: Int): FloatArray =
        FloatArray(size) { index ->
            sin(2 * PI * frequency * index / sampleRate).toFloat()
        }
}
