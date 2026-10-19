package id.andreasmlbngaol.tuner.ui.tuner

import android.annotation.SuppressLint
import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import id.andreasmlbngaol.tuner.audio.AudioCapture
import id.andreasmlbngaol.tuner.audio.PitchResult
import id.andreasmlbngaol.tuner.audio.ReferenceTonePlayer
import id.andreasmlbngaol.tuner.audio.TunerEngine
import id.andreasmlbngaol.tuner.data.MAX_REFERENCE_PITCH
import id.andreasmlbngaol.tuner.data.MIN_REFERENCE_PITCH
import id.andreasmlbngaol.tuner.data.NoteMath
import id.andreasmlbngaol.tuner.data.ThemeMode
import id.andreasmlbngaol.tuner.data.TunerMode
import id.andreasmlbngaol.tuner.data.TunerPreferences
import id.andreasmlbngaol.tuner.data.TunerReading
import id.andreasmlbngaol.tuner.data.TuningPreset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.log10

data class TunerUiState(
    val mode: TunerMode = TunerMode.STRINGS,
    val tuning: TuningPreset = TuningPreset.STANDARD,
    val selectedString: Int? = null,
    val playingString: Int? = null,
    val referencePitch: Int = 440,
    val reading: TunerReading? = null,
    val isListening: Boolean = false,
    val signalLevel: Float = 0f,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val haptics: Boolean = true,
    val audioError: Boolean = false,
)

class TunerViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = TunerPreferences(application)
    private val tonePlayer = ReferenceTonePlayer(application)

    private val _uiState = MutableStateFlow(
        TunerUiState(
            mode = preferences.mode,
            tuning = preferences.tuning,
            selectedString = preferences.selectedString,
            referencePitch = preferences.referencePitch,
            themeMode = preferences.themeMode,
            dynamicColor = preferences.dynamicColor,
            haptics = preferences.haptics,
        ),
    )
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()

    private val recentFrequencies = ArrayDeque<Double>()
    private var smoothedFrequency = 0.0
    private var lastClarity = 0f
    private var lastSignalAt = 0L

    /**
     * Collects microphone frames until the calling scope is cancelled (for
     * example when the activity is paused). The recorder is released by the
     * flow's finally block.
     */
    @SuppressLint("MissingPermission")
    suspend fun listen() {
        _uiState.update { it.copy(isListening = true, audioError = false) }
        try {
            val engine = TunerEngine()
            AudioCapture().frames().collect { block ->
                // Never analyse the reference tone coming out of the speaker.
                if (_uiState.value.playingString != null) return@collect
                // YIN is O(N·tau); keep it off the main thread.
                val result = withContext(Dispatchers.Default) { engine.process(block) }
                if (result != null) {
                    onPitch(result)
                } else {
                    onNoPitch()
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            _uiState.update {
                it.copy(isListening = false, audioError = true, reading = null, signalLevel = 0f)
            }
        } finally {
            resetSmoothing()
            _uiState.update { it.copy(isListening = false, reading = null, signalLevel = 0f) }
        }
    }

    fun setMode(mode: TunerMode) {
        if (_uiState.value.mode == mode) return
        preferences.mode = mode
        _uiState.update { it.copy(mode = mode) }
        recalculate()
    }

    fun setTuning(tuning: TuningPreset) {
        if (_uiState.value.tuning == tuning) return
        preferences.tuning = tuning
        _uiState.update { it.copy(tuning = tuning) }
        recalculate()
    }

    /**
     * Auto follows the string being played. Turning it off pins the nearest
     * detected string so the user can choose manually.
     */
    fun setAutoDetect(enabled: Boolean) {
        if (enabled) {
            setSelectedString(null)
        } else if (_uiState.value.selectedString == null) {
            setSelectedString(_uiState.value.reading?.stringIndex ?: 0)
        }
    }

    /**
     * Tapping a string always plays its reference tone. It only changes the
     * tuning target while auto detection is off.
     */
    fun onStringTap(index: Int) {
        val state = _uiState.value
        if (state.mode != TunerMode.STRINGS || index !in state.tuning.midis.indices) return

        if (state.selectedString != null && state.selectedString != index) {
            preferences.selectedString = index
            _uiState.update { it.copy(selectedString = index) }
            recalculate()
        }

        resetSmoothing()
        val frequency = NoteMath.midiToFrequency(
            state.tuning.midis[index].toDouble(),
            state.referencePitch.toDouble(),
        )
        _uiState.update {
            it.copy(playingString = index, reading = null, signalLevel = 0f)
        }
        tonePlayer.play(frequency) {
            _uiState.update { current ->
                if (current.playingString == index) current.copy(playingString = null) else current
            }
        }
    }

    fun stopTone() {
        tonePlayer.stop()
        _uiState.update {
            if (it.playingString != null) it.copy(playingString = null) else it
        }
    }

    fun setSelectedString(index: Int?) {
        if (_uiState.value.selectedString == index) return
        preferences.selectedString = index
        _uiState.update { it.copy(selectedString = index) }
        recalculate()
    }

    fun setReferencePitch(value: Int) {
        val clamped = value.coerceIn(MIN_REFERENCE_PITCH, MAX_REFERENCE_PITCH)
        if (_uiState.value.referencePitch == clamped) return
        preferences.referencePitch = clamped
        _uiState.update { it.copy(referencePitch = clamped) }
        recalculate()
    }

    fun setThemeMode(mode: ThemeMode) {
        preferences.themeMode = mode
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        preferences.dynamicColor = enabled
        _uiState.update { it.copy(dynamicColor = enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        preferences.haptics = enabled
        _uiState.update { it.copy(haptics = enabled) }
    }

    fun clearAudioError() {
        _uiState.update { it.copy(audioError = false) }
    }

    private fun onPitch(result: PitchResult) {
        lastSignalAt = SystemClock.elapsedRealtime()
        lastClarity = result.clarity

        recentFrequencies.addLast(result.frequency)
        while (recentFrequencies.size > MEDIAN_WINDOW) recentFrequencies.removeFirst()
        val median = recentFrequencies.sorted()[recentFrequencies.size / 2]
        smoothedFrequency = if (smoothedFrequency == 0.0) {
            median
        } else {
            smoothedFrequency * (1 - SMOOTHING) + median * SMOOTHING
        }

        val reading = buildReading(smoothedFrequency, result.clarity)
        val level = ((20.0 * log10(result.rms.toDouble()) + 60.0) / 60.0)
            .coerceIn(0.0, 1.0)
            .toFloat()
        _uiState.update { it.copy(reading = reading, signalLevel = level) }
    }

    private fun onNoPitch() {
        if (SystemClock.elapsedRealtime() - lastSignalAt < SILENCE_TIMEOUT_MS) return
        if (_uiState.value.reading == null && _uiState.value.signalLevel == 0f) return
        resetSmoothing()
        _uiState.update { it.copy(reading = null, signalLevel = 0f) }
    }

    private fun buildReading(frequency: Double, clarity: Float): TunerReading {
        val state = _uiState.value
        val reference = state.referencePitch.toDouble()

        if (state.mode == TunerMode.CHROMATIC) {
            val nearest = NoteMath.nearestNote(frequency, reference)
            return TunerReading(
                frequency = frequency,
                clarity = clarity,
                midi = nearest.midi,
                cents = nearest.cents,
                targetFrequency = nearest.frequency,
                stringIndex = null,
            )
        }

        val index = state.selectedString ?: nearestStringIndex(frequency, state.tuning, reference)
        val targetMidi = state.tuning.midis[index]
        val targetFrequency = NoteMath.midiToFrequency(targetMidi.toDouble(), reference)
        return TunerReading(
            frequency = frequency,
            clarity = clarity,
            midi = targetMidi,
            cents = NoteMath.centsBetween(frequency, targetFrequency),
            targetFrequency = targetFrequency,
            stringIndex = index,
        )
    }

    private fun nearestStringIndex(
        frequency: Double,
        tuning: TuningPreset,
        reference: Double,
    ): Int {
        var bestIndex = 0
        var bestDistance = Double.MAX_VALUE
        tuning.midis.forEachIndexed { index, midi ->
            val distance = abs(NoteMath.centsBetween(frequency, NoteMath.midiToFrequency(midi.toDouble(), reference)))
            if (distance < bestDistance) {
                bestDistance = distance
                bestIndex = index
            }
        }
        return bestIndex
    }

    private fun recalculate() {
        if (smoothedFrequency <= 0.0) return
        val reading = buildReading(smoothedFrequency, lastClarity)
        _uiState.update { it.copy(reading = reading) }
    }

    override fun onCleared() {
        tonePlayer.release()
    }

    private fun resetSmoothing() {
        recentFrequencies.clear()
        smoothedFrequency = 0.0
        lastClarity = 0f
        lastSignalAt = 0L
    }

    private companion object {
        const val MEDIAN_WINDOW = 5
        const val SMOOTHING = 0.4
        const val SILENCE_TIMEOUT_MS = 900L
    }
}
