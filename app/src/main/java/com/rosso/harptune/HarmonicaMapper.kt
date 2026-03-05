package com.rosso.harptune

import android.util.Log
import kotlin.math.log2
import kotlin.math.roundToInt

/**
 * HarmonicaMapper: Bridge JNI y Motor de Mapeo de Frecuencias.
 * Arquitectura optimizada para procesamiento de señales en tiempo real (2026).
 */
class HarmonicaMapper {

    companion object {
        private const val TAG = "HarmonicaMapper"
        private const val REFERENCE_PITCH = 440.0 // A4
        private const val MIDI_A4 = 69

        private val NOTE_NAMES = arrayOf(
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
        )

        init {
            try {
                // El nombre de la librería debe coincidir con el definido en CMakeLists.txt
                System.loadLibrary("harptune-native")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Error crítico: No se pudo cargar la librería nativa. Detalle: ${e.message}")
            }
        }
    }

    /**
     * Punto de entrada JNI.
     * Invoca la implementación en C++ (native-lib.cpp) para realizar el análisis de pitch.
     * @param buffer Bloque de muestras PCM Float32.
     * @return Frecuencia fundamental detectada en Hz.
     */
    external fun processFrequency(buffer: FloatArray): Float

    /**
     * Transduce una frecuencia a una nota musical mediante la fórmula de temperamento igual.
     * Optimizado para reducir la latencia de interpretación en UI.
     * * f = 440 * 2^((n - 69) / 12) => n = 69 + 12 * log2(f / 440)
     */
    fun getNoteFromFrequency(frequency: Float): String {
        if (frequency <= 0) return "---"

        val midiNote = (MIDI_A4 + 12 * log2(frequency.toDouble() / REFERENCE_PITCH)).roundToInt()
        val octave = (midiNote / 12) - 1
        val noteIndex = midiNote % 12

        return "${NOTE_NAMES[noteIndex]}$octave"
    }

    /**
     * Determina la desviación en centésimas (cents) respecto a la nota ideal.
     */
    fun getCentsOff(frequency: Float): Double {
        if (frequency <= 0) return 0.0
        val midiNote = (MIDI_A4 + 12 * log2(frequency.toDouble() / REFERENCE_PITCH)).roundToInt()
        val targetFreq = REFERENCE_PITCH * Math.pow(2.0, (midiNote - MIDI_A4) / 12.0)
        return 1200 * log2(frequency.toDouble() / targetFreq)
    }
}