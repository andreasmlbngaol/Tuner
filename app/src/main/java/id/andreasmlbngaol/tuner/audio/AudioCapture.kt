package id.andreasmlbngaol.tuner.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.math.max

/**
 * Streams mono PCM-16 blocks from the microphone.
 *
 * UNPROCESSED is requested first: noise suppression and automatic gain control
 * smear the decay of a plucked string and hurt pitch stability. Devices that do
 * not expose it fall back to the plain microphone source.
 */
class AudioCapture(private val sampleRate: Int = 44_100) {

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun frames(): Flow<ShortArray> = flow {
        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        check(minBuffer > 0) { "Microphone is not available" }

        val recorder = createRecorder(max(minBuffer, sampleRate / 5 * 2))
        try {
            check(recorder.state == AudioRecord.STATE_INITIALIZED) { "Microphone failed to initialize" }
            recorder.startRecording()
            val block = ShortArray(BLOCK_SIZE)
            while (currentCoroutineContext().isActive) {
                val read = recorder.read(block, 0, block.size, AudioRecord.READ_BLOCKING)
                if (read > 0) {
                    emit(block.copyOf(read))
                } else if (read < 0) {
                    break
                }
            }
        } finally {
            runCatching { recorder.stop() }
            recorder.release()
        }
    }.flowOn(Dispatchers.IO)

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun createRecorder(bufferSize: Int): AudioRecord {
        val sources = intArrayOf(
            MediaRecorder.AudioSource.UNPROCESSED,
            MediaRecorder.AudioSource.MIC,
        )
        sources.forEach { source ->
            val recorder = runCatching {
                AudioRecord(
                    source,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                )
            }.getOrNull()
            if (recorder != null && recorder.state == AudioRecord.STATE_INITIALIZED) {
                return recorder
            }
            recorder?.release()
        }
        error("Could not open the microphone")
    }

    private companion object {
        const val BLOCK_SIZE = 1024
    }
}
