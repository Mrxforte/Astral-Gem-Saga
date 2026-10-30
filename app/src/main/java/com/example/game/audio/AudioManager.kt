package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

enum class SoundType {
    PIECE_SELECT,
    PIECE_SWAP,
    INVALID_SWAP,
    MATCH_3,
    MATCH_4,
    MATCH_5,
    COMBO_UP,
    LINE_BLAST,
    AREA_BOMB,
    COLOR_BOMB,
    SUPERNOVA,
    COIN_PICKUP,
    STAR_EARNED,
    VICTORY_FANFARE,
    DEFEAT_CHIME,
    BOOSTER_USE,
    SHUFFLE_SOUND,
    BUTTON_CLICK,
    CHEST_OPEN
}

class AudioManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var isMuted: Boolean = false
    var masterVolume: Float = 1.0f
    var sfxVolume: Float = 0.9f
    var musicVolume: Float = 0.6f

    private var musicJob: Job? = null
    private var isMusicPlaying = false

    private val sampleRate = 22050
    private val soundBuffers = ConcurrentHashMap<SoundType, ShortArray>()

    init {
        // Pre-render core sound effects into memory buffers for zero-latency playback
        scope.launch {
            preRenderSounds()
        }
    }

    private fun preRenderSounds() {
        soundBuffers[SoundType.BUTTON_CLICK] = generateTone(800.0, 0.04, 0.4f, ToneType.SINE)
        soundBuffers[SoundType.PIECE_SELECT] = generateTone(1200.0, 0.06, 0.5f, ToneType.CHIME)
        soundBuffers[SoundType.PIECE_SWAP] = generateSweep(500.0, 900.0, 0.08, 0.5f)
        soundBuffers[SoundType.INVALID_SWAP] = generateThud(140.0, 0.12, 0.6f)
        soundBuffers[SoundType.MATCH_3] = generateChord(listOf(523.25, 659.25, 783.99), 0.18, 0.7f) // C Major chime
        soundBuffers[SoundType.MATCH_4] = generateChord(listOf(587.33, 739.99, 880.0, 1174.66), 0.25, 0.8f) // D Major energetic
        soundBuffers[SoundType.MATCH_5] = generateChord(listOf(659.25, 830.61, 987.77, 1318.51, 1661.22), 0.35, 0.9f) // E Major celestial
        soundBuffers[SoundType.LINE_BLAST] = generateLaserSweep(1800.0, 300.0, 0.25, 0.8f)
        soundBuffers[SoundType.AREA_BOMB] = generateExplosion(0.35, 0.9f)
        soundBuffers[SoundType.COLOR_BOMB] = generateArpeggio(listOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98), 0.04, 0.85f)
        soundBuffers[SoundType.SUPERNOVA] = generateSupernova(0.6)
        soundBuffers[SoundType.COIN_PICKUP] = generateTone(1800.0, 0.08, 0.6f, ToneType.BELL)
        soundBuffers[SoundType.STAR_EARNED] = generateChord(listOf(880.0, 1108.73, 1318.51), 0.3, 0.8f)
        soundBuffers[SoundType.VICTORY_FANFARE] = generateFanfare()
        soundBuffers[SoundType.DEFEAT_CHIME] = generateDefeat()
        soundBuffers[SoundType.BOOSTER_USE] = generateSweep(400.0, 1400.0, 0.22, 0.8f)
        soundBuffers[SoundType.SHUFFLE_SOUND] = generateShuffle()
        soundBuffers[SoundType.CHEST_OPEN] = generateChestReveal()
    }

    fun playSound(type: SoundType, pitchMultiplier: Float = 1.0f) {
        if (isMuted || masterVolume <= 0f || sfxVolume <= 0f) return

        scope.launch {
            try {
                val buffer = soundBuffers[type] ?: return@launch
                val adjustedBuffer = if (pitchMultiplier != 1.0f) {
                    resample(buffer, pitchMultiplier)
                } else {
                    buffer
                }
                playBuffer(adjustedBuffer, masterVolume * sfxVolume)
            } catch (_: Exception) {
                // Ignore audio device transient issues
            }
        }
    }

    fun playComboSound(comboCount: Int) {
        // Pentatonic pitch escalation with harmonic accents
        val pitches = listOf(1.0f, 1.122f, 1.26f, 1.414f, 1.587f, 1.782f, 2.0f, 2.24f)
        val pitch = pitches.getOrElse(comboCount.coerceAtLeast(1) - 1) { 2.24f }
        if (comboCount >= 5) {
            playSound(SoundType.MATCH_5, pitch)
        } else if (comboCount >= 3) {
            playSound(SoundType.MATCH_4, pitch)
        } else {
            playSound(SoundType.MATCH_3, pitch)
        }
    }

    var currentWorldMusicId: Int = 1

    fun startWorldMusic(worldId: Int = 1) {
        currentWorldMusicId = worldId
        if (isMusicPlaying) {
            // Already playing, update track
            musicJob?.cancel()
            isMusicPlaying = false
        }
        startBackgroundMusic()
    }

    fun startBackgroundMusic() {
        if (isMusicPlaying) return
        isMusicPlaying = true
        musicJob?.cancel()

        musicJob = scope.launch {
            // World-specific musical scales & arpeggios
            val worldNotes = when (currentWorldMusicId) {
                1 -> listOf( // Crystal Valley: Glistening crystalline arpeggios
                    listOf(523.25, 659.25, 783.99, 1046.50),
                    listOf(440.00, 523.25, 659.25, 880.00),
                    listOf(349.23, 440.00, 523.25, 698.46),
                    listOf(392.00, 493.88, 587.33, 783.99)
                )
                2 -> listOf( // Mystic Forest: Celtic woodland flute arpeggios
                    listOf(440.00, 523.25, 587.33, 659.25),
                    listOf(392.00, 440.00, 523.25, 587.33),
                    listOf(349.23, 392.00, 440.00, 523.25),
                    listOf(329.63, 392.00, 440.00, 493.88)
                )
                3 -> listOf( // Sky Islands: Bright aerial winds
                    listOf(587.33, 739.99, 880.00, 1174.66),
                    listOf(493.88, 587.33, 739.99, 987.77),
                    listOf(440.00, 554.37, 659.25, 880.00),
                    listOf(587.33, 659.25, 880.00, 1174.66)
                )
                4 -> listOf( // Volcanic Kingdom: Deep magma resonance
                    listOf(110.00, 130.81, 164.81, 220.00),
                    listOf(123.47, 146.83, 185.00, 246.94),
                    listOf(98.00, 123.47, 146.83, 196.00),
                    listOf(110.00, 146.83, 164.81, 220.00)
                )
                5 -> listOf( // Frozen Realm: Ethereal glacial glass bells
                    listOf(880.00, 1046.50, 1318.51, 1760.00),
                    listOf(783.99, 987.77, 1174.66, 1567.98),
                    listOf(698.46, 880.00, 1046.50, 1396.91),
                    listOf(659.25, 783.99, 987.77, 1318.51)
                )
                6 -> listOf( // Ocean Temple: Deep underwater acoustic pads
                    listOf(293.66, 369.99, 440.00, 587.33),
                    listOf(246.94, 329.63, 392.00, 493.88),
                    listOf(220.00, 277.18, 329.63, 440.00),
                    listOf(261.63, 329.63, 392.00, 523.25)
                )
                7 -> listOf( // Ancient Ruins: Solar mystic chants
                    listOf(329.63, 349.23, 392.00, 493.88),
                    listOf(293.66, 329.63, 369.99, 440.00),
                    listOf(261.63, 329.63, 392.00, 493.88),
                    listOf(329.63, 392.00, 440.00, 523.25)
                )
                8 -> listOf( // Moon Kingdom: Night sky lullaby
                    listOf(349.23, 440.00, 523.25, 659.25),
                    listOf(329.63, 392.00, 493.88, 587.33),
                    listOf(261.63, 329.63, 392.00, 523.25),
                    listOf(293.66, 369.99, 440.00, 587.33)
                )
                9 -> listOf( // Dragon Lands: Heroic brass fanfare
                    listOf(220.00, 293.66, 329.63, 440.00),
                    listOf(246.94, 329.63, 369.99, 493.88),
                    listOf(196.00, 246.94, 293.66, 392.00),
                    listOf(220.00, 277.18, 329.63, 440.00)
                )
                else -> listOf( // Celestial Realm: Cosmic multi-octave harmony
                    listOf(440.00, 554.37, 659.25, 880.00, 1108.73),
                    listOf(392.00, 493.88, 587.33, 783.99, 987.77),
                    listOf(329.63, 440.00, 493.88, 659.25, 880.00),
                    listOf(369.99, 440.00, 554.37, 739.99, 880.00)
                )
            }

            var chordIndex = 0

            while (isActive && isMusicPlaying) {
                if (!isMuted && masterVolume > 0f && musicVolume > 0f) {
                    val chord = worldNotes[chordIndex % worldNotes.size]
                    chordIndex++
                    for (freq in chord) {
                        if (!isActive || !isMusicPlaying) break
                        val noteBuf = generateTone(freq, 0.42, 0.24f * masterVolume * musicVolume, ToneType.AMBIENT)
                        playBuffer(noteBuf, masterVolume * musicVolume)
                        delay(270)
                    }
                } else {
                    delay(500)
                }
            }
        }
    }

    fun stopBackgroundMusic() {
        isMusicPlaying = false
        musicJob?.cancel()
        musicJob = null
    }

    private fun playBuffer(buffer: ShortArray, volume: Float) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.setVolume(volume.coerceIn(0f, 1f))
        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        // Schedule release after playback finishes
        val durationMs = (buffer.size * 1000L / sampleRate) + 50
        scope.launch {
            delay(durationMs)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    // --- Sound Synthesis Helpers ---

    private enum class ToneType { SINE, CHIME, BELL, AMBIENT }

    private fun generateTone(frequency: Double, durationSec: Double, volume: Float, type: ToneType): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val envelope = when (type) {
                ToneType.SINE -> (1.0 - progress)
                ToneType.CHIME -> exp(- progress * 6.0)
                ToneType.BELL -> exp(- progress * 4.0) * (1.0 + 0.3 * sin(2.0 * PI * 18.0 * t))
                ToneType.AMBIENT -> sin(progress * PI)
            }
            val sample = sin(2.0 * PI * frequency * t) * envelope
            buffer[i] = (sample * volume * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateSweep(startFreq: Double, endFreq: Double, durationSec: Double, volume: Float): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * currentFreq / sampleRate
            val envelope = sin(progress * PI)
            val sample = sin(phase) * envelope
            buffer[i] = (sample * volume * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateThud(freq: Double, durationSec: Double, volume: Float): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val t = i.toDouble() / sampleRate
            val currentFreq = freq * (1.0 - progress * 0.5)
            val envelope = exp(- progress * 10.0)
            val sample = sin(2.0 * PI * currentFreq * t) * envelope
            buffer[i] = (sample * volume * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateChord(freqs: List<Double>, durationSec: Double, volume: Float): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val envelope = exp(- progress * 4.5)
            var sampleSum = 0.0
            for (f in freqs) {
                sampleSum += sin(2.0 * PI * f * t)
            }
            sampleSum = (sampleSum / freqs.size) * envelope
            buffer[i] = (sampleSum * volume * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateLaserSweep(startFreq: Double, endFreq: Double, durationSec: Double, volume: Float): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = startFreq * (endFreq / startFreq).pow(progress)
            phase += 2.0 * PI * freq / sampleRate
            val envelope = exp(- progress * 3.0)
            val sample = sin(phase) * envelope
            buffer[i] = (sample * volume * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateExplosion(durationSec: Double, volume: Float): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val rnd = java.util.Random(42)
        var lowPass = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val whiteNoise = (rnd.nextDouble() * 2.0 - 1.0)
            lowPass += (whiteNoise - lowPass) * 0.15 // Low pass filter for deep bass rumble
            val envelope = exp(- progress * 5.0)
            val sample = lowPass * envelope
            buffer[i] = (sample * volume * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateSupernova(durationSec: Double): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val rnd = java.util.Random(99)
        var lowPass = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val t = i.toDouble() / sampleRate
            val whiteNoise = (rnd.nextDouble() * 2.0 - 1.0)
            lowPass += (whiteNoise - lowPass) * 0.2
            val shimmer = sin(2.0 * PI * (440.0 + 880.0 * sin(progress * PI)) * t)
            val envelope = (1.0 - progress) * (1.0 + 0.4 * sin(progress * 16.0 * PI))
            val sample = (lowPass * 0.6 + shimmer * 0.4) * envelope
            buffer[i] = (sample * 0.95 * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateArpeggio(notes: List<Double>, noteDurationSec: Double, volume: Float): ShortArray {
        val totalSamples = (notes.size * noteDurationSec * sampleRate).toInt()
        val buffer = ShortArray(totalSamples)
        val samplesPerNote = (noteDurationSec * sampleRate).toInt()

        for ((idx, freq) in notes.withIndex()) {
            val offset = idx * samplesPerNote
            for (i in 0 until samplesPerNote) {
                if (offset + i >= totalSamples) break
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samplesPerNote
                val env = exp(- progress * 3.0)
                val sample = sin(2.0 * PI * freq * t) * env
                buffer[offset + i] = (sample * volume * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
            }
        }
        return buffer
    }

    private fun generateFanfare(): ShortArray {
        val notes = listOf(523.25, 659.25, 783.99, 1046.50)
        return generateArpeggio(notes, 0.12, 0.9f)
    }

    private fun generateDefeat(): ShortArray {
        val notes = listOf(440.0, 415.3, 392.0, 349.23)
        return generateArpeggio(notes, 0.15, 0.8f)
    }

    private fun generateShuffle(): ShortArray {
        return generateSweep(300.0, 900.0, 0.25, 0.7f)
    }

    private fun generateChestReveal(): ShortArray {
        val notes = listOf(392.0, 523.25, 659.25, 783.99, 1046.50, 1318.51)
        return generateArpeggio(notes, 0.08, 0.9f)
    }

    private fun resample(original: ShortArray, pitchFactor: Float): ShortArray {
        val newLength = (original.size / pitchFactor).toInt().coerceAtLeast(1)
        val result = ShortArray(newLength)
        for (i in 0 until newLength) {
            val originalIndex = (i * pitchFactor).toInt().coerceIn(0, original.size - 1)
            result[i] = original[originalIndex]
        }
        return result
    }
}
