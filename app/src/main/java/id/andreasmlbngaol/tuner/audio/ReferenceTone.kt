package id.andreasmlbngaol.tuner.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

/**
 * Renders a short plucked-string reference tone. Pure Kotlin so it can be
 * exercised from unit tests without an audio device.
 */
object ToneSynth {

    const val SAMPLE_RATE = 44_100
    const val DEFAULT_DURATION_MS = 2_200

    private val HARMONICS = doubleArrayOf(1.0, 0.42, 0.2, 0.09, 0.04)
    private const val ATTACK_SECONDS = 0.008
    private const val RELEASE_SECONDS = 0.06
    private const val PEAK = 0.24

    fun render(frequency: Double, durationMs: Int = DEFAULT_DURATION_MS): ShortArray {
        require(frequency > 0.0) { "frequency must be positive" }
        require(durationMs > 0) { "duration must be positive" }

        val frameCount = SAMPLE_RATE * durationMs / 1000
        val durationSeconds = durationMs / 1000.0
        val nyquist = SAMPLE_RATE / 2.0
        val samples = ShortArray(frameCount)

        for (frame in 0 until frameCount) {
            val time = frame.toDouble() / SAMPLE_RATE
            var value = 0.0
            HARMONICS.forEachIndexed { index, amplitude ->
                val partial = frequency * (index + 1)
                if (partial >= nyquist) return@forEachIndexed
                // Higher partials fade faster, like a real plucked string.
                val decay = exp(-time * (2.2 + index * 1.1))
                value += amplitude * decay * sin(2.0 * PI * partial * time)
            }
            val attack = (time / ATTACK_SECONDS).coerceAtMost(1.0)
            val release = ((durationSeconds - time) / RELEASE_SECONDS).coerceIn(0.0, 1.0)
            samples[frame] = (value * attack * release * PEAK * Short.MAX_VALUE)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return samples
    }
}

/**
 * Plays one reference tone at a time through the media stream. Playback stops
 * automatically after the rendered buffer, and is cancelled when the screen
 * leaves the foreground.
 */
class ReferenceTonePlayer(context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playback: Job? = null
    private var track: AudioTrack? = null

    fun play(frequency: Double, onFinished: () -> Unit) {
        stop()
        playback = scope.launch {
            var output: AudioTrack? = null
            try {
                val samples = ToneSynth.render(frequency)
                val minBuffer = AudioTrack.getMinBufferSize(
                    ToneSynth.SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                )
                val bufferBytes = max(minBuffer, samples.size * 2)
                output = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(ToneSynth.SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(bufferBytes)
                    .build()

                track = output
                output.write(samples, 0, samples.size)
                output.setVolume(0.9f)
                output.play()
                delay(ToneSynth.DEFAULT_DURATION_MS + 100L)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // The audio device may be busy; keep the tuner usable.
                if (track == null) onFinished()
            } finally {
                output?.let {
                    runCatching { it.stop() }
                    it.release()
                }
                if (output != null && track === output) {
                    track = null
                    onFinished()
                }
            }
        }
    }

    fun stop() {
        playback?.cancel()
        playback = null
        track = null
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
