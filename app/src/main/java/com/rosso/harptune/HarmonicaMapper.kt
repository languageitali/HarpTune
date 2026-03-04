package com.rosso.harptune

import kotlin.math.abs

data class NoteInfo(
    val noteName: String,
    val cell: Int,
    val isDraw: Boolean,
    val theoreticalFreq: Float
)

object HarmonicaMapper {
    // Tabla completa Richter para Armónica en C (Do)
    private val harmonicaTable = listOf(
        NoteInfo("C3", 1, false, 130.81f),
        NoteInfo("D3", 1, true, 146.83f),
        NoteInfo("E3", 2, false, 164.81f),
        NoteInfo("G3", 2, true, 196.00f),
        NoteInfo("G3", 3, false, 196.00f),
        NoteInfo("B3", 3, true, 246.94f),
        NoteInfo("C4", 4, false, 261.63f),
        NoteInfo("D4", 4, true, 293.66f),
        NoteInfo("E4", 5, false, 329.63f),
        NoteInfo("F4", 5, true, 349.23f),
        NoteInfo("G4", 6, false, 392.00f),
        NoteInfo("A4", 6, true, 440.00f),
        NoteInfo("C5", 7, false, 523.25f),
        NoteInfo("B4", 7, true, 493.88f),
        NoteInfo("E5", 8, false, 659.25f),
        NoteInfo("D5", 8, true, 587.33f),
        NoteInfo("G5", 9, false, 783.99f),
        NoteInfo("F5", 9, true, 698.46f),
        NoteInfo("C6", 10, false, 1046.50f),
        NoteInfo("A5", 10, true, 880.00f)
    )

    fun mapFrequencyToNote(freq: Float): NoteInfo? {
        if (freq < 40f) return null // Umbral de ruido
        return harmonicaTable.minByOrNull { abs(it.theoreticalFreq - freq) }
    }
}