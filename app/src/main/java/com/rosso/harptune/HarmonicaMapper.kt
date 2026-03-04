package com.rosso.harptune

import kotlin.math.abs
import kotlin.math.log2

data class HarmonicaAction(val hole: Int, val isBlow: Boolean, val noteName: String)

object HarmonicaMapper {
    
    // Tabla Maestra según especificaciones técnicas
    private val masterTable = listOf(
        HarmonicaAction(1, true, "C4") to 261.63,
        HarmonicaAction(1, false, "D4") to 293.66,
        HarmonicaAction(2, true, "E4") to 329.63,
        HarmonicaAction(2, false, "G4") to 392.00,
        HarmonicaAction(3, true, "G4") to 392.00, 
        HarmonicaAction(3, false, "B4") to 493.88,
        HarmonicaAction(4, true, "C5") to 523.25,
        HarmonicaAction(4, false, "D5") to 587.33,
        HarmonicaAction(5, true, "E5") to 659.25,
        HarmonicaAction(5, false, "F5") to 698.46,
        HarmonicaAction(6, true, "G5") to 783.99,
        HarmonicaAction(6, false, "A5") to 880.00,
        HarmonicaAction(7, true, "C6") to 1046.50,
        HarmonicaAction(7, false, "B5") to 987.77,
        HarmonicaAction(8, true, "E6") to 1318.51,
        HarmonicaAction(8, false, "D6") to 1174.66,
        HarmonicaAction(9, true, "G6") to 1567.98,
        HarmonicaAction(9, false, "F6") to 1396.91,
        HarmonicaAction(10, true, "C7") to 2093.00,
        HarmonicaAction(10, false, "A6") to 1760.00
    )

    // Aumentamos a 50 cents para mayor flexibilidad con armónicas reales
    private const val TOLERANCE_CENTS = 50.0

    private fun centsDifference(f1: Double, f2: Double): Double {
        if (f1 <= 0 || f2 <= 0) return 1000.0
        return abs(1200.0 * log2(f2 / f1))
    }

    fun getHarmonicaAction(capturedFreq: Double): HarmonicaAction? {
        if (capturedFreq <= 0) return null

        // Compensación para voz grave del usuario
        val freqToCompare = if (capturedFreq in 80.0..120.0) capturedFreq * 4.0 else capturedFreq

        var bestMatch: HarmonicaAction? = null
        var minCents = Double.MAX_VALUE

        for ((action, targetFreq) in masterTable) {
            val diff = centsDifference(freqToCompare, targetFreq)
            if (diff < minCents) {
                minCents = diff
                bestMatch = action
            }
        }

        // Si la nota detectada es G4 (392Hz), priorizamos la celda 3 si el usuario lo prefiere
        if (bestMatch?.noteName == "G4" && minCents <= TOLERANCE_CENTS) {
            // Retornamos 3↑ para Sol4
            return HarmonicaAction(3, true, "G4")
        }

        return if (minCents <= TOLERANCE_CENTS) bestMatch else null
    }

    fun frequencyToNote(frequency: Double): String? {
        val action = getHarmonicaAction(frequency)
        return action?.noteName
    }
}
