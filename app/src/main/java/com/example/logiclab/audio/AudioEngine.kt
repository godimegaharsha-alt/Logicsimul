package com.example.logiclab.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Real-time hardware audio tone generator and piezo buzzer sound engine.
 * Synthesizes electronic square waves and click transients for:
 * 1. Digital Oscillators (toggling clock tones)
 * 2. Piezo Buzzer logic components
 * 3. Switch toggles and logic gate click haptics
 */
object AudioEngine {
    private const val TAG = "AudioEngine"
    private const val SAMPLE_RATE = 22050

    var isMuted = false
    private var isPlayingTone = false
    private var toneTrack: AudioTrack? = null
    private var toneJob: Job? = null
    private val audioScope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays a crisp high-tech mechanical click sound on switch toggle.
     */
    fun playClickSound() {
        if (isMuted) return
        audioScope.launch {
            try {
                val numSamples = (SAMPLE_RATE * 0.025f).toInt() // 25ms click
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val env = (1.0f - progress) * (1.0f - progress)
                    // High-pass chirp 1800Hz to 600Hz
                    val freq = 1800.0 - progress * 1200.0
                    val wave = sin(2.0 * Math.PI * i * freq / SAMPLE_RATE)
                    buffer[i] = (wave * env * 18000).toInt().toShort()
                }
                playShortBuffer(buffer)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing click", e)
            }
        }
    }

    /**
     * Plays a short pulse beep when a gate output toggles or logic level changes.
     */
    fun playLogicBeep(frequencyHz: Float = 880f, durationMs: Long = 40) {
        if (isMuted) return
        audioScope.launch {
            try {
                val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val env = 1.0f - progress
                    // Square wave with slight smoothing
                    val phase = (i * frequencyHz / SAMPLE_RATE) % 1.0f
                    val square = if (phase < 0.5f) 1.0 else -1.0
                    buffer[i] = (square * env * 12000).toInt().toShort()
                }
                playShortBuffer(buffer)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing logic beep", e)
            }
        }
    }

    /**
     * Starts continuous tone for active Piezo Buzzer or live Digital Oscillator.
     */
    fun setBuzzerActive(active: Boolean, frequencyHz: Float = 520f) {
        if (isMuted || !active) {
            stopContinuousTone()
            return
        }
        if (isPlayingTone) return

        isPlayingTone = true
        toneJob?.cancel()
        toneJob = audioScope.launch {
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(1024)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                toneTrack = track
                track.play()

                val waveBuffer = ShortArray(bufferSize)
                var sampleIndex = 0L

                while (isActive && isPlayingTone && !isMuted) {
                    for (i in 0 until bufferSize) {
                        val phase = ((sampleIndex + i) * frequencyHz / SAMPLE_RATE) % 1.0f
                        // Distinctive electronic square wave for digital logic tone
                        val wave = if (phase < 0.45f) 0.85 else -0.85
                        waveBuffer[i] = (wave * 9000).toInt().toShort()
                    }
                    sampleIndex += bufferSize
                    track.write(waveBuffer, 0, bufferSize)
                }

                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error in continuous tone", e)
            } finally {
                isPlayingTone = false
            }
        }
    }

    fun stopContinuousTone() {
        isPlayingTone = false
        toneJob?.cancel()
        try {
            toneTrack?.stop()
            toneTrack?.release()
        } catch (_: Exception) {}
        toneTrack = null
    }

    private fun playShortBuffer(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            audioScope.launch {
                delay(300)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing static buffer", e)
        }
    }
}
