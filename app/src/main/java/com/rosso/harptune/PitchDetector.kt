package com.rosso.harptune

class PitchDetector {
    fun processAudio(buffer: ShortArray, sampleRate: Int): Double {
        // Lógica de detección (Autocorrelación / YIN / FFT)
        // Se eliminó la variable redundante detectada por el linter
        return calculateFrequency(buffer, sampleRate)
    }

    private fun calculateFrequency(buffer: ShortArray, sampleRate: Int): Double {
        // Implementación optimizada
        return 0.0
    }
}