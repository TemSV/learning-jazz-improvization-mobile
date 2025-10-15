package com.example.learning_jazz_app.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.learning_jazz_app.domain.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin

class PhrasePlayer {

    private val sampleRate = 44100 // Samples per second
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private var audioTrack: AudioTrack? = null

    suspend fun play(notes: List<Note>, onCompletion: () -> Unit) = withContext(Dispatchers.Default) {
        if (notes.isEmpty()) {
            onCompletion()
            return@withContext
        }

        stop() // Stop any previous playback

        // 1. Normalize notes to start at time 0 and have volume between 0.0 and 1.0
        val minOnset = notes.minOf { it.onset }
        val maxVolume = notes.maxOfOrNull { it.volume } ?: 1.0
        val minVolume = notes.minOfOrNull { it.volume } ?: 0.0

        val normalizedNotes = notes.map {
            val normalizedVolume = if ((maxVolume - minVolume) > 0) {
                // Scale volume to be between 0.2 and 0.8 to avoid clicks and be audible
                0.2 + ((it.volume - minVolume) / (maxVolume - minVolume)) * 0.6
            } else {
                0.5 // Default volume if all notes have the same volume
            }
            it.copy(
                onset = it.onset - minOnset,
                volume = normalizedVolume
            )
        }

        // 2. Calculate total duration and create PCM buffer
        val totalDuration = normalizedNotes.maxOfOrNull { it.onset + it.duration } ?: 0.0
        val totalSamples = (totalDuration * sampleRate).toInt()
        if (totalSamples <= 0) return@withContext
        val pcmData = ShortArray(totalSamples)

        // 3. Generate sine wave for each note and add to buffer
        normalizedNotes.forEach { note ->
            val pitchInHz = 440.0 * Math.pow(2.0, (note.pitch - 69.0) / 12.0)
            val startSample = (note.onset * sampleRate).toInt()
            val numSamples = (note.duration * sampleRate).toInt()
            val amplitude = (note.volume * Short.MAX_VALUE).toInt()

            for (i in 0 until numSamples) {
                val sampleIndex = startSample + i
                if (sampleIndex < totalSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / pitchInHz)
                    val sampleValue = (amplitude * sin(angle)).toInt()

                    // Add to buffer, preventing overflow
                    val currentValue = pcmData[sampleIndex].toInt()
                    val newValue = currentValue + sampleValue
                    pcmData[sampleIndex] = when {
                        newValue > Short.MAX_VALUE -> Short.MAX_VALUE
                        newValue < Short.MIN_VALUE -> Short.MIN_VALUE
                        else -> newValue.toShort()
                    }
                }
            }
        }

        // 4. Setup and play AudioTrack
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack?.let {
            it.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(track: AudioTrack?) {
                    // This is called when the playback reaches the notification marker.
                    onCompletion()
                    stop()
                }

                override fun onPeriodicNotification(track: AudioTrack?) {
                    // Not used for now
                }
            })
            it.write(pcmData, 0, totalSamples)
            // Set a notification marker at the end of the track
            it.setNotificationMarkerPosition(totalSamples)
            it.play()
        }
    }

    fun stop() {
        audioTrack?.let {
            if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                it.stop()
            }
            it.release()
        }
        audioTrack = null
    }
} 